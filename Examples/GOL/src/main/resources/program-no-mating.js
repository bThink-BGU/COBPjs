
const tockEvent = bp.Event("Tock");

const anyButTock = bp.EventSet("", function(e) {
   return !e.equals(tockEvent);
});
const anyButGui = bp.EventSet("", function(e) {
    return !e.name.equals("GUI ready");
});

CTX.subscribe("Increment Generation","GameOfLife", function(game) {
    for(let gen = 0; gen < game.maxGeneration; gen ++) {
        bp.sync({request: bp.Event("Tick")});
        bp.sync({waitFor: bp.Event("GUI ready"), block: anyButGui});
        bp.sync({request: tockEvent, block: anyButTock});
    }
});

CTX.subscribe("rule1", "Alive_With_Less_Than_2_Neighbours", function(cell) {
    bp.sync({
        request: bp.Event("Die", {"cell": cell}),
        block: bp.Event("Tick")
    });
});

CTX.subscribe("rule3", "Alive_With_More_Than_3_Neighbours", function(cell) {
    bp.sync({
        request: bp.Event("Die", {"cell": cell}),
        block: bp.Event("Tick")
    });
});

CTX.subscribe("rule4", "Dead_With_3_Neighbours", function(cell) {
    bp.sync({
        request: bp.Event("Reproduce", {"cell": cell}),
        block: bp.Event("Tick")
    });
});

/*CTX.subscribe("New Generation","Generation", function(gen) {
    bp.log.info("generation="+gen);

    const rule1 = CTX.getContextInstances("Alive_With_Less_Than_2_Neighbours");
    for (let index = 0; index < rule1.size(); index++) {
        let cell = rule1.get(index);
        bp.registerBThread("Rule 1-" + CTX.counter.getAndIncrement() + " for " + cell, function () {
            bp.sync({
                request: bp.Event("Die", {"cell": cell}),
                block: bp.Event("Generation Ended")
            });
        });
    }

    /!*const rule2 = CTX.getContextInstances("2_or_3_Neighbours");
    for (let index = 0; index < rule2.size(); index++) {
        let cell = rule2.get(index);
        /!*bp.registerBThread("Rule 2-" + CTX.counter.getAndIncrement() +" for " + cell, function () {

        });*!/
    }*!/

    const rule3 = CTX.getContextInstances("Alive_With_More_Than_3_Neighbours");
    for (let index = 0; index < rule3.size(); index++) {
        let cell = rule3.get(index);
        bp.registerBThread("Rule 3-" + CTX.counter.getAndIncrement() + " for " + cell, function () {
            bp.sync({
                request: bp.Event("Die", {"cell": cell}),
                block: bp.Event("Generation Ended")
            });
        });
    }

    const rule4 = CTX.getContextInstances("Dead_With_3_Neighbours");
    for (let index = 0; index < rule4.size(); index++) {
        let cell = rule4.get(index);
        bp.registerBThread("Rule 4-" + CTX.counter.getAndIncrement() + " for " + cell, function () {
            bp.sync({
                request: bp.Event("Reproduce", {"cell": cell}),
                block: bp.Event("Generation Ended")
            });
        });
    }

    const mating = CTX.getContextInstances("Mating");
    for (let index = 0; index < mating.size(); index++) {
        let cell = mating.get(index)[0];
        let n1 = mating.get(index)[1];
        let n2 = mating.get(index)[2];
        let n3 = mating.get(index)[3];

        bp.log.info("cell="+cell+"\nn1="+n1+"\nn2="+n2+"\nn3="+n3);
        /!*bp.registerBThread("Mating-" + CTX.counter.getAndIncrement() + " for " + cell, function () {
        });*!/
    }
});

CTX.subscribe("Increment Generation","GameOfLife", function(game) {
    var gen = game.generation;
    var maxGen = game.maxGeneration;
    while(gen<maxGen) {
        bp.sync({request: bp.Event("Generation Ended")});
        bp.sync({waitFor: bp.Event("GUI ready")});
        bp.sync({request: bp.Event("IncrementGeneration")});
        gen++;
    }
});*/
