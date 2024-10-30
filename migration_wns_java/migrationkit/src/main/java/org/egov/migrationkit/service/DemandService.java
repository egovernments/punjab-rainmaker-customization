package org.egov.migrationkit.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.migrationkit.constants.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.Demand;
import io.swagger.client.model.Demand.StatusEnum;
import io.swagger.client.model.DemandCriteria;
import io.swagger.client.model.DemandDetail;
import io.swagger.client.model.DemandRequest;
import io.swagger.client.model.DemandResponse;
import io.swagger.client.model.OwnerInfo;
import io.swagger.client.model.RequestInfo;
import io.swagger.client.model.RequestInfoWrapper;
import io.swagger.client.model.User;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class DemandService {
	
	@Autowired
	private CommonService commonService;
	
	@Value("${egov.billingservice.host}")
	private String billingServiceHost;
	
	@Value("${egov.demand.create.url}")
	private String demandCreateEndPoint;
	
	@Value("${egov.bill.expiry.days.in.milliseconds}")
	private Long billExpiryDaysInMilliseconds;
	
	
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private RecordService recordService;
 

	public List<Demand> prepareDemandRequest(Map data, String businessService, String consumerCode, String tenantId, OwnerInfo owner) {
		

		System.out.println("entered in dcb");
		Map<String, List<DemandDetail>> instaWiseDemandMap = new HashMap<>();
		List<Map> dcbDataList = (List) data.get("dcb") != null ? (List) data.get("dcb") : new ArrayList<Map>();
		System.out.println("list in dcb"+dcbDataList);
		List<Demand> demands = new LinkedList<>();
		
//		dcbDataList
		for (Map dcbData : dcbDataList) {
			
			
			String taxHeadMaster = WSConstants.WS_TAX_HEAD_MAP.get((String)dcbData.getOrDefault("demand_reason", "WS_OTHER_FEE"));
			
			DemandDetail dd = null;
			if(taxHeadMaster.matches("(.*)ADVANCE(.*)")) {
				
				//System.out.println("from date"+WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("from_date")));
				//System.out.println("to date"+WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("to_date")));
				System.out.println("TaxAmount 1"+ BigDecimal.valueOf((Integer)dcbData.get("collected_amount")).negate());
				dd = DemandDetail.builder()
					.taxAmount(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")).negate())
					.taxHeadMasterCode(taxHeadMaster)
					.collectionAmount(BigDecimal.ZERO)
					.amountPaid(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")))
					.fromDate(WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("from_date")))
					.toDate(WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("to_date")))
					.tenantId(tenantId)
					.build();
				System.out.println(dd);
				
			} else {
				System.out.println(" Else from date"+WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("from_date")));
				dd = DemandDetail.builder()
					.taxAmount(BigDecimal.valueOf((Integer)dcbData.get("amount")))
					.taxHeadMasterCode(taxHeadMaster)
					.collectionAmount(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")))
					.amountPaid(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")))
					.fromDate(WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("from_date")))
					.toDate(WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("to_date")))
					.tenantId(tenantId)
					.build();
			}
		
			
/*			log.info("from db :"+(String)dcbData.get("demand_reason") +"   @taxHeadMaster " +taxHeadMaster);
			log.info("from_date" +(String)dcbData.get("from_date") + "  and epoc" +WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("from_date")));
			log.info("to_date" +(String)dcbData.get("to_date") + "  and epoc" +WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("to_date")));
			
		*/	
			Integer installmentId = (Integer)dcbData.get("insta_id");
			if(instaWiseDemandMap.containsKey(installmentId+"ONE_TIME_FEE")  && WSConstants.ONE_TIME_TAX_HEAD_MASTERS.contains(dd.getTaxHeadMasterCode()) ) {
				
				instaWiseDemandMap.get(installmentId+"ONE_TIME_FEE").add(dd);
				
			} else if( instaWiseDemandMap.containsKey(installmentId+"WS") && !WSConstants.ONE_TIME_TAX_HEAD_MASTERS.contains(dd.getTaxHeadMasterCode())){
				
				instaWiseDemandMap.get(installmentId+"WS").add(dd);
			}else {
				
				List<DemandDetail> ddList = new ArrayList<>();
				ddList.add(dd);
				
				if(WSConstants.ONE_TIME_TAX_HEAD_MASTERS.contains(dd.getTaxHeadMasterCode()))
					instaWiseDemandMap.put(installmentId+"ONE_TIME_FEE", ddList);
				else
					instaWiseDemandMap.put(installmentId+"WS", ddList);
				
			}
				
		}
		instaWiseDemandMap.forEach((insta_id, ddList) -> {
			Boolean isPaymentCompleted = false;
		
			BigDecimal totoalTax = ddList.stream().map(DemandDetail::getTaxAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			BigDecimal totalCollection = ddList.stream().map(DemandDetail::getCollectionAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			if (totoalTax.compareTo(totalCollection) == 0)
				isPaymentCompleted = true;
			else if (totoalTax.compareTo(totalCollection) != 0)
				isPaymentCompleted= false;
			
			BigDecimal totalAmountPaid = ddList.stream().map(DemandDetail::getAmountPaid)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			if(!ddList.isEmpty() && WSConstants.ONE_TIME_TAX_HEAD_MASTERS.contains(ddList.get(0).getTaxHeadMasterCode())) {
				demands.add(Demand.builder()
						.businessService(businessService + WSConstants.ONE_TIME_FEE_CONST)
						.consumerCode(consumerCode)
						.demandDetails(ddList)
						.payer(User.builder().uuid(owner.getUuid()).name(owner.getName()).build())
						.tenantId(tenantId)
						.billExpiryTime(billExpiryDaysInMilliseconds)
//						.taxPeriodFrom(ddList.get(0).getFromDate())
//						.taxPeriodTo(ddList.get(0).getToDate())
						.taxPeriodFrom(1554057000000l)
						.taxPeriodTo(1869676199000l)
						.minimumAmountPayable(BigDecimal.ZERO)
						.consumerType("waterConnection")
						.status(StatusEnum.valueOf("ACTIVE"))
						.totalAmountPaid(totalAmountPaid)
						.isPaymentCompleted(isPaymentCompleted)
						.build());	
				System.out.println("abcd"+ WSConstants.ONE_TIME_TAX_HEAD_MASTERS);
			}else {
				demands.add(Demand.builder()
						.businessService(businessService)
						.consumerCode(consumerCode)
						.demandDetails(ddList)
						.payer(User.builder().uuid(owner.getUuid()).name(owner.getName()).build())
						.tenantId(tenantId)
//					 
						.taxPeriodFrom(ddList.get(0).getFromDate())
						.taxPeriodTo(ddList.get(0).getToDate())
					//	.taxPeriodFrom(1554076800000l)
					//	.taxPeriodTo(1617175799000l)
						.minimumAmountPayable(BigDecimal.ZERO)
						.consumerType("waterConnection")
						.status(StatusEnum.valueOf("ACTIVE"))
						.totalAmountPaid(totalAmountPaid)
						.isPaymentCompleted(isPaymentCompleted)
						.billExpiryTime(billExpiryDaysInMilliseconds)
						.build());	
						//System.out.println("dd list from dATE"+ ddList.get(0).getFromDate());
			}
				
			});
System.out.println("123456"+demands);
		return demands;
		
	}
	
    /**
     * Creates demand
     * @param requestInfo The RequestInfo of the calculation Request
     * @param demands The demands to be created
     * @return The list of demand created
     */
	public Boolean saveDemand(RequestInfo requestInfo, List<Demand> demands,String erpId,String tenantId,String service){
		boolean isDemandCreated = Boolean.TRUE;
		
		

		try{

			String url = billingServiceHost + demandCreateEndPoint+requestInfo.getUserInfo().getTenantId();
			System.out.println("ERP1:-" + url);
		
			DemandRequest request = new DemandRequest(requestInfo,demands);
			System.out.println("requedt info " + requestInfo);
			
			
			try {
			    // Convert Demand to JSON
				ObjectMapper objectMapper = new ObjectMapper();
			    String json = objectMapper.writeValueAsString(demands);

			    // Print the JSON
			    
			} catch (Exception e) {
			    e.printStackTrace();
			}
			System.out.println("class- " + DemandResponse.class);
			DemandResponse response = restTemplate.postForObject(url, request, DemandResponse.class);
			

			log.info("Demand Create Respone: " + response);
		}
		catch(Exception e){
			if(e.toString().equalsIgnoreCase("Demand already exists in the same period")) {
				isDemandCreated=Boolean.TRUE;
			}else {
				isDemandCreated=Boolean.FALSE;

				recordService.recordError(service,tenantId, e.toString(), erpId);
				
				log.error("Error while Saving demands" + e.toString());
			}
		}
		return isDemandCreated;  
	}
    
    public List<Demand> prepareSwDemandRequest(Map data, String businessService, String consumerCode, String tenantId, OwnerInfo owner) {
		
	Map<String, List<DemandDetail>> instaWiseDemandMap = new HashMap<>();
	List<Map> dcbDataList = (List) data.get("dcb") != null ? (List) data.get("dcb") : new ArrayList<Map>();
	List<Demand> demands = new LinkedList<>();

	//	dcbDataList
	for (Map dcbData : dcbDataList) {
		String taxhead = (String)dcbData.get("demand_reason");
		String taxHeadMaster = WSConstants.SW_TAX_HEAD_MAP.getOrDefault(taxhead,"SW_OTHER_FEE");
		DemandDetail dd = null;
		if(taxHeadMaster.matches("(.*)ADVANCE(.*)")) {
			dd = DemandDetail.builder()
					.taxAmount(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")).negate())
					.taxHeadMasterCode(taxHeadMaster)
					.collectionAmount(BigDecimal.ZERO)
					.amountPaid(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")))
					.fromDate(WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("from_date")))
					.toDate(WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("to_date")))
					//					.fromDate(1554076800000l)
					//					.toDate(1617175799000l)
					.tenantId(tenantId)  
					.build();
		}else {
			dd = DemandDetail.builder()
					.taxAmount(BigDecimal.valueOf((Integer)dcbData.get("amount")))
					.taxHeadMasterCode(taxHeadMaster)
					.collectionAmount(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")))
					.amountPaid(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")))
					.fromDate(WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("from_date")))
					.toDate(WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("to_date")))
					//					.fromDate(1554076800000l)
					//					.toDate(1617175799000l)
					.tenantId(tenantId)  
					.build();
		}

		//	log.info("from db :"+(String)dcbData.get("demand_reason") +"  @taxHeadMaster " +taxHeadMaster );
		//	log.info("from_date" +(String)dcbData.get("from_date") + "  and epoc" +WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("from_date")));
		//	log.info("to_date" +(String)dcbData.get("to_date") + "  and epoc" +WSConstants.TIME_PERIOD_MAP.get((String)dcbData.get("to_date")));


		Integer installmentId = (Integer)dcbData.get("insta_id");
		if(instaWiseDemandMap.containsKey(installmentId+"ONE_TIME_FEE")  && WSConstants.ONE_TIME_TAX_HEAD_MASTERS.contains(dd.getTaxHeadMasterCode()) ) {

			instaWiseDemandMap.get(installmentId+"ONE_TIME_FEE").add(dd);
			
		} else if( instaWiseDemandMap.containsKey(installmentId+"SW") && !WSConstants.ONE_TIME_TAX_HEAD_MASTERS.contains(dd.getTaxHeadMasterCode())){
			
			instaWiseDemandMap.get(installmentId+"SW").add(dd);
		}else {
			
			List<DemandDetail> ddList = new ArrayList<>();
			ddList.add(dd);
			
			if(WSConstants.ONE_TIME_TAX_HEAD_MASTERS.contains(dd.getTaxHeadMasterCode()))
				instaWiseDemandMap.put(installmentId+"ONE_TIME_FEE", ddList);
			else
				instaWiseDemandMap.put(installmentId+"SW", ddList);
			
		}

	}
	instaWiseDemandMap.forEach((insta_id, ddList) -> {
		Boolean isPaymentCompleted = false;
		BigDecimal totoalTax = ddList.stream().map(DemandDetail::getTaxAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		BigDecimal totalCollection = ddList.stream().map(DemandDetail::getCollectionAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (totoalTax.compareTo(totalCollection) == 0)
			isPaymentCompleted = true;
		else if (totoalTax.compareTo(totalCollection) != 0)
			isPaymentCompleted= false;
		
		BigDecimal totalAmountPaid = ddList.stream().map(DemandDetail::getAmountPaid)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		if(!ddList.isEmpty() && WSConstants.ONE_TIME_TAX_HEAD_MASTERS.contains(ddList.get(0).getTaxHeadMasterCode())) {
			demands.add(Demand.builder()
					.businessService(businessService + WSConstants.ONE_TIME_FEE_CONST)
					.consumerCode(consumerCode)
					.demandDetails(ddList)
					.payer(User.builder().uuid(owner.getUuid()).name(owner.getName()).build())
					.tenantId(tenantId)
//					There is no tax periods configured for all the previous year in PB QA environments as of now giving dummy configured tax period. 
			.taxPeriodFrom(ddList.get(0).getFromDate())
			.taxPeriodTo(ddList.get(0).getToDate())
		//.taxPeriodFrom(1554057000000l)
			//		.taxPeriodTo(1869676199000l)
					.minimumAmountPayable(BigDecimal.ZERO)
					.consumerType("sewerageConnection")
					.status(StatusEnum.valueOf("ACTIVE"))
					.totalAmountPaid(totalAmountPaid)
					.isPaymentCompleted(isPaymentCompleted)
					.billExpiryTime(billExpiryDaysInMilliseconds)
					.build());	
		}else {
			demands.add(Demand.builder()
					.businessService(businessService)
					.consumerCode(consumerCode)
					.demandDetails(ddList)
					.payer(User.builder().uuid(owner.getUuid()).name(owner.getName()).build())
					.tenantId(tenantId)
//					There is no tax periods configured for all the previous year in PB QA environments as of now giving dummy configured tax period. 
 					.taxPeriodFrom(ddList.get(0).getFromDate())
  				    .taxPeriodTo(ddList.get(0).getToDate())
//					.taxPeriodFrom(1554076800000l)
//					.taxPeriodTo(1617175799000l)
					.minimumAmountPayable(BigDecimal.ZERO)
					.consumerType("sewerageConnection")
					.status(StatusEnum.valueOf("ACTIVE"))
					.totalAmountPaid(totalAmountPaid)
					.isPaymentCompleted(isPaymentCompleted)
					.billExpiryTime(billExpiryDaysInMilliseconds)
					.build());	
		}
			
		});

	return demands;
		
	}

	public List<Demand> getDemands(DemandCriteria demandCriteria, RequestInfo requestInfo) {
		DemandResponse result = new DemandResponse();
		try {

			String url = commonService.getDemandSearchURL(demandCriteria).toString();
			RequestInfoWrapper request = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
			
			result = restTemplate.postForObject(url , request, DemandResponse.class);
			log.info("Demand Request URL: " + url + "Demand RequestInfo: " + request + "Bill Response: " + result);
			
		} catch (Exception ex) {			
			log.error("Search Demand failure for consumercode: {} ", ex);
		}
		return result.getDemands();
		
	}
    
}

