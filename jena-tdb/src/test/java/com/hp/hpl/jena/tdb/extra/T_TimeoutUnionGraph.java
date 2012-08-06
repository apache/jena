/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2011, 2012. All Rights Reserved.
 * Note to U.S. Government Users Restricted Rights: Use,
 * duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp.
 *******************************************************************************/

// Licensed to ASF in JENA-289

package com.hp.hpl.jena.tdb.extra ;

import java.text.MessageFormat ;
import java.util.Date ;
import java.util.concurrent.TimeUnit ;

import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;

public class T_TimeoutUnionGraph {

	private static String location = "C:\\temp\\TestTimeout"; //$NON-NLS-1$
    private static final String sparql = "SELECT * WHERE { ?a ?b ?c . ?c ?d ?e }"; //$NON-NLS-1$
	//private static final String sparql = "SELECT * WHERE { { ?a ?b ?c . FILTER (!isLiteral(?c)) } . ?c ?d ?e }"; //$NON-NLS-1$
	private static final int limit = 1000;
	private static final int timeout1_sec = 10;
	private static final int timeout2_sec = 10;

	private static boolean CREATE = false;
	private static final int RESOURCES = 100000;
	private static final int COMMIT_EVERY = 1000;
	private static final int TRIPLES_PER_RESOURCE = 100;
	private static final String RES_NS = "http://example.com/"; //$NON-NLS-1$
	private static final String PROP_NS = "http://example.org/ns/1.0/"; //$NON-NLS-1$

	public static void main(String[] args) {
	    
	    System.out.printf("Max mem = %,dM\n", Runtime.getRuntime().maxMemory()/(1000*1000)) ;
	    
	    if ( false )
            SystemTDB.setFileMode(FileMode.direct) ;
	    
        location = "DBX" ;
	    FileOps.ensureDir(location) ;
        TDB.getContext().set(TDB.symUnionDefaultGraph, true);
	    
        Dataset ds = TDBFactory.createDataset(location);
        if ( ds.asDatasetGraph().isEmpty() )
	        create(ds) ; 

		Query query = QueryFactory.create(sparql, Syntax.syntaxSPARQL_11);
		query.setLimit(limit);
		System.out.println(query) ;

		ds.begin(ReadWrite.READ);
		QueryExecution qexec = null;
		Timer timer = new Timer() ;
		
		TransactionManager.QueueBatchSize = 0 ;
		
		System.out.println() ;
        System.out.println("Start query") ;
        
        //ARQ.getContext().set(ARQ.queryTimeout, "10000,10000") ;
        //ARQ.getContext().set(ARQ.symLogExec, "true") ; 
        
		try {
			System.out.println(MessageFormat.format(
				"{0,date} {0,time} Executing query [limit={1} timeout1={2}s timeout2={3}s]: {4}", //$NON-NLS-1$
				new Date(System.currentTimeMillis()), limit, timeout1_sec, timeout2_sec, sparql));
	        timer.startTimer() ;
			qexec = QueryExecutionFactory.create(query, ds);
			qexec.setTimeout(timeout1_sec, TimeUnit.SECONDS, timeout2_sec, TimeUnit.SECONDS);

			ResultSet rs = qexec.execSelect();
			ResultSetFormatter.outputAsXML(System.out, rs);
		} catch (Throwable t) {
			t.printStackTrace(); // OOME
		} finally {
            long x = timer.readTimer() ;
            System.out.printf("Time = %,dms\n", x) ;
			if (qexec != null)
				qexec.close();
			ds.end();
			ds.close();
			System.out.println(MessageFormat.format("{0,date} {0,time} Finished", //$NON-NLS-1$
				new Date(System.currentTimeMillis())));
		}
	}

    private static void create(Dataset ds)
    {
        System.out.println("Start create") ;
        int iR = 0 ;
        
        for (iR = 0; iR < RESOURCES; iR++) {
            if (iR % COMMIT_EVERY == 0) {
                if (ds.isInTransaction()) {
                    ds.commit();
                    ds.end();
                }
                ds.begin(ReadWrite.WRITE);
            }

            Model model = ModelFactory.createDefaultModel();
            Resource res = ResourceFactory.createResource(RES_NS + "resource" + iR); //$NON-NLS-1$
            //Model model = ds.getNamedModel(res.getURI()) ;
            for (int iP = 0; iP < TRIPLES_PER_RESOURCE; iP++) {
                Property prop = ResourceFactory.createProperty(PROP_NS, "property" + iP); //$NON-NLS-1$
                model.add(res, prop, model.createTypedLiteral("Property value " + iP)); //$NON-NLS-1$
            }
            ds.addNamedModel(res.getURI(), model);
            //System.out.println("Created " + res.getURI()); //$NON-NLS-1$
        }
        
        if (ds.isInTransaction()) {
            ds.commit();
            ds.end();
        }

        System.out.println("Finish create") ;
    }
    
}
