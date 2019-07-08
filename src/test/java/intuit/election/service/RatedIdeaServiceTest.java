package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.Manifesto;
import intuit.election.domain.Rating;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RatedIdeaServiceTest {
    private static final int SOME_VALID_RATING_VALUE = 7;
    private static final CitizenToken A_CONTENDER_TOKEN = Mockito.mock(CitizenToken.class);
    private static final Citizen A_CONTENDER_CITIZEN = Citizen.of(A_CONTENDER_TOKEN, "some contender name", "somecontender@email.com");
    private static final Manifesto SOME_MANIFESTO = Manifesto.of(Idea.of("some idea"));
    private static final Contender CONTENDER_WITH_MANIFESTO = Contender.of(A_CONTENDER_CITIZEN, SOME_MANIFESTO);
    private static final CitizenToken IDEA_RATER_TOKEN = Mockito.mock(CitizenToken.class);
    private static final Citizen IDEA_RATING_CITIZEN = Citizen.of(IDEA_RATER_TOKEN, "first follower name", "firstfollower@email.com");

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
        ratedIdeaService.rateAnIdea(IDEA_RATER_TOKEN, someIdea, citizensIdeaRating);

        assertThat(ratedIdeaService.getCitizensRatingFor(IDEA_RATER_TOKEN, someIdea), is(Optional.of(citizensIdeaRating)));
    }

    @Test
    public void citizenCannotRateAnIdeaThatHasNotBeenPublished() {
        Idea someIdea = Idea.of("some idea");
        Rating citizensIdeaRating = Rating.of(SOME_VALID_RATING_VALUE);

        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("This idea has not been published");

        ratedIdeaService.rateAnIdea(IDEA_RATER_TOKEN, someIdea, citizensIdeaRating);
    }

    @Test
    public void citizenCannotGetTheirRatingForAnIdeaTheyHaveNotRated() {
        Idea someIdea = Idea.of("some idea");
        ratedIdeaService.publishIdeaToBeRated(someIdea, CONTENDER_WITH_MANIFESTO);

        assertThat(ratedIdeaService.getCitizensRatingFor(IDEA_RATER_TOKEN, someIdea), is(Optional.empty()));
    }

}