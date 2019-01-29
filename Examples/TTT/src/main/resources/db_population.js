bp.registerBThread("PopulateDB", function() {
    var cells = new ArrayList<Cell>(9);
    for(var i=0; i<3; i++){
        for(var j=0; j<3; j++){
            cells.add(new Cell(i,j));
        }
    }

    bp.sync({ request: CTX.InsertEvent([
            cells.get(0),cells.get(1)
        ]) });
});