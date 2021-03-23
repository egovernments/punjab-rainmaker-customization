package io.swagger.client.model;


import java.util.List;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class StorageResponse {
	
	@JsonProperty("files")
	private List<EgFile> files;

	public List<EgFile> getFiles() {
		return files;
	}

	public void setFiles(List<EgFile> files) {
		this.files = files;
	}

	 
}