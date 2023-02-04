//this file is used to test the following:
// a b-thread can be active only if it its context is active.
// Two contexts are used to test this.

let oneEntity = ctx.Entity("Context1", "type", {inContext: true})
let secondEntity = ctx.Entity("Context2", "type", {inContext: true})
ctx.populateContext([oneEntity, secondEntity])
ctx.registerQuery('Context1',
    function (entity) {
        return entity.id == 'Context1' && entity.inContext
    })
ctx.registerQuery('Context2',
    function (entity) {
        return entity.id == 'Context2' && entity.inContext
    } )

ctx.registerEffect('ToggleContext1', function (data) //can be divided into two effects one for true and one for false
{
    ctx.getEntityById('Context1').inContext = !ctx.getEntityById('Context1').inContext
})
ctx.registerEffect('ToggleContext2', function (data) //can be divided into two effects one for true and one for false
{
    ctx.getEntityById('Context2').inContext = !ctx.getEntityById('Context2').inContext
} )

bthread('ToggleContext1',function (entity) {
    while (true) {
        sync({request: Event('ToggleContext1')})
    }
})
bthread('ToggleContext2',function (entity) {
    while (true) {
        sync({request: Event('ToggleContext2')})
    }
} )

ctx.bthread("a in context1", "Context1", function (entity) {
    while (true) {
        sync({request: Event("a")})
        bp.ASSERT(entity.inContext, "a in context")
    }
})
ctx.bthread("a in context2", "Context2", function (entity) {
    while (true) {
        sync({request: Event("a")})
        bp.ASSERT(entity.inContext, "a in context")
    }
} )
bthread("alwaysOn", function () {
while (true) {
        sync({request: Event("b")})
    }
})


