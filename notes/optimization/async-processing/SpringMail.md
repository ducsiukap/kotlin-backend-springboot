# Spring **_Mail_**

### **1. Dependencies**:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-mail")
```

### **2. Google _`App Password`_**

- **_First_**: Open [Google Account](https://myaccount.google.com/) and **login to your account**

- **_Second_**: go to **_Security & sign-in_** and turn-on `2-Step Verification`

- **_Step 3_**: search "**_App Password_**" on **Search Bar** and go to **`App Password`** (Security)

- _**Step 4**_: enter your app name (ex: "DemoMailSender"), click **Create** and **copy** your **_Generated app password_**

### **`3. spring.mail` configuration**:

Details: [application.properties](/codes/mini-project/src/main/resources/application.properties)

```properties
# mail host
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com

# the generated password in step 2
spring.mail.password=xxxx xxxx xxxx xxxx

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```