/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ResourceImpl.java,v 1.4 2010-01-19 10:06:17 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.enhanced.*;

import com.hp.hpl.jena.graph.*;

/** An implementation of Resource.
 *
 * @author  bwm
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.4 $' Date='$Date: 2010-01-19 10:06:17 $'
 */

public class ResourceImpl extends EnhNode implements Resource {
    
    final static public Implementation factory = new Implementation() {
        @Override
        public boolean canWrap( Node n, EnhGraph eg )
            { return !n.isLiteral(); }
        @Override
        public EnhNode wrap(Node n,EnhGraph eg) {
            if (n.isLiteral()) throw new ResourceRequiredException( n );
            return new ResourceImpl(n,eg);
        }
    };
    final static public Implementation rdfNodeFactory = new Implementation() {
        @Override
        public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
        @Override
        public EnhNode wrap(Node n,EnhGraph eg) {
		if ( n.isURI() || n.isBlank() )
		  return new ResourceImpl(n,eg);
		if ( n.isLiteral() )
		  return new LiteralImpl(n,eg);
		return null;
	}
};
        
    /**
        the master constructor: make a new Resource in the given model,
        rooted in the given node.
    
        NOT FOR PUBLIC USE - used in ModelCom [and ContainerImpl]
    */
     public ResourceImpl( Node n, ModelCom m ) {
        super( n, m );
    }

    /** Creates new ResourceImpl */

    public ResourceImpl() {
        this( (ModelCom) null );
    }

    public ResourceImpl( ModelCom m ) {
        this( fresh( null ), m );
    }

     
    public ResourceImpl( Node n, EnhGraph m ) {
        super( n, m );
    }

    public ResourceImpl( String uri ) {
        super( fresh( uri ), null );
    }

    public ResourceImpl(String nameSpace, String localName) {
        super( Node.createURI( nameSpace + localName ), null );
    }

    public ResourceImpl(AnonId id) {
        this( id, null );
    }

    public ResourceImpl(AnonId id, ModelCom m) {
        this( Node.createAnon( id ), m );
    }

    public ResourceImpl(String uri, ModelCom m) {
        this( fresh( uri ), m );
    }
    
    public ResourceImpl( Resource r, ModelCom m ) {
        this( r.asNode(), m );
    }
    
    public ResourceImpl(String nameSpace, String localName, ModelCom m) {
        this( Node.createURI( nameSpace + localName ), m );
    }

    @Override
    public Object visitWith( RDFVisitor rv )
        { return isAnon() ? rv.visitBlank( this, getId() ) : rv.visitURI( this, getURI() ); }
        
    @Override
    public Resource asResource()
        { return this; }
    
    @Override
    public Literal asLiteral()
        { throw new LiteralRequiredException( asNode() ); }
    
    @Override
    public Resource inModel( Model m )
        { 
        return 
            getModel() == m ? this 
            : isAnon() ? m.createResource( getId() ) 
            : asNode().isConcrete() == false ? (Resource) ((ModelCom) m).getRDFNode( asNode() )
            : m.createResource( getURI() ); 
        }
    
    private static Node fresh( String uri )
        { return uri == null ? Node.createAnon() : Node.createURI( uri ); }

    @Override
    public AnonId getId() 
        { return asNode().getBlankNodeId(); }

    @Override
    public String  getURI() {
        return isAnon() ? null : node.getURI();
    }

    @Override
    public String getNameSpace() {
        return isAnon() ? null : node.getNameSpace();
    }
    
	@Override
    public String getLocalName() {
        return isAnon() ? null : node.getLocalName(); 
    }

    @Override
    public boolean hasURI( String uri ) 
        { return node.hasURI( uri ); }
    
    @Override
    public String toString() 
        { return asNode().toString(); }
    
	protected ModelCom mustHaveModel()
		{
        ModelCom model = getModelCom();
		if (model == null) throw new HasNoModelException( this );
		return model;
		}
		    
    @Override
    public Statement getRequiredProperty(Property p) 
    	{ return mustHaveModel().getRequiredProperty( this, p ); }
        
    @Override
    public Statement getProperty( Property p )
        { return mustHaveModel().getProperty( this, p ); }

    @Override
    public StmtIterator listProperties(Property p) 
		{ return mustHaveModel().listStatements( this, p, (RDFNode) null ); }

    @Override
    public StmtIterator listProperties() 
    	{ return mustHaveModel().listStatements( this, null, (RDFNode) null ); }	
    
    @Override
    public Resource addLiteral( Property p, boolean o ) 
        {
        ModelCom m = mustHaveModel();
        m.add( this, p, m.createTypedLiteral( o ) );
        return this;
        }

    public Resource addProperty(Property p, long o)  {
        mustHaveModel().addLiteral( this, p, o );
        return this;
    }
    
    @Override
    public Resource addLiteral( Property p, long o ) 
        {
        Model m = mustHaveModel();
        m.add( this, p, m.createTypedLiteral( o ) );
        return this;
        }
    
    @Override
    public Resource addLiteral( Property p, char o )  
        {
        ModelCom m = mustHaveModel();
        m.add( this, p, m.createTypedLiteral( o ) );
        return this;
        }

    public Resource addProperty(Property p, float o) {
        mustHaveModel().addLiteral( this, p, o );
        return this;
    }

    public Resource addProperty(Property p, double o) {
        mustHaveModel().addLiteral( this, p, o );
        return this;
    }
    
    @Override
    public Resource addLiteral( Property p, double o ) 
        {
        Model m = mustHaveModel();
        m.add( this, p, m.createTypedLiteral( o ) );
        return this;
        }
    
    @Override
    public Resource addLiteral( Property p, float o ) 
        {
        Model m = mustHaveModel();
        m.add( this, p, m.createTypedLiteral( o ) );
        return this;
        }

    @Override
    public Resource addProperty(Property p, String o) {
        mustHaveModel().add( this, p, o );
        return this;
    }

    @Override
    public Resource addProperty(Property p, String o, String l)
    {
        mustHaveModel().add( this, p, o, l );
        return this;
    }

    @Override
    public Resource addProperty(Property p, String lexicalForm, RDFDatatype datatype)
    {
        mustHaveModel().add(this, p, lexicalForm, datatype) ;
        return this ;
    }

    @Override
    public Resource addLiteral( Property p, Object o ) 
        {
        ModelCom m = mustHaveModel();
        m.add( this, p, m.createTypedLiteral( o ) );
        return this;
        }
    
    @Override
    public Resource addLiteral( Property p, Literal o )
        {
        mustHaveModel().add( this, p, o );
        return this;
        }

    @Override
    public Resource addProperty( Property p, RDFNode o ) 
        {
        mustHaveModel().add( this, p, o );
        return this;
        }

    @Override
    public boolean hasProperty(Property p)  {
        return mustHaveModel().contains( this, p );
    }
    
    @Override
    public boolean hasLiteral( Property p, boolean o )  
        {
        ModelCom m = mustHaveModel();
        return m.contains( this, p, m.createTypedLiteral( o ) );
        }
    
    @Override
    public boolean hasLiteral( Property p, long o ) 
        {
        ModelCom m = mustHaveModel();
        return m.contains( this, p, m.createTypedLiteral( o ) );
        }
    
    @Override
    public boolean hasLiteral( Property p, char o )  
        {
        ModelCom m = mustHaveModel();
        return m.contains( this, p, m.createTypedLiteral( o ) );
        }
    
    @Override
    public boolean hasLiteral( Property p, double o ) 
        {
        ModelCom m = mustHaveModel();
        return m.contains( this, p, m.createTypedLiteral( o ) );
        }
    
    @Override
    public boolean hasLiteral( Property p, float o ) 
        {
        ModelCom m = mustHaveModel();
        return m.contains( this, p, m.createTypedLiteral( o ) );
        }
    
    @Override
    public boolean hasProperty(Property p, String o) {
        return mustHaveModel().contains( this, p, o );
    }

    @Override
    public boolean hasProperty(Property p, String o, String l) {
        return mustHaveModel().contains( this, p, o, l );
    }

    @Override
    public boolean hasLiteral( Property p, Object o ) 
        {
        ModelCom m = mustHaveModel();
        return m.contains( this, p, m.createTypedLiteral( o ) );
        }

    @Override
    public boolean hasProperty(Property p, RDFNode o)  {
        return mustHaveModel().contains( this, p, o );
    }

    @Override
    public Resource removeProperties()  {
        removeAll(null);
        return this;
    }
    
    @Override
    public Resource removeAll( Property p ) {
        mustHaveModel().removeAll( this, p, (RDFNode) null );
        return this;
    }
    
    @Override
    public Resource begin()  {
        mustHaveModel().begin();
        return this;
    }

    @Override
    public Resource abort()  {
        mustHaveModel().abort();
        return this;
    }

    @Override
    public Resource commit()  {
        mustHaveModel().commit();
        return this;
    }

    @Override
    public Model getModel() {
        return (Model) getGraph();
    }
    
    protected ModelCom getModelCom()
        { return (ModelCom) getGraph(); }

    @Override
    public Resource getPropertyResourceValue( Property p )
        {
        for (StmtIterator it = listProperties( p ); it.hasNext();)
            {
            RDFNode n = it.next().getObject();
            if (n.isResource()) return (Resource) n;
            }
        return null;
        }
}

/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
