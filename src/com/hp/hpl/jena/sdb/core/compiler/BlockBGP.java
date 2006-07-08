/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.compiler;

import java.util.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine1.QueryEngineUtils;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.condition.SDBConstraint;

import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;

public class BlockBGP extends BlockBase
{
    // FilteredBasicGraphPattern
    
    private List<Triple> triples = new  LinkedList<Triple>() ;
    private List<SDBConstraint> constraints = new  LinkedList<SDBConstraint>() ;
    
    public BlockBGP()
    {
    }
    
    public void add(Triple triple) { triples.add(triple) ; }
    public void add(SDBConstraint constraint) { constraints.add(constraint) ; }
    
    public List<Triple> getTriples() { return triples ; } 
    public List<SDBConstraint> getConstraints() { return constraints ; }
    
    // ---- Structure
    private boolean classified = false ;
    private Set<Var> definedVars = null ;
    private Set<Node> constants = null ;
    
    public Set<Var> getDefinedVars()
    {
        classify() ;
        return definedVars ;
    }

    public Set<Node> getConstants()
    {
        classify() ;
        return constants ;
    }
    
    private /*synchronized*/ void classify()
    {
        if ( classified )
            return ;
        constants = new LinkedHashSet<Node>() ;
        definedVars = new LinkedHashSet<Var>() ;
        BlockFunctions.classifyNodes(triples, definedVars, constants) ;
        classified = true ;
    }
    
    public SqlNode generateSQL(CompileContext context, QueryCompilerBase queryCompiler)
    {
        return queryCompiler.compile(this, context) ;
    }

    @Override
    public BlockBase substit(Binding binding)
    {
        BlockBGP block = new BlockBGP() ;
        
        for ( Triple t : triples )
        {
            if ( binding != null )
                t = QueryEngineUtils.substituteIntoTriple(t, binding) ;
            block.add(t) ;
        }
        return block ;
    }

    public void output(IndentedWriter out)
    {
        out.print("(BlockBGP") ;
        out.incIndent() ;   // Inc-1
//        out.print("(Vars") ;
//        for ( Var var : patternVars )
//        {
//            out.print(" ") ;
//            out.print(var.toString()) ;
//        }
//        
//        if ( patternVars.size() == 0 )
//            out.print("(<no vars>") ;
//        out.print(")") ;
//        
//        if ( projectVars != null )
//        {
//            String sep = "" ;
//            out.print(" [") ;
//            for ( Var var : projectVars )
//            {
//                out.print(sep) ;
//                sep = " "; 
//                out.print(var.toString()) ;
//            }
//            out.print("]") ;
//        }

        for ( Triple t : triples )
        {
            out.println() ;
            out.print(FmtUtils.stringForTriple(t, null)) ;
        }
        
        if ( constraints.size() > 0 )
        {
            out.println() ;
            for ( SDBConstraint c : constraints )
            {
                out.print("(Condition ") ;
                out.print(c.toString()) ;
                out.print(")") ;
            }
        }
        out.decIndent() ; // Dec-1
        out.println();
        out.print(")") ;
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