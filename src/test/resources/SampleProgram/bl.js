/**
 * A b-thread that is not bound to any context
 */
bthread("test", function () {
  sync({request: Event("haha", 5)})
  sync({request: Event("haha", 3)})
  bp.log.info('entity with id "a5": {0}', ctx.getEntityById('a5'))
})

/**
 * A b-thread that is bound to the {@name B.All} query.
 * A live copy of this b-thread will be spawned for each new answer to the query 'B.All'.
 * The answer will be passed to the {@param entity} parameter of the function.
 */
ctx.bthread('do something with b objects', 'B.All', function (entity) {
  bp.log.info('{0}: {1}', bp.thread.name, entity)
  sync({request: Event("doB")})
})

/**
 * A b-thread that is bound to the {@name B.<5} query.
 */
ctx.bthread('do something with b objects with hahaData smaller than 5', 'B.<5', function (entity) {
  bp.log.info('{0}: {1}', bp.thread.name, entity)
  sync({request: Event("doB")})
})
