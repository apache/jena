/*
 *  (c) Copyright Hewlett-Packard Company 2000 
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
 *
 * ContainerImpl.java
 *
 * Created on 08 August 2000, 16:33
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;

/** An internal class not normally of interest to application developers.
 *  A base class on which the other containers are built.
 *
 * @author  bwm
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.3 $' Date='$Date: 2003-04-15 12:43:41 $'
 */
public class ContainerImpl extends ResourceImpl
                           implements Container, ContainerI {
    
    static NodeIteratorFactory iteratorFactory;
    
    static {
        iteratorFactory = new ContNodeIteratorFactoryImpl();
    }

    /** Creates new ContainerImpl */
    public ContainerImpl(Model model) {
        super(model);
    }
    
    public ContainerImpl(String uri, Model model){
        super(uri, model);
    }
    
    public ContainerImpl(Resource r, Model  model) {
        super(r.asNode(), model);
    }
    
    public ContainerImpl(Node n, EnhGraph g) {
        super(n,g);
    }
    
    protected ContainerImpl( Resource r )
        { this( r, r.getModel() ); }
    
    private boolean is( Resource r ) {
        return hasProperty(RDF.type, r);
    }
    public boolean isAlt() {
        return is(RDF.Alt);
    }
    public boolean isBag() {
        return is(RDF.Bag);
    }
    public boolean isSeq() {
        return is(RDF.Seq);
    }
    public Container add(RDFNode n) throws RDFException {
        int i = size();
        addProperty(RDF.li(i+1), n);
        return this;
    } 
    
    public Container add(boolean o) throws RDFException {
        return add( String.valueOf( o ) );
    }
    
    public Container add(long o) throws RDFException {
        return add( String.valueOf( o ) );
    }
    
    public Container add(char o) throws RDFException {
        return add( String.valueOf( o ) );
    }
    
    public Container add(float o) throws RDFException {
        return add( String.valueOf( o ) );
    }
    
    public Container add(double o) throws RDFException {
        return add( String.valueOf( o ) );
    }

    public Container add(Object o) throws RDFException {
        return add( String.valueOf( o ) );
    }
     
    public Container add(String o) throws RDFException {
        return add( o, "" );
    }
    
    public Container add(String o, String l) throws RDFException {
        return add( literal( o, l ) );
    }
    
    public boolean contains(RDFNode n) throws RDFException {
        return ((ModelI)getModel()).containerContains(this, n);
    }

    public boolean contains(boolean o) throws RDFException {
        return contains( String.valueOf( o ) );
    }

    public boolean contains(long o) throws RDFException {
        return contains( String.valueOf( o ) );
    }

    public boolean contains(char o) throws RDFException {
        return contains( String.valueOf( o ) );
    }

    public boolean contains(float o) throws RDFException {
        return contains( String.valueOf( o ) );
    }

    public boolean contains(double o) throws RDFException {
        return contains( String.valueOf( o ) );
    }

    public boolean contains(Object o) throws RDFException {
        return contains( String.valueOf( o ) );
    }
    
    public boolean contains(String o) throws RDFException {
        return contains( o, "" );
    }
    
    public boolean contains( String o, String l ) throws RDFException {
        return contains( literal( o, l ) );
    }

    private Literal literal( String s, String lang )
        { return new LiteralImpl( Node.createLiteral( s, lang, false ), (Model) getModel() ); }
            
    public NodeIterator iterator() throws RDFException {
        return ((ModelI)getModel()).listContainerMembers(this, iteratorFactory);
    }
        
    public int size() throws RDFException {
        return ((ModelI)getModel()).containerSize(this);
    }
    
    public Container remove(Statement s) throws RDFException {
        int size = size();
        Statement last = null;
        if (s.getPredicate().getOrdinal() == size) {       // if last
            getModel().remove(s);
        } else {
            last = getModel().getProperty(this, RDF.li(size));
            s.changeObject(last.getObject());
            getModel().remove(last);
        }
        if (size() != (size -1)) 
            throw new RDFException(RDFException.ASSERTIONFAILURE); 
        return this;
    }
    
    public Container remove(int index, RDFNode object) throws RDFException {
        remove(getModel().createStatement(this, RDF.li(index), object));
        return this;
    }
}