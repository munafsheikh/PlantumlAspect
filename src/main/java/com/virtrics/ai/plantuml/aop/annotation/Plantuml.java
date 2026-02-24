package com.virtrics.ai.plantuml.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Plantuml {
  DiagramType[] types() default {DiagramType.SEQUENCE};

  String tag() default "auto";

  /**
   * Enable deep runtime tracing through all layers (method calls, HTTP, SQL, messaging). When true,
   * traces through interface boundaries and captures external system calls. When false, uses legacy
   * static bytecode analysis (limited depth).
   *
   * @return true to enable deep tracing (default), false for legacy mode
   */
  boolean deepTrace() default true;
}
