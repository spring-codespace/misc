# Spring MVC Web Application — Reference Journal

---

## 🎯 Difficulty Legend

- 🟢 **Core** — Essential for every Spring MVC developer
- 🟡 **Common** — Frequently used in real projects  
- 🔵 **Advanced** — Production optimization & specialized use cases
- ⚠️ **Legacy** — Reference only, not recommended for new projects
- 🛠️ **Hands-On** — Practical project-building sections

---

## 📚 Complete Table of Contents

---

## **1. Introduction to Spring MVC** 🟢

### 1.1 Overview of Spring Framework
- What is Spring Framework and its philosophy (IoC, DI, AOP)
- Core modules of Spring
- Spring ecosystem overview (Boot, Data, Security, Cloud)
- Where Spring MVC fits in the ecosystem

### 1.2 What is Spring MVC?
- MVC pattern explained with diagrams
- Spring MVC vs other frameworks (Struts, JSF, Play)
- **When to use Spring MVC vs Spring WebFlux** (clearly marked as alternative stack)

### 1.3 Architecture of Spring MVC
- Front Controller design pattern
- Core architectural components with detailed diagrams
- Complete request-response lifecycle (step-by-step)
- How components work together

### 1.4 🛠️ Hands-On: Your First Spring MVC Application
- Building "Hello World" with Spring Boot
- Understanding what happens behind the scenes
- Running and testing your first app

---

## **2. Setting Up a Spring MVC Project** 🟢

### 2.1 Spring Boot vs Classic Spring MVC
- Detailed comparison table
- When to use each approach
- Migration path from Classic to Boot

### 2.2 Creating a Spring MVC Application with Spring Boot
- Using Spring Initializr (web, CLI, IDE)
- Understanding starter dependencies (detailed breakdown)
- Project directory structure and conventions
- Main application class (`@SpringBootApplication`)
- Configuration: `application.properties` vs `application.yml`
- Running and hot-reloading with DevTools

### 2.3 Maven and Gradle Dependency Management
- Essential dependencies with explanations
- Spring Boot parent POM and dependency management
- Understanding transitive dependencies
- Gradle alternative (build.gradle.kts)
- Dependency version conflicts and resolution

### 2.4 Classic Spring MVC Setup ⚠️
- Java-based configuration (`@Configuration`, `@EnableWebMvc`)
- XML-based configuration (legacy reference only)
- Manual DispatcherServlet registration

### 2.5 Containerized Development Workflows 🟡
- Dev Containers for consistent environments
- Docker Compose for local development (app + database + Redis)
- Hot-reload in containerized environments

---

## **3. Spring MVC Core Components** 🟢

### 3.1 Controller Layer

#### 3.1.1 Basics
- `@Controller` vs `@RestController` (detailed comparison)
- `@RequestMapping` and HTTP method shortcuts
- Class-level vs method-level mapping
- Path variables, parameters, headers, content types

#### 3.1.2 Handling Requests
- `@PathVariable` — extracting URI segments
- `@RequestParam` — query strings and form parameters (with defaults, optional)
- `@RequestBody` — parsing JSON/XML request bodies
- `@RequestHeader` — accessing HTTP headers
- `@CookieValue` — reading cookies
- `@SessionAttribute` — accessing session attributes
- `@ModelAttribute` — binding form data to objects

#### 3.1.3 Returning Responses
- Returning view names (String)
- `ModelAndView` — combining view and data
- `@ResponseBody` — returning data directly
- `ResponseEntity<T>` — full control (status, headers, body)
- Redirect vs Forward (`redirect:` vs `forward:`)
- Flash attributes (surviving redirects)

#### 3.1.4 Content Negotiation
- `produces` and `consumes` attributes
- Returning JSON vs XML based on Accept header
- Custom message converters (Jackson, JAXB)

#### 3.1.5 Filtering and Interception 🟡
- `@ControllerAdvice` for global model attributes (not just exceptions)
- Request/response logging filters
- Character encoding filters
- CORS filters

### 3.2 View Layer

#### 3.2.1 View Technologies
- **Thymeleaf** 🟢 (primary focus — modern, natural templates)
- **JSP** ⚠️ (brief coverage — legacy reference)
- FreeMarker, Mustache (brief mentions)
- Frontend frameworks (React, Vue, Angular) with Spring MVC backend

#### 3.2.2 ViewResolver Configuration
- `ThymeleafViewResolver` configuration 🟢
- `InternalResourceViewResolver` for JSP ⚠️
- Prefix and suffix configuration
- Template caching strategies (dev vs prod)

#### 3.2.3 Working with Thymeleaf 🟢
- Variable expressions (`${...}`)
- Selection expressions (`*{...}`)
- URL expressions (`@{...}`)
- Conditionals (`th:if`, `th:unless`, `th:switch`)
- Iteration (`th:each` with status variables)
- Form binding (`th:field`, `th:errors`, `th:object`)
- Fragments and layouts (reusable components)
- Thymeleaf + Spring Security integration

#### 3.2.4 Static Resources Management
- Configuring resource handlers
- Serving CSS, JavaScript, images
- Versioning and cache busting
- WebJars for frontend dependencies (Bootstrap, jQuery)

### 3.3 Model Layer

#### 3.3.1 POJOs and Data Transfer Objects (DTOs)
- Creating model classes
- JavaBean conventions
- **DTOs vs Entities** — when to use each, separation of concerns

#### 3.3.2 Data Binding
- How Spring binds HTTP parameters to objects
- Nested object binding (`address.city`)
- Collection binding (`hobbies[0]`, `hobbies[1]`)
- Type conversion (String → Date, String → Enum, custom converters)
- Property editors

#### 3.3.3 Model Attributes and Scope
- Adding data to model (`Model`, `ModelMap`, `ModelAndView`)
- `@ModelAttribute` at method level (pre-populate)
- Session attributes (`@SessionAttributes`)
- **Bean scopes** — `@Scope` annotation (request, session, application, singleton, prototype)
- `WebApplicationContext` vs `ApplicationContext`

### 3.4 Common Design Patterns in Controllers 🟡
- **Builder pattern** for complex request/response objects
- **Factory pattern** for dynamic controller behavior
- **Strategy pattern** for different processing algorithms
- **Template method pattern** — Spring's callback mechanisms
- When to apply each pattern

---

## **4. Data Handling in Spring MVC** 🟢

### 4.1 Form Handling

#### 4.1.1 Basic Form Processing
- Displaying forms (GET)
- Processing submissions (POST)
- Form-backing objects (`@ModelAttribute`)
- **Post-Redirect-Get (PRG) pattern** — preventing duplicate submissions
- Flash attributes for success/error messages

#### 4.1.2 Multi-Step Forms (Wizard Pattern) 🟡
- Using `@SessionAttributes` to maintain state
- Step-by-step validation with groups
- Progress indicators
- Completing wizard and clearing session

#### 4.1.3 File Uploads 🟡
- Configuring multipart support
- `MultipartFile` interface
- Single and multiple file uploads
- File size and type validation
- **Testing file uploads** with `MockMultipartFile`
- Saving to disk vs cloud storage (S3, Azure Blob)
- Streaming large files

#### 4.1.4 File Downloads 🟡
- Serving files as downloads
- Content-Disposition headers
- Streaming large files efficiently
- Range requests for partial downloads

### 4.2 Validation

#### 4.2.1 Bean Validation (JSR-380 / Jakarta Bean Validation 3.0) 🟢
- Standard annotations (`@NotNull`, `@NotBlank`, `@NotEmpty`, `@Size`, `@Email`, `@Pattern`, etc.)
- **`@NotEmpty` vs `@NotBlank` vs `@NotNull`** — key differences
- `@Valid` vs `@Validated`
- `BindingResult` — capturing validation errors
- Displaying errors in views (Thymeleaf error handling)

#### 4.2.2 Custom Validation 🟡
- Creating custom constraint annotations
- Implementing `ConstraintValidator`
- Cross-field validation (password confirmation)
- Custom constraint message interpolation

#### 4.2.3 Validation Groups 🟡
- Using `@Validated` with groups
- Step-specific validation in wizards
- Different validation rules for create vs update

#### 4.2.4 Programmatic Validation
- Implementing `Validator` interface
- Manual validation in controllers
- Combining annotation-based and custom validators

#### 4.2.5 Global Validation Error Handling 🟡
- `@ControllerAdvice` for validation errors
- Customizing validation error messages
- Internationalized error messages
- Returning structured error responses (REST APIs)

### 4.3 Database Integration (Spring Data JPA) 🟢

#### 4.3.1 Configuration
- DataSource configuration (H2, MySQL, PostgreSQL)
- JPA/Hibernate properties
- Entity scanning
- Connection pooling (HikariCP)

#### 4.3.2 Entities and Relationships
- `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
- Column mapping
- Relationships: `@OneToOne`, `@OneToMany`, `@ManyToOne`, `@ManyToMany`
- Fetch types (EAGER vs LAZY)
- Cascade operations
- Orphan removal

#### 4.3.3 Repositories
- `JpaRepository` interface
- Derived query methods (Spring Data magic)
- `@Query` with JPQL
- Native SQL queries
- `@Modifying` for UPDATE/DELETE
- **Pagination, Sorting & Filtering** — unified concept
  - `Pageable`, `Page<T>`, `Sort`
  - Query parameters (page, size, sort)
  - Applied in both MVC and REST

#### 4.3.4 Service Layer and Transactions
- `@Service` stereotype
- `@Transactional` annotation (propagation, isolation, rollback)
- Read-only transactions
- **Avoiding N+1 queries** — JOIN FETCH, entity graphs, batch fetching

#### 4.3.5 DTO Mapping 🟡
- **Why DTOs matter** — API contract vs database schema
- **When NOT to use DTOs** — simple CRUD, internal APIs (explicit guidance)
- Manual mapping (constructors, setters)
- **MapStruct** 🟢 (recommended — compile-time, type-safe)
- ModelMapper (runtime reflection — slower, performance comparison)
- **Where mapping should live** — service layer vs dedicated mapper classes

### 4.4 🛠️ Hands-On: Building a CRUD Application
- Create Product entity with validation
- Build repository and service layers
- Implement full CRUD with Thymeleaf forms
- Add pagination and sorting
- Handle validation errors gracefully

---

## **5. Advanced Spring MVC Concepts** 🟡

### 5.1 Exception Handling and Error Standards

#### 5.1.1 Exception Handling Strategies
- `@ExceptionHandler` at controller level
- `@ControllerAdvice` for global exception handling
- `@RestControllerAdvice` for REST APIs
- Creating custom exception hierarchy
- When to use checked vs unchecked exceptions

#### 5.1.2 Error Response Standards 🟡
- Standardized error format (timestamp, status, message, path, details)
- HTTP status codes and their meanings (complete guide)
- Custom error pages (404, 500, etc.)
- Error handling: MVC (views) vs REST (JSON)
- ProblemDetail (RFC 7807) for REST APIs

#### 5.1.3 Logging and Error Reporting
- Structured logging for errors
- What to log (never log sensitive data)
- Error tracking services (Sentry, Rollbar)

### 5.2 Interceptors and Filters

#### 5.2.1 HandlerInterceptor Interface
- `preHandle()` — before controller
- `postHandle()` — after controller, before view
- `afterCompletion()` — after view rendering (cleanup)

#### 5.2.2 Common Use Cases
- Logging and performance monitoring
- Authentication and authorization checks
- Request/response modification
- Adding common model attributes

#### 5.2.3 Registering Interceptors
- `WebMvcConfigurer.addInterceptors()`
- Path patterns (include/exclude)
- Execution order

#### 5.2.4 Filters vs Interceptors
- **Detailed comparison table**
- When to use Filters (servlet-level concerns)
- When to use Interceptors (Spring-level concerns)

### 5.3 Spring Security Integration

#### 5.3.1 Security Basics
- Authentication vs Authorization
- Spring Security architecture (filter chain)
- Principal and Authentication objects
- **SecurityContextHolder strategies** (MODE_THREADLOCAL, MODE_INHERITABLETHREADLOCAL)

#### 5.3.2 Configuration
- `SecurityFilterChain` bean
- URL-based authorization rules
- Role-based access control (RBAC)
- Method-level security (`@PreAuthorize`, `@Secured`, `@RolesAllowed`)

#### 5.3.3 Form-Based Authentication
- Custom login page
- Logout configuration
- Remember-me functionality
- UserDetailsService implementation
- Password encoding (BCrypt, Argon2)

#### 5.3.4 CSRF Protection
- How CSRF works (with diagrams)
- CSRF tokens in forms
- CSRF in AJAX requests
- When to disable CSRF (stateless REST APIs)

#### 5.3.5 Security in Views
- Thymeleaf Security extras
- Conditional rendering based on roles
- Displaying authenticated user info

#### 5.3.6 OAuth2 and JWT 🟡
- **Spring Security 6.x OAuth2 changes** (explicit note)
- **OAuth2 authentication flows** (authorization code, client credentials, implicit)
- **JWT tokens** — structure, signing, validation
- `spring-boot-starter-oauth2-client` vs `spring-boot-starter-oauth2-resource-server`
- Spring Security OAuth2 Client
- Spring Security Resource Server
- Securing REST APIs with JWT
- Token refresh strategies
- OAuth2 with social login (Google, GitHub, Okta)

### 5.4 Session and State Management

#### 5.4.1 HTTP Sessions
- `HttpSession` API
- Storing and retrieving session attributes
- Session timeout configuration
- Session invalidation (logout)

#### 5.4.2 Cookies
- Reading (`@CookieValue`)
- Setting (`HttpServletResponse.addCookie()`)
- Cookie properties (max-age, path, domain, secure, httpOnly, SameSite)

#### 5.4.3 Session Persistence 🔵
- In-memory sessions (default)
- Database-backed sessions (Spring Session JDBC)
- Redis-backed sessions (distributed systems)
- Sticky sessions vs session replication

### 5.5 Internationalization (i18n) and Localization (l10n) 🟡

#### 5.5.1 Message Sources
- `ResourceBundleMessageSource`
- Message properties files (messages.properties, messages_fr.properties)
- Parameterized messages

#### 5.5.2 Locale Resolution
- `LocaleResolver` implementations (Cookie, Session, Accept-Header)
- `LocaleChangeInterceptor`
- Building a language switcher

#### 5.5.3 Using Messages in Views
- Thymeleaf message expressions (`#{...}`)
- Formatted dates, numbers, currencies per locale

### 5.6 Asynchronous Request Processing (Servlet-Based)

#### 5.6.1 AJAX with Spring MVC
- Returning JSON for AJAX requests
- JavaScript fetch API examples
- Handling CSRF tokens in AJAX
- Error handling in AJAX calls

#### 5.6.2 Async Controllers (Servlet 3.0+)
- `Callable<T>` return types
- `DeferredResult<T>`
- **`CompletableFuture<T>` as alternative** to DeferredResult
- `@Async` methods
- **Timeout handling** for async requests
- Thread pool configuration

#### 5.6.3 Server-Sent Events (SSE)
- Streaming updates to client
- `SseEmitter`
- Long-polling vs SSE vs WebSockets

#### 5.6.4 Virtual Threads (Project Loom) 🟡

> **⚠️ Requires Java 21+ and Spring Boot 3.2+**

##### 5.6.4.1 What are Virtual Threads?
- **Platform threads vs Virtual threads** (detailed comparison with diagrams)
  - Platform threads: 1 thread = 1 OS thread (heavy, limited to ~thousands)
  - Virtual threads: Millions of lightweight threads on few OS threads
- How they solve the thread-per-request bottleneck
- **Memory efficiency** — KB per virtual thread vs MB per platform thread
- Scheduler and carrier threads (how it works internally)

##### 5.6.4.2 Enabling Virtual Threads in Spring Boot 3.2+

**Configuration:**
```properties
# application.properties
spring.threads.virtual.enabled=true
```

**Programmatic configuration:**
```java
@Configuration
public class VirtualThreadConfig {
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
```

- Tomcat/Jetty/Undertow configuration
- Verification (checking if virtual threads are active)
- JVM flags and requirements (Java 21+)

##### 5.6.4.3 Virtual Threads vs Alternatives (Comparison)

| Approach | Concurrency | Memory | Complexity | Best For |
|----------|-------------|--------|------------|----------|
| **Traditional Threads** | ~200-500 | High (MB/thread) | Low | CPU-bound |
| **`@Async` + Thread Pool** | ~Thousands | Medium | Medium | Background tasks |
| **Spring WebFlux** | Very high | Low | High (reactive) | Streaming, backpressure |
| **Virtual Threads** | Millions | Very low (KB/thread) | Low | I/O-bound workloads |

##### 5.6.4.4 When to Use Virtual Threads

**✅ Perfect for:**
- Database calls (blocking JDBC)
- External API calls (REST, SOAP)
- File I/O operations
- High-concurrency web applications with blocking I/O
- Migrating from platform threads with minimal code changes

**❌ NOT suitable for:**
- CPU-bound processing (use ForkJoinPool, parallel streams)
- Already using reactive stack (WebFlux)
- Java versions < 21

##### 5.6.4.5 Code Examples

**Before (Platform Threads):**
```java
@RestController
public class OrderController {
    
    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Long id) {
        // Blocks platform thread during I/O
        Order order = orderService.findById(id);  // DB call
        Payment payment = paymentService.getPayment(order.getId());  // External API
        return order;
    }
}
// Limited to ~200-500 concurrent requests (thread pool size)
```

**After (Virtual Threads - same code!):**
```java
@RestController
public class OrderController {
    
    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Long id) {
        // Now runs on virtual thread - can handle millions of requests
        Order order = orderService.findById(id);  // DB call
        Payment payment = paymentService.getPayment(order.getId());  // External API
        return order;
    }
}
// With spring.threads.virtual.enabled=true
// Can handle 100,000+ concurrent requests easily
```

##### 5.6.4.6 Best Practices & Pitfalls

**Best Practices:**
- Enable for I/O-bound Spring Boot apps (nearly free performance boost)
- Keep existing blocking code (no need to refactor to reactive)
- Monitor with JFR (Java Flight Recorder) and JDK Mission Control

**Critical Pitfalls to Avoid:**

1. **Avoid `synchronized` blocks** (pins virtual thread to carrier thread)
   ```java
   // ❌ BAD - pins virtual thread
   public synchronized void processOrder(Order order) { ... }
   
   // ✅ GOOD - use ReentrantLock instead
   private final ReentrantLock lock = new ReentrantLock();
   public void processOrder(Order order) {
       lock.lock();
       try {
           // process
       } finally {
           lock.unlock();
       }
   }
   ```

2. **ThreadLocal considerations** (virtual threads are short-lived)
   - Avoid ThreadLocal for caching (use scoped values in Java 21+)
   - Clean up ThreadLocals explicitly

3. **Connection pool sizing adjustments**
   ```properties
   # Before (platform threads): small pool
   spring.datasource.hikari.maximum-pool-size=10
   
   # After (virtual threads): much larger pool
   spring.datasource.hikari.maximum-pool-size=100
   # Virtual threads can handle many more concurrent DB connections
   ```

##### 5.6.4.7 Performance Benchmarks

**Typical improvements (I/O-bound workloads):**
- **Throughput:** 5-10x increase for blocking I/O operations
- **Latency:** P99 latency reduced by 60-80%
- **Resource usage:** Handle 10,000+ requests with 2GB heap (vs 200 requests before)

**When NOT to expect gains:**
- CPU-bound workloads (no improvement)
- Already using reactive stack (WebFlux)
- Very low concurrency (<100 requests/sec)

> **Note:** For true reactive (non-blocking) programming, see Section 10.2 (WebFlux — alternative stack). Virtual threads are a **middle ground** — async benefits with blocking code simplicity.

### 5.7 Spring Profiles 🟢

#### 5.7.1 Profile Configuration
- `@Profile` annotation
- Profile-specific properties files
- Activating profiles (CLI, env var, IDE)

#### 5.7.2 Environment-Specific Beans
- Dev vs Test vs Prod configurations
- Mock services for development
- Production-grade config (pooling, caching)

### 5.8 CORS Configuration 🟡

#### 5.8.1 Understanding CORS
- Same-origin policy
- CORS headers explained
- Preflight requests (OPTIONS)

#### 5.8.2 CORS in Spring MVC
- `@CrossOrigin` annotation (controller/method level)
- Global CORS configuration (`WebMvcConfigurer.addCorsMappings()`)
- Allowed origins, methods, headers
- Credentials and security considerations

---

## **6. Testing Spring MVC Applications** 🟢

> **⚠️ Moved before REST — testing concepts apply to both MVC and REST**

### 6.1 Testing Strategy Overview
- Unit vs Integration vs E2E tests
- Test pyramid concept
- What to test (controllers, services, repositories)
- **Testing anti-patterns to avoid** (testing implementation, not behavior)

### 6.2 Unit Testing Controllers

#### 6.2.1 @WebMvcTest
- Testing only the web layer
- MockMvc setup
- Mocking service dependencies with `@MockBean`
- **`@AutoConfigureMockMvc(addFilters = false)`** — testing without security filters

#### 6.2.2 Mocking and Stubbing (Mockito)
- `@Mock`, `@MockBean`
- `when().thenReturn()` stubbing
- `verify()` for interaction verification
- Argument matchers
- **Best practices for mocking** (when to mock, when not to)
- **Avoiding over-mocking** (integration tests when needed)

#### 6.2.3 Testing Request Mappings
- `MockMvc.perform()` for GET, POST, PUT, DELETE
- Verifying HTTP status codes
- Asserting view names
- Checking model attributes

#### 6.2.4 Testing Form Validation
- Sending invalid data
- Checking for validation errors
- Verifying BindingResult

#### 6.2.5 Testing JSON APIs
- Content type assertions
- JSON path assertions (`$.fieldName`)
- Verifying response structure

### 6.3 Integration Testing

#### 6.3.1 @SpringBootTest
- Loading full application context
- Testing with real database (or in-memory H2)
- `@AutoConfigureMockMvc` for MockMvc

#### 6.3.2 Testing Database Interactions
- `@DataJpaTest` for repository tests
- Test data setup (`@BeforeEach`, `@Sql`)
- Transactional tests (auto-rollback)
- **Cleaning up test data** (best practices)

#### 6.3.3 End-to-End Testing
- `TestRestTemplate` for real HTTP calls
- Testing with embedded server
- Testing security (authentication, authorization)

### 6.4 Test Configuration

#### 6.4.1 Test Profiles
- `@ActiveProfiles("test")`
- Test-specific properties
- Using H2 for tests

#### 6.4.2 Test Data Management
- `@Sql` scripts
- DBUnit, Testcontainers
- Test fixtures and builders (builder pattern)

#### 6.4.3 Testing with Security
- `@WithMockUser` annotation
- `@WithUserDetails`
- `@WithAnonymousUser`
- `@WithSecurityContext` for custom security contexts
- Testing authorization rules

---

## **7. RESTful Web Services with Spring MVC** 🟢

### 7.1 REST Fundamentals

#### 7.1.1 Principles of REST
- Stateless architecture
- Resource-based URLs
- HTTP methods (GET, POST, PUT, PATCH, DELETE)
- Idempotency and safety
- HATEOAS 🔵 (optional advanced concept)

#### 7.1.2 RESTful URL Design Best Practices
- Collection vs single resource
- Nested resources
- Query parameters for filtering, sorting, pagination
- Consistent naming conventions (nouns, not verbs)
- **Anti-patterns to avoid** (verbs in URLs, inconsistent naming)

#### 7.1.3 API Versioning Strategies 🟡
- **URL versioning** (`/api/v1/products`) — pros, cons, examples
- **Header versioning** (`X-API-Version: 1`) — pros, cons, examples
- **Media type versioning** (`application/vnd.myapp.v1+json`) — pros, cons
- **Trade-offs and recommendations**
- Deprecation strategies (headers, documentation)
- Backward compatibility techniques

### 7.2 Building REST APIs

#### 7.2.1 @RestController
- Difference from `@Controller`
- JSON serialization with Jackson
- XML support (JAXB, optional)

#### 7.2.2 CRUD Operations
- GET all resources (with pagination)
- GET single resource by ID
- POST to create (201 Created + Location header)
- PUT for full update
- **PATCH for partial update** — implementation strategies:
  - JSON Merge Patch (RFC 7396)
  - JSON Patch (RFC 6902)
  - `@JsonMerge` annotation
- DELETE to remove (204 No Content)

#### 7.2.3 HTTP Status Codes (Complete Guide)
- **2xx Success** (200 OK, 201 Created, 202 Accepted, 204 No Content, 206 Partial Content)
- **3xx Redirection** (301, 302, 304 Not Modified, 307, 308)
- **4xx Client Errors** (400, 401, 403, 404, 405, 409, 415, 422, 429)
- **5xx Server Errors** (500, 502, 503, 504)
- **When to use each status code** (decision tree)

#### 7.2.4 ResponseEntity
- Full control (status, headers, body)
- Builder pattern
- Setting custom headers

#### 7.2.5 Pagination, Sorting & Filtering (Applied)
- Using `Pageable` and `Page<T>` from Section 4.3.3
- HATEOAS links for pagination
- Filtering with `@RequestParam` or Specifications

#### 7.2.6 Content Negotiation
- Accepting multiple formats (JSON, XML)
- Custom message converters

### 7.3 API Documentation 🟡

#### 7.3.1 OpenAPI/Swagger
- SpringDoc OpenAPI (recommended)
- Auto-generated documentation
- Swagger UI
- Customizing with annotations (`@Operation`, `@Schema`)

#### 7.3.2 API-First Development 🔵
- Designing OpenAPI spec first
- Code generation from spec (OpenAPI Generator)
- Contract-first vs code-first
- **Benefits of API-first** approach

### 7.4 Consuming External APIs

#### 7.4.1 WebClient (Recommended) 🟢
- **Primary REST client for Spring 6+**
- Non-blocking HTTP client
- Builder pattern
- Mono and Flux (reactive types)
- **Using in blocking context** (`.block()`, when appropriate)
- GET, POST, PUT, DELETE requests
- Setting headers (Authorization, Content-Type)
- Error handling (4xx, 5xx)
- Timeouts and retries
- Request/response logging

#### 7.4.2 RestTemplate (Legacy) ⚠️
- **Maintenance mode** — not recommended for new projects
- Brief coverage for legacy codebases
- **Migration guide: RestTemplate → WebClient**

#### 7.4.3 Advanced API Client Patterns 🔵
- Circuit breakers (Resilience4j)
- Retries with exponential backoff
- Bulkhead pattern
- Fallback mechanisms

### 7.5 🛠️ Hands-On: Building a REST API
- Convert CRUD app to REST API
- Implement proper HTTP status codes
- Add pagination and filtering
- Document with SpringDoc
- Write integration tests
- Test with Postman/curl

---

## **8. Best Practices and Performance Optimization** 🟡

### 8.1 Code Organization and Architecture

#### 8.1.1 Layered Architecture
- Controller → Service → Repository → Entity
- Separation of concerns
- **Package structure strategies** (by layer vs by feature)

#### 8.1.2 Dependency Injection Best Practices
- **Constructor injection** (recommended)
- Avoiding field injection (testability, immutability)
- Circular dependency prevention (redesign, `@Lazy`)

### 8.2 Performance Optimization

#### 8.2.1 Database Query Optimization
- Avoiding N+1 queries (revisited with solutions)
- Using JOIN FETCH
- Pagination for large datasets
- Query result caching
- Database indexes
- **Performance anti-patterns** (EAGER fetching, chatty APIs)

#### 8.2.2 Application-Level Caching 🟡
- `@EnableCaching`
- `@Cacheable`, `@CachePut`, `@CacheEvict`
- Cache providers (Caffeine, Ehcache, Redis)
- Cache configuration (TTL, eviction policies)
- Cache key strategies

#### 8.2.3 HTTP Caching 🔵
- ETag and Last-Modified headers
- Cache-Control headers
- Conditional requests (If-None-Match, If-Modified-Since)

#### 8.2.4 Asynchronous Processing
- `@Async` for background tasks
- Thread pool configuration
- When to use async (email, reports, notifications)

#### 8.2.5 Connection Pooling
- HikariCP (default in Spring Boot)
- Pool size configuration (formula: connections = ((core_count * 2) + effective_spindle_count))
- **Adjustments for virtual threads** (much larger pools when using virtual threads)
- Connection timeout settings

#### 8.2.6 Profiling and Optimization Tools 🔵
- **VisualVM** for heap dumps and thread analysis
- **JProfiler** for production profiling
- **Spring Boot DevTools** for development (LiveReload)
- **Actuator metrics** for monitoring
- **JMH (Java Microbenchmark Harness)** for micro-benchmarks
- **Java Flight Recorder (JFR)** for virtual threads monitoring

### 8.3 Security Best Practices

#### 8.3.1 Input Validation
- Always validate on server-side
- Sanitizing inputs (XSS prevention)
- SQL injection prevention (parameterized queries)
- **File upload validation** (type, size, content inspection)

#### 8.3.2 Authentication and Authorization
- Never store passwords in plain text
- Use BCrypt or Argon2
- Session timeout configuration
- Principle of least privilege

#### 8.3.3 HTTPS and Transport Security
- Always use HTTPS in production
- Redirect HTTP to HTTPS
- HSTS headers
- Certificate management

#### 8.3.4 Common Vulnerabilities (OWASP Top 10)
- SQL Injection
- XSS (Cross-Site Scripting)
- CSRF
- Clickjacking (X-Frame-Options, CSP)
- Insecure deserialization
- Security misconfiguration
- **Hardcoded secrets** (never commit to Git)

### 8.4 Logging and Monitoring

#### 8.4.1 Logging Best Practices
- Log levels (TRACE, DEBUG, INFO, WARN, ERROR)
- Structured logging (JSON format)
- MDC (Mapped Diagnostic Context)
- What NOT to log (passwords, PII, credit cards)

#### 8.4.2 Application Monitoring
- Spring Boot Actuator (detailed coverage)
- Health checks (custom health indicators)
- Metrics (JVM, HTTP, custom metrics)
- Prometheus and Grafana integration

#### 8.4.3 APM and Observability 🔵
- New Relic, Datadog, Dynatrace
- Distributed tracing (Spring Cloud Sleuth, Zipkin, Jaeger)
- Log aggregation (ELK stack, Splunk)

---

## **9. Production Readiness & Deployment** 🟡

### 9.1 Build and Packaging

#### 9.1.1 JAR vs WAR
- When to use each
- Spring Boot executable JAR (recommended)
- Traditional WAR for servlet containers

#### 9.1.2 Maven Build
- `mvn clean package`
- Running tests during build
- Skipping tests (`-DskipTests`)
- Build profiles (dev, test, prod)
- Multi-module Maven projects

#### 9.1.3 Gradle Build (Alternative)
- `./gradlew build`
- Kotlin DSL (build.gradle.kts)
- Task customization

### 9.2 Deployment Strategies

#### 9.2.1 Traditional Server Deployment
- Deploying JAR on Linux server
- Creating systemd service
- Process management (start, stop, restart)
- Log rotation

#### 9.2.2 Servlet Container Deployment ⚠️
- Deploying WAR to Tomcat
- Deploying to JBoss/WildFly
- Container configuration

#### 9.2.3 Cloud Deployment 🟡
- **AWS** (Elastic Beanstalk, EC2, ECS, Fargate, Lambda)
- **Google Cloud** (App Engine, Compute Engine, Cloud Run)
- **Azure** (App Service, Container Instances, AKS)
- **Heroku** (quick deploy for demos)

### 9.3 Containerization with Docker 🟡

#### 9.3.1 Creating Dockerfiles
- Multi-stage builds (optimization)
- Using official Java base images
- Layer optimization for faster builds
- Security best practices (non-root user)

#### 9.3.2 Docker Compose
- Multi-container applications (app + DB + Redis + Nginx)
- Environment variable management
- Volume mounting (data persistence)
- Networks and service discovery

#### 9.3.3 Container Orchestration 🔵
- **Kubernetes basics** (Pods, Deployments, Services, Ingress)
- **Helm charts** for Spring Boot apps
- ConfigMaps and Secrets
- Health probes (liveness, readiness, startup)
- **Zero-downtime deployments** (rolling updates, blue-green)

### 9.4 Configuration Management

#### 9.4.1 Externalized Configuration
- Environment variables (12-factor app)
- Config files outside the JAR
- Spring Cloud Config Server 🔵

#### 9.4.2 Secrets Management 🔵
- Never commit secrets to Git
- Environment variables for sensitive data
- **HashiCorp Vault**
- **AWS Secrets Manager**
- **Azure Key Vault**

### 9.5 CI/CD Pipelines 🟡

#### 9.5.1 Continuous Integration
- **GitHub Actions** workflow (detailed example)
- **GitLab CI** pipeline
- **Jenkins** pipeline (Jenkinsfile)

#### 9.5.2 Automated Testing in CI
- Running unit and integration tests
- Code coverage reports (JaCoCo)
- Quality gates (SonarQube)

#### 9.5.3 Continuous Deployment
- Blue-Green deployments
- Canary deployments
- Rolling updates
- Rollback strategies
- **Database migration strategies** during deployment (Flyway, Liquibase)

### 9.6 SSL/TLS Configuration

#### 9.6.1 Enabling HTTPS
- Generating certificates (self-signed for dev)
- Let's Encrypt for production
- Configuring SSL in Spring Boot

#### 9.6.2 Reverse Proxy Setup
- Nginx or Apache as reverse proxy
- SSL termination at proxy
- Load balancing

### 9.7 🛠️ Hands-On: Containerize and Deploy
- Dockerize the CRUD + REST app
- Create docker-compose.yml with PostgreSQL
- Deploy to cloud (AWS/Azure/GCP)
- Set up CI/CD pipeline
- **Implement zero-downtime deployment**

---

## **10. Advanced & Optional Topics** 🔵

> **Explicitly marked as optional to reduce intimidation**

### 10.1 WebSockets
- Real-time bidirectional communication
- STOMP protocol
- Building a chat application

### 10.2 Spring WebFlux (Reactive Programming)

> **⚠️ Alternative stack, not extension of MVC**  
> **Note:** If building a new application with extreme concurrency needs, consider starting with WebFlux instead of migrating from MVC later.  
> **However:** With Java 21 virtual threads, many use cases that previously required WebFlux can now be handled with Spring MVC + virtual threads with simpler, imperative code.

- When to use reactive (high concurrency, backpressure handling, streaming)
- **WebFlux vs Virtual Threads** — updated decision matrix
- Project Reactor (Mono, Flux)
- Functional endpoints
- Reactive database drivers (R2DBC)
- **Performance trade-offs vs Spring MVC with virtual threads**
- **When NOT to use WebFlux** (team expertise, ecosystem maturity, virtual threads sufficient)

### 10.3 GraphQL with Spring 🟡

#### 10.3.1 GraphQL Basics
- GraphQL vs REST (strengths, weaknesses)
- Schema definition language (SDL)
- Queries, mutations, subscriptions

#### 10.3.2 Spring for GraphQL
- `spring-boot-starter-graphql`
- Defining schema
- Resolvers and data fetchers
- Error handling
- Testing GraphQL APIs

#### 10.3.3 GraphQL Best Practices
- N+1 query problem and DataLoader
- Pagination with connections
- Authentication and authorization
- Rate limiting

### 10.4 Microservices with Spring Boot 🔵
- Breaking monoliths into microservices
- Spring Cloud (Config, Discovery, Gateway, Circuit Breaker)
- Service-to-service communication (REST, messaging)
- Distributed tracing
- API Gateway patterns

### 10.5 Serverless Spring 🔵
- **Spring Cloud Function**
- AWS Lambda with Spring Boot
- Azure Functions with Spring
- Cold start optimization
- Event-driven architectures

### 10.6 Event-Driven Architecture 🔵
- Spring Events (application events)
- Message brokers (RabbitMQ, Kafka)
- Asynchronous processing patterns
- Event sourcing and CQRS (brief intro)

### 10.7 API Gateway and Service Mesh 🔵
- Spring Cloud Gateway
- Istio, Linkerd
- Service mesh patterns

### 10.8 Observability (3 Pillars) 🔵
- Logging, Metrics, Tracing
- OpenTelemetry
- ELK stack (Elasticsearch, Logstash, Kibana)

---

## **11. References and Resources**

### 11.1 Official Documentation
- Spring Framework Reference
- Spring Boot Documentation
- Spring Data JPA
- Spring Security
- Thymeleaf Documentation

### 11.2 Books
- *Spring in Action* (Craig Walls) — comprehensive reference
- *Spring Boot in Practice*
- *Pro Spring 6*
- *Cloud Native Java* (microservices focus)

### 11.3 Online Courses
- Spring Academy (official)
- Udemy (Chad Darby, in28minutes)
- Pluralsight
- LinkedIn Learning

### 11.4 Blogs and Community
- **Baeldung** — best for deep dives
- Spring Blog (official)
- DZone
- Reddit: r/java, r/SpringFramework
- Stack Overflow

### 11.5 Sample Projects and Repositories
- Spring PetClinic (official sample)
- Real World App examples
- Awesome Spring (curated list)

---

## **12. Appendices**

### Appendix A: Quick Reference Cheat Sheet
- Common annotations at a glance
- HTTP status codes decision tree
- Spring Data JPA query methods
- Thymeleaf syntax reference
- Maven/Gradle commands

### Appendix B: Troubleshooting Common Issues
- Bean creation errors (circular dependencies, missing beans)
- Circular dependency problems (solutions)
- Database connection issues (driver, URL, credentials)
- View resolution errors (prefix/suffix, templates not found)
- CORS problems (missing headers, preflight failures)
- Form validation not working (BindingResult position)
- "Whitelabel Error Page" fixes (custom error pages)
- **Virtual threads not activating** (Java version, configuration)
- **Virtual thread pinning issues** (synchronized blocks)

### Appendix C: Migration Guides
- Migrating from Spring 5 to Spring 6
- Migrating from Spring Boot 2 to Spring Boot 3
- Migrating from Java 11 to Java 17/21
- Migrating from JSP to Thymeleaf
- Migrating from RestTemplate to WebClient
- **Migrating to Virtual Threads** (configuration, pitfalls)

### Appendix D: Code Snippets Library
- Configuration class templates
- Custom validators
- Exception handlers
- Test templates (MockMvc, @SpringBootTest)
- Dockerfile templates
- docker-compose.yml templates
- **Virtual threads configuration snippets**

### Appendix E: Spring Boot Starters Reference
- Complete list of official starters
- Third-party starters
- When to use each starter

### Appendix F: Common Pitfalls and Anti-patterns

#### Controller Anti-patterns
- **Fat controllers** doing business logic (solution: move to service layer)
- **Returning entities directly** from REST controllers (security risk, serialization issues)
- **Not using PRG pattern** for form submissions (duplicate submissions)
- **Hardcoding URLs** in templates (use `@{...}` expressions)

#### Performance Anti-patterns
- **EAGER fetching everything** (use LAZY, fetch only what's needed)
- **Not paginating large results** (OutOfMemoryError risk)
- **Chatty APIs** (N+1 database queries, multiple API calls)
- **Synchronous processing** for long-running tasks (use `@Async`)
- **Not using virtual threads** for I/O-bound apps (free performance boost on Java 21+)

#### Security Pitfalls
- **Hardcoded secrets** in properties files (use environment variables, vaults)
- **Not validating file upload types** (content sniffing, malicious files)
- **Missing CSRF protection** for state-changing operations
- **Exposing stack traces** to users (information leakage)

#### Testing Pitfalls
- **Testing implementation** instead of behavior (brittle tests)
- **Not cleaning up test data** (test pollution)
- **Mocking too much** (use integration tests when needed)
- **No assertions** (tests that don't verify anything)

#### Virtual Threads Pitfalls
- **Using `synchronized` blocks** (pins virtual thread to carrier)
- **Not adjusting connection pool sizes** (underutilizing virtual threads)
- **Using ThreadLocal for caching** (memory leaks with many virtual threads)

---
