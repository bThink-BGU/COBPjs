/**
 this version is based on the version from "TTT-Final.js"
 In this version, each cell will hold data about its mark. To use this we will use the "Effect" mechanism of cobp.
 In addition, we moved the initialization of the row entities to init functions
 */



// The game is played on a grid of 3x3 cells.
const boardWidth = 3
const boardHeight = 3
let cells = []
for (let i = 0; i < boardWidth; i++) {
    for (let j = 0; j < boardHeight; j++) {
        cells.push(ctx.Entity('cell(' + i + ',' + j + ')', 'cell', {i: i, j: j}))
    }
}
ctx.populateContext(cells)
ctx.registerQuery('Cell', entity => entity.type === String('cell'))


// The game is played by two players, X and O
let players = [ctx.Entity('X', 'player'), ctx.Entity('O', 'player')]
ctx.populateContext(players)
ctx.registerQuery('Player', entity => entity.type === String('player'))

// The players take turns marking the cells
function markCell(player, cell) {
    return Event('mark', {player: player.id, cell: cell.id})
}

ctx.registerEffect('mark', function ({playerID, cellID}) //if this doesnt work, receive "data" instead of {playerID, cellID} and use data.player and data.cell
{
    ctx.getEntityById(cellID).mark = playerID
})
const AnyMark = bp.EventSet('AnyMark', e => e.name === 'mark')

function AnyMarkPlayer(player) {
    return bp.EventSet('AnyMarkPlayer', e => e.name === 'mark' && e.data.player === player.id)
}

function AnyMarkCell(cell) {
    return bp.EventSet('AnyMarkCell', e => e.name === 'mark' && e.data.cell === cell.id)
}

ctx.bthread('2.2 The players take turns marking the cells', function () {
    while (true) {


        sync({
            waitFor: AnyMarkPlayer(ctx.getEntityById('X')),
            block: AnyMark.minus(AnyMarkPlayer(ctx.getEntityById('X')))
        })
        sync({
            waitFor: AnyMarkPlayer(ctx.getEntityById('O')),
            block: AnyMark.minus(AnyMarkPlayer(ctx.getEntityById('O')))
        })
    }
})

// 3. Each cell can be marked only once.
ctx.bthread('3. Each cell can be marked only once.', 'Cell', function (cell) {
    sync({waitFor: AnyMarkCell(cell)})
    sync({block: AnyMarkCell(cell)})
})

// 4. The first player to mark 3 cells in a row wins.

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
//5. if all 9 cells of the board have been marked with X and O, but no player won, the game is over.
function win(player) {
    return Event('win', {player: player.id})
}

const AnyWin = bp.EventSet('AnyWin', e => e.name == 'win')
// ! this context is not working(not supported yet)
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


// ---------------------STRATEGY----------------------------------
/**
 The player O can play a perfect game of tic-tac-toe (to win or at least draw)
 if, each time it is their turn to play,
 they choose the first available move from the following list.
 */
// ctx.registerQuery('Player_O', entity => entity.type === String('player') && entity.id === String('O'))
// //1. If the player has two cells in a row, he plays the third to get three in a row.
// let priority = 100
// ctx.bthread('1. If the player has two cells in a row, he plays the third to get three in a row.', ['Player_O', '3CellsInARow'], function (player, row) {
//     for (let i = 0; i < 2; i++) {
//         sync({waitFor: row.cells.map(cell => markCell(player, cell))})
//     }
//     sync({request: row.cells.filter(cell => cell.mark == null).map(cell => markCell(player, cell))}, priority--)
// })
// //2. If the opponent has two cells in a row, the player plays the third to block him.
// ctx.bthread('2. If the opponent has two cells in a row, the player plays the third to block him.', ['Player_O', '3CellsInARow'], function (playerO, row) {
//     for (let i = 0; i < 2; i++) {
//         sync({waitFor: row.cells.map(cell => markCell(ctx.getEntityById("X"), cell))})
//     }
//     sync({request: row.cells.filter(cell => cell.mark == null).map(cell => markCell(playerO, cell))}, priority--)
// })
//
// //3. fork(skipped for now)
// //4. blocking fork(skipped for now)
// //5. Center: A player marks the center.
// ctx.bthread('5. Center: A player marks the center.', ['Player_O'], function (player) {
//     sync({request: markCell(player, ctx.getEntityById('cell(1,1)'))}, priority--)
// })
//
// //6. Opposite corner: If the opponent is in the corner, the player plays the opposite corner.
// ctx.bthread('6. Opposite corner: If the opponent is in the corner, the player plays the opposite corner.', ['Player_O'], function (Player_O) {
//         sync({waitFor: markCell(ctx.getEntityById('X'), ctx.getEntityById('cell(0,0)'))})
//         sync({request: markCell(Player_O, ctx.getEntityById('cell(2,2)'))}, priority--)
//     }
// )
// //7. Empty corner: The player plays in a corner square.
// const corners = [ctx.getEntityById('cell(0,0)'), ctx.getEntityById('cell(0,2)'), ctx.getEntityById('cell(2,0)'), ctx.getEntityById('cell(2,2)')]
//
// ctx.bthread('7. Empty corner: The player plays in a corner square.', ['Player_O'], function (Player_O) {
//     sync({request: corners.filter(cell => cell.mark == null).map(cell => markCell(Player_O, cell))}, priority--)
//
//
// })
//
// //8. Empty side: The player plays in a middle square on any of the 4 sides.
// const sides = [ctx.getEntityById('cell(0,1)'), ctx.getEntityById('cell(1,0)'), ctx.getEntityById('cell(1,2)'), ctx.getEntityById('cell(2,1)')]
// ctx.bthread('8. Empty side: The player plays in a middle square on any of the 4 sides.', ['Player_O'], function (Player_O) {
//     sync({request: sides.filter(cell => cell.mark == null).map(cell => markCell(Player_O, cell))}, priority--)
// })
