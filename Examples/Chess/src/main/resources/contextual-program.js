function Move(source,target) {
    return bp.Event("Move",{source:source,target:target});
}


function AnyMoveFrom(source) {
    return bp.EventSet("AnyMoveFrom "+ source, function (e) {
        return e.name.equals("Move") && e.data.source != null && e.data.source.equals(source);
    });
}
var moves = bp.EventSet("Moves", function (e) {
    return e.name.equals("Move");
});

var whiteMoves = bp.EventSet("White Moves",function (e) {
    return moves.contains(e) && (e.data.source.piece != null) && (Piece.Color.White.equals(e.data.source.piece.color));
});

var blackMoves = bp.EventSet("black Moves",function (e) {
    return moves.contains(e) && (e.data.source.piece != null) && (Piece.Color.Black.equals(e.data.source.piece.color));
});

var outBoundsMoves = bp.EventSet("",function (e) {
    return moves.contains(e) && (e.data.source.row < 0 || e.data.source.row > 7 || e.data.source.col < 0 || e.data.source.col > 7 || e.data.target.row < 0 || e.data.target.row > 7 || e.data.target.col < 0 || e.data.target.col > 7);
});

var donePopulationEvent = bp.EventSet("Start Event", function (e) {
    return e.name.equals("Done Populate");
});

// Requirement : Turn Base Game, White Starts

bp.registerBThread("EnforceTurns",function ()
{
    bp.sync({waitFor:donePopulationEvent});
    while (true)
    {
        bp.sync({waitFor:whiteMoves,block:blackMoves});
        bp.sync({waitFor:blackMoves,block:whiteMoves});
    }
});

// Requirement : Moving Pieces only inside the board bounds.  - not in wikipedia
bp.registerBThread("Movement in bounds",function ()
{
    bp.sync({waitFor:donePopulationEvent});
    bp.sync({block:outBoundsMoves});
});

//<editor-fold desc="Pawn Rules">
CTX.subscribe("Move 2 forward", "UnmovedPawns", function (pawn) {
    bp.sync({waitFor:donePopulationEvent});

    var contextEndedEvent = CTX.AnyContextEndedEvent("UnmovedPawns",pawn);
    var forward = pawn.color.equals(Piece.Color.Black) ? -2 : 2;
    var currentCell = pawn.cell;

    var targetCell = getCell(currentCell.row + forward, currentCell.col);
    bp.sync({   request: Move(currentCell, targetCell),
                interrupt: contextEndedEvent });
});

CTX.subscribe("Move 1 forward", "Pawns", function (pawn) {
    bp.sync({waitFor:donePopulationEvent});
    let contextEndedEvent = CTX.AnyContextEndedEvent("Pawns",pawn);
    let forward = pawn.color.equals(Piece.Color.Black) ? -1 : 1;
    let currentCell = pawn.cell;
    while(true) {
        let targetCell = getCell(currentCell.row + forward, currentCell.col);
        let e = bp.sync({
            request: Move(currentCell, targetCell), waitFor: AnyMoveFrom(currentCell),
            interrupt: contextEndedEvent
        });
        // bp.log.info(e);
        if(e.data.source.equals(currentCell)) {
            currentCell = getUpdatedCell(e.data.target);
            // CTX[e.data.target].getUpdatedEntity(pawn);
        }
    }
});


//</editor-fold>

// Requirement : A piece moves to a vacant square except when capturing an opponent's piece
// Requirement-"translated" : A piece cannot move to an square occupied with an ally piece

