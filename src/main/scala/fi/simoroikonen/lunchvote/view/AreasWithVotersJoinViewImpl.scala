package fi.simoroikonen.lunchvote.view

import fi.simoroikonen.lunchvote.components.area.domain.{
  AreaIntroduced,
  AreaState,
  Place,
  PlaceArchived,
  PlaceIntroduced
}
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class AreasWithVotersJoinViewImpl(context: ViewContext) extends AbstractAreasWithVotersJoinView {

  object AreasViewTable extends AbstractAreasViewTable {

    override def emptyState: AreaState = AreaState.defaultInstance

    override def processAreaIntroduced(
        state: AreaState,
        event: AreaIntroduced
    ): UpdateEffect[AreaState] =
      effects.updateState(
        state.copy(organization = event.organization, id = event.id)
      )

    override def processPlaceIntroduced(
        state: AreaState,
        event: PlaceIntroduced
    ): UpdateEffect[AreaState] =
      effects.updateState(
        state.copy(places = state.places :+ Place(event.id, event.introducerUsername))
      )

    override def processPlaceArchived(
        state: AreaState,
        event: PlaceArchived
    ): UpdateEffect[AreaState] =
      effects.updateState(
        state.copy(places = state.places.filterNot(_.id.equals(event.id)))
      )

  }

}
