/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.gen;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.compiler.OpSQL;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRename;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.util.PrintSDB;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;

// Alt: an SELECT node with disinct, project, order by
// Distinct(Project(Order(Having(groupBy(restrict(joins)))))
// 
// Table-WHERE-GROUPBY-HAVING-ORDERBY-Project-Distinct

public class RunGen
{
    public static void main(String...argv)
    {
        boolean printOp         = false ;
        boolean printSqlNode    = false ;
        boolean printSQL        = false ;
        
        Query query = QueryFactory.create("SELECT ?x ?v ?w { ?x ?p ?v OPTIONAL { ?x ?q ?w } }") ;
        System.out.println(query.serialize()) ;
        String x = query.serialize() ;
        
        Store store = SDBFactory.connectStore("sdb.ttl") ;
        
        QueryEngineSDB qe = new QueryEngineSDB(store, query) ;
        Op op = qe.getOp() ;
        
        // Check - shouldn't this have been removed as it's part of teh SQL expression? 
        if ( op instanceof OpProject )
            op = ((OpProject)op).getSubOp() ;
        
        SqlNode sqlNode = ((OpSQL)op).getSqlNode() ;
        if ( sqlNode.isProject() )
            sqlNode = sqlNode.asProject().getSubNode() ;
        
        //System.out.println(sqlNode) ;
        
        sqlNode = SqlRename.view("Z", sqlNode) ;

        System.out.println(sqlNode) ;
        
        if ( printOp )
        {
            divider() ;
            PrintSDB.print(op) ;
        }
        if ( printSqlNode )
        {
            divider() ;
            PrintSDB.printSqlNodes(op) ;
        }

        if ( printSQL )
        {
            divider() ;
            PrintSDB.printSQL(op) ;
        }
    }
    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    static boolean needDivider = false ;
    static private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
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