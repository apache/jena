/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
    public void close() { getLoader().close(); isClosed = true; }
    public boolean isClosed() { return isClosed; }
    
    /** Default implementation: get size of Triples table **/
    public long getSize()
    {
    	return TableUtils.getTableSize(getConnection().getSqlConnection(), "Triples");
    }
    
    public TableDescNodes   getNodeTableDesc()                 { return nodeTableDesc ; }
    public TableDescTriples getTripleTableDesc()               { return tripleTableDesc ; }
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

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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