ctx.registerQuery("Cell.All", entity => entity.type.equals('cell'))
ctx.registerQuery("Cell.Center", entity => entity.id.equals('cell(1,1)'))
ctx.registerQuery("Cell.Corner", entity => ['cell(0,0)', 'cell(2,0)', 'cell(0,2)', 'cell(2,2)'].indexOf(entity.id) >= 0)
ctx.registerQuery("Cell.Sides", entity => ['cell(0,1)', 'cell(1,0)', 'cell(2,1)', 'cell(1,2)'].indexOf(entity.id) >= 0)
ctx.registerQuery("Line.All", entity => entity.type.equals('line'))

ctx.populateContext(() => {
  for (let i = 0; i < 3; i++) {
    ctx.insertEntity('line_row_' + i, 'line', {c1: {i: i, j: 0}, c2: {i: i, j: 1}, c3: {i: i, j: 2}})
    ctx.insertEntity('line_col_' + i, 'line', {c1: {i: 0, j: i}, c2: {i: 1, j: i}, c3: {i: 2, j: i}})
    for (let j = 0; j < 3; j++) {
      ctx.insertEntity('cell(' + i + ',' + j + ')', 'cell', {i: i, j: j})
    }
  }
  ctx.insertEntity('line_diag_0', 'line', {c1: {i: 0, j: 0}, c2: {i: 1, j: 1}, c3: {i: 2, j: 2}})
  ctx.insertEntity('line_diag_1', 'line', {c1: {i: 2, j: 0}, c2: {i: 1, j: 1}, c3: {i: 0, j: 2}})
})
