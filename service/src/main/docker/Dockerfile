FROM openjdk:8-jdk-alpine

LABEL version=${project.version} \
      run="docker run ${docker.image.prefix}${project.artifactId}:latest" \
      description="${project.description}"

ADD ${project.build.finalName}-exec.jar /app.jar
RUN apk add libc6-compat curl

EXPOSE 8443 8080
ENTRYPOINT ["java","-jar","app.jar"]