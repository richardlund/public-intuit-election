package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.Manifesto;
import intuit.election.domain.Rating;
import intuit.election.stub.StubbedEmailService;
import lombok.Value;
import lombok.experimental.Accessors;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ElectionAcceptanceTest {

    private static final String SOME_CITIZEN_NAME = "some citizen name";
    private static final String SOME_EMAIL_ADDRESS = "citizen@email.com";
    private static final int SOME_VALID_RATING_VALUE_ABOVE_5 = 8;
    private static final int SOME_VALID_RATING_VALUE = 3;

    private Election election;
    private CitizenRegistry citizenRegistry = CitizenRegistry.getInstance();
    private ContenderService contenderService = new ContenderService();
    private RatedIdeaService ratedIdeaService = new RatedIdeaService();
    private EmailService emailService= new StubbedEmailService();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() {
        election = new ElectionService(citizenRegistry, contenderService, ratedIdeaService, emailService);
    }

    @After
    public void teardown() {
        citizenRegistry.reset();
        ((StubbedEmailService)emailService).reset();
    }

    @Test
    public void citizensMustRegisterToReceiveATokenAllowingInteractionWithElectionFeatures() {
        CitizenToken token = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
        Citizen registeredCitizen = election.getRegisteredCitizen(token).get();

        assertThat(registeredCitizen.getName(), is(equalTo(SOME_CITIZEN_NAME)));
        assertThat(registeredCitizen.getEmail(), is(equalTo(SOME_EMAIL_ADDRESS)));
        assertThat(registeredCitizen.getCitizenToken(), is(equalTo(token)));
    }

    @Test
    public void citizenCanNominateThemselvesForElectionAndBecomeAContender() {
        CitizenToken token = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
        Citizen me = election.getRegisteredCitizen(token).get();

        election.nominateMyself(token);
        Contender myContenderDetails = election.getMyContenderDetails(token).get();

        assertThat(election.isContender(me), is(true));
        assertThat(myContenderDetails.getCitizen(), is(me));
    }

    @Test
    public void contenderCanPostAManifesto() {
        CitizenToken token = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
        Manifesto myManifesto = Manifesto.of(Idea.of("some idea"));

        election.nominateMyself(token);
        election.postMyManifesto(token, myManifesto);
        Contender myContenderDetails = election.getMyContenderDetails(token).get();

        assertThat(myContenderDetails.getManifesto(), is(myManifesto));
    }

    @Test
    public void anyoneCanGetTheListOfContendersAndTheirManifestos() {
        CitizenToken someCitizenToken = election.register(SOME_CITIZEN_NAME, SOME_EMAIL_ADDRESS);
        CitizenToken someOtherCitizenToken = election.register("some other citizen name", "some other email address");
        Manifesto someManifesto = Manifesto.of(Idea.of("some idea"));
        Manifesto someOtherManifesto = Manifesto.of(Idea.of("some other idea"));

        election.nominateMyself(someCitizenToken);
        election.nominateMyself(someOtherCitizenToken);
        election.postMyManifesto(someCitizenToken, someManifesto);
        election.postMyManifesto(someOtherCitizenToken, someOtherManifesto);

        Contender someCitizenContenderDetails = election.getMyContenderDetails(someCitizenToken).get();
        Contender someOtherCitizenContenderDetails = election.getMyContenderDetails(someOtherCitizenToken).get();

        Collection<Contender> actualContenders = election.getContenders();
        List<Manifesto> actualManifestos = actualContenders.stream().map(contender -> contender.getManifesto()).collect(Collectors.toList());

        assertThat(actualContenders, containsInAnyOrder(someCitizenContenderDetails, someOtherCitizenContenderDetails));
        assertThat(actualManifestos, containsInAnyOrder(someManifesto, someOtherManifesto));
    }

    @Test
    public void citizenCanRateAnIdea() {
        Election election = givenAnElectionWithAContenderWithAManifestOfOneIdea();
        CitizenToken ideaRatingCitizen = election.register("idea rating citizen", SOME_EMAIL_ADDRESS);
        Contender theContender = election.getContenders().iterator().next();
        Idea ideaOfTheContender = theContender.getManifesto().getIdeas().iterator().next();
        Rating citizensIdeaRating = Rating.of(SOME_VALID_RATING_VALUE);

        election.rateIdea(ideaRatingCitizen, ideaOfTheContender, citizensIdeaRating);
        Rating actualRating = election.getMyRatingFor(ideaRatingCitizen, ideaOfTheContender).get();

        assertThat(actualRating, is(citizensIdeaRating));
    }

    @Test
    public void citizenCanDeleteTheirIdeaRating() {
        Election election = givenAnElectionWithAContenderWithAManifestOfOneIdea();
        CitizenToken ideaRatingCitizen = election.register("idea rating citizen", SOME_EMAIL_ADDRESS);
        Contender theContender = election.getContenders().iterator().next();
        Idea ideaOfTheContender = theContender.getManifesto().getIdeas().iterator().next();
        Rating citizensIdeaRating = Rating.of(SOME_VALID_RATING_VALUE);
        election.rateIdea(ideaRatingCitizen, ideaOfTheContender, citizensIdeaRating);

        election.deleteRatingForIdea(ideaRatingCitizen, ideaOfTheContender);

        assertThat(election.getMyRatingFor(ideaRatingCitizen, ideaOfTheContender), is(Optional.empty()));
    }

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

    @Test
    public void followerWillReceiveAnEmailWhenAContenderPostsANewIdea() {
        Election election = givenAnElectionWithAContenderWithAManifestOfOneIdea();
        Contender contender = election.getContenders().iterator().next();
        String followerEmail = "follower@email.com";
        CitizenToken followerCitizen = election.register("follower citizen", followerEmail);
        givenCitizenHasBecomeAFollowerOfTheContender(election, contender, followerCitizen);
        Idea someNewIdea = Idea.of("some new idea");

        election.addIdeaToMyManifesto(contender.getCitizenToken(), someNewIdea);

        assertThat("Email should be sent", emailService.messageSent(followerEmail, contender.getName()+" added new idea to manifesto: "+someNewIdea.getDescription()), is(true));
    }

    @Test
    public void contenderWithMaximumSumOfAvgRatingsPerIdeaIsTheWinner() {
        CitizenToken firstContenderToken = election.register("First election contender", "firstcontender@email");
        CitizenToken secondContenderToken = election.register("Second election contender", "secondcontender@email");
        CitizenToken ideaRatingCitizen = election.register("idea rating citizen", SOME_EMAIL_ADDRESS);

        SomeUniqueIdeas firstContendersIdeas = new SomeUniqueIdeas(3);
        SomeUniqueIdeas secondContendersIdeas = new SomeUniqueIdeas(3);

        Manifesto manifestoOfFirstContender = Manifesto.of(firstContendersIdeas.ideas());
        Manifesto manifestoOfSecondContender = Manifesto.of(secondContendersIdeas.ideas());

        election.nominateMyself(firstContenderToken);
        election.postMyManifesto(firstContenderToken, manifestoOfFirstContender);
        election.nominateMyself(secondContenderToken);
        election.postMyManifesto(secondContenderToken, manifestoOfSecondContender);

        List<Integer> ratingsForFirstContender = Arrays.asList(1, 2, 3);
        List<Integer> ratingsForSecondContender = Arrays.asList(4, 5, 6);

        rateIdeas(ideaRatingCitizen, firstContendersIdeas.ideaList, ratingsForFirstContender);
        rateIdeas(ideaRatingCitizen, secondContendersIdeas.ideaList, ratingsForSecondContender);

        Contender expectedWinner = election.getMyContenderDetails(secondContenderToken).get();

        Contender actualWinner = election.getContenderWithHighestFinalRating().get();

        assertThat(actualWinner, is(expectedWinner));
    }

    private void givenCitizenHasBecomeAFollowerOfTheContender(Election election, Contender contender, CitizenToken followerCitizen) {
        Idea ideaOfTheContender = contender.getManifesto().getIdeas().iterator().next();
        Rating citizensIdeaRating = Rating.of(SOME_VALID_RATING_VALUE_ABOVE_5);
        election.rateIdea(followerCitizen, ideaOfTheContender, citizensIdeaRating);
    }

    private Election givenAnElectionWithAContenderWithAManifestOfOneIdea() {
        CitizenToken token = election.register("An election contender", SOME_EMAIL_ADDRESS);
        Manifesto myManifesto = Manifesto.of(Idea.of("some idea"));

        election.nominateMyself(token);
        election.postMyManifesto(token, myManifesto);

        return election;
    }

    private void rateIdeas(CitizenToken raterToken, List<Idea> ideas, List<Integer>ratings) {
        IntStream.range(0, ideas.size()).forEach(index->{
            election.rateIdea(raterToken, ideas.get(index), Rating.of(ratings.get(index)));
        });
    }

    @Value
    @Accessors(fluent = true)
    private class SomeUniqueIdeas {
        private final List<Idea> ideaList;

        private SomeUniqueIdeas(int numberOfIdeas) {
            ideaList=new ArrayList<>(numberOfIdeas);
            IntStream.rangeClosed(1, numberOfIdeas).mapToObj(ideaNumber->"Idea"+ideaNumber).forEach(ideaDescription->ideaList.add(Idea.of(ideaDescription)));
        }

        private Idea[] ideas() {
            return ideaList.stream().toArray(Idea[]::new);
        }
    }
}