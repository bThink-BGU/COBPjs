function Cell(i,j) {
    var cell = {id:i+","+j, type:'Cell', i:i, j:j, pieceId: undefined}
    return cell
}

function Piece(subtype,number, color, cellId) {
    var piece = {id: type+"_"+number, type: 'Piece', subtype: subtype, number:number, color: color, cellId: cellId}
    return piece
}

CTX.registerQuery("Piece.All", entity => entity.type.equals('Piece'))
CTX.registerQuery("Piece.Pawn", entity => entity.type.equals('Piece') && entity.data.type.equals('Pawn'))

CTX.registerEffect("SetupBoard", (bp, e) => {
    CTX.beginTransaction()
    e.data.forEach(entity => CTX.insertEntity(CTX.createEntity(entity.id, entity.type, entity)))
    CTX.endTransaction()
})

bthread("populate data", function () {
    let cells=[]
    let pieces=[]

    sync({request: bp.Event("SetUpBoard", cells.concat(pieces))}, 100)
})
