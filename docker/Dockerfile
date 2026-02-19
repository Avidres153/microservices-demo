FROM eclipse-temurin:21-jre-jammy

ARG APP_NAME
RUN echo "app dir: $APP_DIR"
RUN echo "app name: $APP_NAME"

WORKDIR /app
COPY tmp/${APP_NAME} app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]