package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.Manifesto;
import intuit.election.domain.Rating;

import java.util.Collection;
import java.util.Optional;

public interface Election {
    Collection<Contender> getContenders();

    CitizenToken register(String citizenName, String citizenEmail);

    Optional<Citizen> getRegisteredCitizen(CitizenToken citizenToken);

    boolean isContender(Citizen citizen);

    void nominateMyself(CitizenToken citizenToken);

    Optional<Contender> getMyContenderDetails(CitizenToken citizenToken);

    void postMyManifesto(CitizenToken citizenToken, Manifesto manifesto);

    void addIdeaToMyManifesto(CitizenToken citizenToken, Idea idea);

    void rateIdea(CitizenToken citizenTokenOfRater, Idea idea, Rating rating);

    Optional<Rating> getMyRatingFor(CitizenToken ideaRatingCitizen, Idea idea);

    boolean iFollow(CitizenToken citizenToken, Contender contender);

    void deleteRatingForIdea(CitizenToken ideaRatingCitizen, Idea ideaOfTheContender);

    Optional<Contender> getContenderWithHighestFinalRating();
}
