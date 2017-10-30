# Central KMS API
***

The central kms has a set of APIs targeted for admin users. Functionality includes sending healthchecks to connected sockets, querying sockets for batch results, etc.

## Endpoints
* [**Get registered sidecars**](#get-registered-sidecars)
* [**Create health check**](#create-health-check)
* [**Create inquiry**](#create-inquiry)
* [**Get inquiry result**](#get-inquiry)

---

### Get Registered Sidecars

#### Request
```http 
GET /sidecars HTTP/1.1
```

#### Response 200 OK
| Field | Type | Description |
| ----- | ---- | ----------- |
| Object | Array | Currently registered [Sidecars](#sidecar) |

#### Example Request and Response
``` http
  GET /sidecars HTTP/1.1

  ...

  HTTP/1.1 200 OK
  Content-Type: application/json
  [
    {
      "id": "5AF24DE8-768D-4A27-A9D0-E8B767A2E95C",
      "serviceName": "central-ledger",
      "status": "active"
    },
    {
      "id": "94C339DB-EBF8-42F2-AA26-83B3DE5417C8",
      "serviceName": "central-directory",
      "status": "active"
    }
  ]
```

---

### Create Health Check

#### HTTP Request
``` http
POST /sidecars/{id}/healthcheck HTTP/1.1
Content-Type: application/json
{
  "level": "{level}"
}
```

#### Request Params
| Field | Type | Required | Description |
| ----- | ---- | -------- | ----------- |
| id | UUID | yes | Id of targeted sidecar |
| level | [Health Check Level](#health-check-level) | yes | Type of health check to perform |

#### Responses
##### 200 OK
| Field | Type | Description |
| ----- | ---- | ----------- |
| Object | [HealthCheck](#health-check) | Created health check |

``` http
  HTTP/1.1 200 OK
  Content-Type: application/json
  {
    "id": "33C4FBDC-75AA-4F94-B1B5-6003D7755779",
    "sidecarId": "0B6982C0-566A-4A95-9412-D2A6FDD7CFD9",
    "level": "ping",
    "created": "2017-07-10T15:45:32Z",
    "status": "pending"
  }
```

##### Errors
| Status Code | Code | Message |
| ----------- | ---- | ------- |
| `404` | `40401` | `Sidecar {id} is not registered` |

---

### Create Inquiry

#### Http Request
``` http
POST /inquiry HTTP/1.1
Content-Type: application/json
{
  "service": {service},
  "startTime": {startTime},
  "endTime": {endTime}
}
```

#### Request Params
| Field | Type | Required | Description |
| ----- | ---- | -------- | ----------- |
| service | String | yes | Name of service to query |
| startTime | DateTime | yes | Timestamp of earliest log to return |
| endTime | DateTime | yes | Timestamp of latest log to return |

#### Responses
##### 201 Created
``` http
HTTP/1.1 201 Created
Location: /inquiry/{inquiryId}
```

##### Errors
| Status Code | Code | Message |
| ----------- | ---- | ------- |
| `400` | `40010` | `Date range must be greater than 0 and less than 30 days` |
| `400` | `40002` | `No current sidecars are registered for service '{serviceName}'` |

---

### Get Inquiry

#### Http Request
``` http
GET /inquiry/{id} HTTP/1.1
```

#### Request Params
| Field | Type | Required | Description |
| ----- | ---- | -------- | ----------- |
| id | UUID | yes | Id of inquiry to receive |

#### Responses
##### 200 OK
| Field | Type | Description |
| ----- | ---- | ----------- |
| Object | [Inquiry Summary](#inquiry-summary) | Inquiry summary |

``` http
HTTP/1.1 200 OK
Content-Type: application/json
{
  "status": "pending",
  "total": 100,
  "completed": 50,
  "results": [
    {
      "id": "9E8AAFEE-AA7B-4B80-9649-C480A3B31090",
      "body": "",
      "verified": true
    }
  ]
}
```

##### Errors
| Status Code | Code | Message |
| ----------- | ---- | ------- |
| `404` | `40400` | `Inquiry with id of {id} does not exist` |

---

## Data Structures

### Sidecar

| Name | Type | Description |
| ---- | ---- | ----------- |
| `id` | UUID | Sidecar unique identifier |
| `serviceName` | String | Name of the service sidecar is attached to |
| `status` | String | Current [status](#sidecar-status) of sidecar

### Health Check

| Name | Type | Description |
| ---- | ---- | ----------- |
| `id` | UUID | Health check unique identifier |
| `sidecarId` | UUID | Id of sidecar health check was issued to |
| `level` | [Health Check Level](#health-check-level) | Type of health check |
| `created` | DateTime | Timestamp when health check was created |
| `status` | [Health Check Status](#health-check-status) | Current status of health check |

### Inquiry Summary

| Name | Type | Description |
| ---- | ---- | ----------- |
| `status` | InquiryStatus | Status of the inquiry |
| `total` | Integer | Number of expected results |
| `completed` | Integer | Number of completed results |
| `results` | Array | Summary of [responses](#inquiry-response-summary) from sidecars |

### Inquiry Response Summary

| Name | Type | Description |
| ---- | ---- | ----------- |
| `id` | UUID | Id of the batch |
| `body` | String | Plaintext body of batch |
| `verified` | Boolean | Specifies if batch could be verified by central-kms |

## Enumerations
### Sidecar Status

| Name | Description |
| ---- | ----------- |
| `challenged` | Sidecar has connect to socket but has yet to respond to challenge attempt |
| `registered` | Sidecar has successfully responded to challenge is engaging in requests |
| `terminated` | Sidecar socket has been terminated |
| `suspended` | Sidecar socket has been disrupted due to violation of the contract |

### Health Check Level

| Name | Description |
| ---- | ----------- |
| `ping` | Basic health check |

### Health Check Status

| Name | Description |
| ---- | ----------- |
| `pending` | Health check has been issued but no response has been recorded |

### Inquiry Status

| Name | Description |
| ---- | ----------- |
| `created` | Inquiry has been sent to register sidecars but no results have been obtained |
| `pending` | Some results have been obtained |
| `complete` | All expected results have been returned |0