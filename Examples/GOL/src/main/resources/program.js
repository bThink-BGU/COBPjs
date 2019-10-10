importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema);

CTX.subscribe("New Generation","Generation", function(gen) {
    bp.log.info("generation="+gen);

    const rule1 = CTX.getContextInstances("Less_Than_2_Neighbours");
    for (let index = 0; index < rule1.size(); index++) {
        let cell = rule1.get(index);
        if(cell.alive) {
            bp.registerBThread("Rule 1-" + CTX.counter.getAndIncrement() + " for " + cell, function () {
                bp.sync({
                    request: CTX.UpdateEvent("Die", {"i": cell.i, "j": cell.j}),
                    block: CTX.UpdateEvent("IncrementGeneration")
                });
            });
        }
    }

    const rule2 = CTX.getContextInstances("2_or_3_Neighbours");
    for (let index = 0; index < rule2.size(); index++) {
        let cell = rule2.get(index);
        /*bp.registerBThread("Rule 2-" + CTX.counter.getAndIncrement() +" for " + cell, function () {

        });*/
    }

    const rule3 = CTX.getContextInstances("More_Than_3_Neighbours");
    for (let index = 0; index < rule3.size(); index++) {
        let cell = rule3.get(index);
        if(cell.alive) {
            bp.registerBThread("Rule 3-" + CTX.counter.getAndIncrement() + " for " + cell, function () {
                bp.sync({
                    request: CTX.UpdateEvent("Die", {"i": cell.i, "j": cell.j}),
                    block: CTX.UpdateEvent("IncrementGeneration")
                });
            });
        }
    }

    const rule4 = CTX.getContextInstances("3_Neighbours");
    for (let index = 0; index < rule4.size(); index++) {
        let cell = rule4.get(index);
        if (!cell.alive) {
            bp.registerBThread("Rule 4-" + CTX.counter.getAndIncrement() + " for " + cell, function () {
                bp.sync({
                    request: CTX.UpdateEvent("Reproduce", {"i": cell.i, "j": cell.j}),
                    block: CTX.UpdateEvent("IncrementGeneration")
                });
            });
        }
    }
});

CTX.subscribe("Increment Generation","GameOfLife", function(game) {
    var gen = game.generation;
    var maxGen = game.maxGeneration;
    while(gen<maxGen) {
        bp.sync({request: CTX.UpdateEvent("IncrementGeneration")});
        gen++;
    }
});
