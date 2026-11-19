# AssetPilot API Gateway

## 1. Overview

The AssetPilot API Gateway provides a single entry point for accessing the backend services.

It receives client requests and forwards them to the appropriate microservice based on the configured routing rules.

### Current architecture

| Component | URL | Technology |
|---|---|---|
| API Gateway | `http://localhost:8083` | Spring Cloud Gateway WebMVC |
| Warranty Service | `http://localhost:8082` | Spring Boot + SOAP |
| Maintenance Service | Internal gRPC communication | gRPC |

### Current routing configuration

| Route ID | Gateway path | Destination |
|---|---|---|
| `asset-rest` | `/api/v1/**` | `${ASSET_SERVICE_URL:http://localhost:8081}` |
| `asset-openapi` | `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` | `${ASSET_SERVICE_URL:http://localhost:8081}` |
| `warranty-soap` | `/ws/**` | `${WARRANTY_SERVICE_URL:http://localhost:8082}` |

CORS is configured globally on the Gateway (`CORS_ALLOWED_ORIGINS`, default `http://localhost:3000,4200,5173`) for the front-end.

The Gateway preserves the original request path when forwarding requests.

For example:

```text
Client
  |
  v
http://localhost:8083/ws/warranties.wsdl
  |
  v
API Gateway
  |
  v
http://localhost:8082/ws/warranties.wsdl
  |
  v
Warranty Service
```

---

## 2. Prerequisites

Before testing, make sure you have:

- Java 17 or a compatible Java version.
- Maven installed.
- Postman installed.
- AssetPilot project cloned locally.

Both applications must be running simultaneously.

### Start Warranty Service

Open a terminal:

```powershell
cd warranty-service
mvn spring-boot:run
```

Warranty Service runs on:

```text
http://localhost:8082
```

### Start API Gateway

Open another terminal:

```powershell
cd gateway
mvn spring-boot:run
```

API Gateway runs on:

```text
http://localhost:8083
```

Keep both terminals running while testing.

---

## 3. Test Gateway health using Postman

This test verifies that the Gateway application is running.

1. Open Postman.
2. Create a new HTTP request.
3. Select the `GET` method.
4. Enter the following URL:

```http
http://localhost:8083/actuator/health
```

5. Click **Send**.

Expected response:

```json
{
    "groups": [
        "liveness",
        "readiness"
    ],
    "status": "UP"
}
```

Expected HTTP status:

```text
200 OK
```

---

## 4. Test Warranty WSDL through the Gateway

The Warranty Service exposes its SOAP contract through a WSDL document.

Instead of accessing the Warranty Service directly, we will access it through the API Gateway.

### Request configuration

| Parameter | Value |
|---|---|
| Method | GET |
| URL | `http://localhost:8083/ws/warranties.wsdl` |

In Postman:

1. Create a new request.
2. Select `GET`.
3. Enter:

```http
http://localhost:8083/ws/warranties.wsdl
```

4. Click **Send**.

Expected result:

```text
HTTP 200 OK
```

The response should contain an XML WSDL document describing the Warranty SOAP service.

### Compare direct and Gateway access

You can compare the two URLs:

**Direct Warranty Service:**

```http
http://localhost:8082/ws/warranties.wsdl
```

**Through API Gateway:**

```http
http://localhost:8083/ws/warranties.wsdl
```

Both should return the WSDL document.

This confirms that the Gateway successfully forwards requests to Warranty Service.

---

## 5. Test SOAP operations through Postman

The Warranty Service currently exposes two SOAP operations:

- `CheckWarrantyRequest`
- `GetWarrantyDetailsRequest`

The SOAP endpoint is:

```http
POST http://localhost:8083/ws
```

### Step 1: Create a POST request

In Postman:

1. Create a new request.
2. Select `POST`.
3. Enter:

```http
http://localhost:8083/ws
```

### Step 2: Configure request headers

Open the **Headers** tab and add:

| Key | Value |
|---|---|
| Content-Type | `text/xml; charset=utf-8` |

### Step 3: Configure the request body

Open the **Body** tab:

1. Select **raw**.
2. Select **XML** from the format dropdown.
3. Use the SOAP envelope structure below as a template.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope
    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:war="http://assetpilot.com/warranty/v1">

    <soapenv:Header/>

    <soapenv:Body>
        <war:GetWarrantyDetailsRequest>
            <!-- Add the required fields defined in warranty.xsd -->
        </war:GetWarrantyDetailsRequest>
    </soapenv:Body>

</soapenv:Envelope>
```

**Important:** The exact request fields must be taken from:

```text
warranty-service/src/main/resources/xsd/warranty.xsd
```

Use the fields and values required by that schema. The example above illustrates the SOAP envelope structure; it is not a complete request payload.

### Step 4: Send the request

Click **Send**.

The Gateway should forward the SOAP request to:

```http
http://localhost:8082/ws
```

Inspect the response in Postman.

Possible responses include:

- SOAP response containing the requested warranty information.
- SOAP Fault if the request is invalid or the warranty cannot be found.
- HTTP error if the service or Gateway is unavailable.

---

## 6. Suggested Postman Collection

Create a collection named:

**AssetPilot API Gateway Tests**

Add the following requests:

| Request name | Method | URL |
|---|---|---|
| Gateway Health | GET | `http://localhost:8083/actuator/health` |
| Warranty WSDL | GET | `http://localhost:8083/ws/warranties.wsdl` |
| Check Warranty | POST | `http://localhost:8083/ws` |
| Get Warranty Details | POST | `http://localhost:8083/ws` |

For the two SOAP operations, use their respective XML request bodies and the fields defined in `warranty.xsd`.

---

## 7. Troubleshooting

| Problem | Possible cause | Solution |
|---|---|---|
| Connection refused on port 8083 | Gateway is not running | Start Gateway |
| Connection refused on port 8082 | Warranty Service is not running | Start Warranty Service |
| 404 Not Found | Incorrect Gateway route or request path | Verify `/ws/**` route |
| 500 Internal Server Error | Error inside the backend service | Inspect Warranty Service logs |
| SOAP Fault | Invalid XML, missing fields, or business error | Check the XSD and SOAP response |
| WSDL works directly but not through Gateway | Gateway routing configuration issue | Verify the `warranty-soap` route |

---

## 8. Current Implementation Status

| Feature | Status |
|---|---|
| Gateway project initialization | Completed |
| Gateway application configuration | Completed |
| Gateway running on port 8083 | Completed |
| Warranty Service running on port 8082 | Completed |
| SOAP WSDL exposure | Completed |
| SOAP WSDL accessible through Gateway | Tested successfully |
| SOAP business operations through Gateway | To be tested |
| Asset Service routing | Pending |
| Additional REST routes | Pending |
| OpenAPI documentation | Pending |

---

## 9. Important URLs

| Description | URL |
|---|---|
| API Gateway | `http://localhost:8083` |
| Gateway health | `http://localhost:8083/actuator/health` |
| Warranty Service | `http://localhost:8082` |
| Direct Warranty WSDL | `http://localhost:8082/ws/warranties.wsdl` |
| Gateway Warranty WSDL | `http://localhost:8083/ws/warranties.wsdl` |
| Gateway SOAP endpoint | `http://localhost:8083/ws` |

**Note:** The Gateway currently exposes the Warranty SOAP service. Additional routes will be configured as the other AssetPilot services become available.