function Cell(i,j, pieceId) {
  var cell = {id: i + j, type:'cell', i:i, j:j, pieceId: pieceId }
  return cell
}

function Piece(subtype,number, color, cellId) {
  var piece = { id: 'piece' + "_" + number, type: 'piece', subtype: subtype,
    number:number, color: color, cellId: cellId}
  return piece
}

/*  We have 64 cells on the chess board, we can identify them uniquely the combination of letters as columns and digits as rows, just like in the real gamke.
    For example, let's suppose we are playing white, the board will look like :

    a8 b8 c8 d8 e8 f8 g8 h8
    a7 b7 c7 d7 e7 f7 g7 h7
    a6 b6 c6 d6 e6 f6 g6 h6
    a5 b5 c5 d5 e5 f5 g5 h5
    a4 b4 c4 d4 e4 f4 g4 h4
    a3 b3 c3 d3 e3 f3 g3 h3
    a2 b2 c2 d2 e2 f2 g2 h2
    a1 b1 c1 d1 e1 f1 g1 h1

*/


ctx.registerQuery("Cell.all", entity => entity.type == 'cell')

ctx.registerQuery("Cell.all.nonOccupied", entity => entity.type == 'cell' && entity.pieceId == undefined)


ctx.registerQuery("Piece.White.Pawn" ,
  entity => entity.type == 'piece' && entity.subtype == 'Pawn' && entity.color == 'white')


//ctx.registerQuery("Piece.All", entity => entity.type.equals('piece'))
//ctx.registerQuery("Piece.Pawn", entity => entity.type.equals('piece') && entity.data.type.equals('Pawn'))
ctx.registerQuery("Phase.Opening", entity => entity.id == 'phase' && entity.phase == 'Opening')
ctx.registerQuery("Phase.MidGame", entity => entity.id=='phase' && entity.phase == 'mid game')
//ctx.registerQuery("Phase.Developing.Pawn", entity => entity.id=='phase' && entity.phase == 'opening' && entity.innerPhase == 'develop pawn')

// Game phase changed event
ctx.registerEffect("Game Phase", function(e) {
  bp.log.info("PHASE CHANGE")
  let phase = ctx.getEntityById("phase")
  phase.phase = e
  bp.log.info(phase)
  ctx.updateEntity(phase)
})

/*
ctx.registerEffect("Develop", function(e) {
  let entity = ctx.getEntityById("phase")
  entity.innerPhase = 'develop ' + e
  ctx.updateEntity(entity)
})
*/

ctx.registerEffect("Move", function(e) {
  bp.log.info(" NOTICE : MOVE EVENT IS HAPPENING ")
  let srcCell = ctx.getEntityById(e.src.toString())
  let dstCell = ctx.getEntityById(e.dst.toString())
  bp.log.info(srcCell)
  bp.log.info(dstCell)

  let srcPiece = null
  let dstPiece = null
  if(srcCell.pieceId != null) {
    srcPiece = ctx.getEntityById(srcCell.pieceId.toString())
  }
  if(dstCell.pieceId != null) {
    dstPiece = ctx.getEntityById(dstCell.pieceId.toString())
  }
  bp.log.info(srcPiece)
  bp.log.info(dstPiece)


  dstCell.pieceId = srcPiece.id
  ctx.updateEntity(dstCell)

  srcCell.pieceId = undefined
  ctx.updateEntity(srcCell)

  srcPiece.cellId = dstCell.id
  ctx.updateEntity(srcPiece)

  if(dstPiece)
    ctx.removeEntity(dstPiece)

  bp.log.info("MOVE HAS FINISHED")

})


const prefix = ["", "N", "B", "R", "Q", "K"];
const pieces = ["Pawn", "Knight", "Bishop", "Rook", "Queen", "King"];

function moveEvent(piece, oldCell, newCell) {
  // bp.log.info ("Move Event : " + piece + " " + newCell);
  return bp.Event("Move", {src: oldCell, dst:newCell});
}

function prefixOfPiece(piece) {
  let idx = 0
  for(let i = 0; i < pieces.length; i++) {
    if (pieces[i] == piece) {
      return prefix[idx]
    }
    else {
      idx++
    }
  }
}



bthread("populate data", function() {
  let pieces=[ Piece("King", 1, "White", "e1"),
    Piece("King", 2, "Black", "e8"),
    Piece("Queen", 3, "White", "d1"),
    Piece("Queen", 4, "Black", "d8"),
    Piece("Bishop", 5, "White", "c1"),
    Piece("Bishop", 6, "White", "f1"),
    Piece("Bishop", 7, "Black", "c8"),
    Piece("Bishop", 8, "Black", "f8"),
    Piece("Knight", 9, "White", "b1"),
    Piece("Knight", 10, "White", "g1"),
    Piece("Knight", 11, "Black", "b8"),
    Piece("Knight", 12, "Black", "g8"),
    Piece("Rook", 13, "White", "a1"),
    Piece("Rook", 14, "White", "h1"),
    Piece("Rook", 15, "Black", "a8"),
    Piece("Rook", 16, "Black", "h8"),


    Piece("Pawn", 21, "white", "a2"), Piece("Pawn", 22, "white", "b2"), Piece("Pawn", 23, "white", "c2"),
    Piece("Pawn", 24, "white", "d2"), Piece("Pawn", 25, "white", "e2"), Piece("Pawn", 26, "white", "f2"),
    Piece("Pawn", 27, "white", "g2"), Piece("Pawn", 28, "white", "h2"),
    Piece("Pawn", 31, "Black", "a7"), Piece("Pawn", 32, "Black", "b7"), Piece("Pawn", 33, "Black", "c7"),
    Piece("Pawn", 34, "Black", "d7"), Piece("Pawn", 35, "Black", "e7"), Piece("Pawn", 36, "Black", "f7"),
    Piece("Pawn", 37, "Black", "g7"), Piece("Pawn", 38, "Black", "h7") ]

  let cells=[

    Cell('a', '1', 'piece' + "_" + 13),
    Cell('b', '1', 'piece' + "_" + 9),
    Cell('c', '1', 'piece' + "_" + 5),
    Cell('d', '1', 'piece' + "_" + 3),
    Cell('e', '1', 'piece' + "_" + 1),
    Cell('f', '1', 'piece' + "_" + 6),
    Cell('g', '1', 'piece' + "_" + 10),
    Cell('h', '1', 'piece' + "_" + 14),

    Cell('a', '8', 'piece' + "_" + 15),
    Cell('b', '8', 'piece' + "_" + 11),
    Cell('c', '8', 'piece' + "_" + 7),
    Cell('d', '8', 'piece' + "_" + 4),
    Cell('e', '8', 'piece' + "_" + 2),
    Cell('f', '8', 'piece' + "_" + 8),
    Cell('g', '8', 'piece' + "_" + 12),
    Cell('h', '8', 'piece' + "_" + 16),

    Cell('a', '2', 'piece' + "_" + 21),
    Cell('b', '2', 'piece' + "_" + 22),
    Cell('c', '2', 'piece' + "_" + 23),
    Cell('d', '2', 'piece' + "_" + 24),
    Cell('e', '2', 'piece' + "_" + 25),
    Cell('f', '2', 'piece' + "_" + 26),
    Cell('g', '2', 'piece' + "_" + 27),
    Cell('h', '2', 'piece' + "_" + 28),
    Cell('a', '7', 'piece' + "_" + 31),
    Cell('b', '7', 'piece' + "_" + 32),
    Cell('c', '7', 'piece' + "_" + 33),
    Cell('d', '7', 'piece' + "_" + 34),
    Cell('e', '7', 'piece' + "_" + 35),
    Cell('f', '7', 'piece' + "_" + 36),
    Cell('g', '7', 'piece' + "_" + 37),
    Cell('h', '7', 'piece' + "_" + 38),

    Cell('a', '3', undefined), Cell('b', '3', undefined), Cell('c', '3', undefined), Cell('d', '3', undefined),
    Cell('e', '3', undefined), Cell('f', '3', undefined), Cell('g', '3', undefined), Cell('h', '3', undefined),
    Cell('a', '4', undefined), Cell('b', '4', undefined), Cell('c', '4', undefined), Cell('d', '4', undefined),
    Cell('e', '4', undefined), Cell('f', '4', undefined), Cell('g', '4', undefined), Cell('h', '4', undefined),
    Cell('a', '5', undefined), Cell('b', '5', undefined), Cell('c', '5', undefined), Cell('d', '5', undefined),
    Cell('e', '5', undefined), Cell('f', '5', undefined), Cell('g', '5', undefined), Cell('h', '5', undefined),
    Cell('a', '6', undefined), Cell('b', '6', undefined), Cell('c', '6', undefined), Cell('d', '6', undefined),
    Cell('e', '6', undefined), Cell('f', '6', undefined), Cell('g', '6', undefined), Cell('h', '6', undefined),
  ]

  ctx.beginTransaction()
  ctx.insertEntity("phase", "phase",{phase:""})
  // ctx.insertEntity("explanations", "explanation",{explanations: new Set()})
  cells.forEach(function(c) { ctx.insertEntity(c.id,'cell', c) })
  pieces.forEach(function(p) { ctx.insertEntity(p.id,'piece', p) })
  ctx.endTransaction()
})