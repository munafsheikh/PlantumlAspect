package com.virtrics.ai.plantuml.aop.config;

import com.virtrics.ai.plantuml.aop.aspect.PlantUmlAspect;
import com.virtrics.ai.plantuml.aop.service.PlantUMLDiagramService;
import com.virtrics.ai.plantuml.aop.service.RuntimeTracePlantUMLGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@EnableAspectJAutoProxy
@ConditionalOnProperty(
    prefix = "plantuml.aop",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
public class PlantumlAopConfiguration {

  @Bean
  @ConfigurationProperties(prefix = "plantuml.aop")
  public PlantumlAopProperties plantumlAopProperties() {
    return new PlantumlAopProperties();
  }

  @Bean
  public PlantUMLDiagramService plantUMLDiagramService(PlantumlAopProperties properties) {
    return new PlantUMLDiagramService(properties);
  }

  @Bean
  public RuntimeTracePlantUMLGenerator runtimeTracePlantUMLGenerator(
      PlantumlAopProperties properties) {
    return new RuntimeTracePlantUMLGenerator(properties);
  }

  @Bean
  @Order(Ordered.LOWEST_PRECEDENCE - 1)
  public PlantUmlAspect plantUmlAspect(
      PlantUMLDiagramService diagramService, RuntimeTracePlantUMLGenerator runtimeGenerator) {
    return new PlantUmlAspect(diagramService, runtimeGenerator);
  }
}
