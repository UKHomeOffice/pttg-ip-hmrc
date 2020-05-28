#!/usr/bin/env bash

NAME=${NAME:-pttg-ip-hmrc}
JAR=$(find . -name ${NAME}*.jar|head -1)

if [ -z ${ACP_MODE} ]; then
  echo "EBSA Deployment"
  java ${JAVA_OPTS} -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom -jar "${JAR}"
else
  echo "ACP Deployment"

  certfiles=$(awk '/-----BEGIN CERTIFICATE-----/{filename="acpca-a-"NR; print filename}; {print >filename}' /certs/acp-root.crt)

  echo 'Add the ACP Root CA certs'

  for file in ${certfiles}; do
    echo -n '.'
    keytool -import -alias "${file}" -file "${file}" -keystore ./truststore.jks -noprompt -storepass changeit -trustcacerts > /dev/null
    rm "${file}"
  done

  certfiles=$(awk '/-----BEGIN CERTIFICATE-----/{filename="acpca-b-"NR; print filename}; {print >filename}' /certs/ca-certificates.crt)

  echo 'Now add the Commercial Root CA certs'

  cacount=0

  for file in ${certfiles}; do
    echo -n '.'
    keytool -import -alias "${file}" -file "${file}" -keystore ./truststore.jks -noprompt -storepass changeit -trustcacerts 2> /dev/null
    rm "${file}"
    let "cacount++"
  done

  echo "Trustore has been created with ${cacount} certificates, now start the app ... "

  java ${JAVA_OPTS} -Djavax.net.ssl.trustStore=/app/truststore.jks \
                  -Dcom.sun.management.jmxremote.local.only=false \
                  -Djava.security.egd=file:/dev/./urandom -jar "${JAR}"
fi

