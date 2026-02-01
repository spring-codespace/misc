# THYMELEAF
## Template Engine
### Comprehensive Reference Journal for Spring Boot Applications

*Covering: Syntax & Expressions · Standard Dialects · Text Processing*
*Iteration & Conditionals · Fragments & Layouts · Spring Integration*
*Spring Security · Form Handling · Internationalization · And More*

*January 2026*

---

## Table of Contents

1. [Introduction to Thymeleaf](#1-introduction-to-thymeleaf)
   - 1.1 [Key Features](#11-key-features)
   - 1.2 [Thymeleaf vs. JSP](#12-thymeleaf-vs-jsp)
2. [Project Setup](#2-project-setup)
   - 2.1 [Maven Dependency](#21-maven-dependency)
   - 2.2 [Gradle Dependency](#22-gradle-dependency)
   - 2.3 [Default Configuration](#23-default-configuration)
   - 2.4 [Advanced Configuration & Customization](#24-advanced-configuration--customization)
   - 2.5 [Basic Controller](#25-basic-controller)
   - 2.6 [Basic Template](#26-basic-template)
3. [Thymeleaf Syntax & Expressions](#3-thymeleaf-syntax--expressions)
   - 3.1 [Expression Types Overview](#31-expression-types-overview)
   - 3.2 [Variable Expressions](#32-variable-expressions-)
   - 3.3 [Message Expressions](#33-message-expressions-)
   - 3.4 [Link Expressions](#34-link-expressions-)
   - 3.5 [Selection Variable Expressions](#35-selection-variable-expressions-)
   - 3.6 [Fragment Expressions](#36-fragment-expressions-)
   - 3.7 [Literals & Operators](#37-literals--operators)
4. [Standard Dialect — Attribute Processors](#4-standard-dialect--attribute-processors)
   - 4.1 [Text & Output Processors](#41-text--output-processors)
   - 4.2 [Attribute Modification Processors](#42-attribute-modification-processors)
5. [Text Processing & Inline Expressions](#5-text-processing--inline-expressions)
   - 5.1 [Inline Variable Expressions](#51-inline-variable-expressions)
   - 5.2 [Inline Message Expressions](#52-inline-message-expressions)
   - 5.3 [Disabling Inline Processing](#53-disabling-inline-processing)
   - 5.4 [JavaScript Inline](#54-javascript-inline)
6. [Conditionals & Boolean Logic](#6-conditionals--boolean-logic)
   - 6.1 [th:if — Conditional Rendering](#61-thif--conditional-rendering)
   - 6.2 [th:unless — Negated Conditional](#62-thunless--negated-conditional)
   - 6.3 [th:if / th:unless Truth Rules](#63-thif--thunless-truth-rules)
   - 6.4 [th:switch / th:case — Switch Statements](#64-thswitch--thcase--switch-statements)
7. [Iteration](#7-iteration)
   - 7.1 [Basic Iteration](#71-basic-iteration)
   - 7.2 [Iteration Status Variable](#72-iteration-status-variable)
   - 7.3 [Iterating Over Maps](#73-iterating-over-maps)
   - 7.4 [Iterating Over Arrays & Ranges](#74-iterating-over-arrays--ranges)
   - 7.5 [Empty State Handling](#75-empty-state-handling)
8. [Tag & Element Manipulation](#8-tag--element-manipulation)
   - 8.1 [th:remove — Removing Elements](#81-thremove--removing-elements)
   - 8.2 [th:replace & th:insert](#82-threplace--thinsert)
   - 8.3 [th:with — Local Variable Declaration](#83-thwith--local-variable-declaration)
   - 8.4 [th:fragment — Defining Fragments](#84-thfragment--defining-fragments)
   - 8.5 [th:block — Virtual Wrapper Element](#85-thblock--virtual-wrapper-element)
9. [Fragments & Layout Composition](#9-fragments--layout-composition)
   - 9.1 [Defining Fragments](#91-defining-fragments)
   - 9.2 [Including Fragments](#92-including-fragments)
   - 9.3 [Layout Pattern — Manual Approach](#93-layout-pattern--manual-approach)
   - 9.4 [Thymeleaf Layout Dialect](#94-thymeleaf-layout-dialect)
10. [Form Handling](#10-form-handling)
    - 10.1 [Basic Form Structure](#101-basic-form-structure)
    - 10.2 [How th:field Works](#102-how-thfield-works)
    - 10.3 [Select, Radio, and Checkbox](#103-select-radio-and-checkbox)
    - 10.4 [Validation Error Display](#104-validation-error-display)
11. [Spring Security Integration](#11-spring-security-integration)
    - 11.1 [Adding the Dependency](#111-adding-the-dependency)
    - 11.2 [Template Namespace](#112-template-namespace)
    - 11.3 [sec:authorize — Role-Based Rendering](#113-secauthorize--role-based-rendering)
    - 11.4 [sec:authorize-url — URL-Based Authorization](#114-secauthorize-url--url-based-authorization)
    - 11.5 [sec:authentication — Accessing the Authenticated User](#115-secauthentication--accessing-the-authenticated-user)
    - 11.6 [CSRF Token in Forms](#116-csrf-token-in-forms)
    - 11.7 [Login & Logout Forms](#117-login--logout-forms)
12. [Utility Objects](#12-utility-objects)
    - 12.1 [Overview of Utility Objects](#121-overview-of-utility-objects)
    - 12.2 [#strings — String Utilities](#122-strings--string-utilities)
    - 12.3 [#numbers — Number Formatting](#123-numbers--number-formatting)
    - 12.4 [#dates & #temporals — Date/Time Formatting](#124-dates--temporals--datetime-formatting)
    - 12.5 [#lists — Collection Utilities](#125-lists--collection-utilities)
    - 12.6 [#sets — Set Utilities](#126-sets--set-utilities)
    - 12.7 [#maps — Map Utilities](#127-maps--map-utilities)
    - 12.8 [#objects — General Object Utilities](#128-objects--general-object-utilities)
    - 12.9 [#ids — ID Generation for Fragments](#129-ids--id-generation-for-fragments)
13. [Internationalization (i18n)](#13-internationalization-i18n)
    - 13.1 [Resource Bundle Structure](#131-resource-bundle-structure)
    - 13.2 [Message Files](#132-message-files)
    - 13.3 [Using Messages in Templates](#133-using-messages-in-templates)
    - 13.4 [Locale Switching](#134-locale-switching)
14. [Spring MVC Integration](#14-spring-mvc-integration)
    - 14.1 [Spring Expression Language (SpEL)](#141-spring-expression-language-spel)
    - 14.2 [Spring Model Attributes](#142-spring-model-attributes)
    - 14.3 [Flash Attributes (Redirect Messages)](#143-flash-attributes-redirect-messages)
    - 14.4 [@SessionAttributes](#144-sessionattributes)
    - 14.5 [Static Resources](#145-static-resources)
15. [Advanced Topics](#15-advanced-topics)
    - 15.1 [Thymeleaf Configuration Extensions](#151-thymeleaf-configuration-extensions)
    - 15.2 [Testing Thymeleaf Templates](#152-testing-thymeleaf-templates)
    - 15.3 [Thymeleaf and WebJars Integration](#153-thymeleaf-and-webjars-integration)
    - 15.4 [Thymeleaf for Email Templates](#154-thymeleaf-for-email-templates)
    - 15.5 [Conditional Comments for Legacy Browsers](#155-conditional-comments-for-legacy-browsers)
    - 15.6 [Preprocessing with th:pre](#156-preprocessing-with-thpre)
    - 15.7 [th:classappend & th:styleappend](#157-thclassappend--thstyleappend)
    - 15.8 [Literal Substitution](#158-literal-substitution)
    - 15.9 [Conditional Attributes — th:attrappend](#159-conditional-attributes--thattrappend)
    - 15.10 [Decoupled Template Logic](#1510-decoupled-template-logic-structured-data-files)
    - 15.11 [Performance Considerations](#1511-performance-considerations)
    - 15.12 [Error Handling & Fallbacks](#1512-error-handling--fallbacks)
    - 15.13 [Accessibility Considerations](#1513-accessibility-considerations)
    - 15.14 [Common Pitfalls & Debugging](#1514-common-pitfalls--debugging)
16. [Quick Reference Card](#16-quick-reference-card)
    - 16.1 [Most Common Processors](#161-most-common-processors)
    - 16.2 [Expression Cheat Sheet](#162-expression-cheat-sheet)
    - 16.3 [Spring Security sec: Attributes](#163-spring-security-sec-attributes)
17. [Glossary](#17-glossary)
18. [Best Practices](#18-best-practices)
19. [Troubleshooting FAQ](#19-troubleshooting-faq)

---

## 1. Introduction to Thymeleaf

Thymeleaf is a modern, server-side Java template engine designed to produce well-formed HTML, XML, JavaScript, CSS, and plain text documents. It is the recommended templating solution for Spring Boot web applications and is designed to seamlessly replace JSP in the Spring MVC architecture.

### 1.1 Key Features

- **Natural Templating** — HTML files are valid and can be opened directly in a browser, making design and development seamless.
- **Spring MVC Integration** — First-class support for Spring MVC, including binding, validation, and security.
- **Standard Dialect** — A rich set of built-in processors (`th:text`, `th:each`, `th:if`, etc.) that cover the vast majority of use cases.
- **Extensibility** — Custom dialects can be written to extend Thymeleaf with domain-specific processors.
- Supports HTML5, XML, XHTML, XHTML5, Legacy HTML5, and plain text output modes.

### 1.2 Thymeleaf vs. JSP

| Feature | Thymeleaf | JSP |
|---|---|---|
| **Natural HTML** | Templates are valid HTML; renderable in browsers | Not renderable without a container |
| **Compilation** | No compilation required | Compiled to servlets |
| **XML/HTML Validity** | Enforces well-formed templates | Allows loose syntax |
| **Spring Boot Support** | Default and recommended view | Limited support |
| **Prototyping** | Designers can work directly with static HTML | Requires container for preview |
| **Learning Curve** | Gentle, HTML-based | Steeper, Java-centric |

> 💡 **Note:** Thymeleaf 3.x is the current major version used with Spring Boot 2.x and 3.x. Always ensure your project uses a compatible version.

---

## 2. Project Setup

Adding Thymeleaf to a Spring Boot project is straightforward. Spring Boot auto-configures Thymeleaf when the starter dependency is on the classpath.

### 2.1 Maven Dependency

**pom.xml — Thymeleaf Starter**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 2.2 Gradle Dependency

**build.gradle**

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
}
```

### 2.3 Default Configuration

By default, Spring Boot looks for Thymeleaf templates in `src/main/resources/templates/` with a `.html` suffix. The following properties can be customized in `application.properties`:

**application.properties**

```properties
# Template location (default)
spring.thymeleaf.prefix=classpath:/templates/
# File suffix (default)
spring.thymeleaf.suffix=.html
# Enable caching in production (disable during dev)
spring.thymeleaf.cache=false
# Check that template files exist before rendering
spring.thymeleaf.check-template-location=true
# Template mode (HTML is the default)
spring.thymeleaf.mode=HTML
# Encoding (default UTF-8)
spring.thymeleaf.encoding=UTF-8
```

### 2.4 Advanced Configuration & Customization

**Custom Template Resolver**

```java
@Configuration
public class ThymeleafConfig {

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // Disable for development
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.setEnableSpringELCompiler(true);
        // Add custom dialects
        engine.addDialect(new SpringSecurityDialect());
        engine.addDialect(new LayoutDialect());
        return engine;
    }
}
```

**Performance Configuration for Production**

```properties
# application-prod.properties
spring.thymeleaf.cache=true
spring.thymeleaf.cache.ttl=3600  # Cache TTL in seconds
spring.thymeleaf.servlet.content-type=text/html
```

### 2.5 Basic Controller

**HomeController.java**

```java
@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("name", "World");
        model.addAttribute("currentDate", LocalDate.now());
        return "index";  // Resolves to /templates/index.html
    }
}
```

### 2.6 Basic Template

**templates/index.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="'Hello, ' + ${name}">Page Title</title>
    <meta charset="UTF-8">
</head>
<body>
    <h1 th:text="'Hello, ' + ${name}">Hello, Placeholder</h1>
    <p>Welcome to Thymeleaf!</p>
    <p>Today is: <span th:text="${currentDate}">2026-01-31</span></p>
</body>
</html>
```

---

## 3. Thymeleaf Syntax & Expressions

All Thymeleaf attributes are prefixed with `th:`. The core expression types are used within these attributes to dynamically bind data, evaluate logic, and produce output.

### 3.1 Expression Types Overview

| Expression | Description |
|---|---|
| `${...}` | Variable Expressions — Access model attributes, bean properties, and utility objects. |
| `#{...}` | Message Expressions — Retrieve internationalization (i18n) messages from resource bundles. |
| `@{...}` | Link Expressions — Generate URLs with context-path and query parameter support. |
| `~{...}` | Fragment Expressions — Reference external or inline fragments for layout composition. |
| `*{...}` | Selection Variable Expressions — Used with `th:object` to select a sub-context. |
| `'...'` | Literal — String literals. Use single quotes inside attribute values. |
| `... + ...` | String Concatenation — Use the `+` operator to concatenate strings. |
| `... \|\| ...` | String Append — A more readable alternative to `+` for concatenation. |

### 3.2 Variable Expressions (`${...}`)

Variable expressions access data added to the model by the controller. They support property access, method calls, and inline operations.

**Accessing Model Data**

```html
<!-- Simple property access -->
<p th:text="${user.name}">Name</p>

<!-- Method call on a model object -->
<p th:text="${user.getFullName()}">Full Name</p>

<!-- Accessing a collection size -->
<p th:text="${items.size()}">0 items</p>

<!-- Null-safe navigation (returns null instead of NPE) -->
<p th:text="${user?.address?.city}">City</p>

<!-- Ternary operator -->
<p th:text="${user.active ? 'Active' : 'Inactive'}">Status</p>
```

### 3.3 Message Expressions (`#{...}`)

**Internationalization**

```html
<!-- Simple message lookup -->
<h1 th:text="#{welcome.title}">Welcome</h1>

<!-- Message with parameters -->
<p th:text="#{welcome.greeting(${user.name})}">Hello, User</p>

<!-- Default value if key is missing -->
<p th:text="#{missing.key(Default Text)}">Fallback</p>
```

### 3.4 Link Expressions (`@{...}`)

**URL Generation**

```html
<!-- Simple path -->
<a th:href="@{/login}">Login</a>

<!-- Path with variable substitution -->
<a th:href="@{/users/{id}(id=${user.id})}">View Profile</a>

<!-- Path with query parameters -->
<a th:href="@{/search(q=${query},page=${page})}">Search</a>

<!-- Combined path variable and query parameter -->
<a th:href="@{/users/{id}/posts(id=${user.id},sort='date')}">Posts</a>
```

### 3.5 Selection Variable Expressions (`*{...}`)

**Selection Context with th:object**

```html
<!-- th:object sets the selection context -->
<div th:object="${user}">
    <p th:text="*{name}">Name</p>        <!-- Equivalent to ${user.name} -->
    <p th:text="*{address.city}">City</p> <!-- Equivalent to ${user.address.city} -->
    <p th:text="*{getAge()}">Age</p>      <!-- Method call on the selected object -->
</div>
```

### 3.6 Fragment Expressions (`~{...}`)

**Fragment References**

```html
<!-- Reference a named fragment -->
<div th:replace="~{fragments :: header}">Header</div>

<!-- Fragment with arguments -->
<div th:replace="~{components :: card(${title}, ${body})}">Card</div>

<!-- Inline fragment (no external file) -->
<div th:replace="~{:: #myFragment}">Inline Fragment</div>
```

### 3.7 Literals & Operators

| Type / Operator | Description |
|---|---|
| **Numeric** | `42`, `3.14`, `1_000_000` — Integer and decimal numbers. |
| **String** | `'Hello World'` — Single-quoted strings. |
| **Boolean** | `true`, `false` |
| **Null** | `null` |
| **Token** | Characters that do not need quoting (e.g., `Spring`, `active`). |
| **Arithmetic** | `+` `−` `*` `/` `%` — Standard math operators. |
| **Relational** | `>` `<` `>=` `<=` `==` `!=` — Comparisons. Use `gt`, `lt`, `ge`, `le`, `eq`, `ne` in HTML to avoid escaping issues. |
| **Logical** | `&& (and)`, `\|\| (or)`, `! (not)` |
| **Conditional** | `condition ? valueIfTrue : valueIfFalse` — Ternary operator. |
| **Elvis** | `value ?: defaultValue` — Returns `defaultValue` if value is null or empty. |
| **Range** | `1..10` — Used primarily with `th:each` for iteration. |

---

## 4. Standard Dialect — Attribute Processors

The Standard Dialect is the core set of processors that Thymeleaf provides out of the box. These are divided into several categories: attributes for modifying tags, attributes for text output, attributes for iteration and conditionals, and utility processors.

### 4.1 Text & Output Processors

| Processor | Description |
|---|---|
| `th:text` | Replaces the body of the tag with the evaluated expression (escapes HTML entities). |
| `th:utext` | Replaces the body of the tag with unescaped HTML output. Use with caution (XSS risk). |
| `th:value` | Sets the `value` attribute of form elements (`input`, `select`, `textarea`). |
| `th:action` | Sets the `action` attribute on `<form>` elements, typically with `@{}` link expressions. |

**th:text vs th:utext**

```html
<!-- th:text escapes HTML -->
<!-- If name = "<b>Alice</b>", output is: &lt;b&gt;Alice&lt;/b&gt; -->
<p th:text="${name}">Placeholder</p>

<!-- th:utext renders raw HTML -->
<!-- If html = "<b>Alice</b>", output is: <b>Alice</b> -->
<p th:utext="${html}">Placeholder</p>
```

### 4.2 Attribute Modification Processors

| Processor | Description |
|---|---|
| `th:attr` | Generic processor to set any attribute dynamically. |
| `th:attrsappend` | Appends a value to an existing attribute (useful for CSS classes). |
| `th:attrsprepend` | Prepends a value to an existing attribute. |
| `th:class-append` | Shortcut to append CSS classes. Equivalent to `th:attrsappend="class='...'"` |
| `th:id` | Sets the `id` attribute of a tag. |
| `th:name` | Sets the `name` attribute of a tag. |
| `th:src` | Sets the `src` attribute (for `<img>`, `<script>`, `<iframe>`). |
| `th:href` | Sets the `href` attribute (for `<a>`, `<link>`). |
| `th:data-*` | Sets HTML5 `data-*` attributes dynamically. |

**Attribute Processors in Use**

```html
<!-- th:attr — generic attribute setting -->
<input th:attr="placeholder=${placeholder}, maxlength=${maxLen}" />

<!-- th:class-append — conditional CSS class -->
<div class="card" th:class-append="${item.featured ? 'featured' : ''}">

<!-- th:id and th:name -->
<input th:id="'field-' + ${index}" th:name="items[${index}].value" />

<!-- th:src with link expression -->
<img th:src="@{/images/logo.png}" alt="Logo" />

<!-- th:data-* attributes -->
<tr th:attr="data-id=${item.id}" th:each="item : ${items}">
```

---

## 5. Text Processing & Inline Expressions

Thymeleaf supports inline processing, which allows expressions to be evaluated directly within the text body of an element or within attribute values, without needing a dedicated `th:` attribute.

### 5.1 Inline Variable Expressions

**`[[...]]` Syntax**

```html
<!-- Instead of using th:text, you can inline directly -->
<p>Hello, [[${name}]]!</p>
<!-- Output: Hello, Alice! -->

<!-- Multiple inline expressions in one element -->
<p>[[${firstName}]] [[${lastName}]] (Age: [[${age}]])</p>

<!-- Inline expressions can include operators -->
<p>Status: [[${user.active ? 'Active' : 'Inactive'}]]</p>
```

### 5.2 Inline Message Expressions

**`[[#{...}]]` Syntax**

```html
<!-- Inline i18n message -->
<p>[[#{app.greeting}]], [[${name}]]!</p>
```

### 5.3 Disabling Inline Processing

**`th:inline="none"`**

```html
<!-- Disable inline processing for a block (e.g., for JavaScript literals) -->
<script th:inline="none">
    var message = '[[This is literal text, not processed]]';
</script>
```

### 5.4 JavaScript Inline

**`th:inline="javascript"`**

```html
<script th:inline="javascript">
    // Output a model variable as a safe JavaScript string
    var userName = [[${user.name}]];
    // Output an object as a JSON literal
    var userObj = [[${user}]];
    // Literal output (not processed)
    var literal = /*[[# This is not processed #]]*/  'default';
</script>
```

> 💡 **Note:** JavaScript inline mode automatically escapes strings and serializes objects to JSON, preventing XSS attacks in script contexts.

---

## 6. Conditionals & Boolean Logic

Thymeleaf provides several processors for conditional rendering of elements and attributes.

### 6.1 th:if — Conditional Rendering

**th:if Examples**

```html
<!-- Render the element only if the condition is true -->
<p th:if="${user != null}">Welcome, <span th:text="${user.name}">User</span>!</p>

<!-- Condition on a boolean property -->
<div th:if="${showAnnouncement}" class="alert alert-info">
    <p th:text="${announcement}">Announcement text here.</p>
</div>

<!-- Condition on collection: true if non-null and non-empty -->
<ul th:if="${items}">
    <li th:each="item : ${items}" th:text="${item.name}">Item</li>
</ul>

<!-- String condition: true if non-null and non-empty -->
<span th:if="${errorMessage}" th:text="${errorMessage}" class="error"></span>
```

### 6.2 th:unless — Negated Conditional

**th:unless**

```html
<!-- Render only when the condition is false -->
<p th:unless="${user.loggedIn}">Please log in to continue.</p>

<!-- Equivalent to th:if with negation -->
<div th:unless="${items}" class="empty-state">No items found.</div>
```

### 6.3 th:if / th:unless Truth Rules

Understanding what Thymeleaf considers truthy is essential for correct conditional rendering:

| Value | Truthiness |
|---|---|
| `null` | `false` — Element is NOT rendered. |
| `false` (Boolean) | `false` — Element is NOT rendered. |
| `0` (Number) | `false` — Element is NOT rendered. |
| Empty String `""` | `false` — Element is NOT rendered. |
| Empty Collection | `false` — Element is NOT rendered. |
| `true` (Boolean) | `true` — Element IS rendered. |
| Non-zero Number | `true` — Element IS rendered. |
| Non-empty String | `true` — Element IS rendered. |
| Non-empty Collection | `true` — Element IS rendered. |
| Any Object | `true` — Element IS rendered (if not null). |

### 6.4 th:switch / th:case — Switch Statements

**Switch-Case Example**

```html
<!-- th:switch evaluates the expression once -->
<div th:switch="${user.role}">
    <p th:case="'ADMIN'" class="badge-admin">Administrator</p>
    <p th:case="'EDITOR'" class="badge-editor">Editor</p>
    <p th:case="'USER'" class="badge-user">Standard User</p>
    <!-- Default case -->
    <p th:case="*" class="badge-guest">Guest</p>
</div>

<!-- Switch with numeric values -->
<div th:switch="${status}">
    <span th:case="1">Pending</span>
    <span th:case="2">Active</span>
    <span th:case="3">Closed</span>
    <span th:case="*">Unknown</span>
</div>
```

> 💡 **Note:** Only one `th:case` branch is rendered — the first one that matches. The `*` wildcard serves as the default/else case.

---

## 7. Iteration

The `th:each` processor is the primary mechanism for iterating over collections, arrays, and other iterables.

### 7.1 Basic Iteration

**Simple List Iteration**

```html
<!-- Iterate over a list of strings -->
<ul>
    <li th:each="item : ${items}" th:text="${item}">Item</li>
</ul>

<!-- Iterate over a list of objects -->
<table>
    <tr th:each="user : ${users}">
        <td th:text="${user.name}">Name</td>
        <td th:text="${user.email}">Email</td>
        <td th:text="${user.role}">Role</td>
    </tr>
</table>
```

### 7.2 Iteration Status Variable

A second variable can be declared in `th:each` to receive an iteration status object, which provides index, count, first/last flags, and more.

**Iteration Status**

```html
<ul>
    <li th:each="item, status : ${items}">
        <!-- status.index   — zero-based index (0, 1, 2, ...) -->
        <!-- status.count   — one-based count (1, 2, 3, ...) -->
        <!-- status.first   — true if this is the first iteration -->
        <!-- status.last    — true if this is the last iteration -->
        <!-- status.size    — total number of elements -->
        <!-- status.current — the current iteration element -->

        <span th:text="${status.count}" class="num">1</span>.
        <span th:text="${item.name}">Item Name</span>
        <span th:if="${status.first}" class="badge">New</span>
    </li>
</ul>
```

| Property | Description |
|---|---|
| `status.index` | Zero-based index of the current iteration (0, 1, 2, ...). |
| `status.count` | One-based iteration count (1, 2, 3, ...). |
| `status.first` | Returns `true` if this is the first element. |
| `status.last` | Returns `true` if this is the last element. |
| `status.size` | Total number of elements in the iterable. |
| `status.current` | The element being processed in the current iteration. |
| `status.even` | Returns `true` if the current index is even. |
| `status.odd` | Returns `true` if the current index is odd. |
| `status.empty` | Returns `true` if the iterable is empty. |

### 7.3 Iterating Over Maps

**Map Iteration**

```html
<!-- Iterating over a Map<String, String> -->
<table>
    <tr th:each="entry : ${configMap}">
        <td th:text="${entry.key}">Key</td>
        <td th:text="${entry.value}">Value</td>
    </tr>
</table>
```

### 7.4 Iterating Over Arrays & Ranges

**Arrays and Numeric Ranges**

```html
<!-- Array iteration works identically to lists -->
<div th:each="color : ${colors}" th:text="${color}">Color</div>

<!-- Numeric range iteration (1 to 5 inclusive) -->
<div th:each="i : ${1..5}">
    <span th:text="${i}">N</span>
</div>
```

### 7.5 Empty State Handling

**Handling Empty Collections**

```html
<!-- Show content only when items exist -->
<div th:if="${items and not items.empty}">
    <ul>
        <li th:each="item : ${items}" th:text="${item.name}">Item</li>
    </ul>
</div>

<!-- Show alternative content when empty -->
<div th:unless="${items and not items.empty}" class="empty-state">
    <p>No items found. Try adding one!</p>
</div>
```

---

## 8. Tag & Element Manipulation

Thymeleaf provides processors that control the lifecycle and structure of HTML elements, including removal, replacement, and attribute management.

### 8.1 th:remove — Removing Elements

| Value | Behavior |
|---|---|
| `body` | Removes the body (children) of the tag, leaving the tag itself. |
| `tag` | Removes the tag itself, leaving its body (children) in place. |
| `all` | Removes the tag and all its body (equivalent to not rendering the element at all). |
| `value-if-empty` | Removes the tag body only if it is empty after processing. |
| `only-children` | Removes only the children of the tag, keeping the tag itself. |

**th:remove Examples**

```html
<!-- Remove the entire element and its content -->
<div th:remove="all" class="debug-only">Debug info</div>

<!-- Remove only the wrapper tag, keep content -->
<span th:remove="tag">This text stays, but the span tag is removed.</span>

<!-- Remove children, keep the tag -->
<div th:remove="body" class="container">Children removed.</div>

<!-- Conditional removal -->
<div th:remove="${showDebug ? 'none' : 'all'}" class="debug">Debug Panel</div>
```

### 8.2 th:replace & th:insert

**Fragment Insertion and Replacement**

```html
<!-- th:replace: replaces the entire tag with the fragment -->
<div th:replace="~{fragments :: header}">This div is replaced entirely.</div>

<!-- th:insert: inserts the fragment as a child of the tag -->
<div th:insert="~{fragments :: header}">
    <!-- Fragment content is inserted here, inside the div -->
</div>

<!-- th:replace with arguments -->
<div th:replace="~{components :: alert(type='success', msg='Saved!')}"></div>
```

### 8.3 th:with — Local Variable Declaration

**Local Variables**

```html
<!-- Declare a local variable for use within the scope -->
<div th:with="fullName=${user.firstName + ' ' + user.lastName}">
    <p th:text="${fullName}">Full Name</p>
</div>

<!-- Multiple local variables -->
<div th:with="x=${10}, y=${20}, sum=${10 + 20}">
    <p th:text="'Sum: ' + ${sum}">Sum</p>
</div>

<!-- Useful for computed values to avoid repeating expressions -->
<div th:with="discountedPrice=${product.price * (1 - discount)}">
    <span th:text="'$' + ${#numbers.formatDecimal(discountedPrice, 1, 2)}">$0.00</span>
</div>
```

### 8.4 th:fragment — Defining Fragments

**Fragment Definition**

```html
<!-- Define a named fragment in a template -->
<div th:fragment="header">
    <header>
        <nav>Navigation content</nav>
    </header>
</div>

<!-- Fragment with parameters -->
<div th:fragment="card(title, content)">
    <div class="card">
        <h3 th:text="${title}">Title</h3>
        <p th:text="${content}">Content</p>
    </div>
</div>
```

### 8.5 th:block — Virtual Wrapper Element

`th:block` is a special Thymeleaf element that acts as a logical grouping container but **produces no HTML output**. It is useful when you need to apply `th:each`, `th:if`, or other processors to a group of sibling elements without introducing an extra wrapper `<div>` or `<span>`.

**th:block Examples**

```html
<!-- Without th:block, you'd need a wrapping <div> just for th:each -->
<!-- th:block disappears from the output entirely -->
<th:block th:each="item : ${items}">
    <dt th:text="${item.term}">Term</dt>
    <dd th:text="${item.definition}">Definition</dd>
</th:block>

<!-- Conditional rendering of multiple siblings -->
<th:block th:if="${user.isAdmin}">
    <li><a th:href="@{/admin/users}">Manage Users</a></li>
    <li><a th:href="@{/admin/logs}">View Logs</a></li>
    <li><a th:href="@{/admin/settings}">Settings</a></li>
</th:block>

<!-- Combining th:each and th:if without nesting -->
<th:block th:each="notification : ${notifications}" th:if="${notification.unread}">
    <div class="notification unread">
        <span th:text="${notification.message}">Message</span>
    </div>
</th:block>
```

> 💡 **Note:** `th:block` is rendered as nothing in the final HTML. It exists purely to let you apply Thymeleaf processors to a group of elements that don't share a single natural parent tag.

---

Fragments are the mechanism Thymeleaf uses to achieve template reuse and layout composition. They allow you to define reusable blocks of HTML in one template and include them in others.

### 9.1 Defining Fragments

**fragments.html — Reusable Components**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <!-- Named fragment -->
    <div th:fragment="navbar">
        <nav class="navbar">
            <a th:href="@{/}">Home</a>
            <a th:href="@{/about}">About</a>
        </nav>
    </div>

    <!-- Fragment with parameters -->
    <div th:fragment="alert(type, message)">
        <div th:class="'alert alert-' + ${type}">
            <p th:text="${message}">Alert text</p>
        </div>
    </div>

    <!-- Fragment using CSS selector style -->
    <footer id="siteFooter">
        <p>&copy; 2026 My Application</p>
    </footer>
</body>
</html>
```

### 9.2 Including Fragments

**Including Fragments in Pages**

```html
<!-- th:insert — wraps fragment in the host tag -->
<div th:insert="~{fragments :: navbar}"></div>

<!-- th:replace — replaces the host tag with the fragment -->
<div th:replace="~{fragments :: navbar}"></div>

<!-- Passing arguments to a fragment -->
<div th:replace="~{fragments :: alert('success', 'Record saved successfully!')}"></div>

<!-- Using a variable as argument -->
<div th:replace="~{fragments :: alert(${alertType}, ${alertMessage})}"></div>

<!-- CSS selector-based fragment reference -->
<div th:replace="~{fragments :: #siteFooter}"></div>
```

### 9.3 Layout Pattern — Manual Approach

**layout.html — Page Layout Template**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${pageTitle}">Default Title</title>
    <link rel="stylesheet" th:href="@{/css/style.css}" />
</head>
<body>
    <div th:replace="~{fragments :: navbar}"></div>

    <main>
        <!-- Content will be injected here by child pages -->
        <div th:fragment="content">
            <p>Default content — override in child templates.</p>
        </div>
    </main>

    <div th:replace="~{fragments :: footer}"></div>
</body>
</html>
```

### 9.4 Thymeleaf Layout Dialect

For full layout inheritance (similar to Razor or Blade layouts), the Thymeleaf Layout Dialect is recommended. Add the dependency to your project:

**Maven Dependency**

```xml
<dependency>
    <groupId>nz.net.2of3</groupId>
    <artifactId>thymeleaf-layout-dialect</artifactId>
    <version>3.3.0</version>
</dependency>
```

**layout/default.html — Master Layout**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://ultraq.net.au/thymeleaf/layout">
<head>
    <!-- layout:title-pattern merges child's title with the layout title -->
    <title layout:title-pattern="${ChildTitle} - ${LayoutTitle}">My App</title>
    <link rel="stylesheet" th:href="@{/css/style.css}" />
    <!-- layout:fragment="scripts" will be filled by child -->
    <script layout:fragment="scripts"></script>
</head>
<body>
    <div th:replace="~{fragments :: navbar}"></div>

    <!-- Main content area — child pages fill this -->
    <main>
        <layout:fragment>content</layout:fragment>
    </main>

    <div th:replace="~{fragments :: footer}"></div>
</body>
</html>
```

**pages/dashboard.html — Child Page**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://ultraq.net.au/thymeleaf/layout"
      layout:decorate="~{layout/default}">
<head>
    <title>Dashboard</title>
</head>
<body>
    <!-- This replaces <layout:fragment>content</layout:fragment> -->
    <div layout:fragment="content">
        <h1>Dashboard</h1>
        <p>Your dashboard content here.</p>
    </div>

    <!-- This fills the scripts fragment in the head -->
    <div layout:fragment="scripts">
        <script th:src="@{/js/dashboard.js}"></script>
    </div>
</body>
</html>
```

> 💡 **Note:** The Layout Dialect uses `layout:decorate` to specify which master layout a child template inherits. All content outside `layout:fragment` blocks is discarded.

---

## 10. Form Handling

Thymeleaf integrates tightly with Spring MVC for form binding, validation, and error display. The `th:object` and `th:field` processors are the foundation of form handling.

### 10.1 Basic Form Structure

**Form with th:object and th:field**

```html
<!-- th:object binds the form to a model attribute -->
<form th:action="@{/users/register}" th:object="${registerForm}" method="post">

    <!-- th:field automatically sets id, name, and value attributes -->
    <div class="form-group">
        <label for="username">Username</label>
        <input type="text" th:field="*{username}" class="form-control" />
    </div>

    <div class="form-group">
        <label for="email">Email</label>
        <input type="email" th:field="*{email}" class="form-control" />
    </div>

    <div class="form-group">
        <label for="password">Password</label>
        <input type="password" th:field="*{password}" class="form-control" />
    </div>

    <button type="submit" class="btn btn-primary">Register</button>
</form>
```

### 10.2 How th:field Works

The `th:field` processor expands into three standard HTML attributes: `id`, `name`, and `value`. It uses the selection context set by `th:object`.

| Element | What th:field Generates |
|---|---|
| `text` | `id="username" name="username" value="currentValue"` |
| `password` | `id="password" name="password" value=""` |
| `checkbox` | `id="agree" name="agree"` (checked if value is true) |
| `radio` | `id="role" name="role" value="ADMIN"` (checked if matches) |
| `select` | `id="country" name="country"` (selected option marked) |
| `textarea` | `id="bio" name="bio"` — value is placed as text content |

### 10.3 Select, Radio, and Checkbox

**Complex Form Elements**

```html
<form th:object="${form}" th:action="@{/submit}" method="post">

    <!-- Select with options -->
    <select th:field="*{country}">
        <option value="">-- Select Country --</option>
        <!-- th:each populates options; th:selected marks the current value -->
        <option th:each="country : ${countries}"
                th:value="${country.code}"
                th:text="${country.name}"
                th:selected="${country.code == *{country}}">Country</option>
    </select>

    <!-- Radio buttons -->
    <div th:each="role : ${availableRoles}">
        <input type="radio" th:field="*{role}" th:value="${role}" />
        <label th:text="${role}">Role</label>
    </div>

    <!-- Checkbox (single boolean) -->
    <input type="checkbox" th:field="*{agreeToTerms}" />
    <label>I agree to the terms</label>

    <!-- Checkbox (multiple values — collection binding) -->
    <div th:each="perm : ${allPermissions}">
        <input type="checkbox" th:field="*{permissions}" th:value="${perm}" />
        <label th:text="${perm}">Permission</label>
    </div>
</form>
```

### 10.4 Validation Error Display

**Showing Validation Errors**

```html
<form th:object="${registerForm}" th:action="@{/register}" method="post">

    <!-- Global form errors -->
    <div th:if="${#fields.hasGlobalErrors()}" class="alert alert-danger">
        <ul>
            <li th:each="error : ${#fields.globalErrors()}" th:text="${error}">Error</li>
        </ul>
    </div>

    <!-- Per-field error display -->
    <div class="form-group" th:classappend="${#fields.hasErrors('username') ? 'has-error' : ''}">
        <label for="username">Username</label>
        <input type="text" th:field="*{username}" class="form-control" />
        <!-- #fields.errors('fieldName') returns the list of error messages -->
        <span th:if="${#fields.hasErrors('username')}"
              th:errors="*{username}"
              class="help-block error-text">Error message</span>
    </div>

    <button type="submit" class="btn btn-primary">Register</button>
</form>
```

> 💡 **Note:** The `#fields` utility object works only within the scope of a `th:object`. Use `#fields.hasErrors('fieldName')` to check, and `th:errors` to display the messages.

---

## 11. Spring Security Integration

Thymeleaf provides a dedicated dialect for Spring Security called the Thymeleaf Extras Spring Security Dialect. It enables authorization checks, role-based rendering, and CSRF token handling directly in templates.

### 11.1 Adding the Dependency

**Maven Dependency**

```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    <!-- Use springsecurity5 for Spring Security 5.x -->
    <version>3.1.2</version>
</dependency>
```

### 11.2 Template Namespace

**HTML Element with Security Namespace**

```html
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
```

### 11.3 sec:authorize — Role-Based Rendering

**Authorization Checks**

```html
<!-- Show only if the user is authenticated -->
<div sec:authorize="isAuthenticated()">
    <p>Welcome back!</p>
</div>

<!-- Show only for anonymous users -->
<div sec:authorize="isAnonymous()">
    <a th:href="@{/login}">Please log in</a>
</div>

<!-- Show only for users with a specific role -->
<div sec:authorize="hasRole('ADMIN')">
    <a th:href="@{/admin/dashboard}">Admin Panel</a>
</div>

<!-- Multiple roles (OR logic) -->
<div sec:authorize="hasAnyRole('ADMIN', 'EDITOR')">
    <a th:href="@{/content/manage}">Content Management</a>
</div>

<!-- Show only for users with a specific authority -->
<div sec:authorize="hasAuthority('PERMISSION_EDIT')">
    <button>Edit</button>
</div>

<!-- Combined conditions -->
<div sec:authorize="isAuthenticated() and hasRole('PREMIUM')">
    <a th:href="@{/premium/features}">Premium Features</a>
</div>
```

### 11.4 sec:authorize-url — URL-Based Authorization

**URL Authorization**

```html
<!-- Only show link if user has permission to access the URL -->
<a th:href="@{/admin}" sec:authorize-url="/admin">Admin</a>

<!-- Works with any HTTP method -->
<button sec:authorize-url="post /api/data">Submit</button>
```

### 11.5 sec:authentication — Accessing the Authenticated User

**Displaying User Information**

```html
<!-- Display the username -->
<span sec:authentication-name="">Username</span>

<!-- Access principal properties -->
<span sec:authentication="name">Username</span>

<!-- Access nested principal properties -->
<p sec:authentication="principal.email">user@example.com</p>

<!-- Access the full authentication object in expressions -->
<p th:text="${#authentication.principal.firstName}">First Name</p>
<p th:text="${#authentication.principal.lastName}">Last Name</p>
```

### 11.6 CSRF Token in Forms

**CSRF with Spring Security**

```html
<!-- Spring Security CSRF is automatically included when using th:action -->
<form th:action="@{/submit}" method="post">
    <!-- CSRF token is auto-injected — no manual action needed! -->
    <input type="text" name="data" />
    <button type="submit">Submit</button>
</form>

<!-- Manual CSRF token (if needed outside a form) -->
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
```

> 💡 **Note:** When using `th:action` on a `<form>`, Spring Security's CSRF token is automatically appended as a hidden field. Manual inclusion is only necessary when not using `th:action`.

### 11.7 Login & Logout Forms

**Login Form Template**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head><title>Login</title></head>
<body>
    <h1>Sign In</h1>

    <!-- Display error on failed login -->
    <div th:if="${param.error}" class="alert alert-danger">
        <p>Invalid username or password.</p>
    </div>

    <!-- Display message on logout -->
    <div th:if="${param.logout}" class="alert alert-info">
        <p>You have been logged out.</p>
    </div>

    <!-- Login form — action must match Spring Security's login URL -->
    <form th:action="@{/login}" method="post">
        <div class="form-group">
            <label for="username">Username</label>
            <input type="text" id="username" name="username" class="form-control" />
        </div>
        <div class="form-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" class="form-control" />
        </div>
        <button type="submit" class="btn btn-primary">Sign In</button>
    </form>

    <!-- Logout link -->
    <form th:action="@{/logout}" method="post">
        <button type="submit" class="btn btn-link">Logout</button>
    </form>
</body>
</html>
```

---

## 12. Utility Objects

Thymeleaf provides a set of built-in utility objects (prefixed with `#`) that can be used within expressions to perform common operations like formatting, date manipulation, collections processing, and more.

### 12.1 Overview of Utility Objects

| Object | Purpose |
|---|---|
| `#strings` | String utility methods (`isEmpty`, `contains`, `startsWith`, `trim`, `replace`, etc.). |
| `#lists` | List utility methods (`size`, `contains`, `isEmpty`, `sort`, etc.). |
| `#sets` | Set utility methods (`size`, `contains`, `isEmpty`, etc.). |
| `#maps` | Map utility methods (`size`, `containsKey`, `keys`, `values`, etc.). |
| `#numbers` | Number formatting (`formatDecimal`, `formatInteger`, `formatPercent`, etc.). |
| `#dates` | Date formatting and manipulation (`format`, `createSequence`, `year`, `month`, etc.). |
| `#calendars` | Calendar utility methods (`format`, `year`, `month`, `day`, etc.). |
| `#temporals` | Java 8 Time API utilities (`format`, `year`, `month`, `dayOfWeek`, etc.). |
| `#booleans` | Boolean utility methods (`isTrue`, `isFalse`, etc.). |
| `#objects` | Objects utility (`nullSafe`, `toString`, `equals`, etc.). |
| `#fields` | Form validation field utilities (`hasErrors`, `errors`, `globalErrors`, etc.). |
| `#ids` | ID generation utilities for repeated fragments (`seq`, `next`, etc.). |

### 12.2 #strings — String Utilities

**#strings Examples**

```html
<!-- Check if a string is empty or null -->
<p th:if="${#strings.isEmpty(message)}">No message.</p>

<!-- Contains check -->
<span th:if="${#strings.contains(name, 'admin')}">Admin user detected</span>

<!-- Convert to uppercase / lowercase -->
<p th:text="${#strings.toUpperCase(name)}">NAME</p>
<p th:text="${#strings.toLowerCase(name)}">name</p>

<!-- Trim whitespace -->
<p th:text="${#strings.trim(input)}">trimmed</p>

<!-- Substring -->
<p th:text="${#strings.substring(text, 0, 50)}">First 50 chars...</p>

<!-- Replace -->
<p th:text="${#strings.replace(text, 'old', 'new')}">Replaced text</p>

<!-- Starts with / Ends with -->
<span th:if="${#strings.startsWith(url, 'https')}">Secure</span>
```

### 12.3 #numbers — Number Formatting

**#numbers Examples**

```html
<!-- Format a decimal number -->
<p th:text="${#numbers.formatDecimal(price, 1, 2)}">$19.99</p>

<!-- Format as currency (locale-dependent) -->
<p th:text="${#numbers.formatDecimal(amount, 1, 2, 'COMMA')}">1,234.56</p>

<!-- Format as percentage -->
<p th:text="${#numbers.formatPercent(ratio, 1, 1)}">85.0%</p>

<!-- Format integer with thousand separators -->
<p th:text="${#numbers.formatInteger(bigNum, 3, 'COMMA')}">1,000,000</p>
```

### 12.4 #dates & #temporals — Date/Time Formatting

**Date and Time Formatting**

```html
<!-- Format a java.util.Date -->
<p th:text="${#dates.format(createdAt, 'dd/MM/yyyy')}">31/01/2026</p>

<!-- Format a java.time.LocalDate (Java 8+) -->
<p th:text="${#temporals.format(eventDate, 'MMMM dd, yyyy')}">January 31, 2026</p>

<!-- Format a java.time.LocalDateTime -->
<p th:text="${#temporals.format(timestamp, 'dd/MM/yyyy HH:mm')}">31/01/2026 14:30</p>

<!-- Extract components from LocalDate -->
<p th:text="${#temporals.year(eventDate)}">2026</p>
<p th:text="${#temporals.month(eventDate)}">JANUARY</p>
<p th:text="${#temporals.dayOfWeek(eventDate)}">SATURDAY</p>
```

### 12.5 #lists — Collection Utilities

**#lists Examples**

```html
<!-- Check if a list is empty -->
<div th:if="${#lists.isEmpty(items)}">No items available.</div>

<!-- Check if list contains a value -->
<span th:if="${#lists.contains(selectedRoles, 'ADMIN')}">Has Admin</span>

<!-- Get size -->
<span th:text="${#lists.size(items)}">0</span>

<!-- Convert to a list (useful for arrays) -->
<div th:with="itemList=${#lists.toList(itemArray)}">
    <p th:text="${#lists.size(itemList)}">0</p>
</div>
```

### 12.6 #sets — Set Utilities

**#sets Examples**

```html
<!-- Check if a set is empty -->
<div th:if="${#sets.isEmpty(selectedTags)}">No tags selected.</div>

<!-- Check if a set contains a value -->
<span th:if="${#sets.contains(activeRoles, 'MODERATOR')}">Moderator</span>

<!-- Get size of a set -->
<span th:text="${#sets.size(selectedTags)}">0</span>
```

### 12.7 #maps — Map Utilities

**#maps Examples**

```html
<!-- Check if a map is empty -->
<div th:if="${#maps.isEmpty(metadata)}">No metadata available.</div>

<!-- Check if a map contains a specific key -->
<span th:if="${#maps.containsKey(settings, 'theme')}">
    Theme: [[${settings.get('theme')}]]
</span>

<!-- Get all keys or values -->
<ul>
    <li th:each="key : ${#maps.keys(settings)}" th:text="${key}">Key</li>
</ul>

<!-- Get size -->
<span th:text="${#maps.size(settings)}">0</span>
```

### 12.8 #objects — General Object Utilities

**#objects Examples**

```html
<!-- Null-safe toString — avoids NPE on null objects -->
<p th:text="${#objects.toString(user, 'N/A')}">User info</p>

<!-- Null-safe equals comparison -->
<span th:if="${#objects.equals(user.status, 'ACTIVE')}">Active</span>

<!-- nullSafe — returns empty string if value is null -->
<p th:text="${#objects.nullSafe(user.bio, '')}">Biography</p>

<!-- Useful inside th:with for safe defaults -->
<div th:with="displayName=${#objects.toString(user.name, 'Anonymous')}">
    <h2 th:text="${displayName}">Name</h2>
</div>
```

### 12.9 #ids — ID Generation for Fragments

**Unique ID Generation**

```html
<!-- Generate unique IDs in repeated fragments to avoid conflicts -->
<!-- #ids.seq('prefix') generates: prefix1, prefix2, prefix3, ... -->
<div th:each="item : ${items}">
    <input type="text" th:id="${#ids.seq('input-')}" />
    <label th:for="${#ids.last('input-')}" th:text="${item.name}">Label</label>
</div>

<!-- #ids.next and #ids.last reference the same sequence -->
```

---

## 13. Internationalization (i18n)

Thymeleaf's message expressions (`#{...}`) integrate with Spring's `MessageSource` to support internationalization and localization of your web application.

### 13.1 Resource Bundle Structure

**File Naming Convention**

```
src/main/resources/
├── messages.properties          ← Default (fallback)
├── messages_en.properties       ← English
├── messages_fr.properties       ← French
├── messages_de.properties       ← German
└── messages_es.properties       ← Spanish
```

### 13.2 Message Files

**messages_en.properties**

```properties
# Simple key-value
app.title=My Application
app.welcome=Welcome to our site!

# Message with parameters (indexed)
app.greeting=Hello, {0}! Welcome back.
app.items.count=You have {0} items in your cart.

# Message with named parameters
app.order.summary=Order #{0} placed on {1} for ${2}.
```

**messages_fr.properties**

```properties
app.title=Mon Application
app.welcome=Bienvenue sur notre site!
app.greeting=Bonjour, {0}! Content de vous revoir.
app.items.count=Vous avez {0} articles dans votre panier.
app.order.summary=Commande #{0} passée le {1} pour {2}€.
```

### 13.3 Using Messages in Templates

**Message Expressions**

```html
<!-- Simple message -->
<h1 th:text="#{app.title}">App Title</h1>

<!-- Message with a parameter -->
<p th:text="#{app.greeting(${user.name})}">Hello, User!</p>

<!-- Message with multiple parameters -->
<p th:text="#{app.order.summary(${order.id}, ${order.date}, ${order.total})}">Order details</p>

<!-- Inline message expression -->
<p>[[#{app.welcome}]]</p>

<!-- As an attribute value -->
<input type="text" th:placeholder="#{app.search.placeholder}" />
```

### 13.4 Locale Switching

**Language Switcher in Template**

```html
<!-- Simple language switcher links -->
<div class="language-switcher">
    <a th:href="@{/home(lang='en')}">English</a>
    <a th:href="@{/home(lang='fr')}">Français</a>
    <a th:href="@{/home(lang='de')}">Deutsch</a>
</div>
```

**LocaleChangeInterceptor Configuration**

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        registry.addInterceptor(interceptor);
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
}
```

> 💡 **Note:** Spring Boot auto-configures the `LocaleResolver` bean. The `LocaleChangeInterceptor` reads the `lang` query parameter and updates the locale for the session or cookie.

---

## 14. Spring MVC Integration

Thymeleaf integrates deeply with Spring MVC, providing access to Spring-specific utility objects, environment variables, and expression enhancements.

### 14.1 Spring Expression Language (SpEL)

In a Spring context, `${...}` expressions in Thymeleaf actually evaluate Spring Expression Language (SpEL), which gives access to Spring beans, environment variables, and more.

**SpEL in Thymeleaf**

```html
<!-- Access a Spring bean directly -->
<p th:text="${@userService.getUserCount()}">0 users</p>

<!-- Access environment properties -->
<p th:text="${environment.getProperty('app.version')}">1.0</p>

<!-- Access system properties -->
<p th:text="${T(java.lang.System).getProperty('java.version')}">Java Version</p>
```

### 14.2 Spring Model Attributes

**Controller → Template Data Flow**

```java
// Controller
@Controller
public class ProductController {

    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("currentUser", getCurrentUser());
        return "products/list";
    }
}
```

```html
<!-- templates/products/list.html -->
<!-- Access all model attributes via ${} -->
<h1>Products (Total: [[${products.size()}]])</h1>
<ul>
    <li th:each="product : ${products}" th:text="${product.name}">Product</li>
</ul>
```

### 14.3 Flash Attributes (Redirect Messages)

**RedirectAttributes and Flash Messages**

```java
// Controller
@PostMapping("/products")
public String createProduct(@ModelAttribute Product product,
                            RedirectAttributes redirectAttrs) {
    productService.save(product);
    redirectAttrs.addFlashAttribute("successMessage", "Product created successfully!");
    return "redirect:/products";
}
```

```html
<!-- Template — display flash message after redirect -->
<div th:if="${successMessage}" class="alert alert-success">
    <p th:text="${successMessage}">Success!</p>
</div>
```

### 14.4 @SessionAttributes

**Session-Scoped Model Attributes**

```java
@Controller
@SessionAttributes("wizardForm")
public class WizardController {

    @GetMapping("/wizard/step1")
    public String step1(@ModelAttribute("wizardForm") WizardForm form) {
        return "wizard/step1";
    }

    @PostMapping("/wizard/step1")
    public String step1Submit(@ModelAttribute("wizardForm") WizardForm form) {
        // Form data is stored in the session automatically
        return "redirect:/wizard/step2";
    }
}
```

### 14.5 Static Resources

**Referencing CSS, JS, and Images**

```html
<!-- CSS -->
<link rel="stylesheet" th:href="@{/css/style.css}" />

<!-- JavaScript -->
<script th:src="@{/js/app.js}"></script>

<!-- Image -->
<img th:src="@{/images/logo.png}" alt="Logo" />

<!-- With a context path (e.g., app deployed at /myapp) -->
<!-- @{} automatically prepends the context path -->
<link rel="stylesheet" th:href="@{/css/theme.css}" />
```

> 💡 **Note:** Using `@{}` for static resources ensures the context path is correctly prepended, regardless of how your application is deployed.

---

## 15. Advanced Topics

### 15.1 Thymeleaf Configuration Extensions

**Custom Template Resolver Example**

```java
@Configuration
public class ThymeleafAdvancedConfig {

    @Bean
    public ITemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/email/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(2); // Lower priority than default resolver
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.setAdditionalTemplateResolver(emailTemplateResolver());
        engine.setEnableSpringELCompiler(true);
        engine.addDialect(new SpringSecurityDialect());
        engine.addDialect(new LayoutDialect());
        return engine;
    }
}
```

**Custom Dialect Creation (Brief Overview)**

```java
public class CustomDialect extends AbstractProcessorDialect {
    
    public CustomDialect() {
        super("Custom Dialect", "custom", 1000);
    }
    
    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        Set<IProcessor> processors = new HashSet<>();
        processors.add(new CustomProcessor(dialectPrefix));
        return processors;
    }
}

public class CustomProcessor extends AbstractAttributeTagProcessor {
    // Custom processor implementation
}
```

### 15.2 Testing Thymeleaf Templates

**Unit Testing with SpringBootTest**

```java
@SpringBootTest
@AutoConfigureMockMvc
class ThymeleafTemplateTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHomePage() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(view().name("index"))
               .andExpect(model().attributeExists("name"))
               .andExpect(content().string(containsString("Hello")));
    }

    @Test
    void testFragmentRendering() throws Exception {
        mockMvc.perform(get("/fragments/navbar"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("Home")));
    }
}
```

**Testing i18n Messages**

```java
@Test
void testInternationalization() throws Exception {
    mockMvc.perform(get("/home").locale(Locale.FRENCH))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Bienvenue")));
}
```

### 15.3 Thymeleaf and WebJars Integration

**Adding WebJars Dependencies**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>bootstrap</artifactId>
    <version>5.3.0</version>
</dependency>
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>jquery</artifactId>
    <version>3.7.0</version>
</dependency>
```

**Using WebJars in Thymeleaf Templates**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <!-- Bootstrap CSS from WebJars -->
    <link rel="stylesheet" 
          th:href="@{/webjars/bootstrap/5.3.0/css/bootstrap.min.css}" />
    
    <!-- jQuery from WebJars -->
    <script th:src="@{/webjars/jquery/3.7.0/jquery.min.js}"></script>
    
    <!-- Bootstrap JS from WebJars -->
    <script th:src="@{/webjars/bootstrap/5.3.0/js/bootstrap.bundle.min.js}"></script>
</head>
<body>
    <!-- Use Bootstrap components -->
    <div class="container">
        <button class="btn btn-primary">Bootstrap Button</button>
    </div>
</body>
</html>
```

**Spring Boot Configuration for WebJars**

```properties
# application.properties
spring.mvc.static-path-pattern=/webjars/**
spring.web.resources.static-locations=classpath:/META-INF/resources/webjars/
```

> 💡 **Note:** Spring Boot auto-configures `/webjars/**` out of the box. You only need the properties above if you are overriding the default static resource handling. Adding them unnecessarily can conflict with Spring Boot's auto-configuration and cause WebJars to stop resolving.

**Email Template Structure**

```
src/main/resources/templates/email/
├── welcome-email.html
├── password-reset.html
└── order-confirmation.html
```

**Email Template Example**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${emailSubject}">Welcome</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
        .content { padding: 20px; }
        .button { display: inline-block; padding: 10px 20px; background-color: #4CAF50; 
                 color: white; text-decoration: none; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 th:text="#{email.welcome.title}">Welcome to Our Service</h1>
        </div>
        <div class="content">
            <p th:text="'Hello, ' + ${user.name} + '!'">Hello, User!</p>
            <p th:text="#{email.welcome.message}">
                Thank you for joining our service. We're excited to have you on board.
            </p>
            <p>
                <a th:href="${activationLink}" class="button" 
                   th:text="#{email.welcome.activate}">Activate Account</a>
            </p>
        </div>
    </div>
</body>
</html>
```

**Controller for Sending Email**

```java
@Controller
public class EmailController {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;

    public void sendWelcomeEmail(User user) {
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("activationLink", generateActivationLink(user));
        
        String htmlContent = templateEngine.process("email/welcome-email", context);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(user.getEmail());
        helper.setSubject("Welcome to Our Service");
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
}
```

### 15.5 Conditional Comments for Legacy Browsers

> ⚠️ **Legacy / Archival:** IE conditional comments have been obsolete since Internet Explorer was retired. This section is retained here only as a historical reference for teams maintaining older codebases. New projects targeting Spring Boot 3.x have no need for any of the patterns below.

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <!--[if IE]>
        <link rel="stylesheet" th:href="@{/css/ie-fixes.css}" />
    <![endif]-->
    
    <!--[if lt IE 9]>
        <script th:src="@{/js/html5shiv.min.js}"></script>
        <script th:src="@{/js/respond.min.js}"></script>
    <![endif]-->
</head>
<body>
    <!--[if IE]>
        <div class="ie-warning">
            <p>You are using an outdated browser. Please upgrade for better experience.</p>
        </div>
    <![endif]-->
    
    <main>
        <!-- Modern content -->
        <section th:fragment="modern-content">
            <h1>Modern Content</h1>
        </section>
    </main>
</body>
</html>
```

**Conditional CSS Classes for Browser Detection**

```html
<!-- Use with Modernizr or similar browser detection -->
<html class="no-js" xmlns:th="http://www.thymeleaf.org">
<head>
    <script>
        document.documentElement.className = 
            document.documentElement.className.replace('no-js', 'js');
    </script>
</head>
<body>
    <div th:classappend="${@browserDetector.isIE() ? 'ie-browser' : ''}">
        <!-- IE-specific adjustments -->
    </div>
</body>
</html>
```

### 15.6 Preprocessing with th:pre

**Preprocessed Expressions**

```html
<!-- th:pre is evaluated BEFORE other Thymeleaf attributes -->
<!-- Useful for dynamic attribute names or expressions -->
<div th:pre="'th:text=\'' + ${expressionToEvaluate} + '\''" >
    Preprocessed content
</div>

<!-- Dynamic attribute names -->
<input th:pre="'th:' + ${fieldType} + '=\"*{' + ${fieldName} + '}\"'" />

<!-- Complex preprocessing example -->
<div th:pre="${isRequired ? 'th:required=\"required\"' : ''} 
              ${hasError ? 'th:classappend=\"has-error\"' : ''}">
    Form field
</div>
```

### 15.7 th:classappend & th:styleappend

**Dynamic Styling**

```html
<!-- Append a class based on a condition -->
<div class="btn btn-default"
     th:classappend="${item.active ? 'btn-primary' : 'btn-outline'}">
    Click Me
</div>

<!-- Multiple conditional classes -->
<div class="card"
     th:classappend="${item.priority == 'high' ? 'card-high' : 
                      item.priority == 'medium' ? 'card-medium' : 'card-low'}">
    Priority Card
</div>

<!-- Append inline styles dynamically -->
<div style="display: block;"
     th:styleappend="${'background-color: ' + item.color + ';'}">
    Colored Block
</div>

<!-- Conditional styling with multiple properties -->
<div th:styleappend="${item.isFeatured ? 
                      'border: 2px solid gold; box-shadow: 0 0 10px gold;' : ''}">
    Featured Item
</div>
```

### 15.8 Literal Substitution

**Literal Substitution with `|...|`**

```html
<!-- Literal substitution simplifies string concatenation -->
<!-- Instead of: 'Hello, ' + ${name} + '!' -->
<p th:text="|Hello, ${name}!|">Hello, World!</p>

<!-- Works with link expressions too -->
<a th:href="@{|/users/${user.id}/profile|}">Profile</a>

<!-- Multiple variables -->
<p th:text="|${firstName} ${lastName} (${age} years old)|">Name (Age)</p>

<!-- Complex expressions inside literal substitution -->
<p th:text="|Total: $${#numbers.formatDecimal(total, 1, 2)}|">Total: $0.00</p>

<!-- With i18n messages -->
<p th:text="|#{app.welcome}, ${userName}!|">Welcome, User!</p>
```

### 15.9 Conditional Attributes — th:attrappend

**Conditional Attribute Values**

```html
<!-- Only add the 'disabled' attribute when condition is true -->
<!-- Using th:attr with a null value removes the attribute -->
<input type="text"
       th:attr="disabled=${isReadOnly ? 'disabled' : null}"
       name="field" />

<!-- th:attrappend adds to existing attribute -->
<div class="card"
     th:attrappend="class=${premium ? ' premium-card' : ''}">
    Content
</div>

<!-- Multiple attribute appends -->
<input type="text"
       th:attrappend="placeholder=${hasHint ? hintText : ''}
                      class=${hasError ? 'error' : ''}" />

<!-- Data attribute conditional addition -->
<div th:attrappend="data-config=${configJson ?: ''}"
     th:attr="data-enabled=${isEnabled}">
    Configurable Element
</div>
```

### 15.10 Decoupled Template Logic (Structured Data Files)

Thymeleaf supports an optional mode called "Decoupled Template Logic" where template logic is separated into an external file (e.g., XML or Java) rather than embedded in the HTML. This allows pure HTML files to be used as templates with logic defined externally, maintaining full compatibility with design tools.

**Structure of Decoupled Logic**

```
templates/
├── index.html            ← Pure HTML (no th: attributes)
└── index.th.xml          ← Thymeleaf logic (attributes applied externally)
```

**index.html (Pure HTML)**

```html
<!DOCTYPE html>
<html>
<head><title>Home</title></head>
<body>
    <h1 id="title">Welcome</h1>
    <p id="greeting">Hello!</p>
    <ul id="items">
        <li class="item">Sample Item</li>
    </ul>
</body>
</html>
```

**index.th.xml (External Logic)**

```xml
<div xmlns:th="http://www.thymeleaf.org">
    <body>
        <h1 id="title" th:text="${pageTitle}"/>
        <p id="greeting" th:text="#{welcome.message}"/>
        <ul id="items">
            <li class="item" th:each="item : ${items}" 
                th:text="${item.name}" th:classappend="${item.featured ? 'featured' : ''}"/>
        </ul>
    </body>
</div>
```

**Configuration for Decoupled Templates**

```properties
# application.properties
spring.thymeleaf.decoupled-logic=true
spring.thymeleaf.decoupled-logic-suffix=.th.xml
```

### 15.11 Performance Considerations

**Template Caching Strategy**

```properties
# Development configuration
spring.thymeleaf.cache=false
spring.thymeleaf.template-resolver-order=1

# Production configuration
spring.thymeleaf.cache=true
spring.thymeleaf.cache.ttl=3600  # 1 hour cache
spring.thymeleaf.servlet.content-type=text/html
```

**Performance Optimization Tips**

```html
<!-- 1. Minimize heavy logic in templates -->
<!-- Instead of complex calculations in template: -->
<div th:with="discounted=${#numbers.formatDecimal(product.price * 0.9, 1, 2)}">
    Price: $[[${discounted}]]
</div>

<!-- 2. Use th:remove for debug-only content -->
<div th:remove="${production ? 'all' : 'none'}" class="debug-panel">
    Debug information
</div>

<!-- 3. Avoid deep nesting of th:each -->
<!-- Inefficient: -->
<div th:each="category : ${categories}">
    <div th:each="product : ${category.products}">
        <div th:each="variant : ${product.variants}">
            [[${variant.name}]]
        </div>
    </div>
</div>

<!-- Better: Flatten data in controller -->
<div th:each="variant : ${flattenedVariants}">
    [[${variant.name}]]
</div>

<!-- 4. Use fragments wisely for reuse -->
<th:block th:replace="~{fragments :: expensive-to-render-component}"></th:block>
```

### 15.12 Error Handling & Fallbacks

**Graceful Fallback Strategies**

```html
<!-- Elvis operator for null/empty fallback -->
<p th:text="${user.displayName ?: 'Anonymous User'}">User</p>

<!-- Default values in expressions -->
<p th:text="${#strings.defaultString(message, 'No message available')}">
    Default message
</p>

<!-- Safe navigation to avoid NPE -->
<p th:text="${user?.address?.city ?: 'City not specified'}">City</p>

<!-- Conditional rendering with fallback -->
<div th:if="${data != null and not data.empty}">
    <!-- Process data -->
</div>
<div th:unless="${data != null and not data.empty}">
    <p class="empty-state">No data available</p>
</div>

<!-- Template-level error handling -->
<div th:if="${#fields.hasErrors('global')}">
    <div class="alert alert-danger">
        <p th:each="error : ${#fields.errors('global')}" 
           th:text="${error}">Global error</p>
    </div>
</div>
```

**Controller Exception Handling Integration**

```java
@ControllerAdvice
public class TemplateExceptionHandler {
    
    @ExceptionHandler(TemplateProcessingException.class)
    public String handleTemplateError(Model model, Exception ex) {
        model.addAttribute("errorMessage", "Template processing failed");
        model.addAttribute("debugInfo", ex.getMessage());
        return "error/template-error";
    }
}
```

### 15.13 Accessibility Considerations

**Accessible Thymeleaf Templates**

```html
<!-- Proper label association -->
<div class="form-group">
    <label for="usernameInput" th:text="#{form.username}">Username</label>
    <input type="text" 
           th:id="'usernameInput'" 
           th:field="*{username}"
           th:attr="aria-describedby='usernameHelp'"
           aria-required="true" />
    <small id="usernameHelp" class="form-text">
        [[#{form.username.help}]]
    </small>
</div>

<!-- ARIA attributes with Thymeleaf -->
<button type="button"
        th:attr="aria-expanded=${isExpanded ? 'true' : 'false'},
                data-item-id=${item.id}"
        class="toggle-btn">
    [[${isExpanded ? 'Collapse' : 'Expand'}]]
</button>

<!-- Bind event in a separate JS file (CSP-safe): -->
<!-- document.querySelectorAll('.toggle-btn').forEach(btn => {
         btn.addEventListener('click', () => toggleExpansion(btn.dataset.itemId));
     }); -->

<!-- Screen reader only text -->
<span class="sr-only" th:text="#{sr.current.page}">Current page:</span>
<span th:text="${currentPage}">1</span>

<!-- Accessible error messages -->
<div th:if="${#fields.hasErrors('email')}" 
     role="alert" 
     aria-live="polite">
    <span th:errors="*{email}" class="error-message"></span>
</div>

<!-- Semantic HTML with Thymeleaf -->
<nav th:fragment="main-navigation" role="navigation" aria-label="Main Navigation">
    <ul>
        <li th:each="item : ${navItems}">
            <a th:href="@{${item.url}}" 
               th:text="${item.label}"
               th:classappend="${item.current ? 'active' : ''}"
               th:attr="aria-current=${item.current ? 'page' : null}">
                Link
            </a>
        </li>
    </ul>
</nav>
```

### 15.14 Common Pitfalls & Debugging

**Common Issues and Solutions**

| Issue | Cause | Solution |
|-------|-------|----------|
| **Template not found** | Wrong path or suffix | Check `spring.thymeleaf.prefix` and `.suffix` |
| **Expressions not evaluated** | Missing `th:` namespace | Add `xmlns:th="http://www.thymeleaf.org"` |
| **NullPointerException** | Missing null checks | Use `?.` safe navigation or `th:if` |
| **CSRF token missing** | Form without `th:action` | Add `th:action` or manually include CSRF token |
| **Fragment not rendering** | Wrong fragment reference | Check fragment name and file location |
| **Cached template changes** | Caching enabled in dev | Set `spring.thymeleaf.cache=false` |
| **Encoding issues** | Wrong charset | Set `spring.thymeleaf.encoding=UTF-8` |
| **SpEL not working** | Missing Spring context | Ensure `@Controller` returns view name |

**Debugging Techniques**

```html
<!-- 1. Debug output in templates -->
<div th:if="${debugMode}">
    <pre th:text="${#strings.toString(#ctx)}">Context</pre>
    <pre th:text="${#strings.toString(#vars)}">Variables</pre>
</div>

<!-- 2. Temporary debug attributes -->
<div th:attr="data-debug-id=${item.id}"
     data-debug-type="[[${item.getClass().getSimpleName()}]]">
    Content
</div>

<!-- 3. Conditional logging -->
<script th:inline="javascript">
    console.debug('User object:', [[${user}]]);
    /*[[# th:if="${debugMode}"]]*/
    console.debug('Debug mode enabled');
    /*[[/]]*/
</script>

<!-- 4. Template validation -->
<!-- Add to HTML for W3C validation -->
<!-- <!DOCTYPE html> declaration helps -->
<!-- Use HTML5 Shiv for legacy browsers if needed -->
```

**Spring Boot DevTools for Template Development**

```properties
# application-dev.properties
spring.devtools.livereload.enabled=true
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=file:src/main/resources/templates/
logging.level.org.thymeleaf=DEBUG
```

---

## 16. Quick Reference Card

### 16.1 Most Common Processors

| Processor | Purpose |
|---|---|
| `th:text` | Set element text content (HTML-escaped). |
| `th:utext` | Set element text content (unescaped HTML). |
| `th:value` | Set the `value` attribute of an element. |
| `th:if` | Conditionally render an element. |
| `th:unless` | Render an element if condition is false. |
| `th:each` | Iterate over a collection. |
| `th:switch` / `th:case` | Switch-case conditional rendering. |
| `th:href` | Set `href` attribute with Spring context path. |
| `th:src` | Set `src` attribute with Spring context path. |
| `th:action` | Set form action with CSRF auto-inclusion. |
| `th:object` | Bind a form to a model attribute. |
| `th:field` | Bind an input element to a model property. |
| `th:errors` | Display validation errors for a field. |
| `th:fragment` | Define a reusable fragment. |
| `th:replace` | Replace an element with a fragment. |
| `th:insert` | Insert a fragment inside an element. |
| `th:remove` | Remove an element (`all`, `tag`, `body`, etc.). |
| `th:block` | Virtual wrapper element — applies processors without outputting a tag. |
| `th:with` | Define local variables. |
| `th:classappend` | Append CSS classes dynamically. |
| `th:inline` | Enable inline processing (`text`, `javascript`). |

### 16.2 Expression Cheat Sheet

| Expression | Purpose | Example |
|---|---|---|
| `${variable}` | Model attribute access | `th:text="${user.name}"` |
| `#{key}` | i18n message | `th:text="#{app.title}"` |
| `@{/path}` | URL with context path | `th:href="@{/login}"` |
| `*{field}` | Selection variable | `th:field="*{username}"` |
| `~{tmpl :: frag}` | Fragment expression | `th:replace="~{f :: nav}"` |
| `\|literal\|` | Literal substitution | `th:text="\|Hi ${name}!\|"` |
| `? :` | Ternary operator | `${x ? 'a' : 'b'}` |
| `?:` | Elvis operator | `${value ?: 'default'}` |
| `1..5` | Range | `th:each="i : ${1..5}"` |
| `?.` | Safe navigation | `${user?.address?.city}` |
| `#strings` | String utilities | `${#strings.isEmpty(str)}` |
| `#dates` | Date formatting | `${#dates.format(date, 'dd/MM')}` |

### 16.3 Spring Security sec: Attributes

| Attribute | Purpose |
|---|---|
| `sec:authorize` | Conditional rendering based on security rules. |
| `sec:authorize-url` | Render only if user can access the given URL. |
| `sec:authentication-name` | Display the authenticated username. |
| `sec:authentication` | Access authenticated user properties. |
| `sec:requires-channel` | Channel security (HTTPS enforcement). |

---

## 17. Glossary

| Term | Definition |
|------|-----------|
| **Dialect** | A set of processors that define Thymeleaf's behavior for a specific markup language. |
| **Processor** | An attribute that modifies or generates content in a template (e.g., `th:text`, `th:each`). |
| **Expression** | Code within `${...}`, `#{...}`, `@{...}`, etc. that Thymeleaf evaluates. |
| **Fragment** | A reusable piece of template defined with `th:fragment`. |
| **Model Attribute** | Data passed from controller to template via `Model.addAttribute()`. |
| **Natural Templating** | Thymeleaf's ability to render templates as valid HTML in browsers without processing. |
| **Template Resolution** | The process of finding and loading template files. |
| **Utility Object** | Built-in objects like `#strings`, `#dates` that provide helper methods. |
| **Selection Context** | The object selected by `th:object` for use with `*{...}` expressions. |
| **Inline Processing** | Evaluating expressions directly in text using `[[...]]` syntax. |

---

## 18. Best Practices

### Template Organization
1. **Use fragments** for reusable components (headers, footers, cards)
2. **Separate concerns** - Keep logic in controllers, presentation in templates
3. **Create a fragments directory** for shared components
4. **Use layout dialects** for consistent page structure

### Performance
1. **Enable caching in production** (`spring.thymeleaf.cache=true`)
2. **Avoid complex logic in templates** - Move to controllers/services
3. **Minimize nested iterations** (`th:each` inside `th:each`)
4. **Use `th:remove`** for debug-only content

### Security
1. **Prefer `th:text` over `th:utext`** to prevent XSS
2. **Always use `th:action`** for CSRF protection
3. **Validate user input** in controllers before templates
4. **Use Spring Security dialect** for authorization checks

### Maintainability
1. **Use meaningful fragment names** and organize by feature
2. **Keep templates focused** - One template per view responsibility
3. **Comment complex template logic** with `<!-- /* */ -->`
4. **Use constants for repeated strings** via `@{}` or message properties

### Internationalization
1. **Externalize all text** to message properties files
2. **Provide fallback messages** in default properties file
3. **Test with multiple locales** during development
4. **Consider RTL languages** in CSS planning

### Accessibility
1. **Use semantic HTML elements** with Thymeleaf
2. **Include ARIA attributes** where appropriate
3. **Ensure proper label associations** in forms
4. **Test with screen readers**

---

## 19. Troubleshooting FAQ

### Q1: Why is my Thymeleaf template not rendering?
**A:** Check:
- Template location: `src/main/resources/templates/`
- File extension: `.html`
- Controller returns correct view name
- `spring.thymeleaf.prefix` and `.suffix` settings
- No caching in development: `spring.thymeleaf.cache=false`

### Q2: Why are my expressions `${...}` not evaluated?
**A:** Ensure:
- HTML has `xmlns:th="http://www.thymeleaf.org"`
- Model attributes are correctly added in controller
- Expression syntax is correct
- No typos in attribute names

### Q3: How do I debug Thymeleaf templates?
**A:** Use:
- `th:attr="data-debug=value"` for attribute debugging
- `<pre th:text="${#strings.toString(#vars)}">` to see all variables
- Spring Boot DevTools for live reload
- `logging.level.org.thymeleaf=DEBUG` in application.properties

### Q4: Why is my fragment not found?
**A:** Verify:
- Fragment file exists and is accessible
- Fragment name matches exactly (case-sensitive)
- Correct syntax: `~{file :: fragment}`
- File is in template resolver's search path

### Q5: How to handle null values gracefully?
**A:** Use:
- Safe navigation: `user?.address?.city`
- Elvis operator: `value ?: 'default'`
- `th:if` conditions: `th:if="${value != null}"`
- `#strings.defaultString(value, 'fallback')`

### Q6: CSRF token missing in forms?
**A:** Solutions:
- Use `th:action` instead of plain `action` attribute
- Manually add: `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />`
- Ensure Spring Security is configured

### Q7: How to improve template performance?
**A:** Optimize by:
- Enabling cache in production
- Reducing complex expressions in loops
- Using fragments for repeated content
- Flattening data structures in controller

### Q8: Internationalization not working?
**A:** Check:
- Message files in `src/main/resources/`
- Correct naming: `messages_en.properties`
- Locale resolver configuration
- Message key references in templates

### Q9: Form binding not working?
**A:** Verify:
- `th:object` points to correct model attribute
- `th:field` matches object property names
- Form method matches controller expectation
- No conflicting `name` attributes

### Q10: How to test Thymeleaf templates?
**A:** Use:
- `@SpringBootTest` with `@AutoConfigureMockMvc`
- `mockMvc.perform()` for integration tests
- `TestRestTemplate` for full stack testing
- Fragment-specific test endpoints

---

*This comprehensive reference journal covers Thymeleaf 3.x with Spring Boot 3.x. For the latest updates, always check the official [Thymeleaf documentation](https://www.thymeleaf.org/documentation.html) and [Spring Boot reference](https://docs.spring.io/spring-boot/docs/current/reference/html/).*
