Apache Clerezza Virtuoso Launcher

The provided launcher jar is executable, however on most platform you must
provide additional arguments to java so that Clerezza has enough memory to run.

Start clerezza with:

  java -XX:MaxPermSize=400m -Dfile.encoding=utf-8 -Xss512k -Xmx2g -jar platform.launcher.virtuoso-*.jar

Add the --help at the end of the line above to see possible argument for Clerezza

How to configure Virtuoso JDBC connection?

To provide the initial configuration you can use the following parameters as system properties:
 - virtuoso.host (default localhost)
 - virtuoso.port (default 1111)
 - virtuoso.user (default dba)
 - virtuoso.password (default dba)

for example

  java -XX:MaxPermSize=400m -Dfile.encoding=utf-8 -Xss512k -Xmx2g -Dvirtuoso.password=mysecret -Dvirtuoso.user=clerezza -jar platform.launcher.virtuoso-*.jar


