package com.hp.hpl.jena.sparql.lang.sparql_11_plus;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;

public interface QueryExecutionPlus extends QueryExecution {
	public Dataset execConstructDataset();
}
