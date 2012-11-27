/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.pfunction;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.QueryBuildException ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.IterLib ;

/** Basic property function handler that calls the implementation 
 * subclass one binding at a time */ 

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

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    { 
        if ( subjArgType.equals(PropFuncArgType.PF_ARG_SINGLE) )
            if ( argSubject.isList() )
                throw new QueryBuildException("List arguments (subject) to "+predicate.getURI()) ;
        
        if ( subjArgType.equals(PropFuncArgType.PF_ARG_LIST) && ! argSubject.isList() )
                throw new QueryBuildException("Single argument, list expected (subject) to "+predicate.getURI()) ;

        if ( objFuncArgType.equals(PropFuncArgType.PF_ARG_SINGLE) && argObject.isList() )
        {
            if ( ! argObject.isNode() )
                // But allow rdf:nil.
                throw new QueryBuildException("List arguments (object) to "+predicate.getURI()) ;
        }
        
        if ( objFuncArgType.equals(PropFuncArgType.PF_ARG_LIST) )
            if ( ! argObject.isList() )
                throw new QueryBuildException("Single argument, list expected (object) to "+predicate.getURI()) ;
    }


    
    @Override
    public QueryIterator exec(QueryIterator input, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        // This is the property function equivalent of Substitute.
        // To allow property functions to see the whole input stream,
        // the exec() operation allows the PF implementation to get at the
        // input iterator.  Normally, we just want that applied one binding at a time.

        return new RepeatApplyIteratorPF(input, argSubject, predicate, argObject, execCxt) ;
    }
    
    public abstract QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) ;
    
    
    class RepeatApplyIteratorPF extends QueryIterRepeatApply
    {
        private ExecutionContext execCxt ;
        private PropFuncArg argSubject ; 
        private Node predicate ;
        private PropFuncArg argObject ;
        
       public RepeatApplyIteratorPF(QueryIterator input, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
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
