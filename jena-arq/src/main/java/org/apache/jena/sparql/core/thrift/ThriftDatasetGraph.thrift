namespace java org.apache.jena.sparql.core.thrift

service DatasetGraphThrift {

	// Other

	binary nextIteratorPage(1:string uuid);
	
	void closeIterator(1:string uuid);

	// DatasetGraph

	// void close(); See void close(string uuid)

    bool containsGraph(1:string uuid, 2:binary graphNode);

	string listGraphNodes(1:string uuid);

	string find(1:string uuid);

	string findQ(1:string uuid, 2:binary quad);
	
	string findGSPO(1:string uuid, 2:binary g, 3:binary s, 4:binary p, 5:binary o);
	
	string findNG(1:string uuid, 2:binary g, 3:binary s, 4:binary p, 5:binary o);
	
	bool containsGSPO(1:string uuid, 2:binary g, 3:binary s, 4:binary p, 5:binary o);
	
	bool containsQ(1:string uuid, 2:binary quad);
	
	void clear(1:string uuid);
	
	bool isEmpty(1:string uuid);
	
	void enterCriticalSection(1:string uuid, 2:bool readLockRequested);
	
	void leaveCriticalSection(1:string uuid);
	
	i64 size(1:string uuid);
	
	void close(1:string uuid);
	
	bool supportsTransactions(1:string uuid);
	
	bool supportsTransactionAbort(1:string uuid);
	
	// Transactional
	
	void tBegin(1:string uuid, 2:bool read);
	
	void tCommit(1:string uuid);
	
	void tAbort(1:string uuid);
	
	void tEnd(1:string uuid);
	
	bool tIsInTransaction(1:string uuid);
}