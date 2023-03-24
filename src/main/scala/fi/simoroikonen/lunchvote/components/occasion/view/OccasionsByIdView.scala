package fi.simoroikonen.lunchvote.components.occasion.view

import fi.simoroikonen.lunchvote.components.occasion.domain.OccasionPublished
import fi.simoroikonen.lunchvote.components.occasion.domain.OccasionState
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class OccasionsByIdView(context: ViewContext) extends AbstractOccasionsByIdView {

  override def emptyState: OccasionState = OccasionState.defaultInstance

  override def processOccasionPublished(
      currentState: OccasionState,
      occasionPublished: OccasionPublished
  ): UpdateEffect[OccasionState] =
    effects.updateState(
      currentState.copy(
        id = occasionPublished.id,
        organization = occasionPublished.organization,
        areaId = occasionPublished.areaId,
        startDatetimeIso8610Utc = occasionPublished.startDatetimeIso8610Utc,
        publisherUsername = occasionPublished.publisherUsername
      )
    )

}
