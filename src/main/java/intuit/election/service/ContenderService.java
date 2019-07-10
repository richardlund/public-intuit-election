package intuit.election.service;

import intuit.election.domain.Citizen;
import intuit.election.domain.CitizenToken;
import intuit.election.domain.Contender;
import intuit.election.domain.Idea;
import intuit.election.domain.Manifesto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Package private as this service is not intended to be used outside this package
 */
class ContenderService {

    private static final String NOT_A_CONTENDER_MSG = "Citizen is not a nominated contender";

    private final Map<CitizenToken, Contender> contenders = new HashMap<>();
    //Linked list chosen as insertions are expected to be more prevalent than random access
    private final Map<CitizenToken, LinkedList<Citizen>> contenderFollowers = new HashMap<>();

    void nominate(Citizen citizen) {
        Contender myContenderDetails = Contender.of(citizen, null);
        contenders.put(citizen.getCitizenToken(), myContenderDetails);
        contenderFollowers.put(myContenderDetails.getCitizenToken(), new LinkedList<>());
    }

    Optional<Contender> getContender(CitizenToken citizenToken) {
        return Optional.ofNullable(contenders.get(citizenToken));
    }

    Collection<Contender> getContenders() {
        return Collections.unmodifiableCollection(contenders.values());
    }

    boolean isContender(Citizen citizen){
        return contenders.containsKey(citizen.getCitizenToken());
    }

    void postManifesto(CitizenToken citizenToken, Manifesto manifesto) {
        Optional<Contender> myContenderDetails = getContender(citizenToken);
        myContenderDetails.orElseThrow(()->new UnsupportedOperationException(NOT_A_CONTENDER_MSG));

        if (myContenderDetails.get().getManifesto()!=null) {
            throw new UnsupportedOperationException("Contender can only post a manifesto once");
        }
        contenders.put(citizenToken, Contender.of(myContenderDetails.get().getCitizen(), manifesto));
    }

    void addIdeaToManifesto(CitizenToken citizenToken, Idea idea) {
        Optional<Contender> myContenderDetails = getContender(citizenToken);
        myContenderDetails.orElseThrow(()->new UnsupportedOperationException(NOT_A_CONTENDER_MSG));

        if (myContenderDetails.get().getManifesto()==null) {
            throw new UnsupportedOperationException("Contender has not posted a manifesto yet");
        } else {
            contenders.get(citizenToken).getManifesto().add(idea);
        }
    }

    void startFollowing(Citizen citizen, Contender contender) {
        contenderFollowers.get(contender.getCitizenToken()).add(citizen);
    }

    boolean isFollowerOf(Citizen citizen, Contender contender) {
        return contenderFollowers.get(contender.getCitizenToken()).contains(citizen);
    }

    Set<String> getEmailAddressesOfFollowerChain(Contender contender) {
        LinkedList<Citizen> followers = contenderFollowers.get(contender.getCitizenToken());
        return followers.stream().map(Citizen::getEmail).collect(Collectors.toSet());
    }
}
