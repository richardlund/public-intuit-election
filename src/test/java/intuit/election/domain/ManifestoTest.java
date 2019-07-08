package intuit.election.domain;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class ManifestoTest {

    private static final Idea FIRST_IDEA = Idea.of("First idea");
    private static final Idea SECOND_IDEA = Idea.of("Second idea");
    private static final Idea THIRD_IDEA = Idea.of("Third idea");
    private static final Idea FOURTH_IDEA = Idea.of("Fourth idea");

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void canCreateManifestoWithOneIdea() {
        Manifesto manifesto = Manifesto.of(FIRST_IDEA);
        assertThat(manifesto.getIdeas(), contains(FIRST_IDEA));
    }

    @Test
    public void canCreateManifestoWithTwoIdeas() {
        Manifesto manifesto = Manifesto.of(FIRST_IDEA, SECOND_IDEA);
        assertThat(manifesto.getIdeas(), contains(FIRST_IDEA, SECOND_IDEA));
    }

    @Test
    public void canCreateManifestoWithThreeIdeas() {
        Manifesto manifesto = Manifesto.of(FIRST_IDEA, SECOND_IDEA, THIRD_IDEA);
        assertThat(manifesto.getIdeas(), contains(FIRST_IDEA, SECOND_IDEA, THIRD_IDEA));
    }

    @Test
    public void cannotCreateManifestoWithMoreThanThreeIdeas() {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Manifesto must have between 1 and 3 ideas");
        Manifesto.of(FIRST_IDEA, SECOND_IDEA, THIRD_IDEA, FOURTH_IDEA);
    }

    @Test
    public void cannotCreateManifestoWithNoIdeas() {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Manifesto must have between 1 and 3 ideas");
        Manifesto.of();
    }

    @Test
    public void cannotCreateManifestoWithNullIdeas() {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Manifesto must have between 1 and 3 ideas");
        Manifesto.of(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cannotModifyTheIdeasOfTheManifestoByReference() {
        Manifesto manifesto = Manifesto.of(FIRST_IDEA, SECOND_IDEA);
        manifesto.getIdeas().add(THIRD_IDEA);
    }

    @Test
    public void canAddAnIdeaIfManifestoContainsLessThanThreeIdeas() {
        Manifesto manifesto = Manifesto.of(FIRST_IDEA);
        manifesto.add(SECOND_IDEA);
        assertThat(manifesto.getIdeas(), contains(FIRST_IDEA, SECOND_IDEA));
    }

    @Test
    public void cannotAddAnIdeaIfManifestoAlreadyContainsThreeIdeas() {
        Manifesto manifesto = Manifesto.of(FIRST_IDEA, SECOND_IDEA, THIRD_IDEA);

        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Manifesto cannot have more than 3 ideas");

        manifesto.add(FOURTH_IDEA);
    }

    @Test(expected = NullPointerException.class)
    public void cannotAddANullIdeaToAManifesto() {
        Manifesto manifesto = Manifesto.of(FIRST_IDEA);
        manifesto.add(null);
    }

    @Test
    public void canRemoveAnIdeaFromTheManifesto() {
        Manifesto manifesto = Manifesto.of(FIRST_IDEA, SECOND_IDEA, THIRD_IDEA);
        manifesto.remove(THIRD_IDEA);
        assertThat(manifesto.getIdeas(), contains(FIRST_IDEA, SECOND_IDEA));
    }

    @Test
    public void cannotRemoveAllIdeasFromTheManifesto() {
        Manifesto manifesto = Manifesto.of(FIRST_IDEA);

        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Manifesto cannot have less than 1 idea");

        manifesto.remove(FIRST_IDEA);
    }
}