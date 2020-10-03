importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.java.util);

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
    var val =  e.name.equals("CTX.EntityChanged") && e.data.stream().filter(change => change.type.equals("end") && change.query.equals(query) && change.entity.id.equals(id)).count() > 1
    if(val) bp.log.info("val" + val)
    return val
})
const CtxStartES = (query) => bp.EventSet("", e => {
    if (!e.name.equals("CTX.EntityChanged")) {
        return false
    }
    for (let i = 0; i < e.data.size(); i++) {
        if (e.data.get(i).type.equals("new") && e.data.get(i).query.equals(query)) {
            return true
        }
    }
    return false
})

function assign(target, source) {
    return Object.assign(target, source)
}

var CTX_Instance = ContextService.GetInstance();
bthread("ContextHandler", function () {
    while (true) {
        var e = sync({waitFor: bp.all})
        CTX_Instance.runEffectFunctionsInVerification(e) //NOT ACCURATE since b-threads may advance before this b-thread
        var changes = CTX_Instance.recentChanges()
/*        bp.log.info("last event: " + e)
        bp.log.info("changes length: " + changes.length)
        bp.log.info("changes: " + Arrays.toString(changes))*/
        if (changes.size() > 0) {
            sync({request: CtxEntityChanged(changes)}, 5000)
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
                for (let i = 0; i < changes.size() ; i++) {
                    let change = changes.get(0)
                    if (change.type.equals("new") && change.query.equals(q))
                        active.push(change)
                }
            }
        })
}