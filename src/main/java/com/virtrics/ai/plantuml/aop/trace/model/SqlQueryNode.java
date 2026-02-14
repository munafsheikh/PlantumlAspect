package com.virtrics.ai.plantuml.aop.trace.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Trace node representing a SQL query execution.
 *
 * <p>Captures: - SQL statement text - Operation type (SELECT, INSERT, UPDATE, DELETE) - Target
 * table name - Row count affected/returned
 *
 * <p>PlantUML Rendering: - Generates call to Database participant - Shows operation and table name
 * - Adds note with SQL text and row count
 */
public class SqlQueryNode extends TraceNode {
  private String sql;
  private String operation; // SELECT, INSERT, UPDATE, DELETE, etc.
  private String tableName;
  private long rowCount;

  private static final Pattern TABLE_PATTERN =
      Pattern.compile("(?:FROM|INTO|UPDATE|JOIN)\\s+([\\w.]+)", Pattern.CASE_INSENSITIVE);

  /**
   * Creates a new SQL query node.
   *
   * @param sql The SQL statement text
   */
  public SqlQueryNode(String sql) {
    super();
    this.type = TraceNodeType.SQL_QUERY;
    this.sql = sql;
    this.operation = extractOperation(sql);
    this.tableName = extractTableName(sql);
    this.participantName = "Database";
    this.displayLabel = operation + " " + tableName;
    this.rowCount = 0;
  }

  /**
   * Extracts the SQL operation type from the statement.
   *
   * @param sql SQL statement
   * @return Operation type (SELECT, INSERT, etc.)
   */
  private String extractOperation(String sql) {
    if (sql == null || sql.isEmpty()) {
      return "QUERY";
    }

    String upper = sql.trim().toUpperCase();
    if (upper.startsWith("SELECT")) return "SELECT";
    if (upper.startsWith("INSERT")) return "INSERT";
    if (upper.startsWith("UPDATE")) return "UPDATE";
    if (upper.startsWith("DELETE")) return "DELETE";
    if (upper.startsWith("MERGE")) return "MERGE";
    if (upper.startsWith("CALL")) return "CALL";

    return "QUERY";
  }

  /**
   * Extracts the primary table name from the SQL statement.
   *
   * @param sql SQL statement
   * @return Table name or "TABLE" if not found
   */
  private String extractTableName(String sql) {
    if (sql == null || sql.isEmpty()) {
      return "TABLE";
    }

    Matcher matcher = TABLE_PATTERN.matcher(sql);
    if (matcher.find()) {
      String tableName = matcher.group(1);
      // Extract just the table name (remove schema prefix if present)
      int dotIndex = tableName.lastIndexOf('.');
      if (dotIndex >= 0) {
        tableName = tableName.substring(dotIndex + 1);
      }
      return tableName;
    }

    return "TABLE";
  }

  /**
   * Truncates SQL for display.
   *
   * @param sql SQL statement
   * @return Truncated SQL
   */
  private String truncateSql(String sql) {
    if (sql == null) {
      return "";
    }
    if (sql.length() <= 100) {
      return sql;
    }
    return sql.substring(0, 97) + "...";
  }

  @Override
  public String toPlantUMLFragment(String parentParticipant) {
    StringBuilder sb = new StringBuilder();

    // SQL query arrow
    sb.append(parentParticipant)
        .append(" -> Database : ")
        .append(operation)
        .append(" ")
        .append(tableName)
        .append("\\n[SQL Query]");

    return sb.toString();
  }

  /**
   * Generates PlantUML activation block with SQL details.
   *
   * @return PlantUML activation syntax
   */
  public String toPlantUMLActivation() {
    StringBuilder sb = new StringBuilder();

    // Activate with color
    sb.append("activate Database #LightGreen\n");

    // Add note with SQL details
    sb.append("note right of Database\n");
    sb.append("  SQL: ").append(sanitizeForPlantUML(truncateSql(sql))).append("\n");
    sb.append("  Rows: ").append(rowCount).append("\n");
    sb.append("  Duration: ").append(getDuration()).append("ms\n");
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
    return "Database --> " + parentParticipant + " : Result Set [" + rowCount + " rows]";
  }

  /**
   * Generates PlantUML deactivation.
   *
   * @return PlantUML deactivation syntax
   */
  public String toPlantUMLDeactivation() {
    return "deactivate Database";
  }

  @Override
  public boolean isExternalBoundary() {
    // SQL queries are external boundaries
    return true;
  }

  // Getters and Setters

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public long getRowCount() {
    return rowCount;
  }

  public void setRowCount(long rowCount) {
    this.rowCount = rowCount;
  }

  @Override
  public String toString() {
    return String.format(
        "SqlQueryNode[%s %s, rows=%d, duration=%dms]",
        operation, tableName, rowCount, getDuration());
  }
}
