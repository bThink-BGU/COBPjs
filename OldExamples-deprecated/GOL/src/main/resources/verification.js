CTX.subscribe("Simulate Click", "Cell", function(c){
    bp.sync({request: createEvent("Click",c)}, -20);
});