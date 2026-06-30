# INFORME TÉCNICO: AUTOMATIZACIÓN CI/CD, CONTENERIZACIÓN Y DESPLIEGUE CLOUD
## Asignatura: Introducción a Herramientas DevOps (ISY1101)
## Evaluación Final Transversal (EFT)

---

## 1. Método de Integración del Sistema

La solución implementada consiste en una arquitectura de microservicios desacoplada constituida por tres componentes principales que interactúan a través de protocolos estándar de red (HTTP/REST y conexiones TCP de base de datos):

```
       [ Client Browser ]
               │
               ▼ (HTTP Port 8080)
       ┌────────────────┐
       │   Frontend     │
       │ (Nginx/Static) │
       └───────┬────────┘
               │
       ┌───────┴───────────────┐
       │                       │
       ▼ (REST API Port 8081)  ▼ (REST API Port 8082)
┌──────────────┐        ┌──────────────┐
│  Backend JS  │        │  Backend PY  │
│ (User Serv.) │        │ (Prod. Serv) │
└──────┬───────┘        └──────┬───────┘
       │                       │
       └───────┐       ┌───────┘
               ▼       ▼ (MySQL TCP Port 3306)
        ┌─────────────────────┐
        │  MySQL Database     │
        │      (eval_db)      │
        └─────────────────────┘
```

### Flujo de Comunicación:
1. **Frontend (Generador Estático en Java/Servido por Nginx)**: El cliente descarga el código estático (HTML/CSS/JS) desde el servidor frontend. Una vez en el navegador, el código JavaScript realiza llamadas directas de tipo Fetch API a los microservicios backend utilizando URLs configuradas dinámicamente (`BACKEND_USERS_URL` y `BACKEND_PRODUCTS_URL`).
2. **Backend 1 (Microservicio de Usuarios en Node.js)**: Escucha peticiones en el puerto `8081`. Expone endpoints como `/api/users/register` y realiza operaciones de lectura/escritura en la base de datos MySQL en el puerto TCP `3306`.
3. **Backend 2 (Microservicio de Productos en Python/Flask)**: Escucha peticiones en el puerto `8082`. Expone el catálogo de productos a través de `/api/products` y gestiona los registros en la base de datos relacional.
4. **Base de Datos (MySQL)**: Centraliza la persistencia relacional en un esquema común denominado `eval_db` con tablas independientes para usuarios (`users`) y productos (`products`).

---

## 2. Contenerización y Orquestación Local (Docker & Docker Compose)

Para homogeneizar el entorno de desarrollo y simplificar el despliegue en producción, cada componente se ha contenerizado bajo buenas prácticas de optimización de imágenes (hardening):

### Estructura de Contenedores:
* **Frontend**: Usa un diseño **multi-stage**. La primera etapa utiliza `maven:3.9-eclipse-temurin-17-alpine` para compilar el software y empaquetar el generador estático. La segunda etapa monta un servidor de producción ultra liviano usando `nginx:alpine`, exponiendo únicamente el puerto `80`.
* **Backend JS**: Contenedor basado en `node:20-alpine`. Se separa la instalación de dependencias (`npm ci --only=production`) de la copia del código de la aplicación. Para mayor seguridad, el proceso no se ejecuta como root sino con el usuario del sistema `node`.
* **Backend Python**: Emplea la imagen `python:3.11-slim` por su reducido tamaño. Los paquetes se instalan sin caché de pip (`--no-cache-dir`). Se configura un usuario restringido `appuser` para ejecutar el servidor Flask.

### Orquestación Local (Docker Compose):
El archivo `docker-compose.yml` unifica todos los servicios mediante una red bridge interna llamada `devops-network`:
* Se expone el Frontend en el puerto host `8080`.
* Se implementa un **Healthcheck** en el contenedor MySQL para garantizar que los backends solo inicien una vez que la base de datos esté lista para recibir conexiones (`depends_on` con condición `service_healthy`).

---

## 3. Registro de Imágenes y Flujo de Publicación

El registro elegido para el almacenamiento y la trazabilidad de imágenes de contenedores es **Docker Hub** (configurable alternativamente a **Amazon ECR**). 

### Estrategia de Versionamiento y Trazabilidad (Tags):
Con el fin de asegurar la reproducibilidad y auditoría de la infraestructura, el pipeline etiqueta cada imagen construida con dos tags de forma paralela:
1. `latest`: Utilizado para identificar siempre la última versión estable construida desde la rama principal.
2. `github.sha` (por ejemplo, `front-ex-devops:c7f02494`): Un tag inmutable que coincide directamente con el hash SHA del commit de Git que originó la compilación. Esto permite vincular de manera unívoca cada contenedor desplegado en la nube con la línea exacta del código fuente.

---

## 4. Pipeline de Integración y Entrega Continua (CI/CD)

Cada repositorio contiene su propia configuración de flujo automatizado mediante **GitHub Actions** en `.github/workflows/ci-cd.yml`:

```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│ Trigger: Push │ ──> │ Stage 1: Test │ ──> │ Stage 2: Push │ ──> │ Stage 3: Deploy│
│    (main)     │     │ (Maven/Node/Py)│     │ (Docker Hub)  │     │ (AWS ECS)     │
└───────────────┘     └───────────────┘     └───────────────┘     └───────────────┘
```

### Etapas detalladas del Pipeline:
1. **Stage 1 (Test / Build Check)**:
   * Frontend: Configura Java 17 y compila el proyecto con Maven para detectar fallos de compilación.
   * Backend JS: Instala dependencias y valida la sintaxis de JavaScript con `node --check server.js`.
   * Backend Python: Instala requerimientos y ejecuta `python -m py_compile app.py`.
2. **Stage 2 (Docker Build & Push)**:
   * Inicia sesión en Docker Hub mediante credenciales secretas.
   * Construye la imagen Docker utilizando caché distribuido (`type=gha`) para optimizar tiempos.
   * Publica la imagen en el registro con las etiquetas `latest` y `${{ github.sha }}`.
3. **Stage 3 (Deploy Automatizado)**:
   * Configura las credenciales de AWS de forma segura.
   * Genera dinámicamente una nueva definición de tarea de ECS (Task Definition) actualizando el campo `image` con la versión exacta del commit.
   * Despliega la definición de tareas actualizada en el servicio AWS ECS y monitoriza la estabilidad del nuevo contenedor.

---

## 5. Infraestructura en la Nube (Arquitectura AWS)

Para la producción, se ha diseñado una infraestructura de alta disponibilidad basada en servicios administrados de AWS:

```
[ Internet ] ──> [ ALB (Application Load Balancer) ]
                       │
                       ├──────────────────────┐ (Ruta /api/users/*)
                       │                      ▼
                 [ VPC Public Subnets (ECS Fargate Services) ]
                       │                      │
                       ▼ (Ruta /*)            ▼ (Ruta /api/products/*)
                ┌──────────────┐       ┌──────────────┐
                │  Frontend    │       │  Backend JS  │
                │  Container   │       │  Container   │
                └──────────────┘       └──────┬───────┘
                       ▲                      │
                       │                      ▼ (Ruta BD)
                       │               ┌──────────────┐
                       └───────────────┤  Backend PY  │
                                       │  Container   │
                                       └──────┬───────┘
                                              │
                                              ▼ (MySQL DB)
                                       ┌──────────────┐
                                       │  Amazon RDS  │
                                       │ (Single-AZ)  │
                                       └──────────────┘
```

* **Red y Topología**: Despliegue en una **VPC** dedicada con subredes públicas (para el Balanceador de Carga y el frontend) y privadas (para microservicios backend y base de datos).
* **Seguridad (Security Groups)**:
  * El Load Balancer solo acepta tráfico en puertos `80`/`443`.
  * Los contenedores backend en ECS solo aceptan tráfico proveniente del Security Group del Load Balancer.
  * La base de datos RDS MySQL solo acepta tráfico TCP en el puerto `3306` desde los Security Groups de los microservicios backend.
* **Orquestación**: **AWS ECS con Fargate (Serverless)**, eliminando la necesidad de gestionar instancias EC2 y automatizando el aprovisionamiento de CPU y memoria.

---

## 6. Configuración y Gestión de Secretos

Para evitar la fuga de credenciales sensibles y cumplir con el principio de mínimo privilegio:
* **Entornos Locales**: Se utiliza el archivo `.env` (excluido en `.gitignore`) para configurar variables de desarrollo.
* **Flujos de CI/CD (GitHub Secrets)**: Las credenciales críticas (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `DOCKERHUB_TOKEN`) se almacenan de manera encriptada en la bóveda de secretos de GitHub.
* **Entorno de AWS**: Las contraseñas de bases de datos y cadenas de conexión se inyectan en las tareas de ECS leyendo directamente desde **AWS Secrets Manager** o cargando de manera segura las variables de entorno asociadas a roles de ejecución IAM de ECS que tienen permisos explícitos de lectura limitados.

---

## 7. Observabilidad

El sistema dispone de dos capas de monitoreo esenciales:
1. **GitHub Actions logs**: Proporciona trazabilidad completa en tiempo real de cada paso de construcción, prueba estática y despliegue del pipeline.
2. **AWS CloudWatch**: Los contenedores desplegados en ECS envían sus flujos de salida estándar (`stdout`/`stderr`) a CloudWatch Logs usando el controlador de registros `awslogs`. Asimismo, se monitorizan métricas críticas de infraestructura (uso de CPU y memoria por servicio) para detectar cuellos de botella o necesidad de autoescalado.

---

## 8. Seguridad Básica (Image Hardening)

* **Imágenes Base**: Reducción drástica del espacio de almacenamiento y del número de paquetes instalados usando imágenes del tipo `alpine` y `slim`.
* **Reducción de Privilegios**: Ningún contenedor corre con privilegios `root` en su proceso principal.
* **Restricción de Puertos**: Cada contenedor expone estrictamente un único puerto y las comunicaciones inter-servicios se aíslan dentro de la VPC.

---

## 9. Fundamentación de Orquestación y Escalabilidad

Se seleccionó **AWS ECS (Fargate)** frente a un despliegue manual en servidores tradicionales (EC2) o VPS por las siguientes razones:
1. **Escalabilidad Horizontal Automática**: ECS permite escalar el número de réplicas de los contenedores en cuestión de segundos en base al uso de CPU o número de conexiones HTTP.
2. **Cero Mantenimiento de Servidores**: Fargate abstrae la administración de parches del sistema operativo host, reduciendo costos operativos.
3. **Despliegues sin Interrupción (Rolling Updates)**: ECS gestiona actualizaciones progresivas, iniciando el contenedor nuevo y validando su salud antes de apagar el contenedor antiguo, asegurando cero tiempo de inactividad de la aplicación.
