# Spring _**Security**_

## **1. `Authentication` vs `Authorization`**

- **`Authentication`** - Xác thực: _**"Who r u?"**_

  > _Vì **RESTful APIs** là **stateless**, để xác thực **client** là **ai**, **server** cần cơ chế `authentication`_

- **`Authroization`** - Phân quyền: _**"What you can do?"**_
  > _Sau khi `authentication`, **server** cần cơ chế `authorization` để xác định xem **client** có được **quyền truy cập** vào **tài nguyên nó đang yêu cầu** không_

## **2. Spring _Security_**

**Spring Security** _không phải_ 1 layer nằm trong `controller`, nó là một **chuỗi lọc - `Filter Chain`** được đặt ngoài cùng của **server**, **trước** khi **Request** lọt vào được tới **`server` và `controller`**

**Filter Chain** example:

- `HTTP request`
- **`CORS` Checking**
- **Logout/Login processing**
- **`Token`**
- **`Authorization`**
- ...

> _Nếu ở bất kì `chain` nào, **Request không đáp ứng được yêu cầu** (có dấu hiệu khả nghi) => `chain` đó lập tức **rejects client request**, trả về lỗi `4XX`. Do vậy, **server** và `controller` **được bảo vệ**_

## **3. `Stateful`**

Trước đây, `security` được triển khai bằng cơ chế **Session** / **Cookie**:

- **Client** login
- **Server** checking `login credentials`. If ok:
  - **server** trả về cho client `SESSION_ID=...`
  - Lưu thông tin về client vào bộ nhớ

Do vậy, `stateful` gây:

- **Tốn `memory`**: khi lượng `users` (**client**) lớp, `session` trên server (_dùng để lưu thông tin các client_) **phình to** và **chiếm dụng bộ nhớ lớn**  
  => có thể gây **crash server** hoặc **down-performance** do **cạn RAM**
- **Khó `scale`**: khi lượng `users` trở nên **quá lớn**, hệ thống cần mở thêm **server2**, **server3**, ...  
  => Vấn đề **đồng bộ bộ nhớ** giữa các `server` xảy ra, **client** bị `reject` do `request` request được **phục vụ bởi server khác so với server xác thực** nó

## **4. `Stateless` & `JWT`- JSON Web Token**

Sau khi `login`, thay vì trả `SESSION_ID` và **lưu thông tin _client_ vào `session`**, `server` thực hiện:

- Sử dụng **Secret Key** để `signature` (kí) lên thông tin của **client**, nén lại thành `JWT token`, sau đó:
  - trả `token` về cho client
  - không lưu thông tin gì về client ở serverr
- Khi **client** gửi request lên API cần xác thực, **client cần gửi kèm `JWT token`**, sau đó:
  - server lấy **Secret Key**
  - giải mã `token` bằng **Secret key**

> _Nếu `token` đúng -> request của client được đưa vào `controller` tiếp nhận. Ngược lại, **lỗi 4XX sẽ được trả về client**_

Lợi ích của **JWT Authentication**:

- **`Stateless`** : **server không cần lưu thông tin client trong bộ nhớ**
  > _Tránh được vấn đề **crash** / **down-performace** server do hết RAM_
- **Scalable**: Khi có **nhiều server**, việc duy nhất cần làm là dùng chung **Secret Key** cho các `server` => thuật toán **kí** và **giải mã** sẽ trả về **cùng 1 kết quả**.
  > _Do vậy, không cần giải quyết vấn đề **đồng bộ bộ nhớ** giữa các server_
