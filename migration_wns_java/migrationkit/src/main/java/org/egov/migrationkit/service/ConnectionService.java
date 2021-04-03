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

	public void migrateWtrConnection(String tenantId, RequestInfo requestInfo, List<String> boundaryList) {
		long startTime = System.currentTimeMillis();

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
		
		log.info(qry);

		List<String> queryForList = jdbcTemplate.queryForList(qry, String.class);

		for (String resultJson : queryForList) {
			connStartTime = System.currentTimeMillis();
			connectionCount++;
			try {
				data = objectMapper.readValue(resultJson, Map.class);
				connection = objectMapper.readValue(resultJson, WaterConnection.class);
				if (connection.getConnectionType().trim().equalsIgnoreCase("Non-Metered")) {
					connection.setConnectionType("Non Metered");
				} else {
					connection.setConnectionType("Metered");
				}
				String connectionNo = connection.getConnectionNo() != null ? connection.getConnectionNo()
						: (String) data.get("applicationnumber");
				connection.setConnectionNo(connectionNo);
				connection.setTenantId(requestInfo.getUserInfo().getTenantId());
				connection.setProposedPipeSize(connection.getPipeSize());
				log.info("\n\n initiating migration for  " + connection.getMobilenumber());
				// log.debug("mobile number : " + connection.getMobilenumber());
				//// log.debug("getApplicantname ; " +
				// connection.getApplicantname());
				// log.debug("Guardian name :" + connection.getGuardianname());
				// log.debug("connectionNo; " + connection.getConnectionNo());
				// log.debug("Connection Category : " +
				// connection.getConnectionCategory());
				// log.debug("Connection Type :" +
				// connection.getConnectionType());
				// log.debug("ConnectionDetail id :" + connection.getId());

				// ToDo: populate connectionHolders
				WaterConnectionRequest waterRequest = new WaterConnectionRequest();

				waterRequest.setWaterConnection(connection);
				waterRequest.setRequestInfo(requestInfo);
				locCode = (String) data.get("locality");
				cityCode = (String) data.get("cityCode");

				boolean isMigrated = recordService.recordWaterMigration(connection, tenantId);
				if (isMigrated)
					continue;

				if (connection.getMobilenumber() == null || connection.getMobilenumber().isEmpty()) {
					// recordService.recordError("water", tenantId, "Mobile
					// Number is null ", connection.getId());
					/*
					 * System is allowing only 6-9 series.So as of now added
					 * 9999999999
					 */
					Long mobileNumber = getMobileNumber(cityCode, locCode, tenantId);
					recordService.setMob("water", tenantId, mobileNumber, connection.getId());
					connection.setMobilenumber(String.valueOf(mobileNumber));
					// recordService.setStatus("water", tenantId,
					// "Incompatible", connection.getId());
					// continue;
				}

				String addressQuery = Sqls.GET_ADDRESS;
				addressQuery = addressQuery.replace(":schema_tenantId", tenantId);
				Integer id = (Integer) data.get("applicantaddress.id");
				addressQuery = addressQuery.replace(":id", id.toString());

				address = (Address) jdbcTemplate.queryForObject(addressQuery, new BeanPropertyRowMapper(Address.class));
				locality = new Locality();
				// locality.setCode((String)data.get("locality"));
				// use the map here
				locCode = (String) data.get("locality");
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

				Property property = propertyService.findProperty(waterRequest, data, tenantId);
				if (property == null) {
					continue;
				}
				connection.setPropertyId(property.getId());
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

				connection.setStatus(StatusEnum.Active);

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
				addtionals.put("billingAmount", String.valueOf("billingAmount"));
				addtionals.put("estimationLetterDate", (String) data.get("estimationLetterDate"));
				addtionals.put("connectionCategory", (String) data.get("connectionCategory"));
				addtionals.put("meterId", (String) data.get("meterId"));
				addtionals.put("ledgerId", (String) data.get("ledgerId"));
				addtionals.put("pipeSize", (Double) data.getOrDefault("pipeSize", 1));
				addtionals.put("estimationFileStoreId", (String) data.get("estimationFileStoreId"));
				addtionals.put("meterMake", (String) data.get("meterMake"));
				addtionals.put("securityFee", data.get("securityFee"));
				addtionals.put("isMigrated", Boolean.TRUE);

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

				connection.setOldApplication(true);
				// connection.setOldConnectionNo(connectionNo);

				String response = null;
				try {
					response = restTemplate.postForObject(host + "/" + waterUrl, waterRequest, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					recordService.recordError("water", tenantId, e.toString(), connection.getId());
					continue;
				}

				// log.debug("Response=" + response);

				WaterConnectionResponse waterResponse = objectMapper.readValue(response, WaterConnectionResponse.class);

				WaterConnection wtrConnResp = null;
				if (waterResponse != null && waterResponse.getWaterConnection() != null
						&& !waterResponse.getWaterConnection().isEmpty()) {

					wtrConnResp = waterResponse.getWaterConnection().get(0);
					log.info("status" + wtrConnResp.getStatus() + " application status"
							+ wtrConnResp.getApplicationStatus());
					recordService.updateWaterMigration(wtrConnResp, connection.getId(), tenantId,
							requestInfo.getUserInfo().getUuid());
					String consumerCode = wtrConnResp.getConnectionNo() != null ? wtrConnResp.getConnectionNo()
							: wtrConnResp.getApplicationNo();

					List<Demand> demandRequestList = demandService.prepareDemandRequest(data,
							WSConstants.WATER_BUSINESS_SERVICE, consumerCode, requestInfo.getUserInfo().getTenantId(),
							property.getOwners().get(0));
					if (!demandRequestList.isEmpty()) {

						Boolean isDemandCreated = demandService.saveDemand(requestInfo, demandRequestList,
								connection.getId(), tenantId, "water");
						if (isDemandCreated) {
							recordService.setStatus("water", tenantId, "Demand_Created", connection.getId());
							// Boolean isBillCreated =
							// demandService.fetchBill(demandRequestList,
							// requestInfo);
							// log.info("Bill created" + isBillCreated + "
							// isDemandCreated" + isDemandCreated);

						}

						connectionDuration = System.currentTimeMillis() - connStartTime;
						log.info("Migration completed for connection no : " + connection.getConnectionNo() + "in "
								+ connectionDuration / 1000 + "Secs");

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				recordService.recordError("water", tenantId, e.toString(), connection.getId());
			}

		}
		long duration = System.currentTimeMillis() - startTime;

		log.info("Water Migration completed for " + connectionCount + " connections in "+tenantId+ " in " + duration / 1000 + " Secs");

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

	private String findLocality(String code, String tenantId) {
		// log.debug("Seraching for digit locality maping " + code);
		String digitcode = null;
		try {
			digitcode = jdbcTemplate.queryForObject(
					"select digitcode as digitCode from " + tenantId + ".finallocation where code=?",
					new Object[] { code }, String.class);
		} catch (DataAccessException e) {

			log.error("digit Location code is not mapped for " + code); // no
			// default
			// value
			// digitcode = "ALOC1"; //for Amritsar
			// digitcode = "SUN158"; //for Sunam
			// digitcode = "NSR_112"; //for Nawashahr
			digitcode = "LC-137"; // for Fazilka
			// digitcode="MH38"; //for mohali

		}
		// log.debug("returning " + digitcode);
		return digitcode;
	}

	public void migratev2(String tenantId, RequestInfo requestInfo) {

	}

	public void createSewerageConnection(String tenantId, RequestInfo requestInfo, List<String> boundaryList) {
		long startTime = System.currentTimeMillis();

		recordService.initiateSewrage(tenantId);
		SewerageConnection swConnection = null;
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

		String qry = Sqls.SEWERAGE_CONNECTION_QUERY;

		if (boundaryList != null && !boundaryList.isEmpty())
			qry = qry.replace(":locCondition",
					" and locality.code in (" + String.join(",",
							boundaryList.stream().map(boundary -> ("'" + boundary + "'")).collect(Collectors.toList()))
							+ ") ");
		else
			qry = qry.replace(":locCondition", " ");

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
						: (String) data.get("applicationnumber");
				swConnection.setConnectionNo(connectionNo);
				log.info("Migrating for consumer number : " + swConnection.getConnectionNo());
				// log.debug("getApplicantname ; " +
				// swConnection.getApplicantname());
				// log.debug("connectionNo; " + swConnection.getConnectionNo());
				// log.debug("Connection Category : " +
				// swConnection.getConnectionCategory());
				// log.debug("Connection Type :" +
				// swConnection.getConnectionType());
				// log.debug("Connection id :" + swConnection.getId());
				boolean isMigrated = recordService.recordSewerageMigration(swConnection, tenantId);
				if (isMigrated)
					continue;

				locCode = (String) data.get("locality");
				cityCode = (String) data.get("cityCode");
				if (swConnection.getMobilenumber() == null || swConnection.getMobilenumber().isEmpty()) {
					// recordService.recordError("sewerage", tenantId, "Mobile
					// Number is null ", swConnection.getId());
					Long mobileNumber = getMobileNumber(cityCode, locCode, tenantId);
					recordService.setMob("sewerage", tenantId, mobileNumber, swConnection.getId());
					swConnection.setMobilenumber(String.valueOf(mobileNumber));

					// continue;
				}

				String addressQuery = Sqls.GET_ADDRESS;

				Integer id = (Integer) data.get("applicantaddress.id");

				addressQuery = addressQuery.replace(":schema_tenantId", tenantId);
				addressQuery = addressQuery.replace(":id", id.toString());

				address = (Address) jdbcTemplate.queryForObject(addressQuery, new BeanPropertyRowMapper(Address.class));

				locality = new Locality();
				// locality.setCode((String)data.get("locality"));
				// use the map here
				locCode = (String) data.get("locality");
				localityCode = findLocality(locCode, tenantId);
				if (localityCode == null) {
					recordService.recordError("sewerage", tenantId, "No Mapping for Locality: " + locCode,
							swConnection.getId());
					continue;
				}

				locality.setCode(localityCode);
				address.setLocality(locality);
				address.setCity((String) data.get("cityname"));
				swConnection.setApplicantAddress(address);
				SewerageConnectionRequest sewerageRequest = new SewerageConnectionRequest();

				StringBuilder additionalDetail = new StringBuilder();
				Map addtionals = new HashMap<String, String>();

				addtionals.put("propertyId", (String) data.get("propertyId"));

				addtionals.put("locality", localityCode);
				addtionals.put("billingType", (String) data.get("billingType"));
				// addtionals.put("billingAmount", data.get("billingAmount"));
				addtionals.put("estimationLetterDate", (String) data.get("estimationLetterDate"));
				// addtionals.put("connectionCategory",(String)
				// data.get("connectionCategory"));
				// addtionals.put("meterId",(String) data.get("meterId"));
				addtionals.put("ledgerId", (String) data.get("ledgerId"));
				// addtionals.put("pipeSize",(Double) data.get("pipeSize"));
				addtionals.put("estimationFileStoreId", (String) data.get("estimationFileStoreId"));
				addtionals.put("securityFee", data.get("securityFee"));
				addtionals.put("isMigrated", Boolean.TRUE);
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
				Property property = propertyService.findProperty(sewerageRequest, data, tenantId);

				if (property == null) {
					recordService.recordError("sewerage", tenantId,
							"Property not found or cannot be created  for the record  ", swConnection.getId());
					continue;
				}
				swConnection.setPropertyId(property.getId());
				swConnection.setDocuments(getDocuments(null, data));

				swConnection.setTenantId(requestInfo.getUserInfo().getTenantId());
				
				swConnection.setStatus(StatusEnum.Active);

				swConnection.setApplicationStatus("CONNECTION_ACTIVATED");

				swConnection.setApplicationType("NEW_SEWERAGE_CONNECTION");

				ProcessInstance workflow = new ProcessInstance();
				workflow.setBusinessService("NewSW1");
				workflow.setAction("ACTIVATE_CONNECTION");
				workflow.setTenantId(swConnection.getTenantId());
				workflow.setModuleName("sw-services");
				swConnection.setProcessInstance(workflow);
				
//				ProcessInstance workflow = new ProcessInstance();
//				workflow.setBusinessService("NewSW1");
//				workflow.setAction("SUBMIT");
//				workflow.setTenantId(swConnection.getTenantId());
//				workflow.setModuleName("sw-services");
//				swConnection.setProcessInstance(workflow);

				String response = null;
				try {
					response = restTemplate.postForObject(host + "/" + sewerageUrl, sewerageRequest, String.class);
					recordService.setStatus("sewerage", tenantId, "Saved", swConnection.getId());

				} catch (RestClientException e) {
					log.error(e.getMessage(), e);
					recordService.recordError("sewerage", tenantId,
							"Error in creating sewerage connection record :" + e.getMessage(), swConnection.getId());
					continue;
				}

				// log.debug("Response=" + response);

				SewerageConnectionResponse sewerageResponse = objectMapper.readValue(response,
						SewerageConnectionResponse.class);

				//// log.debug("Sewerage Response=" + sewerageResponse);

				SewerageConnection srgConnResp = null;

				// this will be uncomented after the searage request is
				// completed

				if (sewerageResponse != null && sewerageResponse.getSewerageConnections() != null
						&& !sewerageResponse.getSewerageConnections().isEmpty()) {

					srgConnResp = sewerageResponse.getSewerageConnections().get(0);

					recordService.updateSewerageMigration(srgConnResp, swConnection.getId(), tenantId,
							requestInfo.getUserInfo().getUuid());

					String consumerCode = srgConnResp.getConnectionNo() != null ? srgConnResp.getConnectionNo()
							: srgConnResp.getApplicationNo();

					List<Demand> demandRequestList = demandService.prepareSwDemandRequest(data,
							WSConstants.SEWERAGE_BUSINESS_SERVICE, consumerCode,
							requestInfo.getUserInfo().getTenantId(), property.getOwners().get(0));

					// log.info("Demand Request=" + demandRequestList.size());

					if (!demandRequestList.isEmpty()) {

						Boolean isDemandCreated = demandService.saveDemand(requestInfo, demandRequestList,
								swConnection.getId(), tenantId, "sewerage");
						if (isDemandCreated) {
							// Boolean isBillCreated =
							// demandService.fetchBill(demandRequestList,
							// requestInfo);
							recordService.setStatus("sewerage", tenantId, "Demand_Created", swConnection.getId());
							//
						}

						connectionDuration = System.currentTimeMillis() - connStartTime;
						log.info("Migration completed for connection no : " + swConnection.getConnectionNo() + "in "
								+ connectionDuration / 1000 + "Secs");

					}

				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				recordService.recordError("sewerage", tenantId, e.getMessage(), swConnection.getId());
				return;
			}

		}
		long duration = System.currentTimeMillis() - startTime;
		log.info("Sewerage Migration completed for " + connectionCount + " connections in "+tenantId+ " in " + duration / 1000 + " Secs");

	}

}
