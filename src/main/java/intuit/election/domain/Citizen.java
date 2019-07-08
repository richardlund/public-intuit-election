package intuit.election.domain;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class Citizen {
    @NonNull
    private final CitizenToken citizenToken;
    @NonNull
    private final String name;
    @NonNull
    private final String email;
}
