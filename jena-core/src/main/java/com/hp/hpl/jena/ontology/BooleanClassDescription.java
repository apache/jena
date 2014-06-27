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
package com.hp.hpl.jena.ontology;



// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.*;


/**
 * <p>
 * Encapsulates a class description formed from a boolean combination of other
 * class descriptions (ie union, intersection or complement).
 * </p>
 */
public interface BooleanClassDescription
    extends OntClass
{
    // Constants
    //////////////////////////////////



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
    public void setOperands( RDFList operands );

    /**
     * <p>Add a class the operands of this boolean expression.</p>
     * @param cls A class that will be added to the operands of this Boolean expression
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    public void addOperand( Resource cls );

    /**
     * <p>Add all of the classes from the given iterator to the operands of this boolean expression.</p>
     * @param classes A iterator over classes that will be added to the operands of this Boolean expression
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    public void addOperands( Iterator<? extends Resource> classes );

    /**
     * <p>Answer the list of operands for this Boolean class expression.</p>
     * @return A list of the operands of this expression.
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    public RDFList getOperands();

    /**
     * <p>Answer an iterator over all of the classes that are the operands of this
     * Boolean class expression. Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the operands of the expression.
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntClass> listOperands();

    /**
     * <p>Answer true if this Boolean class expression has the given class as an operand.</p>
     * @param cls A class to test
     * @return True if the given class is an operand to this expression.
     * @exception ProfileException If the operand property is not supported in the current language profile.
     */
    public boolean hasOperand( Resource cls );

    /**
     * <p>Remove the given resource from the operands of this class expression.</p>
     * @param res An resource to be removed from the operands of this class expression
     */
    public void removeOperand( Resource res );


     /**
      * <p>Answer the property that is used to construct this boolean expression, for example
      * {@link Profile#UNION_OF()}.</p>
      * @return The property used to construct this Boolean class expression.
      */
    public Property operator();

}
