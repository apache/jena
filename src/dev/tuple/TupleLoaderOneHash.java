/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.tuple;

import static com.hp.hpl.jena.sdb.util.StrUtils.strjoinNL;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlConstant;
import com.hp.hpl.jena.sdb.layout2.NodeLayout2;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.TableDesc;

public class TupleLoaderOneHash extends TupleLoaderOne
{
    public TupleLoaderOneHash(Store store)
    { super(store) ; }

    /* Convenience constructor */
    public TupleLoaderOneHash(Store store, TableDesc tableDesc)
    { super(store, tableDesc) ; }

    @Override
    public SqlConstant getRefForNode(Node node) throws SQLException 
    {
        return new SqlConstant(NodeLayout2.hash(node)) ;
    }

    @Override
    public SqlConstant insertNode(Node node) throws SQLException 
    {
        int typeId  = NodeLayout2.nodeToType(node) ;
        String lex = NodeLayout2.nodeToLex(node) ;
        String lang = "" ;
        String datatype = "" ;
        
        if ( node.isLiteral() )
        {
            lang = node.getLiteralLanguage() ;
            datatype = node.getLiteralDatatypeURI() ;
            if ( datatype == null )
                datatype = "" ;
        }
        
        long hash = NodeLayout2.hash(lex,lang,datatype,typeId);
        
        // Existance check
        
        String sqlStmtTest = strjoinNL(
                "SELECT hash FROM "+TableDescNodes.name(),
                "WHERE hash = "+hash
                ) ;
        
        ResultSet rs = store.getConnection().execQuery(sqlStmtTest) ;
        boolean b = rs.next();
        rs.close() ;
        if ( b )
            // Exists
            return new SqlConstant(hash) ;
        
        String sqlStmt = strjoinNL(
                "INSERT INTO "+TableDescNodes.name()+"(hash,lex,lang,datatype,type) VALUES",
                "  ("+hash+", ",
                "   "+SQLUtils.quoteStr(lex)+", ",
                "   "+SQLUtils.quoteStr(lang)+", ",
                "   "+SQLUtils.quoteStr(datatype)+", ",
                "   "+typeId, 
                ")" ) ;
        store.getConnection().execUpdate(sqlStmt) ;
        return new SqlConstant(hash) ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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