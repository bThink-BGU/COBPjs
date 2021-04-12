
ctx.registerQuery("Cell.All", entity => entity.type.equals('cell'))
ctx.registerQuery("Line.All", entity => entity.type.equals('line'))

bthread("populate data", function () {
  ctx.beginTransaction()
  for (var i = 0; i < 3; i++) {
    ctx.insertEntity('line_row_'+i, 'line', {c1: 'cell('+i+',0)', c2: 'cell('+i+',1)', c3: 'cell('+i+',2)'})
    ctx.insertEntity('line_col_'+i, 'line', {c1: 'cell(0,'+i+')', c2: 'cell(1,'+i+')', c3: 'cell(2,'+i+')'})
    for (var j = 0; j < 3; j++) {
      ctx.insertEntity('cell('+i+','+j+')', 'cell', {i: i, j: j})
    }
  }
  ctx.insertEntity('line_diag_0', 'line', {c1: 'cell(0,0)', c2: 'cell(1,1)', c3: 'cell(2,2)'})
  ctx.insertEntity('line_diag_1', 'line', {c1: 'cell(2,0)', c2: 'cell(1,1)', c3: 'cell(0,2)'})
  ctx.endTransaction()
})
