# UserInfo

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**tenantId** | **String** | Unique Identifier of the tenant to which user primarily belongs | 
**uuid** | **String** | System Generated User id of the authenticated user. |  [optional]
**userName** | **String** | Unique user name of the authenticated user | 
**password** | **String** | password of the user. |  [optional]
**idToken** | **String** | This will be the OTP. |  [optional]
**mobile** | **String** | mobile number of the autheticated user |  [optional]
**email** | **String** | email address of the authenticated user |  [optional]
**primaryrole** | [**List&lt;Role&gt;**](Role.md) | List of all the roles for the primary tenant | 
**additionalroles** | [**List&lt;TenantRole&gt;**](TenantRole.md) | array of additional tenantids authorized for the authenticated user |  [optional]
