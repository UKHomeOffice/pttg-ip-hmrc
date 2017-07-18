FROM quay.io/ukhomeofficedigital/openjdk8:v1.0.0


ENV USER pttg
ENV GROUP pttg
ENV NAME pttg-ip-hmrc
ENV JAR_PATH build/libs

ARG VERSION

WORKDIR /app

RUN groupadd -r ${GROUP} && \
    useradd -r -g ${USER} ${GROUP} -d /app && \
    mkdir -p /app && \
    chown -R ${USER}:${GROUP} /app

COPY ${JAR_PATH}/${NAME}*.jar /app
COPY run.sh /app

RUN chmod a+x /app/run.sh

EXPOSE 8081

USER pttg

ENTRYPOINT /app/run.sh
