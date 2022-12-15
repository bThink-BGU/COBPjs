/* global bp */ // <-- Turn off warnings

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

let __sync__ = function (stmt, syncData, isHot) {
  return syncData ? bp.hot(isHot).sync(stmt, syncData) : bp.hot(isHot).sync(stmt)
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
    fn = data
    data = {}
  }
  if (!data.request) {
    data.request = []
  }
  if (!data.waitFor) {
    data.waitFor = []
  }
  if (!data.block) {
    data.block = []
  }
  if (!data.interrupt) {
    data.interrupt = []
  }

  bp.registerBThread(name, data, function () {
    if (!data.syncDecorator) {
      data.syncDecorator = __sync__
    }
    fn()
  })
}

/**
 * Appends EventSet(s) to a sync statement
 * @param {SyncStatement} stmt the statement to be added
 * @param {string} field the name of the stmt field (waitFor, request, block, or interrupt
 * @param {EventSet} value the EventSet(s) to add
 * @private
 */
function __appendToStmtPart__(stmt, field, value) {
  if (typeof value === 'undefined' || value == null) {
    return
  }
  if (stmt[field] === 'undefined' || stmt[field] == null) {
    stmt[field] = []
  }
  if (!Array.isArray(stmt[field])) {
    stmt[field] = [stmt[field]]
  }
  if (!Array.isArray(value)) {
    value = [value]
  }
  //since everything is array, and we can assume non-nested arrays in request, then we always concat.
  stmt[field] = value.concat(stmt[field])
}

/**
 * Enters a synchronization point. Honors the RWBI stack.
 *
 * Can also be used as `sync()`. This causes the b-thread to sync using only
 * its RWBI stack.
 *
 * @param {SyncStatement} stmt the statement to be added
 * @param {Object} [syncData] optional sync data
 * @param {Boolean} [isHot=false] whether the sync is hot or not.
 * @returns {BEvent} The selected event
 * FIXME currently this changes stmt, we might not want that.
 *
 */
// the function must not be const since it is overridden in context
const sync = function (stmt, syncData, isHot) {
  if (!stmt) {
    stmt = {}
  }
  if (!isHot) {
    isHot = false
  }

  __appendToStmtPart__(stmt, 'request', bp.thread.data.request)
  __appendToStmtPart__(stmt, 'waitFor', bp.thread.data.waitFor)
  __appendToStmtPart__(stmt, 'block', bp.thread.data.block)
  __appendToStmtPart__(stmt, 'interrupt', bp.thread.data.interrupt)

  return bp.thread.data.syncDecorator(stmt, syncData, isHot)
}

/**
 * Returns true iff the function has been called by a b-thread
 * @returns {boolean}
 */
function isInBThread() {
  try {
    let a = bp.thread.name
    return true
  } catch (ex) {
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

/**
 * Generate an event set based on the filter data.
 * If `filterData` is a String, this is a by-name filter.
 * If `filterData` is a RegExp, this is a by-name filter, but with a regular expression.
 * If `filterData` is an object, we filter by the existing fields of the event's data object.
 *
 * @param {String|RegExp} filterName
 * @param {Object} filterData
 * @returns {bp.EventSet}
 */
function any(filterName, filterData) {
  let filter = null

  if (filterName) {
    let filterType = (typeof filterName)
    switch (filterType) {
      case 'string':
        filter = EventSet('AnyNamed' + filterName, function (e) {
          return e.name === filterName
        })
        break
      case 'object':
        if (filterName instanceof RegExp) {
          filter = EventSet('AnyNamedRegEx ' + filterData, function (e) {
            return filterData.test(e.name)
          })
        } else {
          throw 'Any: Unsupported filterName type: ' + filterType + ' (' + filterName + ')'
        }
        break
      default:
        throw 'Any: Unsupported filterName type: ' + filterType + ' (' + filterName + ')'
    }
  }
  if (filterData) {
    if (typeof filterData !== 'object') {
      throw 'Any: Unsupported filterData type: ' + (typeof filterData) + ' (' + filterData + ')'
    }
    let keys = Object.keys(filterData)
    let str = ''
    for (let idx in keys) {
      str = str + ' ' + keys[idx] + ':' + filterData[keys[idx]]
    }
    let dataFilter = EventSet('AnyWithData' + str, function (e) {
      if (!e.data) return false

      for (let key of keys) {
        if (filterData[key] instanceof RegExp) {
          let match = filterData[key].test(e.data[key])
          if (!match) {
            //bp.log.info("({0}).test({1}) = {2}", filterData[key].toString(), e.data[key], match)
            return false
          }
        } else {
          if (filterData[key] != e.data[key]) {
            return false
          }
        }
      }
      return true
    })
    if (filter === null) {
      return dataFilter
    } else {
      return bp.EventSets.allOf(filter, dataFilter)
    }
  }
  if (filter === null) {
    throw 'Any: No filter was provided'
  }
  return filter
}

/**
 * Function for creating event sets.
 * @param {String} name
 * @param {function} callback
 * @returns {bp.EventSet}
 * @constructor
 */
function EventSet(name, callback) {
  return bp.EventSet(name, callback)
}

/**
 * Function for creating events.
 * @param {String} name
 * @param {Object} data
 * @returns {bp.Event}
 */
function Event(name, data) {
  return (arguments.length === 2) ? bp.Event(name, data) : bp.Event(name)
}