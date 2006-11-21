/*
  (c) Copyright 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: DBQueryStage.java,v 1.14 2006-11-21 16:24:44 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;
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
            
	public DBQueryStage
        ( GraphRDB graph, SpecializedGraph sg, List varList, List dbPat, ExpressionSet constraints )
		{
		this.graph = graph;
		this.compiled = compile ( sg, varList, dbPat, constraints );
        // System.err.println( ">> " + this.compiled.stmt.toString().replaceAll( " AND ", "\n  AND " ) );
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
        
	protected void run( Pipe source, Pipe sink )
        {
        PreparedStatement ps = null;
        try
            {
            if (!compiled.isEmpty) ps = getPreparedStatement();

            if (ps != null) 
                while (source.hasNext())
                    extendSourceBinding( source.get(), sink, ps );
            }
        finally
            {
            if (ps != null) closePreparedStatement( ps );
            if (sink != null) sink.close();
            }
        }

    private void extendSourceBinding( Domain current, Pipe sink, PreparedStatement ps )
        {
        ResultSet rs = null;
        ResultSetIterator it = null;
        setArgs( current, ps );
        // System.out.println( ">> " + compiled.stmt.toString().replaceAll( " AND ", "\n  AND " ) );
        try
            {
            it = new ResultSetIterator();
            ps.execute();
            rs = ps.getResultSet();
            it.reset( rs, ps );
            while (it.hasNext())
                {
                Domain useme = current.copy();
                List row = (List) it.next();
                for (int i = 0; i < compiled.resList.length; i++)
                    {
                    int j = compiled.resList[i];
                    String o = (String) row.get( i );
                    Node n = compiled.driver.RDBStringToNode( o );
                    useme.setElement( j, n );
                    }
                sink.put( useme );
                }
            }
        catch (Exception e)
            { throw new JenaException( "Query execute failed: " + e ); }
        finally
            { 
            if (it != null) it.close(); 
            if (rs != null) closeResultSet( rs );
            }
        }

    private void closePreparedStatement( PreparedStatement ps )
        {
        try { ps.close(); }
        catch (Exception e)
            { throw new JenaException( "Close on prepared stmt failed: " + e ); }
        }

    private void closeResultSet( ResultSet rs )
        {
        try { rs.close(); }
        catch (SQLException e)
            { throw new RDFRDBException( "Failed to get last inserted ID: "  + e ); }
        }

    private PreparedStatement getPreparedStatement()
        {
        try
            {
            IDBConnection conn = compiled.driver.getConnection();
            return conn.getConnection().prepareStatement( compiled.stmt );
            }
        catch (Exception e)
            { throw new JenaException( "Query prepare failed: " + e ); }
        }
    	
    protected void setArgs( Domain args, PreparedStatement ps )
        {
        try
            {
            for (int i = 0; i < compiled.argCnt; i++)
                {
                int ix = ((Integer) compiled.argIndex.get( i )).intValue();
                Node arg = (Node) args.get( ix );
                if (arg == null) throw new JenaException( "Null query argument" );
                String val = compiled.driver.nodeToRDBString( arg, false );
                ps.setString( i + 1, val );
                }
            }
        catch (SQLException e)
            { throw new JenaException( "Bad query argument", e ); }

        }

    public Pipe deliver( final Pipe result )
        {
        final Pipe stream = previous.deliver( new BufferPipe() );
		new Thread() { public void run() { DBQueryStage.this.run( stream, result ); } } .start();
        return result;
        }  
      
    }

/*
    (c) Copyright 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
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
