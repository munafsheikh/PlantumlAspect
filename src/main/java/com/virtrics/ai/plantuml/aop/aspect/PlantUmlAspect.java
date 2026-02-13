package com.virtrics.ai.plantuml.aop.aspect;

import com.virtrics.ai.plantuml.aop.annotation.DiagramType;
import com.virtrics.ai.plantuml.aop.annotation.Plantuml;
import com.virtrics.ai.plantuml.aop.service.PlantUMLDiagramService;
import java.lang.reflect.Method;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

@Aspect
public class PlantUmlAspect {
  private static final Logger log = LoggerFactory.getLogger(PlantUmlAspect.class);

  private final PlantUMLDiagramService diagramService;

  public PlantUmlAspect(PlantUMLDiagramService diagramService) {
    this.diagramService = diagramService;
  }

  @Around("execution(* *(..))")
  public Object generateDiagram(ProceedingJoinPoint joinPoint) throws Throwable {
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

    Object result = joinPoint.proceed();

    try {
      for (DiagramType type : annotation.types()) {
        if (type == DiagramType.SEQUENCE) {
          String file =
              diagramService.generateSequenceDiagram(
                  annotation.tag(), targetClass, method.getName());
          log.info("[@Plantuml] Generated sequence diagram: {}", file);
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

    return result;
  }
}
