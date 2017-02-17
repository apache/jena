*TDB2 is not compatible with Apache Jena TDB (TDB1).*


TDB2 example:

Set the repositories (see below) and use:
```
   <dependency>
     <groupId>org.seaborne.mantis</groupId>
     <artifactId>tdb2</artifactId>
     <version>X.Y.Z</version>
   </dependency>
```


Example code: **`TDB2Factory`**

For disk location 

```
     public static void main(String[] args) {
        Quad q1 = SSE.parseQuad("(_ <s> <p> 1 )") ; 
        # In-memory dataset.
        DatasetGraph dsg = TDB2Factory.createDatasetGraph() ;
        # Transactions are required.
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

Maven repositories setup:

For development builds:

```
  <repositories>
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
