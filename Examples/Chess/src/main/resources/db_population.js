var size = 8;

function createCells() {
    var cells = [], i, j;
    for (i = 0; i < size; i++) {
        for (j = 0; j < size; j++) {
            CTX_instance.registerParameterizedContextQuery("SpecificCell", "Cell(" + i + "," + j + ")", {
                "i": i,
                "j": j
            });
            cells.push(new Cell(i, j));
        }
    }
    return cells;
}

function createPieces() {
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

bp.registerBThread("PopulateDB", function() {
    var cells = createCells();
    var pieces = createPieces();
    bp.sync({ request: CTX.InsertEvent(cells) });
    bp.sync({ request: CTX.InsertEvent(pieces) });
    bp.sync({ request: bp.Event("Context Population Ended") });
});