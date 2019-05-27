importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.chess.events);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.Chess.context);

var size = 8;

var addPieceEventSet = bp.EventSet("AddPieceEventSet", function(e) {
    return e.name.equals("AddPiece");
});

var setColorEventSet = bp.EventSet("SetColorEventSet", function(e) {
    return e.name.equals("Color");
});

bp.registerBThread("Create Board", function() {
    var cells = [], i, j;
    for (i = 0; i < size; i++) {
        for (j = 0; j < size; j++) {
            cells.push(new Cell(i, j));
        }
    }
    bp.sync({ request: CTX.InsertEvent(Game.create(cells)) });
});

CTX.subscribe("Set Color", "GameStateInit", function (game) {
    var myColor = bp.sync({waitFor: setColorEventSet}).data;
    bp.sync({ request: CTX.UpdateEvent("ChangeMyColor", { "color": myColor , "game":game }) });
});

CTX.subscribe("Place Pieces", "GameStateInit", function (game) {
    while(true) {
        var e = bp.sync({waitFor: addPieceEventSet, interrupt: CTX.ContextEndedEvent("GameStateInit", game) });
        var piece = e.data.get("Piece");
        CTX.registerParameterizedContextQuery("CellWithPiece", "CellWithPiece_"+piece.getId(), {"piece":piece});
        bp.sync({
            request:
                CTX.TransactionEvent(
                    CTX.InsertEvent(piece),
                    CTX.UpdateEvent("SetPiece", {
                        "cell": game.Board(e.data.get("Row"), e.data.get("Col")),
                        "piece": piece
                    })
                )
        });
    }
});

CTX.subscribe("Start Game", "GameStateInit", function (game) {
        bp.sync({waitFor: bp.Event("init_end") });
        bp.sync({ request: CTX.UpdateEvent("ChangeGameState", {"game":game, "state": Game.State.PLAYING })});
});

function addAllPieces(game) {
    var types = Type.values();
    var colors = Color.values();
    var pieces = [];
    var piece;
    for (var c = 0; c < colors.length; c++) {
        for (var t = 0; t < types.length; t++) {
            var type = types[t];
            for (var n = 0; n < type.Count; n++) {
                piece = new Piece(colors[c], type, n+1);

                CTX_instance.registerParameterizedContextQuery("PieceOfType", type.toString(), {
                    "type": type
                });
                CTX_instance.registerParameterizedContextQuery("CellWithPiece", "CellWithPiece("+piece.toString()+")", {
                    "p": piece
                });
                CTX_instance.registerParameterizedContextQuery("CellWithColor", "CellWithColor("+ colors[c] +")", {
                    "color" : colors[c]
                });
                CTX_instance.registerParameterizedContextQuery("CellWithType", "CellWithType("+ type +")", {
                    "type" : type
                });
                CTX_instance.registerParameterizedContextQuery("SpecificPiece", "SpecificPiece("+piece.toString()+")", {
                    "piece": piece
                });
                CTX_instance.registerParameterizedContextQuery("PieceOfId", "PieceOfId("+ n+1 +")", {
                    "id": (n+1).toString()
                });

                pieces.push(piece);
            }
        }
    }
    return pieces;
}

/*
bp.registerBThread("PopulateDB", function() {
    var cells = createCells();
    var pieces = createPieces();
    bp.sync({ request: CTX.InsertEvent(cells) });
    bp.sync({ request: CTX.InsertEvent(pieces) });
    bp.sync({ request: bp.Event("Context Population Ended") });
});*/
