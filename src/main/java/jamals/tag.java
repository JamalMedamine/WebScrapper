package jamals;

import java.util.Set;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.FetchType;

import javax.persistence.Table;

@Entity
@Table(name = "tags")
public class tag {
    @Id
    @Column(unique = true)
    private String tagName;

    // A tag can be used in many questions
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<question> questions = new HashSet<>();

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}