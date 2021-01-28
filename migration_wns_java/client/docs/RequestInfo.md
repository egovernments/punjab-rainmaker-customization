# RequestInfo

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**apiId** | **String** | unique API ID | 
**ver** | **String** | API version - for HTTP based request this will be same as used in path | 
**ts** | **Long** | time in epoch | 
**action** | **String** | API action to be performed like _create, _update, _search (denoting POST, PUT, GET) or _oauth etc | 
**did** | **String** | Device ID from which the API is called |  [optional]
**key** | **String** | API key (API key provided to the caller in case of server to server communication) |  [optional]
**msgId** | **String** | Unique request message id from the caller | 
**requesterId** | **String** | UserId of the user calling |  [optional]
**authToken** | **String** | //session/jwt/saml token/oauth token - the usual value that would go into HTTP bearer token |  [optional]
**userInfo** | [**UserInfo**](UserInfo.md) |  |  [optional]
**correlationId** | **String** |  |  [optional]
