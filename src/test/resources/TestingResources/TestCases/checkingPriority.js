//this file is used to test the following:
// a b-thread can be active only if it its context is active

let oneEntity = ctx.Entity("Context", "type", {inContext: true})
ctx.populateContext([oneEntity])

query('Context', function (e) {
    return e.id == 'Context' && e.inContext
} )

ctx.registerEffect('ToggleContext', function (data) //can be divided into two effects one for true and one for false
{
    ctx.getEntityById('Context').inContext = !ctx.getEntityById('Context').inContext
})

bthread('ToggleContext',function (entity) {
    // while (true) {
    sync({request: Event('ToggleContext')},9)
    // }
})

ctx.bthread("a in context", "Context", function (entity) {
    while (true) {
        sync({request: Event("a")},10)
        sync({request: Event("c")},1)

        bp.ASSERT(entity.inContext, "c in context")
    }
})
// bthread("alwaysOn", function () {
// while (true) {
//         sync({request: Event("b")})
//     }
// })