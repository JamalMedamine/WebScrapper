package jamals;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a StackOverflow user.
 */
@Entity
@Table(name = "users")
public class user {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(name = "username", unique = true)
    private String nickName;

    @Column(name = "reputation_score")
    private int reputationScore;

    @Column(name = "gold_badges")
    private int gBadge;

    @Column(name = "silver_badges")
    private int sBadge;

    @Column(name = "bronze_badges")
    private int bBadge;

    // A user can ask many questions
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<question> questions = new ArrayList<>();

    // A user can answer many questions
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<answer> answers = new ArrayList<>();

    /**
     * Default constructor required by Hibernate.
     */
    public user() {
    }

    /**
     * Parameterized constructor for creating users with basic information.
     */
    public user(String nickName, int reputationScore, int gBadge, int sBadge, int bBadge) {
        this.nickName = nickName;
        this.reputationScore = reputationScore;
        this.gBadge = gBadge;
        this.sBadge = sBadge;
        this.bBadge = bBadge;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getNickName() {
        return nickName;
    }

    public int getReputationScore() {
        return reputationScore;
    }

    public int getgBadge() {
        return gBadge;
    }

    public int getsBadge() {
        return sBadge;
    }

    public int getbBadge() {
        return bBadge;
    }

    public List<question> getQuestions() {
        return questions;
    }

    public List<answer> getAnswers() {
        return answers;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setReputationScore(int reputationScore) {
        this.reputationScore = reputationScore;
    }

    public void setgBadge(int gBadge) {
        this.gBadge = gBadge;
    }

    public void setsBadge(int sBadge) {
        this.sBadge = sBadge;
    }

    public void setbBadge(int bBadge) {
        this.bBadge = bBadge;
    }

    public void setQuestions(List<question> questions) {
        this.questions = questions;
    }

    public void setAnswers(List<answer> answers) {
        this.answers = answers;
    }

    /**
     * Add a question to this user's list of questions.
     */
    public void addQuestion(question q) {
        questions.add(q);
        q.setUser(this);
    }

    /**
     * Add an answer to this user's list of answers.
     */
    public void addAnswer(answer a) {
        answers.add(a);
        a.setUser(this);
    }

    /**
     * Print user details to console.
     */
    public void printUser() {
        System.out.println("name: " + this.nickName + " | reputation score: " + this.reputationScore +
                " | badges: gold " + this.gBadge + ", silver " + this.sBadge + ", bronze " + this.bBadge);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        user user = (user) o;
        return id == user.id ||
                (nickName != null && nickName.equals(user.nickName));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nickName);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nickname='" + nickName + '\'' +
                ", reputationScore=" + reputationScore +
                ", badges=" + gBadge + "g/" + sBadge + "s/" + bBadge + "b" +
                '}';
    }
}