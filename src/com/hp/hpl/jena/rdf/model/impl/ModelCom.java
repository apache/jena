/*
 *  (c) Copyright Hewlett-Packard Company 2001
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
 * Model.java
 *
 * Created on 11 March 2001, 16:07
 */
 
package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.mem.*;

import java.io.*;
import java.util.*;

/** Common methods for model implementations.
 *
 * <P>This class implements common methods, mainly convenience methods, for
 *    model implementations.  It is intended use is as a base class from which
 *    model implemenations can be derived.</P>
 *
 * @author bwm
 * hacked by Jeremy, tweaked by Chris (May 2002 - October 2002)
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.11 $' Date='$Date: 2003-03-31 10:04:54 $'
 */

abstract public class ModelCom extends EnhGraph
    implements Model, ModelI {

      private RDFReaderF readerFactory = new RDFReaderFImpl();
      private RDFWriterF writerFactory = new RDFWriterFImpl();
    // next free error code = 3
    /**
    	make a model based on the specified graph
    */
	private ModelCom( Graph base ) {
		this( base, BuiltinPersonalities.model );
	}
	
    public boolean isModel() {
    	return true;
    }
    
    public QueryHandler queryHandler()
    	{ return getGraph().queryHandler(); }
	
	public ModelCom( Graph base, Personality personality )
		{ super( base, personality ); }
		
    public Graph getGraph()
        { return graph; }
               
    /**
        the ModelReifier does everything to do with reification.
    */
    protected ModelReifier modelReifier = new ModelReifier( this ); 
	
    public Resource getResource(String uri, ResourceF f) throws RDFException {
        try {
            return f.createResource(getResource(uri));
        } catch (Exception e) {
            throw new RDFException(e);
        }
    }
    public Model add(Resource s, Property p, boolean o) throws RDFException {
        return add(s, p, String.valueOf( o ) );
    }
    
    public Model add(Resource s, Property p, long o) throws RDFException {
        return add(s, p, String.valueOf( o ) );
    }
    
    public Model add(Resource s, Property p, char o) throws RDFException {
        return add(s, p, String.valueOf( o ) );
    }
    
    public Model add(Resource s, Property p, float o) throws RDFException {
        return add(s, p, String.valueOf( o ) );
    }
    
    public Model add(Resource s, Property p, double o) throws RDFException {
        return add(s, p, String.valueOf( o ) );
    }
    
    public Model add(Resource s, Property p, String o) throws RDFException {
        return add( s, p, o, "", false );
    }
    
    public Model add(Resource s, Property p, String o, boolean wellFormed)
      throws RDFException {
        return add( s, p, o, "", wellFormed );
    }
    
    public Model add(Resource s, Property p, String o, String lang,
      boolean wellFormed) throws RDFException {
        add(s, p, literal(o, lang, wellFormed));
        return this;
    }
    
    private Literal literal( String s, String lang, boolean wellFormed )
        { return new LiteralImpl( Node.createLiteral( s, lang, wellFormed), (Model) this ); }
    
    public Model add(Resource s, Property p, String o, String l)
       throws RDFException {
        return add( s, p, o, l, false );
    }
    
    /**
        ensure that an object is an RDFNode. If it isn't, fabricate a literal
        from its string representation. NOTE: probably proper data-typing
        makes this suspect - Chris introduced it to abstract from some existing code.
    */
    private RDFNode ensureRDFNode( Object o )
        {
        return o instanceof RDFNode 
            ? (RDFNode) o 
            : literal( o.toString(), null, false )
            ;
        }
        
    public Model add(Resource s, Property p, Object o) throws RDFException {
        return add( s, p, ensureRDFNode( o ) );
    }
    
    public Model add(StmtIterator iter) throws RDFException {
        try {
            while (iter.hasNext()) {
                add(iter.nextStatement());
            }
            return this;
        } finally {
            iter.close();
        }
    }
    
    public Model add(Model m) throws RDFException {
        return add(m.listStatements());
    }
    
    public RDFReader getReader() throws RDFException {
        return readerFactory.getReader();
    }
    
    public RDFReader getReader(String lang) throws RDFException {
        return readerFactory.getReader(lang);
    }
    
    public String setReaderClassName(String lang, String className) {
        return readerFactory.setReaderClassName(lang, className);
    } 
    
    public Model read(String url) throws RDFException {
        readerFactory .getReader() .read(this, url);
        return this;
    }
    
    public Model read(Reader reader, String base) throws RDFException {
        readerFactory .getReader() .read(this, reader, base);
        return this;
    }
  	public Model read(InputStream reader, String base) throws RDFException {
  		readerFactory .getReader() .read(this, reader, base);
  		return this;
  	} 
    public Model read(String url, String lang) throws RDFException {
        readerFactory. getReader(lang) .read(this, url);
        return this;
    }
    
    public Model read(Reader reader, String base, String lang)
      throws RDFException {
        readerFactory .getReader(lang) .read(this, reader, base);
        return this;
    }
    
  	public Model read(InputStream reader, String base, String lang)
  	  throws RDFException {
  		readerFactory .getReader(lang) .read(this, reader, base);
  		return this;
  	}

    public RDFWriter getWriter() throws RDFException {
        return writerFactory.getWriter();
    }
    
    public RDFWriter getWriter(String lang) throws RDFException {
        return writerFactory.getWriter(lang);
    }
    
    public String setWriterClassName(String lang, String className) {
        return writerFactory.setWriterClassName(lang, className);
    }
    
    public Model write(Writer writer) throws RDFException {
        try {
            writerFactory.getWriter()
                         .write(this, writer, "");
            return this;
        } catch (Exception e) {
            throw new RDFException(e);
        }
    }
    
    public Model write(Writer writer, String lang) {
        try {
            writerFactory.getWriter(lang)
                         .write(this, writer, "");
            return this;
        } catch (Exception e) {
            throw new RDFException(e);
        }
    }
    
    public Model write(Writer writer, String lang, String base)
      throws RDFException {
        try {
            writerFactory.getWriter(lang)
                         .write(this, writer, base);
            return this;
        } catch (Exception e) {
            throw new RDFException(e);
        }
    }
    
  	public Model write(OutputStream writer) throws RDFException {
  		try {
  			writerFactory.getWriter()
  						 .write(this, writer, "");
  			return this;
  		} catch (Exception e) {
  			throw new RDFException(e);
  		}
  	}
    
  	public Model write(OutputStream writer, String lang) throws RDFException {
  		try {
  			writerFactory.getWriter(lang)
  						 .write(this, writer, "");
  			return this;
  		} catch (Exception e) {
  			throw new RDFException(e);
  		}
  	}
    
  	public Model write(OutputStream writer, String lang, String base)
  	  throws RDFException {
  		try {
  			writerFactory.getWriter(lang)
  						 .write(this, writer, base);
  			return this;
  		} catch (Exception e) {
  			throw new RDFException(e);
  		}
  	}
    
    public Model remove(Statement s) throws RDFException {
        graph.delete(s.asTriple());
        return this;
    }
    
    /**
        BUG. Will likely not deal properly with rel=ving things from itself,
        but no tests catch this. *iter.remove() does not work*, it will
        remove things from the wrong model. ARGH.
    */
    public Model remove(StmtIterator iter) throws RDFException {
        while (iter.hasNext()) {
            Statement s = (Statement) iter.nextStatement();
            this.remove( s ); // iter.remove();
        }
        return this;
    }
    
    public Model remove(Model m) throws RDFException
        {
        StmtIterator iter = m.listStatements();
        if (m.getGraph().mightContain( this.getGraph() ))
            {
            ArrayList X = new ArrayList();
            while (iter.hasNext()) X.add( iter.nextStatement() );
            for (int i = 0; i < X.size(); i += 1) this.remove( (Statement) X.get(i) );
            }
        else
            {
            try { remove(iter); }
            finally { iter.close(); }
            }
        return this;
        }
        
    public boolean contains(Resource s, Property p, boolean o)
      throws RDFException {
        return contains(s, p, String.valueOf( o ) );
    }
    
    public boolean contains(Resource s, Property p, long o)
      throws RDFException {
        return contains(s, p, String.valueOf( o ) );
    }
    
    public boolean contains(Resource s, Property p, char o)
      throws RDFException {
        return contains(s, p, String.valueOf( o ) );
    }
    
    public boolean contains(Resource s, Property p, float o)
      throws RDFException {
        return contains(s, p, String.valueOf( o ) );
    }
    
    public boolean contains(Resource s, Property p, double o)
      throws RDFException {
        return contains(s, p, String.valueOf( o ) );
    }
    
    public boolean contains(Resource s, Property p, String o)
      throws RDFException {
        return contains(s, p, o, "" );
    }
    
    public boolean contains(Resource s, Property p, String o, String l)
      throws RDFException {
        return contains( s, p, literal( o, l, false ) );
    }
    
    public boolean contains(Resource s, Property p, Object o)
      throws RDFException {
          return contains( s, p, ensureRDFNode( o ) );
    }
    
    public boolean containsAny(StmtIterator iter) throws RDFException {
        while (iter.hasNext()) {
            if (contains(iter.nextStatement())) return true;
        }
        return false;
    }
    
    public boolean containsAll(StmtIterator iter) throws RDFException {
        while (iter.hasNext()) {
            if (!contains(iter.nextStatement())) return false;
        }
        return true;
    }
    
    public boolean containsAny(Model model) throws RDFException {
        StmtIterator iter = model.listStatements();
        try {
            return containsAny(iter);
        } finally {
            iter.close();
        }
    }
    
    public boolean containsAll(Model model) throws RDFException {
        StmtIterator iter = model.listStatements();
        try {
            return containsAll(iter);
        } finally {
            iter.close();
        }
    }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        RDFNode  object)
      throws RDFException {
        return listStatements(
                  new SimpleSelector(subject, predicate, object));
    }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        boolean object)
      throws RDFException {
        return listStatements(
                  new SimpleSelector(subject, predicate, object));
    }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        long    object)
      throws RDFException {
        return listStatements(
                  new SimpleSelector(subject, predicate, object));
    }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        char  object)
      throws RDFException {
        return listStatements(
                  new SimpleSelector(subject, predicate, object));
    }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        float   object)
      throws RDFException {
        return listStatements(
                  new SimpleSelector(subject, predicate, object));
    }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        double  object)
      throws RDFException {
        return listStatements(
                  new SimpleSelector(subject, predicate, object));
    }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        String   object)
      throws RDFException {
        return listStatements(
                  new SimpleSelector(subject, predicate, object));
    }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        String   object,
                                        String   lang)
      throws RDFException {
        return listStatements(
                  new SimpleSelector(subject, predicate, object, lang));
    }
    
    public ResIterator listSubjectsWithProperty(Property p, boolean o)
    throws RDFException {
        return listSubjectsWithProperty(p, String.valueOf( o ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, long o)
    throws RDFException {
        return listSubjectsWithProperty(p, String.valueOf( o ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, char o)
    throws RDFException {
        return listSubjectsWithProperty(p, String.valueOf( o ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, float o)
    throws RDFException {
        return listSubjectsWithProperty(p, String.valueOf( o ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, double o)
    throws RDFException {
        return listSubjectsWithProperty(p, String.valueOf( o ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, String o)
    throws RDFException {
        return listSubjectsWithProperty( p, o, "" );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, String o, String l)
    throws RDFException {
        return listSubjectsWithProperty(p, literal( o, l, false ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, Object o)
    throws RDFException {
        return listSubjectsWithProperty( p, ensureRDFNode( o ) );
    }
    
    public Resource createResource(Resource type) throws RDFException {
        return createResource().addProperty(RDF.type, type);
    }
    
    public Resource createResource(String uri,Resource type)
    throws RDFException {
        return getResource(uri)
                   .addProperty(RDF.type, type);
    }
    
    public Resource createResource(ResourceF f) throws RDFException {
        return createResource(null, f);
    }
    
    public Resource createResource(String uri, ResourceF f) throws RDFException {
        try {
            return f.createResource(createResource(uri));
        } catch (Exception e) {
            e.printStackTrace( System.out );
           throw new RDFException(RDFException.NESTEDEXCEPTION, e);
       }
       
    }
    
 
    /** create a type literal from a boolean value.
     *
     * <p> The value is converted to a string using its <CODE>toString</CODE>
     * method. </p>
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(boolean v) throws RDFException {
        return createTypedLiteral(new Boolean(v));
    }
    
    /** create a typed literal from an integer value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */   
    public Literal createTypedLiteral(int v) throws RDFException  {
        return createTypedLiteral(new Integer(v));
    }
    
    /** create a typed literal from a long integer value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */   
    public Literal createTypedLiteral(long v) throws RDFException  {
        return createTypedLiteral(new Long(v));
    }
    
    /** create a typed literal from a char value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(char v) throws RDFException {
        return createTypedLiteral(new Character(v));
    }
    
    /** create a typed literal from a float value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(float v) throws RDFException {
        return createTypedLiteral(new Float(v));
    }
    
    /** create a typed literal from a double value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(double v) throws RDFException {
        return createTypedLiteral(new Double(v));
    }
    
    /** create a typed literal from a String value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(String v) throws RDFException {
        LiteralLabel ll = new LiteralLabel(v);
        return new LiteralImpl(Node.createLiteral(ll), (Model)this);
    }

        
    /**
     * Build a typed literal from its lexical form. The
     * lexical form will be parsed now and the value stored. If
     * the form is not legal this will throw an exception.
     * 
     * @param lex the lexical form of the literal
     * @param lang the optional language tag
     * @param dtype the type of the literal, null for old style "plain" literals
     * @throws DatatypeFormatException if lex is not a legal form of dtype
     */
    public Literal createTypedLiteral(String lex, String lang, RDFDatatype dtype) 
                                        throws DatatypeFormatException {
        LiteralLabel ll = new LiteralLabel(lex, lang, dtype);
        return new LiteralImpl(Node.createLiteral(ll), (Model)this);
    }
    
    /**
     * Build a typed literal from its value form.
     * 
     * @param value the value of the literal
     * @param lang the optional language tag
     * @param dtype the type of the literal, null for old style "plain" literals
     */
    public Literal createTypedLiteral(Object value, String lang, RDFDatatype dtype) {
        LiteralLabel ll = new LiteralLabel(value, lang, dtype);
        return new LiteralImpl(Node.createLiteral(ll), (Model)this);
    }

    /**
     * Build a typed literal from its lexical form. The
     * lexical form will be parsed now and the value stored. If
     * the form is not legal this will throw an exception.
     * 
     * @param lex the lexical form of the literal
     * @param lang the optional language tag
     * @param typeURI the uri of the type of the literal, null for old style "plain" literals
     * @throws DatatypeFormatException if lex is not a legal form of dtype
     */
    public Literal createTypedLiteral(String lex, String lang, String typeURI) throws RDFException {
        RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(typeURI);
        LiteralLabel ll = new LiteralLabel(lex, lang, dt);
        return new LiteralImpl(Node.createLiteral(ll), (Model)this);
    }
        
    /**
     * Build a typed literal from its value form.
     * 
     * @param value the value of the literal
     * @param lang the optional language tag
     * @param typeURI the URI of the type of the literal, null for old style "plain" literals
     */
    public Literal createTypedLiteral(Object value, String lang, String typeURI) {
        RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(typeURI);
        LiteralLabel ll = new LiteralLabel(value, lang, dt);
        return new LiteralImpl(Node.createLiteral(ll), (Model)this);
    }
    
    /**
     * Build a typed literal label from its value form using
     * whatever datatype is currently registered as the the default
     * representation for this java class. No language tag is supplied.
     * @param value the literal value to encapsulate
     */
    public Literal createTypedLiteral(Object value) {
        LiteralLabel ll = new LiteralLabel(value);
        return new LiteralImpl(Node.createLiteral(ll), (Model)this);
    }


    public Literal createLiteral(boolean v) throws RDFException {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(int v) throws RDFException {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(long v) throws RDFException {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(char v) throws RDFException {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(float v) throws RDFException {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(double v) throws RDFException {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(String v) throws RDFException {
        return createLiteral(v, "");
    }
    
    public Literal createLiteral(String v, String l) throws RDFException {
        return createLiteral( v,l, false );
    }
    
    public Literal createLiteral(String v, String l, boolean wellFormed) {
        return literal(v, l, wellFormed);
    }
    
    public Literal createLiteral(Object v) throws RDFException {
        return createLiteral(v.toString(), "");
    }
    
    public Statement createStatement(Resource r, Property p, boolean o)
    throws RDFException {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, long o)
    throws RDFException {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, char o)
    throws RDFException {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, float o)
    throws RDFException {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, double o)
    throws RDFException {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, String o)
    throws RDFException {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, Object o)
    throws RDFException {
        return createStatement( r, p, ensureRDFNode( o ) );
    }
    
    public Statement createStatement(Resource r, Property p, String o,
                                     boolean wellFormed) throws RDFException {
        return createStatement( r, p, o, "", wellFormed );
    }
    
    public Statement createStatement(Resource r, Property p, String o, String l)
      throws RDFException {
        return createStatement( r, p, o, l, false );
    }
    
    public Statement createStatement(Resource r, Property p, String o, String l,
                                     boolean wellFormed) throws RDFException {
        return createStatement(r, p, literal(o,l,wellFormed));
    }
    
    public Bag createBag() throws RDFException {
        return createBag(null);
    }
    
    public Alt createAlt() throws RDFException {
        return createAlt(null);
    }
    
    public Seq createSeq() throws RDFException {
        return createSeq(null);
    }
    
    public Resource getResource(String uri) throws RDFException {
        return IteratorFactory.asResource(makeURI(uri),this);
    }
    
    public Property getProperty(String uri) throws RDFException {
        if ( uri == null )
             throw new RDFException(RDFException.INVALIDPROPERTYURI);
        return IteratorFactory.asProperty(makeURI(uri),this);
    }
    
    public Property getProperty(String nameSpace,String localName)
      throws RDFException {
        return getProperty(nameSpace+localName);
    }
    
    public Seq getSeq(String uri) throws RDFException {
      return (Seq)IteratorFactory.asResource(makeURI(uri),Seq.class, this);
    }
    
    public Seq getSeq(Resource r) throws RDFException {
        return (Seq) r.as( Seq.class );
    }
    
    public Bag getBag(String uri) throws RDFException {
      return (Bag)IteratorFactory.asResource(makeURI(uri),Bag.class, this);
    }
    
    public Bag getBag(Resource r) throws RDFException {
        return (Bag) r.as( Bag.class );
    }
    
    static private Node makeURI(String uri) {
        if ( uri == null )
            return Node.createAnon(new AnonId());
        else
            return Node.createURI(uri);
    }
    
    public Alt getAlt(String uri) throws RDFException {
      return (Alt)IteratorFactory.asResource(makeURI(uri),Alt.class, this);
    }
    
    public Alt getAlt(Resource r) throws RDFException {
        return (Alt) r.as( Alt.class );
    }
    
    public long size() throws RDFException {
        return graph.size();
    }

    private void updateNamespace( HashSet set, Iterator it )
        {
        while (it.hasNext())
            {
            Node node = (Node) it.next();
            if (node.isURI())
                {
                String ns = IteratorFactory.asResource( node, this ).getNameSpace();
                if (ns == null)
                    System.err.println( "updateNamespace: null ns for " + node );
                else
                    set.add( ns );
                }
            }
        }
        
    private Iterator listPredicates()
        {
        HashSet predicates = new HashSet();
        ClosableIterator it = graph.find( null, null, null );
        while (it.hasNext()) predicates.add( ((Triple) it.next()).getPredicate() );
        return predicates.iterator();
        }
     
    private Iterator listTypes()
        {
        HashSet types = new HashSet();
        ClosableIterator it = graph.find( null, RDF.type.asNode(), null );
        while (it.hasNext()) types.add( ((Triple) it.next()).getObject() );
        return types.iterator();
        }
     
    public NsIterator listNameSpaces() throws RDFException {
	    HashSet rSet = new HashSet();
        HashSet nameSpaces = new HashSet();
        updateNamespace( nameSpaces, listPredicates() );
        updateNamespace( nameSpaces, listTypes() );
        return new NsIteratorImpl(nameSpaces.iterator(), nameSpaces);
    }
    
    public StmtIterator listStatements() throws RDFException {
        return IteratorFactory.asStmtIterator(graph.find(null,null,null), this);
    }

    /**
        add a Statement to this Model by adding its SPO components.
    */
    public Model add(Statement s) throws RDFException {
        return add( s.getSubject(), s.getPredicate(), s.getObject() );
    }
    
    public Model add(Resource s,Property p,RDFNode o) throws RDFException {
        modelReifier.noteIfReified( s, p, o );
        graph.add( new Triple( s.asNode(), p.asNode(), o.asNode() ) );
        return this;
    }
    
    /**
        @return an iterator which delivers all the ReifiedStatements in this model
    */
    public RSIterator listReifiedStatements()
        { return modelReifier.listReifiedStatements(); }

    /**
        @return an iterator each of whose elements is a ReifiedStatement in this
            model such that it's getStatement().equals( st )
    */
    public RSIterator listReifiedStatements( Statement st )
        { return modelReifier.listReifiedStatements( st ); }
                
    /**
        @return true iff this model has a reification of _s_ in some Statement
    */
    public boolean isReified( Statement s ) 
        { return modelReifier.isReified( s ); }
   
    /**
        get any reification of the given statement in this model; make
        one if necessary.
        
        @param s for which a reification is sought
        @return a ReifiedStatement that reifies _s_
    */
    public Resource getAnyReifiedStatement(Statement s) 
        { return modelReifier.getAnyReifiedStatement( s ); }
    
    /**
        remove any ReifiedStatements reifying the given statement
        @param s the statement who's reifications are to be discarded
    */
    public void removeAllReifications( Statement s ) 
        { modelReifier.removeAllReifications( s ); }
        
    public void removeReification( ReifiedStatement rs )
        { modelReifier.removeReification( rs ); }
    	
    /**
        create a ReifiedStatement that encodes _s_ and belongs to this Model.
    */
    public ReifiedStatement createReifiedStatement( Statement s )
        { return modelReifier.createReifiedStatement( s ); }
        
    public ReifiedStatement createReifiedStatement( String uri, Statement s )
        { return modelReifier.createReifiedStatement( uri, s ); }
    
    public boolean contains(Statement s) throws RDFException {
        return graph.contains( s.asTriple() );
    }
  
    public boolean contains(Resource s, Property p) throws RDFException {
        ClosableIterator it = graph.find( s.asNode(), p.asNode(), null );
        boolean rslt = it.hasNext();
        it.close();
        return rslt;
    }
    
    public boolean contains(Resource s, Property p, RDFNode o)
      throws RDFException {
        return graph.contains( s.asNode(), p.asNode(), o.asNode() );
    }
        
    public Statement getProperty(Resource s,Property p) throws RDFException {
        StmtIterator iter = null;
        try {
            iter = listStatements(new SimpleSelector(s, p, (RDFNode) null));
            if (iter.hasNext()) {
                return iter.nextStatement();
            } else {
                throw new RDFException(RDFException.PROPERTYNOTFOUND);
            }
        } finally {
            iter.close();
        }
    }
    
    public Node asNode( RDFNode x )
        { return x == null ? null : x.asNode(); }
        
    private NodeIterator listObjectsFor( RDFNode s, RDFNode p )
        {
        ClosableIterator xit = graph.queryHandler().objectsFor( asNode( s ), asNode( p ) );
        return IteratorFactory.asRDFNodeIterator( xit, this );
        }

    private ResIterator listSubjectsFor( RDFNode p, RDFNode o )
        {
        ClosableIterator xit = graph.queryHandler().subjectsFor( asNode( p ), asNode( o ) );
        return IteratorFactory.asResIterator( xit, this );
        }
                
    public ResIterator listSubjects() throws RDFException {
        return listSubjectsFor( null, null );
    }
    
    public ResIterator listSubjectsWithProperty(Property p)
    throws RDFException {
        return listSubjectsFor( p, null );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, RDFNode o)
    throws RDFException {
        return listSubjectsFor( p, o );
    }
    
    public NodeIterator listObjects() throws RDFException {
        return listObjectsFor( null, null );
    }
    
    public NodeIterator listObjectsOfProperty(Property p) throws RDFException {
        return listObjectsFor( null, p );
    }
    
    public NodeIterator listObjectsOfProperty(Resource s, Property p)
      throws RDFException {
        return listObjectsFor( s, p );
    }
            
    public StmtIterator listStatements(final Selector selector)
      throws RDFException {
        Iterator iter;
        if (selector instanceof SimpleSelector) {
            SimpleSelector s = (SimpleSelector) selector;
            iter = graph.find(s.asTripleMatch(this));
        } else {
            iter = graph.find(new StandardTripleMatch(null,null,null){
            	public boolean triple(Triple t) {
            		return selector.test(IteratorFactory.asStatement(t,ModelCom.this));
            	}
            });
        }
        return IteratorFactory.asStmtIterator(iter,this);
    }
	
    public Model begin() throws RDFException {
        throw new RDFException(RDFException.UNSUPPORTEDOPERATION);
    }
    
    public Model abort() throws RDFException {
        throw new RDFException(RDFException.UNSUPPORTEDOPERATION);
    }
    
    public Model commit() throws RDFException {
        throw new RDFException(RDFException.UNSUPPORTEDOPERATION);
    }
    
    public boolean independent() {
        return true;
    }
    
    public Resource createResource() throws RDFException {
        return IteratorFactory.asResource(Node.createAnon(new AnonId()),this);
    }
    
    public Resource createResource(String uri) throws RDFException {
        return getResource(uri);
    }
    
    public Property createProperty(String uri) throws RDFException {
        return getProperty(uri);
    }
    
    public Property createProperty(String nameSpace, String localName)
    throws RDFException {
        return getProperty(nameSpace, localName);
    }
    
    /**
        create a Statement from the given r, p, and o. We go round the houses
        (converting to Nodes, makeing a Triple, and invoking asStatement) so
        that the resulting Statement's components are all in the correct model.
    */
    public Statement createStatement(Resource r, Property p, RDFNode o)
    throws RDFException {
        return IteratorFactory.asStatement(
        new Triple(r.asNode(), p.asNode(), o.asNode()), this);
    }
    
    public Bag createBag(String uri) throws RDFException {
        return (Bag) getBag(uri).addProperty(RDF.type, RDF.Bag);
    }
    
    public Alt createAlt(String uri) throws RDFException {
        return (Alt) getAlt(uri).addProperty(RDF.type, RDF.Alt);
    }
    
    public Seq createSeq(String uri) throws RDFException {
        return (Seq) getSeq(uri).addProperty(RDF.type, RDF.Seq);
    }
    
    public NodeIterator listContainerMembers(Container cont,
                                             NodeIteratorFactory f)
                                                  throws RDFException {
        Iterator iter = listBySubject(cont);
        Statement    stmt;
        String       rdfURI = RDF.getURI();
        Vector       result = new Vector();
        int          maxOrdinal = 0;
        int          ordinal;
        while (iter.hasNext()) {
            stmt = (Statement) iter.next();
            ordinal = stmt.getPredicate().getOrdinal();
            if (stmt.getSubject().equals(cont) && ordinal != 0) {
                if (ordinal > maxOrdinal) {
                    maxOrdinal = ordinal;
                    result.setSize(ordinal);
                }
                result.setElementAt(stmt, ordinal-1);
            }
        }
        WrappedIterator.close( iter );
        try {
             return f.createIterator(result.iterator(), result, cont);
        } catch (Exception e) {
            throw new RDFException(e);
        }
    }
	
	
    public int containerSize(Container cont) throws RDFException {
        int result = 0;
        Iterator iter = listBySubject(cont);
        Property     predicate;
        Statement    stmt;
        String       rdfURI = RDF.getURI();
        while (iter.hasNext()) {
            stmt = (Statement) iter.next();
            predicate = stmt.getPredicate();
            if (stmt.getSubject().equals(cont)
             && predicate.getOrdinal() != 0
               ) {
                result++;
            }
        }
        WrappedIterator.close( iter );
        return result;
    }
	
	private Iterator asStatements( final Iterator it ) {
		final ModelCom self = this;
		return new Iterator() {
			public boolean hasNext() { return it.hasNext(); }
			public Object next() { return IteratorFactory.asStatement( (Triple) it.next(), self ); }
			public void remove() { it.remove(); }
		};
	}
	
	private Iterator listBySubject( Container cont ) {
		return asStatements( graph.find( cont.asNode(), null, null ) );
	}
	
    public int containerIndexOf(Container cont, RDFNode n) throws RDFException {
        int result = 0;
        Iterator iter = listBySubject(cont);
        Property     predicate;
        Statement    stmt;
        String       rdfURI = RDF.getURI();
        while (iter.hasNext()) {
            stmt = (Statement) iter.next();
            predicate = stmt.getPredicate();
            if (stmt.getSubject().equals(cont)
             && predicate.getOrdinal() != 0
             && n.equals(stmt.getObject())
              ) {
                result = predicate.getOrdinal();
                break;
            }
        }
        WrappedIterator.close( iter );
        return result;
    }
    
   public boolean containerContains(Container cont, RDFNode n)
    throws RDFException {
        return containerIndexOf(cont, n) != 0;
    }
    
    public Resource convert(Resource r) throws RDFException {
            return ((ResourceI)r).port(this);
    }
    
    public Property convert(Property p) throws RDFException {
            return (Property) ((ResourceI)p).port(this);
    }
    
    public RDFNode convert(RDFNode n) throws RDFException {
        if (n instanceof Property) {
            return convert((Property) n);
        } else if (n instanceof Resource) {
            return convert((Resource) n);
        } else {
            return n;
        }
    }
    
    public void close() {
        graph.close();
    }
    
    public boolean supportsTransactions() {return false;}
    
    public boolean supportsSetOperations() {return true;}
    
    public Model query(Selector selector) throws RDFException {
        ModelMem model = new ModelMem();
        StmtIterator iter = null;
        try {
            iter = listStatements(selector);
            while (iter.hasNext()) {
                model.add(iter.nextStatement());
            }
            return model;
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }
    
    public Model union(Model model) throws RDFException {
        return (new ModelMem()).add(this)
                               .add(model);
    }
    
    public Model intersection(Model model) throws RDFException {
        Model largerModel = this;
        Model smallerModel  = model;
        ModelMem resultModel = new ModelMem();
        StmtIterator iter = null;
        Statement stmt;
        if (model.size() > this.size()) {
            largerModel = model;
            smallerModel = this;
        }
        try {
            iter = smallerModel.listStatements();
            while (iter.hasNext()) {
                stmt = iter.nextStatement();
                if (largerModel.contains(stmt)) {
                    resultModel.add(stmt);
                }
            }
            return resultModel;
        } finally {
            iter.close();
        }
    }
    
    public Model difference(Model model) throws RDFException {
        ModelMem resultModel = new ModelMem();
        StmtIterator iter = null;
        Statement stmt;
        try {
            iter = listStatements();
            while (iter.hasNext()) {
                stmt = iter.nextStatement();
                if (! model.contains(stmt)) {
                    resultModel.add(stmt);
                }
            }
            return resultModel;
        } finally {
            iter.close();
        }
    }
    
    public boolean isIsomorphicWith(Model m){
    	return isIsomorphicWith((EnhGraph)m);
    }
}
