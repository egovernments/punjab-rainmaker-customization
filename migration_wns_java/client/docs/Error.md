# Error

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**code** | **String** | Error Code will be module specific error label/code to identiffy the error. All modules should also publish the Error codes with their specific localized values in localization service to ensure clients can print locale specific error messages. Example for error code would be User.NotFound to indicate User Not Found by User/Authentication service. All services must declare their possible Error Codes with brief description in the error response section of their API path. | 
**message** | **String** | English locale message of the error code. Clients should make a separate call to get the other locale description if configured with the service. Clients may choose to cache these locale specific messages to enhance performance with a reasonable TTL (May be defined by the localization service based on tenant + module combination). | 
**description** | **String** | Optional long description of the error to help clients take remedial action. This will not be available as part of localization service. |  [optional]
**params** | **List&lt;String&gt;** | Some error messages may carry replaceable fields (say $1, $2) to provide more context to the message. E.g. Format related errors may want to indicate the actual field for which the format is invalid. Client&#x27;s should use the values in the param array to replace those fields. |  [optional]
