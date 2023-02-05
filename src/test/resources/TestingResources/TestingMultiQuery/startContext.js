//file for testing the following:
//At the beginning of the program, the context for "a in context1 and context2" is not good for running because context1 is off
//We turn on context1, so the context for "a in context1 and context2" is good for running.
//we expect the b-thread to start running. and for the event "a" to happen only once.(because there is no loop in the b-thread)

let oneEntity = ctx.Entity("c1", "type", {inContext: false})
let secondEntity = ctx.Entity("c2", "type", {inContext: true})
ctx.populateContext([oneEntity, secondEntity])
ctx.registerWholeDbQuery('Context1', function (entities) {
    return entities.filter(entity => entity.id == 'c1' && entity.inContext)
})
ctx.registerWholeDbQuery('Context2', function (entities) {
    return entities.filter(entity => entity.id == 'c2' && entity.inContext)

})
ctx.registerEffect('TurnContext1On', function (data) {
    ctx.getEntityById('c1').inContext = true
})
ctx.bthread("a in context1 and context2", ["Context1", "Context2"], function (entity1, entity2) {

    // while (true) {
        sync({request: Event("a", {id1: entity1, id2: entity2})})

        bp.ASSERT(entity1.inContext && entity2.inContext, "a in context")
    // }



} )
// ctx.bthread("b in context2" , "Context2", function (entity1, entity2) {
//
//     // while (true) {
//     sync({request: Event("b")})
//
//     // bp.ASSERT(entity.inContext, "a in context")
//     // }
//
//
//
// } )
ctx.bthread("wake context1","Context2", function (entity2) {

    sync({request: Event("TurnContext1On", {id2: entity2.id})})

})
