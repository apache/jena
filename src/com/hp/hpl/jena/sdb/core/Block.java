/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.engine1.QueryEngineUtils;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.query.util.Printable;
import com.hp.hpl.jena.sdb.condition.SDBConstraint;


/** A unit of SQL execution - a basic pattern, filter clauses and optionals.
 *  It is structured as found in the original query (for example, conditions
 *  have not been moved around to reflect SQL requirements).
 *    
 * @author Andy Seaborne
 * @version $Id: Block.java,v 1.34 2006/04/20 17:18:31 andy_seaborne Exp $
 */  
public class Block implements Printable
{
    private static Log log = LogFactory.getLog(Block.class) ;
    
    List<Node> patternVars = new ArrayList<Node>() ;
    List<Var> filterVars   = new ArrayList<Var>() ;
    List<Node> projectVars = null ;
    
    BasicPattern basicPattern = new BasicPattern() ;
    
    List<SDBConstraint> blockConstraints = new ArrayList<SDBConstraint>() ;
    
    List<Block>blockOptionals  = new ArrayList<Block>() ;

    public Block() {}

    public void add(BasicPattern bp)
    {
        for ( Triple t : bp )
            add(t) ;
    }
    
    // Accumulate triples
    public void add(Triple triple)
    { basicPattern.add(triple) ; accVar(patternVars, triple) ; }

    // Accumulate constraints we understand for this block basic patterns
    public void add(SDBConstraint constraint)
    { 
        blockConstraints.add(constraint) ;
        constraint.varsMentioned(filterVars) ;
    }
    
    // Accumulate optionals
    public void addOptional(Block optBlock)
    { checkOptional(optBlock) ; blockOptionals.add(optBlock) ; }
    
    // Access 
    
    public BasicPattern        getBasicPattern()      { return basicPattern ; }
    public List<Block>         getOptionals()         { return blockOptionals ; }
    public List<SDBConstraint> getConstraints()       { return blockConstraints ; }
    public List<Node>          getPatternVars()       { return patternVars ; }
    public List<Var>           getFilterVars()        { return filterVars ; }
    public List<Node>          getProjectVars()       { return projectVars ; }
    public void                addProjectVar(Node var)             
    { 
        if ( projectVars == null )
            projectVars = new ArrayList<Node>() ;
        projectVars.add(var) ;
    }
    
    public List<Node>        getDefinedVars()         { return BlockNodes.definedVars(this) ; }

    // Turn a block into another block, after substituting for variables. 
    
    public Block substitute(Binding binding)
    {
        Block block = new Block() ;
        
        for ( Triple t : basicPattern )
        {
            if ( binding != null )
                t = QueryEngineUtils.substituteIntoTriple(t, binding) ;
            block.add(t) ;
        }

//        for ( Constraint c : blockConstraints )
//        {
//        }
            
        for ( Block opt : blockOptionals )
        {
            Block blockOpt = opt.substitute(binding) ;
            block.addOptional(blockOpt) ;
        }
            
        block.projectVars = projectVars ;
        block.blockConstraints = blockConstraints ;
        return block ;
    }

    public void apply(BlockProc proc, boolean deep)
    {
        proc.basicPattern(basicPattern) ;
        for ( SDBConstraint c : blockConstraints )
            proc.restriction(c) ;
        for ( Block optBlk : blockOptionals )
        {
            proc.optional(optBlk) ;
            if ( deep ) optBlk.apply(proc, deep) ;
        }
    }
    
    // ----------------
    
    private static void accVar(Collection<Node> acc, Triple triple)
    {
        accVar(acc, triple.getSubject()) ;
        accVar(acc, triple.getPredicate()) ;
        accVar(acc, triple.getObject()) ;
    }
    
    private static void accVar(Collection<Node> acc, Node node)
    {
        if ( node.isVariable() ) 
        {
            if ( !acc.contains(node) )
                acc.add(node) ;
            return ;
        }
        // Constant
        
    }
    

    private void checkOptional(Block optBlock)
    {
        for ( Node v : optBlock.getPatternVars() )
        {
            // Look for multiple use free vars
            if ( ! patternVars.contains(v) )
            {
                // If not used by outer (this) then see if used
                // in an optional already part of this Block.
                for ( Block b : blockOptionals )
                {
                    if (  b.getPatternVars().contains(v) ) 
                        log.warn("Unconstrained, multiple use, variable: "+v) ;
                }
            }
        }
    }

    // ---- Appearance
    
    @Override
    public String toString()
    {
        return FmtUtils.toString(this) ;
    }

    public void output(IndentedWriter out)
    {
        out.println("(Block") ;
        out.incIndent() ;   // Inc-1
        out.print("(Vars") ;
        for ( Node n : patternVars )
        {
            out.print(" ") ;
            out.print(n.toString()) ;
        }
        
        if ( patternVars.size() == 0 )
            out.print("(<no vars>") ;
        out.print(")") ;
        
        if ( projectVars != null )
        {
            String sep = "" ;
            out.print(" [") ;
            for ( Node n : projectVars )
            {
                out.print(sep) ;
                sep = " "; 
                out.print(n.toString()) ;
            }
            out.print("]") ;
        }

        for ( Triple t : basicPattern )
        {
            out.println() ;
            out.print(FmtUtils.stringForTriple(t, null)) ;
        }
        
        if ( this.blockConstraints.size() > 0 )
        {
            out.println() ;
            for ( SDBConstraint c : blockConstraints )
            {
                out.print("(Condition ") ;
                out.print(c.toString()) ;
                out.print(")") ;
            }
        }
        
        if ( blockOptionals.size() > 0 )
        {
            out.println() ;
            out.print("(Optionals") ;
            out.incIndent() ;
            
            for ( Block b : blockOptionals )
            {
                out.println() ;
                b.output(out) ;
            }
            out.print(")") ;
            out.decIndent() ;
        }
        out.decIndent() ; // Dec-1
        out.println();
        out.print(")") ;
    }
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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