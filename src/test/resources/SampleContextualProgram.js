/**
 * Populate initial contextual data
 */
ctx.populateContext(() => {
  ctx.insertEntity("id", "type", {})
  // more ctx.insertEntity...
})

/**
 * Register what will happen when event 'haha' will be selected.
 */
ctx.registerEffect('haha', function (data) {
  ctx.insertEntity('a' + data, 'b', {hahaData: data})
})

/**
 * Register queries
 */
ctx.registerQuery('B.All', entity => entity.type == 'b')
ctx.registerQuery('B.<5', entity => entity.type == 'b' && entity.hahaData < 5)

/**
 * A b-thread that is not bound to any context
 */
bthread("test", function () {
  sync({request: bp.Event("haha", 5)})
  sync({request: bp.Event("haha", 3)})
  bp.log.info('entity with id "a5": {0}', ctx.getEntityById('a5'))
})

/**
 * A b-thread that is bound to the {@name B.All} query.
 * A live copy of this b-thread will be spawned for each new answer to the query 'B.All'.
 * The answer will be passed to the {@param entity} parameter of the function.
 */
bthread('do something with b objects', 'B.All', function (entity) {
  bp.log.info('{0}: {1}', bp.thread.name, entity)
  sync({request: bp.Event("doB")})
})

/**
 * A b-thread that is bound to the {@name B.<5} query.
 */
bthread('do something with b objects with hahaData smaller than 5', 'B.<5', function (entity) {
  bp.log.info('{0}: {1}', bp.thread.name, entity)
  sync({request: bp.Event("doB")})
})
