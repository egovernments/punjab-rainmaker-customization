package org.egov.migrationkit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.migrationkit.constants.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.Address;
import io.swagger.client.model.Connection.StatusEnum;
import io.swagger.client.model.Demand;
import io.swagger.client.model.Document;
import io.swagger.client.model.Locality;
import io.swagger.client.model.ProcessInstance;
import io.swagger.client.model.Property;
import io.swagger.client.model.RequestInfo;
import io.swagger.client.model.SewerageConnection;
import io.swagger.client.model.SewerageConnectionRequest;
import io.swagger.client.model.SewerageConnectionResponse;
import io.swagger.client.model.WaterConnection;
import io.swagger.client.model.WaterConnectionRequest;
import io.swagger.client.model.WaterConnectionResponse;
import lombok.extern.slf4j.Slf4j;

@Service
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

	@Value("${egov.services.sewerage.url}")
	private String sewerageUrl = null;

	public void migrateWtrConnection(String tenantId, RequestInfo requestInfo) {

		recordService.initiate(tenantId);
		Map data = null;
		WaterConnection connection = null;
		List<Map> roadCategoryList = null;
		Integer area = null;
		Double areaDouble = null;
		Address address = null;
 		Locality locality = null;
		String locCode = null;
		String localityCode = null;

		String searchPath = jdbcTemplate.queryForObject("show search_path", String.class);
		log.info(searchPath);

		List<String> queryForList = jdbcTemplate.queryForList(Sqls.waterQueryFormatted, String.class);

		for (String json : queryForList) {

			try {

				// IS_EXTERNAL_WORKFLOW_ENABLED
				data = objectMapper.readValue(json, Map.class);

				// WaterConnection connection = mapWaterConnection(data);
				connection = objectMapper.readValue(json, WaterConnection.class);
				/**
				 * Setting If consumer number is null then, assigning
				 * application number of ERP.
				 */
				String connectionNo = connection.getConnectionNo() != null ? connection.getConnectionNo()
						: (String) data.get("applicationnumber");
				connection.setConnectionNo(connectionNo);
				log.info("\n\n initiating migration for  " + connection.getMobilenumber());
				log.info("mobile number : " + connection.getMobilenumber());
				log.info("getApplicantname ; " + connection.getApplicantname());
				log.info("Guardian name :" + connection.getGuardianname());
				log.info("connectionNo; " + connection.getConnectionNo());
				log.info("Connection Category : " + connection.getConnectionCategory());
				log.info("Connection Type :" + connection.getConnectionType());
				log.info("ConnectionDetail id :" + connection.getId());

				// ToDo: populate connectionHolders

				roadCategoryList = (List<Map>) data.get("road_category");
				if (roadCategoryList != null) {
					String roadCategory = (String) roadCategoryList.get(0).get("road_name");
					connection.setRoadType(WSConstants.DIGIT_ROAD_CATEGORIES.get(roadCategory));
					try {
						area = (Integer) roadCategoryList.get(0).get("road_area");

						connection.setRoadCuttingArea(Double.valueOf(area));
					} catch (Exception e) {
						areaDouble = (Double) roadCategoryList.get(0).get("road_area");
						connection.setRoadCuttingArea(areaDouble);
					}

				}
				String addressQuery = Sqls.address;
				addressQuery = addressQuery.replace(":schema_tenantId", tenantId);
				Integer id = (Integer) data.get("applicantaddress.id");
				addressQuery = addressQuery.replace(":id", id.toString());

				address = (Address) jdbcTemplate.queryForObject(addressQuery, new BeanPropertyRowMapper(Address.class));
				
				

 	
				
				
				
				
				
				recordService.recordWaterMigration(connection);
				
				
				if(connection.getMobilenumber()==null || connection.getMobilenumber().isEmpty())
				{
					recordService.recordError("water",tenantId, "Mobile Number is null ", connection.getId());
					recordService.setStatus("water",tenantId, "Incompatible", connection.getId());
					continue;
				}

				locality = new Locality();
				// locality.setCode((String)data.get("locality"));
				// use the map here
				locCode = (String) data.get("locality");
				localityCode = findLocality(locCode);
				if (localityCode == null) {
					recordService.recordError("water",tenantId, "No Mapping for Locality: " + locCode, connection.getId());
					continue;
				}

				locality.setCode(localityCode);
				address.setLocality(locality);
				connection.setStatus(StatusEnum.Active);
				// connection.setConnectionNo((String)
				// data.get("consumercode"));
				connection.setApplicationStatus("CONNECTION_ACTIVATED");

				connection.setApplicantAddress(address);
				//connection.setSource();
 
				connection.setTenantId(requestInfo.getUserInfo().getTenantId());
				connection.setProcessInstance(ProcessInstance.builder().action("INITIATE").build());

				WaterConnectionRequest waterRequest = new WaterConnectionRequest();

				waterRequest.setWaterConnection(connection);
				waterRequest.setRequestInfo(requestInfo);
				connection.setDocuments(getDocuments(waterRequest, data));
				StringBuilder additionalDetail=new StringBuilder();
				Map addtionals=new HashMap<String,String>();
				addtionals.put("locality",localityCode);
				additionalDetail.append("\"locality\":").append("\"").append(localityCode).append("\"");
				
				/*String billingType = (String) data.get("billingType");
				additionalDetail.append(",").append("\"billingType\":").append("\"").append(billingType).append("\"");
				
				String billingAmount = (String) data.get("billingAmount");
				additionalDetail.append(",").append("\"billingAmount\":").append("\"").append(billingAmount).append("\"");
				
				String estimationLetterDate = (String) data.get("estimationLetterDate");
				additionalDetail.append(",").append("\"estimationLetterDate\":").append("\"").append(estimationLetterDate).append("\"");
				
				String estimationFileStoreId = (String) data.get("estimationFileStoreId");
				additionalDetail.append(",").append("\"estimationFileStoreId\":").append("\"").append(estimationFileStoreId).append("\"");
				
				String averageMake = (String) data.get("averageMake");
				additionalDetail.append(",").append("\"averageMake\":").append("\"").append(averageMake).append("\"");
				
				String initialMeterReading = (String) data.get("initialMeterReading");
				additionalDetail.append(",").append("\"initialMeterReading\":").append("\"").append(initialMeterReading).append("\"");
				
				String meterMake = (String) data.get("meterMake");
				additionalDetail.append(",").append("\"meterMake\":").append("\"").append(meterMake).append("\"");
				
				String othersFee = (String) data.get("othersFee");
				additionalDetail.append(",").append("\"othersFee\":").append("\"").append(othersFee).append("\"");
				
				String ledgerId = (String) data.get("ledgerId");
				additionalDetail.append(",").append("\"ledgerId\":").append("\"").append(ledgerId).append("\"");
				*/
				
				
				
				connection.setAdditionalDetails(addtionals);

				Property property = propertyService.findProperty(waterRequest, data,tenantId);
				if (property == null) {
					recordService.recordError("water",tenantId, "Error in while find or create property:", connection.getId());
					continue;
				}
				connection.setPropertyId(property.getId());

				String ss = "{" + requestInfo + ", \"waterConnection\": " + waterRequest + " }";

				log.info("request: " + ss);

				String response = null;
				try {
					response = restTemplate.postForObject(host + "/" + waterUrl, waterRequest, String.class);
				} catch (RestClientException e) {
					log.error(e.getMessage(), e);
					recordService.recordError("water", tenantId,"Error in creating water connection record :" + e.getMessage(),
							connection.getId());
					continue;
				}

				log.info("Response=" + response);

				WaterConnectionResponse waterResponse = objectMapper.readValue(response, WaterConnectionResponse.class);

				WaterConnection wtrConnResp = null;
				if (waterResponse != null && waterResponse.getWaterConnection() != null
						&& !waterResponse.getWaterConnection().isEmpty()) {

					wtrConnResp = waterResponse.getWaterConnection().get(0);
					recordService.updateWaterMigration(wtrConnResp, connection.getId());
					String consumerCode = wtrConnResp.getConnectionNo() != null ? wtrConnResp.getConnectionNo()
							: wtrConnResp.getApplicationNo();

					List<Demand> demandRequestList = demandService.prepareDemandRequest(data,
							WSConstants.WATER_BUSINESS_SERVICE, consumerCode, requestInfo.getUserInfo().getTenantId(),
							property.getOwners().get(0));
					if (!demandRequestList.isEmpty()) {

						Boolean isDemandCreated = demandService.saveDemand(requestInfo, demandRequestList,connection.getId(),tenantId);
						if (isDemandCreated) {
							Boolean isBillCreated = demandService.fetchBill(demandRequestList, requestInfo);
							log.info("Bill created" + isBillCreated + " isDemandCreated" + isDemandCreated);

						}

						recordService.setStatus("water",tenantId, "Demand_Created", connection.getId());
						log.info("waterResponse" + waterResponse);
						log.info("completed for " + connection.getMobilenumber());

					}
				}

			} catch (JsonMappingException e) {
				log.error(e.getMessage(), e);
				recordService.recordError("water",tenantId, e.getMessage(), connection.getId());
			} catch (JsonProcessingException e) {
				recordService.recordError("water",tenantId, e.getMessage(), connection.getId());
				log.error(e.getMessage(), e);
			}

			catch (Exception e) {
				log.error(e.getMessage(), e);
				recordService.recordError("water",tenantId, e.getMessage(), connection.getId());
				return;

			}

		}
		log.info("Migration completed for " + tenantId);
	}

	private List<Document> getDocuments(WaterConnectionRequest waterRequest, Map data) {
		List<Document> documents=new ArrayList<>();
		Document doc=new Document();
		doc.setFileStore("34dc69f0-c5f2-483e-a0ac-e3555dffd5b1");
		doc.setDocumentType("Sample");
		documents.add(doc);
		return documents;
		
	}

	private String getRequestInfoString() {

		StringBuffer buf = new StringBuffer(1000);

		return "";

	}

	private String findLocality(String code) {
		log.info("Seraching  for digit locality maping " + code);
		String digitcode = null;
		try {
			digitcode = jdbcTemplate.queryForObject("select digitcode as digitCode from finallocation where code=?",
					new Object[] { code }, String.class);
		} catch (DataAccessException e) {
			log.error("digit Location code is not mapped for " + code);
			//digitcode = "ALOC3"; //for Amritsar
			//digitcode = "SUN158"; //for Sunam
			digitcode = "NSR_112"; //for Nawashahr
		}
		log.info("returning  " + digitcode);
		return digitcode;
	}

	public void migratev2(String tenantId, RequestInfo requestInfo) {

	}

	public void createSewerageConnection(String tenantId, RequestInfo requestInfo) {

		recordService.initiateSewrage(tenantId);
		SewerageConnection swConnection=null;
		Address address = null;
		Locality locality = null;
		String locCode = null;
		String localityCode = null;
		
		
		

		List<String> queryForList = jdbcTemplate.queryForList(Sqls.sewerageQuery, String.class);

		for (String json : queryForList) {

			try {

				Map data = objectMapper.readValue(json, Map.class);
				swConnection = objectMapper.readValue(json, SewerageConnection.class);
				// connection.setApplicantAddress(address);

				swConnection.setTenantId(requestInfo.getUserInfo().getTenantId());
				swConnection.setProcessInstance(ProcessInstance.builder().action("INITIATE").build());

				String connectionNo = swConnection.getConnectionNo() != null ? swConnection.getConnectionNo()
						: (String) data.get("applicationnumber");
				swConnection.setConnectionNo(connectionNo);
				
				log.info("mobile number : " + swConnection.getMobilenumber());
				log.info("getApplicantname ; " + swConnection.getApplicantname());
				log.info("connectionNo; " + swConnection.getConnectionNo());
				log.info("Connection Category : " + swConnection.getConnectionCategory());
				log.info("Connection Type :" + swConnection.getConnectionType());
				log.info("Connection id :" + swConnection.getId());
				
				
			
				
				recordService.recordSewerageMigration(swConnection,tenantId);
				
				
				if(swConnection.getMobilenumber()==null || swConnection.getMobilenumber().isEmpty())
				{
					recordService.recordError("sewerage",tenantId, "Mobile Number is null ", swConnection.getId());
					recordService.setStatus("sewerage",tenantId, "Incompatible", swConnection.getId());
					continue;
				}
				
				String addressQuery = Sqls.address;
				
				Integer id = (Integer) data.get("applicantaddress.id");
				
				addressQuery = addressQuery.replace(":schema_tenantId", tenantId);
				addressQuery = addressQuery.replace(":id", id.toString());

				address = (Address) jdbcTemplate.queryForObject(addressQuery, new BeanPropertyRowMapper(Address.class));
				

				locality = new Locality();
				// locality.setCode((String)data.get("locality"));
				// use the map here
				locCode = (String) data.get("locality");
				localityCode = findLocality(locCode);
				if (localityCode == null) {
					recordService.recordError("sewerage",tenantId, "No Mapping for Locality: " + locCode, swConnection.getId());
					continue;
				}

				locality.setCode(localityCode);
				address.setLocality(locality);
				swConnection.setApplicantAddress(address);
				SewerageConnectionRequest sewerageRequest = new SewerageConnectionRequest();


				

				sewerageRequest.setSewerageConnection(swConnection);
				sewerageRequest.setRequestInfo(requestInfo);
				Property property = propertyService.findProperty(sewerageRequest, json,tenantId);
				

				if (property == null) {
					recordService.recordError("sewerage",tenantId, "Property not found or cannot be created  for the record  ",
							swConnection.getId());
					continue;
				}
				swConnection.setPropertyId(property.getId());
				swConnection.setDocuments(getDocuments(null, data));

				String response = null;
				try {
					response = restTemplate.postForObject(host + "/" + sewerageUrl, sewerageRequest, String.class);
					recordService.setStatus("sewerage",tenantId, "Saved", swConnection.getId());
				
				} catch (RestClientException e) {
					log.error(e.getMessage(), e);
					recordService.recordError("sewerage",tenantId, "Error in creating water connection record :" + e.getMessage(),
							swConnection.getId());
					continue;
				}

				log.info("Response=" + response);

				SewerageConnectionResponse sewerageResponse = objectMapper.readValue(response,
						SewerageConnectionResponse.class);

				log.info("Sewerage Response=" + sewerageResponse);

				SewerageConnection srgConnResp = null;

				// this will be uncomented after the searage request is
				// completed

				if (sewerageResponse != null && sewerageResponse.getSewerageConnections() != null
						&& !sewerageResponse.getSewerageConnections().isEmpty()) {

					srgConnResp = sewerageResponse.getSewerageConnections().get(0);
					
					  recordService.updateSewerageMigration(srgConnResp,swConnection.getId(),tenantId);

					String consumerCode = srgConnResp.getConnectionNo() != null ? srgConnResp.getConnectionNo()
							: srgConnResp.getApplicationNo();

					List<Demand> demandRequestList = demandService.prepareSwDemandRequest(data,
							WSConstants.SEWERAGE_BUSINESS_SERVICE, consumerCode,
							requestInfo.getUserInfo().getTenantId(), property.getOwners().get(0));

				//	log.info("Demand Request=" + demandRequestList.size());

					if (!demandRequestList.isEmpty()) {

						Boolean isDemandCreated = demandService.saveDemand(requestInfo, demandRequestList,swConnection.getId(),tenantId);
						if (isDemandCreated) {
							Boolean isBillCreated = demandService.fetchBill(demandRequestList, requestInfo);
							recordService.setStatus("sewerage",tenantId, "Demand_Created", swConnection.getId());

						}
						 
						log.info("sewerageResponse" + sewerageResponse);

					}

				}

			} catch (JsonMappingException e) {
				log.error(e.getMessage());
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
			}
			catch (Exception e) {
				log.error(e.getMessage(), e);
				recordService.recordError("sewerage",tenantId, e.getMessage(), swConnection.getId());
				return;
			}

		}
		log.info("Sewarage Connection completed for " + tenantId);  

	}

}
