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
 * @version  $Name: not supported by cvs2svn $ $Revision: 1.6 $ $Date: 2003-04-02 13:26:32 $
 */
public class StatementImpl //extends ResourceImpl 
          implements Statement {
    
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
        this.subject=((ModelI)model).convert(subject);
        this.predicate = ((ModelI)model).convert(predicate);
        this.object = ((ModelI)model).convert(object);
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
    
    public Resource getResource() throws RDFException {
        if (object instanceof Resource) {
            return (Resource) object;
        } else {
            throw new RDFException(RDFException.OBJECTNOTRESOURCE);
        }
    }

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
    
    public Resource getResource(ResourceF f) throws RDFException {
        if (object instanceof Resource) {
            try {
                return
                    f.createResource(((ResourceI)object).getEmbeddedResource());
            } catch (Exception e) {
                throw new RDFException(e);
            }
        } else {
            throw new RDFException(RDFException.OBJECTNOTRESOURCE);
        }
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
    
    public Statement set(boolean o) throws RDFException {
        return stringReplace( String.valueOf( o ) ); 
    }
    
    public Statement set(long o) throws RDFException {
        return stringReplace( String.valueOf( o ) );
    }
    
    public Statement set(char o) throws RDFException {
        return stringReplace( String.valueOf( o ) );
    }
    
    public Statement set(float o) throws RDFException {
        return stringReplace( String.valueOf( o ) );
    }
    
    public Statement set(double o) throws RDFException {
        return stringReplace( String.valueOf( o ) );
    }
    
    public Statement set(String o) throws RDFException {
        return stringReplace( String.valueOf( o ) );
    }  
    
    public Statement set(String o, boolean wellFormed) throws RDFException {
        return stringReplace( String.valueOf( o ), "", wellFormed );
    }  
    
    public Statement set(String o, String l) throws RDFException {
        return stringReplace( String.valueOf( o ), l, false );
    }    
    
    public Statement set(String o, String l, boolean wellFormed) throws RDFException {
        return stringReplace( String.valueOf( o ), l, wellFormed );
    }    
    
    public Statement set(RDFNode o) throws RDFException {
        return replace(o);
    }    
    
    public Statement set(Object o) throws RDFException {
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
    protected StatementImpl replace(RDFNode n) throws RDFException {
    	mustHaveModel().remove( this ).add( subject, predicate, n );
    	object = n;
        return this;
    }
        
    public String toString() {
        String result = "[" + subject.toString() + ", "
                            + predicate.toString() + ", ";
        if (object instanceof Resource) {
            result = result + "Resource<" + ((Resource)object).toString() + ">";
        } else {
            result = result + "Literal<" + ((Literal)object).toString() + ">";
        }
        return result + "]";
    }
    
    public boolean equals(Object o) {
        return o != null && o instanceof Statement &&
              (subject.equals(((Statement) o).getSubject()))
            && (predicate.equals(((Statement) o).getPredicate()))
            && (object.equals(((Statement) o).getObject()));
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