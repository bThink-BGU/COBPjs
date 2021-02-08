function Cell(i,j) {
  var cell = {id:i+","+j, type:'cell', i:i, j:j, pieceId: undefined}
  return cell
}

function Piece(subtype,number, color, cellId) {
  var piece = {id: type+"_"+number, type: 'piece', subtype: subtype, number:number, color: color, cellId: cellId}
  return piece
}

ctx.registerQuery("Piece.All", entity => entity.type.equals('piece'))
ctx.registerQuery("Piece.Pawn", entity => entity.type.equals('piece') && entity.data.type.equals('Pawn'))


bthread("populate data", function () {
  let cells=[]
  let pieces=[]

  ctx.beginTransaction()
  cells.forEach(function(c) { ctx.insertEntity('Cell('+c.id+')','cell',c) })
  pieces.forEach(function(p) { ctx.insertEntity('Piece('+p.id+')','piece',p) })
  ctx.endTransaction()
})
