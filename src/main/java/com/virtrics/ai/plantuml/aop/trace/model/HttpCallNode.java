package com.virtrics.ai.plantuml.aop.trace.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Trace node representing an HTTP/REST API call.
 *
 * <p>Captures: - HTTP method (GET, POST, etc.) - Target URL - Request/response headers -
 * Request/response bodies - HTTP status code
 *
 * <p>PlantUML Rendering: - Generates call to external API with nested activation - Shows HTTP
 * method and path - Adds note with status code and duration
 */
public class HttpCallNode extends TraceNode {
  private String method; // GET, POST, PUT, DELETE, etc.
  private String url;
  private Map<String, String> requestHeaders;
  private String requestBody;
  private int statusCode;
  private String responseBody;
  private Map<String, String> responseHeaders;

  /**
   * Creates a new HTTP call node.
   *
   * @param method HTTP method (GET, POST, etc.)
   * @param url Target URL
   */
  public HttpCallNode(String method, String url) {
    super();
    this.type = TraceNodeType.HTTP_CALL;
    this.method = method != null ? method.toUpperCase() : "UNKNOWN";
    this.url = url;
    this.participantName = sanitizeForPlantUML(extractHostname(url));
    this.displayLabel = this.method + " " + extractPath(url);
    this.requestHeaders = new HashMap<>();
    this.responseHeaders = new HashMap<>();
  }

  /**
   * Extracts hostname from URL for participant name.
   *
   * @param url URL string
   * @return Hostname or "ExternalAPI" if parsing fails
   */
  private String extractHostname(String url) {
    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      if (host != null) {
        // Replace dots with underscores for PlantUML compatibility
        return host.replace(".", "_").replace("-", "_");
      }
    } catch (URISyntaxException e) {
      // Ignore
    }
    return "ExternalAPI";
  }

  /**
   * Extracts path from URL for display.
   *
   * @param url URL string
   * @return Path or full URL if parsing fails
   */
  private String extractPath(String url) {
    try {
      URI uri = new URI(url);
      String path = uri.getPath();
      if (path != null && !path.isEmpty()) {
        // Truncate long paths
        if (path.length() > 50) {
          return path.substring(0, 47) + "...";
        }
        return path;
      }
    } catch (URISyntaxException e) {
      // Ignore
    }
    return url;
  }

  @Override
  public String toPlantUMLFragment(String parentParticipant) {
    StringBuilder sb = new StringBuilder();

    // HTTP request arrow
    sb.append(parentParticipant)
        .append(" -> ")
        .append(participantName)
        .append(" : ")
        .append(method)
        .append(" ")
        .append(sanitizeForPlantUML(extractPath(url)))
        .append("\\n[HTTP Request]");

    return sb.toString();
  }

  /**
   * Generates PlantUML activation block with details.
   *
   * @return PlantUML activation syntax
   */
  public String toPlantUMLActivation() {
    StringBuilder sb = new StringBuilder();

    // Activate with color
    sb.append("activate ").append(participantName).append(" #LightBlue\n");

    // Add note with details
    sb.append("note right of ").append(participantName).append("\n");
    sb.append("  Status: ").append(statusCode).append("\n");
    sb.append("  Duration: ").append(getDuration()).append("ms\n");

    // Truncate response body if present
    if (responseBody != null && !responseBody.isEmpty()) {
      String truncated =
          responseBody.length() > 100 ? responseBody.substring(0, 97) + "..." : responseBody;
      sb.append("  Response: ").append(sanitizeForPlantUML(truncated)).append("\n");
    }

    sb.append("end note");

    return sb.toString();
  }

  /**
   * Generates PlantUML return statement.
   *
   * @param parentParticipant Caller participant
   * @return PlantUML return syntax
   */
  public String toPlantUMLReturn(String parentParticipant) {
    return participantName + " --> " + parentParticipant + " : HTTP " + statusCode;
  }

  /**
   * Generates PlantUML deactivation.
   *
   * @return PlantUML deactivation syntax
   */
  public String toPlantUMLDeactivation() {
    return "deactivate " + participantName;
  }

  @Override
  public boolean isExternalBoundary() {
    // HTTP calls are external boundaries
    return true;
  }

  // Getters and Setters

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map<String, String> getRequestHeaders() {
    return requestHeaders;
  }

  public void setRequestHeaders(Map<String, String> requestHeaders) {
    this.requestHeaders = requestHeaders;
  }

  public String getRequestBody() {
    return requestBody;
  }

  public void setRequestBody(String requestBody) {
    this.requestBody = requestBody;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }

  public Map<String, String> getResponseHeaders() {
    return responseHeaders;
  }

  public void setResponseHeaders(Map<String, String> responseHeaders) {
    this.responseHeaders = responseHeaders;
  }

  @Override
  public String toString() {
    return String.format(
        "HttpCallNode[%s %s, status=%d, duration=%dms]",
        method, extractPath(url), statusCode, getDuration());
  }
}
