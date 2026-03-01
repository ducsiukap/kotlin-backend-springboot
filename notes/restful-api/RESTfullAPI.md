# `RESTful` API

## **1. REST, RESTful & API**

#### `API` - Application Programming Interface - Giao diện lập trình ứng dụng

> _**`API`** là `endpoint` cho phép giao tiếp giữa **server** và **client**, mở cổng để `client` **yêu cầu** `server` thực hiện task_

#### `REST` - Representational State Transfer

> _**`REST`** là kiến trúc thiết kế API qua HTTP (hệ thống Web) sao cho: **resource-based**, **use rightly HTTP methods & codes** and **Stateless**_

`REST` là **lí thuyết**, **`RESTful`** là cách **implements đúng** (thiết kế API **tuân theo các quy tắc**) theo lý thuyết của `REST`.

## **2. Principle to design a `RESTful API`**

### _2.1. `Resource-based` API_

> _In `REST`, anything in **database**, such as User, Product, ..., is `resources`_

**Nguyên tắc đặt tên cho URL**: **_URL chỉ được chứa DANH TỪ_**, hành động đã được thể hiện thông qua **HTTP method** như `GET`, `POST`, `PUT`, ...

**RESTful-style URL** -> rightly

- `GET /api/users` -> get list users
- `POST /api/users` -> create new user
- `DELETE /api/users/1` -> delete user with id=1

**example wrong URL**:

- `RPC-style` -> wrong
  - `GET /api/get-all-users`
  - `POST /api/create-new-user`
  - `POST /api/delete-user?id=1`

### _2.2. **Stateless**_

> _Stateless is the **main-principle** of `RESTful APIs`_

**Stateless**: không trạng thái -> Server doesnot care about _who is client?_.

> _each `request` from client is a **independent-package**, include `request-information` and `token` (to authorization)_

Cụ thể, **Server không được biết: Request này từ client trước đó**. Thay vào đó, **_sau khi `thực hiện xong request, nó không giữ thông tin gì về client`, mỗi `request mới` lên, nó `phải sử dụng thông tin xác thực` của client gửi kèm để `xác định lại client là ai`_**

> _Nhờ vậy, server có thể `scalable` lên nhiều server cùng lúc mà không lo vấn đề **đồng bộ bộ nhớ**_

## _2.3. `Representation` - Giao tiếp bằng ngôn ngữ chung_

Để có thể giao tiếp giữa **Server** và **Client**, `RESTful API` **BẮT BUỘC** phải chuyển đổi mọi thứ ra **`standard-format`** - thứ **readable for any programming-language**

- `XML - eXtensible Markup Language`: là chuẩn cũ
- `JSON - JavaScript Object Notation`: chuẩn mới -> **text-based**, gọn, nhẹ, dễ đọc, có thể dùng để **lưu trữ**/**truyền** dữ liệu.
