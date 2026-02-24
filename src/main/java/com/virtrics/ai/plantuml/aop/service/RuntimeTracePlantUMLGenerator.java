package com.virtrics.ai.plantuml.aop.service;

import com.virtrics.ai.plantuml.aop.config.PlantumlAopProperties;
import com.virtrics.ai.plantuml.aop.trace.model.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates PlantUML sequence diagrams from runtime trace trees.
 *
 * <p>This service converts the TraceNode tree collected during method execution into PlantUML
 * syntax, creating sequence diagrams with: - Method call sequences - Nested activations for HTTP
 * calls and SQL queries - Return arrows with values - Notes with timing and details
 *
 * <p>The generated diagrams show the complete call hierarchy from the @Plantuml method through all
 * nested calls, database operations, and external API calls.
 */
public class RuntimeTracePlantUMLGenerator {
  private static final Logger log = LoggerFactory.getLogger(RuntimeTracePlantUMLGenerator.class);

  private final PlantumlAopProperties properties;

  public RuntimeTracePlantUMLGenerator(PlantumlAopProperties properties) {
    this.properties = properties;
  }

  /**
   * Generates a PlantUML sequence diagram from a trace tree.
   *
   * @param tag Diagram tag for file naming
   * @param rootNode Root trace node containing the complete call tree
   * @param className Simple class name for file naming
   * @param methodName Method name for file naming
   * @return Path to the generated .puml file
   * @throws IOException If file writing fails
   */
  public String generateSequenceDiagram(
      String tag, TraceNode rootNode, String className, String methodName) throws IOException {

    log.debug(
        "[RuntimeTracePlantUMLGenerator] Generating diagram for {}.{} with {} nodes",
        className,
        methodName,
        rootNode.countNodes());

    StringBuilder puml = new StringBuilder();

    // Header
    puml.append("@startuml\n");
    puml.append("title ")
        .append(className)
        .append(".")
        .append(methodName)
        .append(" - Deep Trace\\n[Runtime Trace]\n");
    puml.append("autonumber\n");
    puml.append("skinparam sequenceMessageAlign center\n");
    puml.append("skinparam responseMessageBelowArrow true\n\n");

    // Collect all participants
    Set<String> participants = collectParticipants(rootNode);

    // Declare participants
    declareParticipants(puml, rootNode.getParticipantName(), participants);
    puml.append("\n");

    // Generate sequence calls
    puml.append("' === Sequence Diagram ===\n");
    generateSequenceCalls(puml, rootNode, null, 0);

    // Footer
    puml.append("\n@enduml\n");

    // Write to file
    String filename = buildOutputFilename(className, methodName, tag);
    return writePumlFile(filename, puml.toString());
  }

  /**
   * Recursively collects all participant names from the trace tree.
   *
   * @param node Root node to traverse
   * @return Set of unique participant names
   */
  private Set<String> collectParticipants(TraceNode node) {
    Set<String> participants = new LinkedHashSet<>();
    collectParticipantsRecursive(node, participants);
    return participants;
  }

  private void collectParticipantsRecursive(TraceNode node, Set<String> participants) {
    if (node.getParticipantName() != null) {
      participants.add(node.getParticipantName());
    }

    if (node.hasChildren()) {
      for (TraceNode child : node.getChildren()) {
        collectParticipantsRecursive(child, participants);
      }
    }
  }

  /**
   * Declares all participants at the top of the diagram.
   *
   * @param puml StringBuilder to append to
   * @param rootName Name of the root participant (the @Plantuml method's class)
   * @param participants Set of all participant names
   */
  private void declareParticipants(StringBuilder puml, String rootName, Set<String> participants) {
    // Root participant (actor in green)
    puml.append("actor ").append(rootName).append(" #green\n");

    // Other participants
    for (String participant : participants) {
      if (!participant.equals(rootName)) {
        if (participant.equals("Database")) {
          puml.append("database Database #LightGreen\n");
        } else if (participant.equals("MessageBroker")) {
          puml.append("queue MessageBroker #LightCoral\n");
        } else if (isExternalService(participant)) {
          puml.append("participant ").append(participant).append(" #LightBlue\n");
        } else {
          puml.append("participant ").append(participant).append("\n");
        }
      }
    }
  }

  /**
   * Determines if a participant name represents an external service.
   *
   * @param participantName Participant name
   * @return true if external service
   */
  private boolean isExternalService(String participantName) {
    String lower = participantName.toLowerCase();
    return lower.contains("api")
        || lower.contains("http")
        || lower.contains("rest")
        || lower.contains("external")
        || lower.matches(".*_com|.*_net|.*_org");
  }

  /**
   * Recursively generates PlantUML sequence call syntax from the trace tree.
   *
   * @param puml StringBuilder to append to
   * @param node Current trace node
   * @param caller Name of the calling participant (null for root)
   * @param depth Current recursion depth (for indentation)
   */
  private void generateSequenceCalls(StringBuilder puml, TraceNode node, String caller, int depth) {
    String indent = "  ".repeat(depth);

    if (node.getType() == TraceNodeType.METHOD_CALL) {
      MethodCallNode methodNode = (MethodCallNode) node;

      if (caller != null) {
        // Call arrow
        puml.append(indent).append(methodNode.toPlantUMLFragment(caller)).append("\n");
        puml.append(indent).append("activate ").append(node.getParticipantName()).append("\n");
      }

      // Process children (nested calls)
      if (node.hasChildren()) {
        for (TraceNode child : node.getChildren()) {
          generateSequenceCalls(puml, child, node.getParticipantName(), depth + 1);
        }
      }

      // Return arrow
      if (caller != null) {
        puml.append(indent).append(methodNode.toPlantUMLReturn(caller)).append("\n");
        puml.append(indent).append("deactivate ").append(node.getParticipantName()).append("\n");
      }

    } else if (node.getType() == TraceNodeType.HTTP_CALL) {
      HttpCallNode httpNode = (HttpCallNode) node;

      // HTTP request
      puml.append(indent).append(httpNode.toPlantUMLFragment(caller)).append("\n");
      puml.append(indent).append(httpNode.toPlantUMLActivation()).append("\n");
      puml.append(indent).append(httpNode.toPlantUMLReturn(caller)).append("\n");
      puml.append(indent).append(httpNode.toPlantUMLDeactivation()).append("\n");

    } else if (node.getType() == TraceNodeType.SQL_QUERY) {
      SqlQueryNode sqlNode = (SqlQueryNode) node;

      // SQL query
      puml.append(indent).append(sqlNode.toPlantUMLFragment(caller)).append("\n");
      puml.append(indent).append(sqlNode.toPlantUMLActivation()).append("\n");
      puml.append(indent).append(sqlNode.toPlantUMLReturn(caller)).append("\n");
      puml.append(indent).append(sqlNode.toPlantUMLDeactivation()).append("\n");

    } else if (node.getType() == TraceNodeType.MESSAGE_SEND) {
      MessageNode msgNode = (MessageNode) node;

      // Async message
      puml.append(indent).append(msgNode.toPlantUMLFragment(caller)).append("\n");
    }
  }

  /**
   * Builds the output filename for the diagram.
   *
   * @param className Simple class name
   * @param methodName Method name
   * @param tag Diagram tag
   * @return Full path to output file
   */
  private String buildOutputFilename(String className, String methodName, String tag) {
    return Path.of(
            properties.getOutputDirectory(),
            className + "_" + methodName + "_" + tag + "_DeepTrace_SD.puml")
        .toString();
  }

  /**
   * Writes PlantUML content to a file.
   *
   * @param fileName File path
   * @param content PlantUML syntax string
   * @return Path to written file
   * @throws IOException If writing fails
   */
  private String writePumlFile(String fileName, String content) throws IOException {
    Path filePath = Path.of(fileName);
    Path parent = filePath.getParent();

    // Ensure parent directory exists
    if (parent != null) {
      Files.createDirectories(parent);
    }

    // Write file
    try (PrintWriter writer = new PrintWriter(fileName, StandardCharsets.UTF_8)) {
      writer.println(content);
    }

    log.info("[RuntimeTracePlantUMLGenerator] Wrote diagram to: {}", fileName);

    return fileName;
  }
}
