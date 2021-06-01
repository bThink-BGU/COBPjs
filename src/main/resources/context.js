
/* global bp, ctx_proxy */

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
      e.data.filter(function (change) {
        return change.type.equals('end') && change.query.equals(query) && change.entityId.equals(id)
      }).length > 0
  })
}

function CtxStartES(query) {
  return bp.EventSet('CtxStartES', function (e) {
    return e.name.equals('CTX.Changed') &&
      e.data.filter(function (change) {
        return change.type.equals("new") && change.query.equals(query)
      }).length > 0
  })
}

bthread("ContextHandler", function () {
  while (true) {
    sync({waitFor: ctx.__internal_fields.lock_event})
    sync({waitFor: ctx.__internal_fields.release_event, block: ctx.__internal_fields.lock_event})
    let changes = bp.store.get(String("Context changes"))
    bp.store.remove(String("Context changes"))
    if (changes && changes.length > 0) {
      sync({request: ctx.__internal_fields.CtxEntityChanged(changes), block: ctx.__internal_fields.lock_event})
    }
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
    insertEntityNotSynchronized: function (entity) {
      const key = String("CTX.Entity: " + entity.id)
      if (bp.store.has(key)) {
        throw new Error("Key " + entity.id + " already exists")
      }
      // bp.log.info("e to clone {0}", entity)
      let clone = ctx_proxy.cloner.clone(entity)
      // Object.freeze(clone)
      bp.store.put(key, clone)
      return clone
    },
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
    CtxEntityChanged: changes => bp.Event('CTX.Changed', changes),
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
  },
  beginTransaction: function () {
    this.__internal_fields.lock()
  },
  endTransaction: function () {
    this.__internal_fields.release()
  },
  Entity: function (id, type, data) {
    var entity = {id: String(id), type: String(type)}
    if (data) {
      this.__internal_fields.assign(entity, data)
    }
    return entity
  },
  insertEntity: function (entity) {
    this.beginTransaction()
    let clone = this.__internal_fields.insertEntityNotSynchronized(entity)
    this.endTransaction()
    return clone
  },
  updateEntity: function (entity) {
    this.beginTransaction()
    const key = String("CTX.Entity: " + entity.id)
    if (!bp.store.has(key)) {
      throw new Error("Key " + entity.id + " does not exist")
    }
    let clone = ctx_proxy.cloner.clone(entity)
    // Object.freeze(clone)
    bp.store.put(key, clone)
    // this.__internal_fields.release()
    this.endTransaction()
    return clone
  },
  removeEntity: function (entity_or_id) {
    this.beginTransaction()
    const key = String("CTX.Entity: " + (entity_or_id.id ? entity_or_id.id : entity_or_id))
    if (!bp.store.has(key)) {
      throw new Error("Cannot remove entity, key " + key + " does not exist")
    }
    bp.store.remove(key)
    this.endTransaction()
  },
  getEntityById: function (id) {
    let inBThread = true
    try {
      let a = bp.thread.name
    } catch (e) {
      inBThread = false
    }
    if (inBThread)
      this.beginTransaction()
    // bp.log.info('getEntityById id {0}', id)
    const key = String("CTX.Entity: " + id)
    if (!bp.store.has(key)) {
      throw new Error("Key " + key + " does not exist")
    }
    let res = ctx_proxy.cloner.clone(bp.store.get(key)) //clone (serialization/deserialization) removes freezing
    if (inBThread)
      this.endTransaction()
    //throw new Error("Entity with id '" + id + "' does not exist")
    return res
  },
  runQuery: function (queryName_or_function) {
    let inBThread = isInBThread()
    let func;
    if (typeof (queryName_or_function) === 'string') {
      const key = String(queryName_or_function)
      if (!ctx_proxy.queries.containsKey(key)) throw new Error('Query ' + queryName_or_function + ' does not exist')
      func = ctx_proxy.queries.get(key)
    } else {
      func = queryName_or_function
    }
    let ans = []
    if (inBThread)
      this.beginTransaction()
    bp.store.filter((key, val) => key.startsWith(String("CTX.Entity: ")) && func(val))
      .values().forEach(v => ans.push(ctx_proxy.cloner.clone(v)))

    if (inBThread)
      this.endTransaction()
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
    if (ctx_proxy.effectFunctions.containsKey(key)) throw new Error('Effect already exists for event ' + eventName)
    ctx_proxy.effectFunctions.put(key, effect)

    function f() {
      bthread('Register effect: ' + eventName, function () {
        while (true) {
          let data = sync({waitFor: Any(eventName)}).data
          ctx.beginTransaction()
          ctx_proxy.effectFunctions.get(key)(data)
          ctx.endTransaction()
        }
      })
    }

    f()
  },
  registerEndOfActionEffect: function (action, effect) {
    if (!(typeof action === 'string' || action instanceof String)) {
      throw new Error('The first parameter of registerEndOfActionEffect must be an action name')
    }
    testInBThread('registerEndOfActionEffect', false)

    const key = String('CTX.EndOfActionEffect: ' + action)
    if (ctx_proxy.effectFunctions.containsKey(key)) throw new Error('Effect already exists for action ' + action)
    ctx_proxy.effectFunctions.put(key, action)

    function func(data) {
      bthread(action + "_EndOfActionForContext_" + data.s, function () {
        sync({waitFor: EndOfAction({session: data.s, hidden: true})})
        effect(data)
      })
    }

    bthread("EndOfActionForContext_" + action, function () {
      while (true) {
        func(sync({waitFor: Any(action)}).data)
      }
    })
  },
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
        const res = ctx.runQuery(context)
        for (let i = 0; i < res.length; i++) {
          createLiveCopy(String("Live copy" + ": " + name + " " + res[i].id), context, res[i], bt)
        }
        while (true) {
          let changes = sync({waitFor: CtxStartES(context)}).data
          for (let change of changes) {
            if (change.type.equals("new") && change.query.equals(context)) {
              createLiveCopy(String("Live copy" + ": " + name + " " + change.entityId), context, ctx.getEntityById(change.entityId), bt)
            }
          }
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
    entities.forEach(e => this.__internal_fields.insertEntityNotSynchronized(e))
  }
}

Object.freeze(ctx)

bp.store.put("transaction", 0)

ctx.registerQuery('CurrentPages', entity => entity.id == 'CurrentPages')
ctx.registerEffect('Page', function (data) {
  let p = ctx.getEntityById("CurrentPages")
  p[data.session] = data.name
  ctx.updateEntity(p)
})

ctx.populateContext([
  ctx.Entity('CurrentPages', 'CurrentPages')
])

bthread('Context population', function () {
  sync({request: bp.Event('Context population completed')})
})