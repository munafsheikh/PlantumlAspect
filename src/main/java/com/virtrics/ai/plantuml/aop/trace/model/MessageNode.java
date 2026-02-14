package com.virtrics.ai.plantuml.aop.trace.model;

/**
 * Trace node representing a message sent to a message broker (RabbitMQ, Kafka, etc.).
 *
 * <p>Captures: - Exchange/topic name - Routing key/queue name - Message type/content type - Payload
 * (optional)
 *
 * <p>PlantUML Rendering: - Generates async message arrow (->>) - Shows exchange and routing key
 */
public class MessageNode extends TraceNode {
  private String exchange;
  private String routingKey;
  private String messageType;
  private Object payload;

  /**
   * Creates a new message node.
   *
   * @param exchange Exchange or topic name
   * @param routingKey Routing key or queue name
   * @param messageType Message content type
   */
  public MessageNode(String exchange, String routingKey, String messageType) {
    super();
    this.type = TraceNodeType.MESSAGE_SEND;
    this.exchange = exchange;
    this.routingKey = routingKey;
    this.messageType = messageType;
    this.participantName = "MessageBroker";
    this.displayLabel = "publish to " + (routingKey != null ? routingKey : exchange);
  }

  @Override
  public String toPlantUMLFragment(String parentParticipant) {
    StringBuilder sb = new StringBuilder();

    // Async message arrow
    sb.append(parentParticipant)
        .append(" ->> MessageBroker : ")
        .append(sanitizeForPlantUML(displayLabel))
        .append("\\n[")
        .append(messageType != null ? messageType : "Message")
        .append("]");

    return sb.toString();
  }

  @Override
  public boolean isExternalBoundary() {
    // Messages to brokers are external boundaries
    return true;
  }

  // Getters and Setters

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  public void setRoutingKey(String routingKey) {
    this.routingKey = routingKey;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  public Object getPayload() {
    return payload;
  }

  public void setPayload(Object payload) {
    this.payload = payload;
  }

  @Override
  public String toString() {
    return String.format("MessageNode[%s -> %s, type=%s]", exchange, routingKey, messageType);
  }
}
