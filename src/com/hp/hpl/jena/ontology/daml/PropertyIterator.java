/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            11 Sept 2001
 * Filename           $RCSfile: PropertyIterator.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-06-17 14:53:34 $
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
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////

import java.util.*;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.util.iterator.ConcatenatedIterator;

import com.hp.hpl.jena.vocabulary.DAML_OIL;



/**
 * <p>
 * Provides a means of traversing the relationships in a DAML model, respecting
 * some of the extended semantics of DAML+OIL over RDF.  In particular, the
 * PropertyIterator knows about class, property and instance equivalence,
 * transitive properties, inverse properties, the class hierarchy and the
 * property hierarchy.
 * </p>
 * <p>
 * Given a property P, and a resource x, iterates over all the y such that
 * <code>x P y</code>, respecting the fact that P may be transitive (so
 * <code>x P y</code> and <code>y P z</code> implies <code>x P z</code>), and
 * symmetric (so <code>x P y</code> implies <code>y P x</code>).  The iterator
 * is lazily evaluated, so changes to the model while iterating could generate
 * unpredictable results.  The iterator does do loop detection, so should always
 * terminate (assuming the model is finite!). Deletion is not supported.
 * </p>
 * <p>
 * This iterator also supports the setting of a default value.  The default value
 * is an object that will be returned as the last value of the iteration, unless
 * it has already been returned earlier.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: PropertyIterator.java,v 1.9 2003-06-17 14:53:34 chris-dollin Exp $
 * @since Jena 1.3.0 (was previously in package com.hp.hpl.jena.ontology.daml.impl).
 */
public class PropertyIterator
    implements Iterator
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The queue of nodes to be evaluated */
    protected LinkedList m_nodeQueue = new LinkedList();

    /** The property we're evaluating */
    protected Property m_pred = null;

    /** The inverse of pred, or null */
    protected Property m_inverse = null;

    /** Collection of properties equivalent to m_pred */
    protected HashSet m_predEquivs = new HashSet();

    /** Collection of properties equivalent to m_inverse */
    protected HashSet m_inverseEquivs = new HashSet();

    /** Flag to say if pred is transitive */
    protected boolean m_transitive = false;

    /** Set of nodes we've seen on this trip */
    protected WeakHashMap m_seen = new WeakHashMap();

    /** The resource we started from (if only one) */
    protected Resource m_root = null;

    /** The resources we started from (if many) */
    protected List m_roots = null;

    /** The default value for the iterator, or null if no default */
    protected Object m_defaultValue = null;

    /** A flag to show that the default value has been returned */
    protected boolean m_defaultValueSeen = false;

    /** A flag to control whether we use equivalent values during the iteration */
    protected boolean m_useEquivalence = true;

    /** The model we are operating on */
    protected Model m_model = null;



    // Constructors
    //////////////////////////////////

    /**
     * Construct a property iterator for the given property, starting from the
     * given resource.  The property may be defined to be symmetric by supplying
     * its inverse (which could be itself), and/or transitive.  The property may also
     * be defined to be reflexive, in which case root itself will be returned as
     * a member of the iteration.
     *
     * @param root The root resource from whence to start iterating over the closure of pred
     * @param pred The property to iterate over
     * @param inverse The inverse of pred, or null if pred has no inverse. The inverse is used
     *                to include resource y in the iteration if P' = inverse(P) and y P' x.
     * @param isTransitive If true, the property is transitive
     * @param isReflexive If true, the property is reflexive (so, the root resource will be included
     *                    in the iteration).
     */
    public PropertyIterator( Resource root, Property pred, Property inverse, boolean isTransitive, boolean isReflexive ) {
        this( root, pred, inverse, isTransitive, isReflexive, true );
    }

    /**
     * Construct a property iterator for the given property, starting from the
     * given resource.  The property may be defined to be symmetric by supplying
     * its inverse (which could be itself), and/or transitive.  The property may also
     * be defined to be reflexive, in which case root itself will be returned as
     * a member of the iteration.
     *
     * @param root The root resource from whence to start iterating over the closure of pred
     * @param pred The property to iterate over
     * @param inverse The inverse of pred, or null if pred has no inverse. The inverse is used
     *                to include resource y in the iteration if P' = inverse(P) and y P' x.
     * @param isTransitive If true, the property is transitive
     * @param isReflexive If true, the property is reflexive (so, the root resource will be included
     *                    in the iteration).
     * @param useEquivalence If true, equivalence between DAML values will be included in the
     * iteration (unless the model containing the DAML values
     * has equivalence switched off via {@link DAMLModel#setUseEquivalence}).
     */
    public PropertyIterator( Resource root, Property pred, Property inverse, boolean isTransitive,
                             boolean isReflexive, boolean useEquivalence ) {
        m_root = root;
        m_pred = pred;
        m_inverse = inverse;
        m_transitive = isTransitive;
        m_useEquivalence = useEquivalence;
        setModel();
        cachePropertyEquivs();

        // the root only goes on the queue if the relation is reflexive
        if (isReflexive) {
           enqueue( root );
        }
        else {
            // otherwise, we'll just start with the relations of the root
            expandQueue( root );
        }
    }


    /**
     * Construct a property iterator for the given property, starting from the
     * given set of resources.  The property may be defined to be symmetric by supplying
     * its inverse (which could be itself), and/or transitive.  The property may also
     * be defined to be reflexive, in which case all of the given root resources will
     * be returned as members of the iteration.
     *
     * @param roots A set of root resources from whence to start iterating over the closure of pred,
     *              represented as an iterator
     * @param pred The property to iterate over
     * @param inverse The inverse of pred, or null if pred has no inverse. The inverse is used
     *                to include resource y in the iteration if P' = inverse(P) and y P' x.
     * @param isTransitive If true, the property is transitive
     * @param isReflexive If true, the property is reflexive (so, the root resources will be included
     *                    in the iteration).
     */
    public PropertyIterator( Iterator roots, Property pred, Property inverse, boolean isTransitive, boolean isReflexive ) {
        this( roots, pred, inverse, isTransitive, isReflexive, true );
    }


    /**
     * Construct a property iterator for the given property, starting from the
     * given set of resources.  The property may be defined to be symmetric by supplying
     * its inverse (which could be itself), and/or transitive.  The property may also
     * be defined to be reflexive, in which case all of the given root resources will
     * be returned as members of the iteration.
     *
     * @param roots A set of root resources from whence to start iterating over the closure of pred,
     *              represented as an iterator
     * @param pred The property to iterate over
     * @param inverse The inverse of pred, or null if pred has no inverse. The inverse is used
     *                to include resource y in the iteration if P' = inverse(P) and y P' x.
     * @param isTransitive If true, the property is transitive
     * @param isReflexive If true, the property is reflexive (so, the root resources will be included
     *                    in the iteration).
     * @param useEquivalence If true, equivalence between DAML values will be included in the
     * iteration (unless the model containing the DAML values has equivalence
     * switched off via {@link DAMLModel#setUseEquivalence}.
     */
    public PropertyIterator( Iterator roots, Property pred, Property inverse, boolean isTransitive,
                             boolean isReflexive, boolean useEquivalence ) {
        // copy the roots of the traversal
        m_roots = new ArrayList();
        m_pred = pred;
        m_inverse = inverse;
        m_transitive = isTransitive;
        m_useEquivalence = useEquivalence;
        setModel();
        cachePropertyEquivs();

        // the root only goes on the queue if the relation is reflexive
        if (isReflexive) {
            // reflexive, so we queue each root to be expanded (and it will get returned by next())
            while (roots.hasNext()) {
                Resource next = (Resource) roots.next();

                if (m_model == null  &&  next.getModel() != null) {
                    // keep a reference to the model if we find one
                    m_model = next.getModel();
                }

                if (next instanceof Resource) {
                    m_roots.add( next );
                    enqueue( next );
                }
            }
        }
        else {
            // not reflexive, so we'll just start with the relations of the root on the queue
            while (roots.hasNext()) {
                Resource next = (Resource) roots.next();

                if (m_model == null  &&  next.getModel() != null) {
                    // keep a reference to the model if we find one
                    m_model = next.getModel();
                }

                if (next instanceof Resource) {
                    m_roots.add( next );
                    expandQueue( next );
                }
            }
        }
    }


    // External signature methods
    //////////////////////////////////

    /**
     * Answer true if the iteration over the closure of the predicate will answer any values that have
     * not yet been returned.
     *
     * @return True if there is at least one more element in the iteration
     */
    public boolean hasNext() {
        // is there at least one more node in the queue?
        return !m_nodeQueue.isEmpty()  ||  (hasDefaultValue() && !m_defaultValueSeen);
    }


    /**
     * Answer the next RDFNode in the iteration over the given predicate.  Note that each node in the
     * closure of the predicate will be answered only once, to avoid endless loops.
     *
     * @return The next RDFNode in the iteration over the closure of the predicate.
     * @exception java.util.NoSuchElementException if the iterator has no more elements
     */
    public Object next() {
        if (!m_nodeQueue.isEmpty()) {
            // get the next node from the queue, this will be the one that we return
            RDFNode next = (RDFNode) m_nodeQueue.removeFirst();

            // is this the default value?
            if (hasDefaultValue()  &&  m_defaultValue.equals( next )) {
                m_defaultValueSeen = true;
            }

            // now add the relations of the node to the end of the queue
            if (next instanceof com.hp.hpl.jena.rdf.model.Resource) {
                expandQueue( (Resource) next );
            }

            // answer the next node in the interation
            return next;
        }
        else if (hasDefaultValue()  &&  !m_defaultValueSeen) {
            // return the default value for this iterator
            m_defaultValueSeen = true;
            return m_defaultValue;
        }
        else {
            // no more nodes, so this is an error
            throw new NoSuchElementException( "Tried to access next() element from empty property iterator" );
        }
    }


    /**
     * Unsupported operation in this iterator.
     *
     * @exception java.lang.UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException( "Cannot remove elements from a property iterator" );
    }


    /**
     * Set the default value for this iteration, which will be a value that
     * is guaranteed to be returned as a member of the iteration.  To guarantee
     * that the default value is only returned if it has not already been
     * returned by the iterator, setting the default value should occur before
     * the first call to {@link #next}.
     *
     * @param defaultValue The default value for the iteration, or null for
     *                     there to be no default value.  The default default
     *                     value is null.
     */
    public void setDefaultValue( Object defaultValue ) {
        m_defaultValue = defaultValue;
    }


    /**
     * Answer true if this iteration has a default value.
     *
     * @return true if there is a default value
     */
    public boolean hasDefaultValue() {
        return m_defaultValue != null;
    }



    // Internal implementation methods
    //////////////////////////////////


    /**
     * Queue a node onto the evaluation queue, but only if it has not already been evaluated
     * (to avoid loops).
     *
     * @param node The node to add to the queue
     */
    private void enqueue( RDFNode node ) {
        if (!m_seen.containsKey( node )) {
            // mark this node as seen
            m_seen.put( node, Boolean.TRUE );

            // add it to the queue
            m_nodeQueue.addLast( node );

            // also queue up the equivalens to the node
            if (getUseEquivalence()  &&  node instanceof DAMLCommon) {
                for (Iterator i = ((DAMLCommon) node).getEquivalentValues();  i.hasNext(); ) {
                    enqueue( (RDFNode) i.next() );
                }
            }
        }
    }


    /**
     * Expand the queue with the arcs to/from the given resource
     *
     * @param r The resource we're expanding
     */
    protected void expandQueue( Resource r ) {
        try {
            // add all outgoing arcs if we're at the root or the predicate is transitive
            if (m_pred != null  &&  (m_transitive  ||  isRoot( r ))) {
                // we want all the related items from this node
                for (Iterator i = getStatementObjects( r );
                     i.hasNext();
                     enqueue( (RDFNode) i.next() ));
            }

            // if we know the inverse, we also add the incoming arcs to this node
            if (m_inverse != null  &&  (m_transitive  ||  isRoot( r ))) {
                // find statements whose predicate is m_inverse and object is current node
                for (Iterator i = getStatementSubjects( r );
                     i.hasNext();
                     enqueue( (RDFNode) i.next() ));
            }
        }
        catch (JenaException e) {
            Log.severe( "RDF exception while traversing graph: " + e, e );
        }
    }


    /**
     * Answer true if the given resource was one of the starting points.
     *
     * @param r A resource
     * @return True if resource r was one of the roots of this iteration.
     */
    protected boolean isRoot( Resource r ) {
        boolean isRoot = false;

        if (m_roots != null) {
            // started with a set of roots
            return m_roots.contains( r );
        }
        else {
            // just the one root - compare this one to it
            return r == m_root;
        }
    }


    /**
     * Answer an iterator over objects of statements with res
     * as subject, and m_pred or one of its equivalent properties as property,
     * including all of the equivalent values to the object.
     *
     * @param res A resource
     * @return An iterator over the object of any statements whose subject is res.
     */
    protected Iterator getStatementObjects( Resource res )
    {
        Iterator i = null;

        if (getUseEquivalence()  &&  res instanceof DAMLCommon) {
            // consider equivalents for res and for m_pred
            for (Iterator j = m_predEquivs.iterator();   j.hasNext();  ) {
                // get an iterator for the values of the resource with this predicate
                Iterator pIter = new PropertyIterator( res, (Property) j.next(), null, m_transitive, false, false );

                // build a joint iterator
                i = (i == null) ? pIter  : new ConcatenatedIterator( pIter,  i );
            }
        }
        else {
            // don't use equivalents
            if (m_model != null) {
                // we have a model to query, so use it to get the object of the triple
                i = m_model.listObjectsOfProperty( res, m_pred );
            }
            else {
                // no model (can occur when using built-in constants from vocab)
                i = new LinkedList().iterator();
            }
        }

        return i;
    }


    /**
     * Answer an iterator over subjects of statements with res (or one of its equivalents)
     * as object, and m_inverse or one of its equivalent properties as property.
     *
     * @param res A resource
     * @return An iterator over the subject of any statements whose object is res.
     */
    protected Iterator getStatementSubjects( Resource res )
    {
        Iterator i = null;

        if (getUseEquivalence()  &&  res instanceof DAMLCommon) {
            // consider equivalents for res and for m_pred
            for (Iterator j = m_inverseEquivs.iterator();   j.hasNext();  ) {
                // get an iterator for the values of the resource with this predicate
                Iterator pIter = new PropertyIterator( res, null, (Property) j.next(), m_transitive, false, false );

                // build a joint iterator
                i = (i == null) ? pIter  : new ConcatenatedIterator( pIter,  i );
            }
        }
        else {
            // don't use equivalents
            if (m_model != null) {
                // we have a model to query, so use it to get the subject of the triple
                i = m_model.listSubjectsWithProperty( m_inverse, res );
            }
            else {
                // no model (can occur when using built-in constants from vocab)
                i = new LinkedList().iterator();
            }
        }

        return i;
    }


    /**
     * Cache the equivalent properties to the principal properties and their inverses we
     * are using
     */
    protected void cachePropertyEquivs() {
        if (getUseEquivalence()) {
            // store the equivalent properties to m_pred
            if (m_pred != null) {
                if (m_pred instanceof DAMLProperty) {
                    // daml property - we know about equivalence classes for these
                    for (Iterator i = ((DAMLProperty) m_pred).getEquivalentValues();  i.hasNext(); ) {
                        cacheProperty( m_predEquivs, (Property) i.next() );
                    }
                }
                else {
                    // normal rdf property, is its own equiv
                    cacheProperty( m_predEquivs, m_pred );
                }
            }

            // store the equivalent properties to m_inverse
            if (m_inverse != null) {
                if (m_inverse instanceof DAMLProperty) {
                    // daml property - we know about equivalence classes for these
                    for (Iterator i = ((DAMLProperty) m_inverse).getEquivalentValues();  i.hasNext(); ) {
                        cacheProperty( m_inverseEquivs, (Property) i.next() );
                    }
                }
                else {
                    // normal rdf property, is its own equiv
                    cacheProperty( m_inverseEquivs, m_inverse );
                }
            }
        }
    }


    /**
     * Add the given property, and it sub-properties, to the set of cached properties.
     *
     * @param s A set
     * @param p A property to add to the set s.
     */
    protected void cacheProperty( HashSet s, Property p ) {
        s.add( p );

        // also add the sub-properties of this property: if the sub-prop holds,
        // this property holds also
        // check for p being 'subPropertyOf' to avoid infinite regress!
        if (p instanceof DAMLProperty  &&  !p.getLocalName().equals( DAML_OIL.subPropertyOf.getLocalName() )) {
            for (Iterator i = ((DAMLProperty) p).getSubProperties();  i.hasNext(); ) {
                s.add( i.next() );
            }
        }
    }


    /**
     * Attempt to determine which model we are working on
     */
    protected void setModel() {
        // try the root object, if defined
        if (m_root != null  &&  m_root.getModel() != null) {
            m_model = m_root.getModel();
            return;
        }

        // try the predicate object
        if (m_pred != null  &&  m_pred.getModel() != null) {
            m_model = m_pred.getModel();
            return;
        }

        // try the predicate inverse
        if (m_inverse != null  &&  m_inverse.getModel() != null) {
            m_model = m_inverse.getModel();
            return;
        }

        // try the set of roots
        if (m_roots != null) {
            for (Iterator i = m_roots.iterator();  i.hasNext(); ) {
                RDFNode n = (RDFNode) i.next();
                if (n instanceof Resource  &&  ((Resource) n).getModel() != null) {
                    m_model = ((Resource) n).getModel();
                    return;
                }
            }
        }
    }


    /**
     * Answer true if we are computing with equivalence classes at the moment.
     *
     * @return True if equivlance is to be computed.
     */
    protected boolean getUseEquivalence() {
        // if we have a model, it must have equivalence switched on
        // if no model, default to relying on the m_useEquivalence setting
        boolean modelEquivFlag = m_model == null  ||
                                 !(m_model instanceof DAMLModel) ||
                                 ((DAMLModel) m_model).getUseEquivalence();

        return m_useEquivalence  &&  modelEquivFlag;
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
