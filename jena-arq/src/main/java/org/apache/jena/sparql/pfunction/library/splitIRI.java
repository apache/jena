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

package org.apache.jena.sparql.pfunction.library;

import java.util.Objects;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.QueryBuildException ;
import org.apache.jena.query.QueryException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.pfunction.PropFuncArgType ;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval ;
import org.apache.jena.sparql.util.IterLib ;
import org.apache.jena.sparql.util.NodeUtils ;

public class splitIRI extends PropertyFunctionEval
{
    public splitIRI()
    {
        super(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_LIST) ;
    }

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        // Do some checking.
        // These checks are assumed to be passed in .exec()
        if ( argSubject.isList() )
            throw new QueryBuildException(Lib.className(this)+ "Subject must be a single node or variable, not a list") ;
        if ( ! argObject.isList() )
            throw new QueryBuildException(Lib.className(this)+ "Object must be a list of two elements") ;
        if ( argObject.getArgList().size() != 2 )
            throw new QueryBuildException(Lib.className(this)+ "Object is a list but it has "+argObject.getArgList().size()+" elements - should be 2") ;
    }

    // Implementing .exec requires considering all the cases of variable being
    // bound/constants or unbound variables.  If an unexpected case arises, or
    // one the implementation can't fulfil, then give warning and return
    // QueryIterNullIterator or a null.
    //
    // Do not throw an exception except when an internal error situation occurs.

    @Override
    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        try {
            // Subject bound to something other a URI.
            if ( argSubject.getArg().isLiteral() || argSubject.getArg().isBlank() )
                // Only split IRIs
                return IterLib.noResults(execCxt) ;

            if ( argSubject.getArg().isURI() )
                // Case 1 : subject is a fixed URI or a variable bount to a URI.
                return subjectIsIRI(argSubject.getArg(), argObject, binding, execCxt) ;
            else
                // Case 2 : subject is an unbound variable.
                return subjectIsVariable(argSubject.getArg(), argObject, execCxt) ;
        } catch (QueryException ex)
        {
            Log.warn(this, "Unexpected problems in splitIRI: "+ex.getMessage()) ;
            return null ;
        }
    }

    private QueryIterator subjectIsIRI(Node subject, PropFuncArg argObject, Binding binding, ExecutionContext execCxt)
    {
        String namespace = subject.getNameSpace() ;
        String localname = subject.getLocalName() ;

        Node namespaceNode = argObject.getArg(0) ;
        Node localnameNode = argObject.getArg(1) ;

        // New binding to return.
        BindingBuilder builder = null ;
        if ( Var.isVar(namespaceNode) || Var.isVar(localnameNode) )
            builder = Binding.builder(binding) ;

        if ( Var.isVar(namespaceNode) )
        {
            builder.add(Var.alloc(namespaceNode), NodeFactory.createURI(namespace)) ;
            // Check for the case of (?x ?x) (very unlikely - and even more unlikely to cause a match)
            // but it's possible for strange URI schemes.
            if ( localnameNode.isVariable() && Objects.equals(namespaceNode, localnameNode) )
                // Set localnameNode to a constant which will get checked below.
                localnameNode = NodeFactory.createURI(namespace) ;
        }
        else
        {
            String ns = null ;
            // Allow both IRIs and plain literals in the namespace position.
            if ( namespaceNode.isURI() )
                ns = namespaceNode.getURI() ;
            if ( namespaceNode.isLiteral() )
                ns = NodeUtils.stringLiteral(namespaceNode) ;
            if ( ns == null || ! ns.equals(namespace) )
                return IterLib.noResults(execCxt) ;
            // Fall through and proceed to localname
        }

        if ( Var.isVar(localnameNode) )
            builder.add(Var.alloc(localnameNode), NodeFactory.createLiteral(localname)) ;
        else
        {
            // Only string literals (plain strings or datatype xsd:string)
            String lc = NodeUtils.stringLiteral(localnameNode) ;
            if ( lc == null || ! lc.equals(localname) )
                return IterLib.noResults(execCxt) ;
        }

        Binding b2 = ( builder == null ) ? binding : builder.build() ;
        return IterLib.result(b2, execCxt) ;
    }

    private QueryIterator subjectIsVariable(Node arg, PropFuncArg argObject, ExecutionContext execCxt)
    {
        Log.warn(this, "Subject to property function splitURI is not a bound nor a constant.") ;
        return IterLib.noResults(execCxt) ;
    }
}
