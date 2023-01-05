// Add entities
ctx.populateContext([
  // ctx.Entity('r1', 'room', { subtype: 'kitchen' }),
  // ctx.Entity('r2', 'room', { subtype: 'bedroom' }),
  ctx.Entity('r3', 'room', { subtype: 'bathroom' }),
  ctx.Entity('night', 'system', { on: false })
])

// Specifies the contexts/layers preconditions
ctx.registerQuery('Night',
  function (entity) {
    return entity.id == 'night' && entity.on
  })
ctx.registerQuery('Room.WithTaps',
  function (entity) {
    return entity.type == 'room' &&
      entity.subtype == 'kitchen' || entity.subtype == 'bathroom'
  })

// Specify the effect of certain events on the context
ctx.registerEffect('time 21:00', function (data) {
  ctx.getEntityById('night').on = true
})
ctx.registerEffect('time 08:00', function (data) {
  ctx.getEntityById('night').on = false
})

ctx.registerQuery('Room.Kitchen',
  function (entity) {
    return entity.type == 'room' &&
      'kitchen' == entity.subtype
  })