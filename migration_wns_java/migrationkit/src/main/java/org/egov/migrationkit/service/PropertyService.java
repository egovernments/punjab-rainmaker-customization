package org.egov.migrationkit.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.Channel;
import io.swagger.client.model.CreationReason;
import io.swagger.client.model.OwnerInfo;
import io.swagger.client.model.ProcessInstance;
import io.swagger.client.model.Property;
import io.swagger.client.model.PropertyRequest;
import io.swagger.client.model.PropertyResponse;
import io.swagger.client.model.PropertySearchResponse;
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

	public Property findProperty(WaterConnectionRequest wcr, Map data, String tenantId) {

		Property property = null;
		try {
			property = searchPtRecord(wcr, data, tenantId);

			if (property == null) {
				// log.debug("Propery not found creating new property");
				property = createProperty(wcr, data, tenantId);

			}
		} catch (Exception e) {
			log.error("error while finding or creating property", e.getMessage());
			recordService.recordError("water", tenantId, e.getMessage(), wcr.getWaterConnection().getId());
		}

		return property;
	}

	public Property findProperty(SewerageConnectionRequest swg, Map json, String tenantId) {
		Property property = null;
		try {
			property = searchswPtRecord(swg, json, tenantId);
			if (property == null) {
				// log.debug("Propery not found creating new property");
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
		prequest.setRequestInfo(wcr.getRequestInfo());
		Property property = new Property();
		WaterConnection conn = wcr.getWaterConnection();
		// set all property values

		property.setAddress(conn.getApplicantAddress());
		property.setChannel(Channel.SYSTEM);
		property.setSource(Source.WATER_CHARGES);
		if (conn.getPlotSize() != null)
			property.setLandArea(BigDecimal.valueOf(conn.getPlotSize()));
		else
			property.setLandArea(BigDecimal.valueOf(125));
		property.setNoOfFloors(Long.valueOf(1));
		property.setOldPropertyId(conn.getPropertyId());
		property.setOwners(null);
		// fix this
		property.setOwnershipCategory("INDIVIDUAL.SINGLEOWNER");
		property.setPropertyType("BUILTUP.INDEPENDENTPROPERTY");

		property.setTotalConstructedArea(BigDecimal.valueOf(190));
		property.setStatus(Status.ACTIVE);
		List<Unit> units = new ArrayList<>();
		// units.add(new Unit());
		property.setUnits(units);
		OwnerInfo owner = new OwnerInfo();
		owner.setName(conn.getApplicantname());
		owner.setMobileNumber(conn.getMobilenumber());
		owner.setFatherOrHusbandName(conn.getGuardianname());
		owner.setOwnerType("NONE");
		owner.setGender((String)data.get("gender"));

		property.creationReason(CreationReason.CREATE);
		// log.info("conn.getPropertyType() :" + conn.getPropertyType());
		if (conn.getPropertyType() != null) {
			property.setUsageCategory(conn.getPropertyType().toUpperCase().replace(" ", ""));
		} else {
			property.setUsageCategory("RESIDENTIAL");
		}

		List<OwnerInfo> owners = new ArrayList<>();
		owners.add(owner);
		property.setOwners(owners);

		property.setTenantId(conn.getTenantId());
		prequest.setProperty(property);

		// String response2= restTemplate.postForObject(host + "/" +
		// ptcreatehurl, prequest, String.class);

		PropertyResponse res = restTemplate.postForObject(host + "/" + ptcreatehurl, prequest, PropertyResponse.class);

		// log.info(res.getProperties().get(0).getSource() +"
		// "+res.getProperties().get(0).getAcknowldgementNumber());
		// return res.getProperties().get(0);
		Thread.sleep(2000);
		Property property2 = res.getProperties().get(0);
		property2.setSource(Source.WATER_CHARGES);
		ProcessInstance workflow = new ProcessInstance();
		workflow.setBusinessService("PT.CREATEWITHWNS");
		workflow.setAction("SUBMIT");
		workflow.setTenantId(conn.getTenantId());
		workflow.setModuleName("PT");
		workflow.setBusinessId(property2.getPropertyId());
		property2.setWorkflow(workflow);
		prequest.setProperty(property2);
		PropertyResponse res2 = restTemplate.postForObject(host + "/" + ptupdatehurl, prequest, PropertyResponse.class);
		log.info("newly created pt" + res2.getProperties().get(0).getPropertyId() + " id    "
				+ res2.getProperties().get(0).getStatus());
		return res2.getProperties().get(0);

	}

	private Property createSWProperty(SewerageConnectionRequest swg, Map json, String tenantId) {
//		String uuid = null;
		PropertyRequest prequest = new PropertyRequest();
		prequest.setRequestInfo(swg.getRequestInfo());
		Property property = new Property();
		SewerageConnection conn = swg.getSewerageConnection();
		// set all property values

		property.setAddress(conn.getApplicantAddress());
		property.setChannel(Channel.SYSTEM);
		// property.setInstitution(null);
//		property.setLandArea(BigDecimal.valueOf(50));
		long plotsize = Long.valueOf((Integer)json.getOrDefault("plotsize", 125));
		if (plotsize>0)
			property.setLandArea(BigDecimal.valueOf(plotsize));
		else
			property.setLandArea(BigDecimal.valueOf(125));
		property.setNoOfFloors(Long.valueOf(1));
		property.setOldPropertyId(conn.getPropertyId());
		property.setOwners(null);
		// fix this
		property.setOwnershipCategory("INDIVIDUAL.SINGLEOWNER");
		property.setPropertyType("BUILTUP.INDEPENDENTPROPERTY");

		// may have to change to seweragecharges
		property.setSource(Source.WATER_CHARGES);

		property.setTotalConstructedArea(BigDecimal.valueOf(190));
		property.setStatus(Status.ACTIVE);
		List<Unit> units = new ArrayList<>();
		// units.add(new Unit());
		property.setUnits(units);
		OwnerInfo owner = new OwnerInfo();
		owner.setName(conn.getApplicantname());
		owner.setMobileNumber(conn.getMobilenumber());
		owner.setFatherOrHusbandName(conn.getGuardianname());
		owner.setOwnerType("NONE");
		property.creationReason(CreationReason.CREATE);
		property.setUsageCategory("RESIDENTIAL");
		owner.setGender((String)json.get("gender"));

		List<OwnerInfo> owners = new ArrayList<>();
		owners.add(owner);
		property.setOwners(owners);

		property.setTenantId(conn.getTenantId());
		prequest.setProperty(property);
		PropertyResponse res = null;
		try {
			res = restTemplate.postForObject(host + "/" + ptcreatehurl, prequest, PropertyResponse.class);
			Property property2 = res.getProperties().get(0);
			ProcessInstance workflow = new ProcessInstance();
			workflow.setBusinessService("PT.CREATEWITHWNS");
			workflow.setAction("SUBMIT");
			workflow.setTenantId(conn.getTenantId());
			workflow.setModuleName("PT");
			workflow.setBusinessId(property2.getPropertyId());
			property2.setWorkflow(workflow);
			prequest.setProperty(property2);
			PropertyResponse res2 = restTemplate.postForObject(host + "/" + ptupdatehurl, prequest,
					PropertyResponse.class);
			return res2.getProperties().get(0);
		} catch (RestClientException e) {
			recordService.recordError("sewerage", tenantId, e.getMessage(), conn.getId());
		}

		return null;

	}

	private Property searchPtRecord(WaterConnectionRequest conn, Map data, String tenantId) {

		PropertyRequest pr = new PropertyRequest();
		pr.setRequestInfo(conn.getRequestInfo());
		// String mobileNumber = conn.getWaterConnection().getMobilenumber() !=
		// null || conn.getWaterConnection().getMobilenumber() != "" ?
		// conn.getWaterConnection().getMobilenumber() : "9876543210";

		String propertySeachURL = ptseachurl + "?tenantId=" + conn.getRequestInfo().getUserInfo().getTenantId()
				+ "&mobileNumber=" + conn.getWaterConnection().getMobilenumber() + "&name="
				+ conn.getWaterConnection().getApplicantname();

		PropertySearchResponse response = restTemplate.postForObject(host + "/" + propertySeachURL, pr,
				PropertySearchResponse.class);

		// if property found compare with owner name,father name etc.

		// && property.getStatus().equals(Status.ACTIVE) not used
		if (response != null && response.getProperties() != null && response.getProperties().size() >= 1) {
			// log.debug("found properties" + response.getProperties().size());
			for (Property property : response.getProperties()) {
				/*
				 * log.debug("status" + property.getPropertyId() + "---" +
				 * property.getStatus() + " Usage :" +
				 * property.getUsageCategory());
				 */
				for (OwnerInfo owner : property.getOwners()) {
					// log.debug("owner.getName() : " + owner.getName());
					// log.debug("owner.getFatherOrHusbandName() : " +
					// owner.getFatherOrHusbandName());

					if (owner.getName().equalsIgnoreCase(conn.getWaterConnection().getApplicantname()) && owner
							.getFatherOrHusbandName().equalsIgnoreCase(conn.getWaterConnection().getGuardianname())

					) {

						recordService.recordError("water", tenantId, "Found Property in digit :" + property.getId(),
								conn.getWaterConnection().getId());
						// log.info("no property found in digit system for
						// mobilenumber--"
						// + conn.getWaterConnection().getMobilenumber());
						return property;
					}

				}
			}
		} else {
			/*
			 * log.info("no  property found in digit system for mobilenumber--"
			 * + conn.getWaterConnection().getMobilenumber());
			 */
		}

		return null;

	}

	private Property searchswPtRecord(SewerageConnectionRequest conn, Map json, String tenantId) {

		PropertyRequest pr = new PropertyRequest();
		pr.setRequestInfo(conn.getRequestInfo());

		String ptseachurlStr = ptseachurl + "?tenantId=" + conn.getRequestInfo().getUserInfo().getTenantId()
				+ "&mobileNumber=" + conn.getSewerageConnection().getMobilenumber() + "&applicantName="
				+ conn.getSewerageConnection().getApplicantname();

		PropertySearchResponse response = restTemplate.postForObject(host + "/" + ptseachurlStr, pr,
				PropertySearchResponse.class);

		// String response = restTemplate.postForObject(host + "/" + ptseachurl,
		// pr, String.class);

		// System.out.println("response" + response);

		// if property found compare with owner name,father name etc.
		if (response != null && response.getProperties() != null && response.getProperties().size() >= 1) {
			// log.info("found properties" + response.getProperties().size());
			for (Property property : response.getProperties()) {
				// log.info("status" + property.getPropertyId() + "---" +
				// property.getStatus());
				for (OwnerInfo owner : property.getOwners()) {
					if (owner.getName().equalsIgnoreCase(conn.getSewerageConnection().getApplicantname()) && owner
							.getFatherOrHusbandName().equalsIgnoreCase(conn.getSewerageConnection().getGuardianname())

					) {

						recordService.recordError("sewerage", tenantId, "Found Property in digit :" + property.getId(),
								conn.getSewerageConnection().getId());
						return property;

					}

				}
			}
		} else {
			/*
			 * log.info("no  property found in digit system for mobilenumber--"
			 * + conn.getSewerageConnection().getMobilenumber());
			 */
		}

		return null;

	}

}
