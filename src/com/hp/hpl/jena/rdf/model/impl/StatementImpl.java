
package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.graph.*;

/** An implementation of Statement.
 *
 * @author  bwm
 * @version  $Name: not supported by cvs2svn $ $Revision: 1.25 $ $Date: 2004-08-03 19:00:48 $
 */
public class StatementImpl  extends StatementBase implements Statement {
    
    protected Resource subject;
    protected Property predicate;
    protected RDFNode  object;
    
    public StatementImpl(Resource subject, Property predicate, RDFNode object) {
        super( null );
       
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }
    
    /** Creates new StatementImpl */
    public StatementImpl(Resource subject,
                         Property predicate,
                         RDFNode object,
                         ModelCom model)  {
        super( model );
        this.subject = (Resource) subject.inModel( model ); 
        this.predicate = (Property) predicate.inModel( model ); 
        this.object = object.inModel( model ); 
    }    
    
    /**
        create a Statement from the triple _t_ in the enhanced graph _eg_.
        The Statement has subject, predicate, and object corresponding to
        those of _t_.
    */
    public static Statement toStatement( Triple t, ModelCom eg )
        {
        Resource s = new ResourceImpl( t.getSubject(), eg );
        Property p = new PropertyImpl( t.getPredicate(), eg );
        RDFNode o = createObject( t.getObject(), eg );
        return new StatementImpl( s, p, o, eg );
        }
    
    public Resource getSubject() {
        return subject;
    }
    
    public Property getPredicate() {
        return predicate;
    }
    
    public RDFNode getObject() {
        return object;
    }    
    
    public Statement getStatementProperty(Property p)  {
        return asResource().getRequiredProperty(p);
    }
    
    /**
        utility: check that node is a Resource, throw otherwise
    */
    private Resource mustBeResource( RDFNode n )
        {
        if (n instanceof Resource)
            return (Resource) n;
        else
            throw new ResourceRequiredException( n );
         }
        
    public Resource getResource()
        { return mustBeResource( object ); }
    
    public Resource getResource( ResourceF f )
        { return f.createResource( getResource() ); }
    
    public Statement getProperty(Property p)  {
        return getResource().getRequiredProperty( p );
    }    
        
    /**
        get the object field of this statement, insisting that it be a Literal.
        If it isn't, throw LiteralRequiredException.
    */
    public Literal getLiteral()  {        
        if (object instanceof Literal) {
            return (Literal) object;
        } else {    
            throw new LiteralRequiredException( object );
        }
    }
    

    public Object getObject(ObjectF f)  {
        try {
            return f.createObject(((Literal) object).toString());
        } catch (Exception e) {
            throw new JenaException(e);
        }
    }
    
   /** I suspect that this is now not pulling its weight. */
   private EnhNode get( Class interf ) {
        return (EnhNode) object.as( interf );
    }
        
    public Bag getBag()  {
        return (Bag) get(Bag.class);
    }
    
    public Alt getAlt()  {
        return (Alt)get(Alt.class);
    }
    
    public Seq getSeq()  {
        return (Seq)get(Seq.class);
    }    
    
    public String getLanguage()  {
        return getLiteral().getLanguage();
    }      
    
    public boolean getWellFormed()  {
        return getLiteral().getWellFormed();
    }      
    
    /** it turns out to be handy to return the new StatementImpl as the result */ 
    protected StatementImpl replace(RDFNode n)  {
    	StatementImpl s = new StatementImpl( subject, predicate, n, model );
    	mustHaveModel().remove( this ).add( s );
        return s;
    }
        
    public String toString() 
        {
        return
            "[" 
            + subject.toString()
            + ", " + predicate.toString() 
            + ", " + objectString()
            + "]";
        }
        
    private String objectString()
        {
        return object.asNode().toString( null, true );
//        return object instanceof Resource
//            ? "Resource<" + ((Resource)object).toString() + ">"
//            : "Literal<" + ((Literal)object).toString() + ">"
//            ;
        }
    
    /**
        .equals() defers to .sameAs so we only get the complexity of one cast.
    */
    public boolean equals(Object o)
        { return o instanceof Statement && sameAs( (Statement) o ); }
        
    /**
        sameAs - is this statement equal to the statement o? We can't assume
        o is a StatementImpl
    */
    private final boolean sameAs( Statement o )
        { 
        return subject.equals( o.getSubject() ) 
            && predicate.equals( o.getPredicate() )
            && object.equals( o.getObject() );
        }
    
    public int hashCode() {
    	return asTriple().hashCode();
    }
    
    public Resource asResource() {
    	return mustHaveModel().getAnyReifiedStatement(this);
    }    

    public Statement remove()
        {
        mustHaveModel().remove( this );
        return this;
        }
    
    public void removeReification() {
    	mustHaveModel().removeAllReifications(this);
    }
    
    public Triple asTriple() {
    	return Triple.create( subject.asNode(), predicate.asNode(), object.asNode() );
    }
    
    /**
        returns an array of triples corresponding to the array of statements; ie
        the i'th element of the result is the i'th element of the input as a triple.
        @param statements the array of statements to convert
        @return the corresponding array of triples
    */
    public static Triple [] asTriples( Statement [] statements )
        {        
        Triple [] triples = new Triple[statements.length];
        for (int i = 0; i < statements.length; i += 1) triples[i] = statements[i].asTriple();
        return triples;
        }
    
    public boolean isReified()  {
        return mustHaveModel().isReified( this );
    }
        
    /**
        create a ReifiedStatement corresponding to this Statement
    */
    public ReifiedStatement createReifiedStatement()
        { return ReifiedStatementImpl.create( this ); }
        
    /**
        create a ReifiedStatement corresponding to this Statement
        and with the given _uri_.
    */
    public ReifiedStatement createReifiedStatement( String uri )
        { return ReifiedStatementImpl.create( (ModelCom) this.getModel(), uri, this ); }
        
    public RSIterator listReifiedStatements()
        { return mustHaveModel().listReifiedStatements( this ); }

    /**
        create an RDF node which might be a literal, or not.
    */
    public static RDFNode createObject( Node n, EnhGraph g )
        {
        return n.isLiteral() ? (RDFNode) new LiteralImpl( n, g ) : new ResourceImpl( n, g );
        }
    
}

/*
 *  (c) Copyright 2000, 2001, 2004 Hewlett-Packard Development Company, LP
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
 * StatementImpl.java
 *
 * Created on 03 August 2000, 13:58
 */
