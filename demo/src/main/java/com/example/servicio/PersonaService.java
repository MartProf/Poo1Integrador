package com.example.servicio;

import java.util.List;

import com.example.modelo.Persona;
import com.example.repositorio.PersonaRepository;

public class PersonaService {

    private PersonaRepository personaRepo;

    public PersonaService() {
        this.personaRepo = new PersonaRepository();
    }

    public Persona buscarPorDni(int dni) {
        return personaRepo.findByDni(dni);
    }

    public Persona buscarPorDniConParticipaciones(int dni) {
        return personaRepo.findByDniWithParticipaciones(dni);
    }

    public List<Persona> buscarPorNombre(String nombre) {
        return personaRepo.findByNombreContaining(nombre);
    }

    public void registrarPersona(Persona persona) {
        // Validaciones básicas de campos obligatorios
        if (persona.getNombre() == null || persona.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (persona.getApellido() == null || persona.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (persona.getEmail() == null || persona.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio.");
        }
        if (persona.getUsuario() == null || persona.getUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (persona.getContrasena() == null || persona.getContrasena().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (persona.getTelefono() == null || persona.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }

        // Validación de formato de email
        String email = persona.getEmail().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("El formato del email no es válido.");
        }

        // Validación de DNI positivo
        if (persona.getDni() <= 0) {
            throw new IllegalArgumentException("El DNI debe ser un número positivo.");
        }

        // Validaciones de unicidad (consultas a la BD)
        if (personaRepo.existeDni(persona.getDni())) {
            throw new IllegalArgumentException("Ya existe una persona registrada con este DNI.");
        }
        
        if (personaRepo.existeUsuario(persona.getUsuario().trim())) {
            throw new IllegalArgumentException("Ya existe una persona registrada con este nombre de usuario.");
        }
        
        if (personaRepo.existeEmail(email)) {
            throw new IllegalArgumentException("Ya existe una persona registrada con este email.");
        }

        // Si todas las validaciones pasan, limpiar datos y guardar
        persona.setNombre(persona.getNombre().trim());
        persona.setApellido(persona.getApellido().trim());
        persona.setEmail(email);
        persona.setUsuario(persona.getUsuario().trim());
        persona.setTelefono(persona.getTelefono().trim());
        
        personaRepo.save(persona);
    }

    public void actualizarPersona(Persona persona) {
        personaRepo.actualizar(persona);
    }

    public Persona login(String usuario, String contrasena) {
        // Validaciones básicas
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        
        return personaRepo.buscarPersonaPorUsuarioYContrasena(usuario.trim(), contrasena);
    }
}

