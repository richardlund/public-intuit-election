package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.Manifesto;
import intuit.election.stub.StubbedCitizenToken;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(MockitoJUnitRunner.class)
public class ContenderServiceTest {

    private static final CitizenToken SOME_NON_CONTENDER_CITIZEN_TOKEN = new StubbedCitizenToken();
    private static final CitizenToken A_CONTENDER_TOKEN = new StubbedCitizenToken();
    private static final CitizenToken ANOTHER_CONTENDER_TOKEN = new StubbedCitizenToken();
    private static final CitizenToken YET_ANOTHER_CONTENDER_TOKEN = new StubbedCitizenToken();
    private static final Citizen A_CONTENDER_CITIZEN = Citizen.of(A_CONTENDER_TOKEN, "some contender name", "somecontender@email.com");
    private static final Citizen ANOTHER_CONTENDER_CITIZEN = Citizen.of(ANOTHER_CONTENDER_TOKEN, "some other contender name", "someothercontender@email.com");
    private static final Citizen YET_ANOTHER_CONTENDER_CITIZEN = Citizen.of(YET_ANOTHER_CONTENDER_TOKEN, "yet another contender name", "yetanothercontender@email.com");
    private static final Manifesto SOME_MANIFESTO = Manifesto.of(Idea.of("some idea"));
    private static final Contender CONTENDER_WITH_MANIFESTO = Contender.of(A_CONTENDER_CITIZEN, SOME_MANIFESTO);
    private static final CitizenToken FIRST_FOLLOWER_TOKEN = new StubbedCitizenToken();
    private static final CitizenToken SECOND_FOLLOWER_TOKEN = new StubbedCitizenToken();
    private static final CitizenToken THIRD_FOLLOWER_TOKEN = new StubbedCitizenToken();
    private static final Citizen FIRST_FOLLOWER_CITIZEN = Citizen.of(FIRST_FOLLOWER_TOKEN, "first follower name", "firstfollower@email.com");
    private static final Citizen SECOND_FOLLOWER_CITIZEN = Citizen.of(SECOND_FOLLOWER_TOKEN, "second follower name", "secondfollower@email.com");
    private static final Citizen THIRD_FOLLOWER_CITIZEN = Citizen.of(THIRD_FOLLOWER_TOKEN, "third follower name", "thirdfollower@email.com");

    private ContenderService contenderService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() {
        contenderService = new ContenderService();
    }


    @Test
    public void citizenHasNoContenderDetailsIfTheyAreNotNominated() {
        assertThat(contenderService.getContender(A_CONTENDER_TOKEN), is(Optional.empty()));
    }

    @Test
    public void citizenCanBeNominated() {
        assertThat(contenderService.isContender(CONTENDER_WITH_MANIFESTO.getCitizen()), is(false));
        contenderService.nominate(CONTENDER_WITH_MANIFESTO.getCitizen());
        assertThat(contenderService.isContender(CONTENDER_WITH_MANIFESTO.getCitizen()), is(true));
    }

    @Test
    public void canGetTheListOfContenders() {
        contenderService.nominate(A_CONTENDER_CITIZEN);
        contenderService.nominate(ANOTHER_CONTENDER_CITIZEN);
        contenderService.nominate(YET_ANOTHER_CONTENDER_CITIZEN);

        Collection<Contender> contenders = contenderService.getContenders();
        assertThat(contenders.stream().map(Contender::getCitizen).collect(Collectors.toList()), containsInAnyOrder(A_CONTENDER_CITIZEN, ANOTHER_CONTENDER_CITIZEN, YET_ANOTHER_CONTENDER_CITIZEN));
    }

    @Test
    public void contenderListCannotBeUpdatedByReference() {
        contenderService.nominate(A_CONTENDER_CITIZEN);
        Contender someCitizenContenderDetails = contenderService.getContender(A_CONTENDER_CITIZEN.getCitizenToken()).get();

        exceptionRule.expect(UnsupportedOperationException.class);
        contenderService.getContenders().remove(someCitizenContenderDetails);
    }

    @Test
    public void contenderCanPostAManifestoIfTheyHaveNotDoneSoAlready() {
        Manifesto manifesto = Manifesto.of(Idea.of("some idea"));

        contenderService.nominate(CONTENDER_WITH_MANIFESTO.getCitizen());
        contenderService.postManifesto(CONTENDER_WITH_MANIFESTO.getCitizenToken(), manifesto);

        Contender myContenderDetails = contenderService.getContender(CONTENDER_WITH_MANIFESTO.getCitizenToken()).get();
        assertThat(myContenderDetails.getCitizen(), is(CONTENDER_WITH_MANIFESTO.getCitizen()));
        assertThat(myContenderDetails.getManifesto(), is(manifesto));
    }

    @Test
    public void contenderCanOnlyPostAManifestoOnce() {
        Manifesto manifesto = Manifesto.of(Idea.of("some idea"));
        contenderService.nominate(CONTENDER_WITH_MANIFESTO.getCitizen());

        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Contender can only post a manifesto once");

        contenderService.postManifesto(CONTENDER_WITH_MANIFESTO.getCitizenToken(), manifesto);
        contenderService.postManifesto(CONTENDER_WITH_MANIFESTO.getCitizenToken(), manifesto);
    }

    @Test
    public void citizenCannotPostAManifestoIfTheyAreNotAContender() {
        Manifesto manifesto = Manifesto.of(Idea.of("some idea"));

        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Citizen is not a nominated contender");

        contenderService.postManifesto(CONTENDER_WITH_MANIFESTO.getCitizenToken(), manifesto);
    }

    @Test
    public void contenderCanAddAnIdeaToTheirManifesto() {
        Manifesto manifesto = Manifesto.of(Idea.of("some idea"));
        contenderService.nominate(CONTENDER_WITH_MANIFESTO.getCitizen());
        contenderService.postManifesto(CONTENDER_WITH_MANIFESTO.getCitizenToken(), manifesto);

        Idea someNewIdea = Idea.of("some new idea");
        assertThat(contenderService.getContender(CONTENDER_WITH_MANIFESTO.getCitizenToken()).get().getManifesto().getIdeas(), not(hasItem(someNewIdea)));
        contenderService.addIdeaToManifesto(CONTENDER_WITH_MANIFESTO.getCitizenToken(), someNewIdea);
        assertThat(contenderService.getContender(CONTENDER_WITH_MANIFESTO.getCitizenToken()).get().getManifesto().getIdeas(), hasItem(someNewIdea));
    }

    @Test
    public void contenderCannotAddAnIdeaIfTheyHaveNotPostedAManifesto() {
        contenderService.nominate(CONTENDER_WITH_MANIFESTO.getCitizen());

        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Contender has not posted a manifesto yet");

        Idea someNewIdea = Idea.of("some new idea");
        contenderService.addIdeaToManifesto(CONTENDER_WITH_MANIFESTO.getCitizenToken(), someNewIdea);
    }

    @Test
    public void citizenCannotAddAnIdeaIfTheyAreNotAContender() {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Citizen is not a nominated contender");

        Idea someNewIdea = Idea.of("some new idea");
        contenderService.addIdeaToManifesto(SOME_NON_CONTENDER_CITIZEN_TOKEN, someNewIdea);
    }

    @Test
    public void citizenCanStartFollowingAContender() {
        contenderService.nominate(CONTENDER_WITH_MANIFESTO.getCitizen());

        assertThat(contenderService.isFollowerOf(FIRST_FOLLOWER_CITIZEN, CONTENDER_WITH_MANIFESTO), is(false));
        contenderService.startFollowing(FIRST_FOLLOWER_CITIZEN, CONTENDER_WITH_MANIFESTO);
        assertThat(contenderService.isFollowerOf(FIRST_FOLLOWER_CITIZEN, CONTENDER_WITH_MANIFESTO), is(true));
    }

    @Test
    public void willReturnNoEmailAddressesIfContenderHasNoFollowers() {
        contenderService.nominate(CONTENDER_WITH_MANIFESTO.getCitizen());
        contenderService.getEmailAddressesOfFollowerChain(CONTENDER_WITH_MANIFESTO);

        assertThat(contenderService.getEmailAddressesOfFollowerChain(CONTENDER_WITH_MANIFESTO), is(empty()));
    }

    @Test
    public void willGetTheEmailAddressOfASingleFollowerOfAContender() {
        contenderService.nominate(CONTENDER_WITH_MANIFESTO.getCitizen());
        contenderService.startFollowing(FIRST_FOLLOWER_CITIZEN, CONTENDER_WITH_MANIFESTO);

        Set<String> emailAddressesOfFollowerChain = contenderService.getEmailAddressesOfFollowerChain(CONTENDER_WITH_MANIFESTO);

        assertThat(emailAddressesOfFollowerChain, containsInAnyOrder(FIRST_FOLLOWER_CITIZEN.getEmail()));
    }

    @Test
    public void willGetTheEmailAddressOfMultipleFollowersOfAContender() {
        contenderService.nominate(CONTENDER_WITH_MANIFESTO.getCitizen());
        contenderService.startFollowing(FIRST_FOLLOWER_CITIZEN, CONTENDER_WITH_MANIFESTO);
        contenderService.startFollowing(SECOND_FOLLOWER_CITIZEN, CONTENDER_WITH_MANIFESTO);
        contenderService.startFollowing(THIRD_FOLLOWER_CITIZEN, CONTENDER_WITH_MANIFESTO);

        Set<String> emailAddressesOfFollowerChain = contenderService.getEmailAddressesOfFollowerChain(CONTENDER_WITH_MANIFESTO);

        assertThat(emailAddressesOfFollowerChain, containsInAnyOrder(FIRST_FOLLOWER_CITIZEN.getEmail(), SECOND_FOLLOWER_CITIZEN.getEmail(), THIRD_FOLLOWER_CITIZEN.getEmail()));
    }
}