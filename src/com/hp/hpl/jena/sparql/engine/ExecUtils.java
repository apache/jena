/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot;

/**
 * @author     Andy Seaborne
 * @version    $Id: QueryEngineUtils.java,v 1.14 2007/02/06 17:06:06 andy_seaborne Exp $
 */
 
public class ExecUtils
{
    public static Log log = LogFactory.getLog(ExecUtils.class) ;
    
    public static QueryIterator makeRoot(ExecutionContext execCxt)
    {
        return QueryIterRoot.create(execCxt) ;
    }
  
    public static void compilePattern(com.hp.hpl.jena.graph.query.Query graphQuery,
                               List pattern, Binding presets, Set vars)
    {
        if ( pattern == null )
            return ;
        for (Iterator iter = pattern.listIterator(); iter.hasNext();)
        {
            Triple t = (Triple) iter.next();
            t = BindingUtils.substituteIntoTriple(t, presets) ;
            if ( vars != null )
            {
                if ( t.getSubject().isVariable() )
                    vars.add(t.getSubject()) ;
                if ( t.getPredicate().isVariable() )
                    vars.add(t.getPredicate()) ;
                if ( t.getObject().isVariable() )
                    vars.add(t.getObject()) ;
            }
            graphQuery.addMatch(t);
        }
    }
    
    public static void compileConstraints(com.hp.hpl.jena.graph.query.Query graphQuery, List constraints)
    {
        log.warn("Call to compileConstraints for Jena Expressions") ;
    }
    
    public static Var[] projectionVars(Set vars)
    {
        Var[] result = new Var[vars.size()] ;
    
        int i = 0 ; 
        for ( Iterator iter = vars.iterator() ; iter.hasNext() ; )
        {
            // Or Var.alloc((Node)iter.next()) ;
            Var n = (Var)iter.next() ;
            result[i] = n ;
            i++ ;
        }
        return result ;
    }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
