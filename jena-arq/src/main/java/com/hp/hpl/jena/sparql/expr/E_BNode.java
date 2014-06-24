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

package com.hp.hpl.jena.sparql.expr;

import java.util.IdentityHashMap ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap ;
import com.hp.hpl.jena.sparql.util.Symbol ;

public class E_BNode extends ExprFunctionN // 0 or one
{
    private static final String symbol = Tags.tagBNode ;
    
    private static final Symbol keyMap = Symbol.create("arq:internal:bNodeMappings") ;

    public E_BNode() { this(null) ; }
    
    public E_BNode(Expr expr)
    {
        // Expr maybe null for BNode()
        super(symbol, expr) ;
    }
    
    // Not really a special form but we need access to 
    // the binding to use a key.
    @Override
    public NodeValue evalSpecial(Binding binding, FunctionEnv env)
    {
        Expr expr = null ;
        if ( args.size() == 1 )
            expr = getArg(1) ;

        if ( expr == null )
            return NodeValue.makeNode(NodeFactory.createAnon()) ;

        NodeValue x = expr.eval(binding, env) ;
        if ( ! x.isString() )
            throw new ExprEvalException("Not a string: "+x) ;

        Integer key = System.identityHashCode(binding) ;

        // IdentityHashMap
        // Normally bindings have structural equality (e.g. DISTINCT)
        // we want identify as OpAssign/OpExtend mutates a binding to add new pairs.
        @SuppressWarnings("unchecked")
        IdentityHashMap<Binding, LabelToNodeMap> mapping = (IdentityHashMap<Binding, LabelToNodeMap>)env.getContext().get(keyMap) ;

        if ( mapping == null )
        {
            mapping = new IdentityHashMap<>() ;
            env.getContext().set(keyMap, mapping) ;
        }        
        LabelToNodeMap mapper = mapping.get(binding) ;
        if ( mapper == null )
        {
            mapper = LabelToNodeMap.createBNodeMap() ;
            mapping.put(binding, mapper) ;
        }

        Node bnode = mapper.asNode(x.getString()) ;
        return NodeValue.makeNode(bnode) ; 
    }
    
    @Override
    public NodeValue eval(List<NodeValue> args)
    { throw new ARQInternalErrorException() ; }

    @Override
    public Expr copy(ExprList newArgs)
    {
        if ( newArgs.size() == 0 )
            return new E_BNode() ;
        else
            return new E_BNode(newArgs.get(0)) ;
    } 
}
