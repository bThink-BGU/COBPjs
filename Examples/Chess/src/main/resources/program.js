
//#region HELP FUNCTIONS
function getCell(i, j) {
    return CTX.getContextsOfType("Cell(" + i + "," + j + ")").get(0);
}
function getCellWithPiece(p) {
    try {
        return CTX.getContextsOfType("CellWithPiece(" + p + ")").get(0);
    } catch (e) {
        return null;
    }
}
function getPiece(p) {
    try {
        return CTX.getContextsOfType("SpecificPiece(" + p + ")").get(0);
    } catch (e) {
        return null;
    }
}
function getCellWithColor(c) {
    try {
        return CTX.getContextsOfType("CellWithColor(" + c + ")");
    } catch (e) {
        return null;
    }
}
function getCellWithType(t) {
    try {
        return CTX.getContextsOfType("CellWithType(" + t + ")");
    } catch (e) {
        return null;
    }
}
function getNonEmpty() {
    try {
        return CTX.getContextsOfType("NonEmptyCell");
    } catch (e) {
        return null;
    }
}
function getRealPiece(cell) {
    var piece = {};
    if (getCellWithType(Type.King).contains(cell)) {
        piece.type = "King";
    }
    else if (getCellWithType(Type.Rook).contains(cell)) {
        piece.type = "Rook";
    }
    else {
        bp.log.info("Error");
    }
    if (getCellWithColor(Color.White).contains(cell)) {
        piece.color = "White";
    }
    else {
        piece.color = "Black";
    }
    piece.id = 1;
    return getPiece(piece.color + "_" + piece.type + "_" + piece.id);
}
function getOppositeColor(cell) {

    if (getCellWithColor(Color.Black).contains(cell))
        return Color.White;
    return Color.Black;

}
function getMyColor(cell) {

    if (getCellWithColor(Color.Black).contains(cell))
        return Color.Black;
    return Color.White;

}
function getKingCell(isEnemy) {
    var KingCell;
    if (myColor.equals(Color.White)) {
        if (isEnemy)
            KingCell = getCellWithPiece(blackKing);
        else
            KingCell = getCellWithPiece(whiteKing);
    }
    else {
        if (isEnemy)
            KingCell = getCellWithPiece(whiteKing);
        else
            KingCell = getCellWithPiece(blackKing);
    }
    return KingCell;
}
function isRookInDanger(targetCell) {
    var enemyKingCell = getKingCell(true);
    var myKingCell = getKingCell(false);
    if (Math.abs(targetCell.i - enemyKingCell.i) <= 1 && Math.abs(targetCell.j - enemyKingCell.j) <= 1) {
        if (Math.abs(targetCell.i - myKingCell.i) <= 1 && Math.abs(targetCell.j - myKingCell.j) <= 1) {
            return false;
        }
        return true;
    }
    return false;
}
function getBestDirection() {
    var enemyKingCell = getKingCell(true);
    var right = 7 - enemyKingCell.i;
    var left = enemyKingCell.i;
    var up = 7 - enemyKingCell.j;
    var down = enemyKingCell.j;
    var min = right;
    var minName = "right";
    if (min > left) {
        min = left;
        minName = "left";
    }
    if (min > up) {
        min = up;
        minName = "up";
    }
    if (min > down) {
        min = down;
        minName = "down";
    }
    return minName;
}
function checkDiagonal(cell1, cell2) {
    if (Math.abs(cell1.i - cell2.i) === Math.abs(cell1.j - cell2.j))
        return true;
    return false;
}
function junctionList(list1, list2) {
    var ansList = [];
    for (var i = 0; i < list1.size(); i++) {
        if (list2.contains(list1.get(i))) {
            ansList.push(list1.get(i));
        }
    }
    return ansList;
}
function areKingsOpposite() {
    var myKing = getKingCell(false);
    var enemyKing = getKingCell(true);
    var colDistance = Math.abs(myKing.i - enemyKing.i);
    var rowDistance = Math.abs(myKing.j - enemyKing.j);
    if ((colDistance === 0 && rowDistance === 2) || (colDistance === 2 && rowDistance === 0)) {
        if (bestDirection === "right") {
            if (myKing.i < enemyKing.i)
                return true;
        }
        else if (bestDirection === "left") {
            if (myKing.i > enemyKing.i)
                return true;
        }
        else if (bestDirection === "up") {
            if (myKing.j < enemyKing.j)
                return true;
        }
        else if (bestDirection === "down") {
            if (myKing.j > enemyKing.j)
                return true;
        }
    }
    return false;
}
function isRookNearToTheKing(rook) {
    var enemyKing = getKingCell(true);
    var colDistance = Math.abs(rook.i - enemyKing.i);
    var rowDistance = Math.abs(rook.j - enemyKing.j);

    if (colDistance === 1 || rowDistance === 1) {
        return true;
    }
    return false;
}
function isInDirection(cell1) {
    var enemyKing = getKingCell(true);
    if (bestDirection === ("right")) {
        if (cell1.i < enemyKing.i)
            return true;
    }
    else if (bestDirection === ("left")) {
        if (cell1.i > enemyKing.i)
            return true;
    }
    else if (bestDirection === ("up")) {
        if (cell1.j < enemyKing.j)
            return true;
    }
    else if (bestDirection === ("down")) {
        if (cell1.j > enemyKing.j)
            return true;
    }
    return false;
}
function DetectKRKEndGame() {

    if (getCellWithColor(otherColor).size() > 1)
        return false;
    if (getCellWithColor(myColor).size() !== 2)
        return false;
    if (junctionList(getCellWithColor(myColor), getCellWithType(Type.Rook)).length !== 1)
        return false;


    return true;


}
//#endregion HELP FUNCTIONS

//#region GameRules
CTX.subscribe("EnforceTurns","GameStatePlaying", function(game) {
    while (true) {
        bp.sync({waitFor: Move.ColorMoveEventSet(Color.White), block: Move.ColorMoveEventSet(Color.Black), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});
        bp.sync({waitFor: Move.ColorMoveEventSet(Color.Black), block: Move.ColorMoveEventSet(Color.White), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});
    }
});

CTX.subscribe("UpdateBoardOnMove","GameStatePlaying", function(game) {
        while (true) {
            var move = bp.sync({waitFor: Move.AnyMoveEventSet(), interrupt: CTX.ContextEndedEvent("GameStatePlaying", game)});

            var transaction = [CTX.UpdateEvent("UpdateCell", {"cell": move.source, "piece": null}),
                CTX.UpdateEvent("UpdateCell", {"cell": move.target, "piece": move.piece})];
            if (move.target.piece != null) {
                transaction.push(CTX.UpdateEvent("DeletePiece", {"p": piece}));
            }
            bp.sync({
                block: Move.AnyMoveEventSet(),
                request: CTX.Transaction(transaction)
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


