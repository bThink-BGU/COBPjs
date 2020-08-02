var staticMoves = bp.EventSet("Static Moves",function (e) {
    return moves.contains(e) && (e.data.source.equals(e.data.target) || e.data.source.piece == null);
});

/*CTX.subscribe("assert twice", "Cell", function(c){
    bp.sync({waitFor: [ createEvent("O",c), createEvent("X",c) ]});
    bp.sync({waitFor: [ createEvent("O",c), createEvent("X",c) ]});
    bp.ASSERT(false,"cell marked twice");
});*/

// Requirement : Move is allowed only if source has piece on it. - not in wikipedia
bp.registerBThread("Check static moves", function() {
    bp.sync({waitFor:donePopulationEvent});
    var e = bp.sync({waitFor: staticMoves});
    bp.ASSERT(false, "A static move occurred. Event is " + e);
});