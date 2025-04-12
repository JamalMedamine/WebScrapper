package jamals;

import jamals.question;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "questions")
public class question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int votes;
    private int nAnswers;
    private int views;

    // A question is asked by a single user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private user user;

    // A question can have many answers
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<answer> answers = new ArrayList<>();

    // A question can have many tags
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "question_tags", joinColumns = @JoinColumn(name = "question_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<tag> tags = new HashSet<>();

    public user getUser() {
        return user;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public int getVotes() {
        return this.votes;
    }

    public int getnAnswers() {
        return nAnswers;
    }

    public int getViews() {
        return this.views;
    }

    public List<answer> getAnswers() {
        return this.answers;
    }

    public Set<tag> getTags() {
        return this.tags;
    }

    public void setUser(user user) {
        this.user = user;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void setnAnswers(int nAnswers) {
        this.nAnswers = nAnswers;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void setAnswers(ArrayList<answer> answers) {
        this.answers = answers;
    }

    public void setTags(ArrayList<tag> tags) {
        this.tags = new HashSet<>(tags);
    }

    public void printQuestion() {
        System.out.println("----------------------------infos----------------------------");
        System.out.println("title | " + this.title);
        System.out.println("votes | " + this.votes);
        System.out.println("answers | " + this.nAnswers);
        System.out.println("views | " + this.views);
        System.out.println("-----------------------------tags-------------------------------");
        for (tag t : this.tags) {
            System.out.print(t.getTagName() + ",");

        }
        System.out.println();
        System.out.println("------------------------------content------------------------------");
        System.out.println("content | " + this.content);
        System.out.println("------------------------------Answers-------------------------------");
        for (answer a : this.answers) {
            a.printAnswer();
        }
        System.out.println("---------------------------------------------------------------------");
    }
}
