// Requirement $\ref{r_vacuum}$
bthread('night activation/deactivation',
  function () {
    sync({waitFor: Event('time', '21:00')})
    sync({request: Event('night begins')})
    sync({waitFor: Event('time', '08:00')})
    sync({request: Event('night ends')})
  })

// Requirement $\ref{r_vacuum}$
bthread('no vacuuming at night', 'Night',
  function (entity) {
    sync({block: Event('vacuum')})
  })

// Requirement $\ref{r_cold}$
bthread('Add Cold Three Times', 'Room.WithTaps',
  function (entity) {
    sync({waitFor: Event('press', entity)})
    sync({request: Event('cold', entity)})
    sync({request: Event('cold', entity)})
    sync({request: Event('cold', entity)})
  })

// Requirement $\ref{r_hot}$
bthread('Add Hot Three Times', 'Room.WithTaps',
  function (entity) {
    sync({waitFor: Event('press', entity)})
    sync({request: Event('hot', entity)})
    sync({request: Event('hot', entity)})
    sync({request: Event('hot', entity)})
  })

// Requirement $\ref{kitchen}$
bthread('Interleave', 'Room.Kitchen',
  function (entity) {
    while (true) {
      sync({waitFor: Event('cold', entity), block: Event('hot', entity)})
      sync({waitFor: Event('hot', entity), block: Event('cold', entity)})
    }
  })