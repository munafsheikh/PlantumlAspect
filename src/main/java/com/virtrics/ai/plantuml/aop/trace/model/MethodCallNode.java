package com.virtrics.ai.plantuml.aop.trace.model;

/**
 * Trace node representing a Java method invocation.
 *
 * <p>Captures: - Class name and method name - Parameter types and values (optional) - Return value
 * (optional) - Whether the method is an interface method
 *
 * <p>PlantUML Rendering: - Generates standard sequence diagram call: "Caller -> Target :
 * methodName()" - Generates activation/deactivation boxes - Shows return value if captured
 */
public class MethodCallNode extends TraceNode {
  private String className;
  private String methodName;
  private String[] parameterTypes;
  private Object[] arguments;
  private Object returnValue;
  private boolean isInterfaceMethod;

  /**
   * Creates a new method call node.
   *
   * @param className Fully qualified class name
   * @param methodName Method name
   * @param parameterTypes Array of parameter type names
   */
  public MethodCallNode(String className, String methodName, String[] parameterTypes) {
    super();
    this.type = TraceNodeType.METHOD_CALL;
    this.className = className;
    this.methodName = methodName;
    this.parameterTypes = parameterTypes != null ? parameterTypes : new String[0];
    this.participantName = sanitizeForPlantUML(extractShortClassName(className));
    this.displayLabel = buildDisplayLabel();
    this.isInterfaceMethod = false;
  }

  /**
   * Builds a human-readable display label for this method call.
   *
   * @return Display label string
   */
  private String buildDisplayLabel() {
    StringBuilder sb = new StringBuilder();
    sb.append(methodName);
    sb.append("(");

    if (parameterTypes != null && parameterTypes.length > 0) {
      for (int i = 0; i < parameterTypes.length; i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(extractShortClassName(parameterTypes[i]));
      }
    }

    sb.append(")");
    return sb.toString();
  }

  @Override
  public String toPlantUMLFragment(String parentParticipant) {
    StringBuilder sb = new StringBuilder();

    // Method call arrow
    sb.append(parentParticipant)
        .append(" -> ")
        .append(participantName)
        .append(" : ")
        .append(sanitizeForPlantUML(displayLabel));

    // Add exception indicator if present
    if (hasException()) {
      sb.append(" [ERROR]");
    }

    return sb.toString();
  }

  @Override
  public boolean isExternalBoundary() {
    // Method calls are not external boundaries (they're internal to the application)
    return false;
  }

  /**
   * Generates PlantUML for the method return.
   *
   * @param parentParticipant The participant to return to
   * @return PlantUML return statement
   */
  public String toPlantUMLReturn(String parentParticipant) {
    StringBuilder sb = new StringBuilder();

    sb.append(participantName).append(" --> ").append(parentParticipant);

    // Add return value if captured
    if (returnValue != null) {
      String returnStr = formatReturnValue(returnValue);
      if (returnStr != null && !returnStr.isEmpty()) {
        sb.append(" : ").append(sanitizeForPlantUML(returnStr));
      }
    } else if (hasException()) {
      sb.append(" : **EXCEPTION**");
    }

    return sb.toString();
  }

  /**
   * Formats a return value for display in PlantUML.
   *
   * @param value The return value object
   * @return Formatted string representation
   */
  private String formatReturnValue(Object value) {
    if (value == null) {
      return "null";
    }

    String className = value.getClass().getSimpleName();
    String valueStr = value.toString();

    // Truncate long values
    if (valueStr.length() > 50) {
      valueStr = valueStr.substring(0, 47) + "...";
    }

    // For collections, just show type and size
    if (value instanceof java.util.Collection) {
      return className + "[" + ((java.util.Collection<?>) value).size() + "]";
    }

    // For strings, show with quotes
    if (value instanceof String) {
      return "\"" + valueStr + "\"";
    }

    // For primitives and simple types
    if (className.equals("Integer")
        || className.equals("Long")
        || className.equals("Boolean")
        || className.equals("Double")) {
      return valueStr;
    }

    // Default: show type and toString
    return className + "{" + valueStr + "}";
  }

  // Getters and Setters

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public String[] getParameterTypes() {
    return parameterTypes;
  }

  public void setParameterTypes(String[] parameterTypes) {
    this.parameterTypes = parameterTypes;
  }

  public Object[] getArguments() {
    return arguments;
  }

  public void setArguments(Object[] arguments) {
    this.arguments = arguments;
  }

  public Object getReturnValue() {
    return returnValue;
  }

  public void setReturnValue(Object returnValue) {
    this.returnValue = returnValue;
  }

  public boolean isInterfaceMethod() {
    return isInterfaceMethod;
  }

  public void setInterfaceMethod(boolean interfaceMethod) {
    isInterfaceMethod = interfaceMethod;
  }

  @Override
  public String toString() {
    return String.format(
        "MethodCallNode[%s.%s, duration=%dms, children=%d]",
        extractShortClassName(className),
        methodName,
        getDuration(),
        hasChildren() ? getChildren().size() : 0);
  }
}
