//this file is used to test the following:
// a b-thread can be active only if it its context is active.
// Two contexts are used to test this.


let oneEntity = ctx.Entity('Context1', 'type', { inContext: true })
let secondEntity = ctx.Entity('Context2', 'type', { inContext: true })
ctx.populateContext([oneEntity, secondEntity])
query('Context1', function (e) {
  return e.id == 'Context1' && e.inContext
})
query('Context2', function (e) {
    return e.id == 'Context2' && e.inContext
} )
// ctx.registerWholeDbQuery('Context1', function (entities) {
//     return entities.filter(e => e.id == 'Context1' && e.inContext)
// } )
// ctx.registerWholeDbQuery('Context2',
//   function (entities) {
//     return entities.filter(entity => entity.id == 'Context2' && entity.inContext)
//   })


ctx.registerEffect('ToggleContext1', function (data) //can be divided into two effects one for true and one for false
{
  ctx.getEntityById('Context1').inContext = !ctx.getEntityById('Context1').inContext
})
ctx.registerEffect('ToggleContext2', function (data) //can be divided into two effects one for true and one for false
{
  ctx.getEntityById('Context2').inContext = !ctx.getEntityById('Context2').inContext
})

bthread('ToggleContext1', function (entity) {
  while (true) {
    sync({ request: Event('ToggleContext1') })
  }
})
bthread('ToggleContext2', function (entity) {
  while (true) {
    sync({ request: Event('ToggleContext2') })
  }
})

ctx.bthread('a in context1', 'Context1', function (entity) {
  while (true) {
    sync({ request: Event('a') })
    bp.ASSERT(entity.inContext, 'a in context')
  }
})
ctx.bthread('a in context2', 'Context2', function (entity) {
  while (true) {
    sync({ request: Event('a') })
    bp.ASSERT(entity.inContext, 'a in context')
  }
})
bthread('alwaysOn', function () {
  while (true) {
    sync({ request: Event('b') })
  }
})


// if(typeof this['afterUpdatingToWholeDBQuery'] === 'undefined'  || this['afterUpdatingToWholeDBQuery'] === null) {
//     this['afterUpdatingToWholeDBQuery'] = false;
// }
// function query(name, func) {
//
//   // console.log("query: " + name);
//   bp.log.setLevel("Fine");
//
//   if (afterUpdatingToWholeDBQuery) {
//     bp.log.fine("whole db query");
//     ctx.registerWholeDbQuery(name,
//       function (entities) {
//         return entities.filter(e => func(e))
//       })
//   } else {
//     bp.log.fine("regular query");
//     ctx.registerQuery(name, func)
//   }
// }
