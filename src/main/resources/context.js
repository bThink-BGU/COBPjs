importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.java.util);

/////////////////////////////////////////////////////////////////////
// BP related functions
/////////////////////////////////////////////////////////////////////

function Any(type) {
    return bp.EventSet("Any(" + type + ")", function(e) { return e.name == type })
}

function bthread(name, f) {
    var int = []
    try {
        int = bp.thread.data.interrupt
    } catch (e) { }
    bp.registerBThread(name,
      {interrupt: int, block: []},
      function () {
          f();
      })
}

function block(e, f) {
    if(Array.isArray(e)) {
        e.forEach(ev => bp.thread.data.block.push(ev))
    } else {
        bp.thread.data.block.push(e)
    }
    f();
    if(Array.isArray(e)) {
        e.forEach(ev => bp.thread.data.block.pop())
    } else {
        bp.thread.data.block.pop()
    }
}

function sync(stmt, priority) {
    const appendToPart = function (stmt, target, origin) {
        if (stmt[target]) {
            if (Array.isArray(stmt[target])) {
                stmt[target] = stmt[target].concat(bp.thread.data[origin])
            } else {
                stmt[target] = [stmt[target]].concat(bp.thread.data[origin])
            }
        } else {
            stmt[target] = bp.thread.data[origin]
        }
    }

    // var stmt = assign({}, statement)
    appendToPart(stmt, 'waitFor', 'interrupt')
    appendToPart(stmt, 'block', 'block')

    var e = bp.sync(stmt, priority ? priority : 0)
    if (bp.thread.data.interrupt.find(es => es.contains(e))) {
        throw e
    }
    return e
}

function waitFor(e) {
    return sync({ waitFor: e });
}

/////////////////////////////////////////////////////////////////////
// Context related functions
/////////////////////////////////////////////////////////////////////

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

const CtxInternalEvents = bp.EventSet("Ctx.InternalEvents", function(e) { return ["CTX.Changed", "_____CTX_LOCK_____", "_____CTX_RELEASE_____"].includes(e.name)})
const NonCtxInternalEvents = bp.EventSet("Ctx.NonInternalEvents", function(e) { return  !CtxInternalEvents.contains(e)})
const CtxEndES = (query, id) => bp.EventSet('CtxEndES', function(e) {
    return e.name.equals('CTX.Changed') &&
      Array.from(e.data.toArray()).filter(function(change) { return change.type.equals('end') && change.query.equals(query) && change.entityId.equals(id) }).length > 0
})
const CtxStartES = (query) => bp.EventSet('CtxStartES', function(e) {
    return e.name.equals('CTX.Changed') &&
      Array.from(e.data.toArray()).filter(function(change) { return change.type.equals("new") && change.query.equals(query) }).length > 0
})

bp.registerBThread("ContextHandler", {interrupt: [], block: []}, function () {
    while (true) {
        sync({waitFor: ctx.__internal_fields.lock_event})
        sync({waitFor: ctx.__internal_fields.release_event, block: ctx.__internal_fields.lock_event})
        let changes = bp.store.get(String("Context changes"))
        bp.store.remove(String("Context changes"))
        if (changes && changes.size() > 0) {
            sync({request: ctx.__internal_fields.CtxEntityChanged(changes), block: ctx.__internal_fields.lock_event})
        }
    }
})

function isEndOfContext(exception) {
    if (!(exception instanceof il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet)) return false
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
                    bp.log.warn("Type of property {0} is array, which is not recommended. Consider using Set. Entity is {1}.", property, source)
                }
                target[property] = val
            }
            return target
        },
        CtxEntityChanged: changes => bp.Event('CTX.Changed', changes),
        lock_event: bp.Event('_____CTX_LOCK_____', {hidden:true}),
        release_event: bp.Event('_____CTX_RELEASE_____', {hidden:true}),
        lock: function() {
            let t = bp.store.get("transaction") + 1
            bp.store.put("transaction", t)
            if(t == 1)
                sync({request: this.lock_event} )
        },
        release: function() {
            let t = bp.store.get("transaction") - 1
            bp.store.put("transaction", t)
            if(t == 0)
                sync({request: this.release_event} )
        },
    },
    beginTransaction: function() {
        this.__internal_fields.lock()
    },
    endTransaction: function() {
        this.__internal_fields.release()
    },
    insertEntity: function(id, type, data) {
        this.__internal_fields.lock()
        const key = String("CTX.Entity: " + id)
        if (bp.store.has(key)) {
            throw Error("Key " + id + " already exists")
        }
        var entity = {id: String(id), type: String(type)}
        if(data) {
            this.__internal_fields.assign(entity, data)
        }
        let clone = ctx_proxy.cloner.clone(entity)
        // Object.freeze(clone)
        bp.store.put(key, clone)
        this.__internal_fields.release()
        return clone
    },
    updateEntity: function(entity) {
        this.__internal_fields.lock()
        const key = String("CTX.Entity: " + entity.id)
        if (!bp.store.has(key)) {
            throw Error("Key " + entity.id + " does not exist")
        }
        let clone = ctx_proxy.cloner.clone(entity)
        // Object.freeze(clone)
        bp.store.put(key, clone)
        this.__internal_fields.release()
        return clone
    },
    removeEntity: function(entity) {
        this.__internal_fields.lock()
        const key = String("CTX.Entity: " + entity.id)
        if (!bp.store.has(key)) {
            throw Error("Cannot remove entity, key " + entity.id + " does not exist")
        }
        bp.store.remove(key)
        this.__internal_fields.release()
    },
    getEntityById: function(id) {
        // bp.log.info('getEntityById id {0}', id)
        const key = String("CTX.Entity: " + id)
        if (!bp.store.has(key)) {
            //throw Error("Entity with id '" + id + "' does not exist")
        }
        return ctx_proxy.cloner.clone(bp.store.get(key)) //clone (serialization/deserialization) removes freezing
    },
    runQuery: function(queryName_or_function) {
        let func;
        if (typeof (queryName_or_function) === 'string') {
            const key = String(queryName_or_function)
            if (!ctx_proxy.queries.containsKey(key)) throw Error('Query ' + queryName_or_function + ' does not exist')
            func = ctx_proxy.queries.get(key)
        } else {
            func = queryName_or_function
        }
        let func2 = function (key, val) {
            return key.startsWith(String("CTX.Entity: ")) && func(val)
        }
        let ans = new Set()
        bp.store.filter(func2).forEach((k,v)=>ans.add(ctx_proxy.cloner.clone(v)))
        return ans
    },
    registerQuery: function(name, query) {
        try{
            let a = bp.thread.name
            throw 'all queries must be registered from global scope.'
        }catch(e){
            const key = String(name)
            if (ctx_proxy.queries.containsKey(key)) throw Error('Query ' + name + ' already exists')
            ctx_proxy.queries.put(key, query)
        }
    },
    registerEffect: function(eventName, effect) {
        try{
            // const a = bp.thread.name
            throw 'effects must not be registered from b-threads.'
        }catch(e){
            const key = String('CTX.Effect: ' + eventName)
            if (ctx_proxy.effectFunctions.containsKey(key)) throw Error('Effect already exists for event ' +eventName)
            ctx_proxy.effectFunctions.put(key, effect)
            function f() {
                bthread('Register effect: ' + eventName, function () {
                    while(true) {
                        ctx.beginTransaction()
                        ctx_proxy.effectFunctions.get(key)(sync({waitFor: Any(eventName)}).data)
                        ctx.endTransaction()
                    }
                })
            }
            f()
        }
    },
    bthread: function(name, context, bt) {
        const createLiveCopy = function (name, query, entity, bt) {
            bp.registerBThread(name,
              {query: query, seed: entity.id, interrupt: [CtxEndES(query, entity.id)], block: []},
              function () {
                  try {
                      bt(entity)
                  } catch (e) {
                      // TODO wrap the error message instead of rethrowing
                      if (!isEndOfContext(e)) throw "Exception in b-thread " + name + ". Error message: " + e
                  }
              })
        }
        bp.registerBThread("cbt: " + name,
          {interrupt: [], block: []},
          function () {
              ctx.runQuery(context).forEach(function(entity) {
                createLiveCopy("Live copy" + ": " + name + " " + entity.id, context, entity, bt)})
              while (true) {
                  sync({waitFor: CtxStartES(context)}).data.forEach(function(change) {
                      if(change.type.equals("new") && change.query.equals(context)) {
                          createLiveCopy("Live copy" + ": " + name + " " + change.entityId, context, ctx.getEntityById(change.entityId), bt)
                      }
                  })
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
    }
}

Object.freeze(ctx)

bp.store.put("transaction", 0)