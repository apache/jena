*TDB2 is not compatible with Apache Jena TDB (TDB1).*

The TDB2 command line tools are provided as a single combined jar
``tdb2-cmds-VERSION.jar` containing all necessary dependencies. Each
command has help message; the arguments are the mostly the same as TDB1.

* `tdb2.tdbbackup`
* `tdb2.tdbdump`
* `tdb2.tdbcompact` (not in v 0.3.0)
* `tdb2.tdbloader`
* `tdb2.tdbquery`
* `tdb2.tdbstats`
* `tdb2.tdbupdate`

Example usage:

```
java -cp JAR tdb2.tdbloader --loc <DB location> file1 file2 ...
```
