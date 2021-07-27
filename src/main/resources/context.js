/* global bp, ctx_proxy, Packages, EventSets */ // <-- Turn off warnings
// importPackage(Packages.il.ac.bgu.cs.bp.bpjs.model.eventsets);

function deepFreeze(object) {
  // Retrieve the property names defined on object
  const propNames = Object.getOwnPropertyNames(object);

  // Freeze properties before freezing self

  for (let name of propNames) {
    const value = object[name];

    if (value && typeof value === "object") {
      deepFreeze(value);
    }
  }

  return Object.freeze(object);
}

const CtxInternalEvents = bp.EventSet("Ctx.InternalEvents", function (e) {
  return ctx_proxy.CtxEvents.contains(String(e.name))
})

const NonCtxInternalEvents = CtxInternalEvents.negate()

function CtxEndES(query, id) {
  return bp.EventSet('CtxEndES', function (e) {
    return e.name.equals('CTX.Changed') &&
      e.data.parallelStream().filter(function (change) {
        return change.type.equals('end') && change.query.equals(query) && change.entityId.equals(id)
      }).count() > 0
  })
}

function CtxStartES(query) {
  return bp.EventSet('CtxStartES', function (e) {
    return e.name.equals('CTX.Changed') &&
      e.data.parallelStream().filter(function (change) {
        return change.type.equals("new") && change.query.equals(query)
      }).count() > 0
  })
}

bthread("ContextHandler", function () {
  while (true) {
    sync({waitFor: ctx.__internal_fields.lock_event})
    sync({waitFor: ctx.__internal_fields.release_event, block: ctx.__internal_fields.lock_event})
    let changes = bp.store.get(String("Context changes"))
    bp.store.remove(String("Context changes"))
    if (changes && changes.size() > 0) {
      sync({request: ctx.__internal_fields.CtxEntityChanged(changes), block: ctx.__internal_fields.lock_event})
    }
    changes = []
  }
})

function isEndOfContext(exception) {
  if (!(exception instanceof EventSet)) return false
  if (bp.thread.data.interrupt) {
    if (Array.isArray(bp.thread.data.interrupt)) {
      if (!bp.thread.data.interrupt.find(int => int.contains(exception))) {
        return false
      }
    } else if (!bp.thread.data.interrupt.contains(exception)) return false
  } else return false
  return true
}

const ctx = {
  __internal_fields: {
    assign: function (target, source) {
      for (let property in source) {
        let val = source[property]
        if (source[property] instanceof org.mozilla.javascript.ConsString) {
          val = String(val)
        } else if (Array.isArray(source[property])) {
          bp.log.warn("Type of property {0} is array, which is not recommended. Consider using java.util.HashSet. Entity is {1}.", property, source)
        }
        target[property] = val
      }
      return target
    },
    CtxEntityChanged: function (changes) {
      return bp.Event('CTX.Changed', changes)
    },
    lock_event: bp.Event('_____CTX_LOCK_____', {hidden: true}),
    release_event: bp.Event('_____CTX_RELEASE_____', {hidden: true}),
    lock: function () {
      let t = bp.store.get("transaction") + 1
      bp.store.put("transaction", t)
      if (t == 1)
        sync({request: this.lock_event})
    },
    release: function () {
      let t = bp.store.get("transaction") - 1
      bp.store.put("transaction", t)
      if (t == 0)
        sync({request: this.release_event})
    },
    isEffectFunction: function () { return bp.thread.data.effect === true },
    insertEntityUnsafe: function (entity) {
      const key = String("CTX.Entity: " + entity.id)
      if (bp.store.has(key)) {
        throw new Error("Key " + entity.id + " already exists")
      }
      // bp.log.info("e to clone {0}", entity)
      let clone = ctx_proxy.cloner.clone(entity)
      // Object.freeze(clone)
      bp.store.put(key, clone)
      return clone
    }
  },
  beginTransaction: function () {
    this.__internal_fields.lock()
  },
  endTransaction: function () {
    this.__internal_fields.release()
  },
  Entity: function (id, type, data) {
    let entity = {id: String(id), type: String(type)}
    if (data) {
      this.__internal_fields.assign(entity, data)
    }
    return entity
  },
  insertEntity: function (entity) {
    if(!this.__internal_fields.isEffectFunction())
      throw new Error('ctx.insertEntity must be called from an effect function')

    return this.__internal_fields.insertEntityUnsafe(entity)
  },
  updateEntity: function (entity) {
    if(!this.__internal_fields.isEffectFunction())
      throw new Error('ctx.updateEntity must be called from an effect function')

    const key = String("CTX.Entity: " + entity.id)
    if (!bp.store.has(key)) {
      throw new Error("Key " + entity.id + " does not exist")
    }
    let clone = ctx_proxy.cloner.clone(entity)
    // Object.freeze(clone)
    bp.store.put(key, clone)
    return clone
  },
  removeEntity: function (entity_or_id) {
    if(!this.__internal_fields.isEffectFunction())
      throw new Error('ctx.removeEntity must be called from an effect function')

    const key = String("CTX.Entity: " + (entity_or_id.id ? entity_or_id.id : entity_or_id))
    if (!bp.store.has(key)) {
      throw new Error("Cannot remove entity, key " + key + " does not exist")
    }
    bp.store.remove(key)
  },
  getEntityById: function (id) {
    // bp.log.info('getEntityById id {0}', id)
    const key = String("CTX.Entity: " + id)
    if (!bp.store.has(key)) {
      throw new Error("Key " + key + " does not exist")
    }
    return ctx_proxy.cloner.clone(bp.store.get(key)) //clone (serialization/deserialization) removes freezing
    //throw new Error("Entity with id '" + id + "' does not exist")
  },
  runQuery: function (queryName_or_function) {
    let func;
    if (typeof (queryName_or_function) === 'string') {
      const key = String(queryName_or_function)
      if (!ctx_proxy.queries.containsKey(key)) throw new Error('Query ' + queryName_or_function + ' does not exist')
      func = ctx_proxy.queries.get(key)
    } else {
      func = queryName_or_function
    }
    let ans = []
    bp.store.filter((key, val) => key.startsWith(String("CTX.Entity: ")) && func(val))
      .values().forEach(v => ans.push(ctx_proxy.cloner.clone(v)))
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

    const key1 = String('CTX.Effect: ' + eventName)
    const key2 = String('CTX.EndOfActionEffect: ' + eventName)
    if (ctx_proxy.effectFunctions.containsKey(key1) || ctx_proxy.effectFunctions.containsKey(key2))
      throw new Error('Effect already exists for event ' + eventName)
    ctx_proxy.effectFunctions.put(key1, effect)

    function f() {
      bthread('Effect: ' + eventName, function () {
        bp.thread.data.effect = true
        while (true) {
          let data = sync({waitFor: Any(eventName)}).data
          ctx.beginTransaction()
          ctx_proxy.effectFunctions.get(key1)(data)
          ctx.endTransaction()
        }
      })
    }

    f()
  },
  /*registerEndOfActionEffect: function (eventName, effect) {
    if (!(typeof eventName === 'string' || eventName instanceof String)) {
      throw new Error('The first parameter of registerEndOfActionEffect must be an action name')
    }
    testInBThread('registerEndOfActionEffect', false)

    const key1 = String('CTX.Effect: ' + eventName)
    const key2 = String('CTX.EndOfActionEffect: ' + eventName)
    if (ctx_proxy.effectFunctions.containsKey(key1) || ctx_proxy.effectFunctions.containsKey(key2))
      throw new Error('Effect already exists for event ' + eventName)
    ctx_proxy.effectFunctions.put(key2, effect)

    function func(data) {
      bthread(eventName + "_EndOfActionForContext_" + data.s, function () {
        bp.thread.data.effect = true
        sync({waitFor: EndOfAction({session: data.s, hidden: true})})
        ctx_proxy.effectFunctions.get(key2)(data)
      })
    }

    bthread("EndOfActionForContext_" + eventName, function () {
      while (true) {
        func(sync({waitFor: Any(eventName)}).data)
      }
    })
  },*/
  bthread: function (name, context, bt) {
    const createLiveCopy = function (name, query, entity, bt) {
      bthread(name,
        {query: query, seed: entity.id, interrupt: [CtxEndES(query, entity.id)]},
        function () {
          try {
            bt(entity)
          } catch (e) {
            // TODO wrap the error message instead of rethrowing
            if (!isEndOfContext(e)) throw new Error("Exception in b-thread " + name + ". Error message: " + e)
          }
        })
    }
    bthread("cbt: " + name,
      function () {
        let res = ctx.runQuery(context)
        for (let i = 0; i < res.length; i++) {
          createLiveCopy(String("Live copy" + ": " + name + " " + res[i].id), context, res[i], bt)
        }
        res = [] // reset bthread state for verification
        while (true) {
          let changes = sync({waitFor: CtxStartES(context)}).data.toArray()
          for (let i = 0; i < changes.length; i++) {
            if (changes[i].type.equals("new") && changes[i].query.equals(context)) {
              createLiveCopy(String("Live copy" + ": " + name + " " + changes[i].entityId), context, ctx.getEntityById(changes[i].entityId), bt)
            }
          }
          changes = undefined
        }
      })
  },
  duringAfterContext: function (during, after) {
    try {
      during()
    } catch (exception) {
      if (!isEndOfContext(exception)) throw exception
      after()
    }
  },
  populateContext: function (entities) {
    testInBThread('populateContext', false)
    entities.forEach(e => this.__internal_fields.insertEntityUnsafe(e))
  }
}

bthread('Context population', function () {
  sync({request: bp.Event('Context population completed')})
})

Object.freeze(ctx)

bp.store.put("transaction", 0)
