// Add entities
ctx.populateContext([
  ctx.Entity('r1', 'room', {subtype: 'kitchen'}),
  ctx.Entity('r2', 'room', {subtype: 'bedroom'}),
  ctx.Entity('r3', 'room', {subtype: 'bathroom'}),
])

// Specifies the contexts/layers preconditions
ctx.registerQuery('Night',
  function (entity) {
    return entity.id == 'night'
  })
ctx.registerQuery('Room.WithTaps',
  function (entity) {
    return entity.type == 'room' &&
      entity.subtype == 'kitchen' || entity.subtype == 'bathroom'
  })

// Specify the effect of certain events on the context
ctx.registerEffect('night begins', function (data) {
  ctx.insertEntity(ctx.Entity('night', 'system'))
})
ctx.registerEffect('night ends', function (data) {
  ctx.removeEntity('night')
})

ctx.registerQuery('Room.Kitchen',
  function (entity) {
    return entity.type == 'room' &&
      'kitchen' == entity.subtype
  })