# `HTTP` **Methods** and **Status-Codes**

## 1. `HTTP` Protocol

`HTTP` - HyperText Transfer Protocol - **language of Internet**: is data-transfer protocol between:

- **Client**: browser, mobile app, ...
- **Server**: SpringBoot, NodeJS, ...

**HTTP Message Structure:**

```text
Start Line
Headers

Body (optional)
```

## 2. Request & `HTTP methods`

**HTTP Request message structure:**

- 1. **`Request Line`** (Start Line):

  ```text
  METHOD /url HTTP/version

  ex:
  - METHOD: GET
  - URL: user
  - Version: HTTP/1.1
  ```

  **HTTP MethodsL:**
  - `GET`: -> `request / get` resource
    - empty body
    - not-modify data
  - `POST`: -> `create` a new resource
    - has body
    - create new resource
  - `PUT`: -> `update all` resources
    - override all resources
  - `PATCH`: -> `update a part of` resource
    - update recessary field only
  - `DELETE`: -> `delete` resource
  - some other methods: `HEAD`, `OPTIONS`, `TRACE`, `CONNECT`

- 2. **`Request Headers`** -> request metadata

  ```text
  Host: example.com
  Accept: application/json
  Authorization: Bearer jwtToken
  User-Agent: Chrome
  ```

  - **General Headers**:
    - `Cache-Control`
    - `Connection`
    - `Date`
  - **Request Information Headers**
    - `Host: example.com` -> required (because server can have multi-host)
    - `User-Agent: Mozilla/5.0 Chrome/145` -> client browser
    - `Referer: https://google.com` -> previous site of client

  - **Authentication Headers**
    - `Authorization: <type> <credentials>` -> token / credential
      - type=**Bearer** -> credentials=_JWT Token_
      - type=**Basic** -> credentials= _EncodeBase64(username:password)_
      - type=ApiKey -> credential= _API key_
        > _or custom header: `X-API-KEY: <apikey>`_
  - **Content Headers**
    - `Content-Type` - type of body's content that client had sended
      - application/json -> JSON
      - multipart/form-data -> form có files: images, pdf, excel, ...
      - application/x-www-form-urlencoded -> normal form without files

      > _nếu sai Content-Type, server thường trả **415 Unsupported Media Type**_

    - `Content-Length`: body length (bytes)

  - **Content Negotiation Header**
    - `Accept`: -> type of data that client expected (can be multiple)
      - application/json
      - application/xml

    > _Nếu server không hỗ trợ -> **406 Not Acceptable**_
    - `Accept-Language: en-US` -> multi-language
    - `Accept-Encoding` -> server can compress response, ex: gzip, deflate,...

  - **State Management**
    - `Cookies: sessionId=abc123; theme=dark`
  - **CORS Hearders**
    - `Origin: http://localhost:3000`

- 3. **Empty line**
- 4. **`Request Body` - optional** - can be: JSON, HTML, XML, Files, ...

## 3. Response & `HTTP Status Codes`

**1. Status Line** - Start Line

```text
HTTP/version Status-Code Reason-Phrase

ex:
HTTP/1.1 200 OK
```

- HTTP `status-code`:
  - `1xx` -> Information
  - `2xx` -> Success
    - `200 OK`
    - `201 Created`
    - `204 No Content`
  - `3xx` -> Redirect
    - `301` -> Redirect vĩnh viễn
    - `302 Temporary Redirect`
  - `4xx` -> Client-side Error
    - `400 Bad Request`
    - `401 Unauthorized` -> thiếu Authorization / token, ...
    - `403 Forbidden` -> authorized but cannot access (không đủ quyền)
    - `404 Not Found`
    - `405 Method Not Allowed` -> wrong HTTP method
    - `409 Conflict` -> trùng dữ liệu
    - `415 Unsupported Media Type` -> wrong Content-Type
  - `5xx` -> Server-side Error
    - `500 Internal Server Error`
    - `502 Bad Gateway`
    - `503 Service Unavailable` -> server quá tải

**2. Response Headers**

**3. \<empty-line\>**

**4. Body**
