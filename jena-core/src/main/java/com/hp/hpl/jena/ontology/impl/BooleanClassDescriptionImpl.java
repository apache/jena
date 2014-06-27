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

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;



/**
 * <p>
 * Shared implementation for implementations of Boolean clas expressions.
 * </p>
 */
public abstract class BooleanClassDescriptionImpl
    extends OntClassImpl
    implements BooleanClassDescription
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an boolean class description represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public BooleanClassDescriptionImpl( Node n, EnhGraph g ) {
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
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    @Override
    public void setOperands( RDFList operands ) {
        setPropertyValue( operator(), getOperatorName(), operands );
    }

    /**
     * <p>Add a class the operands of this boolean expression.</p>
     * @param cls A class that will be added to the operands of this Boolean expression
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    @Override
    public void addOperand( Resource cls ) {
        addListPropertyValue( operator(), getOperatorName(), cls );
    }

    /**
     * <p>Add all of the classes from the given iterator to the operands of this boolean expression.</p>
     * @param classes A iterator over classes that will be added to the operands of this Boolean expression
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    @Override
    public void addOperands( Iterator<? extends Resource> classes ) {
        while (classes.hasNext()) {
            addOperand( classes.next() );
        }
    }

    /**
     * <p>Answer the list of operands for this Boolean class expression.</p>
     * @return A list of the operands of this expression.
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    @Override
    public RDFList getOperands() {
        return objectAs( operator(), getOperatorName(), RDFList.class );
    }

    /**
     * <p>Answer an iterator over all of the classes that are the operands of this
     * Boolean class expression. Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the operands of the expression.
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<? extends OntClass> listOperands() {
        return getOperands().iterator().mapWith( new AsMapper<>( OntClass.class ) );
    }

    /**
     * <p>Answer true if this Boolean class expression has the given class as an operand.</p>
     * @param cls A class to test
     * @return True if the given class is an operand to this expression.
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    @Override
    public boolean hasOperand( Resource cls ) {
        return getOperands().contains( cls );
    }


    /**
     * <p>Remove the given resource from the operands of this class expression.</p>
     * @param res An resource to be removed from the operands of this class expression
     */
    @Override
    public void removeOperand( Resource res ) {
        setOperands( getOperands().remove( res ) );
    }

    /**
     * <p>Answer the property that is used to construct this boolean expression, for example
     * {@link Profile#UNION_OF()}.</p>
     * @return The property used to construct this Boolean class expression.
     */
    @Override
    public abstract Property operator();



    // Internal implementation methods
    //////////////////////////////////

    /** Answer the name of the operator, so that we can give informative error messages */
    protected abstract String getOperatorName();


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
