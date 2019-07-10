package intuit.election.service;

import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.RatedIdea;
import intuit.election.domain.Rating;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Package private as this service is not intended to be used outside this package
 */
class RatedIdeaService {
    private final Map<Idea, RatedIdea> ratedIdeas = new HashMap<>();
    private final Map<Contender, Double> finalRating = new HashMap<>();

    void publishIdeaToBeRated(Idea idea, Contender contender) {
        ratedIdeas.put(idea, RatedIdea.of(idea, contender));
    }

    void rateIdea(CitizenToken citizenTokenOfRater, Idea idea, Rating rating) {
        RatedIdea ratedIdea = ratedIdeas.get(idea);
        if (ratedIdea==null) {
            throw new UnsupportedOperationException("This idea has not been published");
        }
        if (citizenTokenOfRater.equals(ratedIdea.getContender().getCitizenToken())) {
            throw new UnsupportedOperationException("Contenders cannot rate their own ideas");
        }

        ratedIdea.addRating(citizenTokenOfRater, rating);

        Contender contender = ratedIdea.getContender();
        Collection<Idea> manifestoIdeas = contender.getManifesto().getIdeas();

        double currentFinalRating = manifestoIdeas.stream()
                                    .map(ratedIdeas::get)
                                    .map(RatedIdea::getAverageRating)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .mapToDouble(Double::doubleValue)
                                    .sum();

        finalRating.put(contender, currentFinalRating);
    }

    Optional<Contender> getIdeaPublisher(Idea idea) {
        RatedIdea ratedIdea = ratedIdeas.get(idea);
        if (ratedIdea!=null) {
            return Optional.of(ratedIdeas.get(idea).getContender());
        } else {
            return Optional.empty();
        }
    }

    Optional<Rating> getCitizensRatingFor(CitizenToken ideaRatingCitizen, Idea idea) {
        RatedIdea ratedIdea = ratedIdeas.get(idea);
        if (ratedIdea!=null) {
            return Optional.ofNullable(ratedIdea.getRatings().get(ideaRatingCitizen));
        } else {
            return Optional.empty();
        }
    }

    void deleteCitizensRatingFor(CitizenToken citizenTokenOfRater, Idea idea) {
        RatedIdea ratedIdea = ratedIdeas.get(idea);
        if (ratedIdea!=null) {
            ratedIdea.deleteRating(citizenTokenOfRater);
        }
    }

    public Optional<Double> getFinalRatingFor(Contender contender) {
        return Optional.ofNullable(finalRating.get(contender));
    }

    public Optional<Contender> getContenderWithHighestFinalRating() {
        Optional<Map.Entry<Contender, Double>> maxEntry =
                finalRating.entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue));
        if (maxEntry.isPresent()) {
            return Optional.of(maxEntry.get().getKey());
        } else {
            return Optional.empty();
        }
    }
}
