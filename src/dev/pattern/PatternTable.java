/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.compiler.SlotCompiler;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;

/** A (description of a) table that holds a cached/optimized
 * version of a pattern.
 */ 

public class PatternTable
{
    private static Log log = LogFactory.getLog(PatternTable.class) ;
    
    static final String subjColName = "subject" ;
    
    // Property => column name
    Map <Node, String> cols = new HashMap<Node, String>() ;
    
    public PatternTable() {}
    
    public void add(Node property, String colname)
    {
        cols.put(property, colname) ;
    }
    
    // trigger if we see a prediate this table supports.
    public boolean trigger(Quad quad)
    {
        for ( Node p : cols.keySet() )
        {
            if ( p.equals(quad.getPredicate()) )
                return true ;
        }
        return false ;
    }

    // Start a table from the i'th quad 
    public SqlStage process(int i, QuadBlock quadBlock)
    {
        QuadBlock tableQuads = new QuadBlock() ;
        Set<Node> predicates = new HashSet<Node>(cols.keySet()) ;
        Node subject = null ;
        
        for ( Node p : predicates )
        {
            // Need common subject search/check.
            int idx = quadBlock.findFirst(i, null, subject, p, null) ;
            if ( idx < 0 )
                // No match.
                // Conservative - must find all predicates
                return null ;
            
            Quad q = quadBlock.get(idx) ;
            tableQuads.add(q) ;
            if ( subject == null )
                subject = q.getSubject() ;
        }
        
        quadBlock.removeAll(tableQuads) ;
        SqlStagePatternTable stage = new SqlStagePatternTable(tableQuads) ;
        return stage ;
    }

//    // -------------------***************************
//    public boolean match(QuadBlock quadBlock)
//    {
//        //QuadBlockMatch.match(pattern, quadBlock) ;
//        
//        Set<Node> predicates = new HashSet<Node>(cols.keySet()) ;
//        
//        //SetUtils.filter(cols.keySet(), f) ;
//        
//        
//        for ( Node p : predicates )
//        {
//            if ( ! quadBlock.contains(null, null, p, null) )
//                return false ;
//        }
//        return true ;
//    }
//
//    // Returns a stage list of a reduced quad block and this step.
//    // Issue: placement of this step.  An SqlNode optimization problem?
//    
//    // "Slot compiler" for QuadBlockCompiler.
//    
//    
//    public SqlStageList modBlock(QuadBlockCompiler compiler, QuadBlock quadBlock)
//    {
//        if ( quadBlock.getGraphNode() != Quad.defaultGraph )
//            log.fatal("Not the default graph") ;
//        
//        Set<Node> predicates = new HashSet<Node>(cols.keySet()) ;
//        SqlStageList sList = new SqlStageList() ;
//
//        QuadBlock tableQuads = new QuadBlock() ; 
//        QuadBlock replacement = quadBlock.clone() ;
//
//        for ( Node p : predicates )
//        {
//            // Need common subject
//            int idx = quadBlock.findFirst(null, null, p, null) ;
//            if ( idx < 0 )
//                // No match.
//                return null ;
//            Quad q = quadBlock.get(idx) ;
//            replacement.remove(q) ; // Not index as it might have moved.
//            tableQuads.add(q) ;
//        }
//        
//        SqlStagePTable stage = new SqlStagePTable(tableQuads) ;
//        sList.add(stage) ;
//        sList.add(new SqlStagePlain(compiler, replacement)) ;
//        return sList ;
//    }
    
    // Context?  Spanning conditions between SqlStages? 
    class SqlStagePatternTable implements SqlStage
    {
        
        private QuadBlock quadBlock ;

        public SqlStagePatternTable(QuadBlock tableQuads)
        { this.quadBlock = tableQuads ;}

        public SqlNode build(SDBRequest request, SlotCompiler slotCompiler)
        {
            QuadBlock quads = new QuadBlock(quadBlock) ;
            
            SqlTable sqlTable = new SqlTable("TABLE", "ALIAS") ;
            
            SqlExprList conditions = new SqlExprList() ;
            //compiler.processSlot(request, sqlTable, conditions, subj, subjColName) ;
            
            for ( Node pred : cols.keySet() )
            {
//                if ( ! quads.contains(null, null, pred, null) )
//                    continue ;

                int idx = quads.findFirst(null, null, pred, null) ;
                if ( idx < 0 )
                {
                    log.fatal("Can't find quad in SqlStagePTable.build") ;
                    throw new SDBException("SqlStagePTable.build") ;
                }
                
                Quad q = quads.remove(idx) ;
                
                String colName = cols.get(pred) ;
                SqlColumn col = new SqlColumn(sqlTable, colName) ;

                
                Node obj = q.getObject() ;
//                if ( Var.isVar(obj) )
//                    sqlTable.setIdColumnForVar(Var.alloc(obj), col) ;
                
                slotCompiler.processSlot(request, sqlTable, conditions, obj, colName) ;
            }
            return SqlRestrict.restrict(sqlTable, conditions) ;
        }
        
        @Override
        public String toString() { return "PTable"; }

        public void output(IndentedWriter out)
        {  out.print("PTable") ; }
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