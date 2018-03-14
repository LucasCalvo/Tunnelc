package com.tinamica.tunnelc.publisherdata.data;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.tinamica.tunnelc.global.beans.Ack;
import com.tinamica.tunnelc.global.beans.railwayincidences.TrackTracerGeoIncidences;
import com.tinamica.tunnelc.publisherdata.exceptions.TCEDCPublisherDataServiceError;
import com.tinamica.tunnelc.publisherdata.exceptions.TCEDCPublisherDataServiceException;
import com.tinamica.tunnelc.publisherdata.utils.ServiceUtils;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.TableId;

@Repository
public class TCBigQueryWrapper {

	private StringBuffer sb;

	private static String _component = "[TCBigQueryWrapper]";

	/** Logger Object **/
	private static Logger _logger = LoggerFactory.getLogger(TCBigQueryWrapper.class);

	public Ack consolidateBigQuery(TrackTracerGeoIncidences trackTracerGeoIncidences) throws Exception {
		Ack ack = new Ack();
		BigQuery bigquery = ServiceUtils.getBigQuery();

		try {
			_logger.info(
					"[........TCEDCPublisherDataService:TCBigQueryWrapper:consolidateBigQuery .......Init Sequence]");
			_logger.info(trackTracerGeoIncidences.toString());

			TableId tableId = TableId.of(ServiceUtils._DATASET_NAME, ServiceUtils._TABLE_ID);

			Map<String, Object> rowContent = new HashMap<>();
			rowContent.put("pk", trackTracerGeoIncidences.getPK());
			rowContent.put("parameter", trackTracerGeoIncidences.getParameter());
			rowContent.put("value", trackTracerGeoIncidences.getValue());
			rowContent.put("comment", trackTracerGeoIncidences.getComment());
			rowContent.put("tunnelName", trackTracerGeoIncidences.getTunnelname());
			rowContent.put("date", trackTracerGeoIncidences.getDate());
			rowContent.put("direction", trackTracerGeoIncidences.getDirection());
			rowContent.put("index", trackTracerGeoIncidences.getIndex());

			bigquery.insertAll(InsertAllRequest.newBuilder(tableId).addRow(rowContent).build());

			_logger.info(
					"[........TCEDCPublisherDataService:TCBigQueryWrapper:consolidateBigQuery .......Ends Sequence]");

			ack.setStreamingEvent(trackTracerGeoIncidences.toString());
			ack.setService("[TCEDCPublisherIncidenceService]");
			ack.setComponent(_component);
			ack.setUser("tunnelc");
			ack.setMsgOk("msgOk");
			ack.setIsOk("isOk");
		} catch (TCEDCPublisherDataServiceException e) {
			_logger.error("ERROR .: " + e.fillInStackTrace());
			sb = new StringBuffer();
			TCEDCPublisherDataServiceError error = new TCEDCPublisherDataServiceError(
					this.getClass().getName(), String.valueOf(TCEDCPublisherDataServiceException.ERROR),
					sb.append("Error .: ").append(e.fillInStackTrace()).toString(), e.getCause());
			throw new TCEDCPublisherDataServiceException(error, _component + "[01]", "[consolidateBigQuery]",
					TCEDCPublisherDataServiceException.ERROR, e);
		}
		return ack;
	}
}
