package org.egov.migrationkit.service;

import java.util.List;

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

@Service
@Transactional
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
				WaterConnection conn = objectMapper.readValue(json, WaterConnection.class);
 
//				String str = requestInfo.replace("\"RequestInfo\":", "");
//				RequestInfo info = objectMapper.readValue(str, RequestInfo.class);
 
				recordService.recordWaterMigration(conn);
 				conn.setPropertyId(propertyService.findProperty(conn));
				conn.setTenantId(requestInfo.getUserInfo().getTenantId());
				WaterConnectionRequest wcr = new WaterConnectionRequest();
				ProcessInstance ps = new ProcessInstance();
				ps.setAction("INITIATE");
				conn.setProcessInstance(ps);

				wcr.setWaterConnection(conn);
				wcr.setRequestInfo(requestInfo);

				String ss = "{" + requestInfo + ", \"waterConnection\": " + json + " }";

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<String> requests = new HttpEntity<String>(ss, headers);
				System.out.println("request: " + ss);
				Object[] o = new Object[1];

				String response = restTemplate.postForObject(host + "/" + waterUrl, wcr, String.class);

				System.out.println("Response=" + response);

				WaterConnectionResponse waterResponse=	objectMapper.readValue(response, WaterConnectionResponse.class);
		       
				recordService.updateWaterMigration(waterResponse.getWaterConnection().get(0));
				System.out.println("waterResponse" + waterResponse);
				
				Demand demand = new Demand();

			} catch (JsonMappingException e) {
				 System.err.println(e.getMessage());
			} catch (JsonProcessingException e) {
				 System.err.println(e.getMessage()); 
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

}
