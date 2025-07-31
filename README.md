# Sistema Municipal de Eventos

## 📋 Descripción del Proyecto

Sistema integral de gestión de eventos municipales desarrollado en JavaFX que permite la administración completa de eventos culturales y recreativos para la municipalidad. El sistema proporciona una plataforma robusta para crear, gestionar y monitorear diferentes tipos de eventos con funcionalidades avanzadas de búsqueda, selección de participantes y manejo de datos.

## 👥 Integrantes del Equipo

- **Ramos Federico**
- **Maidana Martin** 
- **Nuñez Gabriel**

## 🎯 Objetivos del Sistema

- Centralizar la gestión de eventos municipales
- Facilitar el registro y búsqueda de participantes
- Proporcionar herramientas intuitivas para la creación de eventos
- Mantener un registro completo de responsables y asistentes
- Ofrecer diferentes modalidades de eventos especializados

## 🏗️ Arquitectura del Sistema

### Patrón de Diseño
- **MVC (Model-View-Controller)**: Separación clara de responsabilidades
- **Repository Pattern**: Abstracción del acceso a datos
- **Service Layer**: Centralización de la lógica de negocio
- **Helper Pattern**: Utilidades para operaciones comunes

### Tecnologías Utilizadas
- **Java 24**: Lenguaje de programación principal
- **JavaFX**: Framework para interfaz gráfica moderna
- **JPA/Hibernate**: Persistencia de datos y ORM
- **PostgreSQL**: Base de datos relacional
- **Maven**: Gestión de dependencias y construcción
- **Lombok**: Reducción de código boilerplate

## 📁 Estructura del Proyecto

```
demo/
├── src/main/java/com/example/
│   ├── App.java                    # Aplicación principal JavaFX
│   ├── LanzadorApp.java           # Punto de entrada alternativo
│   ├── controlador/               # Controladores de vista
│   │   ├── AgregarPeliculasModalController.java
│   │   ├── BuscarPersonaController.java
│   │   ├── DashboardController.java
│   │   ├── EventosDisponiblesController.java
│   │   ├── MisEventosController.java
│   │   ├── NuevoEventoController.java
│   │   ├── PerfilController.java
│   │   ├── RegistroController.java
│   │   └── SesionController.java
│   ├── modelo/                    # Entidades del dominio
│   │   ├── CicloDeCine.java
│   │   ├── Concierto.java
│   │   ├── EstadoEvento.java
│   │   ├── Evento.java
│   │   ├── Exposicion.java
│   │   ├── Feria.java
│   │   ├── Modalidad.java
│   │   ├── Participante.java
│   │   ├── Pelicula.java
│   │   ├── Persona.java
│   │   ├── Taller.java
│   │   └── TieneCupo.java
│   ├── repositorio/               # Acceso a datos
│   │   ├── EventoRepository.java
│   │   ├── ParticipanteRepository.java
│   │   ├── PersonaRepository.java
│   │   └── Repositorio.java
│   ├── servicio/                  # Lógica de negocio
│   │   ├── EventoService.java
│   │   ├── ParticipanteService.java
│   │   ├── PersonaService.java
│   │   └── Servicio.java
│   └── util/                      # Utilidades
│       ├── BuscarPersonaModalHelper.java
│       └── JpaUtil.java
├── src/main/resources/
│   ├── com/example/               # Archivos FXML
│   │   ├── BuscarPersona.fxml
│   │   ├── dashboard.fxml
│   │   ├── eventos_disponibles.fxml
│   │   ├── eventos.fxml
│   │   ├── misEventos.fxml
│   │   ├── nuevoEvento.fxml
│   │   ├── perfil.fxml
│   │   ├── registro.fxml
│   │   ├── sesion.fxml
│   │   └── ventanaAgregarPeliculas.fxml
│   └── META-INF/
│       └── persistence.xml        # Configuración JPA
└── pom.xml                        # Configuración Maven
```

## 🎭 Tipos de Eventos Soportados

### 1. **Conciertos**
- Gestión de artistas participantes
- Modalidades: Presencial/Virtual
- Control de charlas adicionales

### 2. **Exposiciones**
- Asignación de curadores especializados
- Definición de tipo de arte
- Entrada gratuita opcional

### 3. **Talleres**
- Control de cupo máximo
- Asignación de instructores
- Gestión de inscripciones

### 4. **Ferias**
- Cantidad de stands disponibles
- Modalidad al aire libre
- Entrada gratuita configurable

### 5. **Ciclos de Cine**
- Gestión de películas por ciclo
- Orden de proyección
- Modalidades diversas

## 🔧 Funcionalidades Principales

### Gestión de Personas
- ✅ Registro completo de participantes
- ✅ Búsqueda avanzada con filtros múltiples
- ✅ Selección simple y múltiple mediante modales
- ✅ Validación de datos únicos (DNI, email)
- ✅ Autenticación de usuarios

### Gestión de Eventos
- ✅ Creación de eventos especializados por tipo
- ✅ Asignación de responsables múltiples
- ✅ Control de estados (Programado, En Curso, Finalizado, Cancelado)
- ✅ Configuración específica según tipo de evento
- ✅ Gestión de participantes e inscripciones

### Interfaz de Usuario
- ✅ Dashboard principal intuitivo
- ✅ Navegación fluida entre módulos
- ✅ Modales especializados para operaciones complejas
- ✅ Feedback visual en todas las operaciones
- ✅ Diseño responsivo y moderno

## 🗄️ Modelo de Datos

### Entidades Principales
- **Persona**: Datos básicos y autenticación
- **Evento**: Clase base con herencia por tipo
- **Participante**: Relación many-to-many entre Persona y Evento
- **Pelicula**: Específica para Ciclos de Cine

### Relaciones
- Persona ↔ Evento (many-to-many via Participante)
- Evento ↔ Persona (responsables many-to-many)
- CicloDeCine ↔ Pelicula (one-to-many)

## 🚀 Instalación y Configuración

### Requisitos Previos
- Java 17 o superior
- PostgreSQL 12+
- Maven 3.8+

### Base de Datos
1. Crear base de datos PostgreSQL
2. Configurar `persistence.xml` con credenciales
3. Las tablas se crean automáticamente via JPA

### Ejecución
```bash
# Compilar el proyecto
mvn compile

# Ejecutar la aplicación
mvn exec:java -Dexec.mainClass="com.example.LanzadorApp"
```

## 📊 Configuración de Base de Datos

El sistema utiliza JPA/Hibernate con configuración automática. Archivo `persistence.xml`:

```xml
<persistence-unit name="Municipalidad">
    <!-- Configuración automática de entidades -->
    <!-- Propiedades de conexión PostgreSQL -->
    <!-- Configuración Hibernate DDL -->
</persistence-unit>
```

## 🔍 Casos de Uso Principales

1. **Registro de Ciudadanos**: Captura completa de datos personales
2. **Creación de Eventos**: Wizard especializado por tipo de evento
3. **Búsqueda de Participantes**: Sistema avanzado con filtros múltiples
4. **Gestión de Inscripciones**: Asignación masiva o individual
5. **Seguimiento de Eventos**: Dashboard con estados y progreso


## 📈 Métricas del Proyecto

- **Clases Documentadas**: 15+ clases con JavaDoc completo
- **Líneas de Código**: ~3000+ LOC
- **Cobertura de Funcionalidades**: 90%+ casos de uso implementados
- **Tipos de Eventos**: 5 especializaciones completas
- **Controladores**: 9 controladores especializados

## 🔒 Seguridad y Validaciones

- Validación de datos de entrada en todos los formularios
- Autenticación básica de usuarios
- Prevención de duplicados (DNI, email únicos)
- Manejo robusto de errores y excepciones
- Transacciones seguras en operaciones de BD

## 🚧 Características Técnicas Avanzadas

### Gestión de Memoria
- Cierre automático de EntityManager
- Pooling de conexiones optimizado
- Lazy loading de relaciones pesadas

### Performance
- Consultas JPQL optimizadas
- Carga selectiva de datos según contexto
- Cache de segundo nivel configurado

### Mantenibilidad
- Documentación JavaDoc completa
- Código limpio y bien estructurado
- Separación clara de responsabilidades

## 📝 Documentación Técnica

El proyecto incluye documentación JavaDoc completa para todas las clases principales:
- Repositorios con estrategias de consulta
- Servicios con lógica de negocio detallada
- Controladores con flujos de interacción
- Utilidades con patrones de uso

## 🎓 Contexto Académico

**Asignatura**: Programación Orientada a Objetos I  
**Institución**: Facultad de Ciencias Exactas Quimicas y Naturales
**Año**: 2025  
**Tipo**: Proyecto Integrador  

## 📄 Licencia

Proyecto desarrollado con fines académicos para la asignatura de Programación Orientada a Objetos I.

---

**Desarrollado con ❤️ por el equipo de POO I - 2025**
