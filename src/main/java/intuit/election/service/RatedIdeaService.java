package intuit.election.service;

import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.RatedIdea;
import intuit.election.domain.Rating;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class RatedIdeaService {
    private final Map<Idea, RatedIdea> ratedIdeas = new HashMap<>();

    void publishIdeaToBeRated(Idea idea, Contender contender) {
        ratedIdeas.put(idea, RatedIdea.of(idea, contender));
    }

    void rateAnIdea(CitizenToken citizenTokenOfRater, Idea idea, Rating rating) {
        RatedIdea ratedIdea = ratedIdeas.get(idea);
        if (ratedIdea==null) {
            throw new UnsupportedOperationException("This idea has not been published");
        }
        ratedIdea.addRating(citizenTokenOfRater, rating);
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
        return Optional.ofNullable(ratedIdeas.get(idea).getRatings().get(ideaRatingCitizen));
    }
}
