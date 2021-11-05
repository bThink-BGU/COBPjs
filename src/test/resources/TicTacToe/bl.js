//#region HELPER FUNCTIONS
function getCell(i, j) {
  return ctx.getEntityById("cell(" + i + "," + j + ")")
}

function getLineCells(l) {
  return [getCell(l.c1.i, l.c1.j), getCell(l.c2.i, l.c2.j), getCell(l.c3.i, l.c3.j)]
}

function opposite(c){
  return getCell(2-c.i,2-c.j)
}

//#endregion HELPER FUNCTIONS

//#region EventSets
// Represents Enforce Turns
const move = bp.EventSet("MoveEvents", function (e) {
  return ['X', 'O'].includes(String(e.name))
})
const XEvents = bp.EventSet("XEvents", function (e) {
  return e.name == "X"
})
const OEvents = bp.EventSet("OEvents", function (e) {
  return e.name == "O"
})
const EndGame = bp.EventSet("EndGame", function (e) {
  return ['OWin', 'XWin', 'Draw'].includes(String(e.name))
})
//#endregion EventSets

//#region GAME RULES

//block X,O on nonempty cell
ctx.bthread("Click to X", "Cell.All", function (c) {
  // bp.log.info("Click("+c.i+","+c.j+")")
  sync({waitFor: Event("Click("+c.i+","+c.j+")")})
  sync({request: Event("X", c)})
})

//block X,O on nonempty cell
ctx.bthread("block X,O on nonempty cell", "Cell.All", function (c) {
  bp.log.info("cell={0}",c)
  sync({waitFor: [Event("X", c), Event("O", c)]})
  sync({block: [Event("X", c), Event("O", c)]})
})

bthread("EnforceTurnsXO", function () {
  while (true) {
    sync({waitFor: XEvents, block: OEvents})
    sync({waitFor: OEvents, block: XEvents})
  }
})

// Represents when the game ends
bthread("block moves on endgame", function () {
  sync({waitFor: EndGame})
  sync({block: bp.all})
})

// Represents when it is a draw
bthread("DetectDraw", function () {
  for (let i = 0; i < 9; i++) {
    sync({waitFor: move})
  }
  sync({request: Event('Draw')}, 90)
})

// Represents when X wins
ctx.bthread("DetectXWin", "Line.All", function (l) {
  let cells = getLineCells(l)
  const events = cells.map(c => Event("X", c))
  for (let c = 0; c < 3; c++) {
    sync({waitFor: events})
  }
  sync({request: Event('XWin')}, 100)
})

// Represents when O wins
ctx.bthread("DetectOWin", "Line.All", function (l) {
  let cells = getLineCells(l)
  const events = cells.map(c => Event("O", c))
  for (let c = 0; c < 3; c++) {
    sync({waitFor: events})
  }
  sync({request: Event('OWin')}, 100)
})

//#endregion GAME RULES


//#region PLAYER O STRATEGY

//1) Win: If the player has two in a row, they can place a third to get three in a row.
ctx.bthread("AddThirdO", "Line.All", function (l) {
  const cells = getLineCells(l)
  const events = cells.map(c => Event("O", c))
  sync({waitFor: events})
  sync({waitFor: events})
  sync({request: events}, 50)
})

//2) Block: If the opponent has two in a row, the player must play the third themselves to block the opponent.
ctx.bthread("PreventThirdX", "Line.All", function (l) {
  bp.log.info("line={0}",l)
  const cells = getLineCells(l)
  const OEvents = cells.map(c => Event("O", c))
  const XEvents = cells.map(c => Event("X", c))
  sync({waitFor: XEvents})
  sync({waitFor: XEvents})
  sync({request: OEvents},45)
})

//3) Fork: Create an opportunity where the player has two ways to win (two non-blocked lines of 2).
ctx.bthread("fork", 'Fork.All', function (f) {
  let e1 = Event("O", getCell(f.x[0].i, f.x[0].j))
  let e2 = Event("O", getCell(f.x[1].i, f.x[1].j))
  let O = [];
  for(let i=0; i< f.block.length; i++)
    O.push(Event("O", getCell(f.block[i].i, f.block[i].j)))

  sync({waitFor: [e1,e2]})
  sync({waitFor: [e1,e2]})
  sync({request: O}, 40)
})


/*4) Blocking an opponent's fork:
If there is only one possible fork for the opponent, the player should block it.
Otherwise, the player should block all forks in any way that simultaneously allows them to create two in a row.
Otherwise, the player should create a two in a row to force the opponent into defending, as long as it doesn't result in them creating a fork.
For example, if "X" has two opposite corners and "O" has the center, "O" must not play a corner move in order to win.
(Playing a corner move in this scenario creates a fork for "X" to win.)*/
ctx.bthread("fork", 'Fork.All', function (f) {
  let e1 = Event("X", getCell(f.x[0].i, f.x[0].j))
  let e2 = Event("X", getCell(f.x[1].i, f.x[1].j))
  let O = [];
  for(let i=0; i< f.block.length; i++)
    O.push(Event("O", getCell(f.block[i].i, f.block[i].j)))

  sync({waitFor: [e1, e2]})
  sync({waitFor: [e1, e2]})
  sync({request: O}, 35)
})

/*5) Center: A player marks the center.
(If it is the first move of the game, playing a corner move gives the second player more opportunities to make a mistake and may therefore be the better choice; however, it makes no difference between perfect players.)*/
ctx.bthread("Center", "Cell.Center", function (c) {
  bp.log.info("center={0}",c)
  sync({request: Event("O", c)},30)
})

//6) Opposite corner: If the opponent is in the corner, the player plays the opposite corner.
ctx.bthread("Opposite corner", "Cell.Corner", function (c) {
  bp.log.info("opposite={0}",c)
  sync({waitFor: Event("O", c)})
  sync({request: Event("O", opposite(c))},25)
})

//7) Empty corner: The player plays in a corner square.
ctx.bthread("Corner", "Cell.Corner", function (c) {
  sync({request: Event("O", c)},20)
})

//8) Empty side: The player plays in a middle square on any of the 4 sides.
ctx.bthread("Corner", "Cell.Sides", function (c) {
  bp.log.info("sides={0}",c)
  sync({request: Event("O", c)},15)
})





//
////#old strategy
//
//// Player O strategy to add a the third O to win
//bthread("AddThirdO", "Line.All", function (l) {
//  const cells = getLineCells(l)
//  const events = cells.map(c => Event("O", c))
//  sync({waitFor: events})
//  sync({waitFor: events})
//  sync({request: events}, 50)
//})
//
//// Player O strategy to prevent the third X of player X
//bthread("PreventThirdX", "Line.All", function (l) {
//  const cells = getLineCells(l)
//  const OEvents = cells.map(c => Event("O", c))
//  const XEvents = cells.map(c => Event("X", c))
//  sync({waitFor: XEvents})
//  sync({waitFor: XEvents})
//  sync({request: OEvents})
//})
//
//// Player O strategy to prevent fork of player X
//bthread("fork", 'Fork.All', function (f) {
//  let e1 = Event("X", getCell(f.x[0].i, f.x[0].j))
//  let e2 = Event("X", getCell(f.x[1].i, f.x[1].j))
//  let O = [];
//  for(let i=0; i< f.block.length; i++)
//    O.push(Event("O", getCell(f.block[i].i, f.block[i].j)))
//
//  sync({waitFor: [e1, e2]})
//  sync({waitFor: [e1, e2]})
//  sync({request: O}, 30)
//})
//
//// Preference to put O on the center
//bthread("Center", "Cell.Center", function (c) {
//  sync({request: Event("O", c)}, 35)
//})
//
//// Preference to put O on the corners
//bthread("Corner", "Cell.Corner", function (c) {
//  sync({request: Event("O", c)}, 20)
//})
//
//// Preference to put O on the sides
//bthread("Sides", "Cell.Sides", function (c) {
//  sync({request: Event("O", c)}, 10)
//})

//#endregion PLAYER O STRATEGY