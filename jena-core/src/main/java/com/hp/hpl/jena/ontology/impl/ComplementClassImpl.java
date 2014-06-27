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

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import java.util.Iterator;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Implementation of a node representing a complement class description.
 * </p>
 */
public class ComplementClassImpl
    extends OntClassImpl
    implements ComplementClass
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating ComplementClass facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new ComplementClassImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to ComplementClass");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an ComplementClass facet if it has rdf:type owl:Class and an owl:complementOf statement (or equivalents)
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            Property comp = (profile == null) ? null : profile.COMPLEMENT_OF();

            return (profile != null)  &&
                   profile.isSupported( node, eg, OntClass.class )  &&
                   comp != null &&
                   eg.asGraph().contains( node, comp.asNode(), Node.ANY );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a complement class node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public ComplementClassImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    // operand

    /**
     * <p>Assert that the operands for this boolean class expression are the classes
     * in the given list. Any existing
     * statements for the operator will be removed.</p>
     * @param operands The list of operands to this expression.
     * @exception UnsupportedOperationException since a complement expression takes only a single argument.
     */
    @Override
    public void setOperands( RDFList operands ) {
        throw new UnsupportedOperationException( "ComplementClass takes a single operand, not a list.");
    }


    /**
     * <p>Set the class that the class represented by this class expression is
     * a complement of. Any existing value for <code>complementOf</code> will
     * be replaced.</p>
     * @param cls The class that this class is a complement of.
     */
    @Override
    public void setOperand( Resource cls ) {
        setPropertyValue( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF", cls );
    }

    /**
     * <p>Add a class the operands of this boolean expression.</p>
     * @param cls A class that will be added to the operands of this Boolean expression
     * @exception UnsupportedOperationException since a complement expression takes only
     * a single argument.
     */
    @Override
    public void addOperand( Resource cls ) {
        throw new UnsupportedOperationException( "ComplementClass is only defined for  a single operand.");
    }

    /**
     * <p>Add all of the classes from the given iterator to the operands of this boolean expression.</p>
     * @param classes A iterator over classes that will be added to the operands of this Boolean expression
     * @exception UnsupportedOperationException since a complement expression takes only
     * a single argument.
     */
    @Override
    public void addOperands( Iterator<? extends Resource> classes ) {
        throw new UnsupportedOperationException( "ComplementClass is only defined for  a single operand.");
    }

    /**
     * <p>Answer the list of operands for this Boolean class expression.</p>
     * @return A list of the operands of this expression.
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    @Override
    public RDFList getOperands() {
        throw new UnsupportedOperationException( "ComplementClass takes a single operand, not a list.");
    }

    /**
     * <p>Answer an iterator over all of the classes that are the operands of this
     * Boolean class expression. Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the operands of the expression.
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<? extends OntClass> listOperands() {
        return listAs( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF", OntClass.class );
    }

    /**
     * <p>Answer true if this Boolean class expression has the given class as an operand.</p>
     * @param cls A class to test
     * @return True if the given class is an operand to this expression.
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    @Override
    public boolean hasOperand( Resource cls ) {
        return hasPropertyValue( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF", cls );
    }

    /**
     * <p>Answer the class that the class described by this class description
     * is a complement of.</p>
     * @return The class that this class is a complement of.
     */
    @Override
    public OntClass getOperand() {
        return objectAs( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF", OntClass.class );
    }

    /**
     * <p>Remove the given resource from the operands of this class expression.</p>
     * @param res An resource to be removed from the operands of this class expression
     */
    @Override
    public void removeOperand( Resource res ) {
        removePropertyValue( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF", res );
    }


    /**
     * <p>Answer the property that is used to construct this boolean expression, for example
     * {@link Profile#UNION_OF()}.</p>
     * @return {@link Profile#COMPLEMENT_OF()}
     */
    @Override
    public Property operator() {
        return getProfile().COMPLEMENT_OF();
    }



    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
