package com.example.servicio;

import com.example.modelo.Evento;
import com.example.modelo.Persona;
import com.example.repositorio.EventoRepository;

import java.util.List;

public class EventoService {

    private EventoRepository eventoRepo;

    public EventoService() {
        this.eventoRepo = new EventoRepository();
    }

    public List<Evento> getEventosDisponibles() {
        return eventoRepo.findAllDisponibles();
    }

    public List<Evento> getTodosLosEventos() {
        return eventoRepo.findAllWithRelations();
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

    public void actualizarEvento(Evento evento) {
        eventoRepo.actualizarEvento(evento);
    }

    public boolean esResponsableDelEvento(Persona persona, Evento evento) {
        return eventoRepo.esResponsable(persona, evento);
    }
    
    // Aquí irían reglas de negocio más complejas
}
