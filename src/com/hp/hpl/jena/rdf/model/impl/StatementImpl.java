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
 * StatementImpl.java
 *
 * Created on 03 August 2000, 13:58
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.enhanced.*;

import com.hp.hpl.jena.graph.*;

/** An implementation of Statement.
 *
 * @author  bwm
 * @version  $Name: not supported by cvs2svn $ $Revision: 1.11 $ $Date: 2003-05-19 16:06:52 $
 */
public class StatementImpl  implements Statement {
    
    protected Resource subject;
    protected Property predicate;
    protected RDFNode  object;
    final private Model model;
    
    public StatementImpl(Resource subject, Property predicate, RDFNode object) {
        this.model = null;
       
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }
    
    /** Creates new StatementImpl */
    public StatementImpl(Resource subject,
                         Property predicate,
                         RDFNode object,
                         Model model) throws RDFException {
        this.model = model;
        this.subject = (Resource) subject.inModel( model ); 
        this.predicate = (Property) predicate.inModel( model ); 
        this.object = object.inModel( model ); 
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
    
    public Statement getStatementProperty(Property p) throws RDFException {
        return asResource().getProperty(p);
    }
    
    /**
        utility: check that node is a Resource, throw otherwise
    */
    private Resource mustBeResource( RDFNode n )
        {
        if (n instanceof Resource)
            return (Resource) n;
        else
            throw new RDFException(RDFException.OBJECTNOTRESOURCE);
         }
        
    public Resource getResource()
        { return mustBeResource( object ); }
    
    public Resource getResource( ResourceF f )
        { return f.createResource( getResource() ); }
    
    public Statement getProperty(Property p) throws RDFException {
        return getResource().getProperty( p );
    }    
        
    /**
        get the object field of this statement, insisting that it be a Literal.
        If it isn't, throw RDFException.OBJECTNOTLITERAL.
    */
    public Literal getLiteral() throws RDFException {        
        if (object instanceof Literal) {
            return (Literal) object;
        } else {    
            throw new RDFException(RDFException.OBJECTNOTLITERAL);
        }
    }
    
    public boolean getBoolean() throws RDFException {
        return getLiteral().getBoolean();
    }
    
    public byte getByte() throws RDFException {
        return getLiteral().getByte();
    }
    
    public short getShort() throws RDFException {
        return getLiteral().getShort();
    }
    
    public int getInt() throws RDFException {
        return getLiteral().getInt();
    }
    
    public long getLong() throws RDFException {
        return getLiteral().getLong();
    }
    
    public char getChar() throws RDFException {
        return getLiteral().getChar();
    }
    
    public float getFloat() throws RDFException {
        return getLiteral().getFloat();
    }
    
    public double getDouble() throws RDFException {
        return getLiteral().getDouble();
    }
    
    public String getString() throws RDFException {
        return getLiteral().toString();
    }
    
    public Object getObject(ObjectF f) throws RDFException {
        try {
            return f.createObject(((Literal) object).toString());
        } catch (Exception e) {
            throw new RDFException(e);
        }
    }
    
   /** I suspect that this is now not pulling its weight. */
   private EnhNode get( Class interf ) {
        return (EnhNode) object.as( interf );
    }
        
    public Bag getBag() throws RDFException {
        return (Bag) get(Bag.class);
    }
    
    public Alt getAlt() throws RDFException {
        return (Alt)get(Alt.class);
    }
    
    public Seq getSeq() throws RDFException {
        return (Seq)get(Seq.class);
    }    
    
    public String getLanguage() throws RDFException {
        return getLiteral().getLanguage();
    }      
    
    public boolean getWellFormed() throws RDFException {
        return getLiteral().getWellFormed();
    }      
    
    public Statement changeObject(boolean o) throws RDFException {
        return stringReplace( String.valueOf( o ) ); 
    }
    
    public Statement changeObject(long o) throws RDFException {
        return stringReplace( String.valueOf( o ) );
    }
    
    public Statement changeObject(char o) throws RDFException {
        return stringReplace( String.valueOf( o ) );
    }
    
    public Statement changeObject(float o) throws RDFException {
        return stringReplace( String.valueOf( o ) );
    }
    
    public Statement changeObject(double o) throws RDFException {
        return stringReplace( String.valueOf( o ) );
    }
    
    public Statement changeObject(String o) throws RDFException {
        return stringReplace( String.valueOf( o ) );
    }  
    
    public Statement changeObject(String o, boolean wellFormed) throws RDFException {
        return stringReplace( String.valueOf( o ), "", wellFormed );
    }  
    
    public Statement changeObject(String o, String l) throws RDFException {
        return stringReplace( String.valueOf( o ), l, false );
    }    
    
    public Statement changeObject(String o, String l, boolean wellFormed) throws RDFException {
        return stringReplace( String.valueOf( o ), l, wellFormed );
    }    
    
    public Statement changeObject(RDFNode o) throws RDFException {
        return replace(o);
    }    
    
    public Statement changeObject(Object o) throws RDFException {
        return o instanceof RDFNode
            ? replace( (RDFNode) o )
            : stringReplace( o.toString() )
            ;
    }
    
    /** get the Model for this StatementImpl, and die if it doesn't have one */
	protected Model mustHaveModel()
		{
      	if (model == null) throw new RDFException( RDFException.NOTRELATEDTOMODEL );
		return model; 
		}
        
    /**
        "replace" the Object of this statement with the literal string value _s_. NOTE: this is
        a convenience function to eliminate the use of a deprecated constructor; when 
        data-types are put properly into Jena, it will likely disappear. 
    */
    protected StatementImpl stringReplace( String s )
        { return stringReplace( s, "", false ); };

    protected StatementImpl stringReplace( String s, String lang, boolean wellFormed )
        { return replace( new LiteralImpl( Node.createLiteral( s, lang, wellFormed ), model ) ); };
            
    /** it turns out to be handy to return this StatementImpl as the result */ 
    protected StatementImpl replace(RDFNode n)  {
    	mustHaveModel().remove( this ).add( subject, predicate, n );
    	return new StatementImpl( subject, predicate, n, model );
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
        return object instanceof Resource
            ? "Resource<" + ((Resource)object).toString() + ">"
            : "Literal<" + ((Literal)object).toString() + ">"
            ;
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
    
    public Statement remove() throws RDFException {
    	mustHaveModel().remove( this );
    	return this;
    }
            
    public Model getModel() {
    	return model;
    }
    
    public Resource asResource() {
    	return mustHaveModel().getAnyReifiedStatement(this);
    }    
    
    public void removeReification() {
    	mustHaveModel().removeAllReifications(this);
    }
    
    public Triple asTriple() {
    	return new Triple( subject.asNode(), predicate.asNode(), object.asNode() );
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
    
    public boolean isReified() throws RDFException {
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
        { return ReifiedStatementImpl.create( this.getModel(), uri, this ); }
        
    public RSIterator listReifiedStatements()
        { return mustHaveModel().listReifiedStatements( this ); }

    /**
        create a Statement from the triple _t_ in the enhanced graph _eg_.
        The Statement has subject, predicate, and object corresponding to
        those of _t_.
    */
    public static Statement toStatement( Triple t, EnhGraph eg )
        {
        Resource s = new ResourceImpl( t.getSubject(), eg );
        Property p = new PropertyImpl( t.getPredicate(), eg );
        RDFNode o = createObject( t.getObject(), eg );
        return new StatementImpl( s, p, o, (Model) eg );
        }

    /**
        create an RDF node which might be a literal, or not.
    */
    public static RDFNode createObject( Node n, EnhGraph g )
        {
        return n.isLiteral() ? (RDFNode) new LiteralImpl( n, g ) : new ResourceImpl( n, g );
        }
    
}