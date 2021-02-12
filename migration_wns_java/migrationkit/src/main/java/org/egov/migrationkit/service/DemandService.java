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

import io.swagger.client.model.Demand;
import io.swagger.client.model.Demand.StatusEnum;
import io.swagger.client.model.DemandDetail;
import io.swagger.client.model.DemandRequest;
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
	
	@Autowired
	private RestTemplate restTemplate;

	public List<Demand> prepareDemandRequest(Map data, String businessService, String consumerCode, String tenantId, OwnerInfo owner) {
		
		
		Map<Integer, List<DemandDetail>> instaWiseDemandMap = new HashMap<>();
		List<Map> dcbDataList = (List) data.get("dcb") != null ? (List) data.get("dcb") : new ArrayList<Map>();
		List<Demand> demands = new LinkedList<>();
		
//		dcbDataList
		for (Map dcbData : dcbDataList) {
			
			String taxHeadMaster = WSConstants.TAX_HEAD_MAP.get((String)dcbData.get("demand_reason"));
			DemandDetail dd = DemandDetail.builder()
					.taxAmount(BigDecimal.valueOf((Integer)dcbData.get("amount")))
					.taxHeadMasterCode(taxHeadMaster)
					.collectionAmount(BigDecimal.ZERO)
					.amountPaid(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")))
//					.fromDate((Long)dcbData.get("from_date"))
//					.toDate((Long)dcbData.get("to_date"))
					.tenantId(tenantId)
					.build();
			
			Integer installmentId = (Integer)dcbData.get("insta_id");
			if(instaWiseDemandMap.containsKey(installmentId)) {
				
					instaWiseDemandMap.get(installmentId).add(dd);

			} else {
				List<DemandDetail> ddList = new ArrayList<>();
				
				ddList.add(dd);
				instaWiseDemandMap.put(installmentId, ddList);
			}
				
		}
		instaWiseDemandMap.forEach((insta_id, ddList) -> {
			BigDecimal totalAmountPaid = BigDecimal.ZERO;
			for (DemandDetail demandDetail : ddList) {
				totalAmountPaid = totalAmountPaid.add(demandDetail.getAmountPaid());	
			}

			if(!ddList.isEmpty() && WSConstants.ONE_TIME_TAX_HEAD_MASTERS.contains(ddList.get(0).getTaxHeadMasterCode())) {
				demands.add(Demand.builder()
						.businessService(businessService + WSConstants.ONE_TIME_FEE_CONST)
						.consumerCode(consumerCode)
						.demandDetails(ddList)
						.payer(User.builder().uuid(owner.getUuid()).name(owner.getName()).build())
						.tenantId(tenantId)
//						There is no tax periods configured for all the previous year in PB QA environments as of now giving dummy configured tax period. 
//						.taxPeriodFrom(ddList.get(0).getFromDate())
//						.taxPeriodTo(ddList.get(0).getToDate())
						.taxPeriodFrom(1554076800000l)
						.taxPeriodTo(1617175799000l)
						.minimumAmountPayable(BigDecimal.ZERO)
						.consumerType("waterConnection")
						.status(StatusEnum.valueOf("ACTIVE"))
						.totalAmountPaid(totalAmountPaid)
						.build());	
			}else {
				demands.add(Demand.builder()
						.businessService(businessService)
						.consumerCode(consumerCode)
						.demandDetails(ddList)
						.payer(User.builder().uuid(owner.getUuid()).name(owner.getName()).build())
						.tenantId(tenantId)
//						There is no tax periods configured for all the previous year in PB QA environments as of now giving dummy configured tax period. 
//						.taxPeriodFrom(ddList.get(0).getFromDate())
//						.taxPeriodTo(ddList.get(0).getToDate())
						.taxPeriodFrom(1554076800000l)
						.taxPeriodTo(1617175799000l)
						.minimumAmountPayable(BigDecimal.ZERO)
						.consumerType("waterConnection")
						.status(StatusEnum.valueOf("ACTIVE"))
						.totalAmountPaid(totalAmountPaid)
						.build());	
			}
				
			});

		return demands;
		
	}
	
    /**
     * Creates demand
     * @param requestInfo The RequestInfo of the calculation Request
     * @param demands The demands to be created
     * @return The list of demand created
     */
    public Boolean saveDemand(RequestInfo requestInfo, List<Demand> demands){
    	try{
    		
    	String url = billingServiceHost + demandCreateEndPoint;
        DemandRequest request = new DemandRequest(requestInfo,demands);
        Object result = restTemplate.postForObject(url , request, String.class);
        
        	log.info("Demand Create Request: " + request + "Demand Create Respone: " + result);
        }
        catch(Exception e){
            log.error("Demand PARSING_ERROR: "+demands,"Failed to parse response of create demand " + e);
            return Boolean.FALSE;
        }
		return Boolean.TRUE;
    }
    
    public Boolean fetchBill(List<Demand> demands, RequestInfo requestInfo) {
		for (Demand demand : demands) {
			try {

				String url = commonService.getFetchBillURL(demand.getTenantId(), demand.getConsumerCode()
						, demand.getBusinessService()).toString();
				RequestInfoWrapper request = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
				
				Object result = restTemplate.postForObject(url , request, String.class);
				log.info("Bill Request URL: " + url + "Bill RequestInfo: " + request + "Bill Response: " + result);
				
			} catch (Exception ex) {
				log.error("Fetch Bill Error", ex);
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}
    
public List<Demand> prepareSwDemandRequest(Map data, String businessService, String consumerCode, String tenantId, OwnerInfo owner) {
		
		
		Map<Integer, List<DemandDetail>> instaWiseDemandMap = new HashMap<>();
		List<Map> dcbDataList = (List) data.get("dcb") != null ? (List) data.get("dcb") : new ArrayList<Map>();
		List<Demand> demands = new LinkedList<>();
		
//		dcbDataList
		for (Map dcbData : dcbDataList) {
			
			String taxHeadMaster = WSConstants.TAX_HEAD_MAP.get((String)dcbData.get("demand_reason"));
			DemandDetail dd = DemandDetail.builder()
					.taxAmount(BigDecimal.valueOf((Integer)dcbData.get("amount")))
					.taxHeadMasterCode(taxHeadMaster)
					.collectionAmount(BigDecimal.ZERO)
					.amountPaid(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")))
//					.fromDate((Long)dcbData.get("from_date"))
//					.toDate((Long)dcbData.get("to_date"))
					.tenantId(tenantId)
					.build();
			
			Integer installmentId = (Integer)dcbData.get("insta_id");
			if(instaWiseDemandMap.containsKey(installmentId)) {
				
					instaWiseDemandMap.get(installmentId).add(dd);

			} else {
				List<DemandDetail> ddList = new ArrayList<>();
				
				ddList.add(dd);
				instaWiseDemandMap.put(installmentId, ddList);
			}
				
		}
		instaWiseDemandMap.forEach((insta_id, ddList) -> {
			BigDecimal totalAmountPaid = BigDecimal.ZERO;
			for (DemandDetail demandDetail : ddList) {
				totalAmountPaid = totalAmountPaid.add(demandDetail.getAmountPaid());	
			}

			if(!ddList.isEmpty() && WSConstants.ONE_TIME_TAX_HEAD_MASTERS.contains(ddList.get(0).getTaxHeadMasterCode())) {
				demands.add(Demand.builder()
						.businessService(businessService + WSConstants.ONE_TIME_FEE_CONST)
						.consumerCode(consumerCode)
						.demandDetails(ddList)
						.payer(User.builder().uuid(owner.getUuid()).name(owner.getName()).build())
						.tenantId(tenantId)
//						There is no tax periods configured for all the previous year in PB QA environments as of now giving dummy configured tax period. 
//						.taxPeriodFrom(ddList.get(0).getFromDate())
//						.taxPeriodTo(ddList.get(0).getToDate())
						.taxPeriodFrom(1554076800000l)
						.taxPeriodTo(1617175799000l)
						.minimumAmountPayable(BigDecimal.ZERO)
						.consumerType("sewerageConnection")
						.status(StatusEnum.valueOf("ACTIVE"))
						.totalAmountPaid(totalAmountPaid)
						.build());	
			}else {
				demands.add(Demand.builder()
						.businessService(businessService)
						.consumerCode(consumerCode)
						.demandDetails(ddList)
						.payer(User.builder().uuid(owner.getUuid()).name(owner.getName()).build())
						.tenantId(tenantId)
//						There is no tax periods configured for all the previous year in PB QA environments as of now giving dummy configured tax period. 
//						.taxPeriodFrom(ddList.get(0).getFromDate())
//						.taxPeriodTo(ddList.get(0).getToDate())
						.taxPeriodFrom(1554076800000l)
						.taxPeriodTo(1617175799000l)
						.minimumAmountPayable(BigDecimal.ZERO)
						.consumerType("sewerageConnection")
						.status(StatusEnum.valueOf("ACTIVE"))
						.totalAmountPaid(totalAmountPaid)
						.build());	
			}
				
			});

		return demands;
		
	}
    
}
