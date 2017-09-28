[![Build Status](https://api.travis-ci.org/afs/mantis.png)](https://travis-ci.org/afs/mantis)

# Mantis - Database Operating Environment

Mantis is a set of database-related components. It includes:

* Transaction Coordination
* Copy-on-write data structures
* Query algebra evaluation library
* Abstraction of basic file operations

TDB2 is a reworking for TDB that provides scalable transactions as well
as refining the machinary for reliability.

License: Apache License 

See [LICENSE](LICENSE) and [NOTICE](NOTICE) for details.

Continuous integration: [TravisCI](https://travis-ci.org/afs/mantis)

Notes on using [TDB2 in a Java application](use-tdb2.md).

Notes on using [TDB2 from the command line](use-tdb2-cmds.md).

Notes on using [TDB2 with Apache Jena Fuseki](use-fuseki-tdb2.md).

Status: currently snapshot builds only.

It depends on [Apache Jena](https://jena.apache.org/) snapshots.

Maven repositories setup:

```
  <repositories>
    <!-- Apache Snapshot Repository -->
    <repository>
      <id>apache-repository-snapshots</id>
      <url>https://repository.apache.org/snapshots</url>
      <releases>
        <enabled>false</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
        <updatePolicy>daily</updatePolicy>
        <checksumPolicy>fail</checksumPolicy>
      </snapshots>
    </repository>

    <!-- Sonatype snapshot repository -->
    <repository>
      <id>sonatype.public</id>
      <name>Sonatype Snapshots Repository</name>
      <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
      <releases>
        <enabled>false</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>

  </repositories>
```

