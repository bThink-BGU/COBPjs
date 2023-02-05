//file for testing the following:
//At the beginning of the program, the context for "a in context1 and context2" is good for running
//After the first a, we turn off context1, so the context for "a in context1 and context2" is not good for running.
//we expect the b-thread to stop running. and for the event "a" to happen only once.

let oneEntity = ctx.Entity("Context1", "type", {inContext: true})
let secondEntity = ctx.Entity("Context2", "type", {inContext: true})
ctx.populateContext([oneEntity, secondEntity])
ctx.registerWholeDbQuery('Context1', function (entities) {
    return entities.filter(entity => entity.id === 'Context1' && entity.inContext)
})
ctx.registerWholeDbQuery('Context2', function (entities) {
    return entities.filter(entity => entity.id === 'Context2' && entity.inContext)

})
ctx.registerEffect('TurnContext1Off', function (data) {
    ctx.getEntityById('Context1').inContext = false
})
ctx.bthread("a in context1 and context2", ["Context1", "Context2"], function (entity1, entity2) {

    while (true) {
        sync({request: Event("a")})

        bp.ASSERT(entity1.inContext && entity2.inContext, "a happened but one of the contexts is off")
    }



} )
ctx.bthread("b in context1" , "Context1", function (entity1) {

    while (true) {
        sync({request: Event("b")})
        bp.ASSERT(entity1.inContext, "b happened but context1 is off")
    }
} )
bthread("end context1", function () {
    sync({waitFor: Event("a")})
    sync({request: Event("TurnContext1Off"),block:Event("a") })
})
