


const boardWidth = 3
const boardHeight = 3
let cells = []
for (let i = 0; i < boardWidth; i++) {
    for (let j = 0; j < boardHeight; j++) {
        cells.push(ctx.Entity('cell(' + i + ',' + j + ')', 'cell', {i: i, j: j}))
    }
}
ctx.populateContext(cells)
ctx.registerQuery('Cell', entity => entity.type == String('cell'))

// Define a function to display the game board in the console
const displayBoard = () => {
    bp.log.setLevel("Fine")
    bp.log.info("Board:")
    for (let i = 0; i < cells.length; i++) {
        // console.log(cells[i] + " | " + cells[++i] + " | " + cells[++i]);
        bp.log.info((cells[i].mark ? cells[i].mark :  "-")  + " | " + (cells[++i].mark ? cells[i].mark :  "-") + " | " + (cells[++i].mark ? cells[i].mark :  "-"))
        if (i !== cells.length - 1) {
            // console.log("---------");
            bp.log.info("---------");
        }
    }
    // console.log("\n");
    bp.log.info("\n");
};


let players = [ctx.Entity('X', 'player'), ctx.Entity('O', 'player')]
ctx.populateContext(players)
ctx.registerQuery('Player', entity => entity.type == String('player'))

function markCell(player, cell) {
        return Event('mark', {player: player.id, cell: cell.id})
    }

ctx.registerEffect('mark', function (data)
{
    ctx.getEntityById(data.cell).mark = data.player
    displayBoard()
})
const AnyMark = bp.EventSet('AnyMark', e => e.name == 'mark')

function AnyMarkPlayer(player) {
    return bp.EventSet('AnyMarkPlayer', e => e.name == 'mark' && e.data.player == player.id)
}

function AnyMarkCell(cell) {
    return bp.EventSet('AnyMarkCell', e => e.name == 'mark' && e.data.cell == cell.id)
}

ctx.bthread('2.2 The players take turns marking the cells', function () {
    while (true) {
        sync({ waitFor: AnyMarkPlayer(ctx.getEntityById('X')), block: AnyMarkPlayer(ctx.getEntityById('O')) })
        sync({ waitFor: AnyMarkPlayer(ctx.getEntityById('O')), block: AnyMarkPlayer(ctx.getEntityById('X')) })
    }
})

Example: // 3. Each cell can be marked only once.
    Output: ctx.bthread('3. Each cell can be marked only once.', 'Cell', function (cell) {
        sync({waitFor: AnyMarkCell(cell)})
        sync({block: AnyMarkCell(cell)})
    })

function initVerticalRows(rowLength, boardWidth, boardHeight) {
        let verticalRows = []
        for (let i = 0; i < boardWidth; i++) {
            for (let j = 0; j < boardHeight - (rowLength - 1); j++) {
                let row = []
                for (let k = 0; k < rowLength; k++) {
                    row.push(ctx.getEntityById('cell(' + i + ',' + (j + k) + ')'))
                }
                verticalRows.push(ctx.Entity('Vertical from (' + i + ',' + j + ')to (' + i + ',' + (j + rowLength - 1) + ')', '3CellsInARow', {cells: row}))
            }

        }
        return verticalRows
    }

function initHorizontalRows(rowLength, boardWidth, boardHeight) {
    let horizontalRows = []
    for (let i = 0; i < boardHeight; i++) {
        for (let j = 0; j < boardWidth - (rowLength - 1); j++) {
            let row = []
            for (let k = 0; k < rowLength; k++) {
                row.push(ctx.getEntityById('cell(' + (j + k) + ',' + i + ')'))
            }
            horizontalRows.push(ctx.Entity('Horizontal from (' + j + ',' + i + ')to (' + (j + rowLength - 1) + ',' + i + ')', '3CellsInARow', {cells: row}))
        }
    }
    return horizontalRows
}

function initDiagonalRows(rowLength, boardWidth, boardHeight) {
    let diagonalRows = []
    for (let i = 0; i < boardWidth - (rowLength - 1); i++) {
        for (let j = 0; j < boardHeight - (rowLength - 1); j++) {
            let row = []
            for (let k = 0; k < rowLength; k++) {
                row.push(ctx.getEntityById('cell(' + (i + k) + ',' + (j + k) + ')'))
            }
            rows.push(ctx.Entity('Diagonal_0 from(' + i + ',' + j + ') to (' + (i + rowLength - 1) + ',' + (j + rowLength - 1) + ')', '3CellsInARow', {cells: row}))
        }
    }
    for (let i = 0; i < boardWidth - (rowLength - 1); i++) {
        for (let j = rowLength - 1; j < boardHeight; j++) {
            let row = []
            for (let k = 0; k < rowLength; k++) {
                row.push(ctx.getEntityById('cell(' + (i + k) + ',' + (j - k) + ')'))
            }
            diagonalRows.push(ctx.Entity('Diagonal_1 from(' + i + ',' + j + ') to (' + (i + rowLength - 1) + ',' + (j - rowLength + 1) + ')', '3CellsInARow', {cells: row}))
        }
    }
    return diagonalRows
}

//init rows
let rowLength = 3
let rows = []
rows = rows.concat(initVerticalRows(rowLength, boardWidth, boardHeight), initHorizontalRows(rowLength, boardWidth, boardHeight), initDiagonalRows(rowLength, boardWidth, boardHeight))
ctx.populateContext(rows)
ctx.registerQuery('3CellsInARow', entity => entity.type == String('3CellsInARow'))


//4. The first player to mark 3 cells in a row wins.
function win(player) {
    return Event('win', {player: player.id})
}

const AnyWin = bp.EventSet('AnyWin', e => e.name == 'win')
ctx.bthread('4. The first player to mark 3 cells in a row wins.', ['Player', '3CellsInARow'], function (player, row) {
    for (let i = 0; i < 3; i++) {
        sync({waitFor: row.cells.map(cell => markCell(player, cell))})
    }
    sync({request: win(player), block: win(player).negate()})
    sync({block: bp.all})
})
function tie() {
        return Event('tie')
    }
bthread('5. When all 9 cells have been marked with X and O and no player won, it\'s a tie and the game is over.', function () {
    for (let i = 0; i < 9; i++) {
        sync({waitFor: AnyMark})
    }
    sync({request: tie(), block: tie().negate().minus(AnyWin)})
    sync({block: bp.all})
})





// -----------------------------------------------------------------------
// const readline = require('readline');
// const rl = readline.createInterface({
//     input: process.stdin,
//     output: process.stdout
// });
bthread('ask input from user', function () {
    while (true) {
        //generate random cell

        sync({request: cells.map(cell => markCell(ctx.getEntityById('X'), cell))})
        // sync({request: markCell(ctx.getEntityById('X'), ctx.getEntityById('cell(0,0)'))})
    }
})
bthread('O moves', function () {
    while (true) {
        sync({request: cells.map(cell => markCell(ctx.getEntityById('O'), cell))})
    }
})