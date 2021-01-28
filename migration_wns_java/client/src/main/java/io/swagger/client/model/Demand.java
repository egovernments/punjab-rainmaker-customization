package io.swagger.client.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Object which holds the basic info about the revenue assessment for which the demand is generated like module name, consumercode, owner, etc.
 */


public class Demand   {
	
        @JsonProperty("id")
        private String id;

        @JsonProperty("tenantId")
        private String tenantId;

        @JsonProperty("consumerCode")
        private String consumerCode;

        @JsonProperty("consumerType")
        private String consumerType;

        @JsonProperty("businessService")
        private String businessService;

        @JsonProperty("payer")
        private User payer;

        @JsonProperty("taxPeriodFrom")
        private Long taxPeriodFrom;

        @JsonProperty("taxPeriodTo")
        private Long taxPeriodTo;

        @JsonProperty("demandDetails")
        //@Size(min=1)
        private List<DemandDetail> demandDetails = new ArrayList<>();

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;
        
        @JsonProperty("billExpiryTime")
        private Long billExpiryTime;

        @JsonProperty("additionalDetails")
        private Object additionalDetails;

        @JsonProperty("minimumAmountPayable")
        private BigDecimal minimumAmountPayable = BigDecimal.ZERO;
        
        private Boolean isPaymentCompleted = false;
        
        

        public String getId() {
			return id;
		}


		public String getTenantId() {
			return tenantId;
		}


		public String getConsumerCode() {
			return consumerCode;
		}


		public String getConsumerType() {
			return consumerType;
		}


		public String getBusinessService() {
			return businessService;
		}


		public User getPayer() {
			return payer;
		}


		public Long getTaxPeriodFrom() {
			return taxPeriodFrom;
		}


		public Long getTaxPeriodTo() {
			return taxPeriodTo;
		}


		public List<DemandDetail> getDemandDetails() {
			return demandDetails;
		}


		public AuditDetails getAuditDetails() {
			return auditDetails;
		}


		public Long getBillExpiryTime() {
			return billExpiryTime;
		}


		public Object getAdditionalDetails() {
			return additionalDetails;
		}


		public BigDecimal getMinimumAmountPayable() {
			return minimumAmountPayable;
		}


		public Boolean getIsPaymentCompleted() {
			return isPaymentCompleted;
		}


		public StatusEnum getStatus() {
			return status;
		}


		public void setId(String id) {
			this.id = id;
		}


		public void setTenantId(String tenantId) {
			this.tenantId = tenantId;
		}


		public void setConsumerCode(String consumerCode) {
			this.consumerCode = consumerCode;
		}


		public void setConsumerType(String consumerType) {
			this.consumerType = consumerType;
		}


		public void setBusinessService(String businessService) {
			this.businessService = businessService;
		}


		public void setPayer(User payer) {
			this.payer = payer;
		}


		public void setTaxPeriodFrom(Long taxPeriodFrom) {
			this.taxPeriodFrom = taxPeriodFrom;
		}


		public void setTaxPeriodTo(Long taxPeriodTo) {
			this.taxPeriodTo = taxPeriodTo;
		}


		public void setDemandDetails(List<DemandDetail> demandDetails) {
			this.demandDetails = demandDetails;
		}


		public void setAuditDetails(AuditDetails auditDetails) {
			this.auditDetails = auditDetails;
		}


		public void setBillExpiryTime(Long billExpiryTime) {
			this.billExpiryTime = billExpiryTime;
		}


		public void setAdditionalDetails(Object additionalDetails) {
			this.additionalDetails = additionalDetails;
		}


		public void setMinimumAmountPayable(BigDecimal minimumAmountPayable) {
			this.minimumAmountPayable = minimumAmountPayable;
		}


		public void setIsPaymentCompleted(Boolean isPaymentCompleted) {
			this.isPaymentCompleted = isPaymentCompleted;
		}


		public void setStatus(StatusEnum status) {
			this.status = status;
		}


			/**
   * Gets or Sets status
   */
  public enum StatusEnum {
	  
    ACTIVE("ACTIVE"),
    
    CANCELLED("CANCELLED"),
    
    ADJUSTED("ADJUSTED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equalsIgnoreCase(text)) {
          return b;
        }
      }
      return null;
    }
  }

        @JsonProperty("status")
        private StatusEnum status;


        public Demand addDemandDetailsItem(DemandDetail demandDetailsItem) {
        this.demandDetails.add(demandDetailsItem);
        return this;
        }

}
