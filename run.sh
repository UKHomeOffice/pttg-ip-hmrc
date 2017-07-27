#!/usr/bin/env bash
NAME=${NAME:-pttg-ip-hmrc}

while [[ ! -z ${JDK_TRUST_FILE} && ! -f ${JDK_TRUST_FILE} ]]
do
  echo Wating for ${JDK_TRUST_FILE}
  sleep 2
done

sleep 10

JAR=$(find . -name ${NAME}*.jar|head -1)
java ${JAVA_OPTS} -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom -jar "${JAR}"

