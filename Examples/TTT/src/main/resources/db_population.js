bp.registerBThread("PopulateDB", function() {
    var cells = [];
    var triples = [];

    for(var i=0; i<3; i++){
        var t = [];
        for(var j=0; j<3; j++){
            var cell = new Cell(i,j);
            t.push(cell);
        }
        cells.push(t);
    }

    var diag1= [];
    var diag2= [];
    for(var i=0; i<3; i++) {
        diag1.push(cells[i][i]);
        diag2.push(cells[i][2-i]);
        var row = [];
        var col = [];
        for (var j = 0; j < 3; j++) {
            row.push(cells[i][j]);
            col.push(cells[j][i]);
        }
        triples.push(new Triple("row_"+i, row));
        triples.push(new Triple("col_"+i, col));
    }
    triples.push(new Triple("diag_1", diag1));
    triples.push(new Triple("diag_2", diag2));

    //bp.sync({ request: CTX.InsertEvent(cells) });
    //bp.sync({ request: CTX.InsertEvent(triples) });
});