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