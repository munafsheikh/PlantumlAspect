package com.virtrics.ai.plantuml.aop.aspect;

import com.virtrics.ai.plantuml.aop.annotation.DiagramType;
import com.virtrics.ai.plantuml.aop.annotation.Plantuml;
import com.virtrics.ai.plantuml.aop.service.PlantUMLDiagramService;
import com.virtrics.ai.plantuml.aop.service.RuntimeTracePlantUMLGenerator;
import com.virtrics.ai.plantuml.aop.trace.TraceContext;
import com.virtrics.ai.plantuml.aop.trace.TraceSession;
import java.lang.reflect.Method;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * THE SINGLE ENTRY POINT for PlantUML diagram generation and deep tracing.
 *
 * <p>This aspect intercepts methods annotated with @Plantuml and: 1. Starts deep runtime tracing
 * (if deepTrace=true) 2. Executes the annotated method (all interceptors collect trace data) 3.
 * Ends deep tracing and retrieves the complete call tree 4. Generates PlantUML diagrams from
 * either: - Runtime trace (deep tracing mode) - shows all layers, HTTP, SQL - Static bytecode
 * analysis (legacy mode) - limited depth 5. Returns the original method result unchanged
 *
 * <p>All other interceptors (HTTP, JDBC, method calls) are passive until this aspect activates
 * tracing via TraceContext.startTrace().
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1) // Execute late to allow trace collection
public class PlantUmlAspect {
  private static final Logger log = LoggerFactory.getLogger(PlantUmlAspect.class);

  private final PlantUMLDiagramService diagramService;
  private final RuntimeTracePlantUMLGenerator runtimeGenerator;

  public PlantUmlAspect(
      PlantUMLDiagramService diagramService, RuntimeTracePlantUMLGenerator runtimeGenerator) {
    this.diagramService = diagramService;
    this.runtimeGenerator = runtimeGenerator;
  }

  @Around("@annotation(com.virtrics.ai.plantuml.aop.annotation.Plantuml)")
  public Object generateDiagram(ProceedingJoinPoint joinPoint) throws Throwable {
    // STEP 1: Resolve annotation (existing logic)
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method interfaceMethod = signature.getMethod();
    Class<?> targetClass = joinPoint.getTarget().getClass();
    Method method = AopUtils.getMostSpecificMethod(interfaceMethod, targetClass);

    Plantuml annotation = AnnotationUtils.findAnnotation(method, Plantuml.class);
    if (annotation == null) {
      annotation = AnnotationUtils.findAnnotation(interfaceMethod, Plantuml.class);
    }

    if (annotation == null) {
      return joinPoint.proceed();
    }

    log.info("[@Plantuml] Intercepted: {}.{}", targetClass.getSimpleName(), method.getName());

    // STEP 2: Determine if deep tracing is enabled
    boolean useDeepTrace = annotation.deepTrace();
    TraceSession session = null;

    // STEP 3: START DEEP TRACING (if enabled)
    //         This activates ALL downstream interceptors
    if (useDeepTrace) {
      log.debug(
          "[@Plantuml] Starting deep trace for {}.{}",
          targetClass.getSimpleName(),
          method.getName());
      TraceContext.startTrace(targetClass.getName(), method.getName());
      session = TraceContext.getCurrentSession();
    }

    // STEP 4: Execute the annotated method
    //         All interceptors will now collect trace data (if tracing is active)
    Object result;
    try {
      result = joinPoint.proceed();
    } finally {
      // STEP 5: END DEEP TRACING
      //         Retrieve collected trace and deactivate all interceptors
      if (useDeepTrace && session != null) {
        session.finalizeSession();
        TraceContext.endTrace();
        log.debug(
            "[@Plantuml] Deep trace completed. Collected {} nodes",
            session.getRootNode().countNodes());
      }
    }

    // STEP 6: GENERATE DIAGRAMS
    try {
      for (DiagramType type : annotation.types()) {
        if (type == DiagramType.SEQUENCE) {
          String file;

          if (useDeepTrace && session != null) {
            // Use runtime trace (NEW PATH for deep tracing)
            file =
                runtimeGenerator.generateSequenceDiagram(
                    annotation.tag(),
                    session.getRootNode(),
                    targetClass.getSimpleName(),
                    method.getName());
            log.info("[@Plantuml] Generated DEEP TRACE sequence diagram: {}", file);
          } else {
            // Use existing bytecode analysis (LEGACY PATH)
            file =
                diagramService.generateSequenceDiagram(
                    annotation.tag(), targetClass, method.getName());
            log.info("[@Plantuml] Generated sequence diagram: {}", file);
          }
        } else if (type == DiagramType.CLASS) {
          String file =
              diagramService.generateClassDiagram(
                  diagramService.buildOutputFilePrefix(
                      targetClass.getSimpleName(), method.getName()),
                  annotation.tag(),
                  List.of(targetClass.getPackageName()),
                  List.of());
          log.info("[@Plantuml] Generated class diagram: {}", file);
        }
      }
    } catch (Exception e) {
      log.error(
          "[@Plantuml] Failed to generate diagram for {}.{}: {}",
          targetClass.getSimpleName(),
          method.getName(),
          e.getMessage(),
          e);
    }

    // STEP 7: Return original result (diagram generation doesn't affect business logic)
    return result;
  }
}
