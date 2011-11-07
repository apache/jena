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
        ( GraphRDB graph, SpecializedGraph sg, List<VarDesc> varList, List<DBPattern> dbPat, ExpressionSet constraints )
		{
		this.graph = graph;
		this.compiled = compile ( sg, varList, dbPat, constraints );
        // System.err.println( " " + this.compiled.stmt.toString().replaceAll( " AND ", "\n  AND " ).replaceAll( " Where ", "\n Where " ).replaceAll( " From ", "\n From " ) );
		}

	protected DBQuery compile( SpecializedGraph sg, List<VarDesc> varList, List<DBPattern> dbPat, ExpressionSet constraints )
        { return compile( compiler, sg, varList, dbPat, constraints ); }
        
    protected DBQuery compile( DBQueryStageCompiler compiler, SpecializedGraph sg,
    			List<VarDesc> varList, List<DBPattern> dbPat, ExpressionSet constraints )
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
        ResultSetStringIterator it = null;
        setArgs( current, ps );
        // System.out.println( ">> " + compiled.stmt.toString().replaceAll( " AND ", "\n  AND " ) );
        try
            {
            it = new ResultSetStringIterator();
            ps.execute();
            rs = ps.getResultSet();
            it.reset( rs, ps );
            while (it.hasNext())
                {
                Domain useme = current.copy();
                List<String> row = it.next();
                for (int i = 0; i < compiled.resList.length; i++)
                    {
                    int j = compiled.resList[i];
                    String o = row.get( i );
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
                int ix = (compiled.argIndex.get( i )).intValue();
                Node arg = args.get( ix );
                if (arg == null) throw new JenaException( "Null query argument" );
                String val = compiled.driver.nodeToRDBString( arg, false );
                ps.setString( i + 1, val );
                }
            }
        catch (SQLException e)
            { throw new JenaException( "Bad query argument", e ); }

        }

    @Override
    public Pipe deliver( final Pipe result )
        {
        final Pipe stream = previous.deliver( new BufferPipe() );
		new Thread() { @Override
        public void run() { DBQueryStage.this.run( stream, result ); } } .start();
        return result;
        }  
      
    }
