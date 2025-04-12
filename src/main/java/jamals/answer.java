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

@Entity
@Table(name = "answers")
public class answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "TEXT")
    private String content;

    // A question can have many answers
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private question question;

    // A user can provide many answers
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private user author;

    public String getContent() {
        return content;
    }

    public question getQestion() {
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

    public void printAnswer() {
        System.out.println("answer id : " + this.id);
        System.out.println("answer content : " + this.content);
        this.author.printUser();
    }
}
