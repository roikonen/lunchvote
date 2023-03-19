package fi.simoroikonen.lunchvote.components.area.view

import fi.simoroikonen.lunchvote.components.area.domain.AreaIntroduced
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class AreaIdsView(context: ViewContext) extends AbstractAreaIdsView {

  override def emptyState: Area = Area.defaultInstance

  override def processAreaIntroduced(state: Area, event: AreaIntroduced): UpdateEffect[Area] =
    effects.updateState(state.copy(organization = event.organization, id = event.id))

}
