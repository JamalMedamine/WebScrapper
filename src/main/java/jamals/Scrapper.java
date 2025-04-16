package jamals;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;

public class Scrapper {
    private static final Logger LOGGER = Logger.getLogger(Scrapper.class.getName());

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
            return 0;
        }

        input = input.toLowerCase().trim().replace(",", ".");
        String[] parts = input.split("\\s+");

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
        return 0;
    }

    public void questionScrapper(String tab, int nPages) {
        for (int i = 1; i <= nPages; i++) {
            try {
                Document doc = Jsoup.connect("https://stackoverflow.com/questions?tab=" + tab + "&page=" + i).get();

                Elements questions = doc.select(".s-post-summary.js-post-summary");
                for (Element q : questions) {
                    question question = new question();

                    String idText = q.id();
                    if (!idText.isEmpty() && idText.length() > 17) {
                        question.setId(Integer.parseInt(idText.substring(17)));
                    }

                    question.setTitle(q.select("h3 > a").text());
                    question.setContent(q.select(".s-post-summary--content-excerpt").text());

                    Elements tags = q.select("a.s-tag.post-tag.flex--item.mt0");
                    for (Element t : tags) {
                        tag tag = new tag();
                        tag.setTagName(t.text());
                        question.getTags().add(tag);
                    }

                    Elements stats = q.select(".s-post-summary--stats-item-number");
                    if (stats.size() >= 3) {
                        question.setVotes(toNumber(stats.get(0).text()));
                        question.setnAnswers(toNumber(stats.get(1).text()));
                        question.setViews(toNumber(stats.get(2).text()));
                    } else {
                        question.setVotes(0);
                        question.setnAnswers(0);
                        question.setViews(0);
                    }

                    user u = new user();
                    Elements userBlock = q.select(".s-user-card--link");
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

                user answerUser = new user();
                answerUser.setNickName(answer.select(".user-details > a").text());
                answerUser.setReputationScore(toNumber(answer.select(".reputation-score").text()));
                answerUser.setgBadge(emptyStringHandler(answer.select(".badge1 + .badgecount").text()));
                answerUser.setsBadge(emptyStringHandler(answer.select(".badge2 + .badgecount").text()));
                answerUser.setbBadge(emptyStringHandler(answer.select(".badge3 + .badgecount").text()));

                a.setUser(answerUser);
                q.getAnswers().add(a);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the list of scraped questions.
     *
     * @return The list of questions
     */
    public List<question> getQuestions() {
        return this.questions;
    }

    /**
     * Main method to run the scraper.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            LOGGER.info("Starting StackOverflow scraper...");
            Scrapper scraper = new Scrapper();

            DatabaseConnection.initialize(
                    "jdbc:mysql://localhost:3306/scrappers",
                    "root",
                    "Y1a2s3!?");
            LOGGER.info("Database connection initialized");

            scraper.questionScrapper("Newest", 2);
            LOGGER.info("Scraped " + scraper.getQuestions().size() + " questions");

            int successCount = 0;
            for (question q : scraper.getQuestions()) {
                try {
                    scraper.answerScrapper(q);

                    DatabaseConnection.saveCompleteQuestion(q);

                    q.printQuestion();

                    successCount++;

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error processing question ID " + q.getId(), e);

                }
            }

            LOGGER.info("Scraping completed. Successfully processed " + successCount + " out of "
                    + scraper.getQuestions().size() + " questions.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in main execution", e);
        } finally {
            try {
                LOGGER.info("Shutting down database connection...");
                DatabaseConnection.shutdown();

                TimeUnit.SECONDS.sleep(2);
                LOGGER.info("Shutdown complete");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warning("Interrupted during shutdown");
            }
        }
    }
}