#
# Jetty Logging Module
#   Output Managed by Log4j 1.2.x
#

[name]
logging

[depend]
resources

[lib]
lib/logging/*.jar

[files]
logs/
http://central.maven.org/maven2/org/slf4j/slf4j-api/1.7.13/slf4j-api-1.7.13.jar|lib/logging/slf4j-api-1.7.13.jar
http://central.maven.org/maven2/ch/qos/logback/logback-core/1.1.3/logback-core-1.1.3.jar|lib/logging/logback-core-1.1.3.jar
http://central.maven.org/maven2/ch/qos/logback/logback-classic/1.1.3/logback-classic-1.1.3.jar|lib/logging/logback-classic-1.0.7.jar

https://raw.githubusercontent.com/jetty-project/logging-modules/master/capture-all/logback.xml|resources/logback.xml