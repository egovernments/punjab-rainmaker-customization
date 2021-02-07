package io.swagger.client.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Object which holds the basic info about the revenue assessment for which the demand is generated like module name, consumercode, owner, etc.
 */


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
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
        
        /**
         * This is used to migrate the collection
         */
        private BigDecimal totalAmountPaid = BigDecimal.ZERO;;
        
        
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
