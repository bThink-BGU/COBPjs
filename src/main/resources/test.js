bthread("test", function (){
  sync({request:bp.Event("haha", 5)})
  bp.log.info('entity is: {0}',ctx.getEntityById('a'))
})


ctx.registerEffect('haha', function(data) {
  ctx.insertEntity('a','b',{hahaData:data})
})