cd mwg.wb.client
mvn install:install-file -Dfile=KVClient.jar -DgroupId=com.oracle.kv -DartifactId=kvclient -Dversion=10 -Dpackaging=jar
cd ../mwg.wb.injection
mvn install:install-file -Dfile=jdbc-oracle.jar -DgroupId=com.oracle -DartifactId=ojdbc10 -Dversion=10 -Dpackaging=jar
mvn install:install-file -Dfile=oraclepki.jar -DgroupId=com.oracle -DartifactId=oraclepki -Dversion=3 -Dpackaging=jar
mvn install:install-file -Dfile=osdt_cert.jar -DgroupId=com.oracle -DartifactId=osdt_cert -Dversion=3 -Dpackaging=jar
mvn install:install-file -Dfile=osdt_core.jar -DgroupId=com.oracle -DartifactId=osdt_core -Dversion=3 -Dpackaging=jar