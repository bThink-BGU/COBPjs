bthread("test", function (){
  sync({request:bp.Event("haha", 5)})
  sync({request:bp.Event("haha", 3)})
  bp.log.info('entity with id "a5": {0}',ctx.getEntityById('a5'))
})


ctx.registerEffect('haha', function(data) {
  ctx.insertEntity('a'+data,'b',{hahaData:data})
})

ctx.registerQuery('B.All', entity=>entity.type=='b')
ctx.registerQuery('B.<5', entity=>entity.type=='b' && entity.hahaData < 5)

ctx.bthread('do something with b objects', 'B.All', function(entity) {
  bp.log.info('{0}: {1}', bp.thread.name, entity)
  sync({request: bp.Event("doB")})
})

ctx.bthread('do something with b objects with hahaData smaller than 5', 'B.<5', function(entity) {
  bp.log.info('{0}: {1}', bp.thread.name, entity)
  sync({request: bp.Event("doB")})
})