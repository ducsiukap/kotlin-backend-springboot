# Spring **_Security_**

1. [Spring Security, Authentication, Authorization & JWT Authentication](./SpringSecurity.md)
2. [Spring security & JWT authentication implementation](./SpringSecurityImpl.md)
3. hides `appilication.properties`:
   - **Cách 1**: dùng biến môi trường

     ```properties
     # example
     spring.datasource.url=jdbc:mysql://localhost:3306/your_db
     spring.datasource.username=root

     # use enviroment variable
     spring.datasource.password=${DB_PASSWORD}
     ```

     then, in **IntelliJ**:
     - click on **_Run/Debug Configurations_**
     - then, select your application -> **Modifi options** -> **_Enviroment Variables_**
     - add enviroment variable: `VAR1=secret; VAR2=secret2`

       > _**Note**: không được có khoảng trắng, `APP_PASSWORD=xxxxxxxxxxxxxxxx` thay vì xxxx xxxx xxxx xxxx_

     > _**Cách 1** là cách chuẩn của **Spring (Cloud Native)**, sừa sạch code, vừa dễ quản lý bằng **Docker**/**Kubernetes**_

   - **Cách 2**: use `dotenv-java`

   - **Cách 3**: **External Config**
     > **Spring Boot** có cơ chế: nếu để `applications.properties` **cùng cấp** với file `.jar`, **nó sẽ ưu tiên lấy file đó** thay vì lấy file trong `/resources` (_dùng được khi đã build project -> `.jar` và deloy => tạo `application.properties` đặt cùng chỗ với file `.jar` đó_).

---

## **Contact**

**[LinkedIn](https://www.linkedin.com/in/duc-pham-b19b66351/)  
[Mail](mailto:ducpv.contact@gmail.com)  
[Instagram](https://www.instagram.com/vduczz/)**

---

#vduczz
