
//#region HELP FUNCTIONS

//#endregion HELP FUNCTIONS

//#region GameRules
CTX.subscribe("MoveTheEnemy","GameStatePlaying", function(game) {
    while(true) {
        var e = bp.sync({
            waitFor: bp.EventSet('', function (e) {
                return e.name.equals("EnemyMove")
            })
        });
        var updatedGame = CTX.getContextInstances("GameStatePlaying").get(0);
        var source = game.Board(e.data[0], e.data[1]);
        var target = game.Board(e.data[2], e.data[3]);
        bp.sync({request: Move(source, target)});
    }
});

CTX.subscribe("EnforceTurns","GameStatePlaying", function(game) {
    while (true) {
        bp.sync({waitFor: Move.ColorMoveEventSet(Color.White), block: Move.ColorMoveEventSet(Color.Black), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});
        bp.sync({waitFor: Move.ColorMoveEventSet(Color.Black), block: Move.ColorMoveEventSet(Color.White), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});
    }
});

CTX.subscribe("UpdateBoardOnMove","GameStatePlaying", function(game) {
        while (true) {
            var move = bp.sync({waitFor: Move.AnyMoveEventSet(), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});

            var transaction = [CTX.UpdateEvent(
                "SetPiece", {"cell": move.source, "piece": null}),
                CTX.UpdateEvent("SetPiece", {"cell": move.target, "piece": move.piece})];
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
function movesInFourDirections(start, game) {
    var cells = [];
    //right
    for (var i = start.i + 1; i < size; i++) {
        var c = game.Board(i, start.j);
        if (c.piece != null) {
            if (c.piece.color.equals(game.OpponentColor())) {
                cells.push(c);
            }
            break;
        }
        cells.push(c);
    }
    //left
    for (var i = start.i - 1; i >= 0; i--) {
        var c = game.Board(i, start.j);
        if (c.piece != null) {
            if (c.piece.color.equals(game.OpponentColor())) {
                cells.push(c);
            }
            break;
        }
        cells.push(c);

    }
    //up
    for (var j = start.j + 1; j < size; j++) {
        var c = game.Board(start.i, j);

        if (c.piece != null) {
            if (c.piece.color.equals(game.OpponentColor())) {
                cells.push(c);
            }
            break;
        }
        cells.push(c);
    }
    //down
    for (var j = start.j - 1; j >= 0; j--) {
        var c = game.Board(start.i, j);
        if (c.piece != null) {
            if (c.piece.color.equals(game.OpponentColor())) {
                cells.push(c);
            }
            break;
        }
        cells.push(c);
    }

    return cells.map(function (c) {
        return new Move(start, c);
    });
}

CTX.subscribe("Pieces Moves","GameStatePlaying", function(game) {
    CTX.subscribe("Rook moves", "Rook", function (rook) {
        if (rook.color.equals(game.OpponentColor())) {
            return;
        }
        while (true) {
            var cell = game.BoardWithPiece(rook);
            bp.sync({
                request: movesInFourDirections(cell),
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
            var cell = game.BoardWithPiece(king);
            var cells = [
                game.Board(cell.i - 1, cell.j),
                game.Board(cell.i - 1, cell.j + 1),
                game.Board(cell.i, cell.j + 1),
                game.Board(cell.i + 1, cell.j + 1),
                game.Board(cell.i + 1, cell.j),
                game.Board(cell.i + 1, cell.j - 1),
                game.Board(cell.i, cell.j - 1),
                game.Board(cell.i - 1, cell.j - 1)
            ];
            var moves = cells.map(function (c) {
                return new Move(start, c);
            });
            bp.sync({
                request: moves,
                waitFor: Move.AnyMoveEventSet(),
                interrupt: CTX.ContextEndedEvent("King", king)
            });
        }
    });
});
//#endregion Pieces moves

//#region KingBehaviors
CTX.subscribe("DetectChessWhilePlaying","GameStatePlaying", function(game) {
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
});
//#endregion KingBehaviors


