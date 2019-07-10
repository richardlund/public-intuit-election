package intuit.election.stub;

import intuit.election.service.EmailService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StubbedEmailService implements EmailService {

    private Map<String, String> sentMessages = new HashMap<>();

    @Override
    public void sendMessage(String emailAddress, String message) {
        sentMessages.put(emailAddress, message);
        System.out.println(String.format("Sending email to %s : %s", emailAddress, message));
    }

    @Override
    public void sendMessages(Set<String> emailAddresses, String message) {
        emailAddresses.forEach(emailAddress->sendMessage(emailAddress, message));
    }

    @Override
    public boolean messageSent(String emailAddress, String message) {
        if (sentMessages.get(emailAddress)!=null) {
            return sentMessages.get(emailAddress).equals(message);
        }
        return false;
    }

    public void reset() {
        sentMessages.clear();
    }
}
