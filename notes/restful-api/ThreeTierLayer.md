# `3-Tier` layers Architecture

## **1. `controller` - Receptionist**

> _đây là `endpoint` của hệ thống, nơi cho phép tiếp xúc với Frontend như Web/MobileApp, ..._

Nhiệm vụ:

- nhận Request -> bắt đúng `URL` + `HTTP Method`
- đọc request params, request body, ...
- `validations`
- gọi `service` để xử lý request
- phản hồi cho client

**Note:** `controller` không nên chứa các lệnh `if/else` nghiệp vụ phức tạp, **nhiệm vụ của `controller` là việc chuyển tiếp request/response tới client**

## **2. `service` - business logic**

> _`service` là nơi chứa toàn bộ logic nghiệp vụ xử lý request từ `controller`_

Nhiệm vụ:

- nhận request + data từ `controller`
- xử lý logic // **business logic**
- ném `Exception` / phản hồi - `Response` cho `controller`

**Note:** `service` chỉ quan tâm tới `data` và `business logic`, không quan tâm **client là ai** -> _**`service` không nên chứa các khái niệm liên quan tới HTTP như `HttpServletRequest`, `ResponseEntity` hay `JSON`**_

## **3. `repository` - database communication**

Nhiệm vụ: **giao tiếp (`truy vấn`) database**

**Note:** `repository` chỉ thực hiện nhiệm vụ `read/write data`, **_không xử lý, tính toán bất kỳ business logic nào_**

