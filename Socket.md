# Central KMS Socket

The central kms contains a web socket api that allows sidecars connected to other services to save batch signatures, respond to health checks and search inquiries.

All messages between a sidecar and the kms conform to the jsonrpc spec outlined in the [JSON-RPC Spec](http://www.jsonrpc.org/specification)

The following flow allows a sidecar to register to the central kms

* [Connect to socket](#connect-to-socket)
* [Register](#register)
* [Respond to challenge](#challenge-response)

Once connected, the sidecar can issue the following commands to the kms:
* [Save batch](#save-batch)

On occasion, the kms will issue a command to the kms and expect a response from the sidecar. Those commands and responses are:
* [Health Checks](#health-check)
* [Inquiries](#inquiry)

---

## Errors

All errors specified will be in the following format: 
``` json
{
  "jsonrpc": "2.0",
  "error": {
    "code": {code},
    "message": "{message}"
  },
  "id": "{commandId}"
}
```

### Common Errors

These errors can occur as a response to any request on the kms socket

| Code | Message |
| ---- | ------- |
| `-32700` | `Parse error` |
| `-32600` | `Invalid Request` |
| `-32601` | `Method not found` | 
| `-32602` | `Invalid params` |
| `-32603` | `Internal error`	| 

---

## Connect to socket

### Request
``` http
wscat ws://central-kms/sidecar
```
---
## Register

### Request
``` json
{
  "jsonrpc": "2.0",
  "id": "{commandId}",
  "method": "register",
  "params": {
    "id": "{sidecarId}",
    "serviceName": "{serviceName}"
  }
}
```

### Request Params
| Field | Type | Required | Description |
| ----- | ---- | -------- | ----------- |
| `commandId` | String | yes | Id of register request. Used for correlation between request and response |
| `sidecarId` | UUID | yes | Unique identifier of sidecar. Recommendation to generate a new random UUID on each request |
| `serviceName` | String | yes | Name of service sidecar is attached to |

### Response
``` json
{
  "jsonrpc": "2.0",
  "result": {
    "id": "{sidecarId}",
    "batchKey": "{batchKey}",
    "rowKey": "{rowKey}",
    "challenge": "{challenge}"
  },
  "id": "{commandId}"
}
```

### Response Params
| Field | Type | Description |
| ----- | ---- | ----------- |
| `sidecarId` | UUID | Unique identifier of sidecar |
| `batchKey` | String | Hex encoded key to be used in ed25519 hashing of batch signatures |
| `rowKey` | UUID | yes | Hex encoded key to be used in CMAC hashing of row signatures |
| `challenge` | String | Random string expected to be hashed and the result returned in the challenge response
| `commandId` | String | CommandId from request |

### Errors
| Code | Message |
| ---- | ------- |
| `40001` | `Sidecar with id '{sidecarId}' already exists` |

---

## Challenge Response

In order to be fully registered, a sidecar must issue a challenge response to the kms to verify that the hashing algorithms have been set up correctly. The sidecar has at most one attempt to verify the challenge. If the challenge fails, an error will be returned and the socket connection will be terminated by the kms.

### Request
``` json
{
  "jsonrpc": "2.0",
  "id": "{commandId}",
  "method": "challenge",
  "params": {
    "batchSignature": "{batchSignature}",
    "rowSignature": "{rowSignature}"
  }
}
```

### Request Params
| Field | Type | Required | Description |
| ----- | ---- | -------- | ----------- |
| `commandId` | String | yes | Id of challenge response. Used for correlation between request and response |
| `batchSignature` | UUID | yes | ed25519 signature of challenge and batchKey from register response |
| `serviceName` | String | yes | CMAC signature of challenge and rowKey from register response |

### Response
``` json
{
  "jsonrpc": "2.0",
  "result": {
    "status": "OK"
  },
  "id": "{commandId}"
}
```

### Errors
| Code | Message |
| ---- | ------- |
| `40004` | `Invalid batch signature` |
| `40005` | `Invalid row signature` |

---

## Save Batch

In order to be fully registered, a sidecar must issue a challenge response to the kms to verify that the hashing algorithms have been set up correctly. The sidecar has at most one attempt to verify the challenge. If the challenge fails, an error will be returned and the socket connection will be terminated by the kms.

### Request
``` json
{
  "jsonrpc": "2.0",
  "id": "{commandId}",
  "method": "batch",
  "params": {
    "id": "{batchId}",
    "signature": "{signature}"
  }
}
```

### Request Params
| Field | Type | Required | Description |
| ----- | ---- | -------- | ----------- |
| `commandId` | String | yes | Id of challenge response. Used for correlation between request and response |
| `batchId` | UUID | yes | Unique identifier of the batch |
| `signature` | String | yes | ed25519 signature of the batch |

### Response
``` json
{
  "jsonrpc": "2.0",
  "result": {
    "id": "{batchId}"
  },
  "id": "{commandId}"
}
```

### Response Params
| Field | Type | Description |
| ----- | ---- | ----------- |
| `batchId` | UUID | Unique identifier of the batch |
| `commandId` | String | CommandId from request |

---

## Health Check

The kms may issue a health check request on the socket at any time. The sidecar is expected to respond to the command in a short amount of time.

### Request
``` json
{
  "jsonrpc": "2.0",
  "id": "{healthCheckId}",
  "method": "healthcheck",
  "params": {
    "level": "{level}"
  }
}
```

### Request params
| Field | Type | Description |
| ----- | ---- | ----------- |
| `healthCheckId` | UUID | Unique identifier of the health check. Must be returned in health check response |
| `level` | String | Level of health check to perform |

### Response
``` json
{
  "jsonrpc": "2.0",
  "result": {response},
  "id": "{healthCheckId}"
}
```

### Response Params
| Field | Type | Description |
| ----- | ---- | ----------- |
| `healthCheckId` | UUID | Unique identifier of the health check from the request |
| `response` | Object | Data needed to complete health check |

---

## Inquiry

The kms will from time to time issue inquiry requests to the socket. The connected sidecar should gather the batches that satisfy the inquiry and return them in the response

### Request
``` json
{
  "jsonrpc": "2.0",
  "id": "{commandId}",
  "method": "inquiry",
  "params": {
    "inquiry": "{inquiryId}",
    "startTime": "{startTime}",
    "endTime": "{endTime}"
  }
}
```

### Request params
| Field | Type | Description |
| ----- | ---- | ----------- |
| `commandId` | String | Id of inquiry request |
| `inquiryId` | UUID | Unique identifier of the inquiry |
| `startTime` | DateTime | Earliest time of result to return |
| `endTime` | DateTime | Latest time of result to return |

### Response
``` json
{
  "jsonrpc": "2.0",
  "result": {
    "id": "{batchId}",
    "body": "{body}",
    "inquiry": "{inquiryId}",
    "total": "{total}",
    "item": "{item}"
  },
  "id": "{commandId}"
}
```

### Response Params
| Field | Type | Description |
| ----- | ---- | ----------- |
| `commandId` | String | Unique identifier of the inquiry result from the request |
| `batchId` | UUID | Unique identifier of batch |
| `body` | String | Plaintext body of the batch |
| `inquiry` | UUID | Unique identifier of the inquiry from the request |
| `total` | Integer | Total number of batches that satisfy the inquiry |
| `item` | Integer | Relative position of this batch in the list of batches that satisfy the inquiry |
