bp.registerBThread("PopulateDB", function() {
    var cells = [];
    var triples = [];

    for(var i=0; i<3; i++){
        var t = [];
        for(var j=0; j<3; j++){
            var cell = new Cell(i,j);
            cells.push(cell);
            t.push(cell);
        }
        triples.push(new Triple(t[0],t[1],t[2]));
    }

    bp.sync({ request: CTX.InsertEvent(cells) });
    bp.sync({ request: CTX.InsertEvent(triples) });
});