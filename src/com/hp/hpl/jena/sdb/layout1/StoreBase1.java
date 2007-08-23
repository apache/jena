/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout1;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.compiler.QueryCompilerFactory;
import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionHolder;
import com.hp.hpl.jena.sdb.sql.TableUtils;
import com.hp.hpl.jena.sdb.store.*;

public class StoreBase1 
    extends SDBConnectionHolder
    implements Store
{
    // This duplicates stuff from StoreBase in layout2 - tidy up 
    protected StoreDesc storeDescription ;
    protected StoreFormatter formatter ;
    protected StoreLoader loader ;
    protected QueryCompilerFactory compilerF ;
    protected SQLBridgeFactory sqlBridgeF ;
    protected SQLGenerator sqlGenerator ;
    protected StoreConfig configuration ;
    protected TableDescTriples tripleTable ;
    
    public StoreBase1(StoreDesc desc, SDBConnection connection, 
                     StoreFormatter formatter ,
                     TupleLoaderSimple loader ,
                     QueryCompilerFactory compilerF ,
                     SQLBridgeFactory sqlBridgeF,
                     SQLGenerator sqlGenerator,
                     TableDescTriples tripleTable)
    {
        super(connection) ;
        this.storeDescription = desc ;

        this.formatter = formatter ;
        if ( loader.getTableDesc() == null )
            loader.setTableDesc(tripleTable) ;
        this.loader = new TupleGraphLoader(loader) ;
        this.compilerF = compilerF ;
        this.sqlBridgeF = sqlBridgeF ;
        if ( sqlGenerator == null )
            sqlGenerator = new GenerateSQL() ;
        this.sqlGenerator = sqlGenerator ;
        this.tripleTable = tripleTable ;
        
        configuration = new StoreConfig(connection()) ;
    }
    
    public SDBConnection   getConnection()                      {  return connection() ; }
    
    public QueryCompilerFactory   getQueryCompilerFactory()     { return compilerF ; }

    public SQLBridgeFactory getSQLBridgeFactory()               { return sqlBridgeF ; }

    public SQLGenerator    getSQLGenerator()                    { return sqlGenerator ; }

    public StoreFormatter  getTableFormatter()                  { return formatter ; }

    public StoreLoader     getLoader()                          { return loader ; }

    public StoreConfig     getConfiguration()                   { return configuration ; }
    
    public DatabaseType     getDatabaseType()                   { return storeDescription.getDbType() ; }
    
    public LayoutType       getLayoutType()                     { return storeDescription.getLayout() ; }

    // Note -- this does not close the JDBC connection, which may be shared.
    // See also StoreBaseHSQL
    public void close()                              { }
    
    /** Default implementation: get size of Triples table **/
    public long getSize()
    {
    	return TableUtils.getTableSize(getConnection().getSqlConnection(), "Triples");
    }
    
    /** Irrelevant for layout1 **/
    public long getSize(Node node) {
    	return getSize();
    }
    
    public TableDescNodes   getNodeTableDesc()                 { return null ; }
    public TableDescTriples getTripleTableDesc()               { return tripleTable ; }
    public TableDescQuads   getQuadTableDesc()                 { return null ; }
}



/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */