# PlantUML AOP Bundle

`plantuml-aop-bundle` is a Spring AOP library that generates PlantUML diagrams from annotated method executions.

It supports:
- Sequence diagrams
- Class diagrams
- Auto-configuration for Spring Boot

## What It Comes With

- `@Plantuml` annotation for method-level diagram generation
- `DiagramType` enum:
  - `SEQUENCE`
  - `CLASS`
- `PlantUmlAspect` that intercepts annotated methods
- `PlantUMLDiagramService` for diagram generation and file output
- Spring Boot auto-configuration:
  - `PlantumlAopAutoConfiguration`
  - `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Optional manual enable annotation:
  - `@EnablePlantumlAop`

## Installation

### 1) Build/install locally

```bash
mvn clean install
```

### 2) Add dependency to your app

```xml
<dependency>
  <groupId>com.virtrics.ai</groupId>
  <artifactId>plantuml-aop-bundle</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Using the Library

### Option A: Spring Boot auto-configuration

If your app is Spring Boot, adding the dependency is enough.

### Option B: Manual enable

Add `@EnablePlantumlAop` on a configuration class:

```java
import com.virtrics.ai.plantuml.aop.config.EnablePlantumlAop;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnablePlantumlAop
public class PlantumlConfig {
}
```

## Annotate Methods

```java
import com.virtrics.ai.plantuml.aop.annotation.DiagramType;
import com.virtrics.ai.plantuml.aop.annotation.Plantuml;

public class PaymentService {

    @Plantuml(types = {DiagramType.SEQUENCE, DiagramType.CLASS}, tag = "paymentFlow")
    public void processPayment(String invoiceId) {
        // business logic
    }
}
```

When this method runs, the aspect generates:
- A sequence diagram (`*_SD.puml`)
- A class diagram (`*_ClassDiagram.puml`)

## Configuration

Use properties with prefix `plantuml.aop`:

```properties
plantuml.aop.output-directory=docs
plantuml.aop.class-blacklist-regexp=.*(test|base|springframework).*
plantuml.aop.method-blacklist-regexp=.*(toString|equals|hashCode).*
plantuml.aop.squash-subclasses=false
```

Available properties:
- `output-directory`: Directory where diagram files are written
- `class-blacklist-regexp`: Regex for excluding classes from sequence generation
- `method-blacklist-regexp`: Regex for excluding methods from sequence generation
- `squash-subclasses`: Collapses subclass participant names in cleanup logic

## Output Naming

Sequence diagram:
- `<output-directory>/<ClassSimpleName>_<methodName>_<tag>_SD.puml`

Class diagram:
- `<output-directory>/<ClassSimpleName>_<methodName>_<tag>_ClassDiagram.puml`

## Notes

- Diagram files are generated after the annotated method proceeds.
- Unannotated methods are not processed.
- This project also includes integration tests showing:
  - 2 sequence + 2 class diagrams generated from annotated methods
  - no files generated for unannotated methods
