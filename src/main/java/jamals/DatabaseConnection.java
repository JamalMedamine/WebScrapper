package jamals;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DatabaseConnection {
    private static SessionFactory sessionFactory;
    private static boolean initialized = false;

    // Private constructor to prevent instantiation
    private DatabaseConnection() {
    }

    public static synchronized void initialize(String url, String user, String password) {
        if (initialized) {
            return;
        }

        try {
            Configuration configuration = new Configuration();

            // Database connection properties
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url", url);
            configuration.setProperty("hibernate.connection.username", user);
            configuration.setProperty("hibernate.connection.password", password);
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");

            // Other settings
            configuration.setProperty("hibernate.show_sql", "true");
            configuration.setProperty("hibernate.format_sql", "true");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");

            // Add entity classes
            configuration.addAnnotatedClass(user.class);
            configuration.addAnnotatedClass(question.class);
            configuration.addAnnotatedClass(answer.class);
            configuration.addAnnotatedClass(tag.class);

            sessionFactory = configuration.buildSessionFactory();
            initialized = true;

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Hibernate", e);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (!initialized) {
            throw new IllegalStateException("Hibernate not initialized. Call initialize() first.");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    public static void QuestionDataBaseUpdate(int questionId, String title, String content, int votes, int nAnswers,
            int views, user user) {
        Transaction transaction = null;
        Session session = null;
        question q = new question();
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            user existingUser = session.createQuery("FROM user WHERE  nickName = :username", user.class)
                    .setParameter("username", user.getNickName())
                    .uniqueResult();

            session.saveOrUpdate(q);
            if (existingUser == null || user == null) {
                session.save(user);
            } else {
                user = existingUser;
                session.merge(user);
            }
            q.setTitle(title);
            q.setContent(content);
            q.setVotes(votes);

            q.setnAnswers(nAnswers);

            q.setViews(views);
            q.setUser(user);

            session.merge(q);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Rollback in case of an error
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close(); // Ensuring session is closed
            }
        }
    }

    public static void AnswerDataBaseUpdate(int answerId, String content, question question, user user) {
        Transaction transaction = null;
        Session session = null;
        try {
            // Add this to your answerScrapper method
            try {
                // Add a delay between requests to avoid rate limiting
                Thread.sleep(2000); // 2-second delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // FIRST: Ensure the user exists in the database
            user existingUser = null;
            if (user != null && user.getNickName() != null && !user.getNickName().trim().isEmpty()) {
                existingUser = session.createQuery("FROM user WHERE nickName = :username", user.class)
                        .setParameter("username", user.getNickName())
                        .uniqueResult();
            }

            if (existingUser == null && user != null) {
                // Save new user first to get an ID
                session.save(user);
            } else if (existingUser != null) {
                // Use the existing user from the database
                user = existingUser;
            }

            // SECOND: Ensure the question exists in the database
            question existingQuestion = null;
            if (question != null && question.getId() > 0) {
                existingQuestion = session.get(question.class, question.getId());
            }

            if (existingQuestion == null && question != null) {
                // Save question first to get an ID
                session.save(question);
            } else if (existingQuestion != null) {
                // Use the existing question from the database
                question = existingQuestion;
            }

            // Check if answer with this ID already exists
            answer existingAnswer = null;
            if (answerId > 0) {
                existingAnswer = session.get(answer.class, answerId);
            }

            if (existingAnswer != null) {
                // Update existing answer
                existingAnswer.setContent(content);

                // Only set references if they're valid entities
                if (question != null) {
                    existingAnswer.setQuestion(question);
                }
                if (user != null) {
                    existingAnswer.setUser(user);
                }

                session.update(existingAnswer);
            } else {
                // Create new answer
                answer a = new answer();
                if (answerId > 0) {
                    a.setId(answerId);
                }
                a.setContent(content);

                // Only set references if they're valid entities
                if (question != null) {
                    a.setQuestion(question);
                }
                if (user != null) {
                    a.setUser(user);
                }

                session.save(a);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public static void QuestionTagDataBaseUpdate(tag t, int questionId, String tagName) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            t = (tag) session.merge(t);
            t.setTagName(tagName);

            session.saveOrUpdate(t);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public static void UserDataBaseUpdate(user u, int userId, String nickName, int reputationScore, int gBadge,
            int sBadge, int bBadge) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // First check if user with same username already exists
            user existingUser = null;
            if (nickName != null && !nickName.trim().isEmpty()) {
                existingUser = session.createQuery("FROM user WHERE nickName = :nickname", user.class)
                        .setParameter("nickname", nickName)
                        .uniqueResult();
            }

            if (existingUser != null) {
                // User already exists, update existing user
                existingUser.setReputationScore(reputationScore);
                existingUser.setgBadge(gBadge);
                existingUser.setsBadge(sBadge);
                existingUser.setbBadge(bBadge);
                session.update(existingUser);
            } else {
                // New user, set properties and save
                u.setNickName(nickName);
                u.setReputationScore(reputationScore);
                u.setgBadge(gBadge);
                u.setsBadge(sBadge);
                u.setbBadge(bBadge);
                session.save(u);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public static void TagDataBaseUpdate(tag t, String tagName) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            t = (tag) session.merge(t);
            t.setTagName(tagName);

            session.saveOrUpdate(t);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

}
