/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: EnhNode.java,v 1.8 2003-11-20 16:53:13 chris-dollin Exp $
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
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a> (original code)<br>
 *         <a href="mailto:Chris.Dollin@hp.com">Chris Dollin</a> (original code)<br>
 *         <a href="mailto:Ian.Dickinson@hp.com">Ian Dickinson</a> (refactoring and commentage)
*/
public class EnhNode 
    extends Polymorphic
{
    
    /** The graph node that this enhanced node is wrapping */
    final protected Node node;
    
    /** The enhanced graph containing this node */
    final protected EnhGraph enhGraph;
    
    public EnhNode(Node n,EnhGraph g ) {
        super();
        node=n;
        enhGraph = g;
   }

    // External interface
    
    /** 
     * Answer the graph node that this enhanced node wraps
     * @return A plain node
     */
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
     * Answer a facet of this node, where that facet is denoted by the
     * given type.
     * 
     * @param t A type denoting the desired facet of the underlying node
     * @return An enhanced nodet that corresponds to t; this may be <i>this</i>
     *         Java object, or a different object.
     */ 
    public EnhNode viewAs( Class t ) {
        return (EnhNode) asInternal( t ); 
    }
    
    /** allow subclasses to implement RDFNode & its subinterface */
    public RDFNode as( Class t )
        { return (RDFNode) viewAs( t ); }
      
    /**
        API-level method for polymorphic testing
    */
    public boolean canAs( Class t )
        { return canSupport( t ); }
        
    /**
     * The hash code of an enhanced node is defined to be the same as the underlying node.
     * @return The hashcode as an int
     */
    final public int hashCode() {
     	return node.hashCode();
    }
       
    /**
     * An enhanced node is equal to another enhanced node n iff the underlying 
     * nodes are equal. We generalise to allow the other object to be any class
     * implementing HasNode, because we allow other implemementations of
     * Resource than EnhNodes, at least in principle.
     * This is deemed to be a complete and correct interpretation of enhanced node
     * equality, which is why this method has been marked final.
     * 
     * @param o An object to test for equality with this node
     * @return True if o is equal to this node.
     */
    final public boolean equals( Object o )
        { return o instanceof FrontsNode && node.equals(((FrontsNode) o).asNode()); }
    
    public boolean isValid()
        { return true; }
        
    /** 
     * Answer an enhanced node object that presents <i>this</i> in a way which satisfies type
     * t.
     * @param t A type
     * @return A polymorphic instance that conforms to t.
     */
    protected Polymorphic convertTo( Class t ) 
        {
        EnhGraph eg = getGraph();
        Implementation imp = getPersonality().getImplementation( t );
        if (imp == null) throw new UnsupportedPolymorphismException( eg, t );
        Polymorphic result = imp.wrap( asNode(), eg );          
        this.addView( result );
        return result;
        }
    
    /**
        answer true iff this enhanced node can support the class _t_, ie, it can be
        converted to an instance of t.
        @param t the (interface) class being tested
        @return true iff the implementation can wrap this enhanced node
    */
    protected boolean canSupport( Class t )
        {
        return getPersonality().getImplementation( t ).canWrap( asNode(), getGraph() );
        }

    /**
     * Answer the personality object bound to this enhanced node, which we obtain from 
     * the associated enhanced graph.
     * 
     * @return The personality object
     */
    protected Personality getPersonality() {
        return ((GraphPersonality) getGraph().getPersonality()).nodePersonality();
    }
    
}

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
