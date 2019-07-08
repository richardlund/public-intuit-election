package intuit.election.domain;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class Contender {
    @NonNull
    private final Citizen citizen;
    private final Manifesto manifesto;

    public CitizenToken getCitizenToken() {
        return citizen.getCitizenToken();
    }
    public String getName() {return citizen.getName();}
}
