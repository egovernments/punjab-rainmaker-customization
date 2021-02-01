package org.egov.migrationkit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.client.model.OwnerInfo;
import io.swagger.client.model.Property;
import io.swagger.client.model.PropertyRequest;
import io.swagger.client.model.PropertyResponse;
import io.swagger.client.model.PropertySearchResponse;
import io.swagger.client.model.WaterConnectionRequest;
@Service
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
	
	
	public String findProperty(WaterConnectionRequest wcr,String json)
	{
		
	 
		String uuid=searchPtRecord(wcr,json);
			
		if(uuid==null)
		  uuid=createProperty(wcr,json);
			
		return uuid;
	}

	private String createProperty(WaterConnectionRequest wcr, String json) {
		String uuid=null;
		 PropertyRequest prequest=new PropertyRequest();
		 prequest.setRequestInfo(wcr.getRequestInfo());
		 Property property=new Property();
		 //set all property values
		 
		 property.setProperties.settenantId() = wcr.getWaterConnection().gettenantId();
		 //property.setProperties.setaddress().setlocality = 							;
		 property.setProperties.setpropertyDetail().setusageCategoryMajor() = wcr.getWaterConnection().getconnectionCategory();
		 
		 if (property.setProperties.setpropertyDetail().setusageCategoryMajor()=='Commercial')          
			 //property.setProperties.setpropertyDetail().setbusinessName();
		 
	
		 
		 property.setProperties.setpropertyDetail().setpropertytype()= wcr.getWaterConnection().getconnectionType();
		 

	     if (property.setProperties.setpropertyDetail().setpropertytype() == "Flat/Part of the building") {
	    	 property.setProperties.setpropertyDetail().setunits.setunitType() = wcr.getWaterConnection().getconnectionCategory();
	    	 //property.setProperties.setpropertyDetail().setunits.setusageCategoryMinor()= 
	    	 property.setProperties.setpropertyDetail().setunits.setoccupancyType()= "Self-Occupied";
	    	 property.setProperties.setpropertyDetail().setunits.setunitArea()= 50;
	    	 property.setProperties.setpropertyDetail().setunits.setfloorNo()=	"Ground Floor";
	    			 
	    			 
	     }
	     if (property.setProperties.setpropertyDetail().setpropertytype() =="Independent Building") {
	    	 //property.setProperties.setpropertyDetail().setbuildUpArea()=
	    	 property.setProperties.setpropertyDetail().setnoOfFloors()=	1;	 
	    	 
	     }
	     
	     if (property.setProperties.setpropertyDetail().setpropertytype()=="Vacant Land") {
	    	//property.setProperties.setpropertyDetail().setbuildUpArea()=
	     }
	     
	     
	    	 
	    	 
	    	 
	     property.setProperties.setpropertyDetail().setownershipCategory()== "Single"
	    		property.setProperties.setpropertyDetail().setowners.setname() = wcr.getWaterConnection().getplumberInfo().getname();	 	
	     		property.setProperties.setpropertyDetail().setowners.setmobileNumber() = wcr.getWaterConnection().getplumberInfo().getmobileNumber();
	     		property.setProperties.setpropertyDetail().setowners.setgender() = wcr.getWaterConnection().getplumberInfo().gender();
	     		property.setProperties.setpropertyDetail().setowners.setfatherOrHusbandName() = wcr.getWaterConnection().getplumberInfo().getfatherOrHusbandName();
	     		property.setProperties.setpropertyDetail().setowners.setrelationship() = wcr.getWaterConnection().getplumberInfo().getrelationship();
	     		 

		 //if required information not found write query to find data from erp system 
		// property.setLandArea(landArea);  fill this
		 
		 PropertyResponse res=	 restTemplate.postForObject(host + "/" + ptcreatehurl, prequest, PropertyResponse.class);
		
		 if(res!=null)
		 {
			uuid= res.getProperty().getPropertyId();
		 }
		 
		 return uuid;
		 
	}

	private String searchPtRecord(WaterConnectionRequest conn,String json) {
		 
		PropertyRequest pr=new PropertyRequest();
		pr.setRequestInfo(conn.getRequestInfo());
		ptseachurl=ptseachurl+"?tenantId="+conn.getRequestInfo().getUserInfo().getTenantId()+
				"&mobileNumber="+conn.getWaterConnection().getMobilenumber();
		
		
		PropertySearchResponse response = restTemplate.postForObject(host + "/" + ptseachurl, pr, PropertySearchResponse.class);
		
  
	//	String response = restTemplate.postForObject(host + "/" + ptseachurl, pr, String.class);
		
	System.out.println("response"+response);
		
		//if property found compare with owner name,father name etc.
		if(response!=null && response.getProperties()!=null && response.getProperties().size() >=1 )
		{
			for(Property property:response.getProperties())
			{
				for(OwnerInfo owner:property.getOwners())
				{
					if( owner.getName().equalsIgnoreCase(conn.getWaterConnection().getApplicantname())
							&&
						owner.getFatherOrHusbandName().equalsIgnoreCase(conn.getWaterConnection().getGuardianname()))
						
						return property.getPropertyId();
					
				}
			}
		}
		
			
			return null;
		 
		
	}

}
