//this file is used to test the following:
// a b-thread can be active only if it its context is active.
// Two contexts are used to test this.


let secondEntity = ctx.Entity("Always Off", "type", {inContext: false})
ctx.populateContext([secondEntity])

ctx.registerQuery('Always Off',
    function (entity) {
        return entity.id == 'Always Off' && entity.inContext
    } )

ctx.bthread("a in context Always Off", "Always Off", function (entity) {
    while (true) {
        sync({request: Event("a")})
        bp.ASSERT(false, "bthread is live but context is off")
    }
} )




