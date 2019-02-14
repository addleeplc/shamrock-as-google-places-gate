#
# Jetty Logging Module
#   Output Managed by Log4j 1.2.x
#

[tags]
logging

[depend]
resources

[lib]
lib/logging/*.jar

[files]
logs/
http://central.maven.org/maven2/org/slf4j/slf4j-api/1.7.7/slf4j-api-1.7.7.jar|lib/logging/slf4j-api-1.7.7.jar
http://central.maven.org/maven2/ch/qos/logback/logback-core/1.1.7/logback-core-1.1.7.jar|lib/logging/logback-core-1.1.7.jar
http://central.maven.org/maven2/ch/qos/logback/logback-classic/1.1.7/logback-classic-1.1.7.jar|lib/logging/logback-classic-1.1.7.jar
http://central.maven.org/maven2/org/perf4j/perf4j/0.9.10/perf4j-0.9.10.jar|lib/logging/perf4j-0.9.10.jar
https://raw.githubusercontent.com/jetty-project/logging-modules/master/log4j-1.2/jetty-logging.properties|resources/jetty-logging.properties
#https://raw.githubusercontent.com/jetty-project/logging-modules/master/log4j-1.2/log4j.properties|resources/log4j.properties