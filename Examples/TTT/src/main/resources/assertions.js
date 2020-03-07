CTX.subscribe("assert twice", "Cell", function(c){
    bp.sync({waitFor: [ bp.Event("O",c), bp.Event("X",c) ]});
    bp.sync({waitFor: [ bp.Event("O",c), bp.Event("X",c) ]});
    bp.ASSERT(false,"cell marked twice");
});

bp.registerBThread("Assert X Win", function() {
    bp.sync({waitFor: bp.Event('XWin')});
    bp.ASSERT(false, "X won");
});