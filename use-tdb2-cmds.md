*TDB2 is not compatible with Apache Jena TDB (TDB1).*

The TDB2 command line tools are provided as a single combined jar
``tdb2-cmds-VERSION.jar` containing all necessary dependencies. Each
command has help message; the arguments are the mostly the same as TDB1.

* `tdb2.tdbbackup`
* `tdb2.tdbdump`
* `tdb2.tdbloader`
* `tdb2.tdbquery`
* `tdb2.tdbstats`
* `tdb2.tdbupdate`

[Command line jar (release builds)](http://central.maven.org/maven2/org/seaborne/mantis/tdb2-cmds/

[Development builds (Sonatype snapshot repository)](https://oss.sonatype.org/content/repositories/snapshots/org/seaborne/mantis/tdb2-cmds/)

Example usage:

```
java -cp JAR tdb2.tdbloader --loc <DB location> file1 file2 ...
```
