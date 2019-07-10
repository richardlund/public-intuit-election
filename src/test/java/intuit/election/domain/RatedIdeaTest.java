package intuit.election.domain;

import intuit.election.stub.StubbedCitizenToken;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;

public class RatedIdeaTest {

    private static final Idea SOME_IDEA = Idea.of("Some idea");
    private static final Contender SOME_CONTENDER = Contender.of(Citizen.of(new StubbedCitizenToken(), "some citizen name", "some email address"), Manifesto.of(SOME_IDEA));
    private static final int SOME_VALID_RATING_VALUE = 5;
    private static final int SOME_OTHER_VALID_RATING_VALUE = 4;

    @Test
    public void initialIdeaWillHaveNoRatingsAndNoAverageRating() {
        RatedIdea ratedIdea = RatedIdea.of(SOME_IDEA, SOME_CONTENDER);

        assertThat(ratedIdea.getRatings(), is(anEmptyMap()));
        assertThat(ratedIdea.getAverageRating(), is(Optional.empty()));
    }

    @Test
    public void citizensCanAddTheirRatingToAnIdea() {
        RatedIdea ratedIdea = RatedIdea.of(SOME_IDEA, SOME_CONTENDER);
        CitizenToken citizenTokenOfRater = new StubbedCitizenToken();
        Rating citizensRating = Rating.of(SOME_VALID_RATING_VALUE);
        ratedIdea.addRating(citizenTokenOfRater, citizensRating);

        assertThat(ratedIdea.getRatings().get(citizenTokenOfRater), is(citizensRating));
    }

    @Test
    public void citizensCanDeleteTheirRatingFromAnIdea() {
        RatedIdea ratedIdea = RatedIdea.of(SOME_IDEA, SOME_CONTENDER);
        CitizenToken citizenTokenOfRater = new StubbedCitizenToken();
        Rating citizensRating = Rating.of(SOME_VALID_RATING_VALUE);
        ratedIdea.addRating(citizenTokenOfRater, citizensRating);

        ratedIdea.deleteRating(citizenTokenOfRater);

        assertThat(ratedIdea.getRatings().containsKey(citizenTokenOfRater), is(false));
    }

    @Test
    public void averageRatingForAnIdeaCanBeRetrieved() {
        RatedIdea ratedIdea = RatedIdea.of(SOME_IDEA, SOME_CONTENDER);
        CitizenToken citizenTokenOfRater = new StubbedCitizenToken();
        Rating citizensRating = Rating.of(SOME_VALID_RATING_VALUE);
        ratedIdea.addRating(citizenTokenOfRater, citizensRating);

        CitizenToken citizenTokenOfAnotherRater = new StubbedCitizenToken();
        Rating anotherCitizensRating = Rating.of(SOME_OTHER_VALID_RATING_VALUE);
        ratedIdea.addRating(citizenTokenOfAnotherRater, anotherCitizensRating);

        int totalNumberOfRatings = 2;
        double expectedAverageRating = (double)(SOME_VALID_RATING_VALUE + SOME_OTHER_VALID_RATING_VALUE) / totalNumberOfRatings;

        assertThat(ratedIdea.getAverageRating(), is(Optional.of(expectedAverageRating)));
    }


    @Test(expected = UnsupportedOperationException.class)
    public void cannotModifyTheRatingsOfTheIdea() {
        RatedIdea ratedIdea = RatedIdea.of(SOME_IDEA, SOME_CONTENDER);
        CitizenToken citizenTokenOfRater = new StubbedCitizenToken();

        ratedIdea.getRatings().put(citizenTokenOfRater, Rating.of(SOME_VALID_RATING_VALUE));
    }
}