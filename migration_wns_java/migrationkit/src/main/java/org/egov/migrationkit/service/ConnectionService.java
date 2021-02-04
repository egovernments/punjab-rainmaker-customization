package org.egov.migrationkit.service;

import java.util.List;
import java.util.Map;

import org.egov.migrationkit.constants.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.Address;
import io.swagger.client.model.Demand;
import io.swagger.client.model.Locality;
import io.swagger.client.model.ProcessInstance;
import io.swagger.client.model.Property;
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
	
	@Autowired
	private DemandService demandService;
	

	public void migrateWtrConnection(String tenantId, RequestInfo requestInfo) {

		jdbcTemplate.execute("set search_path to " + tenantId);
		
		jdbcTemplate.execute(Sqls.waterRecord_table);

		List<String> queryForList = jdbcTemplate.queryForList(Sqls.waterQuery, String.class);

		for (String json : queryForList) {

			try {
				
				Map data = objectMapper.readValue(json, Map.class);
				
			//	WaterConnection connection = mapWaterConnection(data);
				WaterConnection connection=	objectMapper.readValue(json, WaterConnection.class);
				
				List<Map> roadCategoryList = (List<Map>) data.get("road_category");
				if (roadCategoryList != null) {
					String roadCategory = (String) roadCategoryList.get(0).get("road_name");
					connection.setRoadType(WSConstants.DIGIT_ROAD_CATEGORIES.get(roadCategory));
					connection.setRoadCuttingArea(Float.valueOf((Integer)roadCategoryList.get(0).get("road_area")));

				}
					String addressQuery=	Sqls.address;
					Integer id =(Integer) data.get("applicantaddress.id");
					addressQuery=addressQuery.replace(":id",id.toString() );
				@SuppressWarnings("deprecation")
				Address address = (Address) jdbcTemplate.queryForObject(addressQuery,new BeanPropertyRowMapper(Address.class));  
				
				Locality locality=new Locality();
				//locality.setCode((String)data.get("locality"));
				//use the map here 
				locality.setCode("ALOC4");
				address.setLocality(locality);
				
			 
				connection.setApplicantAddress(address);

				connection.setTenantId(requestInfo.getUserInfo().getTenantId());
				connection.setProcessInstance(ProcessInstance.builder().action("INITIATE").build());
				
				recordService.recordWaterMigration(connection);
 				
				WaterConnectionRequest waterRequest = new WaterConnectionRequest();
				
				waterRequest.setWaterConnection(connection);
				waterRequest.setRequestInfo(requestInfo);
				Property property = propertyService.findProperty(waterRequest,json);
				connection.setPropertyId(property.getId()); 

				String ss = "{" + requestInfo + ", \"waterConnection\": " + waterRequest + " }";

				log.info("request: " + ss);

				String response = restTemplate.postForObject(host + "/" + waterUrl, waterRequest, String.class);

				log.info("Response=" + response);

				WaterConnectionResponse waterResponse=	objectMapper.readValue(response, WaterConnectionResponse.class);
		       
				WaterConnection wtrConnResp = null;
				if(waterResponse!=null && waterResponse.getWaterConnection() != null 
						&& !waterResponse.getWaterConnection().isEmpty()) {
					
					wtrConnResp = waterResponse.getWaterConnection().get(0);
					
					String consumerCode = wtrConnResp.getConnectionNo() !=null ? wtrConnResp.getConnectionNo()
							: wtrConnResp.getApplicationNo();
					
					List<Demand> demandRequestList = demandService.prepareDemandRequest(data, WSConstants.WATER_BUSINESS_SERVICE, 
							consumerCode, requestInfo.getUserInfo().getTenantId(), property.getOwners().get(0));
					if(!demandRequestList.isEmpty()) {
						
						Boolean isDemandCreated = demandService.saveDemand(requestInfo, demandRequestList);
						
						Boolean isBillCreated = demandService.fetchBill(demandRequestList, requestInfo);
						
						recordService.updateWaterMigration(wtrConnResp);
						log.info("waterResponse" + waterResponse);                                
					
				}
				}
				

			} catch (JsonMappingException e) {
				log.error(e.getMessage());
			} catch (JsonProcessingException e) {
				log.error(e.getMessage()); 
			}

		}
		log.info("Migration completed for "+tenantId);
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
				.mobilenumber((String) data.get("mobilenumber")) 
				.applicantname((String)data.get("applicantname"))
				.build();
		List<Map> roadCategoryList = (List<Map>) data.get("road_category");
		if (roadCategoryList != null) {
			String roadCategory = (String) roadCategoryList.get(0).get("road_name");
			waterConnection.setRoadType(WSConstants.DIGIT_ROAD_CATEGORIES.get(roadCategory));
			waterConnection.setRoadCuttingArea(Float.valueOf((Integer)roadCategoryList.get(0).get("road_area")));

		}

		return waterConnection;

	}
	
	
	public void migrateWtrCollection(String tenantId, RequestInfo requestInfo) {

		jdbcTemplate.execute("set search_path to " + tenantId);
		
		jdbcTemplate.execute(Sqls.WATER_COLLECTION_TABLE);

		List<String> queryForList = jdbcTemplate.queryForList(Sqls.waterQuery, String.class);

		for (String json : queryForList) {

			try {			
		
				recordService.recordWtrCollMigration(null);
				recordService.updateWtrCollMigration(null);
				log.info("waterResponse" + null);                                

			} catch (Exception e) {
				log.error(e.getMessage()); 
			}

		}
		log.info("Migration completed for "+tenantId);
	}

	
}
