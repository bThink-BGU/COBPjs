//this file is used to test the following the priority of bthreads
//this is done by using a bthread that immediately turns off the context of the bthread that is supposed to be active
//the bthread that is supposed to turn the context off is the one with the higher priority

//This is currently not working because the bthreads are not sorted by priority
//? WHY?

let oneEntity = ctx.Entity("Context", "type", {inContext: true})
ctx.populateContext([oneEntity])
ctx.registerQuery('Context',
    function (entities) {
        return entities.filter(entity => entity.id == 'Context' && entity.inContext)
    })

ctx.registerEffect('ToggleContext', function (data) //can be divided into two effects one for true and one for false
{
    ctx.getEntityById('Context').inContext = !ctx.getEntityById('Context').inContext
})

bthread('ToggleContext',function (entity) {
        bp.sync({request: Event('ToggleContext')},11)
})

ctx.bthread("a in context", "Context", function (entity) {
    while (true) {
        bp.sync({request: Event("a")},10)
        bp.ASSERT(false, "a in context")
    }
})
bthread("alwaysOn", function () {
while (true) {
        sync({request: Event("b")})
    }
})


