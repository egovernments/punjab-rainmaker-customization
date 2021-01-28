# SewerageApi

All URIs are relative to *https://virtserver.swaggerhub.com/egov-foundation/Water-Sewerage-1.0/1.0.0*

Method | HTTP request | Description
------------- | ------------- | -------------
[**swcCancelPost**](SewerageApi.md#swcCancelPost) | **POST** /swc/_cancel | Deactivate existing water connection.
[**swcCreatePost**](SewerageApi.md#swcCreatePost) | **POST** /swc/_create | Apply for new Sewerage connection.
[**swcDeletePost**](SewerageApi.md#swcDeletePost) | **POST** /swc/_delete | Delete existing Sewerage connection.
[**swcSearchPost**](SewerageApi.md#swcSearchPost) | **POST** /swc/_search | Get the list of exsting Sewerage connections.
[**swcUpdatePost**](SewerageApi.md#swcUpdatePost) | **POST** /swc/_update | Update existing Sewerage connection details.

<a name="swcCancelPost"></a>
# **swcCancelPost**
> ResponseInfo swcCancelPost(body, tenantId, connectionNo)

Deactivate existing water connection.

Deactivate existing water connection.

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.SewerageApi;


SewerageApi apiInstance = new SewerageApi();
RequestInfo body = new RequestInfo(); // RequestInfo | Request header for the property delete Request.
String tenantId = "tenantId_example"; // String | Unique id for a tenant.
String connectionNo = "connectionNo_example"; // String | The properrtyId to be deactivated
try {
    ResponseInfo result = apiInstance.swcCancelPost(body, tenantId, connectionNo);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SewerageApi#swcCancelPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**RequestInfo**](RequestInfo.md)| Request header for the property delete Request. |
 **tenantId** | **String**| Unique id for a tenant. |
 **connectionNo** | **String**| The properrtyId to be deactivated |

### Return type

[**ResponseInfo**](ResponseInfo.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="swcCreatePost"></a>
# **swcCreatePost**
> SewerageConnectionResponse swcCreatePost(body)

Apply for new Sewerage connection.

Citizen or employee can apply for new Sewerage connection. 

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.SewerageApi;


SewerageApi apiInstance = new SewerageApi();
SewerageConnectionRequest body = new SewerageConnectionRequest(); // SewerageConnectionRequest | Details for the new Sewerage Connection + RequestHeader meta data.
try {
    SewerageConnectionResponse result = apiInstance.swcCreatePost(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SewerageApi#swcCreatePost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**SewerageConnectionRequest**](SewerageConnectionRequest.md)| Details for the new Sewerage Connection + RequestHeader meta data. |

### Return type

[**SewerageConnectionResponse**](SewerageConnectionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="swcDeletePost"></a>
# **swcDeletePost**
> ResponseInfo swcDeletePost(body, tenantId, connectionNo)

Delete existing Sewerage connection.

Delete existing Sewerage connection.

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.SewerageApi;


SewerageApi apiInstance = new SewerageApi();
RequestInfo body = new RequestInfo(); // RequestInfo | Request header for the connection delete Request.
String tenantId = "tenantId_example"; // String | Unique id for a tenant.
String connectionNo = "connectionNo_example"; // String | The connection no to be deactivated
try {
    ResponseInfo result = apiInstance.swcDeletePost(body, tenantId, connectionNo);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SewerageApi#swcDeletePost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**RequestInfo**](RequestInfo.md)| Request header for the connection delete Request. |
 **tenantId** | **String**| Unique id for a tenant. |
 **connectionNo** | **String**| The connection no to be deactivated |

### Return type

[**ResponseInfo**](ResponseInfo.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="swcSearchPost"></a>
# **swcSearchPost**
> SewerageConnectionResponse swcSearchPost(body, tenantId, ids, connectionNo, oldConnectionNo, mobileNumber, fromDate, toDate)

Get the list of exsting Sewerage connections.

Get the water connections list based on the input parameters. 

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.SewerageApi;


SewerageApi apiInstance = new SewerageApi();
RequestInfo body = new RequestInfo(); // RequestInfo | RequestHeader meta data.
String tenantId = "tenantId_example"; // String | Unique id for a tenant.
List<String> ids = Arrays.asList("ids_example"); // List<String> | List of system generated ids of Sewerage connection.
List<String> connectionNo = Arrays.asList("connectionNo_example"); // List<String> | List of Sewerage connection numbers to search..
List<String> oldConnectionNo = Arrays.asList("oldConnectionNo_example"); // List<String> | List of old Sewerage connection numbers to search..
Long mobileNumber = 789L; // Long | MobileNumber of owner whose Sewerage connection is to be searched.
BigDecimal fromDate = new BigDecimal(); // BigDecimal | Fetches Sewerage Connection with created time after fromDate.
BigDecimal toDate = new BigDecimal(); // BigDecimal | Fetches Sewerage Connection with created time till toDate.
try {
    SewerageConnectionResponse result = apiInstance.swcSearchPost(body, tenantId, ids, connectionNo, oldConnectionNo, mobileNumber, fromDate, toDate);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SewerageApi#swcSearchPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**RequestInfo**](RequestInfo.md)| RequestHeader meta data. |
 **tenantId** | **String**| Unique id for a tenant. |
 **ids** | [**List&lt;String&gt;**](String.md)| List of system generated ids of Sewerage connection. | [optional]
 **connectionNo** | [**List&lt;String&gt;**](String.md)| List of Sewerage connection numbers to search.. | [optional]
 **oldConnectionNo** | [**List&lt;String&gt;**](String.md)| List of old Sewerage connection numbers to search.. | [optional]
 **mobileNumber** | **Long**| MobileNumber of owner whose Sewerage connection is to be searched. | [optional]
 **fromDate** | **BigDecimal**| Fetches Sewerage Connection with created time after fromDate. | [optional]
 **toDate** | **BigDecimal**| Fetches Sewerage Connection with created time till toDate. | [optional]

### Return type

[**SewerageConnectionResponse**](SewerageConnectionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="swcUpdatePost"></a>
# **swcUpdatePost**
> SewerageConnectionResponse swcUpdatePost(body)

Update existing Sewerage connection details.

Updates a given &#x60;Sewerage connection&#x60; with newer details.

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.SewerageApi;


SewerageApi apiInstance = new SewerageApi();
SewerageConnectionRequest body = new SewerageConnectionRequest(); // SewerageConnectionRequest | Request of Sewerage connection details.
try {
    SewerageConnectionResponse result = apiInstance.swcUpdatePost(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SewerageApi#swcUpdatePost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**SewerageConnectionRequest**](SewerageConnectionRequest.md)| Request of Sewerage connection details. |

### Return type

[**SewerageConnectionResponse**](SewerageConnectionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

