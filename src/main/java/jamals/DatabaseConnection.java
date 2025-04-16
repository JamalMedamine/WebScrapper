package jamals;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

/**
 * Manages database connections and operations for the StackOverflow scraper.
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static SessionFactory sessionFactory;
    private static boolean initialized = false;
    private static String databaseUrl;

    private DatabaseConnection() {
    }

    /**
     * Initialize the database connection.
     *
     * @param url      Database URL
     * @param user     Database username
     * @param password Database password
     */
    public static synchronized void initialize(String url, String user, String password) {
        if (initialized) {
            LOGGER.info("Database already initialized");
            return;
        }

        try {
            LOGGER.info("Initializing database connection: " + url);
            databaseUrl = url;

            Configuration configuration = new Configuration();

            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url", url);
            configuration.setProperty("hibernate.connection.username", user);
            configuration.setProperty("hibernate.connection.password", password);
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");

            configuration.setProperty("hibernate.connection.pool_size", "10");
            configuration.setProperty("hibernate.current_session_context_class", "thread");
            configuration.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.internal.NoCacheProvider");
            configuration.setProperty("hibernate.connection.autocommit", "false");
            configuration.setProperty("hibernate.connection.release_mode", "after_transaction");
            configuration.setProperty("hibernate.jdbc.batch_size", "30");

            configuration.setProperty("hibernate.show_sql", "false");
            configuration.setProperty("hibernate.format_sql", "true");
            configuration.setProperty("hibernate.use_sql_comments", "true");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");

            configuration.addAnnotatedClass(user.class);
            configuration.addAnnotatedClass(question.class);
            configuration.addAnnotatedClass(answer.class);
            configuration.addAnnotatedClass(tag.class);

            sessionFactory = configuration.buildSessionFactory();
            initialized = true;
            LOGGER.info("Hibernate initialized successfully");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Hibernate", e);
            throw new RuntimeException("Failed to initialize Hibernate", e);
        }
    }

    /**
     * 
     *
     * @return
     */
    public static SessionFactory getSessionFactory() {
        if (!initialized) {
            throw new IllegalStateException("Hibernate not initialized. Call initialize() first.");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            try {
                LOGGER.info("Closing Hibernate SessionFactory...");
                sessionFactory.close();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.warning("Thread interrupted while waiting for cleanup");
                }

                try {
                    LOGGER.info("Deregistering JDBC driver...");
                    Enumeration<Driver> drivers = DriverManager.getDrivers();
                    while (drivers.hasMoreElements()) {
                        Driver driver = drivers.nextElement();
                        if (driver.getClass().getName().contains("com.mysql")) {
                            DriverManager.deregisterDriver(driver);
                            LOGGER.info("Deregistered driver: " + driver);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error deregistering driver: " + e.getMessage(), e);
                }
                System.gc();

                LOGGER.info("Database resources released successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during shutdown: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Save or update a complete question with all its relationships.
     *
     * @param q The question to save
     */
    public static void saveCompleteQuestion(question q) {
        if (q == null)
            return;

        Transaction transaction = null;
        Session session = null;

        try {
            LOGGER.info("Starting saveCompleteQuestion for question ID: " + q.getId());

            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            if (q.getUser() != null) {
                saveUserSafely(session, q.getUser());
            }

            for (answer a : q.getAnswers()) {
                if (a.getUser() != null) {
                    saveUserSafely(session, a.getUser());
                }
            }

            for (tag t : q.getTags()) {
                tag existingTag = session.get(tag.class, t.getTagName());
                if (existingTag == null) {
                    session.save(t);
                }
            }

            session.flush();

            question existingQuestion = null;
            if (q.getId() > 0) {
                existingQuestion = session.get(question.class, q.getId());
            }

            if (existingQuestion == null) {
                ArrayList<answer> tempAnswers = new ArrayList<>(q.getAnswers());
                q.getAnswers().clear();

                session.save(q);
                LOGGER.info("Inserted new question with ID: " + q.getId());

                session.flush();

                for (answer a : tempAnswers) {
                    a.setQuestion(q);

                    a.setId(0);
                    session.save(a);
                    q.getAnswers().add(a);

                    session.flush();
                }
            } else {
                existingQuestion.setTitle(q.getTitle());
                existingQuestion.setContent(q.getContent());
                existingQuestion.setVotes(q.getVotes());
                existingQuestion.setnAnswers(q.getnAnswers());
                existingQuestion.setViews(q.getViews());
                if (q.getUser() != null) {
                    existingQuestion.setUser(q.getUser());
                }

                existingQuestion.getTags().clear();
                existingQuestion.getTags().addAll(q.getTags());

                session.update(existingQuestion);
                LOGGER.info("Updated existing question with ID: " + existingQuestion.getId());

                session.flush();

                ArrayList<answer> tempAnswers = new ArrayList<>(q.getAnswers());
                for (answer a : tempAnswers) {
                    a.setQuestion(existingQuestion);

                    if (a.getId() > 0) {
                        answer existingAnswer = session.get(answer.class, a.getId());

                        if (existingAnswer != null) {
                            existingAnswer.setContent(a.getContent());
                            if (a.getUser() != null) {
                                existingAnswer.setUser(a.getUser());
                            }
                            session.update(existingAnswer);
                        } else {
                            a.setId(0);
                            session.save(a);
                        }
                    } else {

                        session.save(a);
                    }

                    session.flush();
                }
            }

            transaction.commit();
            LOGGER.info("Successfully saved question with ID: " + q.getId());

        } catch (Exception e) {
            if (transaction != null) {
                LOGGER.log(Level.SEVERE, "Rolling back transaction due to error: " + e.getMessage(), e);
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error saving question", e);
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Helper method to safely save a user entity.
     *
     * @param session The Hibernate session
     * @param u       The user to save
     */
    private static void saveUserSafely(Session session, user u) {
        if (u == null || u.getNickName() == null || u.getNickName().trim().isEmpty()) {
            return;
        }

        try {
            String queryString = "FROM user WHERE nickName = :nickname";
            Query<user> query = session.createQuery(queryString, user.class)
                    .setParameter("nickname", u.getNickName());

            user existingUser = query.uniqueResult();

            if (existingUser != null) {

                existingUser.setReputationScore(u.getReputationScore());
                existingUser.setgBadge(u.getgBadge());
                existingUser.setsBadge(u.getsBadge());
                existingUser.setbBadge(u.getbBadge());

                u.setId(existingUser.getId());

                session.update(existingUser);
                LOGGER.info(
                        "Updated existing user: " + existingUser.getNickName() + " with ID: " + existingUser.getId());
            } else {
                session.save(u);
                LOGGER.info("Saved new user: " + u.getNickName() + " with ID: " + u.getId());
            }

            session.flush();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving user: " + e.getMessage(), e);
            throw e;
        }
    }

}