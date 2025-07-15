package com.example.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    private static final EntityManagerFactory emf;

    static {
        try {
            // Acá se lee tu persistence.xml
            emf = Persistence.createEntityManagerFactory("Municipalidad");
        } catch (Throwable ex) {
            System.err.println("Inicialización de EntityManagerFactory falló: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Devuelve el EMF global
    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    // Opcional: devuelve un EM nuevo (útil si no usás repositorio con EMF directo)
    public static jakarta.persistence.EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // Cierra todo (se llama al cerrar la app)
    public static void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
