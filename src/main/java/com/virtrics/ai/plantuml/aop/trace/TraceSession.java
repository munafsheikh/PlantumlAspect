package com.virtrics.ai.plantuml.aop.trace;

import com.virtrics.ai.plantuml.aop.trace.config.TraceFilterConfig;
import com.virtrics.ai.plantuml.aop.trace.model.MethodCallNode;
import com.virtrics.ai.plantuml.aop.trace.model.TraceNode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single tracing session for one @Plantuml annotated method execution.
 *
 * <p>A session maintains: - A unique session ID for tracking - The root trace node (the @Plantuml
 * method itself) - A call stack tracking the current execution hierarchy - Filter configuration for
 * blacklist exclusions
 *
 * <p>Thread Safety: This class is thread-confined via ThreadLocal in TraceContext. It is NOT
 * thread-safe for concurrent access - but that's by design, as each thread has its own instance.
 */
public class TraceSession {
  private static final Logger log = LoggerFactory.getLogger(TraceSession.class);

  private final String sessionId;
  private final long startTime;
  private final TraceNode rootNode;
  private final Deque<TraceNode> callStack;
  private final TraceFilterConfig filterConfig;
  private int totalNodeCount = 0;
  private int maxDepth = 0;

  /**
   * Creates a new trace session for the given initiator method.
   *
   * @param initiatorClass Fully qualified class name of the @Plantuml method
   * @param initiatorMethod Method name of the @Plantuml method
   */
  public TraceSession(String initiatorClass, String initiatorMethod) {
    this.sessionId = UUID.randomUUID().toString().substring(0, 8); // Short ID for logging
    this.startTime = System.currentTimeMillis();

    // Create root node representing the @Plantuml annotated method
    this.rootNode = new MethodCallNode(initiatorClass, initiatorMethod, new String[0]);
    this.rootNode.setStartTime(startTime);

    // Initialize call stack with root
    this.callStack = new ArrayDeque<>();
    this.callStack.push(rootNode);

    // Initialize filter config (will be injected from properties later)
    this.filterConfig = TraceFilterConfig.getDefault();

    this.totalNodeCount = 1;
    this.maxDepth = 1;

    log.debug("[TraceSession:{}] Created for {}.{}", sessionId, initiatorClass, initiatorMethod);
  }

  /**
   * Pushes a new trace node onto the call stack. The node becomes a child of the current top node.
   *
   * @param node The node to push (method call, HTTP, SQL, etc.)
   */
  public void pushNode(TraceNode node) {
    // Check if we should trace this node (blacklist filtering)
    if (!filterConfig.shouldTrace(node)) {
      log.trace("[TraceSession:{}] Filtered out node: {}", sessionId, node.getDisplayLabel());
      return;
    }

    // Get current parent from stack
    TraceNode parent = callStack.peek();
    if (parent == null) {
      log.warn(
          "[TraceSession:{}] Call stack is empty! Cannot push node: {}",
          sessionId,
          node.getDisplayLabel());
      return;
    }

    // Add as child of current parent
    parent.addChild(node);

    // Push onto stack (becomes new current)
    callStack.push(node);

    // Update statistics
    totalNodeCount++;
    maxDepth = Math.max(maxDepth, callStack.size());

    log.trace(
        "[TraceSession:{}] Pushed node (depth {}): {}",
        sessionId,
        callStack.size(),
        node.getDisplayLabel());
  }

  /** Pops the current node from the call stack. Sets the node's end time before popping. */
  public void popNode() {
    if (callStack.isEmpty()) {
      log.warn("[TraceSession:{}] Attempted to pop from empty call stack", sessionId);
      return;
    }

    // Don't pop the root node (it will be popped when session ends)
    if (callStack.size() == 1) {
      log.trace("[TraceSession:{}] Ignoring pop of root node", sessionId);
      return;
    }

    TraceNode node = callStack.pop();
    node.setEndTime(System.currentTimeMillis());

    log.trace(
        "[TraceSession:{}] Popped node (depth {}): {} (duration: {}ms)",
        sessionId,
        callStack.size() + 1,
        node.getDisplayLabel(),
        node.getDuration());
  }

  /** Finalizes the trace session by closing the root node. */
  public void finalizeSession() {
    rootNode.setEndTime(System.currentTimeMillis());
    log.debug(
        "[TraceSession:{}] Finalized. Total nodes: {}, Max depth: {}, Duration: {}ms",
        sessionId,
        totalNodeCount,
        maxDepth,
        rootNode.getDuration());
  }

  /**
   * Returns the root trace node containing the complete call tree.
   *
   * @return The root node
   */
  public TraceNode getRootNode() {
    return rootNode;
  }

  /**
   * Returns the unique session ID.
   *
   * @return Session ID string
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * Returns the session start time in milliseconds.
   *
   * @return Start time
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Returns the total number of nodes collected in this session.
   *
   * @return Total node count
   */
  public int getTotalNodeCount() {
    return totalNodeCount;
  }

  /**
   * Returns the maximum call stack depth reached during this session.
   *
   * @return Maximum depth
   */
  public int getMaxDepth() {
    return maxDepth;
  }

  /**
   * Returns the current call stack depth.
   *
   * @return Current depth
   */
  public int getCurrentDepth() {
    return callStack.size();
  }

  /**
   * Checks if the session has exceeded the configured maximum depth. Can be used to prevent stack
   * overflow in recursive scenarios.
   *
   * @param maxAllowedDepth Maximum allowed depth
   * @return true if current depth exceeds maximum
   */
  public boolean isDepthExceeded(int maxAllowedDepth) {
    return callStack.size() > maxAllowedDepth;
  }

  /**
   * Returns a summary string for logging.
   *
   * @return Summary string
   */
  @Override
  public String toString() {
    return String.format(
        "TraceSession[id=%s, nodes=%d, depth=%d, duration=%dms]",
        sessionId,
        totalNodeCount,
        maxDepth,
        rootNode.getEndTime() > 0
            ? rootNode.getDuration()
            : (System.currentTimeMillis() - startTime));
  }
}
