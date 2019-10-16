const tackEvent = CTX.UpdateEvent("Tack");
const tockEvent = CTX.UpdateEvent("Tock");

const anyButTock = bp.EventSet("", function (e) {
    return !(e.equals(tockEvent) || e instanceof CTX.ContextInternalEvent);
});
const anyButGui = bp.EventSet("", function (e) {
    return !(e.name.equals("GUI ready") || e instanceof CTX.ContextInternalEvent);
});

CTX.subscribe("Increment Generation", "GameOfLife", function (game) {
    for (let gen = 0; gen < game.maxGeneration; gen++) {
        bp.sync({request: CTX.UpdateEvent("Tick")});
        bp.sync({waitFor: bp.Event("GUI ready"), block: anyButGui});
        bp.sync({request: tackEvent});
        bp.sync({request: tockEvent, block: anyButTock});
    }
});

CTX.subscribe("Add Matings", "Mating", function (mating) {
    // const mating = CTX.getContextInstances("Mating");
    const cell = mating[0];
    const n1 = mating[1];
    const n2 = mating[2];
    const n3 = mating[3];
    const matingArea = CTX.getContextInstances("Mating area of Cell(" + cell.i + "," + cell.j + ")");
    const activeMatings = CTX.getContextInstances("ActiveMating");
    const newMating = Mating(cell, n1, n2, n3, matingArea);
    if(!activeMatings.contains(newMating)) {
        bp.sync({request: CTX.InsertEvent(newMating), block: tackEvent});
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

/*
CTX.subscribe("rule4.2", "ActiveMating", function (cell) {
    bp.sync({
        request: CTX.UpdateEvent("Reproduce", {"cell": cell}),
        block: CTX.UpdateEvent("Tick")
    });
});*/
