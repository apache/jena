/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Model.java,v 1.26 2003-06-25 14:20:10 jeremy_carroll Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

import java.io.*;
import java.util.*;

/** An RDF Model.
 *
 * <p>An RDF model is a set of Statements.  Methods are provided for creating
 * resources, properties and literals and the Statements which link them,
 * for adding statements to and removing them from a model, for
 * querying a model and set operations for combining models.</p>
 * <P>This interface defines a set of primitive methods.  A set of
 * convenience methods which extends this interface, e.g. performing
 * automatic type conversions and support for enhanced resources,
 * is defined in {@link ModelCon}.</P>
 *
 * <h2>System Properties</h2>
 *
 *
 * <h3>Firewalls and Proxies</h3>
 *
 * <p>Some of the methods, e.g. the read methods, may have to traverse a
 * firewall.  This can be accomplished using the standard java method
 * of setting system properties.  To use a socks proxy, include on the
 * java command line:</p>
 * <blockquote>
 *   -DsocksProxyHost=[your-proxy-domain-name-or-ip-address]
 * </blockquote>
 *
 * <p>To use an http proxy, include on the command line:</p>
 * <blockquote>
 * -DproxySet=true -DproxyHost=[your-proxy] -DproxyPort=[your-proxy-port-number]
 * </blockquote>
 *
 * <p>Alternatively, these properties can be set programatically, e.g.</p>
 *
 * <code><pre>
 *   System.getProperties().put("proxySet","true");
 *   System.getProperties().put("proxyHost","proxy.hostname");
 *   System.getProperties().put("proxyPort",port_number);
 * </pre></code>
 *
 * @author bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.26 $Date: 2003/06/24 10:34:25 $'
 */
public interface Model 
    extends ModelCon, RDFReaderF, RDFWriterF, PrefixMapping, ModelLock
{

	/** Every model is based on some Graph */
	Graph getGraph();

	/** Every Model has a QueryHandler */
	QueryHandler queryHandler();

	/** (Unwise) Computes the number of statements in the model.
	 * Many implementations cannot do this efficiently.
	 
	 * @return the number of statements in the model
	 */
	long size() ;

	// @deprecated Too difficult to implement scalably.
	/** List all resources which are subjects of statements.
	 *
	 * <p>Subsequent operations on those resource may modify this model.</p>
	 
	 * @return an iterator over a set of resources which are subjects of statements
	 *         in the model.
	 * 
	 */
	ResIterator listSubjects() ;

	//* @deprecated Too difficult to implement scalably.
	/** List all namespaces of predicates in the model.
	 
	 * @return an iterator over the set of namespaces associated with predicates in 
	 *         the model.
	 */
	NsIterator listNameSpaces() ;

    /**
        Answer an iterator over all the bindings implied by the query model.
        
        TODO complete this explanation.
    */
    ExtendedIterator queryBindingsWith( Model query, Resource [] variables );
    
	/** Return a Resource instance in this model.
	 *
	 * <p>Subsequent operations on the returned object may modify this model.</p>
	 * <p>This method should be called if the resource may already exist in the
	 *    model so that an implementation may reuse the same object.  If it does
	 *    not an object will be created.  If it is known that an object for the
	 *    resource does not already exist, then it may be more efficient to call
	 * <CODE>createResource</CODE> instead.</p>
	 * @return a resource instance
	 * @param uri the URI of the resource
	 .
	 */
	Resource getResource(String uri) ;

	/** Return a Property instance in this model.
	 *
	 * <p>Subsequent operations on the returned property may modify this model.</p>
	 * <p>This method should be called if the property may already exist in the
	 *    model so that an implementation may reuse the same object.  If it does
	 *    not an object will be created.  If it is known that an object for the
	 *    property does not already exist, then it may be more efficient to call
	 * <CODE>createProperty</CODE> instead.</p>
	 * @return a property linked to this model
	 * @param nameSpace the RDF namespace of the property
	 * @param localName the localName of the property in its namespace
	 
	 */
	Property getProperty(String nameSpace, String localName)
		;

	/** Create a new anonymous resource.
	 *
	 * <p> Subsequent operations on the returned resource may modify this model.
	 * </p>
	 .
	 * @return a new anonymous resource linked to this model.
	 */
	public Resource createResource() ;

    /**
        create a blank node resource with a specified identifier/
        
        @param id the identifier to use for this blank node
        @return a blank node with that identifier
    */
    public Resource createResource( AnonId id );
    
	/** Create a new resource.
	 *
	 * <p> Subsequent operations on the returned resource may modify this model.
	 * </p>
	 * @param uri the URI of the resource to be created
	 .
	 * @return a new resource linked to this model.
	 */
	public Resource createResource(String uri) ;

	/** Create a property.
	 *
	 * <p> Subsequent operations on the returned property may modify this model.
	 * </p>
	 * @param nameSpace the nameSpace of the property
	 * @param localName the name of the property within its namespace
	 
	 * @return a property instance
	 */
	public Property createProperty(String nameSpace, String localName)
		;

	/** Create a literal from a String value with a specified language.
	 *
	 * <P>If v is null, then a literal with an empty string is created.</P>
	 *
	 * @param v the value of the literal
	 * @param language the language associated with the literal
	 
	 * @return a new literal representing the value v with the given language
	 */

	public Literal createLiteral(String v, String language)
		;

	/** Create a literal from a String value with a specified language.
	 *
	 * <P>If v is null, then a literal with an empty string is created.</P>
	 *
	 * @param v the value of the literal
	 * @param language the language associated with the literal
	 * @param wellFormed true if the Literal is well formed XML
	 
	 * @return a new literal representing the value v with the given language
	 */
	public Literal createLiteral(String v, String language, boolean wellFormed)
		;

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
                                            ;
        
        /**
         * Build a typed literal from its value form.
         * 
         * @param value the value of the literal
         * @param lang the optional language tag
         * @param dtype the type of the literal, null for old style "plain" literals
         */
        public Literal createTypedLiteral(Object value, String lang, RDFDatatype dtype);
        
        /**
         * Build a typed literal label from its value form using
         * whatever datatype is currently registered as the the default
         * representation for this java class. No language tag is supplied.
         * @param value the literal value to encapsulate
         */
        public Literal createTypedLiteral(Object value);

	/** Create a Statement instance.
	 *
	 * <p>Subsequent operations on the statement or any of its parts will
	 * modify this model.</p>
	 * <p>Creating a statement does not add it to the set of statements in the
	 * model. </p>
	 * @param s the subject of the statement
	 * @param p the predicate of the statement
	 * @param o the object of the statement
	 
	 * @return the new statement
	 */
	public Statement createStatement(Resource s, Property p, RDFNode o)
		;

    /**
     * <p>Answer a new empty list. This is equivalent to a list consisting only 
     * of <code>rdf:nil</code>.</p>
     * @return An RDF-encoded list of no elements
     */
    public RDFList createList();
    
    
    /**
     * <p>Answer a new list containing the resources from the given iterator, in order.</p>
     * @param members An iterator, each value of which is expected to be an RDFNode
     * @return An RDF-encoded list of the elements of the iterator
     */
    public RDFList createList( Iterator members );
    
    
    /**
     * <p>Answer a new list containing the nodes from the given array, in order</p>
     * @param members An array of RDF nodes that will be the members of the list
     * @return An RDF-encoded list 
     */
    public RDFList createList( RDFNode[] members );
    
    
	/** Add a statement to this model.
	 * @return This model.
	 * @param s The statement to be added.
	 
	 */
	Model add(Statement s) ;
    
    /**
        Add all the statements to the Model, using through the bulk update interface.

        @param statements the array of statements to add
        @return this model, to allow cascading
    */    
    Model add( Statement [] statements );
    
    /**
        Remove all the statements from the Model, using the bulk update interface.
        @param statements the array of statements to be added
        @return this model, to allow cascading
    */
    Model remove( Statement [] statements );
    
    /**
        add all the statements in the List to this Model, going through the bulk
        update interface (which means turning them into triples in one form or
        another).
        @param statements a List of Statements
        @return this model, to allow cascading
    */    
    Model add( List statements );
    
    /**
        Remove all the statements in the list from this model, using the bulk
        update interface.
        @param statements a List of Statements to remove
        @return this model, to allow cascading
    */
    Model remove( List statements );

	/** Add all the statements returned by an iterator to this model.
	 * @return this model
	 * @param iter An iterator which returns the statements to be added.
	 
	 */
	Model add(StmtIterator iter) ;

	/** Add all the statements in another model to this model.
	 * @return this model
	 * @param m The model whose statements are to be added.
	 
	 */
	Model add(Model m) ;

	/** Add the RDF statements from an XML document.
	 *
	 * <p>See {@link Model} for a description of how to traverse a firewall.</p>
	 * @return this model
	 * @param url of the document containing the RDF statements.
	 
	 */
	public Model read(String url) ;

	/** Add statements from an RDF/XML serialization.
	 * @param in the source of the RDF/XML
	 * @param base the base to use when converting relative to absolute uri's.
	 * The base URI may be null if there are no relative URIs to convert.
	 * A base URI of "" may permit relative URIs to be used in the
	 * model unconverted.
	 * @return the current model
	 */
	public Model read(InputStream in, String base) ;

	/** Add RDF statements represented in language <code>lang</code> to the model.
	 * <br />Predefined values for <code>lang</code> are "RDF/XML", "N-TRIPLE"
	 * and "N3".  <code>null</code> represents the default language, "RDF/XML".
	 * "RDF/XML-ABBREV" is a synonym for "RDF/XML".
     * <br />
	 *
	 * @return this model
	 * @param base the base uri to be used when converting relative
	 * URI's to absolute URI's.
	  The base URI may be null if there are no relative URIs to 
	  convert.
	  A base URI of "" may permit relative URIs to be used in the
	   model.
	 * @param lang the langauge of the serialization <code>null<code>
	 * selects the default
	 * @param in the source of the input serialization
	 */
	public Model read(InputStream in, String base, String lang)
		;
	
    /** Using this method is often a mistake.
	 * Add statements from an RDF/XML serialization.
     * It is generally better to use an InputStream if possible.
     * {@link Model#read(InputStream,String)}, otherwise there is a danger of a
     * mismatch between the character encoding of say the FileReader and the
     * character encoding of the data in the file.
	 * @param reader the source of the RDF/XML
	 * @param base the base to use when converting relative to absolute uri's.
	  The base URI may be null if there are no relative URIs to 
	  convert.
	  A base URI of "" may permit relative URIs to be used in the
	   model.
	 * * @return the current model
	 */
	public Model read(Reader reader, String base) ;
	
	/** 
	 * Add statements from a serializion in language <code>lang</code> to the
	 * model.
     * <br />Predefined values for <code>lang</code> are "RDF/XML", "N-TRIPLE"
     * and "N3".  <code>null</code> represents the default language, "RDF/XML".
     * "RDF/XML-ABBREV" is a synonym for "RDF/XML".
     * <br />
	 *
	 * <p>See {@link Model} for a description of how to traverse a firewall.</p>
	 * @param url a string representation of the url to read from
	 * @param lang the language of the serialization
	 
	 * @return this model
	 */
	public Model read(String url, String lang) ;

	/** Using this method is often a mistake.
	 * Add RDF statements represented in language <code>lang</code> to the model.
	 *<br />
     *Predefined values for <code>lang</code> are "RDF/XML", "N-TRIPLE"
	 *and "N3".  <code>null</code> represents the default language, "RDF/XML".
	 *"RDF/XML-ABBREV" is a synonym for "RDF/XML".
     * <br />
	 * It is generally better to use an InputStream if possible. 
     * {@link Model#read(InputStream,String)}, otherwise there is a danger of a
     * mismatch between the character encoding of say the FileReader and the
     * character encoding of the data in the file.
	 * @return this model
	 * @param base the base uri to be used when converting relative
	 * URI's to absolute URI's.
	 
	  The base URI may be null if there are no relative URIs to 
	  convert.
	  A base URI of "" may permit relative URIs to be used in the
	   model.
	   
	 * @param lang the langauge of the serialization <code>null<code>
	 * selects the default
	 * @param reader the source of the input serialization
	 */
	public Model read(Reader reader, String base, String lang)
		;
	
    // output operations
    
    /** 
     * <p>Write the model as an XML document.
     * It is often better to use an OutputStream rather than a Writer, since this
     * will avoid character encoding errors.  
     * </p>
     * 
     * @param writer A writer to which the XML will be written
     * @return this model
     */
	public Model write( Writer writer ) ;
	
    /** 
     * <p>Write a serialized represention of a model in a specified language.
     * It is often better to use an OutputStream rather than a Writer, since this
     * will avoid character encoding errors.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value,
     * represented by <code>null</code> is "RDF/XML".</p>
     * @param writer The output writer
     * @param lang The output language
     * @return this model
     */
	public Model write( Writer writer, String lang ) ;

    /** 
     * <p>Write a serialized represention of a model in a specified language.
     * It is often better to use an OutputStream rather than a Writer,
     * since this will avoid character encoding errors.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param writer The output writer
     * @param base The base uri for relative URI calculations.
     * <code>null</code> means use only absolute URI's.
     * @param lang The language in which the RDF should be written
     * @return this model
     */
	public Model write( Writer writer, String lang, String base );
	

    /** 
     * <p>Write a serialization of this model as an XML document.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value is
     * represented by <code>null</code> is "RDF/XML".</p>
     * @param out The output stream to which the XML will be written
     * @return This model
     */
	public Model write(OutputStream out) ;

    /** 
     * <p>Write a serialized represention of this model in a specified language.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param out The output stream to which the RDF is written
     * @param lang The output langauge
     * @return This model
     */
	public Model write( OutputStream out, String lang ) ;

    /** 
     * <p>Write a serialized represention of a model in a specified language.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param out The output stream to which the RDF is written
     * @param base The base uri to use when writing relative URI's. <code>null</code>
     * means use only absolute URI's.
     * @param lang The language in which the RDF should be written
     * @return This model
     */
	public Model write( OutputStream out, String lang, String base );

	/** Removes a statement.
	 *
	 * <p> The statement with the same subject, predicate and object as
	 *     that supplied will be removed from the model.</p>
	 * @return this model
	 * @param s The statement to be removed.
	 
	 */
	Model remove(Statement s) ;

	/** Return a statement with given subject and property.
	 *  <p>If more than one statement witht the given subject and property
	 *  exists in the model, it is undefined which will be returned.</p>
	 * @return A statement from the model with the given subject and property.
	 * @param s The subject of the statement to be returned.
	 * @param p The property of the statement to be returned.
	 
	 */
	Statement getProperty(Resource s, Property p) ;

	/** List all subjects with a given property.
	 * @return an iterator over the subjects
	 * @param p the property sought.
	 
	 */
	ResIterator listSubjectsWithProperty(Property p) ;

	/** List all subjects with a given property and property value
	 * @return an iterator over the subjects
	 * @param p The predicate sought
	 * @param o The value sought
	 
	 */
	ResIterator listSubjectsWithProperty(Property p, RDFNode o)
		;

	/** List all objects in a model.
	 * @return an iterator over the objects
	 * @param p The predicate sought
	 
	 */
	NodeIterator listObjects() ;

	/** List all objects of a given property.
	 * @return an iterator over the objects
	 * @param p The predicate sought
	 
	 */
	NodeIterator listObjectsOfProperty(Property p) ;

	/** List the values of a property of a resource.
	 * @return an iterator over the objects
	 * @param p The predicate sought
	 
	 */
	NodeIterator listObjectsOfProperty(Resource s, Property p)
		;

	/** Determine whether this model contains any statements with a given subject
	 *  and property.
	 * @return true if there exists within this model a statement with
	 * subject s and property p, false otherwise
	 * @param s The subject sought.
	 * @param p The predicate sought.
	 
	 */
	boolean contains(Resource s, Property p) ;
    
    /**
        determine if the RDFNode r appears in any statement of this model.
        (containsRDFNode is a horrible name, and in any case, even literals
        will be resources one day)
        
        @param r the RDFNode to be searched for
        @return true iff r appears as some subject, predicate, or object
    */
    boolean containsResource( RDFNode r );

	/** Determine if a statement is present in this model.
	 * @return true if the statement with subject s, property p and object o
	 * is in the model, false otherwise
	 * @param s The subject of the statment tested.
	 * @param p The predicate of the statement tested.
	 * @param o The object of the statement tested.
	 
	 */
	boolean contains(Resource s, Property p, RDFNode o) ;

	/** Determine if a statement is present in this model.
	 * @param s The statement tested.
	 
	 * @return true if the statement s is in this model, false otherwise
	*/
	boolean contains(Statement s) ;

	/** Determine if any of the statements returned by an iterator are
	 * contained in this model.
	 * @param iter an iterator of the statements to be tested
	 
	 * @return true if any of the statements returns by iter are contained
	 *         in this model and false otherwise.
	*/
	boolean containsAny(StmtIterator iter) ;

	/** Determine if all of the statements returned by an iterator are
	 * contained in this model.
	 * @param iter an iterator of the statements to be tested
	 
	 * @return true if any of the statements returns by iter are contained
	 *         in this model and false otherwise.
	*/
	boolean containsAll(StmtIterator iter) ;

	// ModelPersonality getPersonality();
	/** Determine if any of the statements in a model are also contained
	 *  in this model.
	 * @param model the model containing the statements to be tested
	 
	 * @return true if any of the statements in model are also contained
	 *         in this model and false otherwise.
	*/
	boolean containsAny(Model model) ;

	/** Determine if all of the statements in a model are also contained
	 *  in this model.
	 * @param model the model containing the statements to be tested
	 
	 * @return true if all of the statements in model are also contained
	 *         in this model and false otherwise.
	*/
	boolean containsAll(Model model) ;

	/** 
        Determine if this Statement has been reified in this Model.
        
	   @param s The statement tested.
	   @return true iff a ReifiedStatement(s) has been created in this model
	*/
	boolean isReified( Statement s );

	/**
       Find or create a {@link ReifiedStatement} corresponding to a Statement.
        @param a Statement which may or may not already be reified
        @return a Resource [ReifiedStatement] that reifies the specified Statement.
	*/
	Resource getAnyReifiedStatement( Statement s );

	/**
        Remove all reifications (ie implicit reification quads) of _s_.
    */
	void removeAllReifications( Statement s );
    
    /**
        Remove a particular reificiation.
    */
    void removeReification( ReifiedStatement rs );
    
    /** List all statements.
     *
     *  <p>Subsequent operations on those statements may modify this model.</p>
     
     * @return an iterator over all statements in the model.
     */
    StmtIterator listStatements() ;

	/** List the statements matching a selector.
	 *
	 * <p>A statment is considered to match if the <CODE>test</CODE> method
	 * of s returns true when called on s.</p>
	 * @return an iterator over the matching statements
	 * @param s A selector object.
	 .
	 */
	StmtIterator listStatements(Selector s) ;
    /** Find all the statements matching a pattern.
     * <p>Return an iterator over all the statements in a model
     *  that match a pattern.  The statements selected are those
     *  whose subject matches the <code>subject</code> argument,
     *  whose predicate matches the <code>predicate</code> argument
     *  and whose object matches the <code>object</code> argument.
     *  If an argument is <code>null</code> it matches anything.</p>
     * @return an iterator over the subjects
     * @param subject   The subject sought
     * @param predicate The predicate sought
     * @param object    The value sought
     
     */ 
    
    StmtIterator listStatements( Resource s, Property p, RDFNode o );
    
    /**
        Answer a ReifiedStatement that encodes _s_ and belongs to this Model.
    <br>
        result.getModel() == this
    <br>
        result.getStatement() .equals ( s )
    */
    ReifiedStatement createReifiedStatement( Statement s );
    
    /**
        answer a ReifiedStatement that encodes _s_, belongs to this Model,
        and is a Resource with that _uri_.
    */
    ReifiedStatement createReifiedStatement( String uri, Statement s );
    
    /**
        answer an iterator delivering all the reified statements "in" this model
    */
    RSIterator listReifiedStatements();
    
    /**
        answer an iterator delivering all the reified statements "in" this model
        that match the statement _st_.
    */
    RSIterator listReifiedStatements( Statement st );

	/** Create a new model containing the statements matching a query.
	 *
	 * <p>A statment is considered to match if the <CODE>test</CODE> method
	 * of s returns true when called on s.</p>
	 * @return an iterator over the matching statements
	 * @param s A selector object.
	 .
	 */
	Model query(Selector s) ;

	/** Create a new model containing all the statements in this model 
	 * together with all of those in another given model.
	 * @return A new model containing all the statements that are in either model
	 * @param model The other model whose statements are to be included.
	 
	 */
	Model union(Model model) ;

	/** Create a new model containing all the statements which are in both
	 * this model and another.  As models are sets of statements, a statement
	 * contained in both models will only appear once in the resulting model.
	 * @return A new model containing all the statements that are in both models.
	 * @param model The other model.
	 
	 */
	Model intersection(Model model) ;

	/** Create a new model containing all the statements in this model which
	 * are not in another.
	 * @return a new model containing all the statements in this model that
	 *         are not in the given model.
	 * @param model the other model whose statements are to be excluded.
	 
	 */
	Model difference(Model model) ;

	/** Test whether one model is the equal to another.
     * Two Models  are equal iff the underlying graphs are identical Java
     * objects.
     * @param model the model to be compared
	 * @return true if the models are equal
	 */
	public boolean equals(Object model);

	/** Begin a new transation.
	 *
	 * <p> All changes made to a model within a transaction, will either
	 * be made, or none of them will be made.</p>
	 * @return this model to enable cascading.
	 
	 */
	Model begin() ;

	/** Abort the current transaction and abandon any changes in progress.
	 * @return this model to enable cascading.
	 
	 */
	Model abort() ;

	/** Commit the current transaction.
	 * @return this model to enable cascading.
	 
	 */
	Model commit() ;
    
    /**
        Execute the command <code>cmd</code> inside a transaction. If it
        completes, commit the transaction and return the result; if it fails
        (by throwing an exception), abort the transaction and throw an
        exception.
    */
    Object executeInTransaction( Command cmd );

	/** Determine whether this model is independent.
	 *
	 *  <p>For efficiency reasons, some implementations may create models which
	 *  which are dependent on others, i.e. a change in one model may cause
	 *  a change in another.  If this is the case this method will return false,
	 *  otherwise it will return true.</p>
	 *
	 * @return  true if this model is indepdent of others
	 */
	boolean independent();

	/** Determine whether this model supports transactions.
	 * @return  true if this model supports transactions.
	 */
	boolean supportsTransactions();

	/** Determine whether this model supports set operations.
	 * @return true if this model supports set operations.
	 */
	boolean supportsSetOperations();
	/**
	 * Compare this Model with another for equality ignoring the labels on
     * bNodes.
     * See
	 * <a href="http://www.w3.org/TR/rdf-concepts#section-Graph-syntax">RDF
	 * Concepts</a>.
	 * <p>Two models are isomorphic when each statement in one can be matched
	 * with a statement in the other.  Statements which are identical match.</p>
	 *
	 * <p>Special treatment is given to anonymous nodes.  A binding is a one to
	 * one mapping which maps each anonymous node in <code>this</code> model to
	 * an anonymous node in <code>model</code>.  Two statements s1 and s2 match
	 * under a binding if if s1.subject is anonymous and s2.subject is anonymous
	 * and the binding maps s1.subject to s2.subject.</p>
	 *
	 * <p>Two models are isomorphic if there is a binding that allows all the
	 * statements in one model to match a a statement in the other.</p>
	 * @param g Compare against this.
	 * @return boolean True if the two RDF graphs are isomorphic.
	 */
	boolean isIsomorphicWith(Model g);

	/** Close the Model and free up resources held.
	 *
	 *  <p>Not all implementations of Model require this method to be called.  But
	 *     some do, so in general its best to call it when done with the object,
	 *     rather than leave it to the finalizer.</p>
	 */
	public void close();
    
    /** Get the model lock for this model.
     *  See also the convenience operations enterCriticalSection and leaveCriticalSection.
     *  
     * @see ModelLock
     * @return The ModelLock object associated with this model 
     */
    public ModelLock getModelLock() ;
    
}

/*
 *  (c)   Copyright Hewlett-Packard Company 2000, 2001, 2002, 2003
 *   All rights reserved.
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
 * $Id: Model.java,v 1.26 2003-06-25 14:20:10 jeremy_carroll Exp $
 */