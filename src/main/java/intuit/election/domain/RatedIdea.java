package intuit.election.domain;

import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Value(staticConstructor = "of")
public class RatedIdea {
    private final Idea idea;
    private final Contender contender;
    private final Map<CitizenToken, Rating> ratings = new HashMap<>();

    public Map<CitizenToken, Rating> getRatings() {
        return Collections.unmodifiableMap(ratings);
    }

    public void addRating(CitizenToken ideaRatingCitizen, Rating rating) {
        ratings.put(ideaRatingCitizen, rating);
    }

    public void deleteRating(CitizenToken ideaRatingCitizen) {
        ratings.remove(ideaRatingCitizen);
    }

    public Optional<Double> getAverageRating() {
        return (ratings.isEmpty()) ? Optional.empty() : Optional.of(ratings.values().stream().mapToDouble(Rating::value).average().getAsDouble());
    }
}
