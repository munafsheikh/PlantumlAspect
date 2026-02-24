package com.virtrics.ai.plantuml.aop.trace.config;

import com.virtrics.ai.plantuml.aop.trace.model.MethodCallNode;
import com.virtrics.ai.plantuml.aop.trace.model.TraceNode;
import com.virtrics.ai.plantuml.aop.trace.model.TraceNodeType;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Configuration for filtering trace nodes based on blacklist patterns.
 *
 * <p>Provides blacklist regex patterns for: - Class names to exclude (e.g., framework classes, test
 * utilities) - Method names to exclude (e.g., toString, equals, getters/setters)
 *
 * <p>Filtering reduces noise in generated diagrams by excluding common framework and utility calls
 * that don't add meaningful information.
 */
public class TraceFilterConfig {
  private final Pattern classBlacklistPattern;
  private final Pattern methodBlacklistPattern;
  private final Set<String> externalBoundaries;

  /** Default constructor with standard blacklist patterns. */
  public TraceFilterConfig() {
    this(
        ".*(test|base|springframework|jakarta|apache|lombok|gson|jwt|slf4j|mockito|cglib|\\$\\$).*",
        ".*(toString|equals|hashCode|clone|finalize|wait|notify|get|set|is).*");
  }

  /**
   * Constructor with custom blacklist patterns.
   *
   * @param classBlacklistRegex Regex pattern for class names to exclude
   * @param methodBlacklistRegex Regex pattern for method names to exclude
   */
  public TraceFilterConfig(String classBlacklistRegex, String methodBlacklistRegex) {
    this.classBlacklistPattern = Pattern.compile(classBlacklistRegex);
    this.methodBlacklistPattern = Pattern.compile(methodBlacklistRegex);
    this.externalBoundaries = Set.of("Database", "MessageBroker");
  }

  /**
   * Returns a default filter configuration with standard blacklists.
   *
   * @return Default TraceFilterConfig instance
   */
  public static TraceFilterConfig getDefault() {
    return new TraceFilterConfig();
  }

  /**
   * Determines if a trace node should be included in the trace.
   *
   * @param node The node to check
   * @return true if the node should be traced, false if it should be filtered out
   */
  public boolean shouldTrace(TraceNode node) {
    // Always trace external boundaries (HTTP, SQL, messaging)
    if (node.isExternalBoundary()) {
      return true;
    }

    // For method calls, check blacklists
    if (node.getType() == TraceNodeType.METHOD_CALL) {
      MethodCallNode methodNode = (MethodCallNode) node;
      return !isBlacklisted(methodNode.getClassName(), methodNode.getMethodName());
    }

    // Trace all other node types by default
    return true;
  }

  /**
   * Checks if a class/method combination is blacklisted.
   *
   * @param className Fully qualified class name
   * @param methodName Method name
   * @return true if blacklisted, false otherwise
   */
  public boolean isBlacklisted(String className, String methodName) {
    if (className == null || methodName == null) {
      return false;
    }

    // Check class blacklist
    if (classBlacklistPattern.matcher(className).matches()) {
      return true;
    }

    // Check method blacklist
    if (methodBlacklistPattern.matcher(methodName).matches()) {
      return true;
    }

    return false;
  }

  /**
   * Determines if a node represents an external system boundary.
   *
   * @param node The node to check
   * @return true if this is an external boundary
   */
  public boolean isExternalBoundary(TraceNode node) {
    return node.isExternalBoundary() || externalBoundaries.contains(node.getParticipantName());
  }
}
