importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema);

function populate(name, pattern) {
    const boardSize = pattern["board size"], maxGenerations = pattern["generations"], seed = pattern["seed"];

    bp.registerBThread("PopulateDB", function() {
        let cells = [];
        for(let i = 0; i < boardSize; i++) {
            let row=[];
            for(let j = 0; j < boardSize; j++) {
                row.push(new Cell(i, j, false));
            }
            cells.push(row);
        }



        bp.sync({ request: bp.Event("board size", boardSize) }); // for gui
        bp.sync({ waitFor: bp.Event("UI is ready") }); // for gui
        bp.sync({ request: bp.Event("Pattern", name) }); // for gui

        bp.sync({ request: CTX.InsertEvent( [].concat.apply([], cells)) });

        bp.sync({ request: CTX.TransactionEvent(seed.map(cell =>
                CTX.UpdateEvent("Reproduce", {"cell": new Cell(cell.i, cell.j, false)}))) });

        bp.sync({ request: CTX.InsertEvent(new GameOfLife(maxGenerations, boardSize)) });
    });
}

const patterns = {
    "Two Lonely Cells" : {
        "generations": 6,
        "board size": 3,
        "seed": [{"i": 1, "j": 0}, {"i": 1, "j": 2}]
    },
    "A Row" : {
        "generations": 20,
        "board size": 3,
        "seed": [{"i": 1, "j": 0}, {"i": 1, "j": 1}, {"i": 1, "j": 2}]
    },
    "Blinker 1" : {
        "generations": 20,
        "board size": 6,
        "seed": [
            {"i": 1, "j": 3}, {"i": 1, "j": 4},
            {"i": 2, "j": 3}, {"i": 2, "j": 4},
            {"i": 3, "j": 1}, {"i": 3, "j": 2},
            {"i": 4, "j": 1}, {"i": 4, "j": 2},
        ]
    },
    "Mating" : {
        "generations": 20,
        "board size": 6,
        "seed": [
            {"i": 1, "j": 2}, {"i": 2, "j": 3},
            {"i": 3, "j": 3}
        ]
    }
};

// const patternName = "Two Lonely Cells";
// const patternName = "A Row";
// const patternName = "Blinker 1";
const patternName = "Mating";

populate(patternName, patterns[patternName]);
