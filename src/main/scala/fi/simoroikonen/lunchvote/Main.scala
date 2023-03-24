package fi.simoroikonen.lunchvote

import fi.simoroikonen.lunchvote.components.area.action.AreaActionsImpl
import fi.simoroikonen.lunchvote.components.area.domain.Area
import fi.simoroikonen.lunchvote.components.area.view.AreaIdsView
import fi.simoroikonen.lunchvote.components.area.view.AreasByIdView
import fi.simoroikonen.lunchvote.components.occasion.domain.Occasion
import fi.simoroikonen.lunchvote.components.occasion.view.OccasionsByIdView
import fi.simoroikonen.lunchvote.components.occasion.view.RecentOccasionsByAreaView
import fi.simoroikonen.lunchvote.components.voter.domain.Voter
import fi.simoroikonen.lunchvote.components.voter.view.VoterByUsernameView
import fi.simoroikonen.lunchvote.controller.AdminControllerActionImpl
import fi.simoroikonen.lunchvote.controller.VoterControllerActionImpl
import fi.simoroikonen.lunchvote.view.AreasWithVotersJoinViewImpl
import fi.simoroikonen.lunchvote.view.entity.arealvoter.action.ArealVoterActionImpl
import fi.simoroikonen.lunchvote.view.entity.arealvoter.domain.ArealVoter
import kalix.scalasdk.Kalix
import org.slf4j.LoggerFactory

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

object Main {

  private val log = LoggerFactory.getLogger("fi.simoroikonen.lunchvote.Main")

  def createKalix(): Kalix = {
    // The KalixFactory automatically registers any generated Actions, Views or Entities,
    // and is kept up-to-date with any changes in your protobuf definitions.
    // If you prefer, you may remove this and manually register these components in a
    // `Kalix()` instance.
    KalixFactory.withComponents(
      new Area(_),
      new ArealVoter(_),
      new Occasion(_),
      new Voter(_),
      new AdminControllerActionImpl(_),
      new AreaActionsImpl(_),
      new AreaIdsView(_),
      new ArealVoterActionImpl(_),
      new AreasByIdView(_),
      new AreasWithVotersJoinViewImpl(_),
      new OccasionsByIdView(_),
      new RecentOccasionsByAreaView(_),
      new VoterByUsernameView(_),
      new VoterControllerActionImpl(_)
    )
  }

  def main(args: Array[String]): Unit = {
    log.info("starting the Kalix service")
    createKalix().start()
  }
}
