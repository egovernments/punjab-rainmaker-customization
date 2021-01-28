# ResponseInfo

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**apiId** | **String** | unique API ID | 
**ver** | **String** | API version | 
**ts** | **Long** | response time in epoch | 
**resMsgId** | **String** | unique response message id (UUID) - will usually be the correlation id from the server |  [optional]
**msgId** | **String** | message id of the request |  [optional]
**status** | [**StatusEnum**](#StatusEnum) | status of request processing - to be enhanced in futuer to include INPROGRESS | 

<a name="StatusEnum"></a>
## Enum: StatusEnum
Name | Value
---- | -----
SUCCESSFUL | &quot;SUCCESSFUL&quot;
FAILED | &quot;FAILED&quot;
