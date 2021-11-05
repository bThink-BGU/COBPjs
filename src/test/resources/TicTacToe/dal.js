const forks = [
  {
    name: '22',
    x: [[{ i: 1, j: 2 }, { i: 2, j: 0 }], [{ i: 2, j: 1 }, { i: 0, j: 2 }], [{ i: 1, j: 2 }, { i: 2, j: 1 }]],
    block: [{ i: 2, j: 2 }, { i: 0, j: 2 }, { i: 2, j: 0 }]
  },
  {
    name: '02',
    x: [[{ i: 1, j: 2 }, { i: 0, j: 0 }], [{ i: 0, j: 1 }, { i: 2, j: 2 }], [{ i: 1, j: 2 }, { i: 0, j: 1 }]],
    block: [{ i: 0, j: 2 }, { i: 0, j: 0 }, { i: 2, j: 2 }]
  },
  {
    name: '20',
    x: [[{ i: 1, j: 0 }, { i: 2, j: 2 }], [{ i: 2, j: 1 }, { i: 0, j: 0 }], [{ i: 2, j: 1 }, { i: 1, j: 0 }]],
    block: [{ i: 2, j: 0 }, { i: 0, j: 0 }, { i: 2, j: 2 }]
  },
  {
    name: '00',
    x: [[{ i: 0, j: 1 }, { i: 2, j: 0 }], [{ i: 1, j: 0 }, { i: 0, j: 2 }], [{ i: 0, j: 1 }, { i: 1, j: 0 }]],
    block: [{ i: 0, j: 0 }, { i: 0, j: 2 }, { i: 2, j: 0 }]
  },
  {
    name: 'diag',
    x: [[{ i: 0, j: 2 }, { i: 2, j: 0 }],[{ i: 0, j: 0 }, { i: 2, j: 2 }]],
    block: [{ i: 0, j: 1 }, { i: 1, j: 0 }, { i: 2, j: 1 }, { i: 1, j: 2 }]
  }
]

ctx.registerQuery('Cell.All', entity => entity.type==String('cell'))
ctx.registerQuery('Cell.Center', entity => entity.id.equals('cell(1,1)'))
ctx.registerQuery('Cell.Corner', entity => ['cell(0,0)', 'cell(2,0)', 'cell(0,2)', 'cell(2,2)'].indexOf(String(entity.id)) >= 0)
ctx.registerQuery('Cell.Sides', entity => ['cell(0,1)', 'cell(1,0)', 'cell(2,1)', 'cell(1,2)'].indexOf(String(entity.id)) >= 0)
ctx.registerQuery('Line.All', entity => entity.type.equals('line'))
ctx.registerQuery('Fork.All', entity => entity.type.equals('fork'))

let entities = []
for (let i = 0; i < 3; i++) {
  entities.push(ctx.Entity('line_row_' + i, 'line', { c1: { i: i, j: 0 }, c2: { i: i, j: 1 }, c3: { i: i, j: 2 } }))
  entities.push(ctx.Entity('line_col_' + i, 'line', { c1: { i: 0, j: i }, c2: { i: 1, j: i }, c3: { i: 2, j: i } }))
  for (let j = 0; j < 3; j++) {
    entities.push(ctx.Entity('cell(' + i + ',' + j + ')', 'cell', { i: i, j: j }))
  }
}
entities.push(ctx.Entity('line_diag_0', 'line', { c1: { i: 0, j: 0 }, c2: { i: 1, j: 1 }, c3: { i: 2, j: 2 } }))
entities.push(ctx.Entity('line_diag_1', 'line', { c1: { i: 2, j: 0 }, c2: { i: 1, j: 1 }, c3: { i: 0, j: 2 } }))

forks.forEach(function (f) {
  for (let i = 0; i < f.x.length; i++)
    entities.push(ctx.Entity(f.name + '_' + i, 'fork', { x: f.x[i], block: f.block }))
})
ctx.populateContext(entities)