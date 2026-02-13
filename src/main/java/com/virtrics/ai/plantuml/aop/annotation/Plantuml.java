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
}
