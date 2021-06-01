// Requirement $\ref{r_vacuum}$
bthread('night activation/deactivation',
  function () {
    while (true) {
      sync({waitFor: Event('time', '21:00')})
      sync({request: Event('night begins'), block:Event('night begins').negate()})
      sync({waitFor: Event('time', '08:00')})
      sync({request: Event('night ends'), block:Event('night ends').negate()})
    }
  })

// Requirement $\ref{r_vacuum}$
ctx.bthread('no vacuuming at night', 'Night',
  function (entity) {
    sync({block: Event('vacuum')})
  })

// Requirement $\ref{r_cold}$
ctx.bthread('Add Cold Three Times', 'Room.WithTaps',
  function (entity) {
    sync({waitFor: Event('press', entity)})
    sync({request: Event('cold', entity)})
    sync({request: Event('cold', entity)})
    sync({request: Event('cold', entity)})
  })

// Requirement $\ref{r_hot}$
ctx.bthread('Add Hot Three Times', 'Room.WithTaps',
  function (entity) {
    sync({waitFor: Event('press', entity)})
    sync({request: Event('hot', entity)})
    sync({request: Event('hot', entity)})
    sync({request: Event('hot', entity)})
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
    for (let j = 0; j < 3; j++) {
      sync({request:Event('press', entity)})
      for (let i = 0; i < 6; i++) {
        sync({waitFor: [Event('cold', entity), Event('hot', entity)]})
      }
    }
})

bthread('Simulate day/night', function () {
  for (let j = 0; j < 3; j++) {
    sync({request:Event('time', '21:00')})
    sync({request:Event('time', '08:00')})
  }
})

bthread('Simulate vacuum', function () {
  for (let j = 0; j < 3; j++) {
    sync({request:Event('vacuum')})
    sync({request:Event('vacuum end')})
  }
})