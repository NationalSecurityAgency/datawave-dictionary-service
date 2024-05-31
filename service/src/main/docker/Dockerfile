FROM azul/zulu-openjdk-alpine:11

LABEL version=${project.version} \
      run="docker run ${docker.image.prefix}${project.artifactId}:latest" \
      description="${project.description}"

ADD ${project.build.finalName}-exec.jar /app.jar
RUN apk --no-cache add libc6-compat curl && chmod 777 /app.jar

EXPOSE 8443 8080
ENTRYPOINT ["java","-jar","app.jar"]