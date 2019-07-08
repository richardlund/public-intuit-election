package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.Manifesto;
import intuit.election.domain.Rating;
import intuit.election.stub.StubbedEmailService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ElectionAcceptanceTest {

    private static final String SOME_CITIZEN_NAME = "some citizen name";
    private static final String SOME_EMAIL_ADDRESS = "citizen@email.com";
    private static final int SOME_VALID_RATING_VALUE_ABOVE_5 = 8;

    private Election election;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() {
        CitizenRegistry citizenRegistry = new CitizenRegistry();
        ContenderService contenderService = new ContenderService();
        RatedIdeaService ratedIdeaService = new RatedIdeaService();
        EmailService emailService= new StubbedEmailService();

        election = new ElectionService(citizenRegistry, contenderService, ratedIdeaService, emailService);
    }

    @Test
    public void citizensMustRegisterToReceiveATokenAllowingInteractionWithElectionFeatures() {
        CitizenToken token = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
        Citizen registeredCitizen = election.getRegisteredCitizen(token).get();

        assertThat(registeredCitizen.getName(), is(equalTo(SOME_CITIZEN_NAME)));
        assertThat(registeredCitizen.getEmail(), is(equalTo(SOME_EMAIL_ADDRESS)));
        assertThat(registeredCitizen.getCitizenToken(), is(equalTo(token)));
    }
//
//    @Test
//    public void citizenCanNominateThemselvesForElectionAndBecomeAContender() {
//        Election election = new ElectionService(new ContenderService(stubbedEmailService));
//        CitizenToken token = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
//        Citizen me = election.getRegisteredCitizen(token);
//
//        assertThat(election.isContender(me), is(false));
//        election.nominateMyself(token);
//        assertThat(election.isContender(me), is(true));
//        Contender myContenderDetails = election.getMyContenderDetails(token);
//        assertThat(myContenderDetails.getCitizen(), is(me));
//    }
//
//    @Test
//    public void citizenHasNoContenderDetailsIfTheyHaveNotNominatedThemselvesForElection() {
//        Election election = new ElectionService(new ContenderService(stubbedEmailService));
//        CitizenToken token = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
//
//        assertThat(election.getMyContenderDetails(token), is(nullValue()));
//    }
//
//    @Test
//    public void contenderCanOnlyPostAManifestoOnce() {
//        Election election = new ElectionService(new ContenderService(stubbedEmailService));
//        CitizenToken token = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
//        Manifesto myManifesto = Manifesto.of(Idea.of("some idea"));
//
//        exceptionRule.expect(UnsupportedOperationException.class);
//        exceptionRule.expectMessage("Can only post a manifesto once");
//
//        election.nominateMyself(token);
//        election.postMyManifesto(token, myManifesto);
//        election.postMyManifesto(token, myManifesto);
//    }
//
//    @Test
//    public void citizenCannotPostAManifestoIfTheyAreNotAContender() {
//        Election election = new ElectionService(new ContenderService(stubbedEmailService));
//        CitizenToken token = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
//        Manifesto myManifesto = Manifesto.of(Idea.of("some idea"));
//
//        exceptionRule.expect(UnsupportedOperationException.class);
//        exceptionRule.expectMessage("You are not a nominated contender");
//
//        election.postMyManifesto(token, myManifesto);
//    }
//
//    @Test
//    public void anyoneCanGetTheListOfContenders() {
//        Election election = new ElectionService(new ContenderService(stubbedEmailService));
//        CitizenToken someCitizenToken = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
//        CitizenToken someOtherCitizenToken = election.register("some other citizen name", "some other email address");
//
//        election.nominateMyself(someCitizenToken);
//        election.nominateMyself(someOtherCitizenToken);
//        Contender someCitizenContenderDetails = election.getMyContenderDetails(someCitizenToken);
//        Contender someOtherCitizenContenderDetails = election.getMyContenderDetails(someOtherCitizenToken);
//
//        assertThat(election.getContenders(), containsInAnyOrder(someCitizenContenderDetails, someOtherCitizenContenderDetails));
//    }

    @Test
    public void citizenBecomesAFollowerOfACandidateIfTheyRateAnIdeaAbove5() {
        Election election = givenAnElectionWithAContenderWithAManifestOfOneIdea();
        CitizenToken ideaRatingCitizen = election.register("idea rating citizen", SOME_EMAIL_ADDRESS);
        Contender theContender = election.getContenders().iterator().next();
        Idea ideaOfTheContender = theContender.getManifesto().getIdeas().iterator().next();

        assertThat(election.iFollow(ideaRatingCitizen, theContender), is(false));
        Rating citizensIdeaRating = Rating.of(SOME_VALID_RATING_VALUE_ABOVE_5);
        election.rateIdea(ideaRatingCitizen, ideaOfTheContender, citizensIdeaRating);

        assertThat(election.iFollow(ideaRatingCitizen, theContender), is(true));
    }

    private Election givenAnElectionWithAContenderWithAManifestOfOneIdea() {
        CitizenToken token = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
        Manifesto myManifesto = Manifesto.of(Idea.of("some idea"));

        election.nominateMyself(token);
        election.postMyManifesto(token, myManifesto);

        return election;
    }
}