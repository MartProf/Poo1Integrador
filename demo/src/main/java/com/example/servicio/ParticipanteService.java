package com.example.servicio;

import com.example.modelo.Evento;
import com.example.modelo.Participante;
import com.example.modelo.Persona;
import com.example.modelo.EstadoEvento;
import com.example.modelo.TieneCupo;
import com.example.repositorio.ParticipanteRepository;

import java.time.LocalDate;

public class ParticipanteService {

    private ParticipanteRepository participanteRepository;

    public ParticipanteService() {
        this.participanteRepository = new ParticipanteRepository();
    }

    public void inscribirPersona(Evento evento, Persona persona) throws Exception {
        if (evento.getEstado() == EstadoEvento.CONFIRMADO || evento.getEstado() == EstadoEvento.EN_EJECUCION) {

            if (evento instanceof TieneCupo tieneCupo) {
                if (tieneCupo.getCupoDisponible() <= 0) {
                    throw new Exception("No hay cupo disponible");
                }
            }

            Participante participante = new Participante();
            participante.setEvento(evento);
            participante.setPersona(persona);
            participante.setFechaincripción(LocalDate.now());

            participanteRepository.save(participante);

        } else {
            throw new Exception("El evento no está disponible para inscripción");
        }
    }

    public boolean estaInscripto(Persona persona, Evento evento) {
        return participanteRepository.estaInscripto(persona, evento);
    }
}
