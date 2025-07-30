package com.example.modelo;

/**
 * Enumeración que define las modalidades de realización de un evento.
 * 
 * Esta enumeración permite clasificar los eventos según su forma de
 * ejecución, especialmente relevante en el contexto post-pandemia donde
 * las actividades municipales pueden adaptarse a diferentes formatos
 * para garantizar la accesibilidad y participación ciudadana.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Taller
 */
public enum Modalidad {

    /**
     * Modalidad presencial donde el evento se realiza físicamente.
     * 
     * En esta modalidad:
     * - Los participantes deben asistir al lugar físico del evento
     * - Se requiere gestión de espacios y capacidad física
     * - Permite interacción directa entre participantes y organizadores
     * - Necesita consideraciones logísticas de transporte y acceso
     * - Aplicable a la mayoría de eventos culturales tradicionales
     */
    PRESENCIAL,
    
    /**
     * Modalidad virtual donde el evento se realiza de forma remota.
     * 
     * En esta modalidad:
     * - Los participantes acceden desde sus hogares vía internet
     * - Se requiere plataforma tecnológica y conectividad
     * - Permite mayor alcance geográfico sin limitaciones de espacio
     * - Reduce costos logísticos y barreras de acceso físico
     * - Especialmente útil para talleres, conferencias y eventos educativos
     */
    VIRTUAL,
}
