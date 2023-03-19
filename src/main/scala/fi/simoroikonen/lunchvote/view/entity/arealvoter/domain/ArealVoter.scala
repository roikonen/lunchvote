package fi.simoroikonen.lunchvote.view.entity.arealvoter.domain

import com.google.protobuf.empty.Empty
import fi.simoroikonen.lunchvote.view.entity.arealvoter.api
import kalix.scalasdk.valueentity.ValueEntity
import kalix.scalasdk.valueentity.ValueEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class ArealVoter(context: ValueEntityContext) extends AbstractArealVoter {
  override def emptyState: ArealVoterState = ArealVoterState.defaultInstance

  override def introduceArealVoter(
      currentState: ArealVoterState,
      cmd: api.IntroduceArealVoterCommand): ValueEntity.Effect[Empty] =
    effects
      .updateState(currentState.copy(organization = cmd.organization, username = cmd.username, areaId = cmd.areaId))
      .thenReply(Empty.defaultInstance)

  override def archiveArealVoter(
      currentState: ArealVoterState,
      cmd: api.ArchiveArealVoterCommand): ValueEntity.Effect[Empty] =
    effects.deleteEntity().thenReply(Empty.defaultInstance)

}
