function registerAllCellsQueries() {
    for (var i = 0; i < 3; i++) {
        for (var j = 0; j < 3; j++) {
            CTX.registerParameterizedContextQuery("SpecificCell", "Cell[" + i + "," + j + "]", {
                "i": i,
                "j": j
            });
        }
    }
}

registerAllCellsQueries();

bp.registerBThread("PopulateDB", function() {
    var board = [], triples = [], diag1 = [], diag2 = [],
        row, col, i, j, cell, cells;

    for(i=0; i<3; i++){
        row = [];
        for(j=0; j<3; j++){
            cell = new Cell(i,j);
            row.push(cell);
        }
        board.push(row);
    }

    for(i=0; i<3; i++) {
        diag1.push(board[i][i]);
        diag2.push(board[i][2-i]);
        row = [];
        col = [];
        for (j = 0; j < 3; j++) {
            row.push(board[i][j]);
            col.push(board[j][i]);
        }
        triples.push(new Triple("row_"+i, row));
        triples.push(new Triple("col_"+i, col));
    }
    triples.push(new Triple("diag_1", diag1));
    triples.push(new Triple("diag_2", diag2));

    // flattening board - for debugging
    cells = [].concat.apply([], board);

    bp.sync({ request: bp.Event("CTX.Insert", cells.concat(triples)) });
    // bp.sync({ request: bp.Event("CTX.Insert", triples) });
    // bp.log.info("Population ended");
    bp.sync({ request: bp.Event("Context Population Ended") });
});