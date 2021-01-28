# ResponseHeader

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**ts** | **Long** | response time in epoch |  [optional]
**resMsgId** | **String** | unique response message id (UUID) - will usually be the correlation id from the server | 
**msgId** | **String** | message id of the request | 
**status** | [**StatusEnum**](#StatusEnum) | status of request processing | 
**signature** | **String** | Hash describing the current ResponseHeader |  [optional]
**error** | [**Error**](Error.md) |  |  [optional]
**information** | **Object** | Additional information from API |  [optional]
**debug** | **Object** | Debug information when requested |  [optional]
**additionalInfo** | **Object** | Any additional information if required e.g. status url (to find out the current status of an asynchronous processing response), additional links to perform special functions like file uploads etc. |  [optional]

<a name="StatusEnum"></a>
## Enum: StatusEnum
Name | Value
---- | -----
COMPLETED | &quot;COMPLETED&quot;
ACCEPTED | &quot;ACCEPTED&quot;
FAILED | &quot;FAILED&quot;
