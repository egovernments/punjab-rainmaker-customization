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
				 log.debug("Propery not found creating new property");
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
		if (conn.getPlotSize() != null && conn.getPlotSize() > 2)
			property.setLandArea(BigDecimal.valueOf(conn.getPlotSize()));
		else
			property.setLandArea(BigDecimal.valueOf(2));
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
		owner.setName(conn.getApplicantname());
		owner.setMobileNumber(conn.getMobilenumber());
		owner.setFatherOrHusbandName(conn.getGuardianname());
		owner.setOwnerType("NONE");
		owner.setGender((String)data.get("gender"));
		owner.setEmailId((String)data.getOrDefault("emailId", null));
		owner.setRelationship(Relationship.valueOf((String)data.getOrDefault("guardianrelation", "OTHER")));

		property.creationReason(CreationReason.CREATE);
		 
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

		try {
			PropertyResponse res = restTemplate.postForObject(host + "/" + ptcreatehurl, prequest, PropertyResponse.class);
			Property	property2=	res.getProperties().get(0);
			log.info("create pt ws in workflow state "+property2.getPropertyId() +"  status"+property2.getStatus() );
			 return property2;
			
		} catch (Exception e) {
			recordService.recordError("water", tenantId, e.getMessage(), conn.getId());
			try {
				String ptrequest=objectMapper.writeValueAsString(prequest);
				log.error("failed request " +ptrequest);
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return null;
 
	}

	public Property updateProperty(Property property2, String tenantId , RequestInfo requestInfo)
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
			PropertyResponse res2 = restTemplate.postForObject(host + "/" + ptupdatehurl, prequest, PropertyResponse.class);
			log.info("newly created pt" + res2.getProperties().get(0).getPropertyId() + " id    "
					+ res2.getProperties().get(0).getStatus());
		 
			return res2.getProperties().get(0);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Property createSWProperty(SewerageConnectionRequest swg, Map json, String tenantId) throws InterruptedException {

//		String uuid = null;
		PropertyRequest prequest = new PropertyRequest();
		prequest.setRequestInfo(swg.getRequestInfo());
		Property property = new Property();
		SewerageConnection conn = swg.getSewerageConnection();
		// set all property values

		property.setAddress(conn.getApplicantAddress());
		property.setChannel(Channel.SYSTEM);
 
		Object landAreaObj = json.getOrDefault("plotsize", 2);
		if(landAreaObj instanceof Integer && (Integer)landAreaObj > 2) {
			property.setLandArea(BigDecimal.valueOf(Long.valueOf((Integer)landAreaObj)));
			
		}else if(landAreaObj instanceof Long && (Long)landAreaObj > 2){
			property.setLandArea(BigDecimal.valueOf((Long)landAreaObj));
			
		} else {
			property.setLandArea(BigDecimal.valueOf(2));
			
		}
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
		property.setUnits(units);
		OwnerInfo owner = new OwnerInfo();
		owner.setName(conn.getApplicantname());
		owner.setMobileNumber(conn.getMobilenumber());
		owner.setFatherOrHusbandName(conn.getGuardianname());
		owner.setOwnerType("NONE");
		property.creationReason(CreationReason.CREATE);
		property.setUsageCategory("RESIDENTIAL");
		owner.setGender((String)json.get("gender"));
		owner.setEmailId((String)json.getOrDefault("emailId", null));
		owner.setRelationship(Relationship.valueOf((String)json.getOrDefault("guardianrelation", "OTHER")));


		List<OwnerInfo> owners = new ArrayList<>();
		owners.add(owner);
		property.setOwners(owners);

		property.setTenantId(conn.getTenantId());
		prequest.setProperty(property);
		PropertyResponse res = null;
		try {
			res = restTemplate.postForObject(host + "/" + ptcreatehurl, prequest, PropertyResponse.class);
			Property property2 = res.getProperties().get(0);
			log.info("create pt sw in workflow state "+property2.getPropertyId() +"  status"+property2.getStatus() );
			return property2;
		} catch (Exception e) {
			recordService.recordError("sewerage", tenantId, e.getMessage(), conn.getId());
			try {
				String ptrequest=objectMapper.writeValueAsString(prequest);
				log.error("failed request " +ptrequest);
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
	 * @return
	 *  if property found compare with owner name,father name etc.
	 */

	private Property searchPtRecord(WaterConnectionRequest conn, Map data, String tenantId) {

		PropertyRequest pr = new PropertyRequest();
		pr.setRequestInfo(conn.getRequestInfo());
		log.debug("Searching property");

		String propertySeachURL = ptseachurl + "?tenantId=" + conn.getRequestInfo().getUserInfo().getTenantId()
				+ "&mobileNumber=" + conn.getWaterConnection().getMobilenumber() + "&name="
				+ conn.getWaterConnection().getApplicantname();

		PropertySearchResponse response = restTemplate.postForObject(host + "/" + propertySeachURL, pr,
				PropertySearchResponse.class);
	 
		if (response != null && response.getProperties() != null && response.getProperties().size() >= 1) {
		 
			for (Property property : response.getProperties()) {
				 
				for (OwnerInfo owner : property.getOwners()) {
					 

					if (owner.getName().equalsIgnoreCase(conn.getWaterConnection().getApplicantname()) && owner
							.getFatherOrHusbandName().equalsIgnoreCase(conn.getWaterConnection().getGuardianname())

					) {

						recordService.recordError("water", tenantId, "Found Property in digit :" + property.getId(),
							conn.getWaterConnection().getId());
						 
						 
						return property;
					}

				}
			}
		} else {
			log.info("Property not  found");

			 
		}

		return null;

	}

	private Property searchswPtRecord(SewerageConnectionRequest conn, Map json, String tenantId) {

		PropertyRequest pr = new PropertyRequest();
		pr.setRequestInfo(conn.getRequestInfo());
		log.debug("Searching property");
		String ptseachurlStr = ptseachurl + "?tenantId=" + conn.getRequestInfo().getUserInfo().getTenantId()
				+ "&mobileNumber=" + conn.getSewerageConnection().getMobilenumber() + "&name="
				+ conn.getSewerageConnection().getApplicantname();

		PropertySearchResponse response = restTemplate.postForObject(host + "/" + ptseachurlStr, pr,
				PropertySearchResponse.class);

		 

		 
		if (response != null && response.getProperties() != null && response.getProperties().size() >= 1) {
		 
			for (Property property : response.getProperties()) {
				 
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
			log.info("Property not  found"); 
		}

		return null;

	}


    private Property searchPropertyAfterCreate(String tenantId, String propertyId, RequestInfo requestInfo, Property property2) {
    	RequestInfoWrapper wrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
    	
		PropertyResponse res = restTemplate.postForObject(host + "/" + ptseachurl + "?tenantId="+tenantId+"&propertyIds=" + propertyId , wrapper, PropertyResponse.class);
		if(!res.getProperties().isEmpty() && res.getProperties().size()==1) {
			return res.getProperties().get(0);
		}
		return property2;
    	
    }
}
