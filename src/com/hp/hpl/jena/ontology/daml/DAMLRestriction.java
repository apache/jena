/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLRestriction.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:15:00 $
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


/**
 * Java representation of a DAML Restriction.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLRestriction.java,v 1.1.1.1 2002-12-19 19:15:00 bwm Exp $
 */
public interface DAMLRestriction
    extends DAMLClass, DAMLClassExpression
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


    /**
     * Property accessor for the 'onProperty' property of a restriction. This
     * denotes the property to which the restriction applies, and there is normally
     * exactly one of them.
     *
     * @return Property accessor for 'onProperty'.
     */
    public PropertyAccessor prop_onProperty();


    /**
     * Property accessor for the 'toClass' property of a restriction. This denotes
     * the class for which the restricted property always maps to instances that
     * belong to the class given by this property.
     *
     * @return Property accessor for 'toClass'
     */
    public PropertyAccessor prop_toClass();


    /**
     * Property accessor for the 'hasValue' property of a restriction. This denotes
     * the class for which the restricted property sometimes maps to the instance given
     * here.
     *
     * @return Property accessor for 'hasValue'
     */
    public PropertyAccessor prop_hasValue();


    /**
     * Property accessor for the 'hasClass' property of a restriction. This denotes
     * the class for which the restricted property sometimes maps to the instances that
     * belong to the class given here.
     *
     * @return Property accessor for 'hasClass'
     */
    public PropertyAccessor prop_hasClass();


    /**
     * Property accessor for the 'hasClassQ' property of a restriction. This denotes
     * the class for which the restricted property sometimes maps to the instances that
     * belong to the class given here, and which obey given cardinality constraints.
     *
     * @return Property accessor for 'hasClassQ'
     */
    public PropertyAccessor prop_hasClassQ();


    /**
     * Property accessor for the 'cardinality' property of a restriction. This denotes
     * the combination of minCardinality and maxCardinality to the same value.
     *
     * @return Property accessor for 'cardinality'
     */
    public IntLiteralAccessor prop_cardinality();


    /**
     * Property accessor for the 'minCardinality' property of a restriction. This denotes
     * the class of instances that have at least N distict values for the property.
     *
     * @return Property accessor for 'minCardinality'
     */
    public IntLiteralAccessor prop_minCardinality();


    /**
     * Property accessor for the 'maxCardinality' property of a restriction. This denotes
     * the class of instances that have at most N distict values for the property.
     *
     * @return Property accessor for 'maxCardinality'
     */
    public IntLiteralAccessor prop_maxCardinality();


    /**
     * Property accessor for the 'cardinalityQ' property of a restriction. This denotes
     * the combination of minCardinalityQ and maxCardinalityQ to the same value.
     *
     * @return Property accessor for 'cardinalityQ'
     */
    public IntLiteralAccessor prop_cardinalityQ();


    /**
     * Property accessor for the 'minCardinalityQ' property of a restriction. This denotes
     * the class of instances that have at least N distict values of the class denoted by
     * 'hasClassQ' for the property.
     *
     * @return Property accessor for 'minCardinalityQ'
     */
    public IntLiteralAccessor prop_minCardinalityQ();


    /**
     * Property accessor for the 'maxCardinalityQ' property of a restriction. This denotes
     * the class of instances that have at most N distict values of the class denoted by
     * 'hasClassQ' for the property.
     *
     * @return Property accessor for 'maxCardinalityQ'
     */
    public IntLiteralAccessor prop_maxCardinalityQ();

}
