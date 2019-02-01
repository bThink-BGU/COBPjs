importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.ttt.schema);

bp.log.info('Tic-Tac-Toe - Let the game begin!');

// GameRules:

//#region HELP FUNCTIONS
function createEvent(name, c) {
    return bp.Event(name,"{Cell:"+c+"}")
}

//#endregion HELP FUNCTIONS

//#region CEll BEHAVIORS

ctx.subscribe("ClickHandler","Cells", function(c) {
    while (true) {
        bp.sync({ waitFor:[ createEvent('Click', c) ] });
        bp.sync({ request:[ createEvent('X', c) ] });
    }
});

//detect black cell , white cell - query name
ctx.subscribe("detect black cell","Empty Cells",function(c) { //select * from cell where value == null
    bp.sync({ waitFor:[ createEvent('X', c), createEvent('O',c)] });
    ctx.updateDBEvent("mark cell as non empty",{cell:c });
});

//block X,O on black cell
ctx.subscribe("block X,O on black cell","NonEmpty Cells",function(c) { //select * from cell where value != null
    bp.sync({ block:[ createEvent('X', c), createEvent('O',c) ] });
});

//endregion CEll BEHAVIORS

//#region ENFORCE TURNS

// Represents Enforce Turns
var move = bp.EventSet("Move events", function(e) {
    return e.name === 'O' || e.name === 'X';
});
var turnX = bp.EventSet("turnX", function(e) {
    return e.name == 'X';
});
var turnO = bp.EventSet("turnO", function(e) {
    return e.name == 'O' ;
});
var EndGame = bp.EventSet("EndGame", function(e) {
    return e.name == 'OWin' || e.name == 'XWin' || e.name == 'Draw';
});

ctx.subscribe("EnforceTurns", function() {
    while (true) {
        bp.sync({
            waitFor: turnX,
            block: turnO
        });
        bp.sync({
            waitFor: turnO,
            block: turnX
        });
    }
});

//#endregion ENFORCE TURNS

//#region GAME RULES

// Represents when the game ends
ctx.subscribe("block all cells","",function() { //select * from playing where value == false
    bp.sync({ waitFor:EndGame });
    bp.sync({ block:[ turnX, turnO] });
});

// Represents when it is a draw
ctx.subscribe("DetectDraw", function() {
    // For debug
    bp.sync({ waitFor: move });
    bp.sync({ waitFor: move });
    bp.sync({ waitFor: move });

    bp.sync({ waitFor: move });
    bp.sync({ waitFor: move });
    bp.sync({ waitFor: move });

    bp.sync({ waitFor: move });
    bp.sync({ waitFor: move });
    bp.sync({ waitFor: move });
    /*
     * for (var i=0; i< 9; i++) { bp.sync({ waitFor:[ move ] }); }
     */
    bp.sync({ request: bp.Event('Draw') }, 90);
});

//#endregion GAME RULES

//#region TRIPLE BEHAVIORS

// Represents when X wins
//bp.registerBThread("DetectXWin","Triple",interrupt:endgame, function(t) {
//bp.registerBThread("DetectXWin",["Triple","Playing"], function(t) {
ctx.subscribe("DetectXWin","Triple",function(t) {
    for (var c = 0; c < 3; c++) {
        bp.sync({ waitFor:[ createEvent('X', t.cell0), createEvent('X', t.cell1), createEvent('X', t.cell2) ] });
    }
    bp.sync({ request:[ bp.Event('XWin') ] }, 100);
});

// Represents when O wins
ctx.subscribe("DetectOWin","Triple",function(t) {
    for (var c = 0; c < 3; c++) {
        bp.sync({ waitFor:[ createEvent('O', t.cell0), createEvent('O', t.cell1), createEvent('O', t.cell2) ] });
    }
    bp.sync({ request:[ bp.Event('OWin') ] }, 100);
});

// Player O strategy to add a the third O to win
ctx.subscribe("AddThirdO","Triple",function(t) {
    bp.sync({ waitFor:[ createEvent('O', t.cell0), createEvent('O', t.cell1), createEvent('O', t.cell2) ] });
    bp.sync({ waitFor:[ createEvent('O', t.cell0), createEvent('O', t.cell1), createEvent('O', t.cell2) ] });
    bp.sync({ request: [ createEvent('O', t.cell0), createEvent('O', t.cell1), createEvent('O', t.cell2) ] }, 50);
});

// Player O strategy to prevent the third X of player X
ctx.subscribe("PreventThirdX","Triple",function(t) {
    bp.sync({ waitFor:[ createEvent('X', t.cell0), createEvent('X', t.cell1), createEvent('X', t.cell2) ] });
    bp.sync({ waitFor:[ createEvent('X', t.cell0), createEvent('X', t.cell1), createEvent('X', t.cell2) ] });
    bp.sync({ request: [ createEvent('O', t.cell0), createEvent('O', t.cell1), createEvent('O', t.cell2) ] }, 40);
});

//#endregion TRIPLE BEHAVIORS

//#region PLAYER O STRATEGY

// Player O strategy to prevent the Fork22 of player X
function addFork22PermutationBthreads(c1,c2){ //
    ctx.subscribe("PreventFork22X", function() {
        bp.sync({ waitFor:[ createEvent('X',c1) ] });
        bp.sync({ waitFor:[ createEvent('X',c2) ] });
        bp.sync({ request:[ createEvent('O',{x:2,y:2}),createEvent('O',{x:0,y:2}), createEvent('O',{x:2,y:0})]}, 30);
    });
}

// Player O strategy to prevent the Fork02 of player X
function addFork02PermutationBthreads(c1,c2){ //
    ctx.subscribe("PreventFork02X", function() {
        bp.sync({ waitFor:[ createEvent('X',c1) ] });
        bp.sync({ waitFor:[ createEvent('X',c2) ] });
        bp.sync({ request:[ createEvent('O',{x:0,y:2}),createEvent('O',{x:0,y:0}), createEvent('O',{x:2,y:2})]}, 30);
    });
}

// Player O strategy to prevent the Fork20 of player X
function addFork20PermutationBthreads(c1,c2){ //
    ctx.subscribe("PreventFork20X", function() {
        bp.sync({ waitFor:[ createEvent('X',c1) ] });
        bp.sync({ waitFor:[ createEvent('X',c2) ] });
        bp.sync({ request:[ createEvent('O',{x:2,y:0}),createEvent('O',{x:0,y:0}), createEvent('O',{x:2,y:2})] }, 30);
    });
}

// Player O strategy to prevent the Fork00 of player X
function addFork00PermutationBthreads(c1,c2){ //
    ctx.subscribe("PreventFork20X", function() {
        bp.sync({ waitFor:[ createEvent('X',c1) ] });
        bp.sync({ waitFor:[ createEvent('X',c2) ] });
        bp.sync({ request:[ createEvent('O',{x:0,y:0}),createEvent('O', {x:0,y:2}), createEvent('O',{x:2,y:0})] }, 30);
    });
}

// Player O strategy to prevent the Forkdiagonal of player X
function addForkdiagPermutationBthreads(c1,c2){ //
    ctx.subscribe("PreventForkdiagX", function() {
        bp.sync({ waitFor:[ createEvent('X',c1) ] });
        bp.sync({ waitFor:[ createEvent('X',c2) ] });
        bp.sync({ request:[ createEvent('O', {x:0,y:1}),createEvent('O',{x:1,y:0}), reateEvent('O', {x:2,y:1}), createEvent('O', {x:1,y:2}) ] }, 30);
    });
}

// Preference to put O on the center
bp.registerBThread("Center", function() {
    while (true) {
        bp.sync({ request:[ createEvent('O', {x:1,y:1}) ] }, 35);
    }
});

// Preference to put O on the corners
bp.registerBThread("Corners", function() {
    while (true) {
        bp.sync({ request:[ createEvent('O',{x:0,y:0}),createEvent('O',{x:0,y:2}),
                createEvent('O', {x:2,y:0}), createEvent('O', {x:2,y:2}) ] }, 20);
    }
});

// Preference to put O on the sides
bp.registerBThread("Sides", function() {
    while (true) {
        bp.sync({ request:[ createEvent('O', {x:0,y:1}),createEvent('O', {x:1,y:0}),
                createEvent('O',{x:2,y:1}), createEvent('O',{x:1,y:2}) ] }, 10);
    }
});

var forks22 = [ [ { x:1, y:2 }, { x:2, y:0 } ], [ { x:2, y:1 }, { x:0, y:2 } ], [ { x:1, y:2 }, { x:2, y:1 } ] ];
var forks02 = [ [ { x:1, y:2 }, { x:0, y:0 } ], [ { x:0, y:1 }, { x:2, y:2 } ], [ { x:1, y:2 }, { x:0, y:1 } ] ];
var forks20 = [ [ { x:1, y:0 }, { x:2, y:2 } ], [ { x:2, y:1 }, { x:0, y:0 } ], [ { x:2, y:1 }, { x:1, y:0 } ] ];
var forks00 = [ [ { x:0, y:1 }, { x:2, y:0 } ], [ { x:1, y:0 }, { x:0, y:2 } ], [ { x:0, y:1 }, { x:1, y:0 } ] ];

var forksdiag = [ [ { x:0, y:2 }, { x:2, y:0 } ]];

var permsforks = [ [ 0, 1 ], [ 1, 0 ] ];

forks22.forEach(function(f) {
    permsforks.forEach(function(p) {
        addFork22PermutationBthreads(f[p[0]],f[p[1]]);
    });
});

forks02.forEach(function(f) {
    permsforks.forEach(function(p) {
        addFork02PermutationBthreads(f[p[0]], f[p[1]]);
    });
});

forks20.forEach(function(f) {
    permsforks.forEach(function(p) {
        addFork20PermutationBthreads(f[p[0]], f[p[1]]);
    });
});

forks00.forEach(function(f) {
    permsforks.forEach(function(p) {
        addFork00PermutationBthreads(f[p[0]], f[p[1]]);
    });
});

forksdiag.forEach(function(f) {
    permsforks.forEach(function(p) {
        addForkdiagPermutationBthreads(f[p[0]], f[p[1]]);
    });
});
//#endregion PLAYER O STRATEGY

//#region CONTEXT POPULATION

// ADD CELLS
for (var r = 0; r < 3; r++) {
    for (var c = 0; c < 3; c++) {
        //ctx.update('Add Cell', {i: r, j: c, value:null});
        bp.sync({ request:UpdateContexDBEvent("Add Cell").parameters({i: r, j: c, value:null}) });
    }
}

// ADD TRIPLE
for (var c = 0; c < 3; c++) {
    //ctx.update('Add Cell', {i: r, j: c, value:null});
    bp.sync({ request:UpdateContexDBEvent("Add Triple").parameters({cell0: {x:c,y:0}, cell1: {x:c,y:1}, cell2:{x:c,y:2}})});
    bp.sync({ request:UpdateContexDBEvent("Add Triple").parameters({cell0: {x:0,y:c}, cell1: {x:1,y:c}, cell2:{x:2,y:c}})});
}
bp.sync({ request:UpdateContexDBEvent("Add Triple").parameters({cell0: {x:0,y:0}, cell1: {x:1,y:1}, cell2:{x:2,y:2}}) });
bp.sync({ request:UpdateContexDBEvent("Add Triple").parameters({cell0: {x:2,y:0}, cell1: {x:1,y:1}, cell2:{x:0,y:2}}) });

//#endregion CONTEXT POPULATION