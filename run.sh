#!/usr/bin/env bash

NAME=${NAME:-pttg-ip-hmrc}
CACERT=/mnt/certs/local_EBSA_ROOT_CA.crt

[ -f "${CACERT}" ] && keytool -import -alias ca -file "${CACERT}" -keystore /app/truststore.jks -noprompt -storepass changeit -trustcacerts

keytool -importkeystore -destkeystore /app/truststore.jks -srckeystore /opt/jdk/jre/lib/security/cacerts -srcstorepass changeit -noprompt -storepass changeit &> /dev/null

JAR=$(find . -name ${NAME}*.jar|head -1)

java ${JAVA_OPTS} -Djavax.net.ssl.trustStore=/app/truststore.jks -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom -jar "${JAR}"