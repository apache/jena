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

package com.hp.hpl.jena.sdb.layout2;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.compiler.QueryCompilerFactory;
import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionHolder;
import com.hp.hpl.jena.sdb.sql.TableUtils;
import com.hp.hpl.jena.sdb.store.*;

public abstract class StoreBase 
    extends SDBConnectionHolder
    implements Store
{
    protected StoreDesc storeDescription ;
    protected StoreFormatter formatter ;
    protected StoreLoader loader ;
    protected QueryCompilerFactory compilerF ;
    protected SQLBridgeFactory sqlBridgeF ;
    protected SQLGenerator sqlGenerator ;
    protected StoreConfig configuration ;
    protected TableDescTriples tripleTableDesc ;
    protected TableDescQuads quadTableDesc = null ;
    protected TableDescNodes nodeTableDesc ;
    protected boolean isClosed = false ;
    
    public StoreBase(SDBConnection connection, StoreDesc desc, 
                     StoreFormatter formatter ,
                     StoreLoader loader ,
                     QueryCompilerFactory compilerF ,
                     SQLBridgeFactory sqlBridgeF,
                     SQLGenerator sqlGenerator,
                     TableDescTriples    tripleTableDesc,
                     TableDescQuads      quadTableDesc,
                     TableDescNodes      nodeTableDesc)
    {
        super(connection) ;
        this.storeDescription = desc ;
        this.formatter = formatter ;
        this.loader = loader ;
        this.compilerF = compilerF ;
        this.sqlBridgeF = sqlBridgeF ;
        if ( sqlGenerator == null )
            sqlGenerator = new GenerateSQL() ;
        this.sqlGenerator = sqlGenerator ;
        this.tripleTableDesc = tripleTableDesc ;
        this.quadTableDesc = quadTableDesc ;
        this.nodeTableDesc = nodeTableDesc ;
        
        configuration = new StoreConfig(connection()) ;
    }
    
    @Override
    public SDBConnection   getConnection()                      {  return connection() ; }
    
    @Override
    public QueryCompilerFactory   getQueryCompilerFactory()     { return compilerF ; }

    @Override
    public SQLBridgeFactory getSQLBridgeFactory()               { return sqlBridgeF ; }

    @Override
    public SQLGenerator    getSQLGenerator()                    { return sqlGenerator ; }

    @Override
    public StoreFormatter  getTableFormatter()                  { return formatter ; }

    @Override
    public StoreLoader     getLoader()                          { return loader ; }

    @Override
    public StoreConfig     getConfiguration()                   { return configuration ; }
    
    @Override
    public DatabaseType     getDatabaseType()                   { return storeDescription.getDbType() ; }
    
    @Override
    public LayoutType       getLayoutType()                     { return storeDescription.getLayout() ; }

    // Note -- this does not close the JDBC connection, which may be shared.
    // See also StoreBaseHSQL
    @Override
    public void close() { getLoader().close(); isClosed = true; }
    @Override
    public boolean isClosed() { return isClosed; }
    
    /** Default implementation: get size of Triples table **/
    @Override
    public long getSize()
    {
    	return TableUtils.getTableSize(getConnection().getSqlConnection(), "Triples");
    }
    
    @Override
    public TableDescNodes   getNodeTableDesc()                 { return nodeTableDesc ; }
    @Override
    public TableDescTriples getTripleTableDesc()               { return tripleTableDesc ; }
    @Override
    public TableDescQuads   getQuadTableDesc()                 { return quadTableDesc ; }
 
//    public Iterator<Node> listNamedGraphs()
//    {
//        // Only works for Store layout2.  Layout1 overrides and removes.
//        // Name of column that is the node id type (hash or id).
//        String idCol = getNodeTableDesc().getNodeRefColName() ;
//        
//        // %1$s - node table name
//        // %2$s - quad table name
//        // %3$s - node column
//        String str = sqlStr("SELECT %1$s.lex, %1$s.type",
//                            "FROM",
//                            "    (SELECT DISTINCT g FROM %2$s) AS Q",
//                            "  LEFT OUTER JOIN",
//                            "    %1$s",
//                            "ON Q.g = %1$s."+idCol) ;
//        str = String.format(str, getNodeTableDesc().getTableName(),
//                            getQuadTableDesc().getTableName(),
//                            idCol) ;
//        
//        List<Node> nodes = new ArrayList<Node>() ; 
//        try {
//            ResultSet res = getConnection().exec(str).get() ;
//            while(res.next())
//            {
//                String lex = res.getString("lex") ;
//
//                // Check type
//                int type = res.getInt("type") ;
//                if ( type != ValueType.URI.getTypeId() )
//                    ALog.warn(this, "Non-URI for graph name: (lexical form: "+lex) ;
//                
//                nodes.add(Node.createURI(lex)) ;
//            }
//            return nodes.iterator();
//        } catch (SQLException e) {
//            throw new SDBExceptionSQL("Failed to get graph size", e);
//        }
//    }
}
