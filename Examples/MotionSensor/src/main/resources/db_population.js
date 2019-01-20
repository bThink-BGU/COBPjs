bp.registerBThread("PopulateDB", function() {
    var achiya = new Worker(000000001, "Achiya Elyasaf");
    var gera = new Worker(000000002, "Gera Weiss");
    var arnon = new Worker(000000003, "Arnon Sturm");
    var office96_224 = new Office("96/224", achiya);
    var office96_225 = new Office("96/225", arnon);
    var office37_123 = new Office("37/123", gera);

    // bp.sync({ request:CTX.InsertEvent(achiya) });

    bp.sync({ request: CTX.PopulateEvent([achiya,gera,arnon,office37_123,office96_224,office96_225]) });
    // bp.sync({request:bp.Event("end_of_population")});
});