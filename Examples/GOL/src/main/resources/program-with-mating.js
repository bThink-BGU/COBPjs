const tockEvent = CTX.UpdateEvent("Tock");
const matingCalculationEvent = bp.Event("Mating");

const anyButTock = bp.EventSet("", function (e) {
    return !e.equals(tockEvent);
});
const anyButGui = bp.EventSet("", function (e) {
    return !e.name.equals("GUI ready");
});

CTX.subscribe("Increment Generation", "GameOfLife", function (game) {
    for (let gen = 0; gen < game.maxGeneration; gen++) {
        bp.sync({request: matingCalculationEvent});
        bp.sync({request: CTX.UpdateEvent("Tick")});
        bp.sync({waitFor: bp.Event("GUI ready"), block: anyButGui});
        bp.sync({request: tockEvent, block: anyButTock});
    }
});

CTX.subscribe("Mating calculation", "GameOfLife", function (game) {
    while (true) {
        bp.sync({waitFor: matingCalculationEvent});
        const mating = CTX.getContextInstances("Mating");
        for (let index = 0; index < mating.size(); index++) {
            let cell = mating.get(index)[0];
            let n1 = mating.get(index)[1];
            let n2 = mating.get(index)[2];
            let n3 = mating.get(index)[3];

            bp.log.info("cell=" + cell + "\nn1=" + n1 + "\nn2=" + n2 + "\nn3=" + n3);
            /*bp.registerBThread("Mating-" + CTX.counter.getAndIncrement() + " for " + cell, function () {
        });*/
        }
        bp.sync({request: CTX.TransactionEvent()});
    }
});

CTX.subscribe("rule1", "Alive_With_Less_Than_2_Neighbours", function (cell) {
    bp.sync({
        request: CTX.UpdateEvent("Die", {"cell": cell}),
        block: CTX.UpdateEvent("Tick")
    });
});

CTX.subscribe("rule3", "Alive_With_More_Than_3_Neighbours", function (cell) {
    bp.sync({
        request: CTX.UpdateEvent("Die", {"cell": cell}),
        block: CTX.UpdateEvent("Tick")
    });
});

CTX.subscribe("rule4", "Dead_With_3_Neighbours", function (cell) {
    bp.sync({
        request: CTX.UpdateEvent("Reproduce", {"cell": cell}),
        block: CTX.UpdateEvent("Tick")
    });
});