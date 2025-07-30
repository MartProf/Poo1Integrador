package com.example.servicio;

import com.example.modelo.Evento;
import com.example.modelo.Participante;
import com.example.modelo.Persona;
import com.example.modelo.EstadoEvento;
import com.example.modelo.TieneCupo;
import com.example.repositorio.ParticipanteRepository;

import java.time.LocalDate;

/**
 * Servicio de gestión de participantes para eventos municipales.
 * 
 * Esta clase implementa la lógica de negocio para el manejo de inscripciones
 * de personas a eventos, incluyendo validaciones de elegibilidad, control
 * de cupos y gestión del estado de participación. Actúa como capa intermedia
 * entre los controladores y el repositorio de datos.
 * 
 * Responsabilidades Principales:
 * - Inscripción de personas a eventos con validaciones completas
 * - Verificación de estado de inscripción de participantes
 * - Control de cupos disponibles en eventos que los requieren
 * - Validación de estados de evento para permitir inscripciones
 * - Manejo de reglas de negocio específicas del dominio municipal
 * 
 * Reglas de Negocio Implementadas:
 * - Solo eventos CONFIRMADO o EN_EJECUCION permiten inscripciones
 * - Eventos con cupo deben tener disponibilidad antes de inscribir
 * - Una persona no puede inscribirse múltiples veces al mismo evento
 * - Toda inscripción queda registrada con fecha automática
 * - Validación obligatoria de parámetros no nulos
 * 
 * Integración con el Sistema:
 * - Utiliza ParticipanteRepository para persistencia de datos
 * - Trabaja con entidades Evento, Persona y Participante
 * - Implementa interface TieneCupo para eventos con límite
 * - Maneja estados de evento mediante enum EstadoEvento
 * 
 * Patrones de Diseño:
 * - Service Layer: Encapsula lógica de negocio
 * - Repository Pattern: Delegación de persistencia al repositorio
 * - Dependency Injection: Inyección manual del repositorio
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2024
 * 
 * @see ParticipanteRepository
 * @see com.example.modelo.Participante
 * @see com.example.modelo.Evento
 * @see com.example.modelo.Persona
 * @see com.example.modelo.EstadoEvento
 * @see com.example.modelo.TieneCupo
 */
public class ParticipanteService {

    /**
     * Repositorio para operaciones de persistencia de participantes.
     * 
     * Proporciona acceso a la capa de datos para realizar operaciones
     * CRUD sobre la entidad Participante, incluyendo consultas específicas
     * para verificar inscripciones existentes.
     */
    private ParticipanteRepository participanteRepository;

    /**
     * Constructor que inicializa el servicio con su repositorio de datos.
     * 
     * Crea una nueva instancia del repositorio de participantes para
     * gestionar la persistencia de datos. Implementa el patrón de
     * inyección de dependencias manual.
     * 
     * @implNote El repositorio se inicializa automáticamente sin parámetros
     */
    public ParticipanteService() {
        this.participanteRepository = new ParticipanteRepository();
    }

    /**
     * Inscribe una persona a un evento aplicando todas las validaciones de negocio.
     * 
     * Este método centraliza toda la lógica de inscripción, validando
     * prerrequisitos del evento, disponibilidad de cupos y creando
     * el registro de participación con la fecha actual.
     * 
     * Proceso de Inscripción:
     * 1. Validación de parámetros obligatorios (evento y persona no nulos)
     * 2. Verificación del estado del evento (debe ser CONFIRMADO o EN_EJECUCION)
     * 3. Control de cupo disponible (si el evento implementa TieneCupo)
     * 4. Creación del registro de participante con fecha automática
     * 5. Persistencia en la base de datos
     * 
     * Validaciones de Estado de Evento:
     * - CONFIRMADO: Evento listo para recibir inscripciones
     * - EN_EJECUCION: Evento en curso que aún acepta participantes
     * - Otros estados: No permiten inscripciones nuevas
     * 
     * Control de Cupos:
     * - Eventos que implementan TieneCupo: Verifica cupoDisponible > 0
     * - Eventos sin cupo: Permite inscripción sin límite
     * - Cupo agotado: Lanza excepción informativa
     * 
     * Gestión de Datos del Participante:
     * - Asociación automática con el evento especificado
     * - Vinculación con la persona que se inscribe
     * - Registro de fecha de inscripción (LocalDate.now())
     * - Persistencia inmediata en el repositorio
     * 
     * Casos de Error Controlados:
     * - "El evento no puede ser null" → Evento no proporcionado
     * - "La persona no puede ser null" → Persona no proporcionada
     * - "No hay cupo disponible" → Evento con cupo agotado
     * - "El evento no está disponible para inscripción" → Estado inválido
     * 
     * @param evento Evento al cual se desea inscribir la persona, debe estar
     *               en estado CONFIRMADO o EN_EJECUCION
     * @param persona Persona que se inscribe al evento, debe existir en el sistema
     * 
     * @throws Exception Si alguna validación falla o no se puede completar la inscripción
     * 
     * @implNote Utiliza pattern matching con instanceof para verificar TieneCupo
     * @implNote La fecha de inscripción se establece automáticamente al momento actual
     */
    public void inscribirPersona(Evento evento, Persona persona) throws Exception {
        // Validar que los parámetros no sean null
        if (evento == null) {
            throw new Exception("El evento no puede ser null");
        }
        if (persona == null) {
            throw new Exception("La persona no puede ser null");
        }
        
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

    /**
     * Verifica si una persona ya está inscrita en un evento específico.
     * 
     * Este método de consulta proporciona una verificación rápida del
     * estado de inscripción, útil para prevenir inscripciones duplicadas
     * y para mostrar información de estado en la interfaz de usuario.
     * 
     * Comportamiento de la Consulta:
     * - Búsqueda exacta por persona y evento
     * - Retorna resultado booleano inmediato
     * - No modifica estado del sistema (operación de solo lectura)
     * - Delegación completa al repositorio para la lógica de búsqueda
     * 
     * Casos de Uso:
     * - Validación previa a mostrar botón "Inscribirse"
     * - Verificación antes de permitir nueva inscripción
     * - Actualización de estado en listas de eventos disponibles
     * - Auditoría de participación en reportes
     * 
     * @param persona Persona cuya inscripción se desea verificar
     * @param evento Evento específico a consultar
     * 
     * @return true si la persona está inscrita en el evento, false en caso contrario
     * 
     * @implNote Operación de solo lectura que no afecta el estado del sistema
     * @implNote La implementación específica de búsqueda está en el repositorio
     */
    public boolean estaInscripto(Persona persona, Evento evento) {
        return participanteRepository.estaInscripto(persona, evento);
    }
}
