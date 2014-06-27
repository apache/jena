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

import java.io.* ;
import java.net.URL ;
import java.util.* ;

import com.hp.hpl.jena.datatypes.DatatypeFormatException ;
import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime ;
import com.hp.hpl.jena.enhanced.BuiltinPersonalities ;
import com.hp.hpl.jena.enhanced.EnhGraph ;
import com.hp.hpl.jena.enhanced.Personality ;
import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.* ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.util.CollectionFactory ;
import com.hp.hpl.jena.util.iterator.* ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** Common methods for model implementations.
 *
 * <P>This class implements common methods, mainly convenience methods, for
 *    model implementations.  It is intended use is as a base class from which
 *    model implemenations can be derived.</P>
 */

public class ModelCom extends EnhGraph
implements Model, PrefixMapping, Lock
{

    private static final RDFReaderF readerFactory = new RDFReaderFImpl();
    private static final RDFWriterF writerFactory = new RDFWriterFImpl();
    private Lock modelLock = null ;
    private static PrefixMapping defaultPrefixMapping = PrefixMapping.Factory.create();

    static {
        // This forces RIOT (in ARQ) to initialize but after Jena readers/writers
        // have cleanly initialized from the calls of  RDFReaderFImpl and RDFWriterFImpl
        // above.  RIOT initialization happens before model.read can be called.
        IO_Ctl.init();
    }
    
    /**
    	make a model based on the specified graph
     */
    public ModelCom( Graph base ) 
    { this( base, BuiltinPersonalities.model ); }

    public ModelCom( Graph base, Personality<RDFNode> personality )
    { super( base, personality ); 
    withDefaultMappings( defaultPrefixMapping ); }

    public static PrefixMapping getDefaultModelPrefixes()
    { return defaultPrefixMapping; }

    public static PrefixMapping setDefaultModelPrefixes(PrefixMapping pm)
    {
        PrefixMapping result = defaultPrefixMapping ;
        defaultPrefixMapping = pm ;
        return result ;
    }

    @Override
    public Graph getGraph()
    { return graph; }

    protected static Model createWorkModel()
    { return ModelFactory.createDefaultModel(); }

    @Override
    public RDFNode asRDFNode( Node n )
    {
        return n.isLiteral() 
            ? (RDFNode) this.getNodeAs( n, Literal.class )
            : (RDFNode) this.getNodeAs( n, Resource.class );
    }

    @Override
    public Resource wrapAsResource( Node n )
    {
        if (n.isLiteral()) 
            throw new UnsupportedOperationException( "literal cannot be converted to Resource" );
        return this.getNodeAs( n, Resource.class );
    }

    /**
        the ModelReifier does everything to do with reification.
     */
    protected ModelReifier modelReifier = new ModelReifier( this ); 

    @Override
    @Deprecated public Resource getResource(String uri, ResourceF f)  {
        try {
            return f.createResource(getResource(uri));
        } catch (Exception e) {
            throw new JenaException(e);
        }
    }

    @Override
    public Model addLiteral( Resource s, Property p, boolean o )  
    { return add(s, p, createTypedLiteral( o ) ); }

    @Override
    public Model addLiteral( Resource s, Property p, long o )  
    { return add(s, p, createTypedLiteral( o ) ); }

    @Override
    public Model addLiteral( Resource s, Property p, int o )  
    { return add(s, p, createTypedLiteral( o ) ); }

    @Override
    public Model addLiteral( Resource s, Property p, char o )  
    { return add(s, p, createTypedLiteral( o ) ); }

    @Override
    public Model addLiteral( Resource s, Property p, float o )  
    { return add( s, p, createTypedLiteral( o ) ); }

    @Override
    public Model addLiteral( Resource s, Property p, double o )  
    { return add(s, p, createTypedLiteral( o ) ); }

    @Override
    public Model add(Resource s, Property p, String o)  {
        return add( s, p, o, "", false );
    }

    @Override
    public Model add(Resource s, Property p, String o, boolean wellFormed)
    {
        add( s, p, literal( o, "", wellFormed ) );
        return this;
    }

    public Model add( Resource s, Property p, String o, String lang,
                      boolean wellFormed)  {
        add( s, p, literal( o, lang, wellFormed ) );
        return this;
    }

    @Override
    public Model add(Resource s, Property p, String lex, RDFDatatype datatype)
    {
        add( s, p, literal( lex, datatype)) ;
        return this;
    }

    private Literal literal( String s, String lang, boolean wellFormed )
    { return new LiteralImpl( NodeFactory.createLiteral( s, lang, wellFormed), this ); }

    private Literal literal( String lex, RDFDatatype datatype)
    { return new LiteralImpl( NodeFactory.createLiteral( lex, "", datatype), this ); }

    @Override
    public Model add( Resource s, Property p, String o, String l )
    { return add( s, p, o, l, false ); }

    @Override
    @Deprecated public Model addLiteral( Resource s, Property p, Object o )  
    { return add( s, p, asObject( o ) ); }

    @Override
    public Model addLiteral( Resource s, Property p, Literal o )  
    { return add( s, p, o ); }

    private RDFNode asObject( Object o )
    { return o instanceof RDFNode ? (RDFNode) o : createTypedLiteral( o ); }

    @Override
    public Model add( StmtIterator iter )  {
        try { GraphUtil.add( getGraph(), asTriples( iter ) ); }
        finally { iter.close(); }
        return this;
    }

    @Override
    public Model add(Model m) {
        GraphUtil.addInto(getGraph(), m.getGraph()) ;
        return this ;
    }

    @Override
    public RDFReader getReader()  {
        return readerFactory.getReader();
    }

    @Override
    public RDFReader getReader(String lang)  {
        return readerFactory.getReader(lang);
    }

    @Override
    public String setReaderClassName(String lang, String className) {
        return readerFactory.setReaderClassName(lang, className);
    } 
    
    @Override
    public void resetRDFReaderF()
    {
    	readerFactory.resetRDFReaderF();
    }
    
    @Override
    public String removeReader( String lang ) throws IllegalArgumentException
    {
    	return readerFactory.removeReader(lang);
    }

    @Override
    public Model read(String url)  {
        readerFactory .getReader() .read(this, url);
        return this;
    }

    @Override
    public Model read(Reader reader, String base)  {
        readerFactory .getReader() .read(this, reader, base);
        return this;
    }

    @Override
    public Model read(InputStream reader, String base)  {
        readerFactory .getReader() .read(this, reader, base);
        return this;
    } 

    @Override
    public Model read(String url, String lang)  {
        readerFactory. getReader(lang) .read(this, url);
        return this;
    }

    @Override
    public Model read( String url, String base, String lang )
    {
        try ( InputStream is = new URL( url ) .openStream() ) {
            read( is, base, lang );
        }
        catch (IOException e) { throw new WrappedIOException( e ); }
        return this;
    }

    @Override
    public Model read(Reader reader, String base, String lang)
    {
        readerFactory .getReader(lang) .read(this, reader, base);
        return this;
    }

    @Override
    public Model read(InputStream reader, String base, String lang)
    {
        readerFactory .getReader(lang) .read(this, reader, base);
        return this;
    }

    /**
        Get the model's writer after priming it with the model's namespace
        prefixes.
     */
    @Override
    public RDFWriter getWriter()  {
        return writerFactory.getWriter();
    }

    /**
        Get the model's writer after priming it with the model's namespace
        prefixes.
     */
    @Override
    public RDFWriter getWriter(String lang)  {
        return writerFactory.getWriter(lang);
    }


    @Override
    public String setWriterClassName(String lang, String className) {
        return writerFactory.setWriterClassName(lang, className);
    }

    @Override
    public void resetRDFWriterF()  {
    	writerFactory.resetRDFWriterF();
    }
    
    @Override
    public String removeWriter( String lang ) throws IllegalArgumentException {
    	return writerFactory.removeWriter( lang );
    }
    @Override
    public Model write(Writer writer) 
    {
        getWriter() .write(this, writer, "");
        return this;
    }

    @Override
    public Model write(Writer writer, String lang) 
    {
        getWriter(lang) .write(this, writer, "");
        return this;
    }

    @Override
    public Model write(Writer writer, String lang, String base)
    {
        getWriter(lang) .write(this, writer, base);
        return this;
    }

    @Override
    public Model write( OutputStream writer )
    {
        getWriter() .write(this, writer, "");
        return this;    
    }

    @Override
    public Model write(OutputStream writer, String lang) 
    {
        getWriter(lang) .write(this, writer, "");
        return this;
    }

    @Override
    public Model write(OutputStream writer, String lang, String base)
    {
        getWriter(lang) .write(this, writer, base);
        return this;
    }

    @Override
    public Model remove(Statement s)  {
        graph.delete(s.asTriple());
        return this;
    }

    @Override
    public Model remove( Resource s, Property p, RDFNode o ) {
        graph.delete( Triple.create( s.asNode(), p.asNode(), o.asNode() ) );
        return this;
    }


    @Override
    public Model remove( StmtIterator iter ) 
    {
        GraphUtil.delete( getGraph(), asTriples( iter ) );
        return this;
    }

    @Override
    public Model remove( Model m )
    {
        GraphUtil.deleteFrom( getGraph(), m.getGraph());
        return this;
    }

    @Override
    public Model removeAll()
    { 
        getGraph().clear();
        return this; 
    }

    @Override
    public Model removeAll( Resource s, Property p, RDFNode o )
    {
        getGraph().remove( asNode( s ), asNode( p ), asNode( o ) );
        return this;
    }

    @Override
    public boolean containsLiteral( Resource s, Property p, boolean o )
    { return contains(s, p, createTypedLiteral( o ) ); }

    @Override
    public boolean containsLiteral( Resource s, Property p, long o )
    { return contains(s, p, createTypedLiteral( o ) ); }

    @Override
    public boolean containsLiteral( Resource s, Property p, int o )
    { return contains(s, p, createTypedLiteral( o ) ); }

    @Override
    public boolean containsLiteral( Resource s, Property p, char o )
    { return contains(s, p, createTypedLiteral( o ) ); }

    @Override
    public boolean containsLiteral( Resource s, Property p, float o )
    { return contains(s, p, createTypedLiteral( o ) ); }

    @Override
    public boolean containsLiteral( Resource s, Property p, double o )
    { return contains(s, p, createTypedLiteral( o ) ); }

    @Override
    public boolean contains( Resource s, Property p, String o )
    { return contains( s, p, o, "" ); }

    @Override
    public boolean contains( Resource s, Property p, String o, String l )
    { return contains( s, p, literal( o, l, false ) ); }

    @Override
    public boolean containsLiteral(Resource s, Property p, Object o)
    { return contains( s, p, asObject( o ) ); }

    @Override
    public boolean containsAny( Model model ) 
    { return containsAnyThenClose( model.listStatements() ); }

    @Override
    public boolean containsAll( Model model )  
    { return containsAllThenClose( model.listStatements() ); }

    protected boolean containsAnyThenClose( StmtIterator iter )
    { try { return containsAny( iter ); } finally { iter.close(); } }

    protected boolean containsAllThenClose( StmtIterator iter )
    { try { return containsAll( iter ); } finally { iter.close(); } }

    @Override
    public boolean containsAny( StmtIterator iter ) 
    {
        while (iter.hasNext()) if (contains(iter.nextStatement())) return true;
        return false;
    }

    @Override
    public boolean containsAll( StmtIterator iter )  
    {
        while (iter.hasNext()) if (!contains(iter.nextStatement())) return false;
        return true;
    }

    protected StmtIterator listStatements( Resource S, Property P, Node O )
    {
        return IteratorFactory.asStmtIterator
            ( graph.find( asNode( S ), asNode( P ), O ), this );
    }

    @Override
    public StmtIterator listStatements( Resource S, Property P, RDFNode O )
    { return listStatements( S, P, asNode( O ) ); }

    @Override
    public StmtIterator listStatements( Resource S, Property P, String O ) {
        return O == null ? listStatements(S, P, Node.ANY) 
                         :  listStatements( S, P, NodeFactory.createLiteral( O ) ); 
    }

    @Override
    public StmtIterator listStatements( Resource S, Property P, String O, String L ) {
        if (O != null) {
            // this is not OK when L is null: returns only the statements whose lang is ""
            // return listStatements( S, P, Node.createLiteral( O, L, false ) );
            if (L != null) return listStatements( S, P, NodeFactory.createLiteral( O, L, false ) );
            // there's maybe a better way
            return new StringFilteredStmtIterator(O, listStatements(S, P, Node.ANY));
        } else {
            return new LangFilteredStmtIterator(L, listStatements(S, P, Node.ANY));
        }
    }
    
    private class StringFilteredStmtIterator extends FilterKeepIterator<Statement> implements StmtIterator {
        public StringFilteredStmtIterator(final String str, Iterator<Statement> it ) {
            super(
                    new Filter<Statement>() {
                        @Override public boolean accept(Statement s) {
                            RDFNode o = s.getObject();
                            if (o instanceof Literal) {
                                if (str == null) return true; // should not happen
                                return (str.equals(((Literal) o).getString()));
                            } 
                            return false;
                        }
              }, 
              it );
        }
        @Override public Statement nextStatement() { return next(); }
    }
    
    private class LangFilteredStmtIterator extends FilterKeepIterator<Statement> implements StmtIterator {
        public LangFilteredStmtIterator(final String l, Iterator<Statement> it ) {
            super(
                    new Filter<Statement>() {
                        @Override public boolean accept(Statement s) {
                            RDFNode o = s.getObject();
                            if (o instanceof Literal) {
                                if (l == null) return true;
                                return (l.equals(((Literal) o).getLanguage()));
                            } 
                            return false;
                        }
              }, 
              it );
        }
        @Override public Statement nextStatement() { return next(); }
    }
  

    @Override
    public StmtIterator listLiteralStatements( Resource S, Property P, boolean O )
    { return listStatements( S, P, createTypedLiteral( O ) ); }

    @Override
    public StmtIterator listLiteralStatements( Resource S, Property P, long O )
    { return listStatements( S, P, createTypedLiteral( O ) ); }

    @Override
    public StmtIterator listLiteralStatements( Resource S, Property P, char  O )
    { return listStatements( S, P, createTypedLiteral( O ) ); }

    @Override
    public StmtIterator listLiteralStatements( Resource S, Property P, float O )
    { return listStatements( S, P, createTypedLiteral( O ) ); }

    @Override
    public StmtIterator listLiteralStatements( Resource S, Property P, double  O )
    { return listStatements( S, P, createTypedLiteral( O ) ); }

    /*
         list resources with property [was: list subjects with property]
     */

    @Override
    public ResIterator listResourcesWithProperty( Property p, boolean o )
    { return listResourcesWithProperty(p, createTypedLiteral( o ) ); }

    @Override
    public ResIterator listResourcesWithProperty( Property p, char o )
    { return listResourcesWithProperty(p, createTypedLiteral( o ) ); }

    @Override
    public ResIterator listResourcesWithProperty( Property p, long o )
    { return listResourcesWithProperty(p, createTypedLiteral( o ) ); }

    @Override
    public ResIterator listResourcesWithProperty( Property p, float o )
    { return listResourcesWithProperty(p, createTypedLiteral( o ) ); }

    @Override
    public ResIterator listResourcesWithProperty( Property p, double o )
    { return listResourcesWithProperty(p, createTypedLiteral( o ) ); }

    @Override
    public ResIterator listResourcesWithProperty( Property p, Object o )
    { return listResourcesWithProperty( p, createTypedLiteral( o ) ); }

    @Override
    public ResIterator listSubjectsWithProperty( Property p, RDFNode o )
    { return listResourcesWithProperty( p, o ); }

    @Override
    public ResIterator listSubjectsWithProperty( Property p, String o )
    { return listSubjectsWithProperty( p, o, "" ); }

    @Override
    public ResIterator listSubjectsWithProperty( Property p, String o, String l )
    { return listResourcesWithProperty(p, literal( o, l, false ) ); }

    @Override
    public Resource createResource( Resource type )  
    { return createResource().addProperty( RDF.type, type ); }

    @Override
    public Resource createResource( String uri,Resource type )
    { return getResource( uri ).addProperty( RDF.type, type ); }

    @Override
    @Deprecated public Resource createResource( ResourceF f )  
    { return createResource( null, f ); }

    @Override
    public Resource createResource( AnonId id )
    { return new ResourceImpl( id, this ); }

    @Override
    @Deprecated public Resource createResource( String uri, ResourceF f )  
    { return f.createResource( createResource( uri ) ); }


    /** create a type literal from a boolean value.
     *
     * <p> The value is converted to a string using its <CODE>toString</CODE>
     * method. </p>
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */
    @Override
    public Literal createTypedLiteral( boolean v )  {
        return createTypedLiteral( new Boolean( v ) );
    }

    /** create a typed literal from an integer value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */   
    @Override
    public Literal createTypedLiteral(int v)   {
        return createTypedLiteral(new Integer(v));
    }

    /** create a typed literal from a long integer value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */   
    @Override
    public Literal createTypedLiteral(long v)   {
        return createTypedLiteral(new Long(v));
    }

    /** create a typed literal from a char value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */
    @Override
    public Literal createTypedLiteral(char v)  {
        return createTypedLiteral(new Character(v));
    }

    /** create a typed literal from a float value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */
    @Override
    public Literal createTypedLiteral(float v)  {
        return createTypedLiteral(new Float(v));
    }

    /** create a typed literal from a double value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */
    @Override
    public Literal createTypedLiteral(double v)  {
        return createTypedLiteral(new Double(v));
    }

    /** create a typed literal from a String value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */
    @Override
    public Literal createTypedLiteral(String v)  {
        LiteralLabel ll = LiteralLabelFactory.create(v);
        return new LiteralImpl(NodeFactory.createLiteral(ll), this);
    }

    /**
     * Create a typed literal xsd:dateTime from a Calendar object. 
     */
    @Override
    public Literal createTypedLiteral(Calendar cal) {
        Object value = new XSDDateTime(cal);
        LiteralLabel ll = LiteralLabelFactory.create(value, "", XSDDatatype.XSDdateTime);
        return new LiteralImpl(NodeFactory.createLiteral(ll), this);

    }

    /**
     * Build a typed literal from its lexical form. The
     * lexical form will be parsed now and the value stored. If
     * the form is not legal this will throw an exception.
     * 
     * @param lex the lexical form of the literal
     * @param dtype the type of the literal, null for old style "plain" literals
     * @throws DatatypeFormatException if lex is not a legal form of dtype
     */
    @Override
    public Literal createTypedLiteral(String lex, RDFDatatype dtype) 
        throws DatatypeFormatException {
        return new LiteralImpl( NodeFactory.createLiteral( lex, "", dtype ), this);
    }

    /**
     * Build a typed literal from its value form.
     * 
     * @param value the value of the literal
     * @param dtype the type of the literal, null for old style "plain" literals
     */
    @Override
    public Literal createTypedLiteral(Object value, RDFDatatype dtype) {
        LiteralLabel ll = LiteralLabelFactory.create(value, "", dtype);
        return new LiteralImpl( NodeFactory.createLiteral(ll), this );
    }

    /**
     * Build a typed literal from its lexical form. The
     * lexical form will be parsed now and the value stored. If
     * the form is not legal this will throw an exception.
     * 
     * @param lex the lexical form of the literal
     * @param typeURI the uri of the type of the literal, null for old style "plain" literals
     * @throws DatatypeFormatException if lex is not a legal form of dtype
     */
    @Override
    public Literal createTypedLiteral(String lex, String typeURI)  {
        RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(typeURI);
        LiteralLabel ll = LiteralLabelFactory.createLiteralLabel( lex, "", dt );
        return new LiteralImpl( NodeFactory.createLiteral(ll), this );
    }

    /**
     * Build a typed literal from its value form.
     * 
     * @param value the value of the literal
     * @param typeURI the URI of the type of the literal, null for old style "plain" literals
     */
    @Override
    public Literal createTypedLiteral(Object value, String typeURI) {
        RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(typeURI);
        LiteralLabel ll = LiteralLabelFactory.create(value, "", dt);
        return new LiteralImpl(NodeFactory.createLiteral(ll), this);
    }

    /**
     * Build a typed literal label from its value form using
     * whatever datatype is currently registered as the the default
     * representation for this java class. No language tag is supplied.
     * @param value the literal value to encapsulate
     */
    @Override
    public Literal createTypedLiteral( Object value ) 
    {
        // Catch special case of a Calendar which we want to act as if it were an XSDDateTime
        if (value instanceof Calendar) 
            return createTypedLiteral( (Calendar)value );
        LiteralLabel ll = LiteralLabelFactory.create( value );
        return new LiteralImpl( NodeFactory.createLiteral( ll ), this);
    }

    @Override
    public Literal createLiteral( String v )  
    { return createLiteral( v, "" ); }

    @Override
    public Literal createLiteral( String v, String l )  
    { return literal( v, l, false ); }

    @Override
    public Literal createLiteral( String v, boolean wellFormed ) 
    { return literal( v, "", wellFormed ); }

    public Literal createLiteral(String v, String l, boolean wellFormed) 
    { return literal( v, l, wellFormed ); }

    @Override
    public Statement createLiteralStatement( Resource r, Property p, boolean o )
    { return createStatement( r, p, createTypedLiteral( o ) ); }

    @Override
    public Statement createLiteralStatement( Resource r, Property p, long o )
    { return createStatement( r, p, createTypedLiteral( o ) ); }

    @Override
    public Statement createLiteralStatement( Resource r, Property p, int o )
    { return createStatement( r, p, createTypedLiteral( o ) ); }

    @Override
    public Statement createLiteralStatement( Resource r, Property p, char o )
    { return createStatement( r, p, createTypedLiteral( o ) ); }

    @Override
    public Statement createLiteralStatement( Resource r, Property p, float o )
    { return createStatement( r, p, createTypedLiteral( o ) ); }

    @Override
    public Statement createLiteralStatement( Resource r, Property p, double o )
    { return createStatement( r, p, createTypedLiteral( o ) ); }

    @Override
    public Statement createStatement( Resource r, Property p, String o )
    { return createStatement( r, p, createLiteral( o ) ); }

    @Override
    public Statement createLiteralStatement( Resource r, Property p, Object o )
    { return createStatement( r, p, asObject( o ) ); }

    @Override
    public Statement createStatement
    ( Resource r, Property p, String o, boolean wellFormed )  
    { return createStatement( r, p, o, "", wellFormed ); }

    @Override
    public Statement createStatement(Resource r, Property p, String o, String l)
    { return createStatement( r, p, o, l, false ); }

    @Override
    public Statement createStatement
    ( Resource r, Property p, String o, String l, boolean wellFormed )  
    { return createStatement( r, p, literal( o, l, wellFormed ) ); }

    @Override
    public Bag createBag()  
    { return createBag( null ); }

    @Override
    public Alt createAlt()  
    { return createAlt( null ); }

    @Override
    public Seq createSeq()  
    { return createSeq( null ); }

    /**
        Answer a (the) new empty list
        @return An RDF-encoded list of no elements (ie nil)
     */
    @Override
    public RDFList createList() 
    { return getResource( RDF.nil.getURI() ).as( RDFList.class ); }


    /**
     * <p>Answer a new list containing the resources from the given iterator, in order.</p>
     * @param members An iterator, each value of which is expected to be an RDFNode.
     * @return An RDF-encoded list of the elements of the iterator
     */
    @Override
    public RDFList createList( Iterator<? extends RDFNode> members ) 
    {
        RDFList list = createList();
        while (members != null && members.hasNext()) list = list.with( members.next() );
        return list;
    }


    /**
     * <p>Answer a new list containing the RDF nodes from the given array, in order</p>
     * @param members An array of RDFNodes that will be the members of the list
     * @return An RDF-encoded list 
     */
    @Override
    public RDFList createList( RDFNode[] members ) {
        return createList( Arrays.asList( members ).iterator() );
    }

    @Override
    public RDFNode getRDFNode( Node n )
    { return asRDFNode( n ); }

    @Override
    public Resource getResource( String uri )  
    { return IteratorFactory.asResource(makeURI(uri),this); }

    @Override
    public Property getProperty( String uri )  
    {
        if (uri == null) throw new InvalidPropertyURIException( null );
        return IteratorFactory.asProperty( makeURI(uri), this );
    }

    @Override
    public Property getProperty( String nameSpace,String localName )
    { return getProperty( nameSpace + localName ); }

    @Override
    public Seq getSeq( String uri )  
    { return (Seq) IteratorFactory.asResource( makeURI( uri ),Seq.class, this); }

    @Override
    public Seq getSeq( Resource r )  
    { return r.inModel( this ).as( Seq.class ); }

    @Override
    public Bag getBag( String uri )  
    { return (Bag) IteratorFactory.asResource( makeURI( uri ),Bag.class, this ); }

    @Override
    public Bag getBag( Resource r )  
    { return r.inModel( this ).as( Bag.class ); }

    static private Node makeURI(String uri) 
    { return uri == null ? NodeFactory.createAnon() : NodeFactory.createURI( uri ); }

    @Override
    public Alt getAlt( String uri )  
    { return (Alt) IteratorFactory.asResource( makeURI(uri) ,Alt.class, this ); }

    @Override
    public Alt getAlt( Resource r )  
    { return r.inModel( this ).as( Alt.class ); }

    @Override
    public long size()  
    { return graph.size(); }

    @Override
    public boolean isEmpty()
    { return graph.isEmpty(); }

    private void updateNamespace( Set<String> set, Iterator<Node> it )
    {
        while (it.hasNext())
        {
            Node node = it.next();
            if (node.isURI())
            {
                String uri = node.getURI();
                String ns = uri.substring( 0, Util.splitNamespace( uri ) );
                // String ns = IteratorFactory.asResource( node, this ).getNameSpace();
                set.add( ns );
            }
        }
    }

    private ExtendedIterator<Node> listPredicates()
    { return GraphUtil.listPredicates(graph, Node.ANY, Node.ANY ); }

    private Iterator<Node> listTypes()
    {
        Set<Node> types = CollectionFactory.createHashedSet();
        ClosableIterator<Triple> it = graph.find( null, RDF.type.asNode(), null );
        while (it.hasNext()) types.add( it.next().getObject() );
        return types.iterator();
    }

    @Override
    public NsIterator listNameSpaces()  {
        Set<String> nameSpaces = CollectionFactory.createHashedSet();
        updateNamespace( nameSpaces, listPredicates() );
        updateNamespace( nameSpaces, listTypes() );
        return new NsIteratorImpl(nameSpaces.iterator(), nameSpaces);
    }

    private PrefixMapping getPrefixMapping()
    { return getGraph().getPrefixMapping(); }

    @Override
    public boolean samePrefixMappingAs( PrefixMapping other )
    { return getPrefixMapping().samePrefixMappingAs( other ); }

    @Override
    public PrefixMapping lock()
    {
        getPrefixMapping().lock();
        return this;
    }

    @Override
    public PrefixMapping setNsPrefix( String prefix, String uri )
    { 
        getPrefixMapping().setNsPrefix( prefix, uri ); 
        return this;
    }

    @Override
    public PrefixMapping removeNsPrefix( String prefix )
    {
        getPrefixMapping().removeNsPrefix( prefix );
        return this;
    }

    @Override
    public PrefixMapping setNsPrefixes( PrefixMapping pm )
    { 
        getPrefixMapping().setNsPrefixes( pm );
        return this;
    }

    @Override
    public PrefixMapping setNsPrefixes( Map<String, String> map )
    { 
        getPrefixMapping().setNsPrefixes( map ); 
        return this;
    }

    @Override
    public PrefixMapping withDefaultMappings( PrefixMapping other )
    {
        getPrefixMapping().withDefaultMappings( other );
        return this;
    }

    @Override
    public String getNsPrefixURI( String prefix ) 
    { return getPrefixMapping().getNsPrefixURI( prefix ); }

    @Override
    public String getNsURIPrefix( String uri )
    { return getPrefixMapping().getNsURIPrefix( uri ); }

    @Override
    public Map<String, String> getNsPrefixMap()
    { return getPrefixMapping().getNsPrefixMap(); }

    @Override
    public String expandPrefix( String prefixed )
    { return getPrefixMapping().expandPrefix( prefixed ); }

    @Override
    public String qnameFor( String uri )
    { return getPrefixMapping().qnameFor( uri ); }

    @Override
    public String shortForm( String uri )
    { return getPrefixMapping().shortForm( uri ); }

    /**
        Service method to update the namespaces of  a Model given the
        mappings from prefix names to sets of URIs.

        If the prefix maps to multiple URIs, then we discard it completely.

        @param m Model who's namespace is to be updated
        @param ns the namespace map to add to the Model      
     */
    public static void addNamespaces( Model m, Map<String, Set<String>> ns )
    { 
        PrefixMapping pm = m;
        for ( Map.Entry<String, Set<String>> e : ns.entrySet() )
        {
            String key = e.getKey();
            Set<String> values = e.getValue();
            Set<String> niceValues = CollectionFactory.createHashedSet();
            for ( String uri : values )
            {
                if ( PrefixMappingImpl.isNiceURI( uri ) )
                {
                    niceValues.add( uri );
                }
            }
            if ( niceValues.size() == 1 )
            {
                pm.setNsPrefix( key, niceValues.iterator().next() );
            }
        }            
    }

    @Override
    public StmtIterator listStatements()  
    { return IteratorFactory.asStmtIterator( GraphUtil.findAll( graph ), this); }

    /**
        add a Statement to this Model by adding its SPO components.
     */
    @Override
    public Model add( Statement s )  
    {
        add( s.getSubject(), s.getPredicate(), s.getObject() );
        return this;
    }

    /**
        Add all the statements to the model by converting them to an array of corresponding
        triples and removing those from the underlying graph.
     */
    @Override
    public Model add( Statement [] statements )
    {
        GraphUtil.add(getGraph(), StatementImpl.asTriples( statements ) );
        return this;
    }

    /**
        Add all the statements to the model by converting the list to an array of
        Statement and removing that.
     */
    @Override
    public Model add( List<Statement> statements )
    {
        GraphUtil.add(getGraph(), asTriples( statements ) );
        return this;
    }

    private List<Triple> asTriples( List<Statement> statements )
    {
        List<Triple> L = new ArrayList<>( statements.size() );
        for ( Statement statement : statements )
        {
            L.add( statement.asTriple() );
        }
        return L;
    }

    private Iterator<Triple> asTriples( StmtIterator it )
    { return it.mapWith( mapAsTriple ); }

    private Map1<Statement, Triple> mapAsTriple = new Map1<Statement, Triple>() {
        @Override
        public Triple map1( Statement s ) { return s.asTriple(); } 
    };
    
    /**
        remove all the Statements from the model by converting them to triples and
        removing those triples from the underlying graph.        
     */ 
    @Override
    public Model remove( Statement [] statements )
    {
        GraphUtil.delete( getGraph(), StatementImpl.asTriples( statements ) );        
        return this;
    }

    /**
        Remove all the Statements from the model by converting the List to a
        List(Statement) and removing that.
     */
    @Override
    public Model remove( List<Statement> statements )
    {
        GraphUtil.delete( getGraph(), asTriples( statements ) );
        return this;
    }

    @Override
    public Model add( Resource s, Property p, RDFNode o )  {
        modelReifier.noteIfReified( s, p, o );
        graph.add( Triple.create( s.asNode(), p.asNode(), o.asNode() ) );
        return this;
    }

    /**
        @return an iterator which delivers all the ReifiedStatements in this model
     */
    @Override
    public RSIterator listReifiedStatements()
    { return modelReifier.listReifiedStatements(); }

    /**
        @return an iterator each of whose elements is a ReifiedStatement in this
            model such that it's getStatement().equals( st )
     */
    @Override
    public RSIterator listReifiedStatements( Statement st )
    { return modelReifier.listReifiedStatements( st ); }

    /**
        @return true iff this model has a reification of _s_ in some Statement
     */
    @Override
    public boolean isReified( Statement s ) 
    { return modelReifier.isReified( s ); }

    /**
        get any reification of the given statement in this model; make
        one if necessary.

        @param s for which a reification is sought
        @return a ReifiedStatement that reifies _s_
     */
    @Override
    public Resource getAnyReifiedStatement(Statement s) 
    { return modelReifier.getAnyReifiedStatement( s ); }

    /**
        remove any ReifiedStatements reifying the given statement
        @param s the statement who's reifications are to be discarded
     */
    @Override
    public void removeAllReifications( Statement s ) 
    { modelReifier.removeAllReifications( s ); }

    @Override
    public void removeReification( ReifiedStatement rs )
    { modelReifier.removeReification( rs ); }

    /**
        create a ReifiedStatement that encodes _s_ and belongs to this Model.
     */
    @Override
    public ReifiedStatement createReifiedStatement( Statement s )
    { return modelReifier.createReifiedStatement( s ); }

    @Override
    public ReifiedStatement createReifiedStatement( String uri, Statement s )
    { return modelReifier.createReifiedStatement( uri, s ); }

    @Override
    public boolean contains( Statement s )    
    { return graph.contains( s.asTriple() ); }

    @Override
    public boolean containsResource( RDFNode r )
    { return GraphUtil.containsNode( graph, r.asNode() ); }

    @Override
    public boolean contains( Resource s, Property p ) 
    { return contains( s, p, (RDFNode) null );  }

    @Override
    public boolean contains( Resource s, Property p, RDFNode o )
    { return graph.contains( asNode( s ), asNode( p ), asNode( o ) ); }

    @Override
    public Statement getRequiredProperty( Resource s, Property p )  
    { Statement st = getProperty( s, p );
    if (st == null) throw new PropertyNotFoundException( p );
    return st; }

    @Override
    public Statement getProperty( Resource s, Property p )
    {
        StmtIterator iter = listStatements( s, p, (RDFNode) null );
        try { return iter.hasNext() ? iter.nextStatement() : null; }
        finally { iter.close(); }
    }

    public static Node asNode( RDFNode x )
    { return x == null ? Node.ANY : x.asNode(); }

    private NodeIterator listObjectsFor( RDFNode s, RDFNode p )
    {
        ClosableIterator<Node> xit = GraphUtil.listObjects(graph, asNode( s ), asNode( p ) ) ;
        return IteratorFactory.asRDFNodeIterator( xit, this );
    }

    private ResIterator listSubjectsFor( RDFNode p, RDFNode o )
    {
        ClosableIterator<Node> xit = GraphUtil.listSubjects( graph, asNode( p ), asNode( o ) );
        return IteratorFactory.asResIterator( xit, this );
    }

    @Override
    public ResIterator listSubjects()  
    { return listSubjectsFor( null, null ); }

    @Override
    public ResIterator listResourcesWithProperty(Property p)
    { return listSubjectsFor( p, null ); }

    @Override
    public ResIterator listSubjectsWithProperty(Property p)
    { return listResourcesWithProperty( p ); }

    @Override
    public ResIterator listResourcesWithProperty(Property p, RDFNode o)
    { return listSubjectsFor( p, o ); }

    @Override
    public NodeIterator listObjects()  
    { return listObjectsFor( null, null ); }

    @Override
    public NodeIterator listObjectsOfProperty(Property p)  
    { return listObjectsFor( null, p ); }

    @Override
    public NodeIterator listObjectsOfProperty(Resource s, Property p)
    { return listObjectsFor( s, p ); }

    @Override
    public StmtIterator listStatements( final Selector selector )
    {
        StmtIterator sts = IteratorFactory.asStmtIterator( findTriplesFrom( selector ), this );
        return selector.isSimple() 
            ? sts 
            : new StmtIteratorImpl( sts .filterKeep ( asFilter( selector ) ) )
        ;
    }

    /**
        Answer a Filter that filters exactly those things the Selector selects.

        @param s a Selector on statements
        @return a Filter that accepts statements that s passes tests on
     */
     public Filter<Statement> asFilter( final Selector s )
     { return new Filter<Statement>()
         { @Override public boolean accept( Statement x ) { return s.test( x ); } };
     }


     /**
        Answer an [extended] iterator which returns the triples in this graph which
        are selected by the (S, P, O) triple in the selector, ignoring any special
        tests it may do.

        @param s a Selector used to supply subject, predicate, and object
        @return an extended iterator over the matching (S, P, O) triples
      */
     public ExtendedIterator<Triple> findTriplesFrom( Selector s )
     {
         return graph.find
             ( asNode( s.getSubject() ), asNode( s.getPredicate() ), asNode( s.getObject() ) );    
     }

     @Override
     public boolean supportsTransactions() 
     { return getTransactionHandler().transactionsSupported(); }

     @Override
     public Model begin() 
     { getTransactionHandler().begin(); return this; }

     @Override
     public Model abort() 
     { getTransactionHandler().abort(); return this; }

     @Override
     public Model commit() 
     { getTransactionHandler().commit(); return this; }

     @Override
     public Object executeInTransaction( Command cmd )
     { return getTransactionHandler().executeInTransaction( cmd ); }

     private TransactionHandler getTransactionHandler()
     { return getGraph().getTransactionHandler(); }

     @Override
     public boolean independent() 
     { return true; }

     @Override
     public Resource createResource()  
     { return IteratorFactory.asResource( NodeFactory.createAnon(),this ); }

     @Override
     public Resource createResource( String uri )  
     { return getResource( uri ); }

     @Override
     public Property createProperty( String uri )  
     { return getProperty( uri ); }

     @Override
     public Property createProperty(String nameSpace, String localName)
     { return getProperty(nameSpace, localName); }

     /**
        create a Statement from the given r, p, and o.
      */
      @Override
      public Statement createStatement(Resource r, Property p, RDFNode o)
      { return new StatementImpl( r, p, o, this ); }

      @Override
      public Bag createBag(String uri)  
      { return (Bag) getBag(uri).addProperty( RDF.type, RDF.Bag ); }

      @Override
      public Alt createAlt( String uri ) 
      { return (Alt) getAlt(uri).addProperty( RDF.type, RDF.Alt ); }

      @Override
      public Seq createSeq(String uri)  
      { return (Seq) getSeq(uri).addProperty( RDF.type, RDF.Seq ); }

      /**
        Answer a Statement in this Model whcih encodes the given Triple.
        @param t a triple to wrap as a statement
        @return a statement wrapping the triple and in this model
       */
      @Override
      public Statement asStatement( Triple t )
      { return StatementImpl.toStatement( t, this ); }

      public Statement [] asStatements( Triple [] triples )
      {
          Statement [] result = new Statement [triples.length];
          for (int i = 0; i < triples.length; i += 1) result[i] = asStatement( triples[i] );
          return result;    
      }

      public List<Statement> asStatements( List<Triple> triples )
      {
          List<Statement> L = new ArrayList<>( triples.size() );
          for ( Triple triple : triples )
          {
              L.add( asStatement( triple ) );
          }
          return L;
      }

      public Model asModel( Graph g )
      { return new ModelCom( g ); }

      public StmtIterator asStatements( final Iterator<Triple> it ) 
      { return new StmtIteratorImpl( new Map1Iterator<>( mapAsStatement, it ) ); }

      protected Map1<Triple, Statement> mapAsStatement = new Map1<Triple, Statement>()
          { @Override
          public Statement map1( Triple t ) { return asStatement( t ); } };

          public StmtIterator listBySubject( Container cont )
          { return listStatements( cont, null, (RDFNode) null ); }

          @Override
          public void close() 
          { graph.close(); }

          @Override
          public boolean isClosed()
          { return graph.isClosed(); }

          @Override
          public boolean supportsSetOperations() 
          {return true;}

          @Override
          public Model query( Selector selector )  
          { return createWorkModel() .add( listStatements( selector ) ); }

          @Override
          public Model union( Model model )  
          { return createWorkModel() .add(this) .add( model ); }

          /**
        Intersect this with another model. As an attempt at optimisation, we try and ensure
        we iterate over the smaller model first. Nowadays it's not clear that this is a good
        idea, since <code>size()</code> can be expensive on database and inference
        models.

     	@see com.hp.hpl.jena.rdf.model.Model#intersection(com.hp.hpl.jena.rdf.model.Model)
           */
          @Override
          public Model intersection( Model other )
          { return this.size() < other.size() ? intersect( this, other ) : intersect( other, this ); }

          /**
        Answer a Model that is the intersection of the two argument models. The first
        argument is the model iterated over, and the second argument is the one used
        to check for membership. [So the first one should be "small" and the second one
        "membership cheap".]
           */
          public static Model intersect( Model smaller, Model larger )
          {
              Model result = createWorkModel();
              StmtIterator it = smaller.listStatements();
              try { return addCommon( result, it, larger ); }
              finally { it.close(); }
          }

          /**
        Answer the argument result with all the statements from the statement iterator that
        are in the other model added to it.

     	@param result the Model to add statements to and return
     	@param it an iterator over the candidate statements
     	@param other the model that must contain the statements to be added
     	@return result, after the suitable statements have been added to it
           */
          protected static Model addCommon( Model result, StmtIterator it, Model other )
          {
              while (it.hasNext())
              {
                  Statement s = it.nextStatement();
                  if (other.contains( s )) result.add( s );    
              }
              return result;
          }

          @Override
          public Model difference(Model model)  {
              Model resultModel = createWorkModel();
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
                  if (null != iter) {
                      iter.close();
                  }
              }
          }

          @Override
          public String toString()
          { return "<ModelCom  " + getGraph() + " | " + reifiedToString() + ">"; }

          public String reifiedToString()
          { return statementsToString( listStatements() ); }

          protected String statementsToString( StmtIterator it )
          {
              StringBuilder b = new StringBuilder();
              while (it.hasNext()) b.append( " " ).append( it.nextStatement() );
              return b.toString();
          }

          /**
            Answer whether or not these two graphs are isomorphic.
           */
          @Override
          public boolean isIsomorphicWith( Model m )
          {
              Graph L = this.getGraph();  
              Graph R = m.getGraph();
              return L.isIsomorphicWith( R );
          }

          public synchronized Lock getModelLock()
          {
              if ( modelLock == null )
                  modelLock = new LockMRSW() ;
              return modelLock ;
          }

          @Override
          public synchronized Lock getLock()
          {
              return getModelLock() ;
          }


          @Override
          public void enterCriticalSection(boolean requestReadLock)
          {
              this.getModelLock().enterCriticalSection(requestReadLock) ;
          }

          @Override
          public void leaveCriticalSection()
          {
              this.getModelLock().leaveCriticalSection() ;
          }

          /**
        Register the listener with this model by registering its GraphListener
        adaption with the underlying Graph.

        @param listener A ModelChangedListener to register for model events
        @return this model, for cascading 
           */
          @Override
          public Model register( ModelChangedListener listener )
          {
              getGraph().getEventManager().register( adapt( listener ) );
              return this;
          }

          /**
        Unregister the listener from this model by unregistering its GraphListener
        adaption from the underlying Graph.
        @param  listener A ModelChangedListener to unregister from model events
        @return this model, for cascading 
           */
          @Override
          public Model unregister( ModelChangedListener listener )
          {
              getGraph().getEventManager().unregister( adapt( listener ) );
              return this;
          }

          /**
        Answer a GraphListener that, when fed graph-level update events,
        fires the corresponding model-level event handlers in <code>L</code>.
        @see ModelListenerAdapter
        @param L a model listener to be wrapped as a graph listener
        @return a graph listener wrapping L
           */
          public GraphListener adapt( final ModelChangedListener L )
          { return new ModelListenerAdapter( this, L ); }

          @Override
          public Model notifyEvent( Object e )
          {
              getGraph().getEventManager().notifyEvent( getGraph(), e );
              return this;
          }
}
