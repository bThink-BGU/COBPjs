/* global bp, Packages, EventSets */ // <-- Turn off warnings
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.model.eventsets);

/**
 * Tests if `o` is a javascript Set
 * @param o
 * @returns true iff `o` is a Set
 */
function isJSSet(o) {
  try {
    Set.prototype.has.call(o); // throws if o is not an object or has no [[SetData]]
    return true;
  } catch (e) {
    return false;
  }
}

/**
 * Adds a new b-thread, with initialized data field, to the b-program.
 * @param {string} name name of new b-thread
 * @param {object} data the initial data for the b-thread. Optional.
 * @param {function} fn entry point of new b-thread
 * @returns nothing
 */
function bthread(name, data, fn) {
  if (!fn) {
    // data is missing, we were invoked with 2 args
    fn = data;
    data = {};

  }
  if (!data.request) {
    data.request = [];
  }
  if (!data.waitFor) {
    data.waitFor = [];
  }
  if (!data.block) {
    data.block = [];
  }
  if (!data.interrupt) {
    data.interrupt = [];
  }
  bp.registerBThread(name, data, fn);
}

/**
 * Execute fn while requesting `evt`. This requires that all synchronizations will be
 * done using sync().
 *
 * @param {type} evt event to request for during fn's execution.
 * @param {type} fn function (b-thread segment) to run.
 * @returns nothing
 */
function request(evt, fn) {
  bp.thread.data.request.unshift(evt);
  fn();
  bp.thread.data.request.shift();
}

/**
 * Execute fn while waiting for es. This requires that all synchronizations will be
 * done using sync().
 *
 * @param {type} es event(s) to wait for during fn's execution.
 * @param {type} fn function (b-thread segment) to run.
 * @returns nothing
 */
function waitFor(es, fn) {
  if (Array.isArray(es)) {
    es = EventSets.anyOf(es);
  }
  bp.thread.data.waitFor.push(es);
  fn();
  bp.thread.data.waitFor.pop();
}

/**
 * Execute fn while blocking es. This requires that all synchronizations will be
 * done using sync().
 *
 * @param {type} es event(s) to block during fn's execution.
 * @param {type} fn function (b-thread segment) to run.
 * @returns nothing
 */
function block(es, fn) {
  if (Array.isArray(es)) {
    es = EventSets.anyOf(es);
  }
  bp.thread.data.block.push(es);
  fn();
  bp.thread.data.block.pop();
}

/**
 * Execute fn while being interrupted by any event from es.
 * This requires that all synchronizations will be done using sync().
 *
 * @param {type} es event(s) to be interrupted from during fn's execution.
 * @param {type} fn function (b-thread segment) to run.
 * @returns nothing
 */
function interrupt(es, fn) {
  if (Array.isArray(es)) {
    es = EventSets.anyOf(es);
  }
  bp.thread.data.interrupt.push(es);
  fn();
  bp.thread.data.interrupt.pop();
}

/**
 * Enters a synchronization point. Honors the RWBI stack.
 *
 * Can also be used as `sync()`. This causes the b-thread to sync using only
 * its RWBI stack.
 *
 * @param {type} stmt the statmet to be added
 * @param {type} syncData optional sync data
 * @returns {BEvent} The selected event
 * FIXME currently this changes stmt, we might not want that.
 *
 */
function sync(stmt, syncData) {
  function appendToPart(stmt, field) {
    if (bp.thread.data[field].length === 0) {
      return;
    }
    if (Array.isArray(stmt[field])) {
      stmt[field] = stmt[field].concat(bp.thread.data[field]);
    } else {
      if (stmt[field]) {
        stmt[field] = [stmt[field]].concat(bp.thread.data[field]);
      } else {
        stmt[field] = bp.thread.data[field];
      }
    }
  }

  if (!stmt) {
    stmt = {};
  }

  appendToPart(stmt, 'waitFor');
  appendToPart(stmt, 'block');
  appendToPart(stmt, 'interrupt');

  if (bp.thread.data.request.length > 0) {
    if (stmt.request) {
      if (!Array.isArray(stmt.request)) {
        stmt.request = [stmt.request];
      }
      stmt.request = stmt.request.concat(bp.thread.data.request);
    } else {
      stmt.request = bp.thread.data.request;
    }
  }

  let ret = syncData ? bp.sync(stmt, syncData) : bp.sync(stmt);
  const key1 = String('CTX.Effect: ' + ret.name)
  const key2 = String('CTX.EndOfActionEffect: ' + ret.name)
  if ((ctx_proxy.effectFunctions.containsKey(key1) || ctx_proxy.effectFunctions.containsKey(key2)) && !bp.thread.data.effect) {
    bp.sync({waitFor: ctx.__internal_fields.release_event, interrupt:stmt.interrupt})
  }
  return ret
}


/**
 * Returns true iff the function has been called by a b-thread
 * @returns {boolean}
 */
function isInBThread() {
  try {
    let a = bp.thread.name
    return true
  } catch (ignored) {
    return false
  }
}

/**
 * If expectInBThread is true - check if the function was called by a b-thread and throw an Error if not.
 * If expectInBThread is false - check if the function was called from the global scope and throw an Error if not.
 * The exception message will include the caller function name
 * @param caller The caller function name
 * @param expectInBThread Expect the code to be called by a b-thread
 */
function testInBThread(caller, expectInBThread) {
  if (expectInBThread && !isInBThread())
    throw new Error(String("The function " + caller + " must be called by a b-thread"))
  if (!expectInBThread && isInBThread())
    throw new Error(String("The function " + caller + " must be called from the global scope"))
}

function Any(type) {
  return bp.EventSet("Any(" + type + ")", function (e) {
    return String(e.name) == String(type)
  })
}

function Event(name, data) {
  if (data)
    return bp.Event(name, data)
  return bp.Event(name)
}
