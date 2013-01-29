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

package com.hp.hpl.jena.enhanced;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * <p>
 * A specialisation of Polymorphic that models an extended node in a an extended graph. An extended node
 * wraps a normal node, and adds additional convenience access or user affordances, though the state
 * remains in the graph itself.
 * </p>
*/
public class EnhNode extends Polymorphic<RDFNode> implements FrontsNode
{
    
    /** The graph node that this enhanced node is wrapping */
    final protected Node node;
    
    /** The enhanced graph containing this node */
    final protected EnhGraph enhGraph;
    
    public EnhNode( Node n, EnhGraph g ) {
        super();
        node=n;
        enhGraph = g;
   }

    // External interface
    
    /** 
     * Answer the graph node that this enhanced node wraps
     * @return A plain node
     */
    @Override
    public Node asNode() {
        return node;
    }
    
    /**
     * Answer the graph containing this node
     * @return An enhanced graph 
     */
    public EnhGraph getGraph() {
        return enhGraph;
    }
    
    /**
        An enhanced node is Anon[ymous] iff its underlying node is Blank.
    */
    public final boolean isAnon() {
        return node.isBlank();
    }
    
    /**
        An enhanced node is Literal iff its underlying node is too.
    */
    public final boolean isLiteral() {
        return node.isLiteral();
    }
 
    /**
        An enhanced node is a URI resource iff its underlying node is too.
    */
    public final boolean isURIResource() {
        return node.isURI();
    }
    
    /**
        An enhanced node is a resource if it's node is a URI node or a blank node.
    */
    public final boolean isResource() {
        return node.isURI() || node.isBlank();
    }
    
    /**
     * Answer a facet of this node, where that facet is denoted by the
     * given type.
     * 
     * @param t A type denoting the desired facet of the underlying node
     * @return An enhanced nodet that corresponds to t; this may be <i>this</i>
     *         Java object, or a different object.
     */ 
    public <X extends RDFNode> X viewAs( Class<X> t ) {
        return asInternal( t ); 
    }
    
    /** allow subclasses to implement RDFNode & its subinterface */
    public <T extends RDFNode> T as( Class<T> t )
        { return asInternal( t ); }
      
    /**
        API-level method for polymorphic testing
    */
    public <X extends RDFNode> boolean canAs( Class<X> t )
        { return canSupport( t ); }
        
    /**
     * The hash code of an enhanced node is defined to be the same as the underlying node.
     * @return The hashcode as an int
     */
    @Override final public int hashCode() {
     	return node.hashCode();
    }
       
    /**
     * An enhanced node is equal to another enhanced node n iff the underlying 
     * nodes are equal. We generalise to allow the other object to be any class
     * implementing asNode, because we allow other implemementations of
     * Resource than EnhNodes, at least in principle.
     * This is deemed to be a complete and correct interpretation of enhanced node
     * equality, which is why this method has been marked final.
     * 
     * @param o An object to test for equality with this node
     * @return True if o is equal to this node.
     */
    @Override final public boolean equals( Object o )
        { return o instanceof FrontsNode && node.equals(((FrontsNode) o).asNode()); }
    
    @Override public boolean isValid()
        { return true; }
        
    /** 
        Answer an new enhanced node object that presents <i>this</i> in a way 
        which satisfies type <code>t</code>. The new object is linked into this
        object's sibling ring. If the node cannot be converted, throw an
        UnsupportedPolymorphismException.
    */
    @Override protected <X extends RDFNode> X convertTo( Class<X> t ) 
        {
        EnhGraph eg = getGraph();
        if (eg == null) throw new UnsupportedPolymorphismException( this, false, t );
        Implementation imp = getPersonality().getImplementation( t );
        if (imp == null) throw new UnsupportedPolymorphismException( this, true, t );
        EnhNode result = imp.wrap( asNode(), eg );          
        this.addView( result );
        return t.cast( result );
        }
    
    /**
        answer true iff this enhanced node can support the class <code>t</code>,
        ie it is already a value <code>t</code> or it can be reimplemented
        as a <code>t</code> via the graph's personality's implementation.
        If this node has no graph, answer false.       
    */
    @Override protected <X extends RDFNode> boolean canSupport( Class<X> t )
        {
        if (alreadyHasView( t )) return true;
        if (getGraph() == null) return false;
        Implementation imp = getPersonality().getImplementation( t );
        return imp == null ? false : imp.canWrap( asNode(), getGraph() );
        }

    /**
     * Answer the personality object bound to this enhanced node, which we obtain from 
     * the associated enhanced graph.
     * 
     * @return The personality object
     */
    @Override protected Personality<RDFNode> getPersonality() {
        return getGraph().getPersonality();
    }
    
}
