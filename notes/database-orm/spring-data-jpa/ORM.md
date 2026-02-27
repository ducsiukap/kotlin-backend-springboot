# `ORM` - Object-Relational Mapping

> _Trong `Node.js`, `Mongoose` bản chất là **ODM - Object-Document Mapping** bởi `Object` của Javascript tương tự với `Document (JSON)` của Mongoose_

Tuy nhiên, Object trong **Java**/**Kotlin** không giống với Entity của **MySQL** => gặp vấn đề **`Paradigm Mismatch`**:

- `Object-Oriented` : Kotlin có:
  - Class
  - Inheritance
  - List/Set, ..
  - Object móc nối với nhau bằng tham chiếu - Reference
- `Relational - SQL` : Database có:
  - Table
  - Column
  - Row
  - Liên kết bằng `FK` - foreign key

> _So với `Code` (Object-Oriented), `Database` (Relational/SQL) **không thể đưa Object thành 1 Column của Table**_

Do vậy, sự khác biệt này khiến quá trình truy vấn và xử lý dài dòng, **_thủ công_**, **_dễ sai sót_** và code phức tạp.

## **`ORM` - Object-Relational Mapping : Ánh xạ `Object` thành `Relational`**

ORM là công cụ trung gian giống như Translator - phiên dịch giữa Code và DB:

- `O`: Java/Kotlin **_Class_**
- `R`: SQL **Table**
- `M`: Mapping -> ánh xạ: giữa **_Class_** (ex: `User`) và **_Table_** (ex: `users`), biến **Class's field** -> **Table's column**

> _ORM giúp ẩn việc thao tác với DB bằng cách thao tác trực tiếp với Object. Sau đó, ORM **tự động dịch** các thao tác thành các lệnh SQL như **`INSERT`**, **`UPDATE`**, **`DELETE`**, **`SELECT`**, ..._
