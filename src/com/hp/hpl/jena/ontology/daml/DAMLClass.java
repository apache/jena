/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLClass.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:14:48 $
 *               by   $Author: bwm $
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
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////

import com.hp.hpl.jena.rdf.model.ResIterator;

import com.hp.hpl.jena.util.iterator.ConcatenatedIterator;

import java.util.Iterator;



/**
 * Java representation of a DAML ontology Class. Note that the ontology classes are
 * not the same as Java classes: think of classifications rather than active data structures.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLClass.java,v 1.1.1.1 2002-12-19 19:14:48 bwm Exp $
 */
public interface DAMLClass
    extends DAMLCommon, DAMLClassExpression
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


    /**
     * Property accessor for the 'subClassOf' property of a class. This
     * denotes a class-expression that is a super-class of this class.
     *
     * @return Property accessor for 'onProperty'.
     */
    public PropertyAccessor prop_subClassOf();


    /**
     * Property accessor for the 'disjointWith' property of a class. This
     * denotes a class-expression with which this class has no instances in common.
     *
     * @return Property accessor for 'disjointWith'.
     */
    public PropertyAccessor prop_disjointWith();


    /**
     * Property accessor for the 'disjointUnionOf' property of a class. This
     * denotes a list of class expressions that are each pair-wise disjoint, and whose
     * union describes this class.
     *
     * @return Property accessor for 'disjointUnionOf'.
     */
    public PropertyAccessor prop_disjointUnionOf();


    /**
     * Property accessor for the 'sameClassAs' property of a class. This
     * denotes a class-expression whose instances are the same those of this class.
     *
     * @return Property accessor for 'sameClassAs'.
     */
    public PropertyAccessor prop_sameClassAs();


    /**
     * Property accessor for the property 'unionOf', which is one element of the range
     * of boolean expressions over classes permitted by DAML.
     *
     * @return property accessor for 'unionOf'.
     */
    public PropertyAccessor prop_unionOf();


    /**
     * Property accessor for the property 'intersectionOf', which is one element of the range
     * of boolean expressions over classes permitted by DAML.
     *
     * @return property accessor for 'intersectionOf'.
     */
    public PropertyAccessor prop_intersectionOf();


    /**
     * Property accessor for the property 'compelementOf', which is one element of the range
     * of boolean expressions over classes permitted by DAML.
     *
     * @return property accessor for 'complementOf'.
     */
    public PropertyAccessor prop_complementOf();


    /**
     * Property accessor for the 'oneOf' property, which defines a class expression
     * denoting that the class is exactly one of the given list of class expressions.
     *
     * @return property accessor for 'oneOf'
     */
    public PropertyAccessor prop_oneOf();


    /**
     * Answer an iterator over the DAML classes (or, strictly, class expressions)
     * that mention this class as one of its super-classes.  Will generate the
     * closure of the iteration over the sub-class relationship.
     *
     * @return an iterator over this class's sub-classes. The members of the
     *         iteration will be DAMLClass objects.
     */
    public Iterator getSubClasses();


    /**
     * Answer an iterator over the DAML classes (or, strictly, class expressions)
     * that mention this class as one of its super-classes.
     *
     * @param closed If true, close the iteration over the sub-class relation: i.e.
     *               return the sub-classes of the sub-classes, etc.
     * @return an iterator over this class's super-classes. The members of the
     *         iteration will be DAMLClass objects.
     */
    public Iterator getSubClasses( boolean closed );


    /**
     * Answer an iterator over the DAML classes (or, strictly, class expressions)
     * that mention this class as one of its sub-classes.  Will generate the
     * closure of the iteration over the super-class relationship.
     *
     * @return an iterator over this class's super-classes. The members of the
     *         iteration will be DAMLClass objects.
     */
    public Iterator getSuperClasses();


    /**
     * Answer an iterator over the DAML classes (or, strictly, class expressions)
     * that mention this class as one of its sub-classes.
     *
     * @param closed If true, close the iteration over the super-class relation: i.e.
     *               return the super-classes of the super-classes, etc.
     * @return an iterator over this class's sub-classes. The members of the
     *         iteration will be DAMLClass objects.
     */
    public Iterator getSuperClasses( boolean closed );


    /**
     * Answer an iterator over all of the DAML classes that are equivalent to this
     * value under the <code>daml:sameClassAs</code> relation.  Note: only considers
     * <code>daml:sameClassAs</code>, for general equivalence, see
     * {@link #getEquivalentValues}.  Note that the first member of the iteration is
     * always the DAMLClass on which the method is invoked: trivially, a DAMLClass is
     * a member of the set of DAMLClasses equivalent to itself.  If the caller wants
     * the set of classes equivalent to this one, not including itself, simply ignore
     * the first element of the iteration.
     *
     * @return an iterator ranging over every equivalent DAML class - each value of
     *         the iteration will be a DAMLClass object.
     */
    public Iterator getSameClasses();


    /**
     * Answer an iterator over all of the DAML objects that are equivalent to this
     * class, which will be the union of <code>daml:equivalentTo</code> and
     * <code>daml:sameClassAs</code>.
     *
     * @return an iterator ranging over every equivalent DAML class - each value of
     *         the iteration should be a DAMLClass object.
     */
    public Iterator getEquivalentValues();


    /**
     * Answer true if the given class is a sub-class of this class, using information
     * from the <code>rdf:subClassOf</code> or <code>daml:subClassOf</code> relation.
     *
     * @param cls A DAMLClass object
     * @return True if this class is a super-class of the given class.
     */
    public boolean hasSubClass( DAMLClass cls );


    /**
     * Answer true if the given class is a super-class of this class, using information
     * from the <code>rdf:subClassOf</code> or <code>daml:subClassOf</code> relation.
     *
     * @param cls A DAMLClass object
     * @return True if this class is a sub-class of the given class.
     */
    public boolean hasSuperClass( DAMLClass cls );


    /**
     * Answer an iterator over the instances of this class that currently exist
     * in the model.
     *
     * @return An iterator over those instances that have this class as one of
     *         the classes to which they belong
     * @see com.hp.hpl.jena.ontology.daml.DAMLCommon#getRDFTypes
     */
    public Iterator getInstances();


    /**
     * Answer an iteration of the properties that may be used for
     * instances of this class: i.e. the properties that have this class,
     * or one of its super-classes, as domain.
     *
     * @return An iteration of the properties that have this class as domain
     */
    public Iterator getDefinedProperties();


    /**
     * Answer an iteration of the properties that may be used for
     * instances of this class: i.e. the properties that have this class,
     * or optionally one of its super-classes, as domain.
     *
     * @param closed If true, close the iteration over the super-classes
     *               of this class.
     * @return An iteration of the properties that have this class as domain
     */
    public Iterator getDefinedProperties( boolean closed );
}
