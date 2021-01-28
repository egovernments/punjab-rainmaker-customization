# PlumberInfo

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**tenantId** | **String** | Unique ULB identifier. |  [optional]
**name** | **String** | The name of the user. |  [optional]
**licenseNo** | **String** | Plumber unique license number. |  [optional]
**mobileNumber** | **String** | MobileNumber of the user. |  [optional]
**gender** | **String** | Gender of the user. |  [optional]
**fatherOrHusbandName** | **String** | Father or Husband name of the user. |  [optional]
**correspondenceAddress** | **String** | The current address of the owner for correspondence. |  [optional]
**relationship** | [**RelationshipEnum**](#RelationshipEnum) | The relationship of gaurdian. |  [optional]
**additionalDetails** | **Object** | Json object to capture any extra information which is not accommodated of model |  [optional]
**auditDetails** | [**AuditDetails**](AuditDetails.md) |  |  [optional]

<a name="RelationshipEnum"></a>
## Enum: RelationshipEnum
Name | Value
---- | -----
FATHER | &quot;FATHER&quot;
HUSBAND | &quot;HUSBAND&quot;
