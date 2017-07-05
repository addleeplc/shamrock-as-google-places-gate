#!/bin/sh

RMI_SERVER_HOSTNAME=`hostname`
if [ -z ${PARENT_HOSTNAME} ]; then
    RMI_SERVER_HOSTNAME=`hostname`;
else
    RMI_SERVER_HOSTNAME=${PARENT_HOSTNAME};
fi

"${JAVA_HOME}/bin/java" -Xmx256m -Xdebug -agentlib:jdwp=transport=dt_socket,address=GOOGLE_PROVIDER_DEBUG_PORT,server=y,suspend=n\
 -Dcom.sun.management.jmxremote\
 -Dcom.sun.management.jmxremote.port=GOOGLE_PROVIDER_JMX_PORT\
 -Dcom.sun.management.jmxremote.rmi.port=GOOGLE_PROVIDER_JMX_PORT\
 -Djava.rmi.server.hostname=${RMI_SERVER_HOSTNAME}\
 $JAVA_PROPERTIES\
 -Dcom.sun.management.jmxremote.local.only=false\
 -Dcom.sun.management.jmxremote.authenticate=false\
 -Dcom.sun.management.jmxremote.ssl=false\
 -Dcom.sun.management.jmxremote.password.file=jmxremote.password\
 -Dcom.sun.management.jmxremote.access.file=jmxremote.access\
 -Djetty.base=../jetty_base -jar ../jetty_home/start.jar
exit