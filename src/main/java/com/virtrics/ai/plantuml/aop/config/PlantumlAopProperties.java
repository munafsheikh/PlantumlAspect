package com.virtrics.ai.plantuml.aop.config;

public class PlantumlAopProperties {
  private boolean enabled = false;
  private String outputDirectory = "target/plantuml-diagrams";
  private String classBlacklistRegexp =
      ".*(test|base|theokanning|springframework|jakarta|apache|lombok|gson|jwt|slf4j|mockito|build|Builder|BaseRestClient|cglib|\\$\\$).*";
  private String methodBlacklistRegexp =
      ".*(toString|theokanning|equals|void|hashCode|clone|finalize|wait|notify|build|Headers|notifyAll|get|set|is).*";
  private boolean squashSubclasses;

  // Deep tracing properties
  private boolean deepTraceEnabled = true;
  private int maxTraceDepth = 100; // Prevent infinite recursion
  private boolean captureArguments = false; // Privacy/security concern
  private boolean captureReturnValues = false;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public String getClassBlacklistRegexp() {
    return classBlacklistRegexp;
  }

  public void setClassBlacklistRegexp(String classBlacklistRegexp) {
    this.classBlacklistRegexp = classBlacklistRegexp;
  }

  public String getMethodBlacklistRegexp() {
    return methodBlacklistRegexp;
  }

  public void setMethodBlacklistRegexp(String methodBlacklistRegexp) {
    this.methodBlacklistRegexp = methodBlacklistRegexp;
  }

  public boolean isSquashSubclasses() {
    return squashSubclasses;
  }

  public void setSquashSubclasses(boolean squashSubclasses) {
    this.squashSubclasses = squashSubclasses;
  }

  public boolean isDeepTraceEnabled() {
    return deepTraceEnabled;
  }

  public void setDeepTraceEnabled(boolean deepTraceEnabled) {
    this.deepTraceEnabled = deepTraceEnabled;
  }

  public int getMaxTraceDepth() {
    return maxTraceDepth;
  }

  public void setMaxTraceDepth(int maxTraceDepth) {
    this.maxTraceDepth = maxTraceDepth;
  }

  public boolean isCaptureArguments() {
    return captureArguments;
  }

  public void setCaptureArguments(boolean captureArguments) {
    this.captureArguments = captureArguments;
  }

  public boolean isCaptureReturnValues() {
    return captureReturnValues;
  }

  public void setCaptureReturnValues(boolean captureReturnValues) {
    this.captureReturnValues = captureReturnValues;
  }
}
