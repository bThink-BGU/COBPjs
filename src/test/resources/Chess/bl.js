const AnyCastling = bp.EventSet("AnyCastling", function (e) {
  return false
})


/*
  1. PGN basics :
       1.a. Moves :
               Pawn -> e4
               Knight -> Nf6
               Bishop -> Be3
               Rook -> Rh3
               Queen -> Qd2
               King -> Kc2
       1.b. Taking :
               Pawn -> exd5
               Knight -> Nxd5
               Bishop -> Bxf4
               Rook -> Rxd2
               Queen -> Qxe3
               King -> Kxg1
       1.c. Special Exceptions :
               Short castle -> O-O
               Long castle -> O-O-O
               Check ( moving or taking ) -> Qxd3+ , e4+
               Mate ( moving or taking ) -> Qxd3# , e4#
               Two pieces from the same king can move to the same cell ( or take on this cell ) -> R1d1 , fxe5

   2. Center squares - c4, d4, e4, f4, c5, d5, e5, f5

*/

// Define moves

const anyMoves = bp.EventSet("anyMove", function (e) {
  return e.name.startsWith("Move")
})


const ESCenterCaptureMoves = bp.EventSet("EScenterCaptureMoves", function (e) {
  return e.name == 'Move' && ( e.data.dst[1] == '3' || e.data.dst[1] == '4' )
    && ( e.data.src[0] == 'c'  || e.data.src[0] == 'd' || e.data.src[0] == 'e'  || e.data.src[0] == 'f' || e.data.src[0] == 'b'  || e.data.src[0] == 'g' )
    && ( e.data.dst[0] == 'c'  || e.data.dst[0] == 'd' || e.data.dst[0] == 'e'  || e.data.dst[0] == 'f')

})

const ESPawnDevelopingMoves = bp.EventSet("ESpawnDevelopingMoves", function (e) {
  return e.name == 'Move' && ( e.data.dst[1] == '3' || e.data.dst[1] == '4' )
})

const ESFianchettoMoves = bp.EventSet("ESfianchettoMoves", function (e) {
  return e.name == 'Move' &&  e.data.dst[1] == '3' &&
    ( e.data.dst[0] == 'b' || e.data.dst[0] == 'g' )
})


// Game behavioral thread
bthread("Game thread", function (entity) {
  bp.log.info("START!")
  while (true) {
    sync({request: bp.Event("Game Phase", "Opening")})
    sync({waitFor: AnyCastling})
    sync({request: bp.Event("Game Phase", "Mid Game")})
    sync({request: bp.Event("Game Phase", "End Game")})
  }
});

/* Strategies in the opening:
    1. Developing pieces
    2. Strengthening the center squares
    3. Fianchetto
    What is the probability of each of the strategies to be executed given a certain move \ situation ?
 */



/*
ctx.bthread("interleave", "Phase.Opening", function (phase) {
    while(true) {
        sync({waitFor: bp.Event("Starting developing", "pawns")})
        sync({waitFor: bp.Event("Strengthen", "Pawn")})
    }
})
*/


/*ctx.bthread("Developing pieces", "Phase.Opening", function (phase) {
    bp.log.info("Starting Developing pieces...")
    sync({request: bp.Event("Starting developing", "pawns")})
    // until a break event happens
    sync({request: bp.Event("Starting developing", "bishop and knight")})
    sync({request: bp.Event("Starting developing", "queen and rook")})
})*/

/*ctx.bthread("Starting developing", "Phase.Opening", function (phase) {
   /!* bp.log.info("Let's Start ")
    bp.log.info("ESPawnDevelopingMoves")
    bp.log.info(ESpawnDevelopingMoves)
    bp.log.info(ESpawnDevelopingMoves.contains(moveEvent("Pawn", "c2", "c3")))
   *!/
   // let i=0
   //bp.data.put("# developing moves",0)
    // while (devPawns()) {
    while (true) {

        let pawnMoves = []
        let pawnsArr = Array.from(ctx.runQuery("Piece.White.Pawn"))
        bp.log.info(pawnsArr)

        for (let i = 0; i < pawnsArr.length; i++) {
            pawnMoves = pawnMoves.concat(
            availableStraightCellsFromPawn(pawnsArr[i], 2).concat(availableStraightCellsFromPawn(pawnsArr[i], 1)).
            filter(m => ESPawnDevelopingMoves.contains(m)))
            /!*bp.log.info("develop moves")
            bp.log.info(developMoves)*!/
        }
        /!*bp.log.info("PAWN MOVES")
        bp.log.info(pawnMoves)*!/
        let e = sync({request: pawnMoves, waitFor: anyMoves})
        //if(ESPawnDevelopingMoves.contains(e)) bp.data.put("# developing moves",++i) //TODO: check why not working


        //bp.data.put("Explanation - Developing pawn", i++) // this i counter suppose to quantify the probabilities

        // let piece = ctx.getEntityById(ctx.getEntityById(dst).pieceId)
        // sync({request: Explanation("piece", 'Explanation - Developing pawn')})
    }
})*/

/*ctx.bthread("Strengthening the center", "Phase.Opening", function (phase) {
    bp.log.info("Starting Strengthening the center...")
    while (true) {
        sync({request: bp.Event("Strengthen", "Pawn")})
    }
})*/


function clearDuplicates(pawnMoves) {
  let pawnMovesToRequest = []
  let dup = false
  for(let i = 0; i < pawnMoves.length; i++) {
    dup = false
    for(let j = 0; j < pawnMovesToRequest.length; j++) {
      if( pawnMoves[i].data.src == pawnMovesToRequest[j].data.src &&
        pawnMoves[i].data.dst == pawnMovesToRequest[j].data.dst ) {
        dup = true
        break;
      }
    }
    if(!dup)
      pawnMovesToRequest.push(pawnMoves[i])
  }
  return pawnMovesToRequest
}

function filterOccupiedCellsMoves(pawnMovesSet, cellsArr) {
  let retArr = []
  for(let i = 0; i < pawnMovesSet.length; i++) {
    let srcCellFound = false
    let dstCellFound = false
    for(let cell of cellsArr.values()) {
      if (pawnMovesSet[i].data.src == cell.id) {
        srcCellFound = true
      }
      if (pawnMovesSet[i].data.dst == cell.id) {
        dstCellFound = true
      }
    }
    if(srcCellFound == false && dstCellFound == true) {
      retArr.push(pawnMovesSet[i])
    }
  }
  return retArr
}

ctx.bthread("Strengthen", "Phase.Opening", function (entity) {
  while (true) {
    let pawnMoves = []
    let pawnsSet = ctx.runQuery("Piece.White.Pawn")
    let cellsSet = ctx.runQuery("Cell.all.nonOccupied")

    for(let pawn of pawnsSet.values()) {
      pawnMoves = pawnMoves.concat(
        availableStraightCellsFromPawn(pawn, 2)
          .concat(availableStraightCellsFromPawn(pawn, 1))
          .filter(m => ESCenterCaptureMoves.contains(m))
      )
    }

    let pawnMovesSet = clearDuplicates(pawnMoves)
    let pawnMovesToRequest = filterOccupiedCellsMoves(pawnMovesSet, cellsSet)

    sync({request: pawnMovesToRequest, waitFor: anyMoves})
  }
});


/*ctx.bthread("Fianchetto request", "Phase.Opening", function (phase) {
    bp.log.info("Starting Fianchetto development...")
    while (true) {
        sync({request: bp.Event("Fianchetto")})
    }
})


ctx.bthread("Fianchetto", "Phase.Opening", function (entity) {
    while (true) {

        let pawnMoves = []
        let pawnsArr = Array.from(ctx.runQuery("Piece.White.Pawn"))
        for (let i = 0; i < pawnsArr.length; i++) {
            pawnMoves = pawnMoves.concat(availableStraightCellsFromPawn(pawnsArr[i], 2).
            filter(m => ESFianchettoMoves.contains(m)))
        }
        sync({request: pawnMoves})
    }
});*/



/*
 Reasons for moves to be blocked:
 1. The move exposes the king ( causes a check )
 2. The desired move of the king is blocked due an opponent's piece "eyeing" the dst. cell
 3. Opponent's piece is "in the way"
 - Knight can't be blocked
 */


/* Those functions are responsible for finding unoccupied cells in a given distance.
* Those cells are potential destination cells of moves
*/

function availableStraightCellsFromPiece(piece, distance, direction) {
  let col = piece.cellId[0].charCodeAt(0) - 'a'.charCodeAt(0);
  let row = (piece.cellId[1] - '0');
  bp.log.info("Row -> " + row);
  bp.log.info("Col -> " + col);

  let availableCells = [];

  for (let i = 1; i <= distance; i++) {
    if (row + distance <= 7 && row + distance >= 0) {
      if (numericCellToCell(row + distance, col).pieceId == undefined) {
        availableCells.push({row: row + distance, col: col});
      }
    }
    if (row - distance <= 7 && row - distance >= 0) {
      if (numericCellToCell(row - distance, col).pieceId == undefined) {
        availableCells.push({row: row - distance, col: col});
      }
    }
    if (col + distance <= 7 && col + distance >= 0) {
      if (numericCellToCell(row, col + distance).pieceId == undefined) {
        availableCells.push({row: row, col: col + distance});
      }
    }
    if (col - distance <= 7 && col - distance >= 0) {
      if (numericCellToCell(row, col - distance).pieceId == undefined) {
        availableCells.push({row: row, col: col - distance});
      }
    }

    bp.log.info(availableCells)
  }
}

function availableStraightCellsFromPawn(pawn, distance) {
  let col = pawn.cellId[0].charCodeAt(0) - 'a'.charCodeAt(0);
  let row = (pawn.cellId[1] - '0');
  //bp.log.info ( "Row -> " + row);
  //bp.log.info ( "Col -> " + col);

  if (distance == 2 && row != 2) {
    return [];
  }

  let availableCells = [];
  let availableMoves = [];


  for (let i = 1; i <= distance; i++) {
    if (row + distance <= 7 && row + distance >= 0) {
      if (numericCellToCell(row + distance, col).pieceId == undefined) {
        availableCells.push({row: row + distance, col: col});
        availableMoves.push(moveEvent("Pawn", jToCol(col) + row, jToCol(col) + (row + distance)));
      }
    }
  }

  // bp.log.info ( availableCells )
  // bp.log.info ( availableMoves )
  return availableMoves
}

function jToCol(j) {
  let j_char = ''
  switch (j) {
    case 0:
      j_char = 'a';
      break;
    case 1:
      j_char = 'b';
      break;
    case 2:
      j_char = 'c';
      break;
    case 3:
      j_char = 'd';
      break;
    case 4:
      j_char = 'e';
      break;
    case 5:
      j_char = 'f';
      break;
    case 6:
      j_char = 'g';
      break;
    case 7:
      j_char = 'h';
      break;
  }
  return j_char;
}

function numericCellToCell(i, j) {
  let j_char = ''
  switch (j) {
    case 0:
      j_char = 'a';
      break;
    case 1:
      j_char = 'b';
      break;
    case 2:
      j_char = 'c';
      break;
    case 3:
      j_char = 'd';
      break;
    case 4:
      j_char = 'e';
      break;
    case 5:
      j_char = 'f';
      break;
    case 6:
      j_char = 'g';
      break;
    case 7:
      j_char = 'h';
      break;
  }
  // bp.log.info ( j_char + i )
  return Cell(j_char + i);

}

function availableDiagonalCellsFromPiece(piece, distance) {
  let col = piece.cellId[0] - 'a';
  let row = (piece.cellId[1] - '0');
  let availableCells = [];
  let color = piece.color; // use later for recognition of capturing opponet's pieces

  for (let i = 0; i <= distance; i++) {
    if (row + distance <= 7 && row + distance >= 0 && col + distance <= 7 && col + distance <= 0) {
      if (Cell(row + distance, col + distance).pieceId == undefined) {
        availableCells.push({row: row + distance, col: col + distance});
      }
    }
    if (row - distance <= 7 && row - distance >= 0 && col + distance <= 7 && col + distance <= 0) {
      if (Cell(row - distance, col + distance).pieceId == undefined) {
        availableCells.push({row: row - distance, col: col + distance});
      }
    }
    if (row + distance <= 7 && row + distance >= 0 && col - distance <= 7 && col - distance <= 0) {
      if (Cell(row + distance, col - distance).pieceId == undefined) {
        availableCells.push({row: row + distance, col: col - distance});
      }
    }
    if (row - distance <= 7 && row - distance >= 0 && col - distance <= 7 && col - distance <= 0) {
      if (Cell(row - distance, col - distance).pieceId == undefined) {
        availableCells.push({row: row - distance, col: col - distance});
      }
    }
  }
}

/*
    Strategies probabilities:
    1. Start : center - 0.55 , develop - 0.35, fianchetto - 0.1
    2. after 2 pawn develop moves, the probability descreases
    3. after 4 center capture moves, the probability decreases
    4. A pawn on d3, e3 raises the probability of Fianchetto

 */

