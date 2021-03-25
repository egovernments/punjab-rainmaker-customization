package io.swagger.client.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * BillRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillRequestV2   {
	
  @JsonProperty("RequestInfo")
  private RequestInfo requestInfo;
  
  @JsonProperty("Bills")
  @Default
  private List<BillV2> bills = new ArrayList<>();
}

