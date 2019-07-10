package intuit.election.service;

import java.util.Set;

public interface EmailService {
    void sendMessage(String emailAddress, String message);
    void sendMessages(Set<String> emailAddresses, String message);
    boolean messageSent(String emailAddress, String message);
}
