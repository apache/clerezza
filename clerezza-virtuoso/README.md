# Virtuoso/Clerezza storage adapter

This is an implementation of the storage API of Clerezza[1] to use Virtuoso as storage.
 

## Build and Install
To build this project you need Maven. 

### Simple build
From the main folder:

 $ mvn clean install

Results will be 2 bundles:

* ext.virtuoso.jdbc : contains JDBC drivers form Virtuoso
* rdf.virtuoso.storage : contains the implementation of the clerezza storage API for Virtuoso

Bundles are in the /target folders:

* ext.virtuoso.jdbc/target/ext.virtuoso.jdbc-<version>.jar
* rdf.virtuoso.storage/target/rdf.virtuoso.storage-<version>.jar

### Build forcing tests
You must have a Virtuoso running server to do tests.
To activate tests, you must execute maven with the virtuoso-do-tests profile, for example:

 $ mvn test -Pvirtuoso-do-tests
 
By default, the tests will use the parameters configured in the pom.xml. Change the parameters' values to the ones that fit your installation of Virtuoso.

You can configure the following parameters:

* virtuoso.test (default is null, sets to true if you activate the 'virtuoso-do-tests' profile)
* virtuoso.driver (default is 'virtuoso.jdbc4.Driver')
* virtuoso.host (default is 'localhost')
* virtuoso.port (default is '1111')
* virtuoso.user (default is 'dba')
* virtuoso.password (default is 'dba')

To override them from cli, you can also do the following:

 $ mvn test -Pvirtuoso-do-tests -DargLine="-Dvirtuoso.password=mypassword -Dvirtuoso.port=1234"
 

### Hot deploy on a Clerezza or Stanbol[2] running servers

To deploy the bundles in a running Sling instance you can do:

 $ mvn install -PinstallBundle -Dsling.url=http://localhost:8080/system/console (change this to be the actual server admin interface)


* [1] http://incubator.apache.org/clerezza/
* [2] http://incubator.apache.org/stanbol/