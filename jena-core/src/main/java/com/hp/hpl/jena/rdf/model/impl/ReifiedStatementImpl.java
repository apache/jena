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

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;


/**
    A ReifiedStatementImpl encodes a Statement and behaves as a Resource.
*/

public class ReifiedStatementImpl extends ResourceImpl implements ReifiedStatement
    {
    /** the Statement that this ReifiedStatement represents */
    protected Statement statement;
    
    /**
        private constructor, relies (ugh) on super(uri, model) generating
        bnode if uril == null. 
    */

    private ReifiedStatementImpl( ModelCom m, String uri, Statement s ) 
        {
        super( uri, m ); 
        assertStatement( s ); 
        }
        
    protected ReifiedStatementImpl( EnhGraph m, Node n, Statement s )
        {
        super( n, m );
        assertStatement( s );
        }
        
    private void assertStatement( Statement s )
        {
        statement = s;
        }
        
    /** 
        answer [a .equals() version of] the Statement that this ReifiedStatement
        represents.
    */
    @Override
    public Statement getStatement() 
        { return statement; }
       
    static final public Implementation reifiedStatementFactory = new Implementation() 
        {
        /**
            convert a _node_ into a ReifiedStatement in the enhanced graph 
            _eg_ by looking into this graph's reifier to find the binding for the
            node; throw a DoesNotReify exception if there's no mapping. 
        */
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) 
            {
            Triple x = getTriple( eg, n );
            if (x == null) throw new DoesNotReifyException( n );
            Statement st = StatementImpl.toStatement( x, (ModelCom) eg );
            return new ReifiedStatementImpl( eg, n, st );
            }

        /**
            Answer true iff the node <code>n</code> can become a reified statement,
            ie it is associated with a triple by <code>eg</code>'s Reifier.
            @param eg the (enhanced) graph who's Reifier might hold the triple
            @param n the node who's triple is required
            @return true iff there's an associated triple
        */
        @Override
        public boolean canWrap( Node n, EnhGraph eg )
            { return getTriple( eg, n ) != null; }
            
        /**
            Answer the triple associated with <code>n</code> by eg's graph's Reifier.
            @param eg the (enhanced) graph who's Reifier might hold the triple
            @param n the node who's triple is required
            @return the associated triple if any, otherwise null
        */
        private Triple getTriple( EnhGraph eg, Node n )
            { return ReifierStd.getTriple(eg.asGraph(), n ); }
        };
        
//    /**
//        Answer our Reifier (ie our Model's Graph's Reifier).
//    */
//    protected Reifier getReifier()
//        { return getModel().getGraph().getReifier(); }
        
    @Override
    public boolean isValid()
        { return ReifierStd.getTriple(getModel().getGraph(), this.asNode() ) != null; }
        
    /**
        tell the underlying graph's reifier that this ReifiedStatement's node
        represents any Statement with this statement's triple. (May throw an
        exception if the node is already reified to something different.)
    */        
    private ReifiedStatementImpl installInReifier()
        {
        ReifierStd.reifyAs(getModel().getGraph(), this.asNode(), statement.asTriple() );
        return this;
        }
      
    /**
        factory method. answer a ReifiedStatement which encodes the
        Statement _s_. The mapping is remembered.
    */  
    public static ReifiedStatement create( Statement s )
        { return create( (ModelCom) s.getModel(), (String) null, s ); }

    /**
        factory method. answer a ReifiedStatement which encodes the
        Statement _s_ with uri _uri_. The mapping is remembered.
    */        
    public static ReifiedStatementImpl create( ModelCom m, String uri, Statement s )
        { return new ReifiedStatementImpl( m, uri, s ).installInReifier(); }        
        
    public static ReifiedStatementImpl create( EnhGraph eg, Node n, Statement s )
        { return new ReifiedStatementImpl( eg, n, s ).installInReifier(); }
    
    @Override
    public String toString()
        { return super.toString() + "=>" + statement; }

    public static ReifiedStatement createExistingReifiedStatement( ModelCom model, Node n )
        {
        Triple t = ReifierStd.getTriple(model.getGraph(), n );
        return new ReifiedStatementImpl( model, n, model.asStatement( t ) );
        }
    }
