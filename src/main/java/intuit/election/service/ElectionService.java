package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.Manifesto;
import intuit.election.domain.Rating;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class ElectionService implements Election {
    private static final int MINIMUM_FOLLOWER_RATING = 6;
    private final CitizenRegistry citizenRegistry;
    private final ContenderService contenderService;
    private final RatedIdeaService ratedIdeaService;
    private final EmailService emailService;

    public ElectionService(CitizenRegistry citizenRegistry, ContenderService contenderService, RatedIdeaService ratedIdeaService, EmailService emailService) {
        this.citizenRegistry = citizenRegistry;
        this.contenderService = contenderService;
        this.ratedIdeaService = ratedIdeaService;
        this.emailService = emailService;
    }

    @Override
    public CitizenToken register(String citizenName, String citizenEmail) {
        return citizenRegistry.register(citizenName, citizenEmail);
    }

    @Override
    public Optional<Citizen> getRegisteredCitizen(CitizenToken citizenToken) {
        return citizenRegistry.get(citizenToken);
    }

    @Override
    public void nominateMyself(CitizenToken myCitizenToken) {
        contenderService.nominate(getRegisteredCitizen(myCitizenToken).get());
    }

    @Override
    public Optional<Contender> getMyContenderDetails(CitizenToken citizenToken) {
        return contenderService.getContender(citizenToken);
    }

    @Override
    public Collection<Contender> getContenders() {
        return contenderService.getContenders();
    }

    @Override
    public boolean isContender(Citizen citizen){
        return contenderService.isContender(citizen);
    }

    @Override
    public void postMyManifesto(CitizenToken citizenToken, Manifesto manifesto) {
        contenderService.postManifesto(citizenToken, manifesto);
        Contender myContenderDetails = contenderService.getContender(citizenToken).get();
        manifesto.getIdeas().forEach(idea->ratedIdeaService.publishIdeaToBeRated(idea, myContenderDetails));
    }

    @Override
    public void addIdeaToMyManifesto(CitizenToken citizenToken, Idea idea) {
        contenderService.addIdeaToManifesto(citizenToken, idea);
        Contender contender = contenderService.getContender(citizenToken).get();
        ratedIdeaService.publishIdeaToBeRated(idea, contender);
        Set<String> followerEmailAddresses = contenderService.getEmailAddressesOfFollowerChain(contender);
        if (!followerEmailAddresses.isEmpty()) {
            emailService.sendMessages(followerEmailAddresses, String.format("%s added new idea to manifesto: %s", contender.getName(), idea.getDescription()));
        }
    }

    @Override
    public void rateIdea(CitizenToken citizenTokenOfRater, Idea idea, Rating rating) {
        ratedIdeaService.rateIdea(citizenTokenOfRater, idea, rating);
        if (rating.value()>= MINIMUM_FOLLOWER_RATING) {
            Citizen rater = getRegisteredCitizen(citizenTokenOfRater).get();
            Contender contender = ratedIdeaService.getIdeaPublisher(idea).get();
            contenderService.startFollowing(rater, contender);
        }
    }

    @Override
    public Optional<Rating> getMyRatingFor(CitizenToken ideaRatingCitizen, Idea idea) {
        return ratedIdeaService.getCitizensRatingFor(ideaRatingCitizen, idea);
    }

    @Override
    public boolean iFollow(CitizenToken citizenToken, Contender contender) {
        return contenderService.isFollowerOf(getRegisteredCitizen(citizenToken).get(), contender);
    }

    @Override
    public void deleteRatingForIdea(CitizenToken ideaRatingCitizen, Idea ideaOfTheContender) {
        ratedIdeaService.deleteCitizensRatingFor(ideaRatingCitizen, ideaOfTheContender);
    }

    @Override
    public Optional<Contender> getContenderWithHighestFinalRating() {
        return ratedIdeaService.getContenderWithHighestFinalRating();
    }
}
