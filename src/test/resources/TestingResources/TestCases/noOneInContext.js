//this file is used to test the following:
// a b-thread can be active only if it its context is active.
// In this case, no context is active, so no b-thread should be active.


let secondEntity = ctx.Entity("Always Off", "type", {inContext: false})
ctx.populateContext([secondEntity])



query('Always Off', function (e) {
    return e.id == 'Always Off' && e.inContext
} )

ctx.bthread("a in context Always Off", "Always Off", function (entity) {
    while (true) {
        sync({request: Event("a")})
        bp.ASSERT(false, "bthread is live but context is off")
    }
} )




