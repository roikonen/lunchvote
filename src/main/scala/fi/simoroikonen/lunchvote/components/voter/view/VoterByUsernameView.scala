package fi.simoroikonen.lunchvote.components.voter.view

import fi.simoroikonen.lunchvote.components.voter.domain.VoterArchived
import fi.simoroikonen.lunchvote.components.voter.domain.VoterIntroduced
import fi.simoroikonen.lunchvote.components.voter.domain.VoterState
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class VoterByUsernameView(context: ViewContext) extends AbstractVoterByUsernameView {

  override def emptyState: VoterState = VoterState.defaultInstance

  override def processVoterIntroduced(
      currentState: VoterState,
      event: VoterIntroduced
  ): UpdateEffect[VoterState] =
    effects.updateState(
      currentState
        .copy(
          organization = event.organization,
          username = event.username,
          areaIds = event.areaIds,
          archived = false
        )
    )

  override def processVoterArchived(
      currentState: VoterState,
      event: VoterArchived
  ): UpdateEffect[VoterState] =
    effects.updateState(currentState.copy(archived = true))

}
