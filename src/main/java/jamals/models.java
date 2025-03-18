package jamals;
import java.util.ArrayList;
class user{
    int id;
    String nickName;
    int reputationScore;
    int gBadge;
    int sBadge;
    int bBadge;
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
    public void printUser(){
        System.out.println("name: "+this.nickName+" | reputaion score: "+this.reputationScore+"| badges: gold "+this.gBadge+",silver "+this.sBadge+",bronze "+this.bBadge);
    }
}
class tag{
    String tagName;
    public String getTagName() {
        return tagName;
    }
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
class answer{
    int id;
    String content;
    user user = new user();

    public String getContent() {
        return content;
    }
    public int getId() {
        return id;
    }
    public user getUser() {
        return user;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setUser(user user) {
        this.user = user;
    }
    public void printAnswer(){
        System.out.println("answer id : "+this.id);
        System.out.println("answer content : "+this.content);
        this.user.printUser();
    }
}
class question{
    user user=new user();
    int id;
    String title,content;
    int votes,nAnswers,views;
    ArrayList<answer> answers = new ArrayList<answer>();
    ArrayList<tag> tags = new ArrayList<tag>();
    public user getUser() {
        return user;
    }
 public int getId() {
     return id;
 }
    public String getTitle(){
        return this.title;
    }
     public String getContent(){
        return this.content;
    }
    public int getVotes(){
        return this.votes;
    }
    public int getnAnswers() {
        return nAnswers;
    }
    public int getViews(){
        return this.views;
    }
    public ArrayList<answer> getAnswers(){
        return this.answers;
    }
    public ArrayList<tag> getTags(){
        return this.tags;
    }
    public void setUser(user user) {
        this.user = user;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setContent(String content){
        this.content = content;
    }
    public void setVotes(int votes){
        this.votes = votes;
    }
    public void setnAnswers(int nAnswers) {
        this.nAnswers = nAnswers;
    }
    public void setViews(int views){
        this.views = views;
    }
    public void setAnswers(ArrayList<answer> answers){
        this.answers = answers;
    }
     public void setTags(ArrayList<tag> tags){
        this.tags = tags;
    }
    public void printQuestion(){
        System.out.println("----------------------------infos----------------------------");
        System.out.println("title | "+this.title);
        System.out.println("votes | "+this.votes);
        System.out.println("answers | "+this.nAnswers);
        System.out.println("views | "+this.views);
        System.out.println("-----------------------------tags-------------------------------");
        for(tag t : this.tags){
            System.out.print(t.tagName+",");
            
        }
        System.out.println();
        System.out.println("------------------------------content------------------------------");
        System.out.println("content | "+this.content);
        System.out.println("------------------------------Answers-------------------------------");
        for(answer a : this.answers){
            a.printAnswer();
        }
        System.out.println("---------------------------------------------------------------------");
    }
}

public class models {
    
}
