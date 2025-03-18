package jamals;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
/**
 * Hello world!
 */
public class Scrapper {

    ArrayList<question> questions = new ArrayList<question>();

    public int emptyStringHandler(String s){
         s = s.replaceAll("\\s", ""); 
        if (s.equals("") ){
            return 0;
        }else{
            return Integer.parseInt(s);
        }
    }

    public int toNumber(String input){
         
        input = input.toLowerCase().trim().replace(",", "."); // Handle "15,6k"
        String[] parts = input.split("\\s+"); // Split by spaces

    for (String part : parts) { // Process each part
        try {
            if (part.endsWith("k")) {
                part = part.replace("k", "");
                return (int) (Double.parseDouble(part) * 1000);
            }
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {

        }
    }

    throw new NumberFormatException("No valid number found in: " + input);

    }
    public void questionScrapper(String tab,int nPages) {
        for(int i=1;i<=nPages;i++){
            try{
                Document doc = Jsoup.connect("https://stackoverflow.com/questions?tab="+tab+"&page="+i).get();

                Elements questions = doc.select(".s-post-summary.js-post-summary");
                for(Element q : questions){
                    question question = new question();
                    question.setId(Integer.parseInt(q.id().substring(17)));
                    question.setTitle(q.select("h3 > a").text());
                    question.setContent(q.select(".s-post-summary--content-excerpt").text()); 
                    Elements tags = q.select("a.s-tag.post-tag.flex--item.mt0");
                     for(Element t : tags){
                        tag tag = new tag();
                        tag.setTagName(t.text());
                        question.getTags().add(tag);
                     }

                    Elements stats = q.select(".s-post-summary--stats-item-number");
                    question.setVotes(Integer.parseInt(stats.get(0).text())); 
                    question.setnAnswers(Integer.parseInt(stats.get(1).text()));
                    question.setViews(Integer.parseInt(stats.get(2).text())); 

                    this.questions.add(question);
                 }
            }catch(IOException e){
                e.printStackTrace();
             }
        }
     }
     public void answerScrapper(question q){
        String title = q.getTitle().replaceAll(" ", "-");
        int id = q.getId();
        try{
            ArrayList<answer> ans = new ArrayList<answer>();

            Document doc = Jsoup.connect("https://stackoverflow.com/questions/"+id+"/"+title).get();
            Elements questionBlock = doc.select(".postcell.post-layout--right");
            q.getUser().setNickName(questionBlock.select(".user-details > a").text());
            q.getUser().setReputationScore(toNumber(questionBlock.select("span.reputation-score").text()));
            q.getUser().setgBadge(emptyStringHandler(questionBlock.select(".badge1 + .badgecount").text()));
            q.getUser().setsBadge(emptyStringHandler(questionBlock.select(".badge2 + .badgecount").text()));
            q.getUser().setbBadge(emptyStringHandler(questionBlock.select(".badge3 + .badgecount").text()));

            System.out.println(q.getUser().getNickName());
            System.out.println(q.getUser().getReputationScore());
            System.out.println("badges  | gold: "+q.getUser().getgBadge()+" silver: "+q.getUser().getsBadge()+" bronze : "+q.getUser().getbBadge());


            Elements answers = doc.select(".answer.js-answer");

            for(Element answer : answers){
                answer a = new answer();
                a.setId(Integer.parseInt(answer.id().substring(7)));
                a.setContent(answer.select(".s-prose.js-post-body").text());
                a.getUser().setNickName(answer.select("user-details > a").text());
                a.getUser().setReputationScore(toNumber(answer.select(".reputation-score").text()));
                a.getUser().setgBadge(emptyStringHandler(answer.select(".badge1 + .badgecount").text()));
                a.getUser().setsBadge(emptyStringHandler(answer.select(".badge2 + .badgecount").text()));
                a.getUser().setbBadge(emptyStringHandler(answer.select(".badge3 + .badgecount").text()));
            }

        }catch(IOException e){
            e.printStackTrace();
        }
     }
     public static void main(String[] args){
        Scrapper qS  = new Scrapper(); 
        qS.questionScrapper("Newest", 5);
        for(question q:qS.questions){
            qS.answerScrapper(q);
            q.printQuestion();
        }
     }
}
