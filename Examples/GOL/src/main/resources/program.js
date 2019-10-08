importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema);

CTX.subscribe("New Generation","Generation", function(gen) {
    CTX.subscribe("Rule 1", "Less_Than_2_Neighbours", function (cell) {
        if(cell.alive) {
            bp.sync({
                request: CTX.UpdateEvent("Die", {"i": cell.i, "j": cell.j}),
                block: CTX.UpdateEvent("IncrementGeneration")
            });
        }
    });

    CTX.subscribe("Rule 2", "2_or_3_Neighbours", function (cell) {
        //do nothing
    });

    CTX.subscribe("Rule 3", "More_Than_3_Neighbours", function (cell) {
        if(cell.alive) {
            bp.sync({
                request: CTX.UpdateEvent("Die", {"i": cell.i, "j": cell.j}),
                block: CTX.UpdateEvent("IncrementGeneration")
            });
        }
    });

    CTX.subscribe("Rule 4", "More_Than_3_Neighbours", function (cell) {
        if(!cell.alive) {
            bp.sync({
                request: CTX.UpdateEvent("Spawn", {"i": cell.i, "j": cell.j}),
                block: CTX.UpdateEvent("IncrementGeneration")
            });
        }
    });
});

CTX.subscribe("Increment Generation","GameOfLife", function(game) {
    var gen = game.generation;
    var maxGen = game.maxGeneration;
    while(gen<maxGen) {
        bp.sync({request: CTX.UpdateEvent("IncrementGeneration")});
    }
});
