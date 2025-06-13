package com.example.repositorio;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class Repositorio {
    private final EntityManager em;
    public Repositorio(EntityManagerFactory emf) {
        if (!emf.isOpen()) {
            throw new IllegalArgumentException("EntityManagerFactory está cerrado.");
        }
        this.em = emf.createEntityManager();
    }
}
