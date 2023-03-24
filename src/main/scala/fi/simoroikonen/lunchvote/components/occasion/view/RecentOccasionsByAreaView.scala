package fi.simoroikonen.lunchvote.components.occasion.view

import com.google.protobuf.timestamp.Timestamp
import fi.simoroikonen.lunchvote.components.occasion.api.Place
import fi.simoroikonen.lunchvote.components.occasion.domain.OccasionPublished
import fi.simoroikonen.lunchvote.components.occasion.domain.VotePlacedForOccasion
import fi.simoroikonen.lunchvote.components.occasion.view.Occasion
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

import java.time.Instant

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class RecentOccasionsByAreaView(context: ViewContext)
    extends AbstractRecentOccasionsByAreaView {

  override def emptyState: Occasion = Occasion.defaultInstance

  override def processOccasionPublished(
      currentState: Occasion,
      event: OccasionPublished
  ): UpdateEffect[Occasion] =
    effects.updateState(
      currentState.copy(
        id = event.id,
        organization = event.organization,
        areaId = event.areaId,
        startDatetimeIso8610Utc = event.startDatetimeIso8610Utc,
        publisherUsername = event.publisherUsername,
        startDatetime =
          Option(Timestamp(Instant.parse(event.startDatetimeIso8610Utc)))
      )
    )

  override def processVotePlacedForOccasion(
      currentState: Occasion,
      event: VotePlacedForOccasion
  ): UpdateEffect[Occasion] = {
    val votedPlace = event.placeId
    val currentVotedPlaces = currentState.votedPlaces
    val currentNumberOfVotes = currentVotedPlaces
      .find(_.placeId.equals(votedPlace))
      .map(_.votes)
      .getOrElse(0)
    val newVotedPlaces = currentVotedPlaces.filterNot(
      _.placeId.equals(votedPlace)
    ) :+ Place(placeId = votedPlace, votes = currentNumberOfVotes + 1)
    effects.updateState(currentState.copy(votedPlaces = newVotedPlaces))
  }

}
