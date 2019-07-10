package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class CitizenRegistryTest {

    private CitizenRegistry citizenRegistry = CitizenRegistry.getInstance();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @After
    public void teardown() {
        citizenRegistry.reset();
    }

    @Test
    public void citizensReceivesAUniqueTokenWhenTheyRegisterWithNameAndEmail() {
        CitizenToken citizenToken = citizenRegistry.register("someName", "someEmail@email");
        assertThat(citizenToken, is(not(nullValue())));
    }

    @Test
    public void citizensCanRetrieveTheirCitizenDetailsUsingTheirUniqueToken() {
        String expectedCitizenName = "someOtherName";
        String expectedCitizenEmail = "someOtherEmail@email";
        CitizenToken citizenToken = citizenRegistry.register(expectedCitizenName, expectedCitizenEmail);

        Citizen actualCitizen = citizenRegistry.get(citizenToken).get();

        assertThat(actualCitizen.getName(), is(expectedCitizenName));
        assertThat(actualCitizen.getEmail(), is(expectedCitizenEmail));
    }

    @Test
    public void citizenDetailsAreNotReturnedIfAnUnrecognisedTokenIsUsed() {
        assertThat(citizenRegistry.get(new UnrecognisedToken()), is(Optional.empty()));
    }

    @Test
    public void citizenCannotRegisterMoreThanOnce() {
        String citizenName = "yetAnotherCitizen";
        String citizenEmail = "yetAnotherEmail";

        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Citizens can only register once");

        citizenRegistry.register(citizenName, citizenEmail);
        citizenRegistry.register(citizenName, citizenEmail);
    }

    private class UnrecognisedToken implements CitizenToken{};
}