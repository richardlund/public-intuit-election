package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Assumed that there will only ever be one instance of the citizen registry
 * so made this a basic singleton with eager initialisation
 */
public class CitizenRegistry {
    private static final CitizenRegistry INSTANCE = new CitizenRegistry();

    private final Map<CitizenToken, Citizen> registeredCitizens = new HashMap<>();

    private CitizenRegistry(){}

    public static CitizenRegistry getInstance() {
        return INSTANCE;
    }

    public CitizenToken register(String citizenName, String citizenEmail) {
        if (citizenAlreadyRegistered(citizenName, citizenEmail)) {
            throw new UnsupportedOperationException("Citizens can only register once");
        }

        CitizenToken citizenToken = new RegisteredCitizenElectionToken();
        registeredCitizens.put(citizenToken, Citizen.of(citizenToken, citizenName, citizenEmail));
        return citizenToken;
    }

    private boolean citizenAlreadyRegistered(String citizenName, String citizenEmail) {
        return registeredCitizens.values().stream()
                .anyMatch(citizen -> citizen.getName().equals(citizenName)
                          && citizen.getEmail().equals(citizenEmail));
    }

    public Optional<Citizen> get(CitizenToken citizenToken) {
        return Optional.ofNullable(registeredCitizens.get(citizenToken));
    }

    void reset() {
        registeredCitizens.clear();
    }

    @Value
    @Accessors(fluent = true)
    private class RegisteredCitizenElectionToken implements CitizenToken{
        private final UUID value = UUID.randomUUID();
        private RegisteredCitizenElectionToken() {}
    }
}
