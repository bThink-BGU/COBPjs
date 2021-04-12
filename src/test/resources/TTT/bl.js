//#region HELP FUNCTIONS
function Event(name, data) {
  return Event(name, data);
}

function getCell(i, j) {
  return ctx.getEntityById("Cell[" + i + "," + j + "]");
}

//#endregion HELP FUNCTIONS

//#region EventSets
// Represents Enforce Turns
var move = EventSet("MoveEvents", function (e) {
  return e.name.equals("O") || e.name.equals("X");
});
var XEvents = EventSet("XEvents", function (e) {
  return e.name.equals("X");
});
var OEvents = EventSet("OEvents", function (e) {
  return e.name.equals("O");
});
var EndGame = EventSet("EndGame", function (e) {
  return e.name.equals("OWin") || e.name.equals("XWin") || e.name.equals("Draw");
});

//#endregion EventSets

//#region CEll BEHAVIORS
ctx.bthread('ClickHandler', 'Cell.All', function (entity) {
  sync({waitFor: Event("Click", entity)});
  sync({request: Event("X", entity)});
})

//block X,O on nonempty cell
ctx.bthread("block X,O on nonempty cell", "Cell.NonEmpty", function (c) {
  sync({block: [Event("X", c), Event("O", c)]});
})

ctx.bthread("block X,O on nonempty cell - 2nd version", "Cell.All", function (c) {
  sync({waitFor: [Event("X", c), Event("O", c)]});
  sync({block: [Event("X", c), Event("O", c)]});
})
//endregion CEll BEHAVIORS

//#region GAME RULES
bp.registerBThread("EnforceTurnsXO", function () {
  while (true) {
    sync({waitFor: XEvents, block: OEvents});
    sync({waitFor: OEvents, block: XEvents});
  }
})

// Represents when the game ends
bp.registerBThread("block X or O on endgame", function () {
  sync({waitFor: EndGame});
  sync({block: move});
});

// Represents when it is a draw
bp.registerBThread("DetectDraw", function () {
  for (var i = 0; i < 9; i++) {
    sync({waitFor: move});
  }
  sync({request: Event('Draw')}, 90);
});

//#endregion GAME RULES

//#region Line BEHAVIORS
// Represents when X wins
ctx.bthread("DetectXWin", "Line.All", function (t) {
  for (var c = 0; c < 3; c++) {
    sync({waitFor: [Event("X", t.cell0), Event("X", t.cell1), Event("X", t.cell2)]});
  }
  sync({request: [Event('XWin')]}, 100);
})

// Represents when O wins
ctx.bthread("DetectOWin", "Line.All", function (t) {
  for (var c = 0; c < 3; c++) {
    sync({waitFor: [Event("O", t.cell0), Event("O", t.cell1), Event("O", t.cell2)]});
  }
  sync({request: [Event('OWin')]}, 100);
})

// Player O strategy to add a the third O to win
ctx.bthread("AddThirdO", "Line.All", function (t) {
  sync({waitFor: [Event("O", t.cell0), Event("O", t.cell1), Event("O", t.cell2)]});
  sync({waitFor: [Event("O", t.cell0), Event("O", t.cell1), Event("O", t.cell2)]});
  sync({request: [Event("O", t.cell0), Event("O", t.cell1), Event("O", t.cell2)]}, 50);
});

// Player O strategy to prevent the third X of player X
ctx.bthread("PreventThirdX", "Line.All", function (t) {
  sync({waitFor: [Event("X", t.cell0), Event("X", t.cell1), Event("X", t.cell2)]});
  sync({waitFor: [Event("X", t.cell0), Event("X", t.cell1), Event("X", t.cell2)]});
  sync({request: [Event("O", t.cell0), Event("O", t.cell1), Event("O", t.cell2)]}, 40);
});
//#endregion Line BEHAVIORS

//#region PLAYER O STRATEGY

//#region fork functions
// Player O strategy to prevent the Fork22 of player X
function addFork22PermutationBthreads(c1, c2) { //
  bp.registerBThread("PreventFork22X_" + c1 + "_" + c2, function () {
    sync({waitFor: [Event("X", c1)]});
    sync({waitFor: [Event("X", c2)]});
    sync({request: [Event("O", getCell(2, 2)), Event("O", getCell(0, 2)), Event("O", getCell(2, 0))]}, 30);
  });
}

// Player O strategy to prevent the Fork02 of player X
function addFork02PermutationBthreads(c1, c2) { //
  bp.registerBThread("PreventFork02X_" + c1 + "_" + c2, function () {
    sync({waitFor: [Event("X", c1)]});
    sync({waitFor: [Event("X", c2)]});
    sync({request: [Event("O", getCell(0, 2)), Event("O", getCell(0, 0)), Event("O", getCell(2, 2))]}, 30);
  });
}

// Player O strategy to prevent the Fork20 of player X
function addFork20PermutationBthreads(c1, c2) { //
  bp.registerBThread("PreventFork20X_" + c1 + "_" + c2, function () {
    sync({waitFor: [Event("X", c1)]});
    sync({waitFor: [Event("X", c2)]});
    sync({request: [Event("O", getCell(2, 0)), Event("O", getCell(0, 0)), Event("O", getCell(2, 2))]}, 30);
  });
}

// Player O strategy to prevent the Fork00 of player X
function addFork00PermutationBthreads(c1, c2) { //
  bp.registerBThread("PreventFork20X_" + c1 + "_" + c2, function () {
    sync({waitFor: [Event("X", c1)]});
    sync({waitFor: [Event("X", c2)]});
    sync({request: [Event("O", getCell(0, 0)), Event("O", getCell(0, 2)), Event("O", getCell(2, 0))]}, 30);
  });
}

// Player O strategy to prevent the Forkdiagonal of player X
function addForkdiagPermutationBthreads(c1, c2) { //
  bp.registerBThread("PreventForkdiagX_" + c1 + "_" + c2, function () {
    sync({waitFor: [Event("X", c1)]});
    sync({waitFor: [Event("X", c2)]});
    sync({request: [Event("O", getCell(0, 1)), Event("O", getCell(1, 0)), Event("O", getCell(2, 1)), Event("O", getCell(1, 2))]}, 30);
  });
}

//#endregion fork functions

// Preference to put O on the center
bp.registerBThread("Center", function () {
  sync({waitFor: Event("Context Population Ended")});
  sync({request: [Event("O", getCell(1, 1))]}, 35);
});

// Preference to put O on the corners
ctx.bthread("Corner", "Cell.Corner", function (c) {
  sync({request: Event("O", c)}, 20);
});

// Preference to put O on the sides
bp.registerBThread("Sides", function () {
  while (true) {
    sync({
      request: [Event("O", getCell(0, 1)), Event("O", getCell(1, 0)),
        Event("O", getCell(2, 1)), Event("O", getCell(1, 2))]
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
//#endregion PLAYER O STRATEGY
