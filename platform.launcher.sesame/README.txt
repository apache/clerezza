Apache Clerezza TDB Launcher

The provided launcher jar is executable, however on most platform you must
provide additional arguments to java so that Clerezza has enough memory to run.

Start clerezza with:

java -XX:MaxPermSize=400m -Dfile.encoding=utf-8 -Xss512k -Xmx2g -jar platform.launcher.tdb-*.jar

Add the --help at the end of the line above to see possible argument for
Clerezza
