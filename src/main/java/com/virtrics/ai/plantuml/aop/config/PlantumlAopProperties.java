package com.virtrics.ai.plantuml.aop.config;

public class PlantumlAopProperties {
  private String outputDirectory = "docs";
  private String classBlacklistRegexp =
      ".*(test|base|theokanning|springframework|jakarta|apache|lombok|gson|jwt|slf4j|mockito|build|Builder|BaseRestClient).*";
  private String methodBlacklistRegexp =
      ".*(toString|theokanning|equals|void|hashCode|clone|finalize|wait|notify|build|Headers|notifyAll).*";
  private boolean squashSubclasses;

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
}
