package intuit.election.domain;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class Rating {
    private final int value;

    private Rating(int value){
        if (value < 0 || value > 10) {
            throw new UnsupportedOperationException("Rating must be between 0 and 10");
        }

        this.value=value;
    }

    public static Rating of(int value) {
        return new Rating(value);
    }
}
