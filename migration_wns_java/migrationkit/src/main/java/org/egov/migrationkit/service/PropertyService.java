package org.egov.migrationkit.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//import java.util.Map.Entry;

//import static org.egov.migrationkit.constants.WSConstants.PROP_USAGE_TYPE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.Channel;
import io.swagger.client.model.CreationReason;
import io.swagger.client.model.OwnerInfo;
import io.swagger.client.model.ProcessInstance;
import io.swagger.client.model.Property;
import io.swagger.client.model.PropertyRequest;
import io.swagger.client.model.PropertyResponse;
import io.swagger.client.model.PropertySearchResponse;
import io.swagger.client.model.Relationship;
import io.swagger.client.model.RequestInfo;
import io.swagger.client.model.RequestInfoWrapper;
import io.swagger.client.model.SewerageConnection;
import io.swagger.client.model.SewerageConnectionRequest;
import io.swagger.client.model.Source;
import io.swagger.client.model.Status;
import io.swagger.client.model.Unit;
import io.swagger.client.model.WaterConnection;
import io.swagger.client.model.WaterConnectionRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PropertyService {

	private static final Property Property = null;

	@Value("${egov.services.ptsearch.url}")
	private String ptseachurl = null;

	@JsonProperty("host")
	@Value("${egov.services.hosturl}")
	private String host = null;

	@Value("${egov.services.ptcreate.url}")
	private String ptcreatehurl = null;

	@Value("${egov.services.ptupdate.url}")
	private String ptupdatehurl = null;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RecordService recordService;

	@SuppressWarnings("unused")
	public Property findProperty(WaterConnectionRequest wcr, Map data, String tenantId, String localityCode) {

		Property property = null;

		try {
			log.info("WaterConnectionRequest " + wcr);
			property = searchPtRecord(wcr, data, tenantId, localityCode);
			// log.info("entered in find properties in");
			if(property!=null){
			log.info("Property After Search is: " + property.toString());
			
			if(property.getStatus().toString().equalsIgnoreCase("INACTIVE")){
				log.info("Obtained Property Is Inactive "+property.getPropertyId().toString());
				property=null;
			}
			}
			else
			{
				log.info("Oops Unable To Find Property ");
			}
			if (property == null) {
				log.debug("Propery not found creating new property");
				property = createProperty(wcr, data, tenantId);

			}
		} catch (Exception e) {
			log.error("error while finding or creating property {}", e.getMessage());
			e.printStackTrace();
			recordService.recordError("water", tenantId, e.getMessage(), wcr.getWaterConnection().getId());
		}

		return property;
	}

	public Property findProperty(SewerageConnectionRequest swg, Map json, String tenantId, String localityCode) {
		Property property = null;
		log.info("Find Property Request");
		log.info("swg :- " + swg);
		log.info("json:- " + json);
		log.info("tenant id:- " + tenantId);
		log.info("loc code:- " + localityCode);
		// log.info("Find Property Request");

		try {
			property = searchswPtRecord(swg, json, tenantId, localityCode);

			if (property == null) {
				log.debug("Propery not found creating new property");
				property = createSWProperty(swg, json, tenantId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			recordService.recordError("sewerage", tenantId, e.toString(), swg.getSewerageConnection().getId());
		}

		return property;
	}

	private Property createProperty(WaterConnectionRequest wcr, Map data, String tenantId) throws InterruptedException {
		PropertyRequest prequest = new PropertyRequest();
		WaterConnection conn = wcr.getWaterConnection();
		try {
			prequest.setRequestInfo(wcr.getRequestInfo());
			log.info("Creating Property ");
			Property property = new Property();

			// set all property values

			property.setAddress(conn.getApplicantAddress());
			property.setChannel(Channel.SYSTEM);
			property.setSource(Source.WATER_CHARGES);
			if (conn.getPlotSize() != null) {
				if (conn.getPlotSize() > 2 || conn.getPlotSize() > 2.0) {
					log.info("Plot size is greater than 2  or 2.0  " + conn.getPlotSize());
					property.setLandArea(BigDecimal.valueOf(conn.getPlotSize()));
				}
			} else {
				log.info("Plot size is less   than 2  or 2.0" + conn.getPlotSize());
				property.setLandArea(BigDecimal.valueOf(2));
			}
			property.setNoOfFloors(Long.valueOf(1));
			property.setOldPropertyId(conn.getPropertyId());
			property.setOwners(null);
			// fix this
			property.setOwnershipCategory("INDIVIDUAL.SINGLEOWNER");
			property.setPropertyType("BUILTUP.INDEPENDENTPROPERTY");

			property.setTotalConstructedArea(BigDecimal.valueOf(190));
			property.setStatus(Status.ACTIVE);
			List<Unit> units = new ArrayList<>();

			property.setUnits(units);
			OwnerInfo owner = new OwnerInfo();
			owner.setName(conn.getApplicantname().replace('?', ' ').replace('`', ' '));
			owner.setMobileNumber(conn.getMobilenumber());
			owner.setFatherOrHusbandName(
					conn.getGuardianname() != null ? conn.getGuardianname().replace('?', ' ').replace('`', ' ') : null);
			owner.setOwnerType("NONE");
			owner.setGender((String) data.get("gender"));
			owner.setEmailId((String) data.getOrDefault("emailId", null));
			log.info("Create Prop rel: "+Relationship.valueOf((String) data.getOrDefault("guardianrelation", "Other")));
			owner.setRelationship(Relationship.valueOf((String) data.getOrDefault("guardianrelation", "Other")));

			property.creationReason(CreationReason.CREATE);

			if (data.getOrDefault("usage", null) != null) {
				String usageCategory = (String) data.get("usage");
				log.info("usageCategoryInitial:" + usageCategory);

				if (usageCategory.equals("DOMESTIC")) {
					usageCategory = "RESIDENTIAL";
					log.info("usageCategoryDomestic:" + usageCategory);
				} else if (usageCategory.equals("COMMERCIAL")) {
					usageCategory = "NONRESIDENTIAL.COMMERCIAL";
					log.info("usageCategoryCommercial:" + usageCategory);
				} else if (usageCategory.equals("INDUSTRIAL")) {
					usageCategory = "NONRESIDENTIAL.INDUSTRIAL";
					log.info("usageCategoryIndustrial:" + usageCategory);
				} else {
					usageCategory = "NONRESIDENTIAL.OTHERS";
					log.info("usageCategoryinothers:" + usageCategory);
				}

				property.setUsageCategory(usageCategory);
				log.info("usageCategoryfinal:" + usageCategory);
			} else {
				property.setUsageCategory("RESIDENTIAL");
			}

			List<OwnerInfo> owners = new ArrayList<>();
			owners.add(owner);
			property.setOwners(owners);

			property.setTenantId(conn.getTenantId());
			prequest.setProperty(property);
			// prequest
			// String response2= restTemplate.postForObject(host + "/" +
			// ptcreatehurl, prequest, String.class);
			log.info("PT search URL: "+ ptcreatehurl);
			log.info("PT Req URL: "+ property.toString());

			PropertyResponse res = restTemplate.postForObject(host + "/" + ptcreatehurl, prequest,
					PropertyResponse.class);
			Property property2 = res.getProperties().get(0);
			log.info(
					"create pt ws in workflow state " + property2.getPropertyId() + "  status" + property2.getStatus());
			return property2;

		} catch (Exception e) {
			recordService.recordError("water", tenantId, e.getMessage(), conn.getId());
			e.printStackTrace();
			try {
				String ptrequest = objectMapper.writeValueAsString(prequest);
				log.error("failed request " + ptrequest);
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return null;

	}

	public Property updateProperty(Property property2, String tenantId, RequestInfo requestInfo)
			throws InterruptedException {
		log.info("updatig property after 1 sec");
		try {

			PropertyRequest prequest = new PropertyRequest();
			prequest.setRequestInfo(requestInfo);

			property2.setSource(Source.WATER_CHARGES);
			ProcessInstance workflow = new ProcessInstance();
			workflow.setBusinessService("PT.CREATEWITHWNS");
			workflow.setAction("SUBMIT");
			workflow.setTenantId(tenantId);
			workflow.setModuleName("PT");
			workflow.setBusinessId(property2.getPropertyId());
			property2.setWorkflow(workflow);
			prequest.setProperty(property2);
			log.info("PT Update API: " + host + "/" + ptupdatehurl);
			PropertyResponse res2 = restTemplate.postForObject(host + "/" + ptupdatehurl, prequest,
					PropertyResponse.class);
			log.info("newly created pt" + res2.getProperties().get(0).getPropertyId() + " id    "
					+ res2.getProperties().get(0).getStatus());

			return res2.getProperties().get(0);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Property createSWProperty(SewerageConnectionRequest swg, Map json, String tenantId)
			throws InterruptedException {

		// String uuid = null;
		PropertyRequest prequest = new PropertyRequest();
		SewerageConnection conn = swg.getSewerageConnection();
		try {

			// set all property values
			prequest.setRequestInfo(swg.getRequestInfo());
			Property property = new Property();

			property.setAddress(conn.getApplicantAddress());
			property.setChannel(Channel.SYSTEM);

			Object landAreaObj = json.getOrDefault("plotsize", 2);
			if (landAreaObj instanceof Integer && (Integer) landAreaObj > 2) {
				property.setLandArea(BigDecimal.valueOf(Long.valueOf((Integer) landAreaObj)));

			} else if (landAreaObj instanceof Long && (Long) landAreaObj > 2) {
				property.setLandArea(BigDecimal.valueOf((Long) landAreaObj));

			} else {
				property.setLandArea(BigDecimal.valueOf(2));

			}
			property.setNoOfFloors(Long.valueOf(1));
			property.setOldPropertyId(conn.getPropertyId());
			property.setOwners(null);
			// fix this
			property.setOwnershipCategory("INDIVIDUAL.SINGLEOWNER");
			property.setPropertyType("BUILTUP.INDEPENDENTPROPERTY");

			property.setSource(Source.WATER_CHARGES);

			property.setTotalConstructedArea(BigDecimal.valueOf(190));
			property.setStatus(Status.ACTIVE);

			List<Unit> units = new ArrayList<>();
			property.setUnits(units);
			OwnerInfo owner = new OwnerInfo();
			owner.setName(conn.getApplicantname().replace('?', ' ').replace('`', ' '));
			owner.setMobileNumber(conn.getMobilenumber());
			owner.setFatherOrHusbandName(
					conn.getGuardianname() != null ? conn.getGuardianname().replace('?', ' ').replace('`', ' ') : null);
			owner.setOwnerType("NONE");
			property.creationReason(CreationReason.CREATE);

			if (json.getOrDefault("usage", null) != null) {
				String usageCategory = (String) json.get("usage");
				log.info("usageCategoryInitial:" + usageCategory);

				if (usageCategory.equals("DOMESTIC")) {
					usageCategory = "RESIDENTIAL";
					log.info("usageCategoryDomestic:" + usageCategory);
				} else if (usageCategory.equals("COMMERCIAL")) {
					usageCategory = "NONRESIDENTIAL.COMMERCIAL";
					log.info("usageCategoryCommercial:" + usageCategory);
				} else if (usageCategory.equals("INDUSTRIAL")) {
					usageCategory = "NONRESIDENTIAL.INDUSTRIAL";
					log.info("usageCategoryIndustrial:" + usageCategory);
				} else {
					usageCategory = "NONRESIDENTIAL.OTHERS";
					log.info("usageCategoryinothers:" + usageCategory);
				}

				property.setUsageCategory(usageCategory);
				log.info("usageCategoryfinal:" + usageCategory);
			} else {
				property.setUsageCategory("RESIDENTIAL");
			}

			owner.setGender((String) json.get("gender"));
			owner.setEmailId((String) json.getOrDefault("emailId", null));
			owner.setRelationship(Relationship.valueOf((String) json.getOrDefault("guardianrelation", "OTHER")));

			List<OwnerInfo> owners = new ArrayList<>();
			owners.add(owner);
			property.setOwners(owners);

			property.setTenantId(conn.getTenantId());
			prequest.setProperty(property);
			PropertyResponse res = null;

			res = restTemplate.postForObject(host + "/" + ptcreatehurl, prequest, PropertyResponse.class);
			Property property2 = res.getProperties().get(0);
			log.info(
					"create pt sw in workflow state " + property2.getPropertyId() + "  status" + property2.getStatus());
			return property2;
		} catch (Exception e) {
			recordService.recordError("sewerage", tenantId, e.getMessage(), conn.getId());
			e.printStackTrace();
			try {
				String ptrequest = objectMapper.writeValueAsString(prequest);
				log.error("failed request " + ptrequest);
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
			}
		}

		return null;

	}

	/**
	 * 
	 * @param conn
	 * @param data
	 * @param tenantId
	 * @return if property found compare with owner name,father name etc.
	 */

	private Property searchPtRecord(WaterConnectionRequest conn, Map data, String tenantId, String localityCode) {
		log.info("Inside Search Pt Rec");
		PropertyRequest pr = new PropertyRequest();
		pr.setRequestInfo(conn.getRequestInfo());
		log.debug("Searching property");
		String propertySeachURL = null;
//
//		log.info(" conn.getWaterConnection().getMobilenumber()  " + conn.getWaterConnection().getMobilenumber());
//		log.info(" conn.getWaterConnection().getApplicantname()  " + conn.getWaterConnection().getApplicantname());
//		log.info(" conn.getWaterConnection().getGuardianname() " + conn.getWaterConnection().getGuardianname());
		log.info(" localityCode " + localityCode);
		if (conn.getWaterConnection().getPropertyId() != null) {
			propertySeachURL = ptseachurl + "?tenantId=" + conn.getRequestInfo().getUserInfo().getTenantId()
					+ "&propertyIds=" + conn.getWaterConnection().getPropertyId();

		} else {

			propertySeachURL = ptseachurl + "?tenantId=" + conn.getRequestInfo().getUserInfo().getTenantId()
					+ "&mobileNumber=" + conn.getWaterConnection().getMobilenumber() + "&name="
					+ conn.getWaterConnection().getApplicantname() + "&guardianname="
					+ conn.getWaterConnection().getGuardianname() + "&locality=" + localityCode;
		}
		log.info(" Pt Rec url " + propertySeachURL);

		//
		// String propertySeachURL = ptseachurl + "?tenantId=" +
		// conn.getRequestInfo().getUserInfo().getTenantId()
		// + "&mobileNumber=" + conn.getWaterConnection().getMobilenumber()
		// +"&locality="+ localityCode;

		PropertySearchResponse response = restTemplate.postForObject(host + "/" + propertySeachURL, pr,
				PropertySearchResponse.class);
		log.info("entered in Search pt recordsss " + propertySeachURL);

		if (response != null && response.getProperties() != null && response.getProperties().size() >= 1) {

			for (Property property : response.getProperties()) {

				for (OwnerInfo owner : property.getOwners()) {

					// if
					// (owner.getName().equalsIgnoreCase(conn.getWaterConnection().getApplicantname())
					// &&
					// owner.getFatherOrHusbandName().equalsIgnoreCase(conn.getWaterConnection().getGuardianname())
					// &&
					// property.getAddress().getLocality() != null &&
					// localityCode.equalsIgnoreCase(property.getAddress().getLocality().getCode())
					//
					// ) {
					String ownername = owner.getName();
					ownername = ownername.toUpperCase();


					String ownerfathername = owner.getFatherOrHusbandName();
					if (ownerfathername != null) {
						ownerfathername = ownerfathername.toUpperCase();

					}
					String mob = owner.getMobileNumber();


					String propertyname = conn.getWaterConnection().getApplicantname();
					propertyname = propertyname.toUpperCase();


					String propertyguardianname = conn.getWaterConnection().getGuardianname();
					if (propertyguardianname != null) {
						propertyguardianname = propertyguardianname.toUpperCase();

					}
					String phone = conn.getWaterConnection().getMobilenumber();


					String local = localityCode;


					String proplocal = property.getAddress().getLocality().getCode();


					if (ownerfathername != null && propertyguardianname != null) {
						if (ownername.equalsIgnoreCase(propertyname)
								&& ownerfathername.equalsIgnoreCase(propertyguardianname)
								&& property.getAddress().getLocality() != null && local.equalsIgnoreCase(proplocal)) {
							log.info("Everything Matched");

							recordService.recordError("water", tenantId, "Found Property in digit :" + property.getId(),
									conn.getWaterConnection().getId());
							return property;
						}
					}

					else {
						if (ownername.equalsIgnoreCase(propertyname) && property.getAddress().getLocality() != null
								&& local.equalsIgnoreCase(proplocal)) {
							log.info("owner father name is  null ");

							recordService.recordError("water", tenantId, "Found Property in digit :" + property.getId(),
									conn.getWaterConnection().getId());
							return property;
						}
					}

				}
			}
		} else {
			log.info("Property not  found");

		}

		return null;

	}

	private String toLowerCase(String ownername) {
		// TODO Auto-generated method stub
		return null;
	}

	private String tolower(String ownername) {
		// TODO Auto-generated method stub
		return null;
	}

	private Property searchswPtRecord(SewerageConnectionRequest conn, Map json, String tenantId, String localityCode) {

		PropertyRequest pr = new PropertyRequest();
		pr.setRequestInfo(conn.getRequestInfo());
		// String Property1;
		log.info("locc code23:- " + localityCode);
		log.info("tenant code 23 :- " + conn.getRequestInfo().getUserInfo().getTenantId());
		log.info("mobi code:- " + conn.getSewerageConnection().getMobilenumber());
		log.info("app name code:- " + conn.getSewerageConnection().getApplicantname());
		log.info("guardian code:- " + conn.getSewerageConnection().getGuardianname());

		log.info("url code23:- " + ptseachurl);
		String ptseachurlStr = ptseachurl + "?tenantId=" + conn.getRequestInfo().getUserInfo().getTenantId()
				+ "&mobileNumber=" + conn.getSewerageConnection().getMobilenumber() + "&name="
				+ conn.getSewerageConnection().getApplicantname() + "&guardianname="
				+ conn.getSewerageConnection().getGuardianname() + "&locality=" + localityCode;

		log.info("url code23:- " + ptseachurlStr);
		// String ptseachurlStr = ptseachurl + "?tenantId=" +
		// conn.getRequestInfo().getUserInfo().getTenantId()
		// + "&mobileNumber=" + conn.getSewerageConnection().getMobilenumber() +
		// "&locality="
		// + localityCode;
		//
		log.debug("Searching property with url: " + ptseachurlStr);

		PropertySearchResponse response = restTemplate.postForObject(host + "/" + ptseachurlStr, pr,
				PropertySearchResponse.class);

		if (response != null && response.getProperties() != null && response.getProperties().size() >= 1) {

			for (Property property : response.getProperties()) {

				for (OwnerInfo owner : property.getOwners()) {

					log.info("tenant code1 23 :- " + owner.getName());
					log.info("mobi1 code:- " + owner.getFatherOrHusbandName());
					log.info("app name 1 code:- " + localityCode);
					Status status = property.getStatus();
					String statusCode = status.getValue();

					log.info(" Status of Property Is :- " + statusCode);
					if (statusCode == "ACTIVE")

					{
						if (owner.getFatherOrHusbandName() != null) {

							if (owner.getName().equalsIgnoreCase(conn.getSewerageConnection().getApplicantname())
									&& owner.getFatherOrHusbandName()
											.equalsIgnoreCase(conn.getSewerageConnection().getGuardianname())
									&& property.getAddress().getLocality() != null
									&& localityCode.equalsIgnoreCase(property.getAddress().getLocality().getCode())

							) {

								log.info("Entered where ownwer name is not null and property is null");
								recordService.recordError("sewerage", tenantId,
										"Found Property in digit :" + property.getId(),
										conn.getSewerageConnection().getId());
								return property;

							}
						}

						else if (owner.getFatherOrHusbandName() == null
								&& owner.getName().equalsIgnoreCase(conn.getSewerageConnection().getApplicantname())
								&& property.getAddress().getLocality() != null
								&& localityCode.equalsIgnoreCase(property.getAddress().getLocality().getCode())) {
							log.info("Entered where ownwer name is null and property is null");
							recordService.recordError("sewerage", tenantId,
									"Found Property in digit :" + property.getId(),
									conn.getSewerageConnection().getId());
							return property;

						}

					} else {
						log.info("After Checking Property Is :- " + statusCode);
					}
				}
			}
		} else {

			log.info("Property not  found");
		}

		return null;

	}

	private Property searchPropertyAfterCreate(String tenantId, String propertyId, RequestInfo requestInfo,
			Property property2) {
		RequestInfoWrapper wrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

		PropertyResponse res = restTemplate.postForObject(
				host + "/" + ptseachurl + "?tenantId=" + tenantId + "&propertyIds=" + propertyId, wrapper,
				PropertyResponse.class);
		if (!res.getProperties().isEmpty() && res.getProperties().size() == 1) {
			return res.getProperties().get(0);
		}
		return property2;

	}
}
