package io.swagger.client.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class ProcessInstance {


	 
	@JsonProperty("id")
	private String id;

 
	@JsonProperty("tenantId")
	private String tenantId;

	 
	@JsonProperty("businessService")
	private String businessService;

	 
	@JsonProperty("businessId")
	private String businessId;

	 
	@JsonProperty("action")
	private String action;

	 
	@JsonProperty("moduleName")
	private String moduleName;

	 
	
	/* for use of notification service in property*/
	private String notificationAction;

	@JsonProperty("comment")
	private String comment;

	@JsonProperty("documents")
	private List<Document> documents;

	@JsonProperty("assignes")
	private List<User> assignes;

	public ProcessInstance addDocumentsItem(Document documentsItem) {
		if (this.documents == null) {
			this.documents = new ArrayList<>();
		}
		if (!this.documents.contains(documentsItem))
			this.documents.add(documentsItem);

		return this;
	}


}
