importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.java.util);

/////////////////////////////////////////////////////////////////////
// BP related functions
/////////////////////////////////////////////////////////////////////

const Any = (type => bp.EventSet("Any(" + type + ")", e => e.name == type));
const AnyBut = (type => bp.EventSet("AnyBut(" + type + ")", e => e.name != type));

const bthread = function (name, f) {
    bp.registerBThread(name,
      {interrupt: [], block: []},
      function () {
          f();
      })

}

function block(e, f) {
    bp.thread.data.block.push(e);
    f();
    bp.thread.data.block.pop(e);
}

const sync = function (stmt, priority) {
    if (stmt.interrupt) {
        if (Array.isArray(stmt.interrupt)) {
            stmt.interrupt = stmt.interrupt.concat(bp.thread.data.interrupt)
        } else {
            stmt.interrupt = [stmt.interrupt].concat(bp.thread.data.interrupt)
        }
    } else {
        stmt.interrupt = bp.thread.data.interrupt
    }

    if (stmt.block) {
        if (Array.isArray(stmt.block)) {
            stmt.block = stmt.block.concat(bp.thread.data.block)
        } else {
            stmt.block = [stmt.block].concat(bp.thread.data.block)
        }
    } else {
        stmt.block = bp.thread.data.block
    }

    return bp.sync(stmt, priority ? priority : 0)
}

const CtxEntityChanged = changes => bp.Event("CTX.EntityChanged", changes)
const CtxEndOfActionForContext = (action, data) => bp.Event("CTX.EndOfActionForContext", {action: action, data: data})
const CtxEndOfActionForContextES = action => bp.EventSet("", e => {
    return e.name.equals("CTX.EndOfActionForContext") && e.data.action.equals(action)
})
const CtxEndES = (query, id) => bp.EventSet("", e => {
    return e.name.equals("CTX.EntityChanged") &&
      e.data.stream().filter(change => change.type.equals("end") && change.query.equals(query) && change.entity.id.equals(id)).count() > 0
})
const CtxStartES = (query) => bp.EventSet("", e => {
    return e.name.equals("CTX.EntityChanged") &&
      e.data.stream().filter(change => change.type.equals("new") && change.query.equals(query)).count() > 0
})

function assign(target, source) {
    return Object.assign(target, source)
}


bthread("ContextHandler", function () {
    var CTX_Instance = ContextService.GetInstance();
    while (true) {
        sync({waitFor: bp.all})
        // CTX_Instance.runEffectFunctionsInVerification(e) //NOT ACCURATE since b-threads may advance before this b-thread
        var changes = CTX_Instance.recentChanges()
        if (changes.size() > 0) {
            sync({request: CtxEntityChanged(changes)})
        }
    }
})

function cbt(name, q, bt) {
    const createLiveCopy = function(name, query, entity, bt) {
        bp.registerBThread(name,
          {query: query, seed: entity.id, interrupt: CtxEndES(query, entity.id), block: []},
          function () {
              bt(entity)
          })
    }
    bp.registerBThread("cbt: " + name,
      {interrupt: [], block: []},
      function () {
          ContextService.GetInstance().getActive(q).forEach(entity => createLiveCopy(
            "Live copy" + ": " + name + " " + entity.id + " (" + ContextService.generateUniqueId() + ")",
            q,
            entity,
            bt
          ))
          while (true) {
              sync({waitFor: CtxStartES(q)})
                .data
                .stream().filter(change => change.type.equals("new") && change.query.equals(q))
                .forEach(change => createLiveCopy(
                  "Live copy" + ": " + name + " " + change.entity.id + " (" + ContextService.generateUniqueId() + ")",
                  q,
                  change.entity,
                  bt
                ))
          }
      })
}

const CTX = {
    createEntity: (id, type, data) => ContextEntity(id, type, data),
    beginTransaction: () => ContextService.GetInstance().beginTransaction(),
    endTransaction: () => ContextService.GetInstance().endTransaction(),
    insertEntity: entity => ContextService.GetInstance().insertEntity(entity),
    updateEntity: entity => ContextService.GetInstance().updateEntity(entity),
    runQuery: queryName_or_function => ContextService.GetInstance().getQueryResults(queryName_or_function),
    getEntityById: id => ContextService.GetInstance().getEntity(id),
    registerQuery: (name, query) => ContextService.GetInstance().registerQuery(name, query),
    registerEffect: (eventName, effect) => ContextService.GetInstance().addEffectFunction(eventName, effect),
    registerEndOfActionEffect: (action, effect) => {
        const func = function (data) {
            bthread(action + "_EndOfActionForContext_" + data.s, function () {
                sync({waitFor: EndOfAction({session: data.s, hidden: true})})
                sync({request: CtxEndOfActionForContext(action, data)})
            })
        }
        bthread("EndOfActionForContext_" + action, function () {
            while (true) {
                func(waitFor(Any(action)).data)
            }
        })
        ContextService.GetInstance().addEffectFunction("CTX.EndOfActionForContext", action, effect)
    },
}