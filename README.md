[![Build Status](https://api.travis-ci.org/afs/mantis.png)](https://travis-ci.org/afs/mantis)

# Mantis - Database Operating Environment

Mantis is a set of database-related components. It includes:

* Transaction Coordination
* Copy-on-write data structures
* Query algebra evaluation library
* Abstraction of basic file operations

License: Apache License 

See [LICENSE](LICENSE) and [NOTICE](NOTICE) for details.

Continuous integration: [TravisCI](https://travis-ci.org/afs/mantis)

Status: currently snashot builds only.
It depedns on [Apache Jena](https://jena.apache.org/) snapshots.

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

TDB2 example:

```
     public static void main(String[] args) {
        Quad q1 = SSE.parseQuad("(_ <s> <p> 1 )") ; 
        
        DatasetGraph dsg = TDB2Factory.createDatasetGraph() ;
        Txn.execWrite(dsg, ()->{
            dsg.add(q1) ;        
        }) ;
            
        Txn.execRead(dsg, ()->{
            RDFDataMgr.write(System.out, dsg, Lang.TRIG) ;
        }) ;
        System.out.println("-----------") ;
        
        Quad q2 = SSE.parseQuad("(_ <s> <p> 2 )") ;
        TransactionalSystem txnSystem = ((DatasetGraphTDB)dsg).getTxnSystem() ;
        dsg.begin(ReadWrite.READ);
        Transaction txn = txnSystem.getThreadTransaction() ;
        boolean b = txn.promote() ;
        if ( ! b ) {
            System.out.println("Did not promote");
            throw new RuntimeException() ;
        }
        
        dsg.add(q2) ;
        dsg.commit() ;
        dsg.end() ;
        
        Txn.execRead(dsg, ()->{
            RDFDataMgr.write(System.out, dsg, Lang.TRIG) ;
        }) ;
    }        
```