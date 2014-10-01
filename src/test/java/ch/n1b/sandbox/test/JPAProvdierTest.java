package ch.n1b.sandbox.test;

import ch.n1b.sandbox.jpa.AnEntity;
import ch.n1b.sandbox.jpa.JPAProvider;
import ch.n1b.sandbox.jpa.JPAService;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Created on 01.10.2014.
 * @author Thomas Richner
 */
public class JPAProvdierTest {

    private static final String DB = "test2";

    @Test
    public void connectTest(){
        try {
            // create DB, fails if it doesn't exsist (we should check first)
            createDB();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // create a JPAService
        JPAService service = new JPAProvider("jdbc:mysql://localhost:3306/jpatest_");

        // fetch an EntityManager
        EntityManager em = service.getEntityManager(DB);

        // we need a transaction, otherwise nothing will happen (in JEE the container does it for you)
        EntityTransaction t = em.getTransaction();
        t.begin();

        // create a dummy
        AnEntity entity = new AnEntity();
        entity.setText("Hello World!");
        em.persist(entity);

        t.commit();
    }

    private void createDB() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/","root","1234");
        try(Statement stmt = conn.createStatement();) {
            int ret = stmt.executeUpdate("CREATE DATABASE jpatest_" + DB);
        }
        conn.close();
    }
}
