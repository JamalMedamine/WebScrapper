package jamals;

import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.FetchType;
import javax.persistence.Table;

/**
 * Entity representing a StackOverflow tag.
 */
@Entity
@Table(name = "tags")
public class tag {
    @Id
    @Column(unique = true)
    private String tagName;

    // A tag can be used in many questions
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<question> questions = new HashSet<>();

    /**
     * Default constructor required by Hibernate.
     */
    public tag() {
    }

    /**
     * Parameterized constructor for creating a tag with a name.
     */
    public tag(String tagName) {
        this.tagName = tagName;
    }

    // Getters and setters
    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Set<question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<question> questions) {
        this.questions = questions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        tag tag = (tag) o;
        return Objects.equals(tagName, tag.tagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName);
    }

    @Override
    public String toString() {
        return tagName;
    }
}