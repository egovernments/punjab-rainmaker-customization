package org.egov.migrationkit.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.Channel;
import io.swagger.client.model.CreationReason;
import io.swagger.client.model.OwnerInfo;
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
	
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private RecordService recordService;
	
	
	public Property findProperty(WaterConnectionRequest wcr,Map data)
	{
		
		 
		Property property=null;
		try {
			property = searchPtRecord(wcr,data);
			
				
			if(property==null)
			{
			  log.info("Propery not found creating new property");
				property=createProperty(wcr,data);
			  
			}
		} catch (Exception e) {
			log.error("error while finding or creating property",e.getMessage());
			recordService.recordError("water", e.getMessage(), wcr.getWaterConnection().getId());
		}
		
			
		return property;
	}
	

	public Property findProperty(SewerageConnectionRequest swg,String json)
	{
		Property property=null;
		try {
		
			property=searchswPtRecord(swg,json);
		
			
			if(property==null)
			{
				log.info("Propery not found creating new property");
				property=createSProperty(swg,json);
		  
			}
		} catch (Exception e) {
			log.error("error while finding or creating property",e.getMessage());
			log.error("Display proper message: " + e);
			recordService.recordError("sewerage", e.getMessage(), swg.getSewerageConnection().getId());
		}
			
		return property;
	}
	private Property createProperty(WaterConnectionRequest wcr, Map data ) {
		String uuid=null;
		 PropertyRequest prequest=new PropertyRequest();
		 prequest.setRequestInfo(wcr.getRequestInfo());
		 Property property=new Property();
		 WaterConnection conn=	 wcr.getWaterConnection();
		 //set all property values
		
		 property.setAddress(conn.getApplicantAddress());
		 property.setChannel(Channel.SYSTEM);
		// property.setInstitution(null);
		 property.setLandArea(BigDecimal.valueOf(50));
		 property.setNoOfFloors(Long.valueOf(1));
		 property.setOldPropertyId(conn.getPropertyId());
		 property.setOwners(null);
		 //fix this
		 property.setOwnershipCategory("INDIVIDUAL.SINGLEOWNER");
		 property.setPropertyType("BUILTUP.INDEPENDENTPROPERTY");
		 property.setSource(Source.MUNICIPAL_RECORDS);
	 
		 property.setTotalConstructedArea(BigDecimal.valueOf(190));
		 property.setStatus(Status.ACTIVE);
		  List<Unit> units=new ArrayList<>();
		  //units.add(new Unit());
		 property.setUnits(units);
		 OwnerInfo owner=new OwnerInfo();
		 owner.setName(conn.getApplicantname());
		 owner.setMobileNumber(conn.getMobilenumber());
		 owner.setFatherOrHusbandName(conn.getGuardianname());
		 owner.setOwnerType("NONE");
		 property.creationReason(CreationReason.CREATE);
		 property.setUsageCategory((String) data.get("connectionCategory"));
		 
		 List<OwnerInfo> owners=new ArrayList<>();
		 owners.add(owner);
		 property.setOwners(owners);
 
		 property.setTenantId(conn.getTenantId());
		 prequest.setProperty(property);
		 PropertyResponse res=	 restTemplate.postForObject(host + "/" + ptcreatehurl, prequest, PropertyResponse.class);
		 log.info(res.toString());

		 return res.getProperties().get(0);
		 
	}
	
	private Property createSProperty(SewerageConnectionRequest swg, String json) {
		String uuid=null;
		 PropertyRequest prequest=new PropertyRequest();
		 prequest.setRequestInfo(swg.getRequestInfo());
		 Property property=new Property();
		 SewerageConnection conn=	 swg.getSewerageConnection();
		 //set all property values
		
		 property.setAddress(conn.getApplicantAddress());
		 property.setChannel(Channel.SYSTEM);
		// property.setInstitution(null);
		 property.setLandArea(BigDecimal.valueOf(50));
		 property.setNoOfFloors(Long.valueOf(1));
		 property.setOldPropertyId(conn.getPropertyId());
		 property.setOwners(null);
		 //fix this
		 property.setOwnershipCategory("INDIVIDUAL.SINGLEOWNER");
		 property.setPropertyType("BUILTUP.INDEPENDENTPROPERTY");
		 property.setSource(Source.MUNICIPAL_RECORDS);
	 
		 property.setTotalConstructedArea(BigDecimal.valueOf(190));
		 property.setStatus(Status.ACTIVE);
		  List<Unit> units=new ArrayList<>();
		  //units.add(new Unit());
		 property.setUnits(units);
		 OwnerInfo owner=new OwnerInfo();
		 owner.setName(conn.getApplicantname());
		 owner.setMobileNumber(conn.getMobilenumber());
		 owner.setFatherOrHusbandName(conn.getGuardianname());
		 owner.setOwnerType("NONE");
		 property.creationReason(CreationReason.CREATE);
		 property.setUsageCategory("RESIDENTIAL");
		 
		 List<OwnerInfo> owners=new ArrayList<>();
		 owners.add(owner);
		 property.setOwners(owners);
 
		 property.setTenantId(conn.getTenantId());
		 prequest.setProperty(property);
		 PropertyResponse res=	 restTemplate.postForObject(host + "/" + ptcreatehurl, prequest, PropertyResponse.class);
		 log.info(res.toString());

		 return res.getProperties().get(0);
		 
	}

	private Property searchPtRecord(WaterConnectionRequest conn,Map data) {
		 
		PropertyRequest pr=new PropertyRequest();
		pr.setRequestInfo(conn.getRequestInfo());
	//	String mobileNumber = conn.getWaterConnection().getMobilenumber() != null || conn.getWaterConnection().getMobilenumber() != "" ? conn.getWaterConnection().getMobilenumber() : "9876543210";
 
		String propertySeachURL=ptseachurl+"?tenantId="+conn.getRequestInfo().getUserInfo().getTenantId()+
				"&mobileNumber="+conn.getWaterConnection().getMobilenumber();
 
		PropertySearchResponse response = restTemplate.postForObject(host + "/" + propertySeachURL, pr, PropertySearchResponse.class);

				//if property found compare with owner name,father name etc.
		
		//&& property.getStatus().equals(Status.ACTIVE) not used
		if(response!=null && response.getProperties()!=null && response.getProperties().size() >=1 )
		{
			log.info("found properties"+response.getProperties().size());
			for(Property property:response.getProperties())
			{
				log.info("status"+property.getPropertyId()+"---"+property.getStatus());
				
				
				for(OwnerInfo owner:property.getOwners())
				{
					log.info("owner.getName() : "+owner.getName() );
					log.info("owner.getFatherOrHusbandName() : "+owner.getFatherOrHusbandName());
					
					if( 
						owner.getName().equalsIgnoreCase(conn.getWaterConnection().getApplicantname())
					    &&
						owner.getFatherOrHusbandName().equalsIgnoreCase(conn.getWaterConnection().getGuardianname())
						
					 
						)
 
						
						return property;
					
				}
			}
		}else
		{
			log.info("no  property found in digit system for mobilenumber--"+conn.getWaterConnection().getMobilenumber());
		}
		
			
			return null;
		 
		
	}
	private Property searchswPtRecord(SewerageConnectionRequest conn,String json) {
		 
		PropertyRequest pr=new PropertyRequest();
		pr.setRequestInfo(conn.getRequestInfo());
 
		String ptseachurlStr=ptseachurl+"?tenantId="+conn.getRequestInfo().getUserInfo().getTenantId()+
				"&mobileNumber="+conn.getSewerageConnection().getMobilenumber();
 
 
		PropertySearchResponse response = restTemplate.postForObject(host + "/" + ptseachurlStr, pr, PropertySearchResponse.class);
		
  
	//	String response = restTemplate.postForObject(host + "/" + ptseachurl, pr, String.class);
		
		System.out.println("response"+response);
		
		//if property found compare with owner name,father name etc.
		if(response!=null && response.getProperties()!=null && response.getProperties().size() >=1 )
		{
			log.info("found properties"+response.getProperties().size());
			for(Property property:response.getProperties())
			{
				log.info("status"+property.getPropertyId()+"---"+property.getStatus());
				for(OwnerInfo owner:property.getOwners())
				{
					if( 
						owner.getName().equalsIgnoreCase(conn.getSewerageConnection().getApplicantname())
					    &&
						owner.getFatherOrHusbandName().equalsIgnoreCase(conn.getSewerageConnection().getGuardianname())
						&&
						property.getStatus().equals(Status.ACTIVE)
					 
						)
 
						
						return property;
					
				}
			}
		}else
		{
			log.info("no  property found in digit system for mobilenumber--"+conn.getSewerageConnection().getMobilenumber());
		}
		
			
			return null;
		 
		
	}

}
