/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLCommonImpl.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-05-21 15:33:14 $
 *               by   $Author: chris-dollin $
 *
 * (c) Copyright Hewlett-Packard Company 2001
 * All rights reserved.
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
 *
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
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.rdf.model.impl.NodeIteratorImpl;

import java.util.*;

import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.util.iterator.ConcatenatedIterator;

import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLCommon;
import com.hp.hpl.jena.ontology.daml.LiteralAccessor;
import com.hp.hpl.jena.ontology.daml.PropertyAccessor;
import com.hp.hpl.jena.ontology.daml.PropertyIterator;
import com.hp.hpl.jena.ontology.daml.DAMLClass;
import com.hp.hpl.jena.ontology.daml.DAMLProperty;
import com.hp.hpl.jena.ontology.daml.DAMLObjectProperty;

import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.RDF;

import com.hp.hpl.jena.shared.*;

/**
 * Abstract super-class for all DAML resources (including properties).  Defines shared
 * implementations and common services, such as property manipulation, vocabulary
 * management and <code>rdf:type</code> management.  Also defines accessors for common
 * properties, such as comment, label, and equivalentTo.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLCommonImpl.java,v 1.4 2003-05-21 15:33:14 chris-dollin Exp $
 */
public abstract class DAMLCommonImpl
    extends ResourceImpl
    implements DAMLCommon
{
    // Constants
    //////////////////////////////////



    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** Literal accessor for label property */
    private LiteralAccessor m_propLabel = new LiteralAccessorImpl( getVocabulary().label(), this );

    /** Literal accessor for comment property */
    private LiteralAccessor m_propComment = new LiteralAccessorImpl( getVocabulary().comment(), this );

    /** Property accessor for equivalentTo */
    private PropertyAccessor m_propEquivalentTo = null;

    /** The vocabulary that corresponds to the namespace this DAML value was declared in */
    private DAMLVocabulary m_vocabulary = null;

    /** Property accessor for RDF:type */
    private PropertyAccessor m_propType = null;



    // Constructors
    //////////////////////////////////


    /**
     * Constructor, takes the URI this value, and the underlying
     * model it will be attached to.
     *
     * @param uri The URI of the DAML value, or null for an anonymous node.
     * @param model Reference to the DAML model that will contain statements about this DAML value.
     * @param vocabulary The vocabulary for this value (defines the namespace).  Can be null, in which
     *                   case the vocabulary defaults to the most recent.
     */
    public DAMLCommonImpl( String uri, DAMLModel model, DAMLVocabulary vocabulary )
    {
        super( uri, model );
     //   this.model = model;
        m_vocabulary = vocabulary;
    }


    /**
     * Constructor, takes the name and namespace for this value, and the underlying
     * model it will be attached to.
     *
     * @param namespace The namespace the class inhabits, or null
     * @param name The name of the DAML value
     * @param model Reference to the DAML model that will contain statements about this DAML class.
     * @param vocabulary The vocabulary for this value (defines the namespace).  Can be null, in which
     *                   case the vocabulary defaults to the most recent.
     */
    public DAMLCommonImpl( String namespace, String name, DAMLModel model, DAMLVocabulary vocabulary )
    {
        this( namespace + name, model, vocabulary );
    }



    // External signature methods
    //////////////////////////////////

    /**
     * Answer the underlying model
     *
     * @return A DAML model
     */
    public DAMLModel getDAMLModel() {
        return (DAMLModel) getModel();
    }


    /**
     * Set the RDF type property for this node in the underlying model, replacing any
     * existing type property.  To add a second or subsequent type statement to a resource,
     * use {@link #setRDFType( com.hp.hpl.jena.rdf.model.Resource, boolean )
     * setRDFType( Resource, false ) }.
     *
     * @param rdfClass The RDF resource denoting the new value for the rdf:type property,
     *                 which will replace any existing type property.
     */
    public void setRDFType( Resource rdfClass ) {
        setRDFType( rdfClass, true );
    }


    /**
     * Add an RDF type property for this node in the underlying model. If the replace flag
     * is true, this type will replace any current type property for the node. Otherwise,
     * the type will be in addition to any existing type property.  Note that for most normal
     * uses, a DAML resource should have at most one rdf:type property.  One exception to this,
     * in the March 2001 release, is when DatatypeProperties are marked as unique, unambiguous
     * or transitive.  This is achieved by the use of two rdf type properties.
     *
     * @param rdfClass The RDF resource denoting the class that will be new value for the rdf:type property.
     * @param replace  If true, the given class will replace any existing type property for this
     *                 value, otherwise it will be added as an extra type statement.
     */
    public void setRDFType( Resource rdfClass, boolean replace ) {
        // check first that we have a model - classes in the vocabulary don't have a model
        // but still invoke setRDFType when constructing
        if (getModel() != null) {
            if (replace) {
                replaceProperty( RDF.type, rdfClass );
            }
            else {
                try {
                    addProperty( RDF.type, rdfClass );
                }
                catch (JenaException e) {
                    Log.severe( "RDF exception " + e, e );
                    throw new RuntimeException( "RDF Exception " + e );
                }
            }
        }
    }


    /**
     * Answer true if this DAML value is a member of the class denoted by the given URI.
     *
     * @param classURI String denoting the URI of the class to test against
     * @return true if it can be shown that this DAML value is a member of the class, via
     *         <code>rdf:type</code>.
     */
    public boolean hasRDFType( String classURI ) {
        // do we know a daml class that matches this uri?
        DAMLCommon damlVal = ((DAMLModel) getModel()).getDAMLValue( classURI );

        if (damlVal != null  &&  damlVal instanceof com.hp.hpl.jena.ontology.daml.DAMLClass) {
            // we know a class that matches the URI, so test against it
            return hasRDFType( (DAMLClass) damlVal );
        }
        else {
            // last resort, make a temporary resource to hold the uri
           return hasRDFType( new ResourceImpl( classURI, getModel() ) );
        }
    }


    /**
     * Answer true if this DAML value is a member of the class denoted by the
     * given DAML class object.  This will traverse the class hierarchy, until
     * every class and super-class for this
     * DAML value has been examined.  Depending on the depth of the hierarchy,
     * this may be an expensive operation. Cycles are detected, however, so it
     * is guaranteed to terminate.
     *
     * @param damlClass Denotes a class to which this value may belong
     * @return true if the value is a member of the class (or one of its sub-classes)
     *         via <code>rdf:type</code>.
     */
    public boolean hasRDFType( Resource damlClass ) {
        // see if the given class is in the iteration of this object's types
        boolean found = false;
        String damlClassURI = damlClass.getURI();

        for (Iterator i = getRDFTypes( true );  !found  &&  i.hasNext();  ) {
            Resource c = (Resource) i.next();

            // this class could be equal to the target class, or could be a sub-class of it under the
            // built-in knowledge of the DAML hierarchy
            found = c.equals( damlClass ) ||
                    DAMLHierarchy.getInstance().isDAMLSubClassOf( c.getURI(), damlClassURI );
        }

        return found;
    }


    /**
     * Answer an iterator over all of the types to which this class belongs. Optionally,
     * generate a closure by considering the closure of the set of classes over the
     * class hierarchy (e.g. if 'fido' is the resource, the non-closed set of fido's
     * classes might be
     * <code>{Dog, Vaccinated}</code>,
     * i.e. the set of classes for which rdf:type statements exist for fido,
     * while the closed set might be
     * <code>{Dog, Vaccinated, Mammal, Pet, Vertebrate, Thing, MedicallyCertified}</code>
     *
     * @param closed If true, generate the closed set by considering the super-classes of
     *               the known classes of this value.
     * @return an iterator over the set of this value's classes (note: it is a set, so
     *         each class will only appear once)
     */
    public Iterator getRDFTypes( boolean closed ) {
        HashSet types = new HashSet();

        // first we get all of the values for rdf:type or daml:type
        for (Iterator i = prop_type().getAll( false );  i.hasNext(); ) {
            // add each type to the type set
            Resource r = (Resource) i.next();
            types.add( r );

            // for the closed set of types, we also add the super-types of each type
            if (closed && r instanceof DAMLClass) {
                for (Iterator j = ((DAMLClass) r).getSuperClasses();  j.hasNext(); ) {
                    types.add( j.next() );
                }
            }
        }

        // check in case we have a default type to add
        if (closed && getDefaultType() != null) {
            types.add( getDefaultType() );
        }

        return types.iterator();
    }


    /**
     * Answer the value of a given RDF property for this DAML value, or null
     * if it doesn't have one.  The value is returned as an RDFNode, from which
     * the value can be extracted for literals.  If there is more than one RDF
     * statement with the given property for the current value, it is not defined
     * which of the values will be returned.
     *
     * @param property An RDF property
     * @return An RDFNode whose value is the value, or one of the values, of the
     *         given property. If the property is not defined, or an error occurs,
     *         returns null.
     */
    public RDFNode getPropertyValue( Property property ) {
        try {
            return getProperty( property ).getObject();
        }
        catch (JenaException e) {
            Log.severe( "RDF exception while getting property " + property + " value was: " + e, e );
            throw new RuntimeException( "RDF error when getting values for property " + property + ": " + e );
        }
    }


    /**
     * Answer an iterator over the set of all values for a given RDF property. Each
     * value in the iterator will be an RDFNode, representing the value (object) of
     * each statement in the underlying model.
     *
     * @param property The property whose values are sought
     * @return An Iterator over the values of the property, each of which will be an
     *         {@link com.hp.hpl.jena.rdf.model.RDFNode RDFNode}.
     */
    public NodeIterator getPropertyValues( Property property ) {
        return new NodeIteratorImpl( new PropertyIterator( this, property, null, false, false ), null );
    }


    /**
     * Set the value of the given property of this DAML value to the given
     * value, encoded as an RDFNode.  Maintains the invariant that there is
     * at most one value of the property for a given DAML object, so existing
     * property values are first removed.  To add multiple properties to a
     * given DAML object, use
     * {@link com.hp.hpl.jena.rdf.model.Resource#addProperty( com.hp.hpl.jena.rdf.model.Property, com.hp.hpl.jena.rdf.model.RDFNode ) addProperty}.
     *
     * @param property The property to update
     * @param value The new value of the property as an RDFNode, or null to
     *              effectively remove this property.
     */
    public void setPropertyValue( Property property, RDFNode value ) {
        if (value == null) {
            removeAll( property );
        }
        else {
            replaceProperty( property, value );
        }
    }


    /**
     * Remove the specific RDF property-value pair from this DAML resource.
     *
     * @param property The property to be removed
     * @param value The specific value of the property to be removed
     */
    public void removeProperty( Property property, RDFNode value ) {
        removeProperties( getEquivalenceClass( property ), getEquivalenceClass( value ) );
    }


    /**
     * Remove all the values for a given property on this DAML resource.
     *
     * @param prop The RDF resource that defines the property to be removed
     */
    public void removeAll( Property prop ) {
        removeAll( getEquivalenceClass( prop ) );
    }


    /**
     * Replace the value of the named property with the given value.  All existing
     * values, if any, for the property are first removed.
     *
     * @param prop The RDF property to be updated
     * @param value The new value.
     */
    public void replaceProperty( Property prop, RDFNode value ) {
        try {
            // if there is an existing property, remove it
            removeAll( prop );

            // now set the new value
            addProperty( prop, value );
        }
        catch (JenaException e) {
            Log.severe( "RDF exception while replacing value of DAML property: " + e, e );
            throw new RuntimeException( "RDF exception while replacing value of DAML property: " + e );
        }
    }


    /**
     * Answer the number of values a given property has with this value as subject.
     *
     * @param property The property to be tested
     * @return The number of statements with this value as subject and the given
     *         property as relation.
     */
    public int getNumPropertyValues( Property property ) {
        try {
            int count = 0;

            // lookup the super-classes from this model
            for (NodeIterator i = getPropertyValues( property );  i.hasNext();  i.nextNode()) {
                count++;
            }

            return count;
        }
        catch (JenaException e) {
            Log.severe( "Exception while listing values: " + e, e );
            throw new RuntimeException( "RDF failure while listing values: " + e );
        }
    }


    /**
     * Answer an iterator over a set of resources that are the objects of statements
     * with subject this DAML object and predicate the given property. Respects DAML
     * semantics of equivalence, transitivity and the property hierarchy.
     *
     * @param property The property whose values are sought
     * @param closed If true, and the given property is transitive, generate the
     *               closure over the given property from this value.
     * @return An iterator of resources that are the objects of statements whose
     *         subject is this value (or one of its equivalents) and whose predicate
     *         is <code>property</code> or one of its equivalents
     */
    public Iterator getAll( Property property, boolean closed ) {
        // get an iterator over all of the values of this property, including equivalent values
        PropertyIterator iter = new PropertyIterator( getEquivalentValues(), property,
                                                      getPropertyInverse( property ), (closed && isTransitive( property )), false );
        return iter;
    }


    /**
     * Answer the DAML+OIL vocabulary that corresponds to the namespace that this value
     * was declared in.
     *
     * @return a vocabulary object
     */
    public DAMLVocabulary getVocabulary() {
        if (m_vocabulary == null) {
            // need to establish the vocabulary for this object
            m_vocabulary = VocabularyManager.getVocabulary( this );
        }

        return m_vocabulary;
    }


    /**
     * Answer a key that can be used to index collections of this DAML value for
     * easy access by iterators.  Package access only.
     *
     * @return a key object.
     */
    abstract Object getKey();


    /**
     * Answer an iterator over all of the DAML objects that are equivalent to this
     * value under the <code>daml:equivalentTo</code> relation.  The common method
     * just tests this relation, <code>getEquivalentValues()</code> in sub-classes,
     * such as {@link com.hp.hpl.jena.ontology.daml.DAMLClass}, may extend this with specific
     * additional semantics such as <code>daml:sameClassAs</code>.
     *
     * @return an iterator ranging over every equivalent DAML value - each value of
     *         the iteration will be a damlCommon object.
     */
    public Iterator getEquivalentValues() {
        // equivalentTo is transitive, and is its own inverse
        // we also need to exploit background knowledge about the equivalence of DAML relations
        return new ConcatenatedIterator(
                       new PropertyIterator( this, getVocabulary().equivalentTo(), getVocabulary().equivalentTo(), true, true, false ),
                       DAMLHierarchy.getInstance().getEquivalentValues( this ) );
    }


    /**
     * Answer the set of equivalent values to this value, but not including the
     * value itself.  The iterator will range over a set: each element occurs only
     * once.
     *
     * @return An iteration ranging over the set of values that are equivalent to this
     *         value, but not itself.
     */
    public Iterator getEquivalenceSet() {
        HashSet s = new HashSet();

        // eliminate duplicates from the equivalent values iteration
        for (Iterator i = getEquivalentValues();  i.hasNext();  ) {
            Object o = i.next();

            if (!equals( o )) {
                s.add( o );
            }
        }

        return s.iterator();
    }


    /**
     * Answer an iterator that contains exactly this value.  This is useful as
     * we often use Iterators to stand for sets or collections, and two such iterators
     * can be appended to form the union of the collections.
     *
     * @return an iterator whose sole value is this DAML value
     */
    public Iterator getSelfIterator() {
        LinkedList l = new LinkedList();
        l.add( this );
        return l.iterator();
    }


    /**
     * Return a readable representation of the DAML value
     *
     * @return a string denoting this value
     */
    public String toString() {
        // get the public name for this value type (e.g. we change DAMLClassImpl -> DAMLClass)
        String cName = getClass().getName();
        int i = cName.indexOf( "Impl" );
        int j = cName.lastIndexOf( "." ) + 1;
        cName = (i > 0) ? cName.substring( j, i ) : cName.substring( j );

        // now format the return string
        return (isAnon()) ?
                   ("<Anonymous " + getIDTranslation( getId() ) + " " + cName + "@" + Integer.toHexString( hashCode() ) + ">") :
                   ("<" + cName + " " + getURI() + ">");
    }


    /**
     * Remove the DAML object from the model.  All of the RDF statements with this
     * DAML value as its subject will be removed from the model, and this object will
     * be removed from the indexes.  It will be the responsibility of client code to
     * ensure that references to this object are removed so that the object itself
     * can be garbage collected.
     */
    public void remove() {
        try {
            // first remove all of the statements corresponding to this object
            for (StmtIterator i = listProperties();  i.hasNext();  i.nextStatement().remove() );

            // now remove this object from the DAML indexes
            ((DAMLModelImpl) getModel()).unindex( this );
        }
        catch (JenaException e) {
            Log.severe( "RDF exception while removing object from model: " + e, e );
        }
    }



    // Properties
    /////////////

    /**
     * Accessor for the property of the label on the value, whose value
     * is a literal (string).
     *
     * @return Literal accessor for the label property
     */
    public LiteralAccessor prop_label() {
        return m_propLabel;
    }


    /**
     * Accessor for the property of the comment on the value, whose value
     * is a literal (string).
     *
     * @return Literal accessor for the comment property
     */
    public LiteralAccessor prop_comment() {
        return m_propComment;
    }


    /**
     * Property accessor for the 'equivalentTo' property of a DAML value. This
     * denotes that two terms have the same meaning. The spec helpfully
     * says: <i>for equivalentTo(X, Y), read X is an equivalent term to Y</i>.
     *
     * @return Property accessor for 'equivalentTo'.
     */
    public PropertyAccessor prop_equivalentTo() {
        if (m_propEquivalentTo == null) {
            m_propEquivalentTo = new PropertyAccessorImpl( getVocabulary().equivalentTo(), this );
        }

        return m_propEquivalentTo;
    }



    /**
     * Property accessor for the 'rdf:type' property of a DAML value.
     *
     * @return Property accessor for 'rdf:type'.
     */
    public PropertyAccessor prop_type() {
        if (m_propType == null) {
            m_propType = new PropertyAccessorImpl( RDF.type, this );
        }

        return m_propType;
    }



    // Internal implementation methods
    //////////////////////////////////


    /**
     * For debugging, a more concise rendering of the UID
     */
    protected static java.util.HashMap s_UIDs = new java.util.HashMap();
    protected static int s_count = 0;
    protected static String getIDTranslation( com.hp.hpl.jena.rdf.model.AnonId uid ) {
        Integer id = (Integer) s_UIDs.get( uid );
        if (id == null) {
            id = new Integer( s_count++ );
            s_UIDs.put( uid, id );
        }
        return id.toString();
    }


    /**
     * Answer the equivalence class, as an iterator, for a given resource
     *
     * @param n The resource
     * @return eClass An iteration of the resource and its equivalents
     */
    protected Iterator getEquivalenceClass( RDFNode n ) {
        if (n instanceof DAMLCommon) {
            // if it's a DAML object, we can calculate the equivalence classs
            return ((DAMLCommon) n).getEquivalentValues();
        }
        else {
            // otherwise return the singleton set
            List single = new ArrayList();
            single.add( n );
            return single.iterator();
        }
    }


    /**
     * Remove the properties defined by the given equivalence classes, if they exist.
     * Specifically, remove every statement for which the the subject is this DAML value,
     * the predicate is one of the given properties, and the value is one of the given values.
     *
     * @param preds An iterator over a set of properties
     * @param values A set of values to match against
     */
    protected void removeProperties( Iterator preds, Iterator values ) {
        // we have to clone one of the sets in order to do the cross-product
        List predSet = new ArrayList();
        while (preds.hasNext()) {
            predSet.add( preds.next() );
        }

        // now, go through every element of the cross-product, and remove that statement
        while (values.hasNext()) {
            RDFNode n = (RDFNode) values.next();

            for (Iterator i = predSet.iterator();  i.hasNext(); ) {
                Property p = (Property) i.next();

                try {
                    for (StmtIterator j = getModel().listStatements( this, p, n );  j.hasNext(); ) {
                        j.nextStatement().remove();
                    }
                }
                catch (JenaException e) {
                    Log.severe( "Possible RDF error when zapping from model: " + e, e );
                }
            }
        }
    }


    /**
     * Remove all statements whose predicate is in the given iteration, and whose
     * subject is this resource.
     *
     * @param preds An iterator over a set of properties.
     */
    protected void removeAll( Iterator preds ) {
        try {
            while (preds.hasNext()) {
                Property p = (Property) preds.next();

                for (StmtIterator i = getModel().listStatements( this, p, (RDFNode) null );  i.hasNext(); ) {
                    i.nextStatement().remove();
                }
            }
        }
        catch (JenaException e) {
            Log.severe( "Possible RDF error when zapping from model: " + e, e );
        }
    }


    /**
     * Answer the inverse of a property, if we know what it is
     *
     * @param property An RDF or DAML property
     * @return The inverse property of property, or null
     */
    protected Property getPropertyInverse( Property property ) {
        // can only check the inverses of DAML object properties
        if (property instanceof DAMLObjectProperty) {
            // Note: we don't use the prop_inverseOf property accessor here, since that
            // would cause infinite recursion, as the property accessor needs to know what
            // the property inverse is

            DAMLProperty dProperty = (DAMLProperty) property;

            // lookup the inverse of this property, if it has one
            try {
                Statement inv = dProperty.getProperty( dProperty.getVocabulary().inverseOf() );
                return (Property) inv.getObject();
            }
            catch (JenaException e) {
                // can ignore - just means not present
            }
        }

        return null;
    }


    /**
     * Answer true if the given property is transitive
     *
     * @param property A property definition
     * @return True if the property is defined to be transitive in the ontology,
     *         or is known to be a transitive DAML property.
     */
    protected boolean isTransitive( Property property ) {
        return DAMLHierarchy.getInstance().isTransitiveProperty( property )  ||
               ((property instanceof DAMLObjectProperty)  &&
                ((DAMLObjectProperty) property).isTransitive());
    }


    /**
     * Answer a value that will be a default type to include in an iteration of
     * the value's rdf types.  Typically there is no default (null), but for an
     * instance we want to ensure that the default type is daml:Thing.
     *
     * @return The default type or null
     */
    protected Resource getDefaultType() {
        return null;
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================



}
