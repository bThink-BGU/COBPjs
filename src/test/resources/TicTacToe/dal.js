const forks = [
  {
    name: '22',
    x: [[{ i: Number(1), j: Number(2) }, { i: Number(2), j: Number(0) }], [{ i: Number(2), j: Number(1) }, { i: Number(0), j: Number(2) }], [{ i: Number(1), j: Number(2) }, { i: Number(2), j: Number(1) }]],
    block: [{ i: Number(2), j: Number(2) }, { i: Number(0), j: Number(2) }, { i: Number(2), j: Number(0) }]
  },
  {
    name: '02',
    x: [[{ i: Number(1), j: Number(2) }, { i: Number(0), j: Number(0) }], [{ i: Number(0), j: Number(1) }, { i: Number(2), j: Number(2) }], [{ i: Number(1), j: Number(2) }, { i: Number(0), j: Number(1) }]],
    block: [{ i: Number(0), j: Number(2) }, { i: Number(0), j: Number(0) }, { i: Number(2), j: Number(2) }]
  },
  {
    name: '20',
    x: [[{ i: Number(1), j: Number(0) }, { i: Number(2), j: Number(2) }], [{ i: Number(2), j: Number(1) }, { i: Number(0), j: Number(0) }], [{ i: Number(2), j: Number(1) }, { i: Number(1), j: Number(0) }]],
    block: [{ i: Number(2), j: Number(0) }, { i: Number(0), j: Number(0) }, { i: Number(2), j: Number(2) }]
  },
  {
    name: '00',
    x: [[{ i: Number(0), j: Number(1) }, { i: Number(2), j: Number(0) }], [{ i: Number(1), j: Number(0) }, { i: Number(0), j: Number(2) }], [{ i: Number(0), j: Number(1) }, { i: Number(1), j: Number(0) }]],
    block: [{ i: Number(0), j: Number(0) }, { i: Number(0), j: Number(2) }, { i: Number(2), j: Number(0) }]
  },
  {
    name: 'diag',
    x: [[{ i: Number(0), j: Number(2) }, { i: Number(2), j: Number(0) }], [{ i: Number(0), j: Number(0) }, { i: Number(2), j: Number(2) }]],
    block: [{ i: Number(0), j: Number(1) }, { i: Number(1), j: Number(0) }, { i: Number(2), j: Number(1) }, { i: Number(1), j: Number(2) }]
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
  entities.push(ctx.Entity('line_row_' + i, 'line', { cells: [{ i: Number(i), j: Number(0) }, { i: Number(i), j: Number(1) }, { i: Number(i), j: Number(2) }] }))
  entities.push(ctx.Entity('line_col_' + i, 'line', { cells: [{ i: Number(0), j: Number(i) }, { i: Number(1), j: Number(i) }, { i: Number(2), j: Number(i) }] }))
  for (let j = 0; j < 3; j++) {
    entities.push(ctx.Entity('cell(' + i + ',' + j + ')', 'cell', { location: { i: Number(i), j: Number(j) } }))
  }
}
entities.push(ctx.Entity('line_diag_0', 'line', { cells: [{ i: Number(0), j: Number(0) }, { i: Number(1), j: Number(1) }, { i: Number(2), j: Number(2) }] }))
entities.push(ctx.Entity('line_diag_1', 'line', { cells: [{ i: Number(2), j: Number(0) }, { i: Number(1), j: Number(1) }, { i: Number(0), j: Number(2) }] }))

forks.forEach(function (f) {
  for (let i = 0; i < f.x.length; i++)
    for (let j = 0; j < f.block.length; j++)
      entities.push(ctx.Entity(f.name + '_' + i + '_' + j, 'fork', { cells: [f.x[i][0], f.x[i][1], f.block[j]] }))
})
ctx.populateContext(entities)