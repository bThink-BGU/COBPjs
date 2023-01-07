//merge SampleProgram/bl.js and SampleProgram/dal.js to this file
//---------------------dal.js---------------------
/**
 * Populate initial contextual data
 */
ctx.populateContext([
    ctx.Entity("id", "type", {}),
    // more entities...
])

/**
 * Register what will happen when event 'haha' will be selected.
 */
ctx.registerEffect('haha', function (data) {
    ctx.insertEntity(ctx.Entity('a' + data, 'b'))
})

/**
 * Register queries
 */
ctx.registerQuery('B.All', function (entities) {
    return entities.filter(entity => entity.type == 'b')
})
ctx.registerQuery('B.<5', function (entities) {
    return entities.filter(entity => entity.type == 'b' && entity.hahaData < 5)
})
//---------------------bl.js---------------------
/**
 * A b-thread that is not bound to any context
 */
bthread("test", function () {
    sync({request: Event("haha", 5)})
    sync({request: Event("haha", 3)})
    let entity = ctx.getEntityById('a5')
    bp.log.info('entity with id "a5": {0}', entity)
})

/**
 * A b-thread that is bound to the {@name B.All} query.
 * A live copy of this b-thread will be spawned for each new answer to the query 'B.All'.
 * The answer will be passed to the {@param entity} parameter of the function.
 */
ctx.bthread('do something with b objects', 'B.All', function (entity) {
    bp.log.info('{0}: {1}', bp.thread.name, entity)
    ctx.runQuery((e)=>null.length)
    bp.log.info('{0}: {1}', bp.thread.name)
    sync({request: Event("doB")})
})

/**
 * A b-thread that is bound to the {@name B.<5} query.
 */
ctx.bthread('do something with b objects with hahaData smaller than 5', 'B.<5', function (entity) {
    bp.log.info('{0}: {1}', bp.thread.name, entity)
    sync({request: Event("doB")})
})
