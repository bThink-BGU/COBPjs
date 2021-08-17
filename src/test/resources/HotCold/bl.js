// Requirement $\ref{r_vacuum}$
ctx.bthread('no vacuuming at night', 'Night',
  function (entity) {
    sync({block: Event('vacuum')})
  })

// Requirement $\ref{r_cold}$
ctx.bthread('Add Cold Three Times', 'Room.WithTaps',
  function (entity) {
    while (true) {
      sync({waitFor: Event('press', entity)})
      sync({request: Event('cold', entity)})
      sync({request: Event('cold', entity)})
      sync({request: Event('cold', entity)})
    }
  })

// Requirement $\ref{r_hot}$
ctx.bthread('Add Hot Three Times', 'Room.WithTaps',
  function (entity) {
    while (true) {
      sync({waitFor: Event('press', entity)})
      sync({request: Event('hot', entity)})
      sync({request: Event('hot', entity)})
      sync({request: Event('hot', entity)})
    }
  })

// Requirement $\ref{kitchen}$
ctx.bthread('Interleave', 'Room.Kitchen',
  function (entity) {
    while (true) {
      sync({waitFor: Event('cold', entity), block: Event('hot', entity)})
      sync({waitFor: Event('hot', entity), block: Event('cold', entity)})
    }
  })

ctx.bthread('Simulate Press', 'Room.WithTaps',
  function (entity) {
    while (true) {
      sync({request: Event('press', entity)})
      for (let i = 0; i < 6; i++) {
        sync({waitFor: [Event('cold', entity), Event('hot', entity)]})
      }
    }
  })

bthread('Simulate day/night', function () {
  while (true) {
    sync({request: Event('time 21:00')})
    sync({request: Event('time 08:00')})
  }
})

bthread('Simulate vacuum', function () {
  while (true) {
    sync({request: Event('vacuum')})
  }
})

bthread('assert', function () {
  let myBP = bp
  while (true) {
    myBP.sync({waitFor: myBP.Event('time 21:00')})
    let name = myBP.sync({waitFor: [myBP.Event('time 08:00'), myBP.Event('vacuum')]}).name.equals('time 08:00')
    myBP.ASSERT(name, "vacuum at night")
  }
})
