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
    return e.name.equals("CTX.EntityChanged") &&
        e.data.stream().filter(change => change.type.equals("end") && change.query.equals(query) && change.entity.id.equals(id)).count() > 0
})
const CtxStartES = (query) => bp.EventSet("", e => {
    return  e.name.equals("CTX.EntityChanged") &&
        e.data.stream().filter(change => change.type.equals("new") && change.query.equals(query)).count() > 0
})

function assign(target, source) {
    return Object.assign(target, source)
}

var CTX_Instance = ContextService.GetInstance();
bthread("ContextHandler", function () {
    while (true) {
        var e = sync({waitFor: bp.all})
        var changes = CTX_Instance.recentChanges()
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

function createLiveCopy(name, query, entity, bt) {
    bp.registerBThread(name,
        {query: query, seed: entity.id, interrupt: CtxEndES(query, entity.id)}, // from BPjs 0.10.6
        function () {
            bt(entity)
        })
}

var cbt = function (name, q, bt) {
    bp.registerBThread("cbt: " + name,
        {interrupt: []}, // from BPjs 0.10.6
        function () {
            ContextService.GetInstance().getActive(q).forEach(change => createLiveCopy(
                "Live copy" + ": " + name + " " + change.entity.id + " (" + ContextService.generateUniqueId() + ")",
                q,
                change.entity,
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