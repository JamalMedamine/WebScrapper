package jamals;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Entity representing a StackOverflow answer.
 */
@Entity
@Table(name = "answers")
public class answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "TEXT")
    private String content;

    // An answer belongs to a question
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private question question;

    // An answer is provided by a user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private user author;

    /**
     * Default constructor required by Hibernate.
     */
    public answer() {
    }

    /**
     * Parameterized constructor for creating answers with basic information.
     */
    public answer(String content, question question, user author) {
        this.content = content;
        this.question = question;
        this.author = author;
    }

    // Getters and setters
    public String getContent() {
        return content;
    }

    public question getQuestion() {
        return question;
    }

    public int getId() {
        return id;
    }

    public user getUser() {
        return author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUser(user user) {
        this.author = user;
    }

    public void setQuestion(question question) {
        this.question = question;
    }

    /**
     * Print answer details to console.
     */
    public void printAnswer() {
        System.out.println("answer id : " + this.id);
        System.out.println("answer content : " + this.content);
        if (this.author != null) {
            this.author.printUser();
        } else {
            System.out.println("author: unknown");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        answer answer = (answer) o;
        return id == answer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", content='"
                + (content != null ? content.substring(0, Math.min(content.length(), 30)) + "..." : "null") + '\'' +
                ", author=" + (author != null ? author.getNickName() : "unknown") +
                '}';
    }
}