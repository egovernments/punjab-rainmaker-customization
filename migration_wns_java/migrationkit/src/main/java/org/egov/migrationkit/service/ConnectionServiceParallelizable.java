package org.egov.migrationkit.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.egov.migrationkit.constants.WSConstants;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

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
import io.swagger.client.model.Connection.StatusEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionServiceParallelizable implements Callable<String> {
	private final RecordService recordService;
	private final ObjectMapper objectMapper;
	private final String tenantId;
	private final PropertyService propertyService;
	private final RestTemplate restTemplate;
	private final String resultJson;
	private final JdbcTemplate jdbcTemplate;
	private final DemandService demandService;
	private final RequestInfo requestInfo;
	private final String waterUrl;
	private final String host;

	public ConnectionServiceParallelizable(RecordService recordService, ObjectMapper objectMapper,
			PropertyService propertyService, RestTemplate restTemplate, JdbcTemplate jdbcTemplate,DemandService demandService,
			RequestInfo requestInfo, String resultJson, String tenantId, String waterUrl, String host) {
		this.recordService = recordService;
		this.objectMapper = objectMapper;
		this.propertyService = propertyService;
		this.restTemplate = restTemplate;
		this.tenantId = tenantId;
		this.demandService=demandService;
		this.resultJson = resultJson;
		this.jdbcTemplate = jdbcTemplate;
		this.requestInfo = requestInfo;
		this.host = host;
		this.waterUrl = waterUrl;

	}

	@Override
	public String call() throws Exception{

		long connStartTime = System.currentTimeMillis();
		WaterConnection connection = null;
		try {
			Map data = objectMapper.readValue(resultJson, Map.class);
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
			String locCode = (String) data.get("locality");
			String cityCode = (String) data.get("cityCode");

			boolean isMigrated = recordService.recordWaterMigration(connection, tenantId);
			if (isMigrated)
				return null;

			if (connection.getMobilenumber() == null || connection.getMobilenumber().isEmpty()) {
				// recordService.recordError("water", tenantId, "Mobile
				// Number is null ", connection.getId());
				/*
				 * System is allowing only 6-9 series.So as of now added 9999999999
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

			Address address = (Address) jdbcTemplate.queryForObject(addressQuery,
					new BeanPropertyRowMapper(Address.class));
			Locality locality = new Locality();
			// locality.setCode((String)data.get("locality"));
			// use the map here
			locCode = (String) data.get("locality");
			String localityCode = findLocality(locCode, tenantId);
			if (localityCode == null) {
				recordService.recordError("water", tenantId, "No Mapping for Locality: " + locCode, connection.getId());
				return null;
			}

			locality.setCode(localityCode);
			address.setLocality(locality);
			address.setCity((String) data.get("cityname"));
			connection.setApplicantAddress(address);

			Property property = propertyService.findProperty(waterRequest, data, tenantId);
			if (property == null) {
				return null;
			}
			connection.setPropertyId(property.getId());
			List<Map> roadCategoryList = (List<Map>) data.get("road_category");
			/*
			 * if (roadCategoryList != null) { String roadCategory = (String)
			 * roadCategoryList.get(0).get("road_name");
			 * connection.setRoadType(WSConstants.DIGIT_ROAD_CATEGORIES.get(roadCategory));
			 * try { area = (Integer) roadCategoryList.get(0).get("road_area");
			 * 
			 * connection.setRoadCuttingArea(Double.valueOf(area)); } catch (Exception e) {
			 * areaDouble = (Double) roadCategoryList.get(0).get("road_area");
			 * connection.setRoadCuttingArea(areaDouble); }
			 * 
			 * }
			 */
			Double areaDouble = null;
			if (roadCategoryList != null) {
				String roadCategory = (String) roadCategoryList.get(0).get("road_name");
				connection.setRoadType(WSConstants.DIGIT_ROAD_CATEGORIES.get(roadCategory));

				Object areaObject = roadCategoryList.get(0).get("road_area");

				if (areaObject instanceof Integer) {
					Integer area = (Integer) roadCategoryList.get(0).get("road_area");

					connection.setRoadCuttingArea(Double.valueOf(area));
				} else if (areaObject instanceof Double) {

					areaDouble = (Double) roadCategoryList.get(0).get("road_area");
					connection.setRoadCuttingArea(areaDouble);
				} else {
					areaDouble = 0d;
				}

			}

			connection.setStatus(StatusEnum.Active);

			// connection.setApplicationStatus("CONNECTION_ACTIVATED");

			connection.setApplicationStatus(WSConstants.CONNECTION_ACTIVATED);

			// connection.setApplicationType("NEW_WATER_CONNECTION");

			connection.setApplicationType(WSConstants.NEW_WATER_CONNECTION);

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
				return null;
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

					long connectionDuration = System.currentTimeMillis() - connStartTime;
					log.info("Migration completed for connection no : " + connection.getConnectionNo() + "in "
							+ connectionDuration / 1000 + "Secs");

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			recordService.recordError("water", tenantId, e.toString(), connection.getId());
		}
		
		return "Success";
	 

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

	private Long getMobileNumber(String cityCode, String locCode, String tenantId) {
		String loc = locCode.replaceAll("\\D+", "");
		String mobileNumber = String.format("4%4s%5s", cityCode, recordService.nextSequence(tenantId));
		mobileNumber = mobileNumber.replaceAll(" ", "0");
		return Long.valueOf(mobileNumber);

	}

	 
}
