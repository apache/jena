/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
    [See end of file]
    $Id: ModelCom.java,v 1.94 2004-11-04 12:27:10 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.shared.impl.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.query.*;

import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.enhanced.*;

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
 */

public class ModelCom 
    extends EnhGraph
    implements Model, PrefixMapping, ModelLock
{

      private RDFReaderF readerFactory = new RDFReaderFImpl();
      private RDFWriterF writerFactory = new RDFWriterFImpl();
      private ModelLock modelLock = null ;
      
    /**
    	make a model based on the specified graph
    */
	public ModelCom( Graph base ) 
        { this( base, BuiltinPersonalities.model ); }
    
    public ModelCom( Graph base, Personality personality )
        { super( base, personality ); 
        addDefaultPrefixMapping(); }
    
    private static PrefixMapping defaultPrefixMapping = PrefixMapping.Factory.create();
    
    protected void addDefaultPrefixMapping()
        { Map temp = defaultPrefixMapping.getNsPrefixMap();
        temp.putAll( this.getNsPrefixMap() );
        this.setNsPrefixes( temp ); }
    
    public static PrefixMapping getDefaultModelPrefixes()
        { return defaultPrefixMapping; }
    
    public static PrefixMapping setDefaultModelPrefixes( PrefixMapping pm )
        { PrefixMapping result = defaultPrefixMapping;
        defaultPrefixMapping = pm;
        return result; }
    
    public QueryHandler queryHandler()
    	{ return getGraph().queryHandler(); }
		
    public Graph getGraph()
        { return graph; }
               
    protected static Model createWorkModel()
        { return ModelFactory.createDefaultModel(); }
    
    public RDFNode asRDFNode( Node n )
        {
        return n.isLiteral() 
          ? (RDFNode) this.getNodeAs( n, Literal.class )
          : (RDFNode) this.getNodeAs( n, Resource.class );
        }

    /**
        the ModelReifier does everything to do with reification.
    */
    protected ModelReifier modelReifier = new ModelReifier( this ); 
	
    public Resource getResource(String uri, ResourceF f)  {
        try {
            return f.createResource(getResource(uri));
        } catch (Exception e) {
            throw new JenaException(e);
        }
    }
    
    public Model add(Resource s, Property p, boolean o)  {
        return add(s, p, String.valueOf( o ) );
    }
    
    public Model add(Resource s, Property p, long o)  {
        return add(s, p, String.valueOf( o ) );
    }
    
    public Model add(Resource s, Property p, char o)  {
        return add(s, p, String.valueOf( o ) );
    }
    
    public Model add(Resource s, Property p, float o)  {
        return add(s, p, String.valueOf( o ) );
    }
    
    public Model add(Resource s, Property p, double o)  {
        return add(s, p, String.valueOf( o ) );
    }
    
    public Model add(Resource s, Property p, String o)  {
        return add( s, p, o, "", false );
    }
    
    public Model add(Resource s, Property p, String o, boolean wellFormed)
        {
        add(s, p, literal(o, "", wellFormed));
        return this;
        }
    
    public Model add(Resource s, Property p, String o, String lang,
      boolean wellFormed)  {
        add(s, p, literal(o, lang, wellFormed));
        return this;
    }
    
    private Literal literal( String s, String lang, boolean wellFormed )
        { return new LiteralImpl( Node.createLiteral( s, lang, wellFormed), this ); }
    
    public Model add(Resource s, Property p, String o, String l)
        { return add( s, p, o, l, false ); }
    
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
        
    public Model add(Resource s, Property p, Object o)  {
        return add( s, p, ensureRDFNode( o ) );
    }
    
    public Model add( StmtIterator iter )  {
        try { getBulkUpdateHandler().add( asTriples( iter ) ); }
        finally { iter.close(); }
        return this;
    }
    
    public Model add( Model m )  
        { return add( m, false ); }
        
    public Model add( Model m, boolean suppressReifications ) {
        getBulkUpdateHandler().add( m.getGraph(), !suppressReifications );
        return this;
    }
    
    public RDFReader getReader()  {
        return readerFactory.getReader();
    }
    
    public RDFReader getReader(String lang)  {
        return readerFactory.getReader(lang);
    }
    
    public String setReaderClassName(String lang, String className) {
        return readerFactory.setReaderClassName(lang, className);
    } 
    
    public Model read(String url)  {
        readerFactory .getReader() .read(this, url);
        return this;
    }
    
    public Model read(Reader reader, String base)  {
        readerFactory .getReader() .read(this, reader, base);
        return this;
    }
    
  	public Model read(InputStream reader, String base)  {
  		readerFactory .getReader() .read(this, reader, base);
  		return this;
  	} 
    
    public Model read(String url, String lang)  {
        readerFactory. getReader(lang) .read(this, url);
        return this;
    }
    
    public Model read(Reader reader, String base, String lang)
       {
        readerFactory .getReader(lang) .read(this, reader, base);
        return this;
    }
    
  	public Model read(InputStream reader, String base, String lang)
  	   {
  		readerFactory .getReader(lang) .read(this, reader, base);
  		return this;
  	}

    /**
        Get the model's writer after priming it with the model's namespace
        prefixes.
    */
    public RDFWriter getWriter()  {
        return writerFactory.getWriter();
    }
    
    /**
        Get the model's writer after priming it with the model's namespace
        prefixes.
    */
    public RDFWriter getWriter(String lang)  {
        return writerFactory.getWriter(lang);
    }
    

    public String setWriterClassName(String lang, String className) {
        return writerFactory.setWriterClassName(lang, className);
    }
    
    public Model write(Writer writer) 
        {
        getWriter() .write(this, writer, "");
        return this;
        }
    
    public Model write(Writer writer, String lang) 
        {
        getWriter(lang) .write(this, writer, "");
        return this;
        }
    
    public Model write(Writer writer, String lang, String base)
        {
        getWriter(lang) .write(this, writer, base);
        return this;
        }
    
  	public Model write( OutputStream writer )
        {
        getWriter() .write(this, writer, "");
  		return this;    
        }
    
  	public Model write(OutputStream writer, String lang) 
        {
  		getWriter(lang) .write(this, writer, "");
  		return this;
  	    }
    
  	public Model write(OutputStream writer, String lang, String base)
  	    {
        getWriter(lang) .write(this, writer, base);
  		return this;
  	    }
    
    public Model remove(Statement s)  {
        graph.delete(s.asTriple());
        return this;
    }
    
    public Model remove( StmtIterator iter ) 
        {
        getBulkUpdateHandler().delete( asTriples( iter ) );
        return this;
        }
    
    public Model remove( Model m )
        { return remove( m, false ); }
        
    public Model remove( Model m, boolean suppressReifications ) 
        {
        getBulkUpdateHandler().delete( m.getGraph(), !suppressReifications );
        return this;
        }
    
    public Model removeAll()
        { 
        getGraph().getBulkUpdateHandler().removeAll();
        return this; 
        }
    
    public Model removeAll( Resource s, Property p, RDFNode o )
        {
        getGraph().getBulkUpdateHandler().remove( asNode( s ), asNode( p ), asNode( o ) );
        return this;
        }
        
    public boolean contains( Resource s, Property p, boolean o )
        { return contains(s, p, String.valueOf( o ) ); }
    
    public boolean contains( Resource s, Property p, long o )
        { return contains(s, p, String.valueOf( o ) ); }
    
    public boolean contains( Resource s, Property p, char o )
        { return contains(s, p, String.valueOf( o ) ); }
    
    public boolean contains( Resource s, Property p, float o )
        { return contains(s, p, String.valueOf( o ) ); }
    
    public boolean contains( Resource s, Property p, double o )
        { return contains(s, p, String.valueOf( o ) ); }
    
    public boolean contains(Resource s, Property p, String o)
        { return contains(s, p, o, "" ); }
    
    public boolean contains(Resource s, Property p, String o, String l)
        { return contains( s, p, literal( o, l, false ) ); }
    
    public boolean contains(Resource s, Property p, Object o)
        { return contains( s, p, ensureRDFNode( o ) ); }
    
    public boolean containsAny(StmtIterator iter) {
        while (iter.hasNext()) {
            if (contains(iter.nextStatement())) return true;
        }
        return false;
    }
    
    public boolean containsAll(StmtIterator iter)  {
        while (iter.hasNext()) {
            if (!contains(iter.nextStatement())) return false;
        }
        return true;
    }
    
    public boolean containsAny(Model model) {
        StmtIterator iter = model.listStatements();
        try { return containsAny(iter); }
        finally { iter.close(); }
    }
    
    public boolean containsAll(Model model)  {
        StmtIterator iter = model.listStatements();
        try { return containsAll(iter); } 
        finally { iter.close(); }
    }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        RDFNode  object)
        { return listStatements( new SimpleSelector( subject, predicate, object ) ); }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        boolean object)
        { return listStatements( new SimpleSelector( subject, predicate, object ) ); }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        long    object)
        { return listStatements( new SimpleSelector( subject, predicate, object ) ); }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        char  object)
        { return listStatements( new SimpleSelector( subject, predicate, object ) ); }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        float   object)
         { return listStatements( new SimpleSelector( subject, predicate, object ) ); }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        double  object)
        { return listStatements( new SimpleSelector(subject, predicate, object ) ); }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        String   object)
        { return listStatements( new SimpleSelector(subject, predicate, object ) ); }
    
    public StmtIterator listStatements(Resource subject,
                                        Property predicate,
                                        String   object,
                                        String   lang)
        { return listStatements( new SimpleSelector(subject, predicate, object, lang ) ); }
    
    public ResIterator listSubjectsWithProperty(Property p, boolean o)
     {
        return listSubjectsWithProperty(p, String.valueOf( o ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, long o)
     {
        return listSubjectsWithProperty(p, String.valueOf( o ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, char o)
     {
        return listSubjectsWithProperty(p, String.valueOf( o ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, float o)
     {
        return listSubjectsWithProperty(p, String.valueOf( o ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, double o)
     {
        return listSubjectsWithProperty(p, String.valueOf( o ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, String o)
     {
        return listSubjectsWithProperty( p, o, "" );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, String o, String l)
     {
        return listSubjectsWithProperty(p, literal( o, l, false ) );
    }
    
    public ResIterator listSubjectsWithProperty(Property p, Object o)
     {
        return listSubjectsWithProperty( p, ensureRDFNode( o ) );
    }
    
    public Resource createResource(Resource type)  {
        return createResource().addProperty(RDF.type, type);
    }
    
    public Resource createResource(String uri,Resource type)
     {
        return getResource(uri)
                   .addProperty(RDF.type, type);
    }
    
    public Resource createResource(ResourceF f)  {
        return createResource(null, f);
    }
    
    public Resource createResource( AnonId id )
        { return new ResourceImpl( id, this ); }
        
    public Resource createResource(String uri, ResourceF f)  {
       return f.createResource( createResource( uri ) );
    }
    
 
    /** create a type literal from a boolean value.
     *
     * <p> The value is converted to a string using its <CODE>toString</CODE>
     * method. </p>
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(boolean v)  {
        return createTypedLiteral(new Boolean(v));
    }
    
    /** create a typed literal from an integer value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */   
    public Literal createTypedLiteral(int v)   {
        return createTypedLiteral(new Integer(v));
    }
    
    /** create a typed literal from a long integer value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */   
    public Literal createTypedLiteral(long v)   {
        return createTypedLiteral(new Long(v));
    }
    
    /** create a typed literal from a char value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(char v)  {
        return createTypedLiteral(new Character(v));
    }
    
    /** create a typed literal from a float value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(float v)  {
        return createTypedLiteral(new Float(v));
    }
    
    /** create a typed literal from a double value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(double v)  {
        return createTypedLiteral(new Double(v));
    }
    
    /** create a typed literal from a String value.
     *
     * @param v the value of the literal
     * 
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(String v)  {
        LiteralLabel ll = new LiteralLabel(v);
        return new LiteralImpl(Node.createLiteral(ll), this);
    }

    /**
     * Create a typed literal xsd:dateTime from a Calendar object. 
     */
    public Literal createTypedLiteral(Calendar cal) {
        Object value = new XSDDateTime(cal);
        LiteralLabel ll = new LiteralLabel(value, "", XSDDatatype.XSDdateTime);
        return new LiteralImpl(Node.createLiteral(ll), this);
        
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
    public Literal createTypedLiteral(String lex, RDFDatatype dtype) 
                                        throws DatatypeFormatException {
        LiteralLabel ll = new LiteralLabel(lex, "", dtype);
        return new LiteralImpl(Node.createLiteral(ll), this);
    }
    
    /**
     * Build a typed literal from its value form.
     * 
     * @param value the value of the literal
     * @param dtype the type of the literal, null for old style "plain" literals
     */
    public Literal createTypedLiteral(Object value, RDFDatatype dtype) {
        LiteralLabel ll = new LiteralLabel(value, "", dtype);
        return new LiteralImpl( Node.createLiteral(ll), this );
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
    public Literal createTypedLiteral(String lex, String typeURI)  {
        RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(typeURI);
        LiteralLabel ll = new LiteralLabel(lex, "", dt);
        return new LiteralImpl( Node.createLiteral(ll), this );
    }
        
    /**
     * Build a typed literal from its value form.
     * 
     * @param value the value of the literal
     * @param typeURI the URI of the type of the literal, null for old style "plain" literals
     */
    public Literal createTypedLiteral(Object value, String typeURI) {
        RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(typeURI);
        LiteralLabel ll = new LiteralLabel(value, "", dt);
        return new LiteralImpl(Node.createLiteral(ll), this);
    }
        
    /**
     * Build a typed literal label from its value form using
     * whatever datatype is currently registered as the the default
     * representation for this java class. No language tag is supplied.
     * @param value the literal value to encapsulate
     */
    public Literal createTypedLiteral(Object value) {
        LiteralLabel ll = new LiteralLabel(value);
        return new LiteralImpl(Node.createLiteral(ll), this);
    }

    public Literal createLiteral(boolean v)  
        { return createLiteral(String.valueOf(v), ""); }
    
    public Literal createLiteral(int v)  {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(long v)  {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(char v)  {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(float v)  {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(double v)  {
        return createLiteral(String.valueOf(v), "");
    }
    
    public Literal createLiteral(String v)  {
        return createLiteral(v, "");
    }
    
    public Literal createLiteral(String v, String l)  {
        return literal(v, l, false);
    }
    
    public Literal createLiteral(String v, boolean wellFormed) {
        return literal(v, "", wellFormed);
    }
    
    public Literal createLiteral(String v, String l, boolean wellFormed) {
        return literal(v, l, wellFormed);
    }
    
    public Literal createLiteral(Object v)  {
        return createLiteral(v.toString(), "");
    }
    
    public Statement createStatement(Resource r, Property p, boolean o)
     {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, long o)
     {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, char o)
     {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, float o)
     {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, double o)
     {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, String o)
     {
        return createStatement(r, p, createLiteral(o));
    }
    
    public Statement createStatement(Resource r, Property p, Object o)
     {
        return createStatement( r, p, ensureRDFNode( o ) );
    }
    
    public Statement createStatement(Resource r, Property p, String o,
                                     boolean wellFormed)  {
        return createStatement( r, p, o, "", wellFormed );
    }
    
    public Statement createStatement(Resource r, Property p, String o, String l)
       {
        return createStatement( r, p, o, l, false );
    }
    
    public Statement createStatement(Resource r, Property p, String o, String l,
                                     boolean wellFormed)  {
        return createStatement(r, p, literal(o,l,wellFormed));
    }
    
    public Bag createBag()  {
        return createBag(null);
    }
    
    public Alt createAlt()  {
        return createAlt(null);
    }
    
    public Seq createSeq()  {
        return createSeq(null);
    }
    
    /**
     * <p>Answer a new empty list</p>
     * @return An RDF-encoded list of no elements
     */
    public RDFList createList() {
        Resource list = getResource( RDF.nil.getURI() );
        
        return (RDFList) list.as( RDFList.class );
    }
    
    
    /**
     * <p>Answer a new list containing the resources from the given iterator, in order.</p>
     * @param members An iterator, each value of which is expected to be an RDFNode.
     * @return An RDF-encoded list of the elements of the iterator
     */
    public RDFList createList( Iterator members ) {
        RDFList list = createList();
        
        while (members != null && members.hasNext()) {
            list = list.with( (RDFNode) members.next() );
        }
        
        return list;
    }
    
    
    /**
     * <p>Answer a new list containing the RDF nodes from the given array, in order</p>
     * @param members An array of RDFNodes that will be the members of the list
     * @return An RDF-encoded list 
     */
    public RDFList createList( RDFNode[] members ) {
        return createList( Arrays.asList( members ).iterator() );
    }
    
    public RDFNode getRDFNode( Node n )
        {   
        return n.isURI() || n.isBlank()
            ? (RDFNode) new ResourceImpl( n, this )
            : (RDFNode) new LiteralImpl( n, this ); 
        }
    
    public Resource getResource(String uri)  {
        return IteratorFactory.asResource(makeURI(uri),this);
    }
    
    public Property getProperty(String uri)  {
        if ( uri == null )
             throw new InvalidPropertyURIException( null );
        return IteratorFactory.asProperty(makeURI(uri),this);
    }
    
    public Property getProperty(String nameSpace,String localName)
       {
        return getProperty(nameSpace+localName);
    }
    
    public Seq getSeq(String uri)  {
      return (Seq)IteratorFactory.asResource(makeURI(uri),Seq.class, this);
    }
    
    public Seq getSeq(Resource r)  {
        return (Seq) r.as( Seq.class );
    }
    
    public Bag getBag(String uri)  {
      return (Bag)IteratorFactory.asResource(makeURI(uri),Bag.class, this);
    }
    
    public Bag getBag(Resource r)  {
        return (Bag) r.as( Bag.class );
    }
    
    static private Node makeURI(String uri) {
        if ( uri == null )
            return Node.createAnon(new AnonId());
        else
            return Node.createURI(uri);
    }
    
    public Alt getAlt(String uri)  {
      return (Alt)IteratorFactory.asResource(makeURI(uri),Alt.class, this);
    }
    
    public Alt getAlt(Resource r)  {
        return (Alt) r.as( Alt.class );
    }
    
    public long size()  {
        return graph.size();
    }

    public boolean isEmpty()
        { return graph.isEmpty(); }
        
    private void updateNamespace( Set set, Iterator it )
        {
        while (it.hasNext())
            {
            Node node = (Node) it.next();
            if (node.isURI())
                {
                String uri = node.getURI();
                String ns = uri.substring( 0, Util.splitNamespace( uri ) );
                // String ns = IteratorFactory.asResource( node, this ).getNameSpace();
                set.add( ns );
                }
            }
        }
        
    private Iterator listPredicates()
        {
        Set predicates = HashUtils.createSet();
        ClosableIterator it = graph.find( null, null, null );
        while (it.hasNext()) predicates.add( ((Triple) it.next()).getPredicate() );
        return predicates.iterator();
        }
     
    private Iterator listTypes()
        {
        Set types = HashUtils.createSet();
        ClosableIterator it = graph.find( null, RDF.type.asNode(), null );
        while (it.hasNext()) types.add( ((Triple) it.next()).getObject() );
        return types.iterator();
        }
     
    public NsIterator listNameSpaces()  {
        Set nameSpaces = HashUtils.createSet();
        updateNamespace( nameSpaces, listPredicates() );
        updateNamespace( nameSpaces, listTypes() );
        return new NsIteratorImpl(nameSpaces.iterator(), nameSpaces);
    }
    
    private PrefixMapping getPrefixMapping()
        { return getGraph().getPrefixMapping(); }
        
    public PrefixMapping lock()
        {
        getPrefixMapping().lock();
        return this;
        }
        
    public PrefixMapping setNsPrefix( String prefix, String uri )
        { 
        getPrefixMapping().setNsPrefix( prefix, uri ); 
        return this;
        }
        
    public PrefixMapping removeNsPrefix( String prefix )
        {
        getPrefixMapping().removeNsPrefix( prefix );
        return this;
        }
    
    public PrefixMapping setNsPrefixes( PrefixMapping pm )
        { 
        getPrefixMapping().setNsPrefixes( pm );
        return this;
        }
        
    public PrefixMapping setNsPrefixes( Map map )
        { 
        getPrefixMapping().setNsPrefixes( map ); 
        return this;
        }
        
    public String getNsPrefixURI( String prefix ) 
        { return getPrefixMapping().getNsPrefixURI( prefix ); }

    public String getNsURIPrefix( String uri )
        { return getPrefixMapping().getNsURIPrefix( uri ); }
                
    public Map getNsPrefixMap()
        { return getPrefixMapping().getNsPrefixMap(); }
        
    public String expandPrefix( String prefixed )
        { return getPrefixMapping().expandPrefix( prefixed ); }
        
    public String usePrefix( String uri )
        { return getPrefixMapping().shortForm( uri ); }
    
    public String qnameFor( String uri )
        { return getPrefixMapping().qnameFor( uri ); }
    
    public String shortForm( String uri )
        { return getPrefixMapping().shortForm( uri ); }
        
    /**
        Service method to update the namespaces of  a Model given the
        mappings from prefix names to sets of URIs.
        
        If the prefix maps to multiple URIs, then we discard it completely.
        
        @param the Model who's namespace is to be updated
        @param ns the namespace map to add to the Model      
    */
    public static void addNamespaces( Model m, Map ns )
        { 
        PrefixMapping pm = m;
        Iterator it  = ns.entrySet().iterator();
        while (it.hasNext())
            {
            Map.Entry e = (Map.Entry) it.next();
            String key = (String) e.getKey();
            Set  values = (Set) e.getValue();
            Set niceValues = HashUtils.createSet();
            Iterator them = values.iterator();
            while (them.hasNext())
                {
                String uri = (String) them.next();
                if (PrefixMappingImpl.isNiceURI( uri )) niceValues.add( uri );
                }
            if (niceValues.size() == 1)
                pm.setNsPrefix( key, (String) niceValues.iterator().next() );
            }            
        }
        
    public StmtIterator listStatements()  {
        return IteratorFactory.asStmtIterator( GraphUtil.findAll( graph ), this);
    }

    /**
        add a Statement to this Model by adding its SPO components.
    */
    public Model add(Statement s)  {
        add( s.getSubject(), s.getPredicate(), s.getObject() );
        return this;
    }
    
    /**
        Add all the statements to the model by converting them to an array of corresponding
        triples and removing those from the underlying graph.
    */
    public Model add( Statement [] statements )
        {
        getBulkUpdateHandler().add( StatementImpl.asTriples( statements ) );
        return this;
        }
        
    protected BulkUpdateHandler getBulkUpdateHandler()
        { return getGraph().getBulkUpdateHandler(); }
        
    /**
        Add all the statements to the model by converting the list to an array of
        Statement and removing that.
    */
    public Model add( List statements )
        {
        getBulkUpdateHandler().add( asTriples( statements ) );
        return this;
        }
        
    private List asTriples( List statements )
        {
        List L = new ArrayList( statements.size() );
        for (int i = 0; i < statements.size(); i += 1) 
            L.add( ((Statement) statements.get(i)).asTriple() );
        return L;
        }
        
    private Iterator asTriples( StmtIterator it )
        { return new Map1Iterator( mapAsTriple, it ); }
        
    private Map1 mapAsTriple = new Map1()
        { public Object map1( Object s ) { return ((Statement) s).asTriple(); } };
        
    /**
        remove all the Statements from the model by converting them to triples and
        removing those triples from the underlying graph.        
    */ 
    public Model remove( Statement [] statements )
        {
        getBulkUpdateHandler().delete( StatementImpl.asTriples( statements ) );        
        return this;
        }
     
     /**
        Remove all the Statements from the model by converting the List to a
        Statement [] and removing that.
     */
    public Model remove( List statements )
        {
        getBulkUpdateHandler().delete( asTriples( statements ) );
        return this;
        }
           
    public Model add(Resource s,Property p,RDFNode o)  {
        modelReifier.noteIfReified( s, p, o );
        graph.add( Triple.create( s.asNode(), p.asNode(), o.asNode() ) );
        return this;
    }
    
    public ReificationStyle getReificationStyle()
        { return modelReifier.getReificationStyle(); }
        
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
    
    public boolean contains( Statement s )    
        { return graph.contains( s.asTriple() ); }
    
    public boolean containsResource( RDFNode r )
        { return graph.queryHandler().containsNode( r.asNode() ); }
  
    public boolean contains( Resource s, Property p )  {
        ClosableIterator it = graph.find( asNode( s ), asNode( p ), null );
        try { return it.hasNext(); } finally { it.close(); }
    }
    
    public boolean contains( Resource s, Property p, RDFNode o )
        { return graph.contains( asNode( s ), asNode( p ), asNode( o ) ); }
        
    public Statement getRequiredProperty( Resource s, Property p )  
        { Statement st = getProperty( s, p );
        if (st == null) throw new PropertyNotFoundException( p );
        return st; }
    
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
        ClosableIterator xit = graph.queryHandler().objectsFor( asNode( s ), asNode( p ) );
        return IteratorFactory.asRDFNodeIterator( xit, this );
        }

    private ResIterator listSubjectsFor( RDFNode p, RDFNode o )
        {
        ClosableIterator xit = graph.queryHandler().subjectsFor( asNode( p ), asNode( o ) );
        return IteratorFactory.asResIterator( xit, this );
        }
                
    public ResIterator listSubjects()  
        { return listSubjectsFor( null, null ); }
    
    public ResIterator listSubjectsWithProperty(Property p)
        { return listSubjectsFor( p, null ); }
    
    public ResIterator listSubjectsWithProperty(Property p, RDFNode o)
        { return listSubjectsFor( p, o ); }
    
    public NodeIterator listObjects()  
        { return listObjectsFor( null, null ); }
    
    public NodeIterator listObjectsOfProperty(Property p)  
        { return listObjectsFor( null, p ); }
    
    public NodeIterator listObjectsOfProperty(Resource s, Property p)
        { return listObjectsFor( s, p ); }
            
    public StmtIterator listStatements(final Selector selector)
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
    public Filter asFilter( final Selector s )
        { return new Filter()
                { public boolean accept( Object x ) { return s.test( (Statement) x ); } };
        }
        
    
    /**
        Answer an [extended] iterator which returns the triples in this graph which
        are selected by the (S, P, O) triple in the selector, ignoring any special
        tests it may do.
        
        @param s a Selector used to supply subject, predicate, and object
        @return an extended iterator over the matching (S, P, O) triples
    */
    public ExtendedIterator findTriplesFrom( Selector s )
        {
        return graph.find
            ( asNode( s.getSubject() ), asNode( s.getPredicate() ), asNode( s.getObject() ) );    
        }

    public boolean supportsTransactions() 
        { return getTransactionHandler().transactionsSupported(); }
    	
    public Model begin() 
        { getTransactionHandler().begin(); return this; }
    
    public Model abort() 
        { getTransactionHandler().abort(); return this; }
    
    public Model commit() 
        { getTransactionHandler().commit(); return this; }
    
    public Object executeInTransaction( Command cmd )
        { return getTransactionHandler().executeInTransaction( cmd ); }
        
    private TransactionHandler getTransactionHandler()
        { return getGraph().getTransactionHandler(); }
        
    public boolean independent() {
        return true;
    }
    
    public Resource createResource()  {
        return IteratorFactory.asResource(Node.createAnon(new AnonId()),this);
    }
    
    public Resource createResource(String uri)  {
        return getResource(uri);
    }
    
    public Property createProperty(String uri)  {
        return getProperty(uri);
    }
    
    public Property createProperty(String nameSpace, String localName)
     {
        return getProperty(nameSpace, localName);
    }
    
    /**
        create a Statement from the given r, p, and o.
    */
    public Statement createStatement(Resource r, Property p, RDFNode o)
     {
        return new StatementImpl( r, p, o, this ); 
    }
    
    public Bag createBag(String uri)  {
        return (Bag) getBag(uri).addProperty(RDF.type, RDF.Bag);
    }
    
    public Alt createAlt(String uri)  {
        return (Alt) getAlt(uri).addProperty(RDF.type, RDF.Alt);
    }
    
    public Seq createSeq(String uri)  {
        return (Seq) getSeq(uri).addProperty(RDF.type, RDF.Seq);
    }

    /**
        Answer a Statement in this Model whcih encodes the given Triple.
        @param t a triple to wrap as a statement
        @return a statement wrapping the triple and in this model
    */
    public Statement asStatement( Triple t )
        { return StatementImpl.toStatement( t, this ); }
        
    public Statement [] asStatements( Triple [] triples )
        {
        Statement [] result = new Statement [triples.length];
        for (int i = 0; i < triples.length; i += 1) result[i] = asStatement( triples[i] );
        return result;    
        }
        
    public List asStatements( List triples )
        {
        List L = new ArrayList( triples.size() );
        for (int i = 0; i < triples.size(); i += 1) L.add( asStatement( (Triple) triples.get(i) ) );
        return L;
        }
        
    public Model asModel( Graph g )
        { return new ModelCom( g ); }
        
	public StmtIterator asStatements( final Iterator it ) 
        { return new StmtIteratorImpl( new Map1Iterator( mapAsStatement, it ) ); }
    
    protected Map1 mapAsStatement = new Map1()
        { public Object map1( Object t ) { return asStatement( (Triple) t ); } };
	
	public StmtIterator listBySubject( Container cont )
        { return listStatements( cont, null, (RDFNode) null ); }

    public void close() 
        { graph.close(); }
    
    public boolean supportsSetOperations() 
        {return true;}
    
    public Model query( Selector selector )  
        { return createWorkModel() .add( listStatements( selector ) ); }
    
    public Model union( Model model )  
        { return createWorkModel() .add(this) .add( model ); }
    
    /**
        Intersect this with another model. As an attempt at optimisation, we try and ensure
        we iterate over the smaller model first. Nowadays it's not clear that this is a good
        idea, since <code>size()</code> can be expensive on database and inference
        models.
        
     	@see com.hp.hpl.jena.rdf.model.Model#intersection(com.hp.hpl.jena.rdf.model.Model)
    */
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
            iter.close();
        }
    }
    
    public String toString()
        { return "<ModelCom  " + getGraph() + " | " + reifiedToString() + ">"; }
        
    public String reifiedToString()
        { return statementsToString( getHiddenStatements().listStatements() ); }
        
    protected String statementsToString( StmtIterator it )
        {
        StringBuffer b = new StringBuffer();
        while (it.hasNext()) b.append( " " ).append( it.nextStatement() );
        return b.toString();
        }
	/**
		a read-only Model with all the statements of this Model and any
		statements "hidden" by reification. That model is dynamic, ie
		any changes this model will be reflected that one.
	*/    
    public Model getHiddenStatements()
        { return modelReifier.getHiddenStatements(); }
        
    /**
        Answer whether or not these two graphs are isomorphic, taking the
        hidden (reification) statements into account.
    */
    public boolean isIsomorphicWith( Model m )
        {
        Graph L = ModelFactory.withHiddenStatements( this ).getGraph();            
        Graph R = ModelFactory.withHiddenStatements( m ).getGraph();
        return L.isIsomorphicWith( R );
        }
        
    public synchronized ModelLock getModelLock()
    {
        if ( modelLock == null )
            modelLock = new ModelLockImpl() ;
        return modelLock ;
    }
    
    public void enterCriticalSection(boolean requestReadLock)
    {
        this.getModelLock().enterCriticalSection(requestReadLock) ;
    }
    
    public void leaveCriticalSection()
    {
        this.getModelLock().leaveCriticalSection() ;
    }
        
    /**
        Register the listener with this model by registering its GraphListener
        adaption with the underlying Graph.
       
        @param a ModelChangedListener to register for model events
        @return this model, for cascading 
    */
    public Model register( ModelChangedListener listener )
        {
        getGraph().getEventManager().register( adapt( listener ) );
        return this;
        }
        
    /**
        Unregister the listener from this model by unregistering its GraphListener
        adaption from the underlying Graph.
        @param  a ModelChangedListener to unregister from model events
        @return this model, for cascading 
    */
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
    
    public Model notifyEvent( Object e )
        {
        getGraph().getEventManager().notifyEvent( getGraph(), e );
        return this;
        }
    }

/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
 