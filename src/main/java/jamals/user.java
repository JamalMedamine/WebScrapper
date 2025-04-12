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

    // Getters and setters (updated for new field names)
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

    public void printUser() {
        System.out.println("name: " + this.nickName + " | reputaion score: " + this.reputationScore + "| badges: gold "
                + this.gBadge + ",silver " + this.sBadge + ",bronze " + this.bBadge);
    }
}