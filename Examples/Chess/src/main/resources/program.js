
//#region HELP FUNCTIONS
function getUpdatedGame() {
    return CTX.getContextInstances("GameStatePlaying").get(0)
}
//#endregion HELP FUNCTIONS

//#region GameRules
CTX.subscribe("MoveTheEnemy","GameStatePlaying", function(game) {
    while(true) {
        var e = bp.sync({
            waitFor: bp.EventSet('', function (e) {
                return e.name.equals("EnemyMove")
            })
        });
        var updatedGame = getUpdatedGame();
        var source = game.Board(e.data[0], e.data[1]);
        var target = game.Board(e.data[2], e.data[3]);
        bp.sync({request: Move(source, target)});
    }
});

/*CTX.subscribe("chessmove","GameStatePlaying", function(game) {
    bp.sync({waitFor: Move.AnyMoveEventSet(), superBlock: chessonmycolor, interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});
});*/

CTX.subscribe("EnforceTurns","GameStatePlaying", function(game) {
    while (true) {
        bp.sync({waitFor: Move.ColorMoveEventSet(Color.White), block: Move.ColorMoveEventSet(Color.Black), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});
        bp.sync({waitFor: Move.ColorMoveEventSet(Color.Black), block: Move.ColorMoveEventSet(Color.White), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});
    }
});

CTX.subscribe("UpdateBoardOnMove","GameStatePlaying", function(game) {
        while (true) {
            var move = bp.sync({waitFor: Move.AnyMoveEventSet(), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});

            var transaction = [
                CTX.UpdateEvent("SetPiece", {"cell": move.source, "piece": null}),
                CTX.UpdateEvent("SetPiece", {"cell": move.target, "piece": move.source.piece})];
            if (move.target.piece != null) {
                transaction.push(CTX.UpdateEvent("DeletePiece", {"p": piece}));
            }
            bp.sync({
                block: Move.AnyMoveEventSet(),
                request: CTX.TransactionEvent(transaction)
            });
        }
    });

CTX.subscribe("block moving to the same place","GameStatePlaying", function(game) {
    bp.sync({block: Move.SamePlaceMoveEventSet(), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});
});

CTX.subscribe("block out of board moves","GameStatePlaying", function(game) {
    bp.sync({block: Move.OutOfBoardMoveEventSet(), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});
});

//TODO if happened no eventselect && my turn(||go) && chess = math else tie
//#endregion GameRules

//#region Pieces moves
function whileLegalMove(start, game, maxDepth, cells, iFunc, jFunc) {
    var i = iFunc(start.i);
    var j = jFunc(start.j);
    var counter = 0;
    while(i < 8 && i>=0 && j < 8 && j >=0 && counter < maxDepth) {
        var c = game.Board(i, j);
        if (c.piece != null) {
            if (c.piece.color.equals(game.OpponentColor())) {
                cells.push(c);
            }
            return;
        } else {
            cells.push(c);
        }
        i = iFunc(i);
        j = jFunc(j);
        counter++;
    }
}

function cellsInStraightLines(start, game, maxDepth, cells) {
    //right
    whileLegalMove(start, game, maxDepth, cells, function(i) { return i;}, function(j) { return j+1;});

    //left
    whileLegalMove(start, game, maxDepth, cells, function(i) { return i;}, function(j) { return j-1;});

    //up
    whileLegalMove(start, game, maxDepth, cells, function(i) { return i-1;}, function(j) { return j;});

    //down
    whileLegalMove(start, game, maxDepth, cells, function(i) { return i+1;}, function(j) { return j;});
}

function cellsInDiagonals(start, game, maxDepth, cells) {
    //down + right
    whileLegalMove(start, game, maxDepth, cells, function(i) { return i+1;}, function(j) { return j+1;});

    //down + left
    whileLegalMove(start, game, maxDepth, cells, function(i) { return i+1;}, function(j) { return j-1;});

    //up + right
    whileLegalMove(start, game, maxDepth, cells, function(i) { return i-1;}, function(j) { return j+1;});

    //up + left
    whileLegalMove(start, game, maxDepth, cells, function(i) { return i-1;}, function(j) { return j-1;});
}

CTX.subscribe("Pieces Moves","GameStatePlaying", function(game) {
    CTX.subscribe("Rook moves", "Rook", function (rook) {
        if (rook.color.equals(game.OpponentColor())) {
            return;
        }
        game = getUpdatedGame();
        while(true) {
            var cell = CTX.getContextInstances("CellWithPiece_"+rook.getId()).get(0);
            var cells = [];
            cellsInStraightLines(cell, updatedGame, 8, cells);
            var possibleMoves = cells.map(function (c) {
                return new Move(start, c);
            });
            bp.sync({
                request: possibleMoves,
                waitFor: Move.AnyMoveEventSet(),
                interrupt: CTX.ContextEndedEvent("Rook", rook)
            });
        }
    });

    CTX.subscribe("King moves", "King", function (king) {
        if (king.color.equals(game.OpponentColor())) {
            return;
        }
        while (true) {
            var cell = CTX.getContextInstances("CellWithPiece_"+rook.getId()).get(0);
            var updatedGame = getUpdatedGame();
            var cells = [];
            cellsInStraightLines(cell, updatedGame, 1, cells);
            var possibleMoves = cells.map(function (c) {
                return new Move(start, c);
            });
            bp.sync({
                request: possibleMoves,
                waitFor: Move.AnyMoveEventSet(),
                interrupt: CTX.ContextEndedEvent("Rook", rook)
            });
        }
    });
});
//#endregion Pieces moves

//#region KingBehaviors
/*CTX.subscribe("DetectChessWhilePlaying","GameStatePlaying", function(game) {
    CTX.subscribe("DetectChess", "King", function (king) {
        if (king.color.equals(Color.Black)) {
            blackKing = king;
        } else {
            whiteKing = king;
        }
        if (myColor.equals(Color.Black))
            bp.sync({waitFor: bp.Event("EnginePlayed")});
        while (true) {
            var kingCell = game.BoardWithPiece(king);
            if (kingCell === null)
                break;
            if (isColor(kingCell, otherColor))
                break;
            var cells = [];
            var currentCell;
            var currentColor = getMyColor(kingCell);
            if (!kingController(kingCell, currentColor)) {
                bp.sync({request: bp.Event("Chess Event"), waitFor: bp.Event("My Color Played")});
            }
            bp.sync({waitFor: bp.Event("EnginePlayed")});
        }
    });
});*/
//#endregion KingBehaviors


