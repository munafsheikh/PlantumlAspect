package com.virtrics.ai.plantuml.aop.trace;

import com.virtrics.ai.plantuml.aop.trace.model.TraceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadLocal-based context for managing deep tracing sessions.
 *
 * <p>This is the central coordination point for all tracing activity. When PlantUmlAspect starts
 * tracing via startTrace(), a TraceSession is created and stored in ThreadLocal, activating all
 * downstream interceptors (HTTP, JDBC, method calls, etc.).
 *
 * <p>Thread Safety: Uses ThreadLocal for complete thread isolation - each request thread has its
 * own independent trace session with no shared mutable state.
 */
public class TraceContext {
  private static final Logger log = LoggerFactory.getLogger(TraceContext.class);

  private static final ThreadLocal<TraceSession> CURRENT_SESSION = new ThreadLocal<>();

  /**
   * Starts a new tracing session for the current thread. This activates all interceptors - they
   * will now collect trace data.
   *
   * @param initiatorClass Fully qualified name of the class containing the @Plantuml method
   * @param initiatorMethod Name of the @Plantuml annotated method
   */
  public static void startTrace(String initiatorClass, String initiatorMethod) {
    if (CURRENT_SESSION.get() != null) {
      log.warn(
          "[TraceContext] Trace already active for thread {}. Ignoring nested startTrace() call.",
          Thread.currentThread().getName());
      return;
    }

    TraceSession session = new TraceSession(initiatorClass, initiatorMethod);
    CURRENT_SESSION.set(session);

    log.debug(
        "[TraceContext] Started trace session {} for {}.{}",
        session.getSessionId(),
        initiatorClass,
        initiatorMethod);
  }

  /**
   * Ends the current tracing session and deactivates all interceptors. MUST be called in a finally
   * block to prevent ThreadLocal memory leaks.
   *
   * @return The completed TraceSession, or null if no session was active
   */
  public static TraceSession endTrace() {
    TraceSession session = CURRENT_SESSION.get();
    if (session == null) {
      log.warn(
          "[TraceContext] No active trace session to end for thread {}",
          Thread.currentThread().getName());
      return null;
    }

    CURRENT_SESSION.remove(); // Critical: clear ThreadLocal to prevent memory leak

    log.debug(
        "[TraceContext] Ended trace session {} with {} total nodes",
        session.getSessionId(),
        session.getTotalNodeCount());

    return session;
  }

  /**
   * Checks if tracing is currently active for this thread. All interceptors use this as a fast-path
   * check before doing any work.
   *
   * @return true if a trace session is active, false otherwise
   */
  public static boolean isTracing() {
    return CURRENT_SESSION.get() != null;
  }

  /**
   * Gets the current active trace session for this thread.
   *
   * @return The current TraceSession, or null if tracing is not active
   */
  public static TraceSession getCurrentSession() {
    return CURRENT_SESSION.get();
  }

  /**
   * Pushes a new trace node onto the call stack for the current session. The node becomes a child
   * of the current top-of-stack node.
   *
   * <p>This method is called by all interceptors when they detect a traced operation.
   *
   * @param node The trace node to push (method call, HTTP call, SQL query, etc.)
   */
  public static void pushCall(TraceNode node) {
    TraceSession session = getCurrentSession();
    if (session != null) {
      session.pushNode(node);
    }
  }

  /**
   * Pops the current trace node from the call stack. Sets the node's end time and moves back up the
   * call hierarchy.
   *
   * <p>This method is called by interceptors when a traced operation completes.
   */
  public static void popCall() {
    TraceSession session = getCurrentSession();
    if (session != null) {
      session.popNode();
    }
  }

  /**
   * Forcefully clears the current trace session without returning it. Use this for error recovery
   * or cleanup scenarios.
   */
  public static void clearTrace() {
    TraceSession session = CURRENT_SESSION.get();
    if (session != null) {
      log.warn(
          "[TraceContext] Forcefully clearing trace session {} for thread {}",
          session.getSessionId(),
          Thread.currentThread().getName());
      CURRENT_SESSION.remove();
    }
  }
}
