FROM eclipse-temurin:17-jdk-alpine

ARG APP_NAME=vacancies-alert
ENV SERVICE_PATH /opt/${APP_NAME}
ENV JAR_NAME app.jar

RUN mkdir ${SERVICE_PATH}

COPY build/libs/${JAR_NAME} ${SERVICE_PATH}

WORKDIR ${SERVICE_PATH}
CMD exec java ${JAVA_OPTS} -jar ${JAR_NAME}
