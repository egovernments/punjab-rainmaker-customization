package io.swagger.client.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OwnerInfo extends User {


	@JsonProperty("ownerInfoUuid")
	private String ownerInfoUuid;

	@JsonProperty("gender")
	private String gender;

	@JsonProperty("fatherOrHusbandName")
	private String fatherOrHusbandName;

	@JsonProperty("correspondenceAddress")
	private String correspondenceAddress;

	@JsonProperty("isPrimaryOwner")
	private Boolean isPrimaryOwner;

	@JsonProperty("ownerShipPercentage")
	private Double ownerShipPercentage;

	@JsonProperty("ownerType")
	private String ownerType;

	@JsonProperty("institutionId")
	private String institutionId;

	@JsonProperty("status")
	private Status status;

	@JsonProperty("documents")
	private List<Document> documents;

	@JsonProperty("relationship")
	private Relationship relationship;

	public OwnerInfo addDocumentsItem(Document documentsItem) {
		if (this.documents == null) {
			this.documents = new ArrayList<>();
		}
		this.documents.add(documentsItem);
		return this;
	}

	
}
