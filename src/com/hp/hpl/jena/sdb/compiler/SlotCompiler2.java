/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.store.NodeTableDesc;
import com.hp.hpl.jena.sdb.store.TripleTableDesc;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

public abstract class SlotCompiler2 extends SlotCompiler
{
    private static Log log = LogFactory.getLog(SlotCompiler2.class) ;

    protected TripleTableDesc tripleTableDesc ;
    protected NodeTableDesc   nodeTableDesc ;
    
    private List<Node> constants = new ArrayList<Node>() ;
    private List<Var>  vars = new ArrayList<Var>() ;
    
    public SlotCompiler2(SDBRequest request)
    { 
        super() ;
        tripleTableDesc = request.getStore().getTripleTableDesc() ;
        nodeTableDesc = request.getStore().getNodeTableDesc() ;
    }

    @Override
    final protected SqlNode start(QuadBlock quads)
    {
        classify(quads, constants, vars) ;
        SqlNode sqlNode = insertConstantAccesses(constants) ;
        return sqlNode ;
    }

    @Override
    final protected SqlNode finish(SqlNode sqlNode, QuadBlock quads)
    { return sqlNode ; }
    
    protected abstract 
    SqlNode insertConstantAccesses(Collection<Node> constants) ;

    @Override
    protected abstract 
    void constantSlot(SDBRequest request, Node node, SqlColumn thisCol, SqlExprList conditions) ;
 
    
    private static void classify(QuadBlock quadBlock, Collection<Node> constants, Collection<Var>vars)
    {
        for ( Quad quad : quadBlock )
        {
            if ( ! quad.getGraph().equals(Quad.defaultGraph) )
            {
                log.fatal("Non-default graph") ;
                throw new SDBException("Non-default graph") ;
            }
            if ( false )
                // Not quadding currently.
                acc(constants, vars, quad.getGraph()) ;
            acc(constants, vars, quad.getSubject()) ;
            acc(constants, vars, quad.getPredicate()) ;
            acc(constants, vars, quad.getObject()) ;
        }
    }

    private static void acc(Collection<Node>constants,  Collection<Var>vars, Node node)
    { 
        // ?? node.isConcrete()
        if ( node.isLiteral() || node.isBlank() || node.isURI() )
        {
            constants.add(node) ;
            return ;
        }
        if ( Var.isVar(node) )
        {
            vars.add(Var.alloc(node)) ;
            return ;
        }
        if ( node.isVariable() )
        {
            log.warn("Node_Varable but not a Var; bodged") ;
            vars.add(Var.alloc(node)) ;
            return ;
        }
        log.fatal("Unknown Node type: "+node) ;
        throw new SDBException("Unknown Node type: "+node) ;
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