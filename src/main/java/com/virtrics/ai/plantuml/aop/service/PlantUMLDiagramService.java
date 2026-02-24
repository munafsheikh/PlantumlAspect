package com.virtrics.ai.plantuml.aop.service;

import com.virtrics.ai.plantuml.aop.config.PlantumlAopProperties;
import de.elnarion.util.plantuml.generator.classdiagram.PlantUMLClassDiagramGenerator;
import de.elnarion.util.plantuml.generator.classdiagram.config.PlantUMLClassDiagramConfigBuilder;
import de.elnarion.util.plantuml.generator.sequencediagram.PlantUMLSequenceDiagramGenerator;
import de.elnarion.util.plantuml.generator.sequencediagram.config.PlantUMLSequenceDiagramConfigBuilder;
import de.elnarion.util.plantuml.generator.sequencediagram.exception.NotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.plantuml.SourceStringReader;

public class PlantUMLDiagramService {
  private final PlantumlAopProperties properties;

  public PlantUMLDiagramService(PlantumlAopProperties properties) {
    this.properties = properties;
  }

  public String generateSequenceDiagram(String tag, Class<?> clazz, String methodName)
      throws IOException, NotFoundException {
    return generateSequenceDiagram(
        tag,
        clazz,
        methodName,
        properties.getClassBlacklistRegexp(),
        properties.getMethodBlacklistRegexp());
  }

  public byte[] generateSequenceDiagramAsStream(String tag, Class<?> clazz, String methodName)
      throws IOException, NotFoundException {
    String filename =
        generateSequenceDiagram(
            tag,
            clazz,
            methodName,
            properties.getClassBlacklistRegexp(),
            properties.getMethodBlacklistRegexp());
    return convertToImage(filename);
  }

  public String generateSequenceDiagram(
      String tag,
      Class<?> clazz,
      String methodName,
      String paramClassBlacklistRegexp,
      String methodExclusionList)
      throws IOException, NotFoundException {
    PlantUMLSequenceDiagramConfigBuilder builder =
        new PlantUMLSequenceDiagramConfigBuilder(clazz.getName(), methodName)
            .withUseShortClassName(true)
            .withShowReturnTypes(true)
            .withIgnoreStandardClasses(true)
            .withClassBlacklistRegexp(paramClassBlacklistRegexp)
            .withMethodBlacklistRegexp(methodExclusionList);

    PlantUMLSequenceDiagramGenerator generator =
        new PlantUMLSequenceDiagramGenerator(builder.build());

    String generatedDiagram = generator.generateDiagramText();
    generatedDiagram = cleanup(generatedDiagram, clazz.getSimpleName());

    return writePumlFile(
        buildOutputFilePrefix(clazz.getSimpleName(), methodName) + "_" + tag + "_SD.puml",
        generatedDiagram);
  }

  private String cleanup(String generatedDiagram, String origin) {
    String tmp =
        generatedDiagram
            .trim()
            .replaceAll("\\r", "")
            .replaceAll("\\$", "_")
            .replaceAll("participant " + origin, "actor " + origin + " #green");

    String[] participants =
        Arrays.stream(tmp.split("\n"))
            .filter(s -> s.contains("participant") && s.contains("_"))
            .toArray(String[]::new);

    if (properties.isSquashSubclasses()) {
      Map<String, String> map = new HashMap<>();
      for (String p : participants) {
        String[] parts = p.split("_|");
        String[] r = p.split("");
        map.put(r[1].trim(), parts[1].trim());
      }

      for (Map.Entry<String, String> entry : map.entrySet()) {
        tmp = tmp.replaceAll(entry.getKey(), entry.getValue());
      }
    }

    return tmp;
  }

  public String generateClassDiagram(
      String filename, String tag, List<String> scanPackages, List<String> hideClasses)
      throws IOException {
    PlantUMLClassDiagramConfigBuilder configBuilder =
        new PlantUMLClassDiagramConfigBuilder(scanPackages).withHideClasses(hideClasses);

    PlantUMLClassDiagramGenerator generator =
        new PlantUMLClassDiagramGenerator(configBuilder.build());
    String generatedDiagram = generator.generateDiagramText();
    return writePumlFile(filename + "_" + tag + "_ClassDiagram.puml", generatedDiagram);
  }

  public String buildOutputFilePrefix(String classSimpleName, String methodName) {
    return Path.of(properties.getOutputDirectory(), classSimpleName + "_" + methodName).toString();
  }

  private String writePumlFile(String fileName, String generatedDiagram) throws IOException {
    ensureParentDirectoryExists(fileName);
    try (PrintWriter writer = new PrintWriter(fileName, StandardCharsets.UTF_8)) {
      writer.println(generatedDiagram);
    }
    return fileName;
  }

  private byte[] convertToImage(String filename) throws IOException {
    SourceStringReader reader = new SourceStringReader(readPumlFile(filename));
    String imageFile = filename + ".png";
    ensureParentDirectoryExists(imageFile);
    reader.generateImage(new File(imageFile));
    return Files.readAllBytes(Path.of(imageFile));
  }

  private String readPumlFile(String fileName) throws IOException {
    try (FileInputStream fis = new FileInputStream(fileName)) {
      return new String(fis.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private void ensureParentDirectoryExists(String fileName) throws IOException {
    Path parent = Path.of(fileName).getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
  }
}
