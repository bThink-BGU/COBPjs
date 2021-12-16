function behaviorSet0(input, index) {
  bp.registerBThread("O_Player_Thread_0_0", function(){
    bp.sync({waitFor:[O(input[0]), X(input[2]), X(input[1])]});
    bp.sync({waitFor:[O(input[3]), X(input[4]), X(input[5])]});
    bp.sync({request:[O(input[6])]},2);
  });
}
var inputs_0 = [
  //    0             1               2           3             4             5              6
  [{x: 2, y: 2}, {x: 0, y: 1}, /*____1___*/, {x: 0, y: 2}, {x: 0, y: 1}, {x: 1, y: 2}, {x: 0, y: 1}],
  [{x: 1, y: 1}, {x: 0, y: 1}, {x: 2, y: 1}, {x: 0, y: 1}, {x: 0, y: 2}, /*____4___*/, {x: 2, y: 0}],
  [{x: 2, y: 2}, {x: 1, y: 0}, /*____1___*/, {x: 1, y: 2}, {x: 1, y: 1}, {x: 2, y: 0}, {x: 2, y: 1}],
  [{x: 1, y: 2}, {x: 2, y: 1}, {x: 1, y: 1}, {x: 2, y: 0}, {x: 2, y: 2}, {x: 2, y: 1}, {x: 0, y: 0}],
  [{x: 1, y: 0}, {x: 2, y: 1}, {x: 0, y: 2}, {x: 0, y: 0}, {x: 2, y: 0}, /*____4___*/, {x: 0, y: 2}],
  [{x: 1, y: 2}, {x: 2, y: 2}, {x: 1, y: 1}, {x: 1, y: 1}, {x: 0, y: 0}, {x: 0, y: 2}, {x: 2, y: 1}],
  [{x: 0, y: 2}, {x: 1, y: 2}, {x: 0, y: 0}, {x: 1, y: 2}, {x: 1, y: 0}, {x: 0, y: 0}, {x: 2, y: 2}],
  [{x: 2, y: 2}, /*____0___*/, {x: 0, y: 1}, {x: 1, y: 2}, {x: 2, y: 0}, {x: 1, y: 0}, {x: 0, y: 1}],
  [{x: 0, y: 0}, {x: 1, y: 1}, {x: 0, y: 2}, {x: 0, y: 2}, /*____3___*/, {x: 2, y: 2}, {x: 2, y: 2}]]
inputs_0.forEach(function (input, index) {
  behaviorSet0(input, index);
});//2->1,3->2,1->3

function behaviorSet1(input, index) {
  bp.registerBThread("O_Player_Thread_1_0", function(){
    bp.sync({waitFor:[X(input[0])]});
    bp.sync({request:[O(input[1])]},9);
  });
}
var inputs_1 = [
  [{x: 1, y: 0}, {x: 0, y: 0}],
  [{x: 0, y: 1}, {x: 2, y: 2}],
  [{x: 2, y: 2}, {x: 0, y: 2}],
  [{x: 1, y: 2}, {x: 1, y: 0}],
  [{x: 0, y: 2}, {x: 2, y: 0}],
  [{x: 2, y: 2}, {x: 2, y: 0}],
  [{x: 2, y: 2}, {x: 1, y: 1}],
  [{x: 2, y: 1}, {x: 0, y: 1}],
  [{x: 0, y: 2}, {x: 2, y: 2}]]
inputs_1.forEach(function (input, index) {
  behaviorSet1(input, index);
});

function behaviorSet2(input, index) {
  bp.registerBThread("O_Player_Thread_2_1", function(){
    bp.sync({request:[O(input[0].x, input[0].y)]},7);
  });

}
var inputs_2 = [
  [{x: 1, y: 0}],
  [{x: 0, y: 0}],
  [{x: 1, y: 1}],
  [{x: 2, y: 0}],
  [{x: 1, y: 2}],
  [{x: 0, y: 2}]]
inputs_2.forEach(function (input, index) {
  behaviorSet2(input, index);
});

function behaviorSet3(input, index) {
  bp.registerBThread("O_Player_Thread_3_0", function(){
    bp.sync({request:[O(input[1])]},10);
  });

  bp.registerBThread("O_Player_Thread_3_1", function(){
    bp.sync({waitFor:[X(input[2])]});
    bp.sync({waitFor:[O(input[3])]});
    bp.sync({request:[O(input[0])]},2);
  });

  bp.registerBThread("O_Player_Thread_3_2", function(){
    bp.sync({waitFor:[X(input[1]), O(input[1]), X(input[2])]});
    bp.sync({request:[O(input[1]), O(input[2])]},9);
  });

}
var inputs_3 = [[{x: 1, y: 0}, {x: 2, y: 2}, {x: 0, y: 2}, {x: 0, y: 1}]]
inputs_3.forEach(function (input, index) {
  behaviorSet3(input, index);
});