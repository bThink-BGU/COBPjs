importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

/////////////////////////////////////////////////////////////////////
// BP related functions
/////////////////////////////////////////////////////////////////////

const bthread = function (name, f) {
    bp.registerBThread(name + "-" + random.string(4),
        { interrupt:[] },
        function () {
            f.bname = name
            f()
        })
}

const sync = function (stmt) {
    stmt.interrupt.push(...bp.thread.data.interrupt)
    return bp.sync(stmt)
}

/////////////////////////////////////////////////////////////////////
// Context related functions
/////////////////////////////////////////////////////////////////////
const ___CTX___ = bp.Event("___CTX___", {})

const CtxStart = (ctx => bp.Event("CtxStart", ctx))
const CtxUpdate = (ctx => bp.Event("CtxUpdate", ctx))
const CtxEnd = ((name, id) => bp.Event("CtxEnd", {name: name, id: id}))

const ctxEvents = ["CtxStart", "CtxUpdate", "CtxEnd"]

const AllButCTX = bp.EventSet("AllButCTX", e => (String(e.name) != "___CTX___"))
const AllButContext = bp.EventSet("AllButContext", e => (!ctxEvents.includes(String(e.name))))
const ContextEvents = bp.EventSet("ContextEvents", e => (ctxEvents.includes(String(e.name))))
const AnyEvents = AllButCTX

const AnyInContext = ((type,ctx) => bp.EventSet("Any(" + type + ")", e => e.name == type && e.data.id == ctx.id))

bthread("ContextHandler", function () {
        var CTX_Instance = Context.CreateInstance(new Map())
        while (true) {
            sync({waitFor: bp.all})
        }
})

function startContext(ctx) {
    sync({request: ___CTX___, block: AllButCTX});
    //TODO: throw an error if ctx.id exists
    Object.assign(ctx, {id: Context.getId()})
    var c = Object.assign({}, ctx)
    Context.GetInstance().set(c.id, c)
    sync({request: CtxStart(c), block: AllButContext});
    return c.id;
}

function updateContext(ctx) {
    sync({request: ___CTX___, block: AllButCTX});
    Object.assign(Context.GetInstance().get(ctx.id), ctx)
    sync({request: CtxUpdate(ctx), block: AllButContext});
    return ctx.id;
}

function getQueryResults(query) {
    // bp.sync({request: ___CTX___, block: AllButCTX}); //do we need this?
    var ans=[]
    for(let ctx of Context.GetInstance().values()) {
        if (query(ctx)) ans.push(ctx)
    }
    return ans
}

var cbt = function(name, q, bt) {
    bp.registerBThread("cbt: " + name, {interrupt:[]}, function () {
        var activated = {};
        while (true) {
            sync({waitFor: ContextEvents})
            for (let val of Context.GetInstance().values()) {
                // let ctx = val;
                let ctx = Object.assign({},val)
                if (!activated[ctx.id] && q.query(ctx)) {
                    activated[ctx.id] = true;
                    bp.registerBThread("Live copy"+ " (" + Context.getId() + "):" + name + " "+ctx.id,
                        {query: q.name, seed: ctx, interrupt: CtxEnd(q.name, ctx.id)},
                        function () {
                            bt(ctx)
                        });
                } else if (activated[ctx.id] && !q.query(ctx)) {
                    activated[ctx.id] = undefined
                    sync({request: CtxEnd(q.name, ctx.id), block: AllButContext})
                }
            }
        }
    })
}