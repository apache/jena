/*
 * (c) 2010 Talis Systems Ltd
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.IdentityHashMap ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap ;
import com.hp.hpl.jena.sparql.util.Symbol ;

public class E_BNode extends ExprFunctionN // 0 or one
{
    private static final String symbol = "bnode" ;
    
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
            return NodeValue.makeNode(Node.createAnon()) ;

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
            mapping = new IdentityHashMap<Binding, LabelToNodeMap>() ;
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
    protected NodeValue eval(List<NodeValue> args)
    { throw new ARQInternalErrorException() ; }

    @Override
    protected Expr copy(ExprList newArgs)
    {
        if ( newArgs.size() == 0 )
            return new E_BNode() ;
        else
            return new E_BNode(newArgs.get(0)) ;
    } 
}

/*
 * (c) 2010 Talis Systems Ltd
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
