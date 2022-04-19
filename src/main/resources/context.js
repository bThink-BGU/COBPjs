/* global bp, ctx_proxy, Packages, EventSets */ // <-- Turn off warnings
// importPackage(Packages.il.ac.bgu.cs.bp.bpjs.model.eventsets);


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
let sync = function (stmt, syncData) {
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

  // Implementation note: we use [].concat.apply(...) to flatten arrays that
  // may have been passed to `request`. This is not needed in waitFor etc.,
  // because these arguments accept event sets, while `request` needs explicit events
  if (bp.thread.data.request.length > 0) {
    if (stmt.request) {
      if (!Array.isArray(stmt.request)) {
        stmt.request = [stmt.request]
      }
      stmt.request = stmt.request.concat([].concat.apply([], bp.thread.data.request))
    } else {
      stmt.request = [].concat.apply([], bp.thread.data.request)
    }
  }

  let ret = null
  let changes = null
  let query = null
  let id = null
  while (true) {
    stmt.waitFor.push(ContextChanged)
    ret = syncData ? bp.sync(stmt, syncData) : bp.sync(stmt)
    stmt.waitFor.pop()
    if (ContextChanged.contains(ret)) {
      ctx_proxy.waitForEffect(bp.store, ret, this)
      changes = ctx_proxy.getChanges().toArray()
      query = bp.thread.data.query
      id = bp.thread.data.seed
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
  ret = null
  changes = null
  query = null
  id = null
}

const ContextChanged = bp.EventSet('CTX.ContextChanged', function (e) {
  return ctx_proxy.effectFunctions.containsKey(String('CTX.Effect: ' + e.name))
})

const ctx = {
  __internal_fields: {
    createLiveCopy: function (name, query, entity, bt) {
      bthread(name,
        { query: query, seed: entity.id },
        function () {
          try {
            bt(entity)
          } catch (ex) {
            if (!ctx.isEndOfContextException(ex)) {
              ctx.rethrowException(ex)
            }
          }
        })
    },
    assign: function (target, source) {
      for (let property in source) {
        let val = source[property]
        if (source[property] instanceof org.mozilla.javascript.ConsString) {
          val = String(val)
        } else if (Array.isArray(source[property])) {
          bp.log.warn('Property \'\'{0}\'\' is an array. If the order of the array\'\'s elements is not important, you should use java.util.HashSet. Entity is {1}.', property, source)
        }
        target[property] = val
      }
      return target
    },
    insertEntityUnsafe: function (entity) {
      const key = String('CTX.Entity: ' + entity.id)
      if (bp.store.has(key)) {
        throw new Error('Key ' + entity.id + ' already exists')
      }
      bp.store.put(key, entity)
      return entity
    },
    testIsEffect(caller, expected) {
      if (expected && isInBThread())
        throw new Error(String('The function ' + caller + ' must be called by an effect function'))
      if (!expected && !isInBThread())
        throw new Error(String('The function ' + caller + ' must no be called by an effect function'))
    }
  },
  Entity: function (id, type, data) {
    let entity = { id: String(id), type: String(type) }
    if (typeof data !== 'undefined' && data != null) {
      if (typeof data.id !== 'undefined' || typeof data.type !== 'undefined') {
        throw new Error(String('Entity\'s data must not include "id" or "type" fields.'))
      }
      this.__internal_fields.assign(entity, data)
    }
    return entity
  },
  insertEntity: function (entity) {
    this.__internal_fields.testIsEffect('insertEntity', true)

    return this.__internal_fields.insertEntityUnsafe(entity)
  },
  removeEntity: function (entity_or_id) {
    this.__internal_fields.testIsEffect('removeEntity', true)

    const key = String('CTX.Entity: ' + (entity_or_id.id ? entity_or_id.id : entity_or_id))
    if (!bp.store.has(key)) {
      throw new Error('Cannot remove entity, key ' + key + ' does not exist')
    }
    bp.store.remove(key)
  },
  getEntityById: function (id) {
    // bp.log.info('getEntityById id {0}', id)
    const key = String('CTX.Entity: ' + id)
    if (!bp.store.has(key)) {
      throw new Error('Key ' + key + ' does not exist')
    }
    return bp.store.get(key)
    //throw new Error("Entity with id '" + id + "' does not exist")
  },
  runQuery: function (queryName_or_function) {
    let func
    if (typeof (queryName_or_function) === 'string') {
      const key = String(queryName_or_function)
      if (!ctx_proxy.queries.containsKey(key)) throw new Error('Query ' + queryName_or_function + ' does not exist')
      func = ctx_proxy.queries.get(key)
    } else {
      func = queryName_or_function
    }
    let ans = []
    let storeEntries = bp.store.entrySet().toArray()
    for (let i = 0; i < storeEntries.length; i++) {
      let entry = storeEntries[i]
      if (entry.getKey().startsWith(String('CTX.Entity:')) && func(entry.getValue()))
        ans.push(entry.getValue())
    }
    return ans
  },
  registerQuery: function (name, query) {
    testInBThread('registerQuery', false)
    const key = String(name)
    if (ctx_proxy.queries.containsKey(key)) throw new Error('Query ' + name + ' already exists')
    ctx_proxy.queries.put(key, query)
  },
  registerEffect: function (eventName, effect) {
    if (!(typeof eventName === 'string' || eventName instanceof String)) {
      throw new Error('The first parameter of registerEffect must be an event name')
    }
    testInBThread('registerEffect', false)

    const key = String('CTX.Effect: ' + eventName)
    if (ctx_proxy.effectFunctions.containsKey(key))
      throw new Error('Effect already exists for event ' + eventName)
    ctx_proxy.effectFunctions.put(key, effect)
  },
  bthread: function (name, context, bt) {
    bthread('cbt: ' + name,
      function () {
        let res = ctx.runQuery(context)
        for (let i = 0; i < res.length; i++) {
          ctx.__internal_fields.createLiveCopy(String('Live copy' + ': ' + name + ' ' + res[i].id), context, res[i], bt)
        }
        res = undefined
        let changes = null
        while (true) {
          sync({ waitFor: ContextChanged })
          // bp.log.info("changesA {0}: {1}", context, bp.store.get("CTX.Changes"))
          changes = ctx_proxy.getChanges().parallelStream()
          .filter(function (change) {
            return change.type.equals('new') && change.query.equals(context)
          }).toArray()
          // bp.log.info("changesB {0}: {1}", context, changes)
          for (let i = 0; i < changes.length; i++) {
            // bp.log.info("changesC {0}: {1}", context, changes[i])
            // bp.log.info(bp.store.keys())
            if (changes[i].type.equals('new') && changes[i].query.equals(context)) {
              ctx.__internal_fields.createLiveCopy(String('Live copy' + ': ' + name + ' ' + changes[i].entityId), context, ctx.getEntityById(changes[i].entityId), bt)
            }
          }
          changes = null
        }
      })
  },
  isEndOfContextException: function (e) {
    return typeof e.javaException !== 'undefined' && ctx_proxy.isEndOfContextException(e.javaException)
  },
  rethrowException: function (e) {
    if (e.javaException) {
      ctx_proxy.rethrowException(e.javaException)
    } else if (e.rhinoException) {
      ctx_proxy.rethrowException(e.rhinoException)
    } else {
      throw e
    }
  },
  populateContext: function (entities) {
    testInBThread('populateContext', false)
    for (let i = 0; i < entities.length; i++)
      this.__internal_fields.insertEntityUnsafe(entities[i])
  }
}

Object.freeze(ctx)
