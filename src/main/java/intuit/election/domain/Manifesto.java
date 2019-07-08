package intuit.election.domain;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Manifesto {
    private static final int MIN_IDEAS = 1;
    private static final int MAX_IDEAS = 3;
    private final Set<Idea> ideas = new HashSet<>(MAX_IDEAS);

    public static Manifesto of(Idea... ideasArray) {
        if (null == ideasArray || ideasArray.length == 0 || ideasArray.length > 3) {
            throw new UnsupportedOperationException(String.format("Manifesto must have between %d and %d ideas", MIN_IDEAS, MAX_IDEAS));
        }
        Manifesto manifesto = new Manifesto();
        manifesto.ideas.addAll(Arrays.asList(ideasArray));
        return manifesto;
    }

    public Collection<Idea> getIdeas() {
        return Collections.unmodifiableSet(ideas);
    }

    public void add(@NonNull Idea idea) {
        if (ideas.size() == MAX_IDEAS) {
            throw new UnsupportedOperationException(String.format("Manifesto cannot have more than %d ideas", MAX_IDEAS));
        }
        ideas.add(idea);
    }

    public void remove(@NonNull Idea idea) {
        if (ideas.size() == MIN_IDEAS) {
            throw new UnsupportedOperationException(String.format("Manifesto cannot have less than %d ideas", MIN_IDEAS));
        }
        ideas.remove(idea);
    }
}
