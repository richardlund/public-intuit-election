package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CitizenRegistry {
    private final Map<CitizenToken, Citizen> registeredCitizens = new HashMap<>();

    public CitizenToken register(String citizenName, String citizenEmail) {
        CitizenToken citizenToken = new RegisteredCitizenElectionToken();
        registeredCitizens.put(citizenToken, Citizen.of(citizenToken, citizenName, citizenEmail));
        return citizenToken;
    }

    public Optional<Citizen> get(CitizenToken citizenToken) {
        return Optional.ofNullable(registeredCitizens.get(citizenToken));
    }

    @Value
    @Accessors(fluent = true)
    private class RegisteredCitizenElectionToken implements CitizenToken{
        private final UUID value = UUID.randomUUID();
        private RegisteredCitizenElectionToken() {}
    }
}
