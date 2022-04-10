/* global bp, Packages, EventSets */ // <-- Turn off warnings
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.model.eventsets)

function deepFreeze(object) {
  // Retrieve the property names defined on object
  const propNames = Object.getOwnPropertyNames(object)

  // Freeze properties before freezing self

  for (let name of propNames) {
    let value = object[name]

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
