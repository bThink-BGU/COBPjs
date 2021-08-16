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

const ContextChanged = bp.EventSet("CTX.ContextChanged", function (e) {
  return ctx_proxy.effectFunctions.containsKey(String("CTX.Effect: " + e.name))
})

const NonCtxInternalEvents = CtxInternalEvents.negate()

const ctx = {
  __internal_fields: {
    assign: function (target, source) {
      for (let property in source) {
        let val = source[property]
        if (source[property] instanceof org.mozilla.javascript.ConsString) {
          val = String(val)
        } else if (Array.isArray(source[property])) {
          bp.log.warn("Property {0} is an array. If the order of the array's elements is not important, you should use java.util.HashSet. Entity is {1}.", property, source)
        }
        target[property] = val
      }
      return target
    },
    insertEntityUnsafe: function (entity) {
      const key = String("CTX.Entity: " + entity.id)
      if (bp.store.has(key)) {
        throw new Error("Key " + entity.id + " already exists")
      }
      // bp.log.info("e to clone {0}", entity)
      let clone = ctx_proxy.clone(entity)
      // Object.freeze(clone)
      bp.store.put(key, clone)
      return clone
    },
    testIsEffect(caller, expected) {
      if (expected && isInBThread())
        throw new Error(String("The function " + caller + " must be called by an effect function"))
      if (!expected && !isInBThread())
        throw new Error(String("The function " + caller + " must no be called by an effect function"))
    }
  },
  Entity: function (id, type, data) {
    let entity = {id: String(id), type: String(type)}
    if (data) {
      this.__internal_fields.assign(entity, data)
    }
    ctx_proxy.removeScope(entity)
    return entity
  },
  insertEntity: function (entity) {
    this.__internal_fields.testIsEffect('insertEntity', true)

    return this.__internal_fields.insertEntityUnsafe(entity)
  },
  updateEntity: function (entity) {
    this.__internal_fields.testIsEffect('updateEntity', true)

    const key = String("CTX.Entity: " + entity.id)
    if (!bp.store.has(key)) {
      throw new Error("Key " + entity.id + " does not exist")
    }
    let clone = ctx_proxy.clone(entity)
    // Object.freeze(clone)
    bp.store.put(key, clone)
    return clone
  },
  removeEntity: function (entity_or_id) {
    this.__internal_fields.testIsEffect('removeEntity', true)

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
    return ctx_proxy.clone(bp.store.get(key)) //clone (serialization/deserialization) removes freezing
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
    let filtered = bp.store.filter(function (key, val) {
      return key.startsWith(String("CTX.Entity: ")) && func(val)
    }).values().toArray()
    for (let i = 0; i < filtered.length; i++)
      ans.push(ctx_proxy.clone(filtered[i]))
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
  },
  bthread: function (name, context, bt) {
    const createLiveCopy = function (name, query, entity, bt) {
      bthread(name,
        {query: query, seed: entity.id},
        function () {
          try {
            bt(entity)
          } catch (e) {
            if (e.javaException) {
              if (ctx_proxy.isEndOfContextException(e.javaException)) {
                return
              } else {
                if (e.javaException) ctx_proxy.rethrowException(e.javaException)
              }
            }
            throw e
          }
        })
    }
    bthread("cbt: " + name,
      function () {
        let res = ctx.runQuery(context)
        for (let i = 0; i < res.length; i++) {
          createLiveCopy(String("Live copy" + ": " + name + " " + res[i].id), context, res[i], bt)
        }
        res = undefined
        while (true) {
          sync({waitFor: ContextChanged})
          // bp.log.info("changesA {0}: {1}", context, bp.store.get("CTX.Changes"))
          let changes = bp.store.get("CTX.Changes").parallelStream()
            .filter(function (change) {
              return change.type.equals("new") && change.query.equals(context)
            }).toArray();
          // bp.log.info("changesB {0}: {1}", context, changes)
          for (let i = 0; i < changes.length; i++) {
            // bp.log.info("changesC {0}: {1}", context, changes[i])
            // bp.log.info(bp.store.keys())
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
    for (let i = 0; i < entities.length; i++)
      this.__internal_fields.insertEntityUnsafe(entities[i])
  }
}

bthread('Context population', function () {
  sync({request: bp.Event('Context population completed')})
})

/*bthread("assert no continuation before CTX.Update", function() {
  while (true) {
    let e = sync({waitFor:bp.all}).name
    const key1 = String('CTX.Effect: ' + e)
    const key2 = String('CTX.EndOfActionEffect: ' + e)
    if (ctx_proxy.effectFunctions.containsKey(key1) || ctx_proxy.effectFunctions.containsKey(key2)) {
      bp.log.info("HERE")
    }
  }
})*/

Object.freeze(ctx)
