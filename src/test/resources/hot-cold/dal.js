ctx.registerQuery('Room.WithTaps', entity => entity.type == 'room' && ['kitchen', 'bathroom'].indexOf(entity.subtype) > -1)

// Add entities
ctx.populateContext(() => {
  ctx.beginTransaction()
  ctx.insertEntity('r1', 'room', {subtype: 'kitchen'})
  ctx.insertEntity('r2', 'room', {subtype: 'bedroom'})
  ctx.insertEntity('r3', 'room', {subtype: 'bathroom'})
  ctx.endTransaction()
})