importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema);

function registerAllCellsQueries() {
    for (var i = 0; i < 3; i++) {
        for (var j = 0; j < 3; j++) {
            CTX.registerParameterizedContextQuery("SpecificCell", "Cell[" + i + "," + j + "]", {
                "i": i,
                "j": j
            });
        }
    }
}

function populate(name, pattern) {
    const boardSize = pattern["board size"], maxGenerations = pattern["generations"], seed = pattern["seed"];

    bp.registerBThread("PopulateDB", function() {
        let cells = [];
        for(let i = 0; i < boardSize; i++) {
            let row=[];
            for(let j = 0; j < boardSize; j++) {
                row.push(new Cell(i, j, false));
                CTX.registerParameterizedContextQuery("MatingArea", "Mating area of Cell(" + i + "," + j + ")", {
                    "deadCellI": i,
                    "deadCellJ": j
                });
            }
            cells.push(row);
        }



        bp.sync({ request: bp.Event("board size", boardSize) }); // for gui
        bp.sync({ waitFor: bp.Event("UI is ready") }); // for gui
        bp.sync({ request: bp.Event("Pattern", name) }); // for gui

        bp.sync({ request: bp.Event("CTX.Insert", [].concat.apply([], cells)) });
        for (let i = 0; i < seed.length; i++)
            bp.sync({ request: bp.Event("Reproduce", {"cell": new Cell(seed[i].i, seed[i].j, false)}) });

        bp.sync({ request: bp.Event("CTX.Insert", new GameOfLife(maxGenerations, boardSize)) });
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
        "board size": 8,
        "seed": [
            {"i": 1, "j": 2}, {"i": 2, "j": 3}, {"i": 3, "j": 3},
            {"i": 2, "j": 5}, {"i": 3, "j": 5}, {"i": 4, "j": 5}
        ]
    }
};

const patternName = "Two Lonely Cells";
// const patternName = "A Row";
// const patternName = "Blinker 1";
// const patternName = "Mating";

populate(patternName, patterns[patternName]);
