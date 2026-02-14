package com.virtrics.ai.plantuml.aop.trace.model;

/**
 * Enumeration of trace node types for polymorphic trace tree representation.
 *
 * <p>Each type corresponds to a different kind of operation that can be traced: - METHOD_CALL:
 * Standard Java method invocation - HTTP_CALL: External HTTP/REST API call - SQL_QUERY: Database
 * query execution - MESSAGE_SEND: Message queue publishing (RabbitMQ, etc.)
 */
public enum TraceNodeType {
  /** Represents a Java method call (service method, DAO method, etc.) */
  METHOD_CALL,

  /** Represents an HTTP request to an external API */
  HTTP_CALL,

  /** Represents a SQL query execution against a database */
  SQL_QUERY,

  /** Represents a message being sent to a message broker */
  MESSAGE_SEND,

  /** Represents a message being received from a message broker */
  MESSAGE_RECEIVE
}
