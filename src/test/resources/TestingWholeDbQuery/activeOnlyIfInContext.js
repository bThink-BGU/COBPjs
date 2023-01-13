//this file is used to test the following:
// a b-thread can be active only if it its context is active

let oneEntity = ctx.Entity("Context", "type", {inContext: true})
ctx.populateContext([oneEntity])
ctx.registerQuery('Context',
    function (entities) {
        return entities.filter(entity => entity.id == 'Context' && entity.inContext)
    },true)

ctx.registerEffect('ToggleContext', function (data) //can be divided into two effects one for true and one for false
{
    ctx.getEntityById('Context').inContext = !ctx.getEntityById('Context').inContext
})

bthread('ToggleContext',function (entity) {
    while (true) {
        sync({request: Event('ToggleContext')})
    }
})

ctx.bthread("a in context", "Context", function (entity) {
    while (true) {
        sync({request: Event("a")})
        bp.ASSERT(entity.inContext, "a in context")
    }
})
bthread("alwaysOn", function () {
    while (true) {
        sync({request: Event("b")})
    }
})


