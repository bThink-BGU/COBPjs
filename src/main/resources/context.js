importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

/////////////////////////////////////////////////////////////////////
// BP related functions
/////////////////////////////////////////////////////////////////////

const Any = (type => bp.EventSet("Any(" + type + ")", e => e.name == type));
const AnyBut = (type => bp.EventSet("AnyBut(" + type + ")", e => e.name != type));

const bthread = function (name, f) {
    bp.registerBThread(name,
        {interrupt: [], name: name}, // from BPjs 0.10.6
        function () {
            f()
        })
}

const sync = function (stmt, priority) {
    if (stmt.interrupt) {
        if (isArray(stmt.interrupt)) {
            stmt.interrupt = stmt.interrupt.concat(bp.thread.data.interrupt)
        } else {
            stmt.interrupt = [stmt.interrupt].concat(bp.thread.data.interrupt)
        }
    } else {
        stmt.interrupt = bp.thread.data.interrupt
    } // from BPjs 0.10.6
    return bp.sync(stmt, priority ? priority : 0)
}

/////////////////////////////////////////////////////////////////////
// Context related functions
/////////////////////////////////////////////////////////////////////
const ___CTX___ = bp.Event("___CTX___", {})

const CtxInsertEntity = (ctx => bp.Event("CTX.InsertEntity", ctx))
const CtxUpdateEntity = (ctx => bp.Event("CTX.UpdateEntity", ctx))
const CtxDeleteEntity = (ctx => bp.Event("CTX.DeleteEntity", ctx))
const CtxRegisterQuery = (q => bp.Event("CTX.RegisterQuery", q))
const CtxQueryRegistered = (q => bp.Event("CTX.QueryRegistered", q))
const CtxEnd = ((query, id) => bp.Event("CtxEnd", {query: query, id: id}))
const CtxEntityChanged = ((changeType, ctx) => bp.Event("CTX.EntityChanged", {type: changeType, entity: ctx}))

const ctxResultEvents = ["CTX.EntityChanged", "CTX.QueryRegistered", "CtxEnd"]
const ctxRequestEvents = ["CTX.InsertEntity", "CTX.UpdateEntity", "CTX.DeleteEntity", "CTX.RegisterQuery"]
const ctxEvents = ctxResultEvents.concat(ctxRequestEvents)

const AnyInsertEntityCTX = bp.EventSet("AnyInsertEntityCTX", e => (String(e.name).equals("CTX.InsertEntity")))
const AnyUpdateEntityCTX = bp.EventSet("AnyUpdateEntityCTX", e => (String(e.name).equals("CTX.UpdateEntity")))
const AnyDeleteEntityCTX = bp.EventSet("AnyDeleteEntityCTX", e => (String(e.name).equals("CTX.DeleteEntity")))
const AnyChangedEntityCTX = bp.EventSet("AnyEntityChangedCTX", e => (String(e.name).equals("CTX.EntityChanged")))
const AnyRegisterQueryCTX = bp.EventSet("AnyRegisterQueryCTX", e => (String(e.name).equals("CTX.RegisterQuery")))
const AllButCTX = bp.EventSet("AllButCTX", e => (String(e.name) != "___CTX___"))
const AllButContext = bp.EventSet("AllButContext", e => (ctxEvents.indexOf(String(e.name)) === -1))
const ContextEvents = bp.EventSet("ContextEvents", e => (ctxEvents.indexOf(String(e.name))) > -1)
const ContextResultEvents = bp.EventSet("ContextResultEvents", e => (ctxResultEvents.indexOf(String(e.name))) > -1)
const ContextRequestEvents = bp.EventSet("ContextRequestEvents", e => (ctxRequestEvents.indexOf(String(e.name))) > -1)
const AnyEvents = AllButCTX

const AnyInContext = ((type, ctx) => bp.EventSet("Any(" + type + ")", e => e.name == type && e.data.id == ctx.id))

function assign(target, source) {
    return Object.assign(target, source)
}

bthread("ContextHandler", function () {
    var CTX_Instance = ContextService.GetInstance()
    while (true) {
        sync({waitFor: ContextResultEvents})
    }
})

bthread("CTX.InsertEntityHandler", function () {
    while (true) {
        let ctx = sync({waitFor: AnyInsertEntityCTX}).data
        sync({request: ___CTX___, block: AllButCTX})
        ContextService.GetInstance().insertEntity(ctx)
        sync({request: CtxEntityChanged("Insert", ctx), block: AnyBut("CTX.EntityChanged")})
    }
})

bthread("CTX.UpdateEntityHandler", function () {
    while (true) {
        let ctx = sync({waitFor: AnyUpdateEntityCTX}).data
        sync({request: ___CTX___, block: AllButCTX})
        ContextService.GetInstance().updateEntity(ctx)
        sync({request: CtxEntityChanged("Update", ctx), block: AnyBut("CTX.EntityChanged")})
    }
})

bthread("CTX.DeleteEntityHandler", function () {
    while (true) {
        let ctx = sync({waitFor: AnyDeleteEntityCTX}).data
        sync({request: ___CTX___, block: AllButCTX})
        ContextService.GetInstance().deleteEntity(ctx)
        sync({request: CtxEntityChanged("Delete", ctx), block: AnyBut("CTX.EntityChanged")})
    }
})

/*bthread("CTX.RegisterQueryHandler", function () {
    while (true) {
        let query = sync({waitFor: AnyRegisterQueryCTX}).data
        sync({request: ___CTX___, block: AllButCTX})
        ContextService.GetInstance().registerQuery(query)
        sync({request: CtxQueryRegistered(query), block: AnyBut("CTX.QueryRegistered")})
    }
})*/

bthread("AnnounceEndedCTX", function () {
    while (true) {
        sync({waitFor: AnyChangedEntityCTX})
        // sync({request: ___CTX___, block: AllButCTX})
        let changes = ContextService.GetInstance().getRecentCtxEnd()
        for (let i = 0; i < changes.length; i++) {
            let change = changes[i]
            if (change.type.equals("end")) {
                sync({request: CtxEnd(change.query, change.entity.id), block: AnyBut("CtxEnd")})
            }
        }
    }
})

function getQueryResults(query) {
    return ContextService.GetInstance().getQueryResults(query)
}

function getActiveResults(query) {
    return ContextService.GetInstance().getActive(query)
}

var cbt = function (name, q, bt) {
    bp.registerBThread("cbt: " + name,
        {interrupt: []}, // from BPjs 0.10.6
        function () {
            while (true) {
                let changes = ContextService.GetInstance().getNewForQuery(q)
                for (let i = 0; i < changes.length; i++) {
                    let change = changes[i];
                    // bp.log.info(change)
                    let btname = "Live copy" + ": " + name + " " + change.entity.id + " (" + ContextService.generateUniqueId() + ")";
                    ((btname, query, entity) => {
                        bp.registerBThread(btname,
                            {query: query, seed: entity.id, interrupt: CtxEnd(query, entity.id)}, // from BPjs 0.10.6
                            function () {
                                bt(entity)
                            });
                    })(btname,
                        q,
                        change.entity)
                }
                sync({waitFor: AnyChangedEntityCTX})
            }
        })
}