bp.registerBThread("PopulateDB", function() {
    const boardSize = 3;
    let cells = [];
    const livingCells = [{"i": 1, "j": 0}, {"i": 1, "j": 1}, {"i": 1, "j": 2}];

    for(let i = 0; i < boardSize; i++) {
        for(let j = 0; j < boardSize; j++) {
            cells.push(new Cell(i, j, false));
        }
    }

    bp.sync({ request: CTX.InsertEvent(cells) });

    bp.sync({ request: CTX.TransactionEvent(livingCells.map(cell => CTX.UpdateEvent("Spawn", cell))) });

    bp.sync({ request: CTX.InsertEvent(new GameOfLife(0, 3, boardSize)) });
});