const tackEvent = CTX.UpdateEvent("Tack");
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
        bp.sync({request: CTX.UpdateEvent("Tick")});
        bp.sync({waitFor: bp.Event("GUI ready"), block: anyButGui});
        bp.sync({request: tackEvent});
        bp.sync({waitFor: bp.Event("Mating preparations ended")});
        bp.sync({request: tockEvent, block: anyButTock});
    }
});

CTX.subscribe("Add Matings", "Mating", function (mating) {
    // const mating = CTX.getContextInstances("Mating");
    const cell = mating.get(index)[0];
    const n1 = mating.get(index)[1];
    const n2 = mating.get(index)[2];
    const n3 = mating.get(index)[3];
    const matingArea = CTX.getContextInstances("MatingArea", {"deadCellI":cell.i, "deadCellJ":cell.j});
    const activeMatings = CTX.getContextInstances("ActiveMating");
    if(!activeMatings.contains(cell)) {
        bp.sync({request: CTX.InsertEvent(Mating(cell, n1, n2, n3, matingArea))});
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