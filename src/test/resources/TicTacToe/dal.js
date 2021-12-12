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
    x: [[{ i: 0, j: 2 }, { i: 2, j: 0 }], [{ i: 0, j: 0 }, { i: 2, j: 2 }]],
    block: [{ i: 0, j: 1 }, { i: 1, j: 0 }, { i: 2, j: 1 }, { i: 1, j: 2 }]
  }
]

ctx.registerQuery('Cell.All', entity => entity.type == String('cell'))
ctx.registerQuery('Cell.Center', entity => entity.id.equals('cell(1,1)'))
ctx.registerQuery('Cell.Corner', entity => ['cell(0,0)', 'cell(2,0)', 'cell(0,2)', 'cell(2,2)'].indexOf(String(entity.id)) >= 0)
ctx.registerQuery('Cell.Sides', entity => ['cell(0,1)', 'cell(1,0)', 'cell(2,1)', 'cell(1,2)'].indexOf(String(entity.id)) >= 0)
ctx.registerQuery('Line.All', entity => entity.type.equals('line'))
ctx.registerQuery('Fork.All', entity => entity.type.equals('fork'))

let entities = []
for (let i = 0; i < 3; i++) {
  entities.push(ctx.Entity('line_row_' + i, 'line', { cells: [{ i: i, j: 0 }, { i: i, j: 1 }, { i: i, j: 2 }] }))
  entities.push(ctx.Entity('line_col_' + i, 'line', { cells: [{ i: 0, j: i }, { i: 1, j: i }, { i: 2, j: i }] }))
  for (let j = 0; j < 3; j++) {
    entities.push(ctx.Entity('cell(' + i + ',' + j + ')', 'cell', { location: { i: i, j: j } }))
  }
}
entities.push(ctx.Entity('line_diag_0', 'line', { cells: [{ i: 0, j: 0 }, { i: 1, j: 1 }, { i: 2, j: 2 }] }))
entities.push(ctx.Entity('line_diag_1', 'line', { cells: [{ i: 2, j: 0 }, { i: 1, j: 1 }, { i: 0, j: 2 }] }))

forks.forEach(function (f) {
  for (let i = 0; i < f.x.length; i++)
    for (let j = 0; j < f.block.length; j++)
      entities.push(ctx.Entity(f.name + '_' + i + '_' + j, 'fork', { cells: [f.x[i][0], f.x[i][1], f.block[j]] }))
})
ctx.populateContext(entities)