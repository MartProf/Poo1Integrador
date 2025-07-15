package com.example.servicio;

import com.example.modelo.Evento;
import com.example.modelo.Persona;
import com.example.repositorio.EventoRepository;

import jakarta.persistence.EntityManager;
import java.util.List;

public class EventoService {

    private EventoRepository eventoRepo;

    public EventoService(EntityManager em) {
        this.eventoRepo = new EventoRepository(em);
    }

    public List<Evento> getEventosDisponibles() {
        return eventoRepo.findAllDisponibles();
    }

    public List<Evento> getEventosPorResponsable(Persona responsable) {
        return eventoRepo.findByResponsable(responsable);
    }

    public void guardarEvento(Evento evento) {
        eventoRepo.save(evento);
    }

    public void eliminarEvento(Evento evento) {
        eventoRepo.delete(evento);
    }

    public Evento getEventoById(int id) {
        return eventoRepo.findById(id);
    }

    // Aquí irían reglas de negocio más complejas
}
