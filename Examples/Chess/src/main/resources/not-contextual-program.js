// /**
//  * Created by Ronit on 02-Oct-18.
//  */

importPackage(Packages.il.ac.bgu.cs.bp.bpjs.Chess.events);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.Chess.context.schema);


var isGameEnded = bp.EventSet("GameEnded Events", function (e) {
    return (e instanceof GameEnded);
});

var isAMove = bp.EventSet("Move events", function (e) {
    return (e instanceof AMove);
});

var isAmoveInPlace = bp.EventSet("AMove In Place events", function (e) {
    if (e instanceof AMove) {
        return (e.getSourceX() === e.getTargetX() && e.getSourceY() === e.getTargetY());
    }
    return false;
});

var isWhiteMove = bp.EventSet("White Move events", function (e) {
    if (e instanceof AMove) {
        return e.getPiece().getColor() === Piece.Color.white;
    }
    return false;
});

var isBlackMove = bp.EventSet("Black Move events", function (e) {
    if (e instanceof AMove) {
        return e.getPiece().getColor() === Piece.Color.black;
    }
    return false;
});

var outOfBoardMove = bp.EventSet("Illegal out of board moves", function (e) {
    if (e instanceof AMove) {
        return (e.getTargetX() < 0 || e.getTargetX() > 7 || e.getTargetY() < 0 || e.getTargetY() > 7);
    }
    return false;
});

var arrOfAMoveTo = new Array(8);
var arrOfAMoveFrom = new Array(8);
var arrOfMoveTo = new Array(8);
var arrOfEatTo = new Array(8);
var arrOfAMoveWhiteTo = new Array(8);
var arrOfAMoveBlackTo = new Array(8);
var arrOfEatWhiteTo = new Array(8);
var arrOfEatBlackTo = new Array(8);


function cellInitialization(i, j) {
    //  EVENT SETS FOR CELLS
    arrOfAMoveTo[i][j] = bp.EventSet("AMove To_" + i + "_" + j, function (e) {
        if (e instanceof AMove) {
            return (e.getTargetX() === i && e.getTargetY() === j);
        }
        return false;
    });
    arrOfAMoveFrom[i][j] = bp.EventSet("AMove From_" + i + "_" + j, function (e) {
        if (e instanceof AMove) {
            return (e.getSourceX() === i && e.getSourceY() === j);
        }
        return false;
    });
    arrOfMoveTo[i][j] = bp.EventSet("Move To_" + i + "_" + j, function (e) {
        if (e instanceof Move) {
            return (e.getTargetX() === i && e.getTargetY() === j);
        }
        return false;
    });
    arrOfEatTo[i][j] = bp.EventSet("Eat To_" + i + "_" + j, function (e) {
        if (e instanceof Eat) {
            return (e.getTargetX() === i && e.getTargetY() === j);
        }
        return false;
    });
    arrOfAMoveWhiteTo[i][j] = bp.EventSet("AMoveWhite To _" + i + "_" + j, function (e) {
        if (e instanceof AMove) {
            return (e.getTargetX() === i && e.getTargetY() === j && e.getPiece().getColor() === Piece.Color.white);
        }
        return false;
    });
    arrOfAMoveBlackTo[i][j] = bp.EventSet("AMoveBlack To _" + i + "_" + j, function (e) {
        if (e instanceof AMove) {
            return (e.getTargetX() === i && e.getTargetY() === j && e.getPiece().getColor() === Piece.Color.black);
        }
        return false;
    });
    arrOfEatWhiteTo[i][j] = bp.EventSet("EatWhite To _" + i + "_" + j, function (e) {
        if (e instanceof Eat) {
            return (e.getTargetX() === i && e.getTargetY() === j && e.getPiece().getColor() === Piece.Color.white);
        }
        return false;
    });
    arrOfEatBlackTo[i][j] = bp.EventSet("EatBlack To _" + i + "_" + j, function (e) {
        if (e instanceof Eat) {
            return (e.getTargetX() === i && e.getTargetY() === j && e.getPiece().getColor() === Piece.Color.black);
        }
        return false;
    });

    // BTHREADS FOR CELLS
    bp.registerBThread("block move to occupied cell_" + i + "_" + j, function () {
        while (true) {
            bp.sync({waitFor: arrOfAMoveTo[i][j]});
            bp.sync({block: arrOfMoveTo[i][j], waitFor: arrOfAMoveFrom[i][j]});
        }
    });
    bp.registerBThread("block move from empty cell_" + i + "_" + j, function () {
        while (true) {
            bp.sync({waitFor: arrOfAMoveTo[i][j], block: arrOfAMoveFrom[i][j]});
            bp.sync({waitFor: arrOfAMoveFrom[i][j]});
        }
    });
    bp.registerBThread("block eat to empty cell_" + i + "_" + j, function () {
        while (true) {
            bp.sync({waitFor: arrOfAMoveTo[i][j], block: arrOfEatTo[i][j]});
            bp.sync({waitFor: arrOfAMoveFrom[i][j]});
        }
    });
    bp.registerBThread("block eat black to black_" + i + "_" + j, function () {
        while (true) {
            bp.sync({waitFor: arrOfAMoveBlackTo[i][j]});
            bp.sync({waitFor: [arrOfAMoveWhiteTo[i][j], arrOfAMoveFrom[i][j]], block: arrOfEatBlackTo[i][j]});
        }
    });
    bp.registerBThread("block eat white to white" + i + "_" + j, function () {
        while (true) {
            bp.sync({waitFor: arrOfAMoveWhiteTo[i][j]});
            bp.sync({waitFor: [arrOfAMoveBlackTo[i][j], arrOfAMoveFrom[i][j]], block: arrOfEatWhiteTo[i][j]});
        }
    });
}
for (var i = 0; i < 8; i++) {
    arrOfAMoveTo [i] = new Array(8);
    arrOfAMoveFrom[i] = new Array(8);
    arrOfMoveTo[i] = new Array(8);
    arrOfEatTo[i] = new Array(8);
    arrOfAMoveWhiteTo[i] = new Array(8);
    arrOfAMoveBlackTo[i] = new Array(8);
    arrOfEatWhiteTo[i] = new Array(8);
    arrOfEatBlackTo[i] = new Array(8);
    for (var j = 0; j < 8; j++) {
        cellInitialization(i, j);
    }
}
bp.registerBThread("game_duration", function () {
    bp.sync({request: bp.Event("init_start")});
    bp.sync({waitFor: bp.Event("init_end")});
    bp.sync({request: bp.Event("game_start")});
    bp.sync({waitFor: isGameEnded});
});



function rookBTs(color, id) {
    var rookMoveES = bp.EventSet(color + " rook " + id + " move", function (e) {
        if (!(e instanceof AMove)) {
            return false
        }
        ;
        p = e.getPiece();
        return p.getType() === Piece.Type.rook && p.getColor() === color && p.getId() === id;
    });
    bp.registerBThread("move rook " + color + " " + id, function () {
        var move = bp.sync({waitFor: rookMoveES});
        bp.sync({waitFor: bp.Event("game_start")});
        while (true)
            move = bp.sync({
                request: [
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() + 1, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() + 2, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() + 3, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() + 4, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() + 5, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() + 6, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() + 7, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() - 1, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() - 2, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() - 3, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() - 4, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() - 5, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() - 6, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() - 7, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 1, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 2, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 3, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 4, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 5, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 6, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 7, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 1, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 2, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 3, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 4, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 5, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 6, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 7, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() + 1, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() + 2, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() + 3, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() + 4, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() + 5, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() + 6, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() + 7, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() - 1, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() - 2, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() - 3, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() - 4, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() - 5, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() - 6, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() - 7, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 1, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 2, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 3, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 4, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 5, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 6, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 7, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 1, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 2, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 3, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 4, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 5, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 6, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 7, move.getPiece())
                ]
            });
    });

}

function kingBTs(color) {
    var isKingMove = bp.EventSet(color + " King Move events", function (e) {
        if (!(e instanceof AMove)) {
            return false
        }
        ;
        p = e.getPiece();
        return p.getType() === Piece.Type.king && p.getColor() === color;
    });

    bp.registerBThread("move king " + color, function () {
        var move = bp.sync({waitFor: isKingMove});
        bp.sync({waitFor: bp.Event("game_start")});
        while (true) {
            move = bp.sync({
                request: [
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() - 1, move.getTargetY() - 1, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 1, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() + 1, move.getTargetY() - 1, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() - 1, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() + 1, move.getTargetY(), move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() - 1, move.getTargetY() + 1, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 1, move.getPiece()),
                    new Move(move.getTargetX(), move.getTargetY(), move.getTargetX() + 1, move.getTargetY() + 1, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() - 1, move.getTargetY() - 1, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() - 1, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() + 1, move.getTargetY() - 1, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() - 1, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() + 1, move.getTargetY(), move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() - 1, move.getTargetY() + 1, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX(), move.getTargetY() + 1, move.getPiece()),
                    new Eat(move.getTargetX(), move.getTargetY(), move.getTargetX() + 1, move.getTargetY() + 1, move.getPiece())
                ]
            });
        }
    });

}

function initPiecesBTs() {
    var colors = [Piece.Color.black];
    // var colors = [piece.Color.black, piece.Color.white];
    for (var i = 0; i < colors.length; i++) {
        kingBTs(colors[i]);
        for (var j = 1; j <= 2; j++) {
            rookBTs(colors[i], j);
        }
    }
}
initPiecesBTs();

bp.registerBThread("block out of board moves", function () {
    bp.sync({block: outOfBoardMove});
});

bp.registerBThread("EnforceTurns", function () {
    bp.sync({waitFor: bp.Event("game_start")});
    while (true) {
        bp.sync({waitFor: isWhiteMove, block: isBlackMove});
        bp.sync({waitFor: isBlackMove, block: isWhiteMove});

    }
});

bp.registerBThread("Wait For Color", function () {
    var color = bp.sync({waitFor: [bp.Event("color","black"),bp.Event("color","white")]}).data;

    bp.registerBThread("Wait For My Turn", function () {
        while (true) {
            bp.sync({waitFor: bp.Event("My Turn"), block: isMyMove(color)});
            bp.sync({waitFor: isMyMove(color)});

        }
    });
});

bp.registerBThread("Wait For My Turn", function () {
    bp.sync({waitFor: bp.Event("game_start")});
    while (true) {
        bp.sync({waitFor: bp.Event("My Turn"), block: isBlackMove});
        bp.sync({waitFor: isBlackMove});

    }
});

bp.registerBThread("block moving to the same place", function () {
    bp.sync({waitFor: bp.Event("game_start")});
    bp.sync({block: isAmoveInPlace});
});

// bp.registerBThread("init_Start_thread", function () {
//     bp.sync({waitFor: bp.Event("init_start")});
//     bp.sync({request: Init(4, 4, new piece(piece.Color.black, piece.Type.rook, 1))});
//     bp.sync({request: Init(6, 5, new piece(piece.Color.white, piece.Type.king, 1))});
//     bp.sync({request: Init(5, 4, new piece(piece.Color.black, piece.Type.rook, 2))});
//     bp.sync({request: Init(5, 5, new piece(piece.Color.black, piece.Type.king, 1))});
//     bp.sync({request: bp.Event("init_end")});
// });





