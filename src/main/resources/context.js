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
const CtxEntityChanged = (changes => bp.Event("CTX.EntityChanged", changes))
const CtxEndES = (query, id) => bp.EventSet("", e => {
    return e.name.equals("CTX.EntityChanged") && e.data.filter(change => change.type.equals("end") && change.query.equals(query) && change.entity.id.equals(id)).length > 1
})
const CtxStartES = (query) => bp.EventSet("", e => {
    if (!e.name.equals("CTX.EntityChanged")) {
        return false
    }
    for (let i = 0; i < e.data.length; i++) {
        if (e.data[i].type.equals("new") && e.data[i].query.equals(query)) {
            return true
        }
    }
    return false
})

function assign(target, source) {
    return Object.assign(target, source)
}

bthread("ContextHandler", function () {
    var CTX_Instance = ContextService.GetInstance()
    while (true) {
        sync({waitFor: bp.all})
        var changes = CTX_Instance.recentChanges()
        if (changes.length > 0)
            sync({request: CtxEntityChanged(changes)}, 5000)
    }
})
/*
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
})*/

/*bthread("CTX.RegisterQueryHandler", function () {
    while (true) {
        let query = sync({waitFor: AnyRegisterQueryCTX}).data
        sync({request: ___CTX___, block: AllButCTX})
        ContextService.GetInstance().registerQuery(query)
        sync({request: CtxQueryRegistered(query), block: AnyBut("CTX.QueryRegistered")})
    }
})

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
})*/

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
            let active = ContextService.GetInstance().getActive(q)
            while (true) {
                for (let i = 0; i < active.length; i++) {
                    let change = active[i];
                    // bp.log.info(change)
                    let btname = "Live copy" + ": " + name + " " + change.entity.id + " (" + ContextService.generateUniqueId() + ")";
                    ((btname, query, entity) => {
                        bp.registerBThread(btname,
                            {query: query, seed: entity.id, interrupt: CtxEndES(query, entity.id)}, // from BPjs 0.10.6
                            function () {
                                bt(entity)
                            });
                    })(btname,
                        q,
                        change.entity)
                }
                let changes = sync({waitFor: CtxStartES(q)}).data
                active = []
                for (let i = 0; i < changes.length; i++) {
                    if (changes[i].type.equals("new") && changes[i].query.equals(q))
                        active.push(changes[i])
                }
            }
        })
}