/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: OntProperty.java,v $
 * Revision           $Revision: 1.13 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-08-12 15:19:44 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
 * footer for full conditions)
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;



/**
 * <p>
 * Interface encapsulating a property in an ontology.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntProperty.java,v 1.13 2004-08-12 15:19:44 ian_dickinson Exp $
 */
public interface OntProperty
    extends OntResource, Property
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    // subPropertyOf
    
    /**
     * <p>Assert that this property is sub-property of the given property. Any existing 
     * statements for <code>subPropertyOf</code> will be removed.</p>
     * @param prop The property that this property is a sub-property of
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public void setSuperProperty( Property prop );

    /**
     * <p>Add a super-property of this property.</p>
     * @param prop A property that is a super-property of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public void addSuperProperty( Property prop );

    /**
     * <p>Answer a property that is the super-property of this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A super-property of this property
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public OntProperty getSuperProperty();

    /**
     * <p>Answer an iterator over all of the properties that are declared to be super-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the super-properties of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listSuperProperties();

    /**
     * <p>Answer an iterator over all of the properties that are declared to be super-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @param direct If true, only answer the direcly adjacent properties in the
     * property hierarchy: i&#046;e&#046; eliminate any property for which there is a longer route
     * to reach that child under the super-property relation.
     * @return An iterator over the super-properties of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listSuperProperties( boolean direct );

    /**
     * <p>Answer true if the given property is a super-property of this property.</p>
     * @param prop A property to test.
     * @param direct If true, only consider the direcly adjacent properties in the
     * property hierarchy
     * @return True if the given property is a super-property of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */
    public boolean hasSuperProperty( Property prop, boolean direct );
    
    /**
     * <p>Remove the given property from the super-properties of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property to be removed from the super-properties of this property
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */
    public void removeSuperProperty( Property prop );
    
    /**
     * <p>Assert that this property is super-property of the given property. Any existing 
     * statements for <code>subPropertyOf</code> on <code>prop</code> will be removed.</p>
     * @param prop The property that is a sub-property of this property
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public void setSubProperty( Property prop );

    /**
     * <p>Add a sub-property of this property.</p>
     * @param prop A property that is a sub-property of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public void addSubProperty( Property prop );

    /**
     * <p>Answer a property that is the sub-property of this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A sub-property of this property
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public OntProperty getSubProperty();

    /**
     * <p>Answer an iterator over all of the properties that are declared to be sub-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the sub-properties of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listSubProperties();

    /**
     * <p>Answer an iterator over all of the properties that are declared to be sub-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @param direct If true, only answer the direcly adjacent properties in the
     * property hierarchy: i&#046;e&#046; eliminate any property for which there is a longer route
     * to reach that child under the sub-property relation.
     * @return An iterator over the sub-properties of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listSubProperties( boolean direct );

    /**
     * <p>Answer true if the given property is a sub-property of this property.</p>
     * @param prop A property to test.
     * @param direct If true, only consider the direcly adjacent properties in the
     * property hierarchy
     * @return True if the given property is a sub-property of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */
    public boolean hasSubProperty( Property prop, boolean direct );
    
    /**
     * <p>Remove the given property from the sub-properties of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property to be removed from the sub-properties of this property
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */
    public void removeSubProperty( Property prop );
    
    // domain
    
    /**
     * <p>Assert that the given resource represents the class of individuals that form the 
     * domain of this property. Any existing <code>domain</code> statements for this property are removed.</p>
     * @param res The resource that represents the domain class for this property.
     * @exception OntProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.   
     */ 
    public void setDomain( Resource res );

    /**
     * <p>Add a resource representing the domain of this property.</p>
     * @param res A resource that represents a domain class for this property. 
     * @exception OntProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.   
     */ 
    public void addDomain( Resource res );

    /**
     * <p>Answer a resource that represents the domain class of this property. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An resource representing the class that forms the domain of this property
     * @exception OntProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.   
     */ 
    public OntResource getDomain();

    /**
     * <p>Answer an iterator over all of the declared domain classes of this property.
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the classes that form the domain of this property.
     * @exception OntProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listDomain();

    /**
     * <p>Answer true if the given resource a class specifying the domain of this property.</p>
     * @param res A resource representing a class
     * @return True if the given resource is one of the domain classes of this property.
     * @exception OntProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.   
     */
    public boolean hasDomain( Resource res );
    
    /**
     * <p>Remove the given class from the stated domain(s) of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class to be removed from the declared domain(s) of this property
     * @exception OntProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.   
     */
    public void removeDomain( Resource cls );
    

    // range
    
    /**
     * <p>Assert that the given resource represents the class of individuals that form the 
     * range of this property. Any existing <code>range</code> statements for this property are removed.</p>
     * @param res The resource that represents the range class for this property.
     * @exception OntProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.   
     */ 
    public void setRange( Resource res );

    /**
     * <p>Add a resource representing the range of this property.</p>
     * @param res A resource that represents a range class for this property. 
     * @exception OntProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.   
     */ 
    public void addRange( Resource res );

    /**
     * <p>Answer a resource that represents the range class of this property. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An resource representing the class that forms the range of this property
     * @exception OntProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.   
     */ 
    public OntResource getRange();

    /**
     * <p>Answer an iterator over all of the declared range classes of this property.
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the classes that form the range of this property.
     * @exception OntProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listRange();

    /**
     * <p>Answer true if the given resource a class specifying the range of this property.</p>
     * @param res A resource representing a class
     * @return True if the given resource is one of the range classes of this property.
     * @exception OntProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.   
     */
    public boolean hasRange( Resource res );
    
    /**
     * <p>Remove the given class from the stated range(s) of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class to be removed from the declared range(s) of this property
     * @exception OntProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.   
     */
    public void removeRange( Resource cls );
    

    // relationships between properties

    // equivalentProperty
    
    /**
     * <p>Assert that the given property is equivalent to this property. Any existing 
     * statements for <code>equivalentProperty</code> will be removed.</p>
     * @param prop The property that this property is a equivalent to.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.   
     */ 
    public void setEquivalentProperty( Property prop );

    /**
     * <p>Add a property that is equivalent to this property.</p>
     * @param prop A property that is equivalent to this property.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.   
     */ 
    public void addEquivalentProperty( Property prop );

    /**
     * <p>Answer a property that is equivalent to this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A property equivalent to this property
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.   
     */ 
    public OntProperty getEquivalentProperty();

    /**
     * <p>Answer an iterator over all of the properties that are declared to be equivalent properties to
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the properties equivalent to this property.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listEquivalentProperties();

    /**
     * <p>Answer true if the given property is equivalent to this property.</p>
     * @param prop A property to test for
     * @return True if the given property is equivalent to this property.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.   
     */
    public boolean hasEquivalentProperty( Property prop );
    
    /**
     * <p>Remove the statement that this property and the given property are
     * equivalent.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property that may be declared to be equivalent to this property 
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.   
     */
    public void removeEquivalentProperty( Property prop );
    

    // inverseProperty
    
    /**
     * <p>Assert that this property is the inverse of the given property. Any existing 
     * statements for <code>inverseOf</code> will be removed.</p>
     * @param prop The property that this property is a inverse to.
     * @exception OntProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.   
     */ 
    public void setInverseOf( Property prop );

    /**
     * <p>Add a property that this property is the inverse of.</p>
     * @param prop A property that is the inverse of this property.
     * @exception OntProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.   
     */ 
    public void addInverseOf( Property prop );

    /**
     * <p>Answer a property of which this property is the inverse, if known,
     * or null if there is no such property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * <p>Note that this method is slightly different to {@link #getInverse}. See
     * the Javadoc on {@link #getInverse} for a detailed explanation.</p>
     * @return A property which this property is the inverse of, or null.
     * @exception OntProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.   
     */ 
    public OntProperty getInverseOf();

    /**
     * <p>Answer an iterator over all of the properties that this property is declared to be the inverse of.
     * Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the properties inverse to this property.
     * @exception OntProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listInverseOf();

    /**
     * <p>Answer true if this property is the inverse of the given property.</p>
     * @param prop A property to test for
     * @return True if the this property is the inverse of the the given property.
     * @exception OntProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.   
     */
    public boolean isInverseOf( Property prop );
    
    /**
     * <p>Remove the statement that this property is the inverse of the given property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property that may be declared to be inverse to this property 
     * @exception OntProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.   
     */
    public void removeInverseProperty( Property prop );
    

    // view conversion functions
    
    /** 
     * <p>Answer a view of this property as a functional property</p>
     * @return This property, but viewed as a FunctionalProperty node
     * @exception ConversionException if the resource cannot be converted to a functional property
     * given the lanuage profile and the current state of the underlying model.
     */
    public FunctionalProperty asFunctionalProperty();

    /** 
     * <p>Answer a view of this property as a datatype property</p>
     * @return This property, but viewed as a DatatypeProperty node
     * @exception ConversionException if the resource cannot be converted to a datatype property
     * given the lanuage profile and the current state of the underlying model.
     */
    public DatatypeProperty asDatatypeProperty();

    /** 
     * <p>Answer a view of this property as an object property</p>
     * @return This property, but viewed as an ObjectProperty node
     * @exception ConversionException if the resource cannot be converted to an object property
     * given the lanuage profile and the current state of the underlying model.
     */
    public ObjectProperty asObjectProperty();
    
    /** 
     * <p>Answer a view of this property as a transitive property</p>
     * @return This property, but viewed as a TransitiveProperty node
     * @exception ConversionException if the resource cannot be converted to a transitive property
     * given the lanuage profile and the current state of the underlying model.
     */
    public TransitiveProperty asTransitiveProperty();
    
    /** 
     * <p>Answer a view of this property as an inverse functional property</p>
     * @return This property, but viewed as an InverseFunctionalProperty node
     * @exception ConversionException if the resource cannot be converted to an inverse functional property
     * given the lanuage profile and the current state of the underlying model.
     */
    public InverseFunctionalProperty asInverseFunctionalProperty();
    
    /** 
     * <p>Answer a view of this property as a symmetric property</p>
     * @return This property, but viewed as a SymmetricProperty node
     * @exception ConversionException if the resource cannot be converted to a symmetric property
     * given the lanuage profile and the current state of the underlying model.
     */
    public SymmetricProperty asSymmetricProperty();
    
    // conversion functions
    
    /** 
     * <p>Answer a facet of this property as a functional property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a FunctionalProperty facet
     */
    public FunctionalProperty convertToFunctionalProperty();

    /** 
     * <p>Answer a facet of this property as a datatype property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a DatatypeProperty facet
     */
    public DatatypeProperty convertToDatatypeProperty();

    /** 
     * <p>Answer a facet of this property as an object property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to an ObjectProperty facet
     */
    public ObjectProperty convertToObjectProperty();
    
    /** 
     * <p>Answer a facet of this property as a transitive property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a TransitiveProperty facet
     */
    public TransitiveProperty convertToTransitiveProperty();
    
    /** 
     * <p>Answer a facet of this property as an inverse functional property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to an InverseFunctionalProperty facet
     */
    public InverseFunctionalProperty convertToInverseFunctionalProperty();
    
    /** 
     * <p>Answer a facet of this property as a symmetric property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a SymmetricProperty facet
     */
    public SymmetricProperty convertToSymmetricProperty();
    
    
    // tests on property sub-types
    
    /** 
     * <p>Answer true if this property is a functional property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a functional property.
     */
    public boolean isFunctionalProperty();

    /** 
     * <p>Answer true if this property is a datatype property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a datatype property.
     */
    public boolean isDatatypeProperty();

    /** 
     * <p>Answer true if this property is an object property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as an object property.
     */
    public boolean isObjectProperty();
    
    /** 
     * <p>Answer true if this property is a transitive property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a transitive property.
     */
    public boolean isTransitiveProperty();
    
    /** 
     * <p>Answer true if this property is an inverse functional property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as an inverse functional property.
     */
    public boolean isInverseFunctionalProperty();
    
    /** 
     * <p>Answer true if this property is a symmetric property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a symmetric property.
     */
    public boolean isSymmetricProperty();

    /**
     * <p>Answer the property that has declared itself to be the inverse of this property,
     * if any such property is defined.  
     * If no such property is defined,
     * return null.  If more than one inverse is defined, return an abritrary selection.</p>
     * <p>Note that this method is slightly different from {@link #getInverseOf}. Suppose that
     * we have:</p>
     * <pre>
     * :p an owl:ObjectProperty.
     * :q an owl:ObjectProperty.
     * :p owl:inverseOf :q.
     * </pre> 
     * <p>Then, <code>getInverse()</code> called on the <code>OntProperty</code>
     * corresponding to <code>q</code> will return <code>p</code>, since <code>q</code>
     * declares itself to be <code>p</code>'s inverse.  However, <em>unless an OWL
     * reasoner is used</em>, calling <code>getInverse()</code> on <code>p</code>
     * will return <code>null</code>. Conversely, absent a reasoner,  
     * <code>{@link #getInverseOf}</code> on <code>p</code> will return <code>q</code>,
     * but will return null when called on <code>q</code>.  In the presence of an OWL
     * reasoner, <code>getInverse()</code> and <code>getInverseOf()</code> are 
     * equivalent, since <code>owl:inverseOf</code> is a symmetric property.
     * </p>
     * 
     * @return The property that is the inverse of this property, or null. 
     */
    public OntProperty getInverse();
    
    /**
     * <p>Answer an iterator over the properties that are defined to be inverses of this property.</p>
     * @return An iterator over the properties that declare themselves the <code>inverseOf</code> this property.
     */
    public ExtendedIterator listInverse();
    
    /**
     * <p>Answer true if there is at least one inverse property for this property.</p>
     * @return True if property has an inverse.
     */
    public boolean hasInverse();
}


/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

