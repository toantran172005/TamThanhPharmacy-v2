package app;

import db.ConnectDb;
import jakarta.persistence.EntityManager;

public class RunApplication {
    public static void main(String[] args) {
        EntityManager em = ConnectDb.getEntityManager();
    }
}
