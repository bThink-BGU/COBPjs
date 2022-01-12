ctx.bthread("simulate x", "Cell.All", function (cell) {
  sync({request: Event("X", cell)})
})