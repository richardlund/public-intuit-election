package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.Manifesto;
import intuit.election.domain.Rating;
import intuit.election.stub.StubbedCitizenToken;
import lombok.Value;
import lombok.experimental.Accessors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RatedIdeaServiceTest {
    private static final int SOME_VALID_RATING_VALUE = 7;
    private static final CitizenToken A_CONTENDER_TOKEN = new StubbedCitizenToken();
    private static final CitizenToken ANOTHER_CONTENDER_TOKEN = new StubbedCitizenToken();
    private static final Citizen A_CONTENDER_CITIZEN = Citizen.of(A_CONTENDER_TOKEN, "some contender name", "somecontender@email.com");
    private static final Citizen ANOTHER_CONTENDER_CITIZEN = Citizen.of(ANOTHER_CONTENDER_TOKEN, "some other contender name", "someothercontender@email.com");
    private static final Manifesto SOME_MANIFESTO = Manifesto.of(Idea.of("some idea"));
    private static final Contender CONTENDER_WITH_MANIFESTO = Contender.of(A_CONTENDER_CITIZEN, SOME_MANIFESTO);
    private static final CitizenToken IDEA_RATER_TOKEN = new StubbedCitizenToken();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private RatedIdeaService ratedIdeaService;

    @Before
    public void setup() {
        ratedIdeaService = new RatedIdeaService();
    }

    @Test
    public void ideasCanBePublishedByContenders() {
        Idea someIdea = Idea.of("some idea");
        ratedIdeaService.publishIdeaToBeRated(someIdea, CONTENDER_WITH_MANIFESTO);

        assertThat(ratedIdeaService.getIdeaPublisher(someIdea).get(), is(CONTENDER_WITH_MANIFESTO));
    }

    @Test
    public void cannotGetTheContenderForAnIdeaThatHasNotBeenPublished() {
        Idea someIdea = Idea.of("some idea");

        assertThat(ratedIdeaService.getIdeaPublisher(someIdea), is(Optional.empty()));
    }

    @Test
    public void citizenCanRateAnIdeaFromACandidateManifestoAndConfirmTheirRating() {
        Idea someIdea = Idea.of("some idea");
        ratedIdeaService.publishIdeaToBeRated(someIdea, CONTENDER_WITH_MANIFESTO);

        Rating citizensIdeaRating = Rating.of(SOME_VALID_RATING_VALUE);
        ratedIdeaService.rateIdea(IDEA_RATER_TOKEN, someIdea, citizensIdeaRating);

        assertThat(ratedIdeaService.getCitizensRatingFor(IDEA_RATER_TOKEN, someIdea), is(Optional.of(citizensIdeaRating)));
    }

    @Test
    public void citizenCannotRateAnIdeaThatHasNotBeenPublished() {
        Idea someIdea = Idea.of("some idea");
        Rating citizensIdeaRating = Rating.of(SOME_VALID_RATING_VALUE);

        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("This idea has not been published");

        ratedIdeaService.rateIdea(IDEA_RATER_TOKEN, someIdea, citizensIdeaRating);
    }

    @Test
    public void citizenCannotGetTheirRatingForAnIdeaThatHasNotBeenPublished() {
        Idea someIdea = Idea.of("some idea");

        assertThat(ratedIdeaService.getCitizensRatingFor(IDEA_RATER_TOKEN, someIdea), is(Optional.empty()));
    }

    @Test
    public void citizenCannotGetTheirRatingForAnIdeaTheyHaveNotRated() {
        Idea someIdea = Idea.of("some idea");
        ratedIdeaService.publishIdeaToBeRated(someIdea, CONTENDER_WITH_MANIFESTO);

        assertThat(ratedIdeaService.getCitizensRatingFor(IDEA_RATER_TOKEN, someIdea), is(Optional.empty()));
    }

    @Test
    public void citizenCanDeleteTheirRatingForAnIdea() {
        Idea someIdea = Idea.of("some idea");
        ratedIdeaService.publishIdeaToBeRated(someIdea, CONTENDER_WITH_MANIFESTO);
        Rating citizensIdeaRating = Rating.of(SOME_VALID_RATING_VALUE);
        ratedIdeaService.rateIdea(IDEA_RATER_TOKEN, someIdea, citizensIdeaRating);
        assertThat(ratedIdeaService.getCitizensRatingFor(IDEA_RATER_TOKEN, someIdea), is(Optional.of(citizensIdeaRating)));

        ratedIdeaService.deleteCitizensRatingFor(IDEA_RATER_TOKEN, someIdea);

        assertThat(ratedIdeaService.getCitizensRatingFor(IDEA_RATER_TOKEN, someIdea), is(Optional.empty()));
    }

    @Test
    public void contenderCannotRateTheirOwnIdeas() {
        Idea someIdea = Idea.of("some idea");
        ratedIdeaService.publishIdeaToBeRated(someIdea, CONTENDER_WITH_MANIFESTO);

        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Contenders cannot rate their own ideas");

        ratedIdeaService.rateIdea(CONTENDER_WITH_MANIFESTO.getCitizenToken(), someIdea, Rating.of(10));
    }

    @Test
    public void attemptingToDeleteAnUnpublishedIdeaDoesNotCauseAProblem() {
        Idea someIdea = Idea.of("some idea");
        ratedIdeaService.deleteCitizensRatingFor(IDEA_RATER_TOKEN, someIdea);

        assertThat(ratedIdeaService.getCitizensRatingFor(IDEA_RATER_TOKEN, someIdea), is(Optional.empty()));
    }

    @Test
    public void finalRatingOfAContenderIsTheSumOfTheAverageRatingForTheirIdeas() {
        SomeUniqueIdeas contenderIdeas = new SomeUniqueIdeas(3);
        Manifesto manifesto = Manifesto.of(contenderIdeas.ideas());
        Contender contender = Contender.of(A_CONTENDER_CITIZEN, manifesto);
        publishManifesto(contender);

        List<Integer> ratings = Arrays.asList(1, 2, 3);
        rateIdeas(IDEA_RATER_TOKEN, contenderIdeas.ideaList, ratings);

        double expectedFinalRating = ratings.stream().mapToDouble(Double::valueOf).sum();

        double actualFinalRating = ratedIdeaService.getFinalRatingFor(contender).get();

        assertThat(actualFinalRating, is(expectedFinalRating));
    }

    @Test
    public void thereisNoFinalRatingForAContenderWhenTheirIdeasAreAllUnrated() {
        Manifesto manifesto = Manifesto.of(new SomeUniqueIdeas(3).ideas());
        Contender contender = Contender.of(A_CONTENDER_CITIZEN, manifesto);

        assertThat(ratedIdeaService.getFinalRatingFor(contender), is(Optional.empty()));
    }

    @Test
    public void canRetrieveTheContenderWithTheHighestFinalRating() {
        SomeUniqueIdeas firstContendersIdeas = new SomeUniqueIdeas(3);
        SomeUniqueIdeas secondContendersIdeas = new SomeUniqueIdeas(3);

        Manifesto manifestoOfFirstContender = Manifesto.of(firstContendersIdeas.ideas());
        Manifesto manifestoOfSecondContender = Manifesto.of(secondContendersIdeas.ideas());
        Contender firstContender = Contender.of(A_CONTENDER_CITIZEN, manifestoOfFirstContender);
        Contender secondContender = Contender.of(ANOTHER_CONTENDER_CITIZEN, manifestoOfSecondContender);

        publishManifesto(firstContender);
        publishManifesto(secondContender);

        List<Integer> ratingsForFirstContender = Arrays.asList(1, 2, 3);
        List<Integer> ratingsForSecondContender = Arrays.asList(4, 5, 6);
        rateIdeas(IDEA_RATER_TOKEN, firstContendersIdeas.ideaList, ratingsForFirstContender);
        rateIdeas(IDEA_RATER_TOKEN, secondContendersIdeas.ideaList, ratingsForSecondContender);

        Contender expectedWinner = secondContender;

        Contender actualWinner = ratedIdeaService.getContenderWithHighestFinalRating().get();

        assertThat(actualWinner, is(expectedWinner));
    }

    @Test
    public void thereIsNoWinnerIfThereAreNoContenders() {
        assertThat(ratedIdeaService.getContenderWithHighestFinalRating(), is(Optional.empty()));
    }

    @Test
    public void thereIsNoWinnerIfAllContendersIdeasAreUnrated() {
        givenTwoContendersWithUnratedManifestos();

        assertThat(ratedIdeaService.getContenderWithHighestFinalRating(), is(Optional.empty()));
    }

    private void rateIdeas(CitizenToken raterToken, List<Idea> ideas, List<Integer>ratings) {
        IntStream.range(0, ideas.size()).forEach(index->{
            ratedIdeaService.rateIdea(raterToken, ideas.get(index), Rating.of(ratings.get(index)));
        });
    }

    private void publishManifesto(Contender contender) {
        contender.getManifesto().getIdeas().forEach(idea -> ratedIdeaService.publishIdeaToBeRated(idea, contender));
    }

    private void givenTwoContendersWithUnratedManifestos() {
        Manifesto manifestoOfFirstContender = Manifesto.of(new SomeUniqueIdeas(3).ideas());
        Manifesto manifestoOfSecondContender = Manifesto.of(new SomeUniqueIdeas(3).ideas());
        Contender firstContender = Contender.of(A_CONTENDER_CITIZEN, manifestoOfFirstContender);
        Contender secondContender = Contender.of(ANOTHER_CONTENDER_CITIZEN, manifestoOfSecondContender);

        publishManifesto(firstContender);
        publishManifesto(secondContender);
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