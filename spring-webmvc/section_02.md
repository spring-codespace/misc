# Spring MVC Web Application — Reference Journal

## **2. Setting Up a Spring MVC Project** 🟢

This section covers the various ways to set up a Spring MVC project, from modern Spring Boot applications to classic configurations, along with dependency management and containerized development workflows.

---

### **2.1 Spring Boot vs Classic Spring MVC**

Understanding the differences between Spring Boot and classic Spring MVC helps you choose the right approach for your project.

**Detailed Comparison Table**:

| Aspect | Spring Boot | Classic Spring MVC |
|--------|-------------|-------------------|
| **Configuration** | Auto-configuration, minimal setup | Manual configuration required |
| **Application Setup** | Opinionated defaults, convention-over-configuration | Explicit configuration in Java/XML |
| **Embedded Server** | Tomcat/Jetty/Undertow embedded by default | Requires external servlet container (WAR deployment) |
| **Dependency Management** | Starter dependencies with managed versions | Manual dependency and version management |
| **Project Structure** | Standard, predictable structure | Flexible but requires manual setup |
| **Development Speed** | Fast - running in minutes | Slower - more boilerplate setup |
| **Production Deployment** | Self-contained JAR with `java -jar` | WAR file deployed to application server |
| **Configuration Files** | `application.properties` or `application.yml` | `web.xml`, multiple XML files, or Java config |
| **DevTools Support** | Built-in hot reload with DevTools | Requires manual setup or IDE support |
| **Monitoring** | Actuator endpoints out-of-the-box | Manual integration required |
| **Learning Curve** | Lower - less to configure | Steeper - need to understand all components |
| **Flexibility** | High - can override any auto-configuration | Complete control from start |
| **Best For** | Modern applications, microservices, rapid development | Legacy integration, specific container requirements |

**When to Use Each Approach**:

**Use Spring Boot when**:
- Starting a new project (recommended for 95% of cases)
- Building microservices or cloud-native applications
- Need rapid development and deployment
- Want embedded server convenience
- Prefer convention over configuration
- Building standalone applications

**Use Classic Spring MVC when**:
- Maintaining legacy applications
- Deploying to existing application servers (WebLogic, WebSphere)
- Organization mandates WAR deployment to specific containers
- Need complete control over every configuration detail
- Integrating with existing enterprise infrastructure

**Migration Path from Classic to Boot**:

1. **Assessment Phase**:
   - Document current configuration (XML, Java Config)
   - Identify custom beans and configurations
   - List all dependencies and versions

2. **Create Spring Boot Project**:
   - Use Spring Initializr with equivalent dependencies
   - Copy application code (controllers, services, repositories)

3. **Migrate Configuration**:
   - Convert XML beans to `@Configuration` classes or properties
   - Replace `web.xml` with Spring Boot auto-configuration
   - Move servlet filters to Spring Boot configuration
   - Update datasource configuration to `application.properties`

4. **Update Dependencies**:
   - Replace individual dependencies with Spring Boot starters
   - Remove version numbers (managed by Spring Boot parent)
   - Update package names if moving from javax.* to jakarta.*

5. **Test and Refine**:
   - Run tests to ensure functionality is preserved
   - Adjust custom configurations as needed
   - Enable Actuator for monitoring

6. **Deployment**:
   - Package as executable JAR or WAR (if container required)
   - Update deployment scripts

**Example Migration**:

Before (Classic):
```xml
<!-- web.xml -->
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/spring/dispatcher-config.xml</param-value>
    </init-param>
</servlet>
```

After (Spring Boot):
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
All servlet configuration is automatic!

---

### **2.2 Creating a Spring MVC Application with Spring Boot**

Spring Boot makes creating Spring MVC applications straightforward with multiple initialization methods.

#### **Using Spring Initializr**

**Web Interface** (https://start.spring.io):

1. **Project Metadata**:
   - **Project**: Maven or Gradle
   - **Language**: Java (Kotlin and Groovy also supported)
   - **Spring Boot**: Latest stable version (e.g., 3.2.x)
   - **Packaging**: Jar (executable) or War (if deploying to external container)
   - **Java**: 17, 21, or latest LTS

2. **Project Coordinates**:
   - **Group**: `com.example` (your organization domain reversed)
   - **Artifact**: `demo` (project name)
   - **Name**: `demo` (application name)
   - **Description**: Brief project description
   - **Package name**: `com.example.demo` (base package)

3. **Dependencies** (for basic Spring MVC):
   - **Spring Web** - REST endpoints and Spring MVC
   - **Thymeleaf** - Server-side template engine
   - **Spring Boot DevTools** - Hot reload and developer tools
   - **Lombok** (optional) - Reduce boilerplate code
   - **Validation** - Bean validation with Hibernate Validator

4. **Generate** and download the ZIP file

**Command Line Interface** (Spring Boot CLI):

Install Spring Boot CLI first:
```bash
# macOS (Homebrew)
brew install springboot

# Linux (SDKMAN)
sdk install springboot

# Windows (Chocolatey)
choco install springbootcli
```

Create project:
```bash
spring init \
  --dependencies=web,thymeleaf,devtools \
  --build=maven \
  --java-version=21 \
  --packaging=jar \
  --name=demo \
  --group-id=com.example \
  --artifact-id=demo \
  demo
```

**IDE Integration**:

**IntelliJ IDEA**:
- File → New → Project → Spring Initializr
- Configure and select dependencies
- Generates project directly in IDE

**Eclipse/STS**:
- File → New → Spring Starter Project
- Similar wizard to web interface

**VS Code**:
- Install Spring Initializr Java Support extension
- Ctrl+Shift+P → "Spring Initializr: Create a Maven Project"

#### **Understanding Starter Dependencies**

Spring Boot starters are curated dependency descriptors that bring in all necessary libraries for a specific functionality.

**Core Starters for Spring MVC**:

1. **spring-boot-starter-web**:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
   ```
   **Includes**:
   - `spring-webmvc` - Spring MVC framework
   - `spring-web` - Web utilities
   - `spring-boot-starter-tomcat` - Embedded Tomcat
   - `spring-boot-starter-json` - JSON support (Jackson)
   - Logging (Logback, SLF4J)
   
   **Use for**: REST APIs and web applications

2. **spring-boot-starter-thymeleaf**:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-thymeleaf</artifactId>
   </dependency>
   ```
   **Includes**:
   - Thymeleaf template engine
   - Thymeleaf Spring integration
   - Thymeleaf layout dialect
   
   **Use for**: Server-side HTML rendering

3. **spring-boot-starter-validation**:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-validation</artifactId>
   </dependency>
   ```
   **Includes**:
   - Hibernate Validator (Jakarta Bean Validation implementation)
   - Jakarta Bean Validation API
   
   **Use for**: Form validation, DTO validation

4. **spring-boot-devtools**:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-devtools</artifactId>
       <scope>runtime</scope>
       <optional>true</optional>
   </dependency>
   ```
   **Features**:
   - Automatic application restart on code changes
   - LiveReload browser plugin support
   - Disabled template caching for development
   - Additional development-time features
   
   **Note**: Automatically disabled in production

**Other Common Starters**:

- `spring-boot-starter-data-jpa` - JPA with Hibernate
- `spring-boot-starter-security` - Spring Security
- `spring-boot-starter-test` - Testing libraries (JUnit, Mockito, AssertJ)
- `spring-boot-starter-actuator` - Production-ready features (health, metrics)

#### **Project Directory Structure and Conventions**

Standard Spring Boot project layout:

```
my-spring-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── demo/
│   │   │               ├── DemoApplication.java          # Main class
│   │   │               ├── controller/                   # Controllers
│   │   │               │   ├── HomeController.java
│   │   │               │   └── UserController.java
│   │   │               ├── service/                      # Business logic
│   │   │               │   ├── UserService.java
│   │   │               │   └── impl/
│   │   │               │       └── UserServiceImpl.java
│   │   │               ├── repository/                   # Data access
│   │   │               │   └── UserRepository.java
│   │   │               ├── model/                        # Domain objects
│   │   │               │   ├── User.java
│   │   │               │   └── Product.java
│   │   │               ├── dto/                          # Data Transfer Objects
│   │   │               │   ├── UserDTO.java
│   │   │               │   └── ProductDTO.java
│   │   │               ├── config/                       # Configuration classes
│   │   │               │   ├── WebConfig.java
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── exception/                    # Custom exceptions
│   │   │               │   ├── ResourceNotFoundException.java
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               └── util/                         # Utility classes
│   │   │                   └── DateUtil.java
│   │   └── resources/
│   │       ├── templates/                                # Thymeleaf templates
│   │       │   ├── index.html
│   │       │   ├── user/
│   │       │   │   ├── list.html
│   │       │   │   └── form.html
│   │       │   └── fragments/
│   │       │       ├── header.html
│   │       │       └── footer.html
│   │       ├── static/                                   # Static resources
│   │       │   ├── css/
│   │       │   │   └── style.css
│   │       │   ├── js/
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.png
│   │       ├── application.properties                    # Configuration
│   │       ├── application-dev.properties                # Dev profile
│   │       ├── application-prod.properties               # Prod profile
│   │       ├── messages.properties                       # i18n
│   │       └── data.sql                                  # Sample data (optional)
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── demo/
│                       ├── DemoApplicationTests.java
│                       ├── controller/
│                       │   └── UserControllerTest.java
│                       ├── service/
│                       │   └── UserServiceTest.java
│                       └── repository/
│                           └── UserRepositoryTest.java
├── pom.xml (or build.gradle)
├── mvnw (Maven wrapper)
├── mvnw.cmd
├── .gitignore
└── README.md
```

**Key Conventions**:

- **Base Package**: All application code under single root package (e.g., `com.example.demo`)
- **Main Class**: In root package with `@SpringBootApplication`
- **Templates**: In `src/main/resources/templates/` (auto-detected by Thymeleaf)
- **Static Files**: In `src/main/resources/static/` (served at `/`)
- **Configuration**: In `src/main/resources/` as `.properties` or `.yml` files
- **Test Structure**: Mirrors `src/main/java` structure in `src/test/java`

#### **Main Application Class (`@SpringBootApplication`)**

The main class is the entry point of a Spring Boot application:

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

**What `@SpringBootApplication` Includes**:

1. **@SpringBootConfiguration**: 
   - Specialization of `@Configuration`
   - Marks this as a configuration class
   - Allows defining additional beans

2. **@EnableAutoConfiguration**:
   - Enables Spring Boot's auto-configuration mechanism
   - Scans classpath and configures beans automatically
   - Example: Detects `spring-boot-starter-web` → configures `DispatcherServlet`

3. **@ComponentScan**:
   - Scans current package and sub-packages for components
   - Finds `@Controller`, `@Service`, `@Repository`, `@Component`
   - Registers them as Spring beans

**Customizing the Main Class**:

```java
@SpringBootApplication(
    scanBasePackages = {"com.example.demo", "com.example.shared"}, // Multiple packages
    exclude = {DataSourceAutoConfiguration.class}  // Exclude specific auto-configurations
)
public class DemoApplication {
    
    public static void main(String[] args) {
        // Customize before running
        SpringApplication app = new SpringApplication(DemoApplication.class);
        app.setBannerMode(Banner.Mode.OFF);  // Disable startup banner
        app.setAdditionalProfiles("dev");     // Activate profile
        app.run(args);
    }
    
    // Define additional beans if needed
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

#### **Configuration: `application.properties` vs `application.yml`**

Spring Boot supports both formats. Choose based on preference and team standards.

**application.properties**:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Logging
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
logging.file.name=logs/application.log

# Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Datasource (H2 example)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# DevTools
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

**application.yml** (equivalent):

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

logging:
  level:
    root: INFO
    com.example.demo: DEBUG
  file:
    name: logs/application.log

spring:
  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html
  
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
  
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

**Comparison**:

| Feature | .properties | .yml |
|---------|-------------|------|
| **Readability** | Flat, repetitive | Hierarchical, DRY |
| **Comments** | `# comment` | `# comment` |
| **Lists** | Indexed: `list[0]=a` | Native: `- a` |
| **Multi-document** | Not supported | Supported with `---` |
| **IDE Support** | Universal | Good in modern IDEs |
| **Strictness** | Forgiving | Whitespace-sensitive |

**Profile-Specific Configuration**:

Create environment-specific configs:
- `application-dev.properties` - Development
- `application-test.properties` - Testing  
- `application-prod.properties` - Production

Activate profile:
```bash
# Via command line
java -jar app.jar --spring.profiles.active=prod

# Via environment variable
export SPRING_PROFILES_ACTIVE=prod

# Via application.properties
spring.profiles.active=dev
```

#### **Running and Hot-Reloading with DevTools**

**Running the Application**:

1. **From IDE**:
   - Run `DemoApplication.main()` method
   - Uses IDE's classpath and enables debugging

2. **Maven**:
   ```bash
   ./mvnw spring-boot:run
   
   # With profile
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   
   # With arguments
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
   ```

3. **Gradle**:
   ```bash
   ./gradlew bootRun
   
   # With profile
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

4. **Executable JAR**:
   ```bash
   # Package
   ./mvnw clean package
   
   # Run
   java -jar target/demo-0.0.1-SNAPSHOT.jar
   
   # With profile
   java -jar target/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

**Spring Boot DevTools Features**:

1. **Automatic Restart**:
   - Monitors classpath changes
   - Restarts application when Java files change
   - Fast restart (only reloads application classes, not base libraries)
   
   **Trigger restart**: Save file in IDE or compile

2. **LiveReload**:
   - Browser auto-refresh when resources change
   - Install LiveReload browser extension
   - Automatic when DevTools is active

3. **Template Caching Disabled**:
   - Thymeleaf, FreeMarker templates reload without restart
   - See changes immediately

4. **Property Defaults**:
   - Optimized settings for development
   - Example: `spring.thymeleaf.cache=false`

**DevTools Configuration**:

```properties
# Exclude certain paths from triggering restart
spring.devtools.restart.exclude=static/**,public/**

# Additional paths to watch
spring.devtools.restart.additional-paths=src/main/resources

# Disable restart
spring.devtools.restart.enabled=false

# Trigger file (create this file to trigger restart instead of auto-detection)
spring.devtools.restart.trigger-file=.reloadtrigger
```

**Hot Swap vs Restart**:

| Mechanism | Speed | Scope | Use Case |
|-----------|-------|-------|----------|
| **Hot Swap** (JVM) | Instant | Method body changes only | Quick code fixes |
| **DevTools Restart** | Fast (~2-3s) | Any Java change | Structural changes |
| **Full Restart** | Slow (~10-30s) | Everything | Dependency changes |

---

### **2.3 Maven and Gradle Dependency Management**

Understanding dependency management is crucial for maintaining stable, reproducible builds.

#### **Maven - Essential Dependencies**

**Complete pom.xml Example**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Spring Boot Parent POM -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    
    <!-- Project Coordinates -->
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>demo</name>
    <description>Spring MVC Demo Project</description>
    
    <!-- Java Version -->
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <!-- No version needed - managed by parent -->
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Database Drivers -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok (optional) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- DevTools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### **Spring Boot Parent POM**

**What It Provides**:

1. **Dependency Management**:
   - Pre-defined versions for 300+ dependencies
   - Ensures compatible versions
   - No need to specify versions for managed dependencies

2. **Plugin Configuration**:
   - Maven compiler plugin configured for Java version
   - Spring Boot Maven plugin pre-configured
   - Resource filtering enabled

3. **Properties**:
   - Common properties (Java version, encoding)
   - Version properties for dependencies

**Benefits**:
```xml
<!-- Without parent - need to specify versions -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>6.1.2</version>
</dependency>

<!-- With parent - version managed -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <!-- Version inherited from parent -->
</dependency>
```

**Alternative - Dependency Management Without Parent**:

If you can't use the parent POM (already have a corporate parent):

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.2.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### **Understanding Transitive Dependencies**

Dependencies can bring their own dependencies (transitive dependencies).

**Example**:
```
spring-boot-starter-web
  ├── spring-webmvc
  │   ├── spring-web
  │   ├── spring-context
  │   └── spring-beans
  ├── spring-boot-starter-tomcat
  │   ├── tomcat-embed-core
  │   └── tomcat-embed-el
  └── spring-boot-starter-json
      └── jackson-databind
```

**View Dependency Tree**:
```bash
./mvnw dependency:tree
```

**Excluding Transitive Dependencies**:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <!-- Exclude default Tomcat, use Jetty instead -->
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- Add Jetty -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

#### **Gradle Alternative (build.gradle.kts)**

Kotlin DSL for Gradle (modern, type-safe):

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Database
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // DevTools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

**Groovy DSL (build.gradle)**:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.1'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '21'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    
    runtimeOnly 'com.h2database:h2'
    
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

#### **Dependency Version Conflicts and Resolution**

**Common Conflict Scenarios**:

1. **Multiple versions of same dependency**:
   - Maven uses "nearest definition" strategy
   - Dependency declared directly wins over transitive

2. **Incompatible transitive dependencies**:
   - Can cause runtime errors
   - Need to exclude and declare correct version

**Resolving Conflicts**:

**Maven - Enforce Versions**:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.3</version> <!-- Override managed version if needed -->
</dependency>
```

**Maven - Dependency Plugin**:
```bash
# Find conflicts
./mvnw dependency:tree -Dverbose

# Analyze
./mvnw dependency:analyze
```

**Gradle - View Conflicts**:
```bash
./gradlew dependencies --configuration compileClasspath
```

**Gradle - Force Version**:
```kotlin
configurations.all {
    resolutionStrategy {
        force("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    }
}
```

**Best Practices**:
- Let Spring Boot manage versions when possible
- Override only when necessary (security patches, bug fixes)
- Document why you're overriding
- Test thoroughly after version changes

---

### **2.4 Classic Spring MVC Setup** ⚠️

*Legacy reference - not recommended for new projects*

This section covers traditional Spring MVC configuration for maintaining legacy applications.

#### **Java-Based Configuration**

**Web Application Initializer** (replaces web.xml):

```java
package com.example.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        // Root context configuration (Services, Repositories)
        return new Class[] { RootConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        // Web context configuration (Controllers, Views)
        return new Class[] { WebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        // DispatcherServlet mapping
        return new String[] { "/" };
    }
}
```

**Root Configuration**:

```java
package com.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;

@Configuration
@ComponentScan(
    basePackages = "com.example",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = Controller.class
    )
)
public class RootConfig {
    // Root context beans (Services, Repositories, etc.)
}
```

**Web Configuration** (`@EnableWebMvc`):

```java
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableWebMvc
@ComponentScan("com.example.controller")
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
    }
}
```

#### **XML-Based Configuration** (Legacy)

**web.xml**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

    <!-- Root Context -->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/spring/root-context.xml</param-value>
    </context-param>

    <listener>
        <listener-class>
            org.springframework.web.context.ContextLoaderListener
        </listener-class>
    </listener>

    <!-- DispatcherServlet -->
    <servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>
            org.springframework.web.servlet.DispatcherServlet
        </servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>/WEB-INF/spring/servlet-context.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

    <!-- Character Encoding Filter -->
    <filter>
        <filter-name>encodingFilter</filter-name>
        <filter-class>
            org.springframework.web.filter.CharacterEncodingFilter
        </filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>encodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

</web-app>
```

**servlet-context.xml**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd
           http://www.springframework.org/schema/mvc
           http://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <!-- Enable annotations -->
    <mvc:annotation-driven />

    <!-- Component scanning -->
    <context:component-scan base-package="com.example.controller" />

    <!-- Static resources -->
    <mvc:resources mapping="/resources/**" location="/resources/" />

    <!-- View Resolver -->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/views/" />
        <property name="suffix" value=".jsp" />
    </bean>

</beans>
```

#### **Manual DispatcherServlet Registration**

For complete control without `AbstractAnnotationConfigDispatcherServletInitializer`:

```java
package com.example.config;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class ManualWebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // Create web application context
        AnnotationConfigWebApplicationContext context = 
            new AnnotationConfigWebApplicationContext();
        context.register(WebConfig.class);
        
        // Create and register DispatcherServlet
        DispatcherServlet servlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic registration = 
            servletContext.addServlet("dispatcher", servlet);
        
        registration.setLoadOnStartup(1);
        registration.addMapping("/");
    }
}
```

---

### **2.5 Containerized Development Workflows** 🟡

Modern development often involves containerization for consistency across environments.

#### **Dev Containers for Consistent Environments**

Dev Containers provide consistent development environments using Docker.

**VS Code Dev Container Configuration**:

Create `.devcontainer/devcontainer.json`:

```json
{
  "name": "Spring MVC Dev",
  "dockerComposeFile": "docker-compose.yml",
  "service": "app",
  "workspaceFolder": "/workspace",
  
  "customizations": {
    "vscode": {
      "extensions": [
        "vscjava.vscode-java-pack",
        "vscjava.vscode-spring-boot-dashboard",
        "pivotal.vscode-boot-dev-pack",
        "gabrielbb.vscode-lombok"
      ],
      "settings": {
        "java.configuration.runtimes": [
          {
            "name": "JavaSE-21",
            "path": "/usr/lib/jvm/java-21-openjdk"
          }
        ]
      }
    }
  },
  
  "forwardPorts": [8080, 5432, 6379],
  "postCreateCommand": "./mvnw install",
  "remoteUser": "vscode"
}
```

**Dockerfile for Dev Container**:

`.devcontainer/Dockerfile`:

```dockerfile
FROM mcr.microsoft.com/devcontainers/java:21

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean

# Install additional tools
RUN apt-get install -y postgresql-client redis-tools

WORKDIR /workspace
```

#### **Docker Compose for Local Development**

Complete local environment with app, database, and cache.

**docker-compose.yml**:

```yaml
version: '3.8'

services:
  # Spring Boot Application
  app:
    build:
      context: .
      dockerfile: Dockerfile.dev
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/appdb
      - SPRING_DATASOURCE_USERNAME=appuser
      - SPRING_DATASOURCE_PASSWORD=apppass
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
    volumes:
      - .:/app
      - maven-repo:/root/.m2
    depends_on:
      - db
      - redis
    networks:
      - app-network
    command: ./mvnw spring-boot:run

  # PostgreSQL Database
  db:
    image: postgres:16-alpine
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=appdb
      - POSTGRES_USER=appuser
      - POSTGRES_PASSWORD=apppass
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - app-network

  # Redis Cache
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - app-network

  # PgAdmin (optional - database management UI)
  pgadmin:
    image: dpage/pgadmin4:latest
    ports:
      - "5050:80"
    environment:
      - PGADMIN_DEFAULT_EMAIL=admin@example.com
      - PGADMIN_DEFAULT_PASSWORD=admin
    volumes:
      - pgadmin-data:/var/lib/pgadmin
    networks:
      - app-network

volumes:
  postgres-data:
  redis-data:
  pgadmin-data:
  maven-repo:

networks:
  app-network:
    driver: bridge
```

**Development Dockerfile** (`Dockerfile.dev`):

```dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for layer caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Expose port
EXPOSE 8080

# Use Maven wrapper to run (enables hot reload)
CMD ["./mvnw", "spring-boot:run"]
```

**Docker-specific application properties**:

`application-docker.properties`:

```properties
# Database
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# Redis
spring.data.redis.host=${SPRING_REDIS_HOST}
spring.data.redis.port=${SPRING_REDIS_PORT}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Logging
logging.level.com.example=DEBUG
```

#### **Hot-Reload in Containerized Environments**

**Option 1: Volume Mounting with DevTools**

Mount source code as volume and use Spring Boot DevTools:

```yaml
services:
  app:
    volumes:
      - ./src:/app/src  # Mount source code
      - maven-repo:/root/.m2
```

In `pom.xml`, ensure DevTools is present:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

**Option 2: Debugging with Remote Debug**

Enable remote debugging in Dockerfile:

```dockerfile
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]
```

Expose debug port:
```yaml
ports:
  - "8080:8080"
  - "5005:5005"  # Debug port
```

Attach debugger from IDE to `localhost:5005`.

**Option 3: Docker Build Caching**

Use multi-stage builds to cache dependencies:

```dockerfile
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached)
RUN ./mvnw dependency:go-offline

# Build application
COPY src src
RUN ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Running Docker Compose**:

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Restart app after code changes
docker-compose restart app

# Stop all services
docker-compose down

# Clean volumes (fresh start)
docker-compose down -v
```

**Development Workflow**:

1. Start containers: `docker-compose up -d`
2. Make code changes
3. Changes auto-reload (DevTools) or restart: `docker-compose restart app`
4. Access app: `http://localhost:8080`
5. Access database: `localhost:5432` with `appuser/apppass`
6. Access PgAdmin: `http://localhost:5050`

**Benefits of Containerized Development**:
- **Consistency**: Everyone uses the same environment
- **Isolation**: No local dependency installation
- **Reproducibility**: Environment defined as code
- **Easy onboarding**: New developers run `docker-compose up`
- **Production parity**: Dev environment matches production

---

**Summary of Section 2**:

This section covered:
- ✅ Spring Boot vs Classic MVC comparison and migration path
- ✅ Multiple ways to create Spring Boot projects
- ✅ Understanding starters, directory structure, and main class
- ✅ Configuration with properties/YAML and profiles
- ✅ Maven and Gradle dependency management
- ✅ Legacy classic Spring MVC setup (for reference)
- ✅ Modern containerized development workflows

**Next Section**: Section 3 will dive into Spring MVC Core Components (Controllers, Views, Models) with practical examples and patterns.

---
