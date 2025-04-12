package jamals;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import jamals.DatabaseConnection;

/**
 * Hello world!
 */
public class Scrapper {

    ArrayList<question> questions = new ArrayList<question>();

    public int emptyStringHandler(String s) {
        s = s.replaceAll("\\s", "");
        if (s.equals("")) {
            return 0;
        } else {
            return Integer.parseInt(s);
        }
    }

    public int toNumber(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0; // Return 0 if input is empty
        }

        input = input.toLowerCase().trim().replace(",", "."); // Handle "15,6k"
        String[] parts = input.split("\\s+"); // Split by spaces

        for (String part : parts) {
            try {
                if (part.endsWith("k")) {
                    part = part.replace("k", "");
                    return (int) (Double.parseDouble(part) * 1000);
                }
                return Integer.parseInt(part);
            } catch (NumberFormatException e) {
                System.err.println("Skipping invalid number: " + part);
            }
        }

        System.err.println("No valid number found in input: '" + input + "'");
        return 0; // Return 0 if no number was found
    }

    public void questionScrapper(String tab, int nPages) {
        for (int i = 1; i <= nPages; i++) {
            try {
                Document doc = Jsoup.connect("https://stackoverflow.com/questions?tab=" + tab + "&page=" + i).get();

                Elements questions = doc.select(".s-post-summary.js-post-summary");
                for (Element q : questions) {
                    question question = new question();

                    // Extracting ID
                    String idText = q.id();
                    if (!idText.isEmpty() && idText.length() > 17) {
                        question.setId(Integer.parseInt(idText.substring(17)));
                    }

                    // Extracting title and content
                    question.setTitle(q.select("h3 > a").text());
                    question.setContent(q.select(".s-post-summary--content-excerpt").text());

                    // Extracting tags
                    Elements tags = q.select("a.s-tag.post-tag.flex--item.mt0");
                    for (Element t : tags) {
                        tag tag = new tag();
                        tag.setTagName(t.text());
                        question.getTags().add(tag);
                    }

                    // Extracting votes, answers, and views safely
                    Elements stats = q.select(".s-post-summary--stats-item-number");
                    if (stats.size() >= 3) { // Ensure all three elements exist
                        question.setVotes(toNumber(stats.get(0).text()));
                        question.setnAnswers(toNumber(stats.get(1).text()));
                        question.setViews(toNumber(stats.get(2).text()));
                    } else {
                        question.setVotes(0);
                        question.setnAnswers(0);
                        question.setViews(0);
                    }

                    // Extracting user details
                    user u = new user();
                    Elements userBlock = q.select(".s-user-card--link"); // Corrected selector
                    if (!userBlock.isEmpty()) {
                        u.setNickName(userBlock.text());
                        u.setReputationScore(toNumber(q.select("span.reputation-score").text()));
                        u.setgBadge(emptyStringHandler(q.select(".badge1 + .badgecount").text()));
                        u.setsBadge(emptyStringHandler(q.select(".badge2 + .badgecount").text()));
                        u.setbBadge(emptyStringHandler(q.select(".badge3 + .badgecount").text()));
                    }

                    question.setUser(u);
                    this.questions.add(question);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void answerScrapper(question q) {
        String title = q.getTitle().replaceAll(" ", "-");
        int id = q.getId();
        try {
            Document doc = Jsoup.connect("https://stackoverflow.com/questions/" + id + "/" + title).get();

            // Ensure question user is not null
            if (q.getUser() == null) {
                q.setUser(new user());
            }

            System.out.println(q.getUser().getNickName());
            System.out.println(q.getUser().getReputationScore());
            System.out.println("badges  | gold: " + q.getUser().getgBadge() +
                    " silver: " + q.getUser().getsBadge() + " bronze : " + q.getUser().getbBadge());

            Elements answers = doc.select(".answer.js-answer");

            for (Element answer : answers) {
                answer a = new answer();
                a.setId(Integer.parseInt(answer.id().substring(7)));
                a.setContent(answer.select(".s-prose.js-post-body").text());

                // Ensure answer user is not null
                user answerUser = new user();
                answerUser.setNickName(answer.select(".user-details > a").text());
                answerUser.setReputationScore(toNumber(answer.select(".reputation-score").text()));
                answerUser.setgBadge(emptyStringHandler(answer.select(".badge1 + .badgecount").text()));
                answerUser.setsBadge(emptyStringHandler(answer.select(".badge2 + .badgecount").text()));
                answerUser.setbBadge(emptyStringHandler(answer.select(".badge3 + .badgecount").text()));

                a.setUser(answerUser); // Assign user to answer
                q.getAnswers().add(a); // Add answer to question
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scrapper qS = new Scrapper();

        DatabaseConnection.initialize(
                "jdbc:mysql://localhost:3306/scrappers",
                "root",
                "Y1a2s3!?");

        qS.questionScrapper("Newest", 5);

        for (question q : qS.questions) {
            qS.answerScrapper(q);

            for (tag t : q.getTags()) {
                DatabaseConnection.TagDataBaseUpdate(t, t.getTagName());
            }
            user u = new user();
            DatabaseConnection.UserDataBaseUpdate(u, q.getUser().getId(), q.getUser().getNickName(),
                    q.getUser().getReputationScore(), q.getUser().getgBadge(),
                    q.getUser().getsBadge(), q.getUser().getbBadge());
            DatabaseConnection.QuestionDataBaseUpdate(q.getId(), q.getTitle(),
                    q.getContent(), q.getVotes(),
                    q.getnAnswers(), q.getViews(), q.getUser());

            for (answer a : q.getAnswers()) {
                user aUser = new user();
                DatabaseConnection.UserDataBaseUpdate(aUser, a.getUser().getId(),
                        a.getUser().getNickName(),
                        a.getUser().getReputationScore(), a.getUser().getgBadge(),
                        a.getUser().getsBadge(),
                        a.getUser().getbBadge());
                DatabaseConnection.AnswerDataBaseUpdate(a.getId(), a.getContent(), q,
                        a.getUser());
            }
            q.printQuestion();
        }

        // Shutdown the connection at the end of all operations
        DatabaseConnection.shutdown();
    }
}
