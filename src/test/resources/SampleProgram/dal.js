/**
 * Populate initial contextual data
 */
ctx.populateContext([
  ctx.Entity("id", "type", {}),
  // more ctx.insertEntity...
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
ctx.registerQuery('B.All', function (entity) {
  return entity.type == 'b'
})
ctx.registerQuery('B.<5', function (entity) {
  return entity.type == 'b' && entity.hahaData < 5
})
