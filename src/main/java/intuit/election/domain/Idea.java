package intuit.election.domain;

import lombok.Value;

@Value(staticConstructor = "of")
public class Idea {
    private final String description;
}
