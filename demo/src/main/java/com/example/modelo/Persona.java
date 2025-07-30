package com.example.modelo;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa a una persona en el sistema de gestión municipal.
 * 
 * Esta clase es central en el sistema ya que una persona puede desempeñar
 * múltiples roles dentro de los eventos municipales: puede ser responsable
 * de eventos, artista en conciertos, curador de exposiciones, instructor
 * de talleres, o simplemente participante inscripto en cualquier evento.
 * 
 * El DNI actúa como clave primaria natural, garantizando la unicidad de
 * cada persona en el sistema y facilitando la validación de duplicados
 * en diferentes contextos de la aplicación.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 * @see Concierto
 * @see Exposicion
 * @see Taller
 * @see Participante
 */
@Entity
@Table(name="Persona")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Persona {
    
    /**
     * Documento Nacional de Identidad de la persona.
     * 
     * Actúa como clave primaria natural del sistema, garantizando que
     * cada persona sea única y fácilmente identificable. Se utiliza
     * para validaciones de duplicados y como referencia en todas las
     * relaciones del sistema.
     * 
     * @implNote Debe ser un número de DNI válido y único en el sistema.
     *           No se permite modificar una vez establecido
     */
    @Id
    private int dni;
    
    /**
     * Nombre de la persona.
     * 
     * Almacena el o los nombres de pila de la persona para su
     * identificación en el sistema y comunicaciones oficiales.
     * 
     * @implNote No puede ser null ni vacío
     */
    private String nombre;
    
    /**
     * Apellido de la persona.
     * 
     * Almacena el apellido o apellidos de la persona para completar
     * su identificación formal en el sistema.
     * 
     * @implNote No puede ser null ni vacío
     */
    private String apellido;
    
    /**
     * Número de teléfono de contacto de la persona.
     * 
     * Medio de comunicación directa para notificaciones urgentes,
     * confirmaciones de eventos y comunicaciones administrativas.
     * 
     * @implNote Debe ser un número de teléfono válido, puede incluir
     *           código de área y caracteres separadores
     */
    private String telefono;
    
    /**
     * Dirección de correo electrónico de la persona.
     * 
     * Principal medio de comunicación para notificaciones automáticas,
     * confirmaciones de inscripción, recordatorios de eventos y
     * comunicaciones oficiales del municipio.
     * 
     * @implNote Debe ser una dirección de email válida y única en el sistema
     */
    private String email;
    
    /**
     * Nombre de usuario para acceso al sistema.
     * 
     * Identificador único que utiliza la persona para iniciar sesión
     * en la plataforma y acceder a sus funcionalidades según su rol.
     * 
     * @implNote Debe ser único en el sistema y no modificable una vez establecido
     */
    private String usuario;
    
    /**
     * Contraseña encriptada para autenticación en el sistema.
     * 
     * Credencial de seguridad que permite verificar la identidad de la
     * persona al acceder al sistema. Se almacena de forma encriptada
     * para garantizar la seguridad de los datos.
     * 
     * @implNote Debe cumplir con políticas de seguridad establecidas
     *           y almacenarse siempre encriptada
     */
    private String contrasena;

    /**
     * Lista de eventos donde esta persona actúa como responsable.
     * 
     * Relación Many-to-Many bidireccional que permite que una persona
     * sea responsable de múltiples eventos y que un evento tenga
     * múltiples responsables. Los responsables tienen permisos para
     * modificar y gestionar los eventos asignados.
     * 
     * @see Evento#responsables
     */
    @ManyToMany(mappedBy = "responsables")
    private List<Evento> eventosResponsable;

    /**
     * Lista de conciertos donde esta persona actúa como artista.
     * 
     * Relación Many-to-Many bidireccional que registra todos los
     * conciertos en los que esta persona participa como músico,
     * cantante o performer. Permite el seguimiento de la carrera
     * artística dentro del sistema municipal.
     * 
     * @see Concierto#artistas
     */
    @ManyToMany(mappedBy = "artistas")
    private List<Concierto> conciertosComoArtista;

    /**
     * Lista de exposiciones donde esta persona actúa como curador.
     * 
     * Relación One-to-Many que registra todas las exposiciones bajo
     * la curaduría de esta persona. Un curador puede dirigir múltiples
     * exposiciones, pero cada exposición tiene un curador principal.
     * 
     * @see Exposicion#curador
     */
    @OneToMany(mappedBy = "curador")
    private List<Exposicion> exposicionesComoCurador;

    /**
     * Lista de talleres donde esta persona actúa como instructor.
     * 
     * Relación One-to-Many que registra todos los talleres dirigidos
     * por esta persona. Un instructor puede dictar múltiples talleres,
     * pero cada taller tiene un instructor principal.
     * 
     * @see Taller#instructor
     */
    @OneToMany(mappedBy = "instructor")
    private List<Taller> talleresComoInstructor;

    /**
     * Lista de participaciones como inscripto en eventos.
     * 
     * Relación One-to-Many que registra todas las inscripciones de
     * esta persona a diferentes eventos municipales. Permite el
     * seguimiento del historial de participación ciudadana.
     * 
     * @see Participante#persona
     */
    @OneToMany(mappedBy = "persona")
    private List<Participante> participaciones;

    /**
     * Representación textual de la persona.
     * 
     * @return string con el formato "Nombre Apellido" para identificación
     *         rápida en interfaces de usuario y listados
     */
    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}
    