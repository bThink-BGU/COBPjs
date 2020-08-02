importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema);

//<editor-fold desc="Events">
var fenEvent = bp.EventSet("", function (e) {
    return e.name.equals("ParseFen");
});
//</editor-fold>

//<editor-fold desc="Cells Functions">
function registerCellsQueries()
{
    for (var i = 0; i < 8; i++)
    {
        for (var j = 0; j < 8; j++)
        {
            CTX.registerParameterizedContextQuery("SpecificCell", "Cell[" + i + "," + j + "]", {
                "row": i,
                "col": j
            });
        }
    }
}

registerCellsQueries();

function getCell(i,j){
    return CTX.getContextInstances("Cell["+i+","+j+"]").get(0);
}

function getUpdatedCell(c){
    return getCell(c.row, c.col);
}

function getCellByPiece(piece)
{
    return CTX.getContextInstances("Piece[" + piece + "]").get(0);
}
//</editor-fold>

bp.registerBThread("Populate",function ()
{
    // cells
    for(var row = 0; row < 8; row++)
    {
        for(var col = 0; col < 8; col++)
        {
            bp.sync({ request: bp.Event("Add Cell", new Cell(row,col))});
        }
    }

    // pieces
    while (true)
    {
        var fen = bp.sync({waitFor:fenEvent}).data;

        // delete old
        /*var nonEmpty = CTX.getContextInstances("NotEmptyCell");
        for(var i = 0; i < nonEmpty.size(); i++)
        {
            bp.sync({ request: CTX.UpdateEvent("UpdateCell",{cell:nonEmpty.get(i), piece: null})});
        }*/

        // populate new
        parseBoard(fen);

        // prepare board for debug and print
        var board = [];

        for(var row = 0; row < 8; row++)
        {
            var r = [];
            for(var col = 0; col < 8; col++)
            {
                r.push(getCell(row,col));
            }
            board.push(r);
        }

        bp.sync({request:bp.Event("Done Populate",board)});
    }
});


function parseBoard(toParse)
{
    // bp.log.info("fen: " + toParse);
    var tokens = toParse.split("/");
    var row = 0,column = 0;

    for(var i = 0; i < tokens.length; i++)
    {
        for(var token = 0; token < tokens[i].length(); token++)
        {
            var currentToken = tokens[i].substring(token,token+1);
            var toNum = parseInt(currentToken);

            if(isNaN(toNum))
            {
                var piece = null;
                switch(String(currentToken))
                {
                    case "p": piece = new Pawn(Piece.Color.White); break;
                    case "n": piece = new Piece(Piece.Type.Knight,Piece.Color.White); break;
                    case "b": piece = new Piece(Piece.Type.Bishop,Piece.Color.White); break;
                    case "r": piece = new Piece(Piece.Type.Rook,Piece.Color.White); break;
                    case "q": piece = new Piece(Piece.Type.Queen,Piece.Color.White); break;
                    case "k": piece = new Piece(Piece.Type.King,Piece.Color.White); break;
                    case "P": piece = new Pawn(Piece.Color.Black); break;
                    case "N": piece = new Piece(Piece.Type.Knight,Piece.Color.Black); break;
                    case "B": piece = new Piece(Piece.Type.Bishop,Piece.Color.Black); break;
                    case "R": piece = new Piece(Piece.Type.Rook,Piece.Color.Black); break;
                    case "Q": piece = new Piece(Piece.Type.Queen,Piece.Color.Black); break;
                    case "K": piece = new Piece(Piece.Type.King,Piece.Color.Black); break;
                }

                if(piece != null)
                {
                    // bp.log.info("piece" + piece+" row: "+row+" col: "+column);
                    // update cell to store piece
                    var cell = getCell(row,column);
                    // bp.log.info("piece" + piece);
                    bp.sync({ request: bp.Event("Add Piece", {"piece":piece , "cell": cell})});

                    CTX.registerParameterizedContextQuery("PieceCell", "Piece[" + piece + "]", {"piece": piece});
                }

                column++;
            }
            else column += toNum;
        }

        column = 0;
        row++;
    }
}