FROM openjdk:8-jdk-alpine as build
WORKDIR /workspace/app

COPY backend/target/*.jar .

RUN mkdir -p dependency && (cd dependency; jar -xf ../*.jar)

FROM metersphere/fabric8-java-alpine-openjdk8-jre

LABEL maintainer="FIT2CLOUD <support@fit2cloud.com>"

ARG MS_VERSION=dev
ARG DEPENDENCY=/workspace/app/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

COPY backend/target/classes/jmeter/ /app/jmeter/

RUN mkdir -p /opt/jmeter/lib/junit
COPY backend/target/classes/jmeter/ /opt/jmeter/

ENV JAVA_CLASSPATH=/app:/app/lib/*
ENV JAVA_MAIN_CLASS=io.metersphere.Application
ENV AB_OFF=true
ENV MS_VERSION=${MS_VERSION}
ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Djava.awt.headless=true -javaagent:/app/file/polaris-agent.jar -Djava._appid_=int-website-arch-autotest-metersphere -Djava._environment_=work -server -Xms8g -Xmx8g -XX:NewRatio=1 -XX:SurvivorRatio=3  -XX:permSize=256M -XX:MaxPermSize=256M -XX:-CMSIncrementalMode -XX:-UseConcMarkSweepGC -XX:+UseG1GC -XX:-UseParNewGC -XX:MaxGCPauseMillis=200 -verbose:gc -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintGCTimeStamps -Xss256k -XX:+UseParallelGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp "

CMD ["/deployments/run-java.sh"]
