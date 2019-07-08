package intuit.election.domain;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RatingTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void ratingCanBeBetween0and10() {
        int valueBetween0And10 = 5;
        Rating rating = Rating.of(valueBetween0And10);
        assertThat(rating.value(), is(valueBetween0And10));
    }

    @Test
    public void ratingCannotBeLessThan0() {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Rating must be between 0 and 10");

        Rating.of(-1);
    }

    @Test
    public void ratingCannotBeMoreThan10() {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Rating must be between 0 and 10");

        Rating.of(11);
    }
}