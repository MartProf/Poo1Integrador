package com.example.servicio;

import com.example.modelo.Persona;
import com.example.repositorio.Repositorio;

public class Servicio {
    private Repositorio repositorio;

    public Servicio(Repositorio repo) {
        this.repositorio = repo;
    }

    public Persona login(String usuario, String contrasena) {
        return repositorio.buscarPersonaPorUsuarioYContrasena(usuario, contrasena);
    }

    public void registrarPersona(Persona persona) {
        repositorio.guardarPersona(persona);
    }
}
