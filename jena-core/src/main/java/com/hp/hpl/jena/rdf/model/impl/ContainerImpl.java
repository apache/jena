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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;

/** An internal class not normally of interest to application developers.
 *  A base class on which the other containers are built.
*/

public class ContainerImpl extends ResourceImpl
                           implements Container, ContainerI {
    
    static NodeIteratorFactory iteratorFactory;
    
    static {
        iteratorFactory = new ContNodeIteratorFactoryImpl();
    }

    /** Creates new ContainerImpl */
    public ContainerImpl( ModelCom model ) {
        super(model);
    }
    
    public ContainerImpl( String uri, ModelCom model ){
        super(uri, model);
    }
    
    public ContainerImpl(Resource r, ModelCom  model) {
        super(r.asNode(), model);
    }
    
    public ContainerImpl(Node n, EnhGraph g) {
        super(n,g);
    }
    
    protected ContainerImpl( Resource r )
        { this( r, (ModelCom) r.getModel() ); }
    
    private boolean is( Resource r ) {
        return hasProperty(RDF.type, r);
    }
    
    @Override
    public boolean isAlt() {
        return is(RDF.Alt);
    }
    
    @Override
    public boolean isBag() {
        return is(RDF.Bag);
    }
    
    @Override
    public boolean isSeq() {
        return is(RDF.Seq);
    }
    
    @Override
    public Container add(RDFNode n)  {
        int i = size();
        addProperty(RDF.li(i+1), n);
        return this;
    } 
    
    @Override
    public Container add(boolean o)  {
        return add( String.valueOf( o ) );
    }
    
    @Override
    public Container add(long o)  {
        return add( String.valueOf( o ) );
    }
    
    @Override
    public Container add(char o)  {
        return add( String.valueOf( o ) );
    }
    
    @Override
    public Container add( float o )  {
        return add( String.valueOf( o ) );
    }
    
    @Override
    public Container add(double o)  {
        return add( String.valueOf( o ) );
    }

    @Override
    public Container add(Object o)  {
        return add( String.valueOf( o ) );
    }
     
    @Override
    public Container add(String o)  {
        return add( o, "" );
    }
    
    @Override
    public Container add(String o, String l)  {
        return add( literal( o, l ) );
    }
    
    @Override
    public boolean contains(RDFNode n)  {
        return containerContains( n );
    }

    @Override
    public boolean contains(boolean o)  {
        return contains( String.valueOf( o ) );
    }

    @Override
    public boolean contains(long o)  {
        return contains( String.valueOf( o ) );
    }

    @Override
    public boolean contains(char o)  {
        return contains( String.valueOf( o ) );
    }

    @Override
    public boolean contains(float o)  {
        return contains( String.valueOf( o ) );
    }

    @Override
    public boolean contains(double o)  {
        return contains( String.valueOf( o ) );
    }

    @Override
    public boolean contains(Object o)  {
        return contains( String.valueOf( o ) );
    }
    
    @Override
    public boolean contains(String o)  {
        return contains( o, "" );
    }
    
    @Override
    public boolean contains( String o, String l )  {
        return contains( literal( o, l ) );
    }

    private Literal literal( String s, String lang )
        { return new LiteralImpl( NodeFactory.createLiteral( s, lang, false ), getModelCom() ); }
            
    @Override
    public NodeIterator iterator()  
        { return listContainerMembers( iteratorFactory ); }
        
    @Override
    public int size()  
        {
        int result = 0;
        StmtIterator iter = listProperties();
        while (iter.hasNext()) 
            if (iter.nextStatement().getPredicate().getOrdinal() != 0) result += 1;
        iter.close();
        return result;
        }
        
    @Override
    public Container remove(Statement s)  {
        int size = size();
        Statement last = null;
        if (s.getPredicate().getOrdinal() == size) {       // if last
            getModel().remove(s);
        } else {
            last = getModel().getRequiredProperty(this, RDF.li(size));
            s.changeObject(last.getObject());
            getModel().remove(last);
        }
        if (size() != (size -1)) 
            throw new AssertionFailureException( "container size" ); 
        return this;
    }
    
    @Override
    public Container remove(int index, RDFNode object)  {
        remove(getModel().createStatement(this, RDF.li(index), object));
        return this;
    }
        
   /**
        Answer an iterator over the members of this container.
        @param f the factory for constructing the final iterator
        @return the member iterator
   */
   public NodeIterator listContainerMembers( NodeIteratorFactory f )
        {
        StmtIterator iter = listProperties(); 
        Vector<Statement> result = new Vector<>();
        int maxOrdinal = 0;
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            int ordinal = stmt.getPredicate().getOrdinal();
            if (ordinal != 0) {
                if (ordinal > maxOrdinal) {
                    maxOrdinal = ordinal;
                    result.setSize(ordinal);
                }
                result.setElementAt(stmt, ordinal-1);
            }
        }
        iter.close();
        return f.createIterator( result.iterator(), result, this );
    }  
    
    public int containerIndexOf( RDFNode n )  {
        int result = 0;
        StmtIterator iter = listProperties();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            int ordinal = stmt.getPredicate().getOrdinal();
            if (ordinal != 0 && n.equals( stmt.getObject() )) {
                result = ordinal;
                break;
            }
        }
        iter.close();
        return result;
    }
    
   public boolean containerContains( RDFNode n)
        { return containerIndexOf( n ) != 0; }
            
}
