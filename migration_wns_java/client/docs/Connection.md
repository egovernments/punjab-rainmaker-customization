# Connection

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** | Unique Identifier of the connection for internal reference. |  [optional]
**tenantId** | **String** | Unique ULB identifier. |  [optional]
**propertyId** | **String** | UUID of the property. |  [optional]
**applicationNo** | **String** | Formatted application number, which will be generated using ID-Gen at the time . |  [optional]
**applicationStatus** | **String** |  |  [optional]
**status** | [**StatusEnum**](#StatusEnum) |  |  [optional]
**connectionNo** | **String** | Formatted connection number, which will be generated using ID-Gen service after aproval of connection application in case of new application. If the source of data is \&quot;DATA_ENTRY\&quot; then application status will be considered as \&quot;APROVED\&quot; application. |  [optional]
**oldConnectionNo** | **String** | Mandatory if source is \&quot;DATA_ENTRY\&quot;. |  [optional]
**documents** | [**List&lt;Document&gt;**](Document.md) | The documents attached by owner for exemption. |  [optional]
**plumberInfo** | [**List&lt;PlumberInfo&gt;**](PlumberInfo.md) | The documents attached by owner for exemption. |  [optional]
**roadType** | **String** | It is a master data, defined in MDMS. If road cutting is required to established the connection then we need to capture the details of road type. |  [optional]
**roadCuttingArea** | **Float** | Capture the road cutting area in sqft. |  [optional]
**connectionExecutionDate** | **Long** |  |  [optional]
**connectionCategory** | **String** | It is a master data, defined in MDMS | 
**connectionType** | **String** | It is a master data, defined in MDMS. | 
**additionalDetails** | **Object** | Json object to capture any extra information which is not accommodated of model |  [optional]
**auditDetails** | [**AuditDetails**](AuditDetails.md) |  |  [optional]

<a name="StatusEnum"></a>
## Enum: StatusEnum
Name | Value
---- | -----
ACTIVE | &quot;Active&quot;
INACTIVE | &quot;Inactive&quot;
