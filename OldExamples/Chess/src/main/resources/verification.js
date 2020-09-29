CTX.subscribe("Simulate UCI", "GameStateInit", function (game) {
    bp.sync({ request: bp.Event("Color", Color.Black)});
    var parameters = new HashMap();
    parameters.put("Piece", new Piece(Color.Black, Piece.Types.Rook, 1));
    parameters.put("Row", 0);
    parameters.put("Col", 0);
    bp.sync({ request: bp.Event("AddPiece", Color.Black) });
    var myColor = bp.sync({waitFor: [bp.Event("Color", "Black"), bp.Event("Color", "White")]}).data;
    bp.sync({ request: CTX.UpdateEvent("ChangeMyColor", { "color": myColor , "game":game }) });
});
