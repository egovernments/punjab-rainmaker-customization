package io.swagger.client.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionPaymentResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfoV2 responseInfo;

    @JsonProperty("Payments")
    private List<CollectionPayment> payments;

}
