package org.egov.migrationkit.service;

import java.util.List;
import java.util.Map;

import org.egov.migrationkit.constants.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.Demand;
import io.swagger.client.model.ProcessInstance;
import io.swagger.client.model.RequestInfo;
import io.swagger.client.model.WaterConnection;
import io.swagger.client.model.WaterConnectionRequest;
import io.swagger.client.model.WaterConnectionResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ConnectionService {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@JsonProperty("host")
	@Value("${egov.services.hosturl}")
	private String host = null;

	@Value("${egov.services.water.url}")
	private String waterUrl = null;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PropertyService propertyService;
	@Autowired
	private RecordService recordService;

	public void migrate(String tenantId, RequestInfo requestInfo) {

		jdbcTemplate.execute("set search_path to " + tenantId);
		
		jdbcTemplate.execute(Sqls.waterRecord_table);

		List<String> queryForList = jdbcTemplate.queryForList(Sqls.waterQuery, String.class);

		for (String json : queryForList) {

			try {
				
				Map data = objectMapper.readValue(json, Map.class);
				
				WaterConnection connection = mapWaterConnection(data);
				connection.setPropertyId(propertyService.findProperty(connection));
				connection.setTenantId(requestInfo.getUserInfo().getTenantId());
				connection.setProcessInstance(ProcessInstance.builder().action("INITIATE").build());
				
//				String str = requestInfo.replace("\"RequestInfo\":", "");
//				RequestInfo info = objectMapper.readValue(str, RequestInfo.class);
 
				recordService.recordWaterMigration(connection);
 				
				WaterConnectionRequest waterRequest = new WaterConnectionRequest();
				

				waterRequest.setWaterConnection(connection);
				waterRequest.setRequestInfo(requestInfo);

				String ss = "{" + requestInfo + ", \"waterConnection\": " + waterRequest + " }";

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<String> requests = new HttpEntity<String>(ss, headers);
				log.info("request: " + ss);
				Object[] o = new Object[1];

				String response = restTemplate.postForObject(host + "/" + waterUrl, waterRequest, String.class);

				System.out.println("Response=" + response);

				WaterConnectionResponse waterResponse=	objectMapper.readValue(response, WaterConnectionResponse.class);
		       
				recordService.updateWaterMigration(waterResponse.getWaterConnection().get(0));
				System.out.println("waterResponse" + waterResponse);
				
				Demand demand = new Demand();

			} catch (JsonMappingException e) {
				log.error(e.getMessage());
			} catch (JsonProcessingException e) {
				log.error(e.getMessage()); 
			}

		}
		System.out.println("Migration completed for "+tenantId);
	}

	private String getRequestInfoString() {

		StringBuffer buf = new StringBuffer(1000);

		return "";

	}

	public void migratev2(String tenantId, RequestInfo requestInfo) {
		 
		
	}
	
	private WaterConnection mapWaterConnection(Map data) {
		WaterConnection waterConnection = WaterConnection.builder()
				.actualTaps(Double.valueOf((Integer)data.get("actualTaps")))
				.proposedTaps(Double.valueOf((Integer)data.get("proposedTaps")))
				.proposedPipeSize((Double)data.get("proposedPipeSize"))
				.actualPipeSize((Double)data.get("actualPipeSize"))
				.waterSource((String) data.get("waterSource"))
				//.connectionNo((String) data.get("consumercode"))
				.connectionCategory((String) data.get("propertytype"))
				.connectionExecutionDate((Long)data.get("executiondate"))
				.build();
		List<Map> roadCategoryList = (List<Map>) data.get("road_category");
		if (roadCategoryList != null) {
			String roadCategory = (String) roadCategoryList.get(0).get("road_name");
			waterConnection.setRoadType(WSConstants.DIGIT_ROAD_CATEGORIES.get(roadCategory));
			waterConnection.setRoadCuttingArea(Float.valueOf((Integer)roadCategoryList.get(0).get("road_area")));

		}

//		.propertyId()
//		.connectionNo()
		return waterConnection;

	}

}
