/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.pfunction;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.IterLib;

/** Basic property function handler that calls the implementation 
 * subclass one binding at a time
 * @author Andy Seaborne
 */ 

public abstract class PropertyFunctionBase implements PropertyFunction
{
    PropFuncArgType subjArgType ;
    PropFuncArgType objFuncArgType ;
    
    protected PropertyFunctionBase()
    {
        this(PropFuncArgType.PF_ARG_EITHER, PropFuncArgType.PF_ARG_EITHER) ;
    }
    
    protected PropertyFunctionBase(PropFuncArgType subjArgType,  PropFuncArgType objFuncArgType)
    {
        this.subjArgType = subjArgType ;
        this.objFuncArgType = objFuncArgType ;
    }

    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    { 
        if ( subjArgType.equals(PropFuncArgType.PF_ARG_SINGLE) )
            if ( argSubject.isList() )
                throw new QueryBuildException("List arguments (subject) to "+predicate.getURI()) ;
        
        if ( subjArgType.equals(PropFuncArgType.PF_ARG_LIST) )
            if ( ! argSubject.isList() )
                throw new QueryBuildException("Single argument, list expected (subject) to "+predicate.getURI()) ;

        if ( objFuncArgType.equals(PropFuncArgType.PF_ARG_SINGLE) )
            if ( argObject.isList() )
            {
                if ( ! argObject.isNode() )
                    // But allow rdf:nil.
                    throw new QueryBuildException("List arguments (object) to "+predicate.getURI()) ;
            }
        
        if ( objFuncArgType.equals(PropFuncArgType.PF_ARG_LIST) )
            if ( ! argObject.isList() )
                throw new QueryBuildException("Single argument, list expected (object) to "+predicate.getURI()) ;
    }


    
    public QueryIterator exec(QueryIterator input, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        return new RepeatApplyIterator(input, argSubject, predicate, argObject, execCxt) ;
    }
    
    public abstract QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) ;
    
    
    class RepeatApplyIterator extends QueryIterRepeatApply
    {
        private ExecutionContext execCxt ;
        private PropFuncArg argSubject ; 
        private Node predicate ;
        private PropFuncArg argObject ;
        
       public RepeatApplyIterator(QueryIterator input, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
       { 
           super(input, execCxt) ;
           this.argSubject = argSubject ;
           this.predicate = predicate ;
           this.argObject = argObject ;
       }

        @Override
        protected QueryIterator nextStage(Binding binding)
        {
            QueryIterator iter = exec(binding, argSubject, predicate, argObject, super.getExecContext()) ;
            if ( iter == null ) 
                iter = IterLib.noResults(execCxt) ;
            return iter ;
        }
        
        @Override
        protected void details(IndentedWriter out, SerializationContext sCxt)
        {
            out.print("PropertyFunction ["+FmtUtils.stringForNode(predicate, sCxt)+"]") ;
            out.print("[") ;
            argSubject.output(out, sCxt) ;
            out.print("][") ;
            argObject.output(out, sCxt) ;
            out.print("]") ;
            out.println() ;
        }
    }
    
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