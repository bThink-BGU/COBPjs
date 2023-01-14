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

/**
 * Adds a new b-thread, with initialized data field, to the b-program.
 * @param {string} name of new b-thread
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
    bp.registerBThread(name, data, function () {
        if (!data.syncDecorator) {
            data.syncDecorator = __sync__;
        } //CANNOT BE REMOVED: sync for all bthread must respect the context decoration, even if they don't use it.
        // therefore it must be initialized here, after b-threads start running.
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
 *
 * NOTE currently this changes stmt, we might not want that.
 *
 */
let __sync__ = function (stmt, syncData, isHot) {
    if (!stmt) {
        stmt = {};
    } else {
        let tmp = {}
        Object.assign(tmp,stmt)
        stmt = tmp
    }
    if (!isHot) {
        isHot = false;
    }

    __appendToStmtPart__(stmt, 'waitFor', bp.thread.data.waitFor);
    __appendToStmtPart__(stmt, 'block', bp.thread.data.block);
    __appendToStmtPart__(stmt, 'interrupt', bp.thread.data.interrupt);

    // Implementation note: we use [].concat.apply(...) to flatten arrays that
    // may have been passed to `request`. This is not needed in waitFor etc.,
    // because these arguments accept event sets, while `request` needs explicit events

    if (bp.thread.data.request.length > 0) {
        if (stmt.request) {
            if (!Array.isArray(stmt.request)) {
                stmt.request = [stmt.request];
            }
            stmt.request = stmt.request.concat([].concat.apply([], bp.thread.data.request));
        } else {
            stmt.request = [].concat.apply([], bp.thread.data.request);
        }
    }

    return syncData ? bp.hot(isHot).sync(stmt, syncData) : bp.hot(isHot).sync(stmt);
}

const sync = function (stmt, syncData, isHot) {
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