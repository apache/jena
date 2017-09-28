*TDB2 is not compatible with Apache Jena TDB (TDB1).*

Simple migration of code is to use `TDB2Factory` in place of TDBFactory to create
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
