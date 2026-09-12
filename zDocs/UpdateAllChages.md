Bilkul bhai. **Pura issue ka simple but detailed summary** ye hai:

# 🔴 Actual Error Kya Tha?

Swagger UI open karte time tumhe pehle ye errors dikh rahe the:

```text
Failed to load resource: 403
```

aur:

```text
MIME type ('text/html') is not executable
```

Isse laga ki **Spring Security/JWT Swagger files ko block kar raha hai**.

Lekin baad mein tumhare backend log ne actual root cause reveal kiya:

```text
java.lang.NoSuchMethodError:
'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'
```

Aur:

```text
/v3/api-docs → 500
```

## 🎯 Root cause

**Spring Boot / Spring Framework aur Springdoc Swagger ki versions compatible nahi hain.**

Yaani problem primarily:

```text
Spring Boot
     ↓
Spring Framework
     ↓
springdoc-openapi
```

in versions ke mismatch ki hai.

---

# 🧠 Error kaise ho raha hai?

Swagger UI directly tumhare APIs ko understand nahi karta.

Uska flow roughly:

```text
Browser
   ↓
/derp_docs
   ↓
Swagger UI
   ↓
/v3/api-docs
   ↓
springdoc
   ↓
Spring Controllers scan
   ↓
OpenAPI JSON generate
```

Tumhare case mein `/v3/api-docs` generate karte waqt springdoc internally Spring Framework ki class:

```text
ControllerAdviceBean
```

ko use kar raha hai.

Lekin jo constructor springdoc expect kar raha hai:

```java
ControllerAdviceBean(Object)
```

tumhare installed Spring Framework version mein available nahi hai.

Isliye JVM bolta hai:

```text
NoSuchMethodError
```

### Iska meaning:

> "Code compile/runtime mein jis method/constructor ko call karna chahta hai, installed library mein woh method/constructor hai hi nahi."

---

# 🔥 Important: `NoSuchMethodError` kya indicate karta hai?

Ye normally **tumhare Java code ka normal coding error nahi hota**.

Example:

Springdoc ko laga:

```java
new ControllerAdviceBean(something);
```

available hai.

Lekin actual Spring library mein woh constructor change/remove ho chuka hai.

So:

```text
springdoc version
       ❌
       ↓
expects old/different Spring API

Spring Boot version
       ↓
provides another Spring API
```

Therefore:

```text
NoSuchMethodError
```

---

# ❌ Security class problem nahi hai

Tumhari Security mein ye already hai:

```java
.requestMatchers(
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/v3/api-docs",
    "/webjars/**"
).permitAll()
```

Matlab Swagger ke important endpoints ko public access diya hua hai.

So **current 500 error ke liye Security class ko change karna solution nahi hai.**

---

# ❌ JwtFilter bhi main problem nahi hai

Tumhara `JwtFilter`:

```java
filterChain.doFilter(request, response);
```

call kar raha hai.

Matlab filter request ko automatically:

```text
403
```

nahi kar raha.

Aur jo actual log mila:

```text
NoSuchMethodError
```

woh Swagger/OpenAPI generation ke andar aa raha hai.

Therefore:

```text
JWT ❌
Authentication ❌
Authorization ❌
Controller ❌
Swagger dependency compatibility ✅ ← MAIN PROBLEM
```

---

# 🟡 Phir 403 aur MIME error kyu dikha?

Ye thoda confusing part hai.

Browser Swagger ki CSS/JS files request karta hai.

For example:

```text
/swagger-ui/swagger-ui.css
/swagger-ui/swagger-ui-bundle.js
```

Agar server response deta hai:

```text
403 Forbidden
```

to browser ko expected JavaScript/CSS nahi milti.

Instead response kuch HTML/error response ho sakta hai.

Browser phir bolta hai:

```text
Expected JavaScript
but received text/html
```

Isliye:

```text
MIME type ('text/html') is not executable
```

### Therefore:

```text
MIME error
     ↓
secondary symptom

403
     ↓
could be security/routing-related

/v3/api-docs = 500
     ↓
actual confirmed backend problem
     ↓
NoSuchMethodError
     ↓
dependency version mismatch
```

---

# 🛠️ Kya change karna hai?

## 1. `pom.xml` mein springdoc version check karo

Tum likely kuch aisa use kar rahe ho:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>...</version>
</dependency>
```

**Yahi version important hai.**

Springdoc ki official compatibility information ke according Spring Boot 3.x ke different versions ke liye compatible springdoc versions alag hain. For example, Boot 3.5.x ke liye 2.8.x line recommended hai.

Agar tumhara project Boot **3.5.x** hai, safe choice:

```xml
<version>2.8.13</version>
```

So:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.13</version>
</dependency>
```

---

# ⚠️ But blindly version change mat karna

Sabse pehle `pom.xml` mein ye dekho:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>????</version>
</parent>
```

For example:

```xml
<version>3.5.3</version>
```

Then springdoc:

```xml
<version>2.5.0</version>
```

to problem obvious hai:

```text
Spring Boot 3.5.3
       +
springdoc 2.5.0
       ↓
❌ incompatible
```

There is even an official springdoc issue documenting this exact kind of `ControllerAdviceBean.<init>(Object)` failure with Spring Boot 3.5.3 and springdoc 2.5.0.

---

# 2. YAML bhi thoda correct karna hai

Tumne diya tha:

```yaml
springdoc:
  swagger-ui:
    path: ${SWAGGER_URL:/swagger}

SWAGGER_URL=/derp_docs
```

Yahan confusion hai.

Agar tum simply `/derp_docs` chahte ho, best:

```yaml
springdoc:
  swagger-ui:
    path: /derp_docs
```

Then Swagger UI:

```text
http://localhost:8080/derp_docs
```

---

## Environment variable use karna hai to

```yaml
springdoc:
  swagger-ui:
    path: ${SWAGGER_URL:/derp_docs}
```

And Windows environment variable:

```powershell
$env:SWAGGER_URL="/derp_docs"
```

**YAML ke andar**:

```yaml
SWAGGER_URL=/derp_docs
```

aise nahi likhna chahiye as an environment-variable assignment.

---

# 3. Security mein `/derp_docs` bhi permit kar sakte ho

Tumhara current:

```java
.requestMatchers(
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/v3/api-docs",
    "/webjars/**"
).permitAll()
```

is good for the underlying Swagger endpoints.

For clarity, tum custom path bhi explicitly add kar sakte ho:

```java
.requestMatchers(
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/v3/api-docs",
    "/webjars/**",
    "/derp_docs/**"
).permitAll()
```

But **important**:

### Ye change `NoSuchMethodError` fix nahi karega.

Ye sirf authorization side ko clear karega.

---

# 🔄 Final architecture after fix

Correct setup:

```text
                    Browser
                       │
                       ▼
               /derp_docs
                       │
                       ▼
                Swagger UI
                       │
                       ▼
               /v3/api-docs
                       │
                       ▼
            Springdoc OpenAPI
                       │
                       ▼
             Spring Controllers
                       │
                       ▼
              OpenAPI JSON
                       │
                       ▼
               Swagger UI
                       │
                       ▼
                API Testing
```

Dependencies:

```text
Spring Boot 3.x
       │
       ├── Spring Framework
       │
       └── springdoc compatible version
                    │
                    ▼
              Swagger/OpenAPI
```

**Versions compatible honi chahiye.**

---

# 🧹 Change karne ke baad

PowerShell mein project folder ke andar:

```powershell
mvn clean
```

then:

```powershell
mvn dependency:tree | Select-String "springdoc|spring-web|spring-context"
```

Isse hum check kar sakte hain ki multiple/conflicting versions to nahi aa rahi.

Then:

```powershell
mvn spring-boot:run
```

---

# 🧪 Testing ka correct order

Sabse pehle:

```text
http://localhost:8080/v3/api-docs
```

### Expected:

JSON output:

```json
{
  "openapi": "3.0.x",
  "info": {
    ...
  },
  "paths": {
    ...
  }
}
```

Agar **JSON aa gaya**:

```text
/v3/api-docs
     ✅
```

then:

```text
http://localhost:8080/derp_docs
```

open karo.

Agar Swagger UI aa gaya:

```text
Swagger UI
     ✅
```

then problem solved.

---

# 📌 Ek line mein pura issue

Tumhara problem:

```text
Swagger UI
   ↓
/v3/api-docs
   ↓
springdoc
   ↓
Spring Framework ke incompatible API ko call karta hai
   ↓
NoSuchMethodError
   ↓
500
   ↓
Swagger API definition load nahi hoti
```

### Main change:

**`pom.xml` mein Spring Boot version ke according compatible `springdoc-openapi-starter-webmvc-ui` version use karo.**

Aur **Security/JwtFilter ko sirf is error ko fix karne ke liye change mat karo.**
