importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.ttt.schema);


//#region HELP FUNCTIONS
function createEvent(name, c) {
    return bp.Event(name, c);
}

function getCell(i,j){
    return CTX.getContextInstances("Cell["+i+","+j+"]").get(0);
}
//#endregion HELP FUNCTIONS

//#region EventSets
// Represents Enforce Turns
var move = bp.EventSet("MoveEvents", function(e) {
    return e.name.equals("O") || e.name.equals("X");
});
var XEvents = bp.EventSet("XEvents", function(e) {
    return e.name.equals("X") ;
});
var OEvents = bp.EventSet("OEvents", function(e) {
    return e.name.equals("O");
});
var EndGame = bp.EventSet("EndGame", function(e) {
    return e.name.equals("OWin") || e.name.equals("XWin") || e.name.equals("Draw");
});

//#endregion EventSets

//#region CEll BEHAVIORS
CTX.subscribe("ClickHandler","Cell", function(c) {
    bp.sync({ waitFor: createEvent("Click", c) });
    bp.sync({ request: createEvent("X", c) });
});

//block X,O on nonempty cell
CTX.subscribe("block X,O on nonempty cell","NonEmptyCell",function(c) {
    bp.sync({ block:[ createEvent("X", c), createEvent("O",c) ] });
});
//endregion CEll BEHAVIORS

//#region GAME RULES

bp.registerBThread("EnforceTurnsXO", function() {
    while (true) {
        bp.sync({
            waitFor: XEvents,
            block: OEvents
        });
        bp.sync({
            waitFor: OEvents,
            block: XEvents
        });
    }
});

// Represents when the game ends
bp.registerBThread("block X or O on endgame", function() {
    bp.sync({ waitFor: EndGame });
    bp.sync({ block: move });
});

// Represents when it is a draw
bp.registerBThread("DetectDraw", function() {
    for (var i = 0; i < 9; i++) {
        bp.sync({ waitFor: move });
    }
    bp.sync({ request: bp.Event('Draw') }, 90);
});

//#endregion GAME RULES

//#region TRIPLE BEHAVIORS
// Represents when X wins
CTX.subscribe("DetectXWin", "Triple", function(t) {
    for (var c = 0; c < 3; c++) {
        bp.sync({ waitFor:[ createEvent("X", t.cell0), createEvent("X", t.cell1), createEvent("X", t.cell2) ] });
    }
    bp.sync({ request:[ bp.Event('XWin') ] }, 100);
});

// Represents when O wins
CTX.subscribe("DetectOWin", "Triple", function(t) {
    for (var c = 0; c < 3; c++) {
        bp.sync({ waitFor:[ createEvent("O", t.cell0), createEvent("O", t.cell1), createEvent("O", t.cell2) ] });
    }
    bp.sync({ request:[ bp.Event('OWin') ] }, 100);
});

// Player O strategy to add a the third O to win
CTX.subscribe("AddThirdO", "Triple", function(t) {
    bp.sync({ waitFor:[ createEvent("O", t.cell0), createEvent("O", t.cell1), createEvent("O", t.cell2) ] });
    bp.sync({ waitFor:[ createEvent("O", t.cell0), createEvent("O", t.cell1), createEvent("O", t.cell2) ] });
    bp.sync({ request: [ createEvent("O", t.cell0), createEvent("O", t.cell1), createEvent("O", t.cell2) ] }, 50);
});

// Player O strategy to prevent the third X of player X
CTX.subscribe("PreventThirdX", "Triple", function(t) {
    bp.sync({ waitFor:[ createEvent("X", t.cell0), createEvent("X", t.cell1), createEvent("X", t.cell2) ] });
    bp.sync({ waitFor:[ createEvent("X", t.cell0), createEvent("X", t.cell1), createEvent("X", t.cell2) ] });
    bp.sync({ request: [ createEvent("O", t.cell0), createEvent("O", t.cell1), createEvent("O", t.cell2) ] }, 40);
});
//#endregion TRIPLE BEHAVIORS

//#region PLAYER O STRATEGY

//#region fork functions
// Player O strategy to prevent the Fork22 of player X
function addFork22PermutationBthreads(c1,c2){ //
    bp.registerBThread("PreventFork22X", function() {
        bp.sync({ waitFor:[ createEvent("X",c1) ] });
        bp.sync({ waitFor:[ createEvent("X",c2) ] });
        bp.sync({ request:[ createEvent("O",getCell(2,2)),createEvent("O",getCell(0,2)), createEvent("O",getCell(2,0))]}, 30);
    });
}

// Player O strategy to prevent the Fork02 of player X
function addFork02PermutationBthreads(c1,c2){ //
    bp.registerBThread("PreventFork02X", function() {
        bp.sync({ waitFor:[ createEvent("X",c1) ] });
        bp.sync({ waitFor:[ createEvent("X",c2) ] });
        bp.sync({ request:[ createEvent("O",getCell(0,2)),createEvent("O",getCell(0,0)), createEvent("O",getCell(2,2))]}, 30);
    });
}

// Player O strategy to prevent the Fork20 of player X
function addFork20PermutationBthreads(c1,c2){ //
    bp.registerBThread("PreventFork20X", function() {
        bp.sync({ waitFor:[ createEvent("X",c1) ] });
        bp.sync({ waitFor:[ createEvent("X",c2) ] });
        bp.sync({ request:[ createEvent("O",getCell(2,0)),createEvent("O",getCell(0,0)), createEvent("O",getCell(2,2))] }, 30);
    });
}

// Player O strategy to prevent the Fork00 of player X
function addFork00PermutationBthreads(c1,c2){ //
    bp.registerBThread("PreventFork20X", function() {
        bp.sync({ waitFor:[ createEvent("X",c1) ] });
        bp.sync({ waitFor:[ createEvent("X",c2) ] });
        bp.sync({ request:[ createEvent("O",getCell(0,0)),createEvent("O", getCell(0,2)), createEvent("O",getCell(2,0))] }, 30);
    });
}

// Player O strategy to prevent the Forkdiagonal of player X
function addForkdiagPermutationBthreads(c1,c2){ //
    bp.registerBThread("PreventForkdiagX", function() {
        bp.sync({ waitFor:[ createEvent("X",c1) ] });
        bp.sync({ waitFor:[ createEvent("X",c2) ] });
        bp.sync({ request:[ createEvent("O", getCell(0,1)),createEvent("O",getCell(1,0)), createEvent("O", getCell(2,1)), createEvent("O", getCell(1,2)) ] }, 30);
    });
}
//#endregion fork functions

// Preference to put O on the center
bp.registerBThread("Center", function() {
    bp.sync({waitFor: bp.Event("Context Population Ended")});
    bp.sync({request: [createEvent("O", getCell(1,1))]}, 35);
});

// Preference to put O on the corners
CTX.subscribe("Corner", "CornerCell", function(c) {
    bp.sync({ request: createEvent("O", c) }, 20);
});

bp.registerBThread("PLAYER O STRATEGIES", function() {
    bp.sync({waitFor: bp.Event("Context Population Ended")});
    // Preference to put O on the sides
    bp.registerBThread("Sides", function () {
        while (true) {
            bp.sync({
                request: [createEvent("O", getCell(0,1)), createEvent("O", getCell(1,0)),
                    createEvent("O", getCell(2,1)), createEvent("O", getCell(1,2))]
            }, 10);
        }
    });
    var forks22 = [[getCell(1, 2), getCell(2, 0)], [getCell(2, 1), getCell(0, 2)], [getCell(1, 2), getCell(2, 1)]];
    var forks02 = [[getCell(1, 2), getCell(0, 0)], [getCell(0, 1), getCell(2, 2)], [getCell(1, 2), getCell(0, 1)]];
    var forks20 = [[getCell(1, 0), getCell(2, 2)], [getCell(2, 1), getCell(0, 0)], [getCell(2, 1), getCell(1, 0)]];
    var forks00 = [[getCell(0, 1), getCell(2, 0)], [getCell(1, 0), getCell(0, 2)], [getCell(0, 1), getCell(1, 0)]];

    var forksdiag = [[getCell(0, 2), getCell(2, 0)]];

    var permsforks = [[0, 1], [1, 0]];

    forks22.forEach(function (f) {
        permsforks.forEach(function (p) {
            addFork22PermutationBthreads(f[p[0]], f[p[1]]);
        });
    });

    forks02.forEach(function (f) {
        permsforks.forEach(function (p) {
            addFork02PermutationBthreads(f[p[0]], f[p[1]]);
        });
    });

    forks20.forEach(function (f) {
        permsforks.forEach(function (p) {
            addFork20PermutationBthreads(f[p[0]], f[p[1]]);
        });
    });

    forks00.forEach(function (f) {
        permsforks.forEach(function (p) {
            addFork00PermutationBthreads(f[p[0]], f[p[1]]);
        });
    });

    forksdiag.forEach(function (f) {
        permsforks.forEach(function (p) {
            addForkdiagPermutationBthreads(f[p[0]], f[p[1]]);
        });
    });
});
//#endregion PLAYER O STRATEGY
