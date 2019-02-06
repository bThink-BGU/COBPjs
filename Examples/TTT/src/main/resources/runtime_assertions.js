CTX.subscribe("assert twice", "Cell", function(c){
    bp.sync({waitFor: [ createEvent("O",c), createEvent("X",c) ]});
    bp.sync({waitFor: [ createEvent("O",c), createEvent("X",c) ]});
    bp.ASSERT(false,"cell marked twice");
});