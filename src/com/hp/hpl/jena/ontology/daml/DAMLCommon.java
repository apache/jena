/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLCommon.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:50:18 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.*;


/**
 * <p>Abstract super-class for all DAML resources (including properties).  Defines shared
 * implementations and common services, such as property manipulation, vocabulary
 * management and <code>rdf:type</code> management.  Also defines accessors for common
 * properties, including <code>comment</code>, <code>label</code>, and <code>equivalentTo</code>.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLCommon.java,v 1.9 2004-12-06 13:50:18 andy_seaborne Exp $
 */
public interface DAMLCommon
    extends OntResource
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the DAML model wherein this value is stored.</p>
     *
     * @return a DAMLModel reference.
     */
    public DAMLModel getDAMLModel();


    /**
     * <p>Add an RDF type property for this node in the underlying model. If the replace flag
     * is true, this type will replace any current type property for the node. Otherwise,
     * the type will be in addition to any existing type property.</p>
     * <p>Deprecated in favour of {@link OntResource#addRDFType} for add, or 
     * {@link OntResource#setRDFType} for replace.</p>
     *
     * @param rdfClass The RDF resource denoting the class that will be new value for the rdf:type property.
     * @param replace  If true, the given class will replace any existing type property for this
     *                 value, otherwise it will be added as an extra type statement.
     * @deprecated Use {@link OntResource#addRDFType} or {@link OntResource#setRDFType}.
     */
    public void setRDFType( Resource rdfClass, boolean replace );


    /**
     * <p>Answer an iterator over all of the types to which this resource belongs. Optionally,
     * restrict the results to the most specific types, so that any class that is subsumed by
     * another class in this resource's set of types is not reported.</p>
     * <p><strong>Note:</strong> that the interpretation of the <code>complete</code> flag has
     * changed since Jena 1.x. Previously, the boolean flag was to generated the transitive 
     * closure of the class hierarchy; this is now handled by the underlyin inference graph
     * (if specified). Now the flag is used to restrict the returned values to the most-specific
     * types for this resource.</p>
     *
     * @param complete If true, return all known types; if false, return only the most-specific
     * types.
     * @return an iterator over the set of this value's classes
     */
    public ExtendedIterator getRDFTypes( boolean complete );


    /**
     * <p>Answer the DAML+OIL vocabulary that corresponds to the namespace that this value
     * was declared in.</p>
     *
     * @return A vocabulary object
     */
    public DAMLVocabulary getVocabulary();


    /**
     * <p>Answer an iterator over all of the DAML objects that are equivalent to this
     * value under the <code>daml:equivalentTo</code> relation.
     * Note that the first member of the iteration is
     * always the DAML value on which the method is invoked: trivially, a value is
     * a member of the set of values equivalent to itself.  If the caller wants
     * the set of values equivalent to this one, not including itself, simply ignore
     * the first element of the iteration.</p>
     *
     * @return An iterator ranging over every equivalent DAML value
     */
    public ExtendedIterator getEquivalentValues();


    /**
     * <p>Answer the set of equivalent values to this value, but not including the
     * value itself.  The iterator will range over a set: each element occurs only
     * once.</p>
     *
     * @return An iteration ranging over the set of values that are equivalent to this
     *         value, but not itself.
     */
    public ExtendedIterator getEquivalenceSet();


    // Properties
    /////////////

    /**
     * <p>Accessor for the property of the label on the value, whose value
     * is a literal (string).</p>
     *
     * @return Literal accessor for the label property
     */
    public LiteralAccessor prop_label();

    /**
     * <p>Accessor for the property of the comment on the value, whose value
     * is a literal (string).</p>
     *
     * @return Literal accessor for the comment property
     */
    public LiteralAccessor prop_comment();

    /**
     * <p>Property accessor for the <code>equivalentTo</code> property. This
     * denotes that two terms have the same meaning. The DAML spec helpfully
     * says: <i>for equivalentTo(X, Y), read X is an equivalent term to Y</i>.
     *
     * @return Property accessor for <code>equivalentTo</code>.
     */
    public PropertyAccessor prop_equivalentTo();


    /**
     * <p>Property accessor for the <code>rdf:type</code> property of a DAML value.
     *
     * @return Property accessor for <code>rdf:type</code>
     */
    public PropertyAccessor prop_type();


}



/*
    (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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

