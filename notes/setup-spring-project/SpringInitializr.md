# Init Spring project using _Spring Initalizr_

Spring Initializr: [start.spring.io](start.spring.io)

### **Project setup**

> _select Project (Gradle - Kotlin, ...), Language (Kotlin, ...) and SpringBoot version_

### **Project metadata**

- `Group`: com.your_name, ... (thường là domain của công ty viêt ngược)
- `Artifact`: project's name
- `Name` : auto-generated based on `Artifact`
- `Description` : project description (optional)
- `Package name` : auto-generated based on `Group` and `Artifact`
- `Packaging` : Jar
- `Configuration` : Properties
- `Java` : 21 (recommended at 2/2026).

### **Dependencies**

- Click on **_ADD DEPENDENCIES..._** or using **_Ctrl+ B_** to add project dependencies.
- Example dependencies:
  - `Spring Web`: required to implement RESTful API
  - `Spring Data JPA`: ORM / Database using Java/Kotlin code OOP.
  - `PostgreSQL Driver` / `MySQL Driver`, ... : DB driver
  - `Validation`: input data validation
  - `Spring Security`, ...

### **Finally, just click on _GENERATE_**
