//file for testing the following:
//case 1: both contexts are false
// At the beginning of the program, the context for "a in context1 and context2" is not good for running because context1 and context2 are off(one of them is enough)
//After the first a, we turn on context1, but the context for "a in context1 and context2" is still not good for running.
//we expect the b-thread never to start running.
//case 2: context1 is false, context2 is true
//At the beginning of the program, the context for "a in context1 and context2" is not good for running because context1 is off
//We turn on context1, so the context for "a in context1 and context2" is good for running.
//we expect the b-thread to start running. and for the event "a" to happen, until we turn off context1 again and then on again.


let case1 = [false, false]
let case2 = [false, true]
let oneEntity = ctx.Entity("c1", "type", {inContext: case2[0]})
let secondEntity = ctx.Entity("c2", "type", {inContext: case2[1]})//works for true as well
ctx.populateContext([oneEntity, secondEntity])
ctx.registerWholeDbQuery('Context1', function (entities) {
    return entities.filter(entity => entity.id == 'c1' && entity.inContext)
})
ctx.registerWholeDbQuery('Context2', function (entities) {
    return entities.filter(entity => entity.id == 'c2' && entity.inContext)

})
ctx.registerEffect('ToggleContext1', function (data) {
    ctx.getEntityById('c1').inContext = !ctx.getEntityById('c1').inContext
})
ctx.registerEffect('ToggleContext2', function (data) {
    ctx.getEntityById('c2').inContext = !ctx.getEntityById('c2').inContext
} )
ctx.bthread("a in context1 and context2", ["Context1", "Context2"], function (entity1, entity2) {

    while (true) {
        sync({request: Event("a", {id1: entity1, id2: entity2})})
        bp.ASSERT(entity1.inContext && entity2.inContext, "a in context")
    }
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
ctx.bthread("toggle context1", function () {

    while (true) {
        sync({request: Event("ToggleContext1")})
    }


})
