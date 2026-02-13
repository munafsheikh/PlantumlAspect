package com.virtrics.ai.plantuml.aop.config;

import com.virtrics.ai.plantuml.aop.aspect.PlantUmlAspect;
import com.virtrics.ai.plantuml.aop.service.PlantUMLDiagramService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
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
  public PlantUmlAspect plantUmlAspect(PlantUMLDiagramService diagramService) {
    return new PlantUmlAspect(diagramService);
  }
}
