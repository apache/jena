/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
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
 * ResourceImpl.java
 *
 * Created on 03 August 2000, 13:45
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.enhanced.*;

import com.hp.hpl.jena.graph.*;

/** An implementation of Resource.
 *
 * @author  bwm
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.9 $' Date='$Date: 2003-04-17 20:14:00 $'
 */

public class ResourceImpl extends EnhNode implements Resource {
    
    final static public Implementation factory = new Implementation() {
        public EnhNode wrap(Node n,EnhGraph eg) {
            return new ResourceImpl(n,eg);
        }
    };
    final static public Implementation rdfNodeFactory = new Implementation() {
	public EnhNode wrap(Node n,EnhGraph eg) {
		if ( n.isURI() || n.isBlank() )
		  return new ResourceImpl(n,eg);
		if ( n.isLiteral() )
		  return new LiteralImpl(n,eg);
		return null;
	}
};
    private int splitHere = 0;

    private ResourceImpl( Resource r )
        { this( r.asNode(), r.getModel() ); }
        
    /**
        the master constructor: make a new Resource in the given model,
        rooted in the given node, with a namespace splitpoint at _split_.
    */
    private ResourceImpl( Node n, int split, Model m )
        {
        super( n, (ModelCom)m );
        this.splitHere = split;
        }

    /** Creates new ResourceImpl */

    public ResourceImpl() {
        this( (ModelCom) null );
    }

    public ResourceImpl( Model m ) {
        this( fresh( null ), 0, m );
    }

    /**
        NOT FOR PUBLIC USE - used in PersonalityCore.
    */
     public ResourceImpl( Node n, Model m ) {
        this( n, (n.isURI() ? whereToSplit( n.toString() ) : 0), m );
    }
    public ResourceImpl( Node n, EnhGraph m ) {
        this( n, (Model)m );
    }

    public ResourceImpl( String uri ) {
        this( fresh( uri ), whereToSplit( uri ), null );
    }

    public ResourceImpl(String nameSpace, String localName) {
        this( Node.createURI( nameSpace + localName ), nameSpace.length(), null );
    }

    public ResourceImpl(AnonId id) {
        this( id, (ModelCom) null );
    }

    public ResourceImpl(AnonId id, Model m) {
        this( Node.createAnon( id ), 0, m );
    }

    public ResourceImpl(String uri, Model m) {
       this( fresh( uri ), whereToSplit( uri ), m );
    }
    
    public ResourceImpl( Resource r, Model m ) {
        this( r.getNode(), m );
    }
    
    public ResourceImpl(String nameSpace, String localName, Model m) {
        this( Node.createURI( nameSpace + localName ), nameSpace.length(), m );
    }

    public RDFNode inModel( Model m )
        { 
        return 
            getModel() == m ? this 
            : isAnon() ? m.createResource( getId() ) 
            : m.createResource( getURI() ); 
        }
    
    private static int whereToSplit( String s )
        {
        if (s == null) return 0;
        int where = Util.splitNamespace( s );
        return where == 0 ? s.length() : where;
        }

    private static Node fresh( String uri )
        { return uri == null ? Node.createAnon( new AnonId() ) : Node.createURI( uri ); }

    public Node getNode() 
        { return asNode(); }

    public AnonId getId() 
        { return asNode().getBlankNodeId(); }

    public String  getURI() {
        return isAnon() ? null : asNode().toString();
    }

    public String getNameSpace() {
        return isAnon() ? null : getURI().substring( 0, splitHere );
    }

    public String getLocalName() {
        return isAnon() ? null : getURI().substring( splitHere );
    }

    public String  toString() {
    	return asNode().toString();
    }

    public boolean isAnon() {
        return asNode().isBlank();
    }

	protected ModelCom mustHaveModel()
		{
        ModelCom model = (ModelCom)getGraph();
		if (model == null) throw new RDFException( RDFException.NOTRELATEDTOMODEL );
		return model;
		}
		
    public Statement getProperty(Property p) throws RDFException
    	{ return mustHaveModel().getProperty( this, p ); }

    public StmtIterator listProperties(Property p) throws RDFException
		{ return mustHaveModel().listStatements( this, p, (RDFNode) null ); }

    public StmtIterator listProperties() throws RDFException
    	{ return mustHaveModel().listStatements( this, null, (RDFNode) null ); }	

    public Resource addProperty(Property p, boolean o) throws RDFException
    	{
    	mustHaveModel().add( this, p, o );
    	return this;
     	}

    public Resource addProperty(Property p, long o) throws RDFException {
        mustHaveModel().add( this, p, o );
        return this;
    }
    public Resource addProperty(Property p, char o) throws RDFException {
        mustHaveModel().add( this, p, o );
        return this;
    }

    public Resource addProperty(Property p, float o) throws RDFException {
        mustHaveModel().add( this, p, o );
        return this;
    }

    public Resource addProperty(Property p, double o) throws RDFException {
        mustHaveModel().add( this, p, o );
        return this;
    }

    public Resource addProperty(Property p, String o) throws RDFException {
        mustHaveModel().add( this, p, o );
        return this;
    }

    public Resource addProperty(Property p, String o, String l)
      throws RDFException {
        mustHaveModel().add( this, p, o, l );
        return this;
    }

    public Resource addProperty(Property p, Object o) throws RDFException {
        mustHaveModel().add( this, p, o );
        return this;
    }

    public Resource addProperty(Property p, RDFNode o) throws RDFException {
        mustHaveModel().add( this, p, o );
        return this;
    }

    public boolean hasProperty(Property p) throws RDFException {
        return mustHaveModel().contains( this, p );
    }
    public boolean hasProperty(Property p, boolean o) throws RDFException {
        return mustHaveModel().contains( this, p, o );
    }

    public boolean hasProperty(Property p, long o) throws RDFException {
        return mustHaveModel().contains( this, p, o );
    }

    public boolean hasProperty(Property p, char o) throws RDFException {
        return mustHaveModel().contains( this, p, o );
    }

    public boolean hasProperty(Property p, float o) throws RDFException {
        return mustHaveModel().contains( this, p, o );
    }

    public boolean hasProperty(Property p, double o) throws RDFException {
        return mustHaveModel().contains( this, p, o );
    }

    public boolean hasProperty(Property p, String o) throws RDFException {
        return mustHaveModel().contains( this, p, o );
    }

    public boolean hasProperty(Property p, String o, String l)
      throws RDFException {
        return mustHaveModel().contains( this, p, o, l );
    }

    public boolean hasProperty(Property p, Object o) throws RDFException {
        return mustHaveModel().contains( this, p, o );
    }

    public boolean hasProperty(Property p, RDFNode o) throws RDFException {
        return mustHaveModel().contains( this, p, o );
    }

    public Resource removeProperties() throws RDFException {
        StmtIterator it  = mustHaveModel().listStatements( this, null, (RDFNode) null );
        while (it.hasNext()) { it.nextStatement(); it.remove(); }
        return this;
    }

    public Resource begin() throws RDFException {
        mustHaveModel().begin();
        return this;
    }

    public Resource abort() throws RDFException {
        mustHaveModel().abort();
        return this;
    }

    public Resource commit() throws RDFException {
        mustHaveModel().commit();
        return this;
    }

    
    public Resource port(Model m) throws RDFException {
        if ( getGraph() == m )
            return this;
        if ( m instanceof ModelCom )
            return IteratorFactory.asResource( asNode(), (ModelCom)m);
        if ( isAnon() )
            return m.createResource();
        return m.createResource(getURI());
    }

    public Model getModel() {
        return (ModelCom)getGraph();
    }
}
