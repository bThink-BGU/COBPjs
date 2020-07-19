var moves = bp.EventSet("Moves", function (e) {
    return e.name.equals("Move");
});

var whiteMoves = bp.EventSet("White Moves",function (e) {
    return moves.contains(e) && (e.data.source.piece !== null) && (Piece.Color.White.equals(e.data.source.piece.color));
});

var blackMoves = bp.EventSet("black Moves",function (e) {
    return moves.contains(e) && (e.data.source.piece !== null) && (Piece.Color.Black.equals(e.data.source.piece.color));
});

var outBoundsMoves = bp.EventSet("",function (e) {
    return moves.contains(e) && (e.data.source.row < 0 || e.data.source.row > 7 || e.data.source.col < 0 || e.data.source.col > 7 || e.data.target.row < 0 || e.data.target.row > 7 || e.data.target.col < 0 || e.data.target.col > 7);
});

var staticMoves = bp.EventSet("Non Moves",function (e) {
    return moves.contains(e) && (e.data.source.equals(e.data.target) || e.data.source.piece === null);
});

// Requirement : Turn Base Game, White Starts
/*bp.registerBThread("EnforceTurns",function ()
{
    while (true)
    {
        bp.sync({waitFor:whiteMoves,block:blackMoves});
        bp.sync({waitFor:blackMoves,block:whiteMoves});
    }
});

// Requirement : Moving Pieces only inside the board bounds.  - not in wikipedia
bp.registerBThread("Movement in bounds",function ()
{
    bp.sync({block:outBoundsMoves});
});

// Requirement : Move is allowed only if source has piece on it. - not in wikipedia
bp.registerBThread("Enforce Movement to a new cell", function ()
{
    bp.sync({block:staticMoves});
});

//<editor-fold desc="Pawn Rules">
CTX.subscribe("Move 2 forward", "UnmovedPawns", function (pawn) {
    bp.sync({waitFor:donePopulationEvent});

    var contextEndedEvent = CTX.AnyContextEndedEvent("UnmovedPawns",pawn);
    var forward = pawn.color.equals(Piece.Color.Black) ? -2 : 2;

    var currentCell = pawn.cell;
    var targetCell = getCell(currentCell.row + forward, currentCell.col);
    bp.sync({   request: getMove(currentCell, targetCell),
                interrupt: contextEndedEvent });
});

CTX.subscribe("Move 1 forward", "Pawns", function (pawn) {
    bp.sync({waitFor:donePopulationEvent});

    var contextEndedEvent = CTX.AnyContextEndedEvent("Pawns",pawn);
    var forward = pawn.color.equals(Piece.Color.Black) ? -1 : 1;

    var currentCell = pawn.cell;
    var targetCell = getCell(currentCell.row + forward, currentCell.col);
    bp.sync({   request: getMove(currentCell, targetCell),
        interrupt: contextEndedEvent });
});*/
//</editor-fold>

// Requirement : A piece moves to a vacant square except when capturing an opponent's piece
// Requirement-"translated" : A piece cannot move to an square occupied with an ally piece

