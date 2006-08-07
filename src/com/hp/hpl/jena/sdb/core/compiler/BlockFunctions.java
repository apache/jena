/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.compiler;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.plan.PlanBlockTriples;
import com.hp.hpl.jena.query.engine1.plan.PlanFilter;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.sdb.engine.PlanSDB;

/**
 * Separated out code sequences
 * @author Andy Seaborne
 * @version $Id$
 */

public class BlockFunctions
{
    private static Log log = LogFactory.getLog(BlockFunctions.class) ;
    

    static public void classifyNodes(Collection<Triple> triples, Collection<Var> definedVars, Collection<Node> constants)
    {
        for ( Triple t : triples )
        {
            node(t.getSubject(), definedVars, constants) ;
            node(t.getPredicate(), definedVars, constants) ;
            node(t.getObject(), definedVars, constants) ;
        }
    }

    static private void node(Node node, Collection<Var> definedVars, Collection<Node> constants)
    {
        if ( node.isVariable() )
        {
            if ( definedVars != null )
            {
                Var v = new Var(node) ;
                if ( ! definedVars.contains(v) )
                    definedVars.add(v) ;
            }
        }
        else
        {
            if ( constants != null )
            {
                if ( ! constants.contains(node) )
                    constants.add(node) ;
            }
        }
    }
    
    // Allow filters to be moved later in the BGP
    private static boolean MaximiseBoundVariables = false ; 
    
    // Think: ?? should be done in ARQ => before PlanSDB'ization
    // No - do in PlanBasicGraphPattern.build hence do in SDB.
    // Do in BlockBGP!
    
    public static List<PlanElement> properOrder(List<PlanElement> planElements)
    {
        List<Triple> triples = new ArrayList<Triple>() ;
        List<Expr> filters = new ArrayList<Expr>() ;
        Set<Var> varsSeen = new HashSet<Var>() ;
        
        for ( PlanElement obj : planElements )
        {
            if ( obj instanceof PlanSDB )
            {
                PlanSDB planSDB = (PlanSDB)obj ;
                varsSeen.addAll(planSDB.getBlock().getDefinedVars()) ;
                continue ;
            }
            
            if ( obj instanceof PlanFilter )
            {
                PlanFilter filter = (PlanFilter)obj ;
                @SuppressWarnings("unchecked")
                Set<Var> vars = filter.getExpr().getVarsMentioned() ;
                if ( ! varsSeen.containsAll(vars) )
                    log.info("Out of order filter") ;
                continue ;
            }
            if ( obj instanceof PlanBlockTriples )
            {
                PlanBlockTriples tBlk = (PlanBlockTriples)obj ;
                @SuppressWarnings("unchecked")
                List<Triple> triplePattern = (List<Triple>)tBlk.getPattern() ;
                findVars(varsSeen, triplePattern) ;
                continue ;
            }
            log.warn("Unexpected plan element type: "+Utils.className(obj)) ;
        }
        return planElements ;
    }
    
    // Very like Block.getDefinedVars: Suggests there is common processing going on.
    // Maybe Blocks don't need to calculate defined vars, but can have setDefinedVars
    
    private static void findVars(Set<Var> varsSeen, List<Triple> triplePattern)
    {
        for ( Triple t : triplePattern )
        {
            node(t.getSubject(), varsSeen) ;
            node(t.getPredicate(), varsSeen) ;
            node(t.getObject(), varsSeen) ;
        }
    }

    private static void node(Node node, Set<Var> varsSeen)
    {
        if ( node.isVariable() )
        {
            Var v = new Var(node) ;
            varsSeen.add(v) ;
        }
    }
    
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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