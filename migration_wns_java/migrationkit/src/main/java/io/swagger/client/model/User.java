package io.swagger.client.model;

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
public class User {


	private String uuid;
	private Long id;
	private String userName;
	private String type;
	private String salutation;
	private String name;
	private String gender;
	private String mobileNumber;
	private String emailId;
	private String altContactNumber;
	private String pan;
	private String aadhaarNumber;
	private String permanentAddress;
	private String permanentCity;
	private String permanentPinCode;
	private String correspondenceAddress;
	private String correspondenceCity;
	private String correspondencePinCode;
	private Boolean active;
	private String tenantId;
	public String getUuid() {
		return uuid;
	}
	public Long getId() {
		return id;
	}
	public String getUserName() {
		return userName;
	}
	public String getType() {
		return type;
	}
	public String getSalutation() {
		return salutation;
	}
	public String getName() {
		return name;
	}
	public String getGender() {
		return gender;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public String getEmailId() {
		return emailId;
	}
	public String getAltContactNumber() {
		return altContactNumber;
	}
	public String getPan() {
		return pan;
	}
	public String getAadhaarNumber() {
		return aadhaarNumber;
	}
	public String getPermanentAddress() {
		return permanentAddress;
	}
	public String getPermanentCity() {
		return permanentCity;
	}
	public String getPermanentPinCode() {
		return permanentPinCode;
	}
	public String getCorrespondenceAddress() {
		return correspondenceAddress;
	}
	public String getCorrespondenceCity() {
		return correspondenceCity;
	}
	public String getCorrespondencePinCode() {
		return correspondencePinCode;
	}
	public Boolean getActive() {
		return active;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public void setAltContactNumber(String altContactNumber) {
		this.altContactNumber = altContactNumber;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	public void setAadhaarNumber(String aadhaarNumber) {
		this.aadhaarNumber = aadhaarNumber;
	}
	public void setPermanentAddress(String permanentAddress) {
		this.permanentAddress = permanentAddress;
	}
	public void setPermanentCity(String permanentCity) {
		this.permanentCity = permanentCity;
	}
	public void setPermanentPinCode(String permanentPinCode) {
		this.permanentPinCode = permanentPinCode;
	}
	public void setCorrespondenceAddress(String correspondenceAddress) {
		this.correspondenceAddress = correspondenceAddress;
	}
	public void setCorrespondenceCity(String correspondenceCity) {
		this.correspondenceCity = correspondenceCity;
	}
	public void setCorrespondencePinCode(String correspondencePinCode) {
		this.correspondencePinCode = correspondencePinCode;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

		
}