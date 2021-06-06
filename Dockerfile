ARG JAVA_VERSION=16
ARG JVM_FLAVOR=hotspot

FROM adoptopenjdk:${JAVA_VERSION}-jdk-${JVM_FLAVOR} AS builder
WORKDIR /builddir

COPY gradlew build.gradle settings.gradle ./
COPY gradle/ gradle/
RUN ./gradlew --no-daemon # Download gradle and init

COPY license*.txt ./
COPY src/ src/
RUN ./gradlew clean buildForDocker --no-daemon


ARG JAVA_VERSION
ARG JVM_FLAVOR

FROM adoptopenjdk:${JAVA_VERSION}-jre-${JVM_FLAVOR}
WORKDIR /app

RUN groupadd --system spring \
    && useradd --system spring --gid spring \
    && chown -R spring:spring /app
USER spring:spring

VOLUME /data/storage

# We override default config location search path,
# so that a custom file with defaults can be used
# Normally would use environment variables,
# but they take precedence over config file
# https://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/html/boot-features-external-config.html
ENV SPRING_CONFIG_LOCATION="optional:classpath:/,optional:classpath:/config/,file:./dockerdefaults.yaml,optional:file:./,optional:file:./config/"
COPY ./dockerdefaults.yaml ./dockerdefaults.yaml

COPY --from=builder /builddir/build/libs/docker/bibliothek.jar ./
CMD ["java", "-jar", "/app/bibliothek.jar"]
