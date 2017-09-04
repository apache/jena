*TDB2 is not compatible with Apache Jena TDB (TDB1).*

The maven artifact, a non-Apache build, is in maven central:
```
   <dependency>
     <groupId>org.seaborne.mantis</groupId>
     <artifactId>tdb2</artifactId>
     <version>0.3.0</version>
   </dependency>
```

Simple migration is to use `TDB2Factory` in placeTDFCatory to create
datasets. `DatasetGraph` objects are now created via `DatabaseMgr`.

Example code: **`TDB2Factory`**

```
    public static void main(String[] args) {
         Dataset ds = TDB2Factory.createDatasetGraph() ;
         Txn.execWrite(ds, ()->{
              RDFDataMgr.read(ds, "SomeData.ttl");
         }) ;
          Txn.execRead(dsg, ()->{
             RDFDataMgr.write(System.out, ds, Lang.TRIG) ;
         }) ;
    }
```
