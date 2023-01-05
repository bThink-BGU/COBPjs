/* global bp, ctx_proxy, Packages, EventSets */ // <-- Turn off warnings
// importPackage(Packages.il.ac.bgu.cs.bp.bpjs.model.eventsets);

const ContextChanged = EventSet('CTX.ContextChanged', function (e) {
  return ctx_proxy.effectFunctions.containsKey(String('CTX.Effect: ' + e.name))
})

__sync__ = function (stmt, syncData, isHot) {
  let ret = null
  let changes = null
  let query = null
  let id = null
  while (true) {
    stmt.waitFor.unshift(ContextChanged)
    ret = syncData ? bp.hot(isHot).sync(stmt, syncData) : bp.hot(isHot).sync(stmt)
    stmt.waitFor.shift()
    if (ContextChanged.contains(ret)) {
      ctx_proxy.waitForEffect(bp.store, ret, ctx)
      changes = ctx_proxy.getChanges().toArray()
      query = bp.thread.data.query
      id = bp.thread.data.seed
      if (query) {
        // change's structure - {type: 'end/new', query: 'query_name', entityId: 'id'}
        for (let i = 0; i < changes.length; i++) {
          //TODO in multiple queries, we will change the following if
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
    ret = null
    changes = null
    query = null
    id = null
  }
}

const ctx = {
  __internal_fields__: {
    createLiveCopy: function (name, query, entity, bt) {
      bthread(name,
        { query: query, seed: entity.id },
        //TODO in multiple queries, we will have instead:
        // {seed: {query[0]: entity[0], query[1]: entity[1],...}},
        function () {
          try {
            bt(entity)
            //TODO in multiple queries, we will have instead: bt(entity[0], entity[1],...)
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
      this.__internal_fields__.assign(entity, data)
    }
    return entity
  },
  insertEntity: function (entity) {
    this.__internal_fields__.testIsEffect('insertEntity', true)

    return this.__internal_fields__.insertEntityUnsafe(entity)
  },
  removeEntity: function (entity_or_id) {
    this.__internal_fields__.testIsEffect('removeEntity', true)

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
  /**
   * Returns an array of all entities that are in the given context
   * @param {string|delegate} queryName_or_function query name or a query function that represents a context in the system
   * @returns {Entity[]} all entities that are in the given context
   */
  runQuery: function (queryName_or_function) {
    let func
    if (typeof (queryName_or_function) === 'string') {
      const key = String(queryName_or_function)
      if (!ctx_proxy.queries.containsKey(key)) throw new Error('Query ' + queryName_or_function + ' does not exist')
      func = ctx_proxy.queries.get(key)
    } else {
      func = queryName_or_function
    }
    // TODO check if you can apply .map or .filter on the result
    //let entities = bp.store.entrySet().stream().filter(e=>e.getKey().startsWith('CTX.Entity: ')).map(e=>e.getValue()).toArray()
    //return func(entities)
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
  //TODO the entire function will need an update to support multiple queries
  bthread: function (name, context, bt) {
    bthread('cbt: ' + name,
      function () {
        // spawn live copies for current answers
        let res = ctx.runQuery(context)
        for (let i = 0; i < res.length; i++) {
          ctx.__internal_fields__.createLiveCopy(String('Live copy' + ': ' + name + ' ' + res[i].id), context, res[i], bt)
        }
        res = undefined
        let changes = null
        while (true) {
          sync({ waitFor: ContextChanged })
          // bp.log.info("changesA {0}: {1}", context, ctx_proxy.getChanges())
          //Filter all changes that are new and are relevant to the context
          changes = ctx_proxy.getChanges().parallelStream()
          .filter(function (change) {
            return change.type.equals('new') && change.query.equals(context)
          }).toArray()
          // bp.log.info("changesB {0}: {1}", context, changes)
          for (let i = 0; i < changes.length; i++) {
            // bp.log.info("changesC {0}: {1}", context, changes[i])
            // bp.log.info(bp.store.keys())
            if (changes[i].type.equals('new') && changes[i].query.equals(context)) {
              ctx.__internal_fields__.createLiveCopy(String('Live copy' + ': ' + name + ' ' + changes[i].entityId), context, ctx.getEntityById(changes[i].entityId), bt)
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
      this.__internal_fields__.insertEntityUnsafe(entities[i])
  }
}
Object.freeze(ctx)
