// Requirement $\ref{r_vacuum}$
/*ctx.bthread('no vacuuming at night', 'Night',
  function (entity) {
    sync({block: Event('vacuum')})
  })*/

// Requirement $\ref{r_cold}$
ctx.bthread('Add Cold Three Times', 'Room.WithTaps',
  function (entity) {
    while (true) {
      sync({waitFor: Event('press', entity.id)})
      sync({request: Event('cold', entity.id)})
      sync({request: Event('cold', entity.id)})
      sync({request: Event('cold', entity.id)})
    }
  })

// Requirement $\ref{r_hot}$
ctx.bthread('Add Hot Three Times', 'Room.WithTaps',
  function (entity) {
    while (true) {
      sync({waitFor: Event('press', entity.id)})
      sync({request: Event('hot', entity.id)})
      sync({request: Event('hot', entity.id)})
      sync({request: Event('hot', entity.id)})
    }
  })

// Requirement $\ref{kitchen}$
ctx.bthread('Interleave', 'Room.Kitchen',
  function (entity) {
    while (true) {
      sync({waitFor: Event('cold', entity.id), block: Event('hot', entity.id)})
      sync({waitFor: Event('hot', entity.id), block: Event('cold', entity.id)})
    }
  })

ctx.bthread('Simulate Press', 'Room.WithTaps',
  function (entity) {
    while (true) {
      sync({request: Event('press', entity.id)})
      for (let i = 0; i < 6; i++) {
        sync({waitFor: [Event('cold', entity.id), Event('hot', entity.id)]})
      }
    }
  })

bthread('Simulate day/night', function () {
  while (true) {
    sync({request: Event('time 21:00')})
    sync({request: Event('time 08:00')})
  }
})

/*bthread('Simulate vacuum', function () {
  while (true) {
    sync({request: Event('vacuum')})
  }
})*/
