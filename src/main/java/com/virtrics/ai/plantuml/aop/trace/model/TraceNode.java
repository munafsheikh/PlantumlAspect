package com.virtrics.ai.plantuml.aop.trace.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class for all trace node types in the runtime trace tree.
 *
 * <p>A trace node represents a single operation (method call, HTTP request, SQL query, etc.) and
 * forms a tree structure where each node can have child nodes representing nested operations.
 *
 * <p>Design Philosophy: - Polymorphic: Different node types (MethodCallNode, HttpCallNode, etc.)
 * extend this base - Tree Structure: Each node can have multiple children, forming the call
 * hierarchy - Time Tracking: Start and end times for duration calculation - PlantUML Generation:
 * Each node knows how to render itself as PlantUML syntax
 */
public abstract class TraceNode {
  protected String nodeId;
  protected TraceNodeType type;
  protected long startTime;
  protected long endTime;
  protected List<TraceNode> children;
  protected String participantName; // PlantUML participant identifier
  protected String displayLabel; // Human-readable label for diagram
  protected Throwable exception; // If operation threw an exception

  /** Default constructor initializing common fields. */
  protected TraceNode() {
    this.nodeId = UUID.randomUUID().toString();
    this.startTime = System.currentTimeMillis();
    this.children = new ArrayList<>();
  }

  /**
   * Converts this node to a PlantUML sequence diagram fragment. Each concrete subclass implements
   * its own rendering logic.
   *
   * @param parentParticipant The participant name of the caller
   * @return PlantUML syntax string
   */
  public abstract String toPlantUMLFragment(String parentParticipant);

  /**
   * Determines if this node represents an external system boundary (database, external API, message
   * broker).
   *
   * @return true if this is an external boundary node
   */
  public abstract boolean isExternalBoundary();

  /**
   * Adds a child node to this node's children list.
   *
   * @param child The child node to add
   */
  public void addChild(TraceNode child) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(child);
  }

  /**
   * Returns the duration of this operation in milliseconds.
   *
   * @return Duration in ms, or 0 if endTime not set
   */
  public long getDuration() {
    if (endTime == 0) {
      return 0;
    }
    return endTime - startTime;
  }

  /**
   * Recursively counts total nodes in the subtree rooted at this node.
   *
   * @return Total node count including this node and all descendants
   */
  public int countNodes() {
    int count = 1; // This node
    if (children != null) {
      for (TraceNode child : children) {
        count += child.countNodes();
      }
    }
    return count;
  }

  // Getters and setters

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public TraceNodeType getType() {
    return type;
  }

  public void setType(TraceNodeType type) {
    this.type = type;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public List<TraceNode> getChildren() {
    return children != null ? Collections.unmodifiableList(children) : Collections.emptyList();
  }

  public boolean hasChildren() {
    return children != null && !children.isEmpty();
  }

  public String getParticipantName() {
    return participantName;
  }

  public void setParticipantName(String participantName) {
    this.participantName = participantName;
  }

  public String getDisplayLabel() {
    return displayLabel;
  }

  public void setDisplayLabel(String displayLabel) {
    this.displayLabel = displayLabel;
  }

  public Throwable getException() {
    return exception;
  }

  public void setException(Throwable exception) {
    this.exception = exception;
  }

  public boolean hasException() {
    return exception != null;
  }

  /**
   * Helper method to extract short class name from fully qualified name.
   *
   * @param fqn Fully qualified class name
   * @return Simple class name
   */
  protected String extractShortClassName(String fqn) {
    if (fqn == null || fqn.isEmpty()) {
      return "Unknown";
    }
    int lastDot = fqn.lastIndexOf('.');
    return lastDot >= 0 ? fqn.substring(lastDot + 1) : fqn;
  }

  /**
   * Sanitizes a string for PlantUML (removes special characters).
   *
   * @param input Input string
   * @return Sanitized string safe for PlantUML
   */
  protected String sanitizeForPlantUML(String input) {
    if (input == null) {
      return "";
    }
    return input
        .replace("$", "_")
        .replace("<", "[")
        .replace(">", "]")
        .replaceAll("\\s+", " ")
        .trim();
  }

  @Override
  public String toString() {
    return String.format(
        "%s[%s: %s, duration=%dms, children=%d]",
        getClass().getSimpleName(),
        type,
        displayLabel,
        getDuration(),
        children != null ? children.size() : 0);
  }
}
