package intuit.election.stub;

import intuit.election.service.EmailService;

import java.util.Set;

public class StubbedEmailService implements EmailService {
    @Override
    public void sendMessage(String emailAddress, String message) {
        System.out.println(String.format("Sending email to %s : %s", emailAddress, message));
    }

    @Override
    public void sendMessages(Set<String> emailAddresses, String message) {
        emailAddresses.forEach(emailAddress->sendMessage(emailAddress, message));
    }
}
