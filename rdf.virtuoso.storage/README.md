# rdf.virtuoso.storage

This module includes an implementation of the storage SPI of Clerezza that connects to a Virtuoso instance.

## Build and Install
To build this project you need Maven. 

### Simple build

 $ mvn clean install

This module depends on:
* ext.virtuoso.jdbc : contains JDBC drivers form Virtuoso

Bundle is in the /target folder:
* rdf.virtuoso.storage/target/rdf.virtuoso.storage-<version>.jar

### Build forcing tests
You must have a Virtuoso running server to do tests.
Tests are skipped by default.
To activate tests, you can set the system property "virtuoso.test" to true:

 $ mvn test -Dvirtuoso.test=true
 
By default, the tests will use the parameters configured in the pom.xml. Change the parameters' values to the ones that fit your installation of Virtuoso.

You can configure the following parameters:

* virtuoso.test (default is null)
* virtuoso.driver (default is 'virtuoso.jdbc4.Driver')
* virtuoso.host (default is 'localhost')
* virtuoso.port (default is '1111')
* virtuoso.user (default is 'dba')
* virtuoso.password (default is 'dba')

To override them from cli, you can also do the following:

 $ mvn test -Dvirtuoso.test=true -DargLine="-Dvirtuoso.password=mypassword -Dvirtuoso.port=1234"

### Deploy
This bundle needs the following:
* org.jboss.spec.javax.transaction:jboss-transaction-api_1.1_spec:1.0.1.Final
* org.apache.clerezza:ext.virtuoso.jdbc:0.3

To deploy the bundle in a running Felix instance you can do:

 $ mvn install -PinstallBundle -Dsling.url=http://localhost:8080/system/console (change this to be the actual server admin interface)


