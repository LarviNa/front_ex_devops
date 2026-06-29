# Frontend - Gestión de Productos (Contenedores & CI/CD)

Este es el componente Frontend para la **Evaluación Final Transversal (EFT)** de la asignatura **Introducción a Herramientas Devops**. Genera una página estática HTML conectada con microservicios en segundo plano.

---

## Contenerización (Docker)

Este proyecto está preparado para ejecutarse de manera autónoma en contenedores Docker mediante una estructura **multi-etapa (multi-stage)** para garantizar la eficiencia y seguridad en producción:

1. **Etapa 1: Compilación**: Utiliza la imagen oficial de `maven:3.9-eclipse-temurin-17-alpine` para compilar el código de Java y generar los archivos estáticos en `/app/output`.
2. **Etapa 2: Servidor Web**: Copia la salida en un contenedor minimalista y optimizado de `nginx:alpine` para servir el sitio estático de forma segura en el puerto `80`.

### Construcción Local de la Imagen

Para construir la imagen de manera independiente:
```bash
docker build \
  --build-arg BACKEND_USERS_URL=http://localhost:8081 \
  --build-arg BACKEND_PRODUCTS_URL=http://localhost:8082 \
  -t frontend:latest .
```

### Ejecutar Contenedor
```bash
docker run -d -p 8080:80 frontend:latest
```
El sitio estará accesible en: `http://localhost:8080`

---

## Orquestación Local con Docker Compose

Para orquestar este servicio junto con los microservicios y la base de datos MySQL de forma integrada, utiliza el archivo `docker-compose.yml` de la raíz del proyecto global:

```bash
# Desde la raíz del workspace:
docker compose up --build
```

---

## GitHub Actions - Pipeline CI/CD

El flujo automatizado se ejecuta con cada push a la rama `main` y realiza los siguientes pasos:

1. **Compilación y Test**: Valida la construcción en Java.
2. **Docker Build & Push**: Construye la imagen de producción y la publica en el registro Docker Hub (utilizando etiquetas semánticas y el SHA del commit).
3. **AWS ECS Deploy**: Actualiza de forma segura la definición de tareas (Task Definition) e implementa el servicio en AWS ECS Fargate.

### Configuración de Secretos en GitHub

Asegúrate de agregar los siguientes secretos a tu repositorio en GitHub (`Settings > Secrets and variables > Actions`):

* `DOCKERHUB_USERNAME`: Tu usuario de Docker Hub.
* `DOCKERHUB_TOKEN`: Token de acceso seguro de Docker Hub.
* `AWS_ACCESS_KEY_ID`: Credenciales de acceso de AWS IAM.
* `AWS_SECRET_ACCESS_KEY`: Clave secreta de acceso de AWS IAM.
* `BACKEND_USERS_URL`: Endpoint público de la API de Usuarios.
* `BACKEND_PRODUCTS_URL`: Endpoint público de la API de Productos.
