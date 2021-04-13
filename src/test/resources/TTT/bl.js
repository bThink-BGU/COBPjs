//#region HELPER FUNCTIONS
function Event(name, data) {
  return bp.Event(name, data)
}

function getCell(i, j) {
  return ctx.getEntityById("cell(" + i + "," + j + ")")
}

function getLineCells(l) {
  return [getCell(l.c1.i, l.c1.j), getCell(l.c2.i, l.c2.j), getCell(l.c3.i, l.c3.j)]
}

//#endregion HELPER FUNCTIONS

//#region EventSets
// Represents Enforce Turns
const move = bp.EventSet("MoveEvents", function (e) {
  return e.name.equals("O") || e.name.equals("X")
})
const XEvents = bp.EventSet("XEvents", function (e) {
  return e.name.equals("X")
})
const OEvents = bp.EventSet("OEvents", function (e) {
  return e.name.equals("O")
})
const EndGame = bp.EventSet("EndGame", function (e) {
  return e.name.equals("OWin") || e.name.equals("XWin") || e.name.equals("Draw")
})

//#endregion EventSets

//#region GAME RULES

//block X,O on nonempty cell
bthread("block X,O on nonempty cell", "Cell.All", function (c) {
  sync({waitFor: [Event("X", c), Event("O", c)]})
  sync({block: [Event("X", c), Event("O", c)]})
})
/*bthread("block X,O on nonempty cell", "Cell.NonEmpty", function (c) {
  sync({block: [Event("X", c), Event("O", c)]})
})*/

bthread("EnforceTurnsXO", function () {
  while (true) {
    sync({waitFor: XEvents, block: OEvents})
    sync({waitFor: OEvents, block: XEvents})
  }
})

// Represents when the game ends
bthread("block moves on endgame", function () {
  sync({waitFor: EndGame})
  sync({block: move})
})

// Represents when it is a draw
bthread("DetectDraw", function () {
  for (let i = 0; i < 9; i++) {
    sync({waitFor: move})
  }
  sync({request: Event('Draw')}, 90)
})

// Represents when X wins
bthread("DetectXWin", "Line.All", function (l) {
  let cells = getLineCells(l)
  const events = cells.map(c => Event("X", c))
  for (let c = 0; c < 3; c++) {
    sync({waitFor: events})
  }
  sync({request: [Event('XWin')]}, 100)
})

// Represents when O wins
bthread("DetectOWin", "Line.All", function (l) {
  let cells = getLineCells(l)
  const events = cells.map(c => Event("X", c))
  for (let c = 0; c < 3; c++) {
    sync({waitFor: events})
  }
  sync({request: [Event('OWin')]}, 100)
})

//#endregion GAME RULES


//#region PLAYER O STRATEGY

// Player O strategy to add a the third O to win
bthread("AddThirdO", "Line.All", function (l) {
  const cells = getLineCells(l)
  const events = cells.map(c => Event("O", c))
  sync({waitFor: events})
  sync({waitFor: events})
  sync({request: events}, 50)
})

// Player O strategy to prevent the third X of player X
bthread("PreventThirdX", "Line.All", function (l) {
  const cells = getLineCells(l)
  const OEvents = cells.map(c => Event("O", c))
  const XEvents = cells.map(c => Event("X", c))
  sync({waitFor: XEvents})
  sync({waitFor: XEvents})
  sync({request: OEvents})
})

//#region fork functions
// Player O strategy to prevent the Fork22 of player X
function addFork22PermutationBthreads(c1, c2) { //
  bthread("PreventFork22X_" + c1 + "_" + c2, function () {
    sync({waitFor: [Event("X", c1)]})
    sync({waitFor: [Event("X", c2)]})
    sync({request: [Event("O", getCell(2, 2)), Event("O", getCell(0, 2)), Event("O", getCell(2, 0))]}, 30)
  })
}

// Player O strategy to prevent the Fork02 of player X
function addFork02PermutationBthreads(c1, c2) { //
  bthread("PreventFork02X_" + c1 + "_" + c2, function () {
    sync({waitFor: [Event("X", c1)]})
    sync({waitFor: [Event("X", c2)]})
    sync({request: [Event("O", getCell(0, 2)), Event("O", getCell(0, 0)), Event("O", getCell(2, 2))]}, 30)
  })
}

// Player O strategy to prevent the Fork20 of player X
function addFork20PermutationBthreads(c1, c2) { //
  bthread("PreventFork20X_" + c1 + "_" + c2, function () {
    sync({waitFor: [Event("X", c1)]})
    sync({waitFor: [Event("X", c2)]})
    sync({request: [Event("O", getCell(2, 0)), Event("O", getCell(0, 0)), Event("O", getCell(2, 2))]}, 30)
  })
}

// Player O strategy to prevent the Fork00 of player X
function addFork00PermutationBthreads(c1, c2) { //
  bthread("PreventFork20X_" + c1 + "_" + c2, function () {
    sync({waitFor: [Event("X", c1)]})
    sync({waitFor: [Event("X", c2)]})
    sync({request: [Event("O", getCell(0, 0)), Event("O", getCell(0, 2)), Event("O", getCell(2, 0))]}, 30)
  })
}

// Player O strategy to prevent the Forkdiagonal of player X
function addForkdiagPermutationBthreads(c1, c2) { //
  bthread("PreventForkdiagX_" + c1 + "_" + c2, function () {
    sync({waitFor: [Event("X", c1)]})
    sync({waitFor: [Event("X", c2)]})
    sync({request: [Event("O", getCell(0, 1)), Event("O", getCell(1, 0)), Event("O", getCell(2, 1)), Event("O", getCell(1, 2))]}, 30)
  })
}

//#endregion fork functions

// Preference to put O on the center
bthread("Center", "Cell.Center", function (c) {
  sync({request: Event("O", c)}, 35)
})

// Preference to put O on the corners
bthread("Corner", "Cell.Corner", function (c) {
  sync({request: Event("O", c)}, 20)
})

// Preference to put O on the sides
bthread("Sides", "Cell.Sides", function (c) {
  sync({request: Event("O", c)}, 10)
})

bthread("init forks", function () {
  sync({waitFor: Any('CTX.Changed')})
  const forks22 = [[getCell(1, 2), getCell(2, 0)], [getCell(2, 1), getCell(0, 2)], [getCell(1, 2), getCell(2, 1)]]
  const forks02 = [[getCell(1, 2), getCell(0, 0)], [getCell(0, 1), getCell(2, 2)], [getCell(1, 2), getCell(0, 1)]]
  const forks20 = [[getCell(1, 0), getCell(2, 2)], [getCell(2, 1), getCell(0, 0)], [getCell(2, 1), getCell(1, 0)]]
  const forks00 = [[getCell(0, 1), getCell(2, 0)], [getCell(1, 0), getCell(0, 2)], [getCell(0, 1), getCell(1, 0)]]

  const forksdiag = [[getCell(0, 2), getCell(2, 0)]]

  const permsforks = [[0, 1], [1, 0]]

  forks22.forEach(function (f) {
    permsforks.forEach(function (p) {
      addFork22PermutationBthreads(f[p[0]], f[p[1]])
    })
  })

  forks02.forEach(function (f) {
    permsforks.forEach(function (p) {
      addFork02PermutationBthreads(f[p[0]], f[p[1]])
    })
  })

  forks20.forEach(function (f) {
    permsforks.forEach(function (p) {
      addFork20PermutationBthreads(f[p[0]], f[p[1]])
    })
  })

  forks00.forEach(function (f) {
    permsforks.forEach(function (p) {
      addFork00PermutationBthreads(f[p[0]], f[p[1]])
    })
  })

  forksdiag.forEach(function (f) {
    permsforks.forEach(function (p) {
      addForkdiagPermutationBthreads(f[p[0]], f[p[1]])
    })
  })
})
//#endregion PLAYER O STRATEGY


bthread("simulate x", "Cell.All", function (cell) {
  sync({request: Event("X", cell)})
})