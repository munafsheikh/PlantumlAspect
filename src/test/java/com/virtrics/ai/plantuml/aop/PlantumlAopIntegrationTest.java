package com.virtrics.ai.plantuml.aop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.virtrics.ai.plantuml.aop.annotation.DiagramType;
import com.virtrics.ai.plantuml.aop.annotation.Plantuml;
import com.virtrics.ai.plantuml.aop.aspect.PlantUmlAspect;
import com.virtrics.ai.plantuml.aop.config.PlantumlAopProperties;
import com.virtrics.ai.plantuml.aop.service.PlantUMLDiagramService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

class PlantumlAopIntegrationTest {

  @Test
  void annotatedMethodsGenerateTwoClassAndTwoSequenceDiagrams(@TempDir Path tempDir)
      throws IOException {
    try (AnnotationConfigApplicationContext context = buildContext()) {
      context.getBean(PlantumlAopProperties.class).setOutputDirectory(tempDir.toString());

      DemoService service = context.getBean(DemoService.class);
      service.createInvoice("inv-1");
      service.payInvoice("inv-1");

      Path seq1 = tempDir.resolve("DemoServiceImpl_createInvoice_seqOne_SD.puml");
      Path cls1 = tempDir.resolve("DemoServiceImpl_createInvoice_seqOne_ClassDiagram.puml");
      Path seq2 = tempDir.resolve("DemoServiceImpl_payInvoice_seqTwo_SD.puml");
      Path cls2 = tempDir.resolve("DemoServiceImpl_payInvoice_seqTwo_ClassDiagram.puml");

      assertTrue(Files.exists(seq1), "first sequence diagram should exist");
      assertTrue(Files.exists(cls1), "first class diagram should exist");
      assertTrue(Files.exists(seq2), "second sequence diagram should exist");
      assertTrue(Files.exists(cls2), "second class diagram should exist");

      String seqContent = Files.readString(seq1);
      assertTrue(seqContent.contains("@startuml"), "sequence output should be PlantUML content");
    }
  }

  @Test
  void unannotatedMethodDoesNotGenerateDiagrams(@TempDir Path tempDir) throws IOException {
    try (AnnotationConfigApplicationContext context = buildContext()) {
      context.getBean(PlantumlAopProperties.class).setOutputDirectory(tempDir.toString());

      DemoService service = context.getBean(DemoService.class);
      service.healthCheck();

      try (Stream<Path> files = Files.list(tempDir)) {
        assertEquals(0, files.count(), "no diagrams should be generated for unannotated methods");
      }
    }
  }

  interface DemoService {
    String createInvoice(String id);

    String payInvoice(String id);

    String healthCheck();
  }

  static class DemoServiceImpl implements DemoService {
    @Override
    @Plantuml(
        types = {DiagramType.SEQUENCE, DiagramType.CLASS},
        tag = "seqOne")
    public String createInvoice(String id) {
      return "created-" + normalize(id);
    }

    @Override
    @Plantuml(
        types = {DiagramType.SEQUENCE, DiagramType.CLASS},
        tag = "seqTwo")
    public String payInvoice(String id) {
      return "paid-" + normalize(id);
    }

    @Override
    public String healthCheck() {
      return "ok";
    }

    private String normalize(String id) {
      assertFalse(id.isBlank());
      return id.trim().toLowerCase();
    }
  }

  private AnnotationConfigApplicationContext buildContext() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(AopConfig.class);
    context.registerBean(PlantumlAopProperties.class, PlantumlAopProperties::new);
    context.registerBean(
        PlantUMLDiagramService.class,
        () -> new PlantUMLDiagramService(context.getBean(PlantumlAopProperties.class)));
    context.registerBean(
        PlantUmlAspect.class,
        () -> new PlantUmlAspect(context.getBean(PlantUMLDiagramService.class)));
    context.registerBean(DemoService.class, DemoServiceImpl::new);
    context.refresh();
    return context;
  }

  @Configuration
  @EnableAspectJAutoProxy
  static class AopConfig {}
}
