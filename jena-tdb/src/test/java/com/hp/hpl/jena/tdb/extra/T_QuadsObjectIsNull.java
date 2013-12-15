/*
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

package com.hp.hpl.jena.tdb.extra ;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;
import com.hp.hpl.jena.tdb.transaction.Journal ;
import com.hp.hpl.jena.tdb.transaction.JournalControl ;
import com.hp.hpl.jena.tdb.transaction.NodeTableTrans ;

public class T_QuadsObjectIsNull {
//	static {
//		ARQ.getContext().set(SystemTDB.symFileMode, "direct");
//		TDB.getContext().set(TDB.symUnionDefaultGraph, true);
//	}

	static String DIR = "DBX" ;
	static Location location = new Location(DIR) ;

	public static void main(String[] args) {

	    if ( false )
	    {
    	    LogCtl.enable(SystemTDB.syslog.getName()) ;
    	    LogCtl.enable(Journal.class) ;
    	    LogCtl.enable(JournalControl.class) ;
    	    LogCtl.enable(NodeTableTrans.class) ;
	    }
	    if ( false )
	    {
	        String journal = "DBX/journal.jrnl" ;
	        if ( FileOps.exists(journal))
	            JournalControl.print(journal) ;
	    } 
	    
	    if ( false ) {
	        FileOps.ensureDir(DIR) ;
	        FileOps.clearDirectory(DIR) ;
	    }
	    one() ;
	}
	
	public static void write(DatasetGraphTransaction dsg, Quad quad)
	{
        dsg.begin(ReadWrite.WRITE) ;
        dsg.add(quad) ;
        if ( ! dsg.contains(quad) )
            throw new RuntimeException("No quad: "+quad) ;
        dsg.commit() ;
        dsg.end() ;
	}
	
    private static void dump(DatasetGraphTransaction dsg)
    {
        dsg.begin(ReadWrite.READ);
        Iterator<Quad> iter = dsg.find() ;
        for ( ; iter.hasNext() ; )
        {
            Quad q = iter.next() ;
            System.out.println(q) ;
        }
        //RiotWriter.writeNQuads(System.out, dsg) ;
        dsg.commit();
        dsg.end();
    }

    public static void one()
	{
	    Quad q1 = SSE.parseQuad("(<g1> <s1> <p1> '1')") ;
	    Quad q2 = SSE.parseQuad("(<g2> <s2> <p2> '2')") ;
        Quad q3 = SSE.parseQuad("(<g3> <s3> <p3> '3')") ;

        DatasetGraphTransaction dsg = (DatasetGraphTransaction)TDBFactory.createDatasetGraph(location);
        System.out.println("Start") ;
        dump(dsg) ;
        
        write(dsg, q1) ;
        write(dsg, q2) ;
        //write(dsg, q3) ;
        System.out.println("Finish") ;
        dump(dsg) ;
	}

	
}
