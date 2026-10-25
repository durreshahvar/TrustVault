FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends \
    libreoffice-writer libreoffice-calc libreoffice-impress \
    fonts-liberation fonts-crosextra-carlito fonts-crosextra-caladea \
    && rm -rf /var/lib/apt/lists/*
COPY . .
RUN chmod +x mvnw && ./mvnw -B package -DskipTests
EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar target/*.jar"]
