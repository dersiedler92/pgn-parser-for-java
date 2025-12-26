# DefaultApi

All URIs are relative to *http://localhost*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**convertPgnToCombined**](#convertpgntocombined) | **POST** /pgn/combined | Convert a PGN into combined PGN|
|[**convertPgnToSeparated**](#convertpgntoseparated) | **POST** /pgn/separated | Convert a PGN into separated PGN|

# **convertPgnToCombined**
> CombinedPgnResponse convertPgnToCombined(pgnRequest)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    PgnRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let pgnRequest: PgnRequest; //

const { status, data } = await apiInstance.convertPgnToCombined(
    pgnRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **pgnRequest** | **PgnRequest**|  | |


### Return type

**CombinedPgnResponse**

### Authorization

[basicAuth](../README.md#basicAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | PGN converted successfully |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **convertPgnToSeparated**
> SeparatedPgnResponse convertPgnToSeparated(pgnRequest)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    PgnRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let pgnRequest: PgnRequest; //

const { status, data } = await apiInstance.convertPgnToSeparated(
    pgnRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **pgnRequest** | **PgnRequest**|  | |


### Return type

**SeparatedPgnResponse**

### Authorization

[basicAuth](../README.md#basicAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | PGN converted successfully |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

