/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: DBQueryStage.java,v 1.7 2003-12-12 22:15:31 wkw Exp $
*/

package com.hp.hpl.jena.db.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.BufferPipe;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.ExpressionSet;
import com.hp.hpl.jena.graph.query.Pipe;
import com.hp.hpl.jena.graph.query.Stage;
import com.hp.hpl.jena.shared.JenaException;

/**
    @author hedgehog
*/

public class DBQueryStage extends Stage
    {
    protected Graph graph;
    protected DBQuery compiled;
            
	public DBQueryStage( GraphRDB graph, SpecializedGraph sg, 
					List varList, List dbPat, ExpressionSet constraints )
		{
		this.graph = graph;
		this.compiled = compile (sg, varList, dbPat, constraints);
		}

	protected DBQuery compile( SpecializedGraph sg, List varList, List dbPat, ExpressionSet constraints )
        { return compile( compiler, sg, varList, dbPat, constraints ); }
        
    protected DBQuery compile( DBQueryStageCompiler compiler, SpecializedGraph sg,
    			List varList, List dbPat, ExpressionSet constraints )
        {
        return DBQueryStageCompiler.compile( compiler, (DBQueryHandler) graph.queryHandler(),
        			sg, varList, dbPat, constraints );
        }
                 
    private static final DBQueryStageCompiler compiler = new DBQueryStageCompiler();
        
	protected void run(Pipe source, Pipe sink) {
		PreparedStatement ps = null;
		Domain current;
		Domain useme;
		IDBConnection conn = compiled.driver.getConnection();
		 try {
			 ps = conn.getConnection().prepareStatement(compiled.stmt);
		 } catch (Exception e) {
			 throw new JenaException("Query prepare failed: " + e);
		 }

		if ( ps != null) while (source.hasNext()) {
			current = source.get();
			setArgs(current, ps);
// System.out.println(compiled.stmt);

			try {
				ResultSetIterator it = new ResultSetIterator();
				ps.execute();
				ResultSet rs = ps.getResultSet();
				it.reset(rs, ps, null, null);
				while (it.hasNext()) {
					useme = current.copy();
					List row = (List) it.next();
					for(int i=0;i<compiled.resList.length;i++) {
						int j = compiled.resList[i];
						String o = (String) row.get(i);
						Node n = compiled.driver.RDBStringToNode(o);
						useme.setElement(j,n);
					}
					sink.put(useme);
				}
				it.close();
			} catch (Exception e) {
				throw new JenaException("Query execute failed: " + e);
			}
		}
		if ( ps != null ) try {
			ps.close();
		} catch (Exception e) {
			throw new JenaException("Close on repared stmt failed: " + e);
		}
		sink.close();
	}
    	
    protected void setArgs ( Domain args, PreparedStatement ps ) {
    	int i, ix;
    	String val;
    	Node arg;
    	try {
    		for ( i=0;i<compiled.argCnt;i++) {
    			ix = ((Integer)compiled.argIndex.get(i)).intValue();
    			arg = (Node) args.get(ix);
    			if ( arg == null ) throw new JenaException("Null query argument");
    			val = compiled.driver.nodeToRDBString(arg,false);
    			ps.setString(i+1, val);	
    		}
		} catch (Exception e) {
			throw new JenaException("Bad query argument: " + e);
		}

    }

    public Pipe deliver( final Pipe result )
        {
        final Pipe stream = previous.deliver( new BufferPipe() );
		new Thread() { public void run() { DBQueryStage.this.run( stream, result ); } } .start();
        return result;
        }  
      
    }

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
