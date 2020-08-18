# shellcheck disable=SC2096
#/opt/apache-maven/bin/mvn clean install

#jarsigner -verify -verbose -certs target/benchmarks.jar
#jarsigner -verify target/benchmarks.jar
#java -cp target/benchmarks.jar tests.MapTestRunner


mvn exec:java  -Dexec.mainClass=ManualTests