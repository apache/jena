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
    protected boolean isClosed = false ;

    public StoreBase1(SDBConnection connection, StoreDesc desc, 
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
    public void close()                              { isClosed = true; }
    public boolean isClosed()                        { return isClosed; }

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

//    @Override
//    public Iterator<Node> listNamedGraphs()
//    {
//        return new NullIterator<Node>() ;
//    }
}
