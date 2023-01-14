//this file is used to test the following:
// a b-thread can be active only if its context is active.
// Here, we check the case where the context is not active at the beginning of the program.
// and will be always off.
// Two contexts are used to test this.

let oneEntity = ctx.Entity("Context1", "type", {inContext: true})
let secondEntity = ctx.Entity("Always Off", "type", {inContext: false})
ctx.populateContext([oneEntity, secondEntity])
ctx.registerQuery('Context1',
    function (entity) {
        return entity.id == 'Context1' && entity.inContext
    })
ctx.registerQuery('Always Off',
    function (entity) {
        return entity.id == 'Always Off' && entity.inContext
    } )

ctx.registerEffect('ToggleContext1', function (data) //can be divided into two effects one for true and one for false
{
    ctx.getEntityById('Context1').inContext = !ctx.getEntityById('Context1').inContext
})

bthread('ToggleContext1',function (entity) {
    while (true) {
        sync({request: Event('ToggleContext1')})
    }
})


ctx.bthread("a in context1", "Context1", function (entity) {
    while (true) {
        sync({request: Event("Context1")})
        // bp.ASSERT(entity.inContext, "bthread is live but context is off")
        // sync({request: Event("Error")})
    }
})
ctx.bthread("a in context Always Off", "Always Off", function (entity) {
    while (true) {
        sync({request: Event("Always Off")})
        // bp.ASSERT(false, "bthread is live but context is off")
    }
} )
bthread("alwaysOn", function () {
    while (true) {
        sync({request: Event("alwaysOn")})
    }
})



