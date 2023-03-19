package fi.simoroikonen.lunchvote.view.entity.arealvoter.action

import com.google.protobuf.empty.Empty
import fi.simoroikonen.lunchvote.components.voter.domain.VoterArchived
import fi.simoroikonen.lunchvote.components.voter.domain.VoterIntroduced
import fi.simoroikonen.lunchvote.view.entity.arealvoter.api.{ ArchiveArealVoterCommand, IntroduceArealVoterCommand }
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

import scala.concurrent.Future

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class ArealVoterActionImpl(creationContext: ActionCreationContext) extends AbstractArealVoterAction {

  override def processVoterIntroduced(evt: VoterIntroduced): Action.Effect[Empty] = {
    val cmds = evt.areaIds.map(areaId =>
      IntroduceArealVoterCommand(organization = evt.organization, username = evt.username, areaId = areaId))
    val resultF = Future
      .sequence(cmds.map(cmd => components.arealVoter.introduceArealVoter(cmd).execute()))
      .map(_ => Empty.defaultInstance)
    effects.asyncReply(resultF)
  }

  override def processVoterArchived(evt: VoterArchived): Action.Effect[Empty] = {
    val cmds = evt.areaIds.map(areaId =>
      ArchiveArealVoterCommand(organization = evt.organization, username = evt.username, areaId = areaId))
    val resultF = Future
      .sequence(cmds.map(cmd => components.arealVoter.archiveArealVoter(cmd).execute()))
      .map(_ => Empty.defaultInstance)
    effects.asyncReply(resultF)
  }
}
