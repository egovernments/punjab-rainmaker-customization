# WaterApi

All URIs are relative to *https://virtserver.swaggerhub.com/egov-foundation/Water-Sewerage-1.0/1.0.0*

Method | HTTP request | Description
------------- | ------------- | -------------
[**wcCancelPost**](WaterApi.md#wcCancelPost) | **POST** /wc/_cancel | Deactivate existing water connection.
[**wcCreatePost**](WaterApi.md#wcCreatePost) | **POST** /wc/_create | Apply for new water connection.
[**wcDeletePost**](WaterApi.md#wcDeletePost) | **POST** /wc/_delete | Delete existing water connection.
[**wcSearchPost**](WaterApi.md#wcSearchPost) | **POST** /wc/_search | Get the list of exsting water connections.
[**wcUpdatePost**](WaterApi.md#wcUpdatePost) | **POST** /wc/_update | Update existing water connection details.

<a name="wcCancelPost"></a>
# **wcCancelPost**
> ResponseInfo wcCancelPost(body, tenantId, propertyId)

Deactivate existing water connection.

Deactivate existing water connection.

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.WaterApi;


WaterApi apiInstance = new WaterApi();
RequestInfo body = new RequestInfo(); // RequestInfo | Request header for the property delete Request.
String tenantId = "tenantId_example"; // String | Unique id for a tenant.
String propertyId = "propertyId_example"; // String | The properrtyId to be deactivated
try {
    ResponseInfo result = apiInstance.wcCancelPost(body, tenantId, propertyId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling WaterApi#wcCancelPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**RequestInfo**](RequestInfo.md)| Request header for the property delete Request. |
 **tenantId** | **String**| Unique id for a tenant. |
 **propertyId** | **String**| The properrtyId to be deactivated |

### Return type

[**ResponseInfo**](ResponseInfo.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="wcCreatePost"></a>
# **wcCreatePost**
> WaterConnectionResponse wcCreatePost(body)

Apply for new water connection.

Citizen or employee can apply for new water connection. 

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.WaterApi;


WaterApi apiInstance = new WaterApi();
WaterConnectionRequest body = new WaterConnectionRequest(); // WaterConnectionRequest | Details for the new property + RequestHeader meta data.
try {
    WaterConnectionResponse result = apiInstance.wcCreatePost(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling WaterApi#wcCreatePost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**WaterConnectionRequest**](WaterConnectionRequest.md)| Details for the new property + RequestHeader meta data. |

### Return type

[**WaterConnectionResponse**](WaterConnectionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="wcDeletePost"></a>
# **wcDeletePost**
> ResponseInfo wcDeletePost(body, tenantId, connectionNo)

Delete existing water connection.

Delete existing water connection.

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.WaterApi;


WaterApi apiInstance = new WaterApi();
RequestInfo body = new RequestInfo(); // RequestInfo | Request header for the connection delete Request.
String tenantId = "tenantId_example"; // String | Unique id for a tenant.
String connectionNo = "connectionNo_example"; // String | The connection no to be deactivated
try {
    ResponseInfo result = apiInstance.wcDeletePost(body, tenantId, connectionNo);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling WaterApi#wcDeletePost");
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

<a name="wcSearchPost"></a>
# **wcSearchPost**
> WaterConnectionResponse wcSearchPost(body, tenantId, ids, connectionNo, oldConnectionNo, mobileNumber, fromDate, toDate)

Get the list of exsting water connections.

Get the water connections list based on the input parameters. 

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.WaterApi;


WaterApi apiInstance = new WaterApi();
RequestInfo body = new RequestInfo(); // RequestInfo | RequestHeader meta data.
String tenantId = "tenantId_example"; // String | Unique id for a tenant.
List<String> ids = Arrays.asList("ids_example"); // List<String> | List of system generated ids of water connection.
List<String> connectionNo = Arrays.asList("connectionNo_example"); // List<String> | List of water connection numbers to search..
List<String> oldConnectionNo = Arrays.asList("oldConnectionNo_example"); // List<String> | List of old water connection numbers to search..
Long mobileNumber = 789L; // Long | MobileNumber of owner whose water connection is to be searched.
BigDecimal fromDate = new BigDecimal(); // BigDecimal | Fetches properties with created time after fromDate.
BigDecimal toDate = new BigDecimal(); // BigDecimal | Fetches properties with created time till toDate.
try {
    WaterConnectionResponse result = apiInstance.wcSearchPost(body, tenantId, ids, connectionNo, oldConnectionNo, mobileNumber, fromDate, toDate);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling WaterApi#wcSearchPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**RequestInfo**](RequestInfo.md)| RequestHeader meta data. |
 **tenantId** | **String**| Unique id for a tenant. |
 **ids** | [**List&lt;String&gt;**](String.md)| List of system generated ids of water connection. | [optional]
 **connectionNo** | [**List&lt;String&gt;**](String.md)| List of water connection numbers to search.. | [optional]
 **oldConnectionNo** | [**List&lt;String&gt;**](String.md)| List of old water connection numbers to search.. | [optional]
 **mobileNumber** | **Long**| MobileNumber of owner whose water connection is to be searched. | [optional]
 **fromDate** | **BigDecimal**| Fetches properties with created time after fromDate. | [optional]
 **toDate** | **BigDecimal**| Fetches properties with created time till toDate. | [optional]

### Return type

[**WaterConnectionResponse**](WaterConnectionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="wcUpdatePost"></a>
# **wcUpdatePost**
> WaterConnectionResponse wcUpdatePost(body)

Update existing water connection details.

Updates a given &#x60;water connection&#x60; with newer details.

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.WaterApi;


WaterApi apiInstance = new WaterApi();
WaterConnectionRequest body = new WaterConnectionRequest(); // WaterConnectionRequest | Request of water connection details.
try {
    WaterConnectionResponse result = apiInstance.wcUpdatePost(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling WaterApi#wcUpdatePost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**WaterConnectionRequest**](WaterConnectionRequest.md)| Request of water connection details. |

### Return type

[**WaterConnectionResponse**](WaterConnectionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

