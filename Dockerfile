# Stage 1: Build stage (Maven)
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build arguments for backend API URLs
ARG BACKEND_USERS_URL=http://localhost:8081
ARG BACKEND_PRODUCTS_URL=http://localhost:8082

# Generate .env file dynamically from build args
RUN echo "BACKEND_USERS_URL=${BACKEND_USERS_URL}" > .env && \
    echo "BACKEND_PRODUCTS_URL=${BACKEND_PRODUCTS_URL}" >> .env

# Run Java generator to output static index.html, styles.css, script.js
RUN mvn clean compile exec:java

# Stage 2: Serve stage (Nginx)
FROM nginx:alpine

# Copy the static page outputs to Nginx web root
COPY --from=builder /app/output /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
