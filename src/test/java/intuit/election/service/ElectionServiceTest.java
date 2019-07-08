package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.Manifesto;
import intuit.election.domain.Rating;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElectionServiceTest {
    private static final String SOME_CITIZEN_NAME = "someCitizenName";
    private static final String SOME_CITIZEN_EMAIL = "someCitizen@email.com";

    @Mock
    private CitizenRegistry mockCitizenRegistry;
    @Mock
    private ContenderService mockContenderService;
    @Mock
    private RatedIdeaService mockRatedIdeaService;
    @Mock
    private EmailService mockEmailService;

    private ElectionService electionService;

    @Before
    public void setup() {
        electionService = new ElectionService(mockCitizenRegistry, mockContenderService, mockRatedIdeaService, mockEmailService);
    }

    @Test
    public void delegateToCitizenRegistryToGenerateCitizenTokens() {
        CitizenToken expectedCitizenToken =  new StubbedCitizenToken();
        when(mockCitizenRegistry.register(SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL)).thenReturn(expectedCitizenToken);

        CitizenToken actualCitizenToken = electionService.register(SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL);

        assertThat(actualCitizenToken, is(expectedCitizenToken));
    }

    @Test
    public void delegateToCitizenRegistryToRetrieveRegisteredCitizenDetails() {
        CitizenToken someCitizenToken =  new StubbedCitizenToken();
        Citizen expectedCitizen = Citizen.of(someCitizenToken, SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL);
        when(mockCitizenRegistry.get(someCitizenToken)).thenReturn(Optional.of(expectedCitizen));

        Citizen actualCitizen = electionService.getRegisteredCitizen(someCitizenToken).get();

        assertThat(actualCitizen, is(expectedCitizen));
    }

    @Test
    public void delegateCitizenNominationToContenderService() {
        CitizenToken citizenToken = new StubbedCitizenToken();
        Citizen expectedCitizen = Citizen.of(citizenToken, SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL);
        when(mockCitizenRegistry.get(citizenToken)).thenReturn(Optional.of(expectedCitizen));

        electionService.nominateMyself(citizenToken);

        verify(mockContenderService).nominate(expectedCitizen);
    }

    @Test
    public void delegateContenderDetailsRetrievalToContenderService() {
        CitizenToken citizenToken = new StubbedCitizenToken();
        Citizen citizen = Citizen.of(citizenToken, SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL);
        Manifesto manifesto = Manifesto.of(Idea.of("someIdea"));
        Contender expectedContender = Contender.of(citizen, manifesto);
        when(mockContenderService.getContender(citizenToken)).thenReturn(Optional.of(expectedContender));

        Contender actualContender = electionService.getMyContenderDetails(citizenToken).get();

        assertThat(actualContender, is(expectedContender));
    }

    @Test
    public void delegateAllContendersRetrievalToContenderService() {
        CitizenToken citizenToken = new StubbedCitizenToken();
        Citizen citizen = Citizen.of(citizenToken, SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL);
        Manifesto manifesto = Manifesto.of(Idea.of("someIdea"));
        List<Contender> expectedContenders = Arrays.asList(Contender.of(citizen, manifesto));
        when(mockContenderService.getContenders()).thenReturn(expectedContenders);

        Collection<Contender> actualContenders = electionService.getContenders();

        assertThat(actualContenders, is(expectedContenders));
    }

    @Test
    public void delegateContenderCheckToContenderService() {
        CitizenToken citizenToken = new StubbedCitizenToken();
        Citizen citizen = Citizen.of(citizenToken, SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL);
        boolean expectedResponse = true;
        when(mockContenderService.isContender(citizen)).thenReturn(expectedResponse);

        boolean actualResponse = electionService.isContender(citizen);

        assertThat(actualResponse, is(expectedResponse));
    }

    @Test
    public void whenAManifestoIsPostedTheContenderServiceHandlesThePostingAndTheRatedIdeaServicePublishesTheIdeas() {
        CitizenToken citizenToken = new StubbedCitizenToken();
        Citizen citizen = Citizen.of(citizenToken, SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL);
        Idea idea = Idea.of("someIdea");
        Manifesto manifesto = Manifesto.of(idea);
        Contender contender = Contender.of(citizen, manifesto);
        when(mockContenderService.getContender(citizenToken)).thenReturn(Optional.of(contender));

        electionService.postMyManifesto(citizenToken, manifesto);

        verify(mockContenderService).postManifesto(citizenToken, manifesto);
        verify(mockRatedIdeaService).publishIdeaToBeRated(idea, contender);
    }

    @Test
    public void whenAnIdeaIsAddedToAManifestoForAContenderWithNoFollowersTheManifestoIsUpdatedAndTheIdeaIsPublished() {
        CitizenToken citizenToken = new StubbedCitizenToken();
        Contender contender = aContenderWithAManifesto(citizenToken);
        Idea newIdea = Idea.of("someNewIdea");
        when(mockContenderService.getEmailAddressesOfFollowerChain(contender)).thenReturn(Collections.EMPTY_SET);

        electionService.addIdeaToMyManifesto(citizenToken, newIdea);

        verify(mockContenderService).addIdeaToManifesto(citizenToken, newIdea);
        verify(mockRatedIdeaService).publishIdeaToBeRated(newIdea, contender);
        verifyZeroInteractions(mockEmailService);
    }

    @Test
    public void whenAnIdeaIsAddedToAManifestoForAContenderWithFollowersTheyAreEmailed() {
        CitizenToken citizenToken = new StubbedCitizenToken();
        Contender contender = aContenderWithAManifesto(citizenToken);
        Idea newIdea = Idea.of("someNewIdea");
        Set<String> expectedEmailAddresses = Collections.singleton("someEmailAddress");
        when(mockContenderService.getEmailAddressesOfFollowerChain(contender)).thenReturn(expectedEmailAddresses);

        electionService.addIdeaToMyManifesto(citizenToken, newIdea);

        verify(mockContenderService).addIdeaToManifesto(citizenToken, newIdea);
        verify(mockRatedIdeaService).publishIdeaToBeRated(newIdea, contender);
        verify(mockEmailService).sendMessages(expectedEmailAddresses, contender.getName()+ " added new idea to manifesto: "+newIdea.getDescription());
    }

    @Test
    public void citizensCanRateIdeas() {
        CitizenToken contenderToken = new StubbedCitizenToken();
        CitizenToken raterToken = new StubbedCitizenToken();
        Contender contender = aContenderWithAManifesto(contenderToken);
        Idea ideaToRate = contender.getManifesto().getIdeas().iterator().next();
        Rating rating = Rating.of(3);

        electionService.rateIdea(raterToken, ideaToRate, rating);

        verify(mockRatedIdeaService).rateAnIdea(raterToken, ideaToRate, rating);
    }

    @Test
    public void whenAnIdeaIsRatedMoreThan5ThenTheRaterBecomesAFollowerOfTheContender() {
        CitizenToken contenderToken = new StubbedCitizenToken();
        CitizenToken raterToken = new StubbedCitizenToken();
        Citizen rater = Citizen.of(raterToken, SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL);
        Contender contender = aContenderWithAManifesto(contenderToken);
        Idea ideaToRate = contender.getManifesto().getIdeas().iterator().next();
        Rating rating = Rating.of(7);
        when(mockCitizenRegistry.get(raterToken)).thenReturn(Optional.of(rater));
        when(mockRatedIdeaService.getIdeaPublisher(ideaToRate)).thenReturn(Optional.of(contender));

        electionService.rateIdea(raterToken, ideaToRate, rating);

        verify(mockRatedIdeaService).rateAnIdea(raterToken, ideaToRate, rating);
        verify(mockContenderService).startFollowing(rater, contender);
    }

    @Test
    public void ideaRatingRetrievalIsDelegatedToRatedIdeaService() {
        CitizenToken citizenToken = new StubbedCitizenToken();
        Idea someIdea = Idea.of("someIdea");
        Rating expectedRating = Rating.of(4);
        when(mockRatedIdeaService.getCitizensRatingFor(citizenToken, someIdea)).thenReturn(Optional.of(expectedRating));

        Rating actualRating = electionService.getMyRatingFor(citizenToken, someIdea).get();

        assertThat(actualRating, is(expectedRating));
    }

    @Test
    public void followerStatusCheckIsDelegatedToContenderService() {
        CitizenToken citizenToken = new StubbedCitizenToken();
        Citizen citizen = Citizen.of(citizenToken, SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL);
        CitizenToken contenderToken = new StubbedCitizenToken();
        Contender contender = aContenderWithAManifesto(contenderToken);
        boolean expectedResponse = true;
        when(mockCitizenRegistry.get(citizenToken)).thenReturn(Optional.of(citizen));
        when(mockContenderService.isFollowerOf(citizen, contender)).thenReturn(expectedResponse);

        boolean actualResponse = electionService.iFollow(citizenToken, contender);

        assertThat(actualResponse, is(expectedResponse));
    }

    Contender aContenderWithAManifesto(CitizenToken contenderToken) {
        Citizen citizen = Citizen.of(contenderToken, SOME_CITIZEN_NAME, SOME_CITIZEN_EMAIL);
        Manifesto manifesto = Manifesto.of(Idea.of("someIdea"));
        Contender contender = Contender.of(citizen, manifesto);
        when(mockContenderService.getContender(contenderToken)).thenReturn(Optional.of(contender));
        return contender;
    }

    private class StubbedCitizenToken implements CitizenToken{}
}