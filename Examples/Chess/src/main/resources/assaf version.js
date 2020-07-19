importPackage(Packages.chess.DAL);

//<editor-fold desc="EventSet">

var donePopulationEvent = bp.EventSet("Start Event", function (e) {
    return e.name.equals("Done Populate");
});

//<editor-fold desc="Moves">
var moves = bp.EventSet("Moves", function (e)
{
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

var sameTeamCaptureMoves = bp.EventSet("Same Team Capture Moves",function (e)
{
    return moves.contains(e) && (e.data.source.piece !== null) && (e.data.target.piece !== null) && (e.data.source.piece.color.equals(e.data.target.piece.color));
});

//</editor-fold>

//</editor-fold>

//<editor-fold desc="Helper Functions">
function getMove(source,target)
{
    return bp.Event("Move",{source:source,target:target});
}

function inRange(row,column)
{
    return row >= 0 && row < 8 && column >= 0 && column < 8;
}

function allMovesFromSourceExcept(source, exceptGroup)
{
    var options = [];

    for(var row = 0; row < 8; row++)
    {
        for(var col = 0; col < 8; col++)
        {
            if(row != source.row || col != source.col)
            {
                var move = Move(source,getCell(row,col));
                var found = false;

                for(var i = 0; i < exceptGroup.length && !found; i++)
                {
                    if(move.equals(exceptGroup[i])) found = true;
                }
                
                if(!found) options.push(move);
            }
        }
    }

    return options;
}
//</editor-fold>

//<editor-fold desc="General Rules">

// Requiremnet : Turn Base Game, White Starts
bp.registerBThread("EnforceTurns",function ()
{
    while (true)
    {
        bp.sync({waitFor:whiteMoves,block:blackMoves});

        bp.sync({waitFor:blackMoves,block:whiteMoves});
    }
});

// Requiremnet : A piece moves to a vacant square except when capturing an opponent's piece
bp.registerBThread("Only Capture Enemies", function ()
{
    bp.sync({block:sameTeamCaptureMoves});
});

// Requiremnet : Moving Pieces only inside the board bounds.
bp.registerBThread("Movement in bounds",function ()
{
    bp.sync({block:outBoundsMoves});
});
// Requiremnet : Move is allowed only if source has piece on it.
bp.registerBThread("Enforce Movement to a new cell", function ()
{
    bp.sync({block:staticMoves});
});
//</editor-fold>

//<editor-fold desc="Pawn Rules">
CTX.subscribe("Move forward", "Pawns", function (pawn) {
    var end = CTX.AnyContextEndedEvent("Pawns",pawn);
    var forward = pawn.color.equals(Piece.Color.Black) ? -1 : 1;

    bp.sync({waitFor:donePopulationEvent});

    while (true)
    {
        var currentCell = getCellByPiece(pawn);
        var targetCell = getCell(currentCell.row + forward,currentCell.col);

        if(targetCell.piece == null)
        {
            bp.sync({request: getMove(currentCell,targetCell),
            waitFor: moves,
            interrupt:end});
        } else {
            bp.sync({waitFor: moves, interrupt:end});
        }
    }
});

function pawnMoves(pawnCell)
{
    var optionalMoves = [];
    var myForward = pawnCell.piece.color.equals(Piece.Color.Black) ? -1 : 1;

    // Rule: "A pawn can capture an enemy piece on either of the two squares diagonally in front of the pawn (but cannot move to those squares if they are vacant)."
    if(inRange(pawnCell.row + myForward,pawnCell.col + 1) && getCell(pawnCell.row + myForward,pawnCell.col + 1).piece !== null) optionalMoves.push(Move(pawnCell, getCell(pawnCell.row + myForward, pawnCell.col + 1)));
    if(inRange(pawnCell.row + myForward,pawnCell.col - 1) && getCell(pawnCell.row + myForward,pawnCell.col - 1).piece !== null) optionalMoves.push(Move(pawnCell, getCell(pawnCell.row + myForward, pawnCell.col - 1)));
    // Rule: "A pawn moves straight forward one square, if that square is vacant."
    if(inRange(pawnCell.row + myForward,pawnCell.col) && getCell(pawnCell.row + myForward,pawnCell.col).piece === null) optionalMoves.push(Move(pawnCell, getCell(pawnCell.row + myForward, pawnCell.col)));
    // Rule: "If it has not yet moved, a pawn also has the option of moving two squares straight forward, provided both squares are vacant."
    if(inRange(pawnCell.row + myForward,pawnCell.col) && inRange(pawnCell.row + myForward*2,pawnCell.col) && getCell(pawnCell.row + myForward,pawnCell.col).piece === null && getCell(pawnCell.row + myForward*2,pawnCell.col).piece === null) optionalMoves.push(Move(pawnCell, getCell(pawnCell.row + myForward*2, pawnCell.col)));

    return optionalMoves;
}
//</editor-fold>
