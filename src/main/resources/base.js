/* global bp, Packages, EventSets */ // <-- Turn off warnings
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.model.eventsets)

function deepFreeze(object) {
  // Retrieve the property names defined on object
  const propNames = Object.getOwnPropertyNames(object)

  // Freeze properties before freezing self

  for (let name of propNames) {
    const value = object[name]

    if (value && typeof value === 'object') {
      deepFreeze(value)
    }
  }

  return Object.freeze(object)
}

/**
 * Tests if `o` is a javascript Set
 * @param o
 * @returns true iff `o` is a Set
 */
function isJSSet(o) {
  try {
    Set.prototype.has.call(o) // throws if o is not an object or has no [[SetData]]
    return true
  } catch (e) {
    return false
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
  function initField(data, fieldName) {
    if (!data[fieldName]) {
      data[fieldName] = []
    } else if (!Array.isArray(data[fieldName])) {
      data[fieldName] = [data[fieldName]]
    }
  }

  if (!fn) {
    // data is missing, we were invoked with 2 args
    fn = data
    data = {}

  }

  initField(data, 'request')
  initField(data, 'waitFor')
  initField(data, 'block')
  initField(data, 'interrupt')

  bp.registerBThread(name, data, fn)
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
  bp.thread.data.request.unshift(evt)
  fn()
  bp.thread.data.request.shift()
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
    es = EventSets.anyOf(es)
  }
  bp.thread.data.waitFor.push(es)
  fn()
  bp.thread.data.waitFor.pop()
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
    es = EventSets.anyOf(es)
  }
  bp.thread.data.block.push(es)
  fn()
  bp.thread.data.block.pop()
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
    es = EventSets.anyOf(es)
  }
  bp.thread.data.interrupt.push(es)
  fn()
  bp.thread.data.interrupt.pop()
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
    if (Array.isArray(stmt[field])) {
      stmt[field] = stmt[field].concat(bp.thread.data[field])
    } else {
      if (stmt[field]) {
        stmt[field] = [stmt[field]].concat(bp.thread.data[field])
      } else {
        stmt[field] = bp.thread.data[field]
      }
    }
  }

  if (!stmt) {
    stmt = {}
  }

  appendToPart(stmt, 'waitFor')
  appendToPart(stmt, 'block')
  appendToPart(stmt, 'interrupt')
  appendToPart(stmt, 'request')

  while (true) {
    stmt.waitFor.push(ContextChanged)
    let ret = syncData ? bp.sync(stmt, syncData) : bp.sync(stmt)
    stmt.waitFor.pop()
    if (ContextChanged.contains(ret)) {
      ctx_proxy.waitForEffect(bp.store, ret, this)
      let changes = ctx_proxy.getChanges().toArray()
      let query = bp.thread.data.query
      let id = bp.thread.data.seed
      if (query) {
        for (let i = 0; i < changes.length; i++) {
          if (changes[i].type.equals('end') && changes[i].query.equals(query) && changes[i].entityId.equals(id)) {
            ctx_proxy.throwEndOfContext()
          }
        }
      }
      if (ctx_proxy.shouldWake(stmt, ret)) {
        return ret
      }
    } else {
      return ret
    }
  }
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
    throw new Error(String('The function ' + caller + ' must be called by a b-thread'))
  if (!expectInBThread && isInBThread())
    throw new Error(String('The function ' + caller + ' must be called from the global scope'))
}

function Any(type) {
  return bp.EventSet('Any(' + type + ')', function (e) {
    return String(e.name) == String(type)
  })
}

function Event(name, data) {
  if (typeof data !== 'undefined' && data != null)
    return bp.Event(name, data)
  return bp.Event(name)
}
