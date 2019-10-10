bp.registerBThread("PopulateDB", function() {
    const boardSize = 3;
    const maxGenerations = 4;
    let cells = [];
    const row = [{"i": 1, "j": 0}, {"i": 1, "j": 1}, {"i": 1, "j": 2}];
    const twoLonely = [{"i": 1, "j": 0}, {"i": 1, "j": 2}];

    for(let i = 0; i < boardSize; i++) {
        for(let j = 0; j < boardSize; j++) {
            cells.push(new Cell(i, j, false));
        }
    }

    bp.sync({ request: bp.Event("board size", boardSize) }); // for gui

    bp.sync({ request: CTX.InsertEvent(cells) });

    bp.sync({ request: CTX.TransactionEvent(row.map(cell => CTX.UpdateEvent("Reproduce", cell))) });

    bp.sync({ request: CTX.InsertEvent(new GameOfLife(maxGenerations, boardSize)) });
});