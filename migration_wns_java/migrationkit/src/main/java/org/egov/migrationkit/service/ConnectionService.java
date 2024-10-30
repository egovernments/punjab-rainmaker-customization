package org.egov.migrationkit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.Address;
import io.swagger.client.model.Connection.StatusEnum;
import io.swagger.client.model.Demand;
import io.swagger.client.model.Document;
import io.swagger.client.model.Locality;
import io.swagger.client.model.OwnerInfo;
import io.swagger.client.model.ProcessInstance;
import io.swagger.client.model.Property;
import io.swagger.client.model.RequestInfo;
import io.swagger.client.model.RoadCuttingInfo;
import io.swagger.client.model.SewerageConnection;
import io.swagger.client.model.SewerageConnectionRequest;
import io.swagger.client.model.SewerageConnectionResponse;
import io.swagger.client.model.Status;
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
	
	@Value("${egov.services.status.to.ignore}")
	private String status="Demand_Created";

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
	
	@Autowired
	private CommonService commonService;

	@Value("${egov.services.sewerage.url}")
	private String sewerageUrl = null;

	public void migrateWtrConnection(String tenantId, RequestInfo requestInfo, List<String> boundaryList) {
		long startTime = System.currentTimeMillis();

		recordService.initiate(tenantId);
		Map data = null;
		WaterConnection connection = null;
		List<Map<String, Object>> roadCategoryList = null;
		Address address = null;
		Locality locality = null;
		String locCode = null;
		String localityCode = null;
		String cityCode = null;
		long connStartTime = 0l;
		long connectionDuration = 0l;
		long connectionCount=0l;

		String searchPath = jdbcTemplate.queryForObject("show search_path", String.class);
		log.info(searchPath);

		String qry = Sqls.WATER_CONNECTION_QUERY;

		if (boundaryList != null && !boundaryList.isEmpty())
			qry = qry.replace(":locCondition",
					" and locality.code in (" + String.join(",",
							boundaryList.stream().map(boundary -> ("'" + boundary + "'")).collect(Collectors.toList()))
							+ ") ");

		else
			qry = qry.replace(":locCondition", " ");
		
			qry = qry.replace(":status",status);
					 
		 
		
		log.info(qry);
		
		List<String> queryForList = jdbcTemplate.queryForList(qry, String.class);

		for (String resultJson : queryForList) {
			connStartTime = System.currentTimeMillis();
			connectionCount++;
			try {
				data = objectMapper.readValue(resultJson, Map.class);
				//log.info("Query Data Return: "+data.toString());
				connection = objectMapper.readValue(resultJson, WaterConnection.class);
				if (connection.getConnectionType().trim().equalsIgnoreCase("Non-Metered")) {
					connection.setConnectionType("Non Metered");
				} else {
					connection.setConnectionType("Metered");
				}
				String connectionNo = connection.getConnectionNo() != null ? connection.getConnectionNo()
						: (String) data.get("applicationNo");
				connection.setConnectionNo(connectionNo);
				
				//connection.setConnectionNo("2102000013");
				connection.setTenantId(requestInfo.getUserInfo().getTenantId());
				connection.setProposedPipeSize(connection.getPipeSize());
				log.info(" Water connection migrating for " + connectionNo +" MobileNumber "+connection.getMobilenumber() +" Connection Number :- "+connection.getId()+ " Property Id:- "+ connection.getPropertyId());
				WaterConnectionRequest waterRequest = new WaterConnectionRequest();

				waterRequest.setWaterConnection(connection);
				waterRequest.setRequestInfo(requestInfo);
				locCode = (String) data.get("locality");
				cityCode = (String) data.get("citycode");

				List<String> isConnectionMigrated = recordService.recordWaterMigration(connection, tenantId);
				log.info("isConnectionMigrated: " + isConnectionMigrated);
				if (isConnectionMigrated != null && !isConnectionMigrated.isEmpty() && isConnectionMigrated.contains("Saved")) {
					try {
						OwnerInfo ownerInfo = commonService.searchConnection(requestInfo, connectionNo, connection.getTenantId(), "water");
						if( ownerInfo != null) {
							
							createWaterDemand(data, connection.getId(), connectionNo, requestInfo, ownerInfo, tenantId);
							connectionDuration = System.currentTimeMillis() - connStartTime;
							log.info("Migration completed for connection no : " + connection.getConnectionNo() + "in "
									+ connectionDuration / 1000 + "Secs");
						} else {
							recordService.recordError("water", tenantId, "Connection or Property not found to generate the demand" ,
									connection.getId());
						}
					}catch (Exception e) {
						recordService.recordError("water", tenantId, "Exception occured while generating the demand"+e.toString() ,
								connection.getId());
					}
					continue;
				}else if(isConnectionMigrated != null && !isConnectionMigrated.isEmpty() && (isConnectionMigrated.contains("Demand_Created") || isConnectionMigrated.contains("NO_DEMANDS"))) {
					continue;					
				}

				if (connection.getMobilenumber() == null || connection.getMobilenumber().isEmpty() || connection.getMobilenumber().length()<10 ) {
				 
					
					Long mobileNumber = getMobileNumber(cityCode, locCode, tenantId);
					recordService.setMob("water", tenantId, mobileNumber, connection.getId());
					connection.setMobilenumber(String.valueOf(mobileNumber));
				}

				String addressQuery = Sqls.GET_ADDRESS;
				addressQuery = addressQuery.replace(":schema_tenantId", tenantId);
				Integer id = (Integer) data.get("applicantaddress.id");
				addressQuery = addressQuery.replace(":id", id.toString());

				address = (Address) jdbcTemplate.queryForObject(addressQuery, new BeanPropertyRowMapper(Address.class));
				locality = new Locality();
			 
				locCode = (String) data.get("locality");
				log.info("Locality code : "+locCode);
				localityCode = findLocality(locCode, tenantId);
				if (localityCode == null) {
					recordService.recordError("water", tenantId, "No Mapping for Locality: " + locCode,
							connection.getId());
					continue;
				}

				locality.setCode(localityCode);
				address.setLocality(locality);
				address.setCity((String) data.get("cityname"));
				connection.setApplicantAddress(address);

				Property property = propertyService.findProperty(waterRequest, data, tenantId,localityCode);
				
				if (property == null) {
					continue;
				}
				log.info("Ram Ram");
				connection.setPropertyId(property.getPropertyId());
			
				roadCategoryList = (List<Map<String, Object>>) data.get("road_category");
				List<RoadCuttingInfo> cuttingInfoList = new ArrayList<>();
				RoadCuttingInfo cuttingInfo = null;
				if (roadCategoryList != null && !roadCategoryList.isEmpty()) {
					for (Map roadCategory : roadCategoryList) {
						cuttingInfo = new RoadCuttingInfo();
						String road_name = (String) roadCategory.get("road_name");
						cuttingInfo.setRoadType(WSConstants.DIGIT_ROAD_CATEGORIES.get(road_name));
						try {
							Object areaObj = roadCategory.get("road_area");
							if(areaObj instanceof Integer)
								cuttingInfo.setRoadCuttingArea(Float.valueOf((Integer)areaObj));
							else if(areaObj instanceof Double)
								cuttingInfo.setRoadCuttingArea(((Double)areaObj).floatValue());
							else if(areaObj instanceof Float)
								cuttingInfo.setRoadCuttingArea(((Float)areaObj));
							else
								cuttingInfo.setRoadCuttingArea(0f);

						} catch (Exception e) {
							cuttingInfo.setRoadCuttingArea(0f);
						}
						cuttingInfoList.add(cuttingInfo);
					}

				}
				connection.setRoadCuttingInfo(cuttingInfoList);
				
				if(Status.ACTIVE.compareTo(property.getStatus())!=0 )
				{ 	
					Thread.sleep(2000); 
					Property approvedProperty = propertyService.updateProperty(property, connection.getTenantId(), requestInfo);
					Thread.sleep(1000); 
				}

				connection.setApplicationStatus("CONNECTION_ACTIVATED");

				connection.setApplicationType("NEW_WATER_CONNECTION");

				ProcessInstance workflow = new ProcessInstance();
				workflow.setBusinessService("NewWS1");
				workflow.setAction("ACTIVATE_CONNECTION");
				
				workflow.setTenantId(connection.getTenantId());
			
				workflow.setModuleName("ws-services");
				connection.setProcessInstance(workflow);
				
				// connection.setDocuments(getDocuments(waterRequest, data));
				Map<Object, Object> addtionals = new HashMap<Object, Object>();

				addtionals.put("propertyId", (String) data.get("propertyId"));
				addtionals.put("locality", localityCode);
				addtionals.put("billingType", (String) data.get("billingType"));
				addtionals.put("billingAmount", data.get("billingAmount") != null ? data.get("billingAmount") : 0);
				addtionals.put("estimationLetterDate", (String) data.get("estimationLetterDate"));
				addtionals.put("connectionCategory", (String) data.get("connectionCategory"));
				addtionals.put("meterId", (String) data.get("meterId"));
				addtionals.put("ledgerId", (String) data.get("ledgerId"));
				addtionals.put("pipeSize", (Double) data.getOrDefault("pipeSize", 1));
				addtionals.put("estimationFileStoreId", (String) data.get("estimationFileStoreId"));
				addtionals.put("meterMake", (String) data.get("meterMake"));
				addtionals.put("securityFee", data.get("securityFee"));
				addtionals.put("applicationNo", data.getOrDefault("applicationNo", null));
				addtionals.put("isexempted", data.getOrDefault("isexempted", false));
				
				if(data.get("connectionreason") == null ) {
					addtionals.put("isMigrated", Boolean.FALSE);
				}else {
					addtionals.put("isMigrated", Boolean.TRUE);
				}

				if (data.get("averageMeterReading") != null) {
					try {
						Integer averageMeterReading = (Integer) data.get("averageMeterReading");
						 
						addtionals.put("averageMeterReading", Double.valueOf(averageMeterReading));
						
					} catch (Exception e) {
						Double averageMeterReading = (Double) data.get("averageMeterReading");
						addtionals.put("averageMeterReading", averageMeterReading);

					}
				} else
					addtionals.put("averageMeterReading", 0);
			
				if (data.get("initialMeterReading") != null) {
					try {
						Integer initialMeterReading = (Integer) data.get("initialMeterReading");

						addtionals.put("initialMeterReading", Double.valueOf(initialMeterReading));
				
					} catch (Exception e) {
						Double initialMeterReading = (Double) data.get("initialMeterReading");
						addtionals.put("initialMeterReading", initialMeterReading);

					}
				} else
					addtionals.put("initialMeterReading", 0);

				connection.setAdditionalDetails(addtionals);
				
				connection.setOldApplication(false);
				// connection.setOldConnectionNo(connectionNo);
				
			
				//connection.setPropertyId(approvedProperty.getId());
				
				Boolean isMigration=true;
				String response = null;
				try {
					
					response = restTemplate.postForObject(host + "/" + waterUrl+"?isMigration="+isMigration, waterRequest, String.class);
					log.info(response);
				} catch (Exception e) {
					e.printStackTrace();
					
					recordService.recordError("water", tenantId, e.toString(), connection.getId());
					continue;
				}

				log.info("Connection Migrated "+connection.getConnectionNo());

				WaterConnectionResponse waterResponse = objectMapper.readValue(response, WaterConnectionResponse.class);

				WaterConnection wtrConnResp = null;
				if (waterResponse != null && waterResponse.getWaterConnection() != null
						&& !waterResponse.getWaterConnection().isEmpty()) {

					wtrConnResp = waterResponse.getWaterConnection().get(0);
					log.debug("status" + wtrConnResp.getStatus() + " application status"
							+ wtrConnResp.getApplicationStatus());
					recordService.updateWaterMigration(wtrConnResp, connection.getId(), tenantId, requestInfo.getUserInfo().getUuid());
					String consumerCode = wtrConnResp.getConnectionNo() != null ? wtrConnResp.getConnectionNo()
							: wtrConnResp.getApplicationNo();
					
					//Creating demand for water connection
					
					createWaterDemand(data, connection.getId(), consumerCode, requestInfo, property.getOwners().get(0), tenantId);
					
						connectionDuration = System.currentTimeMillis() - connStartTime;
						log.debug("Water Migration completed for connection no : " + connection.getConnectionNo() + "in "
								+ connectionDuration / 1000 + "Secs");

//					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				recordService.recordError("water", tenantId, e.toString(), connection.getId());
			}

		}
		long duration = System.currentTimeMillis() - startTime;

		log.info("Water Migration completed for " + connectionCount + " connections in "+tenantId+ " in " + duration / 1000 + " Secs");

	}
	
	private void createWaterDemand(Map data,String connectionId, String consumerCode, RequestInfo requestInfo, OwnerInfo ownerInfo, String tenantId ) {
		List<Demand> demandRequestList = demandService.prepareDemandRequest(data,
				WSConstants.WATER_BUSINESS_SERVICE, consumerCode, requestInfo.getUserInfo().getTenantId(),
				ownerInfo);
		if (!demandRequestList.isEmpty()) {
			
			Boolean isDemandCreated = demandService.saveDemand(requestInfo, demandRequestList,
					connectionId, tenantId, "water");
			if (isDemandCreated) {
				recordService.setStatus("water", tenantId, "Demand_Created", connectionId);

			}
		}else {
			recordService.setStatus("water", tenantId, "NO_DEMANDS", connectionId);
		}
		
	}

	private Long getMobileNumber(String cityCode, String locCode, String tenantId) {
		String loc = locCode.replaceAll("\\D+", "");
		String mobileNumber = String.format("4%4s%5s", cityCode, recordService.nextSequence(tenantId));
		mobileNumber = mobileNumber.replaceAll(" ", "0");
		return Long.valueOf(mobileNumber);

	}

	private List<Document> getDocuments(WaterConnectionRequest waterRequest, Map data) {
		List<Document> documents = new ArrayList<>();
		Document doc = new Document();
		// doc.setDocumentCode("OWNER.IDENTITYPROOF.AADHAAR");
		// doc.setFileName("0a5b93d4-9eaa-4605-aaf1-970026ec3606.png");
		// doc.setFileUrl("");
		doc.setFileStore("34dc69f0-c5f2-483e-a0ac-e3555dffd5b1");
		doc.setDocumentType("OWNER.IDENTITYPROOF.AADHAAR");
		documents.add(doc);
		return documents;

	}

	private String getRequestInfoString() {

		StringBuffer buf = new StringBuffer(1000);

		return "";

	}

	@SuppressWarnings("deprecation")
	private String findLocality(String code, String tenantId) {
		String Temp=null;
		 log.debug("Searching for digit locality mapping code: {} and schemaname: {}" , code,tenantId);
		String digitcode = null;
		try {
			 Temp="select digitcode as digitCode from " + tenantId + ".finallocation where code=?";
			digitcode = jdbcTemplate.queryForObject(Temp
					,
					new Object[] { code }, String.class);
		} catch (DataAccessException e) {
			e.printStackTrace();
log.info("query datea: "+Temp);
			log.error("digit Location code is not mapped for " + code); // no
			// default
			// value
			// digitcode = "ALOC1"; //for Amritsar
			// digitcode = "SUN158"; //for Sunam
			// digitcode = "NSR_112"; //for Nawashahr
			//digitcode = "LC-137"; // for Fazilka
			// digitcode="MH38"; //for mohali

		}
		// log.debug("returning " + digitcode);
		return digitcode;
	}

	public void migratev2(String tenantId, RequestInfo requestInfo) {

	}

	public void createSewerageConnection(String erpSchema, RequestInfo requestInfo, List<String> boundaryList) {
		long startTime = System.currentTimeMillis();

		recordService.initiateSewrage(erpSchema);
		SewerageConnection swConnection = null;
		Address address = null;
		Locality locality = null;
		String locCode = null;
		String localityCode = null;
		String cityCode = null;
		long connStartTime = 0l;
		long connectionDuration = 0l;
		long connectionCount=0l;
		List<Map<String, Object>> roadCategoryList = null;

		String searchPath = jdbcTemplate.queryForObject("show search_path", String.class);
		log.info(searchPath);

		String qry = Sqls.SEWERAGE_CONNECTION_QUERY;

		if (boundaryList != null && !boundaryList.isEmpty())
			qry = qry.replace(":locCondition",
					" and locality.code in (" + String.join(",",
							boundaryList.stream().map(boundary -> ("'" + boundary + "'")).collect(Collectors.toList()))
							+ ") ");
		else
			qry = qry.replace(":locCondition", " ");
		
		qry = qry.replace(":status",status);

		List<String> queryForList = jdbcTemplate.queryForList(qry, String.class);

		for (String json : queryForList) {
			connectionCount++;
			connStartTime = System.currentTimeMillis();
			try {

				Map data = objectMapper.readValue(json, Map.class);
				swConnection = objectMapper.readValue(json, SewerageConnection.class);
				swConnection.setTenantId(requestInfo.getUserInfo().getTenantId());
				swConnection.setConnectionType("Non Metered");

				String connectionNo = swConnection.getConnectionNo() != null ? swConnection.getConnectionNo()
						: (String) data.get("applicationNo");
				
				String propertyId=swConnection.getPropertyId ()!=null? swConnection.getPropertyId():(String) data.get("PropertyId");
				swConnection.setConnectionNo(connectionNo);
				
				
				 
				SewerageConnectionRequest sewerageRequest = new SewerageConnectionRequest();
				
				sewerageRequest.setSewerageConnection(swConnection);
				sewerageRequest.setRequestInfo(requestInfo);
				
				
				List<String> listStatuses = recordService.recordSewerageMigration(swConnection, erpSchema);
				if (listStatuses != null && !listStatuses.isEmpty() && listStatuses.contains("Saved")) {
					try {
					OwnerInfo ownerInfo = commonService.searchConnection(requestInfo, connectionNo,  swConnection.getTenantId(), "sewerage");
					
					if( ownerInfo != null) {
					createSewerageDemand(data, swConnection.getId(), connectionNo, requestInfo, ownerInfo, erpSchema);
					connectionDuration = System.currentTimeMillis() - connStartTime;
					
					log.debug("Migration completed for connection no : " + swConnection.getConnectionNo() + "in "
							+ connectionDuration / 1000 + "Secs");
					}else {
						recordService.recordError("sewerage", erpSchema, "Connection or Property not found to generate the demand" + locCode,
								swConnection.getId());
					}
					}catch (Exception e) {
						recordService.recordError("sewerage", erpSchema, "Exception occured while generating the demand"+e.toString() ,
								swConnection.getId());
					}
					continue;
				}
				else if(listStatuses != null && !listStatuses.isEmpty() && (listStatuses.contains("Demand_Created")  || listStatuses.contains("NO_DEMANDS"))) {
					continue;					
				}
				
				locCode = (String) data.get("locality");
		
				cityCode = (String) data.get("citycode");
				
				if (swConnection.getMobilenumber() == null || swConnection.getMobilenumber().isEmpty()) {
					Long mobileNumber = getMobileNumber(cityCode, locCode, erpSchema);
					recordService.setMob("sewerage", erpSchema, mobileNumber, swConnection.getId());
					swConnection.setMobilenumber(String.valueOf(mobileNumber));

					 
				}

				String addressQuery = Sqls.GET_ADDRESS;

				Integer id = (Integer) data.get("applicantaddress.id");
			

				addressQuery = addressQuery.replace(":schema_tenantId", erpSchema);
				
				addressQuery = addressQuery.replace(":id", id.toString());
			
				

				address = (Address) jdbcTemplate.queryForObject(addressQuery, new BeanPropertyRowMapper(Address.class));
				
				locality = new Locality();
				
				// locality.setCode((String)data.get("locality"));
				// use the map here
				locCode = (String) data.get("locality");
				//System.out.println(locCode);
				localityCode = findLocality(locCode, erpSchema);
				
				if (localityCode == null) {
					recordService.recordError("sewerage", erpSchema, "No Mapping for Locality: " + locCode,
							swConnection.getId());
					continue;
				}

				locality.setCode(localityCode);
				address.setLocality(locality);
				
				address.setCity((String) data.get("cityname"));
				swConnection.setApplicantAddress(address);
				
				Property property = propertyService.findProperty(sewerageRequest, data, erpSchema, localityCode);
				
				System.out.println("endding ");
				if (property == null) {
					recordService.recordError("sewerage", erpSchema,
							"Property not found or cannot be created  for the record  ", swConnection.getId());
					continue;
				}
				swConnection.setPropertyId(property.getPropertyId());
				StringBuilder additionalDetail = new StringBuilder();
				Map addtionals = new HashMap<String, String>();

				addtionals.put("propertyId", (String) data.get("propertyId"));
               addtionals.put("locality", localityCode);
				addtionals.put("billingType", (String) data.get("billingType"));
				 addtionals.put("billingAmount", data.get("billingAmount") != null ? data.get("billingAmount") : 0 );
				addtionals.put("estimationLetterDate", (String) data.get("estimationLetterDate"));
				
				addtionals.put("ledgerId", (String) data.get("ledgerId"));
				// addtionals.put("pipeSize",(Double) data.get("pipeSize"));
				addtionals.put("estimationFileStoreId", (String) data.get("estimationFileStoreId"));
				addtionals.put("securityFee", data.get("securityFee"));
				addtionals.put("applicationNo", data.getOrDefault("applicationNo", null));
				addtionals.put("isexempted", data.getOrDefault("isexempted", false));
				
				if(data.get("connectionreason") == null ) {
					addtionals.put("isMigrated", Boolean.FALSE);
				}else {
					addtionals.put("isMigrated", Boolean.TRUE);
				}
				// addtionals.put("meterMake",(String) data.get("meterMake"));

				if (data.get("averageMeterReading") != null) {
					try {
						Integer averageMeterReading = (Integer) data.get("averageMeterReading");

						addtionals.put("averageMeterReading", Double.valueOf(averageMeterReading));
					} catch (Exception e) {
						Double averageMeterReading = (Double) data.get("averageMeterReading");
						addtionals.put("averageMeterReading", averageMeterReading);

					}
				} else
					addtionals.put("averageMeterReading", 0);
				if (data.get("initialMeterReading") != null) {
					try {
						Integer initialMeterReading = (Integer) data.get("initialMeterReading");

						addtionals.put("initialMeterReading", Double.valueOf(initialMeterReading));
					} catch (Exception e) {
						Double initialMeterReading = (Double) data.get("initialMeterReading");
						addtionals.put("initialMeterReading", initialMeterReading);

					}
				} else {
					addtionals.put("initialMeterReading", 0);
				}

				swConnection.setAdditionalDetails(addtionals);
			
				sewerageRequest.setSewerageConnection(swConnection);
				sewerageRequest.setRequestInfo(requestInfo);
			
				if(Status.ACTIVE.compareTo(property.getStatus())!=0 )
				{ 
					Thread.sleep(2000);  
					Property approvedProperty = propertyService.updateProperty(property, swConnection.getTenantId(), requestInfo);
					
					Thread.sleep(1000); 
				}
				
			
				
				roadCategoryList = (List<Map<String, Object>>) data.get("road_category");
				List<RoadCuttingInfo> cuttingInfoList = new ArrayList<>();
				RoadCuttingInfo cuttingInfo = null;
				if (roadCategoryList != null && !roadCategoryList.isEmpty()) {
					for (Map roadCategory : roadCategoryList) {
						cuttingInfo = new RoadCuttingInfo();
						String road_name = (String) roadCategory.get("road_name");
						cuttingInfo.setRoadType(WSConstants.DIGIT_ROAD_CATEGORIES.get(road_name));
						try {
							Object areaObj = roadCategory.get("road_area");
							if(areaObj instanceof Integer)
								cuttingInfo.setRoadCuttingArea(Float.valueOf((Integer)areaObj));
							else if(areaObj instanceof Double)
								cuttingInfo.setRoadCuttingArea(((Double)areaObj).floatValue());
							else if(areaObj instanceof Float)
								cuttingInfo.setRoadCuttingArea(((Float)areaObj));
							else
								cuttingInfo.setRoadCuttingArea(0f);

						} catch (Exception e) {
							cuttingInfo.setRoadCuttingArea(0f);
						}
						cuttingInfoList.add(cuttingInfo);
					}

				}
				swConnection.setRoadCuttingInfo(cuttingInfoList);
				swConnection.setDocuments(getDocuments(null, data));

				swConnection.setTenantId(requestInfo.getUserInfo().getTenantId());
				
 	 

				swConnection.setApplicationStatus("CONNECTION_ACTIVATED");

				swConnection.setApplicationType("NEW_SEWERAGE_CONNECTION");
				ProcessInstance workflow = new ProcessInstance();
				workflow.setBusinessService("NewSW1");
				workflow.setAction("ACTIVATE_CONNECTION");
				workflow.setTenantId(swConnection.getTenantId());
				workflow.setModuleName("sw-services");
				swConnection.setProcessInstance(workflow);
				
				Boolean isMigration=true;
				String response = null;
				try {
					
					response = restTemplate.postForObject(host + "/" + sewerageUrl+"?isMigration=" + isMigration, sewerageRequest, String.class);
					recordService.setStatus("sewerage", erpSchema, "Saved", swConnection.getId());
					System.out.println("response" + response );
					

				} catch (RestClientException e) {
					log.error(e.getMessage(), e);
					recordService.recordError("sewerage", erpSchema,
							"Error in creating sewerage connection record :" + e.getMessage(), swConnection.getId());
					continue;
				}

				SewerageConnectionResponse sewerageResponse = objectMapper.readValue(response,
						SewerageConnectionResponse.class);
				
				

				SewerageConnection srgConnResp = null;

				

				if (sewerageResponse != null && sewerageResponse.getSewerageConnections() != null
						&& !sewerageResponse.getSewerageConnections().isEmpty()) {

					srgConnResp = sewerageResponse.getSewerageConnections().get(0);
					

					recordService.updateSewerageMigration(srgConnResp, swConnection.getId(), erpSchema,
							requestInfo.getUserInfo().getUuid());

					String consumerCode = srgConnResp.getConnectionNo() != null ? srgConnResp.getConnectionNo()
							: srgConnResp.getApplicationNo();
				
					createSewerageDemand(data, swConnection.getId(), connectionNo, requestInfo, property.getOwners().get(0), erpSchema);

						connectionDuration = System.currentTimeMillis() - connStartTime;
						log.debug("Sewerage Migration completed for connection no : " + swConnection.getConnectionNo() + "in "
								+ connectionDuration / 1000 + "Secs");


				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				recordService.recordError("sewerage", erpSchema, e.getMessage(), swConnection.getId());
				return;
			}

		}
		long duration = System.currentTimeMillis() - startTime;
		log.info("Sewerage Migration completed for " + connectionCount + " connections in "+erpSchema+ " in " + duration / 1000 + " Secs");

	}
	
	private void createSewerageDemand(Map data,String connectionId, String consumerCode, RequestInfo requestInfo, OwnerInfo ownerInfo, String tenantId ) {
		List<Demand> demandRequestList = demandService.prepareSwDemandRequest(data,
				WSConstants.SEWERAGE_BUSINESS_SERVICE, consumerCode,
				requestInfo.getUserInfo().getTenantId(), ownerInfo);

		if (!demandRequestList.isEmpty()) {

			Boolean isDemandCreated = demandService.saveDemand(requestInfo, demandRequestList,
					connectionId, tenantId, "sewerage");
			if (isDemandCreated) {
				recordService.setStatus("sewerage", tenantId, "Demand_Created", connectionId);
			}


		}else {
			recordService.setStatus("sewerage", tenantId, "NO_DEMANDS", connectionId);
		}

	}


}
