/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            28-Apr-2003
 * Filename           $RCSfile: BooleanClassDescription.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-21 12:35:39 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

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
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: BooleanClassDescription.java,v 1.6 2003-06-21 12:35:39 ian_dickinson Exp $
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
	 * @exception OntProfileException If the operand property is not supported in the current language profile.   
	 */ 
	public void setOperands( RDFList operands );

	/**
	 * <p>Add a class the operands of this boolean expression.</p>
	 * @param cls A class that will be added to the operands of this Boolean expression
	 * @exception OntProfileException If the operand property is not supported in the current language profile.   
	 */ 
	public void addOperand( Resource cls );

	/**
	 * <p>Add all of the classes from the given iterator to the operands of this boolean expression.</p>
	 * @param cls A iterator over classes that will be added to the operands of this Boolean expression
	 * @exception OntProfileException If the operand property is not supported in the current language profile.   
	 */ 
	public void addOperands( Iterator classes );

	/**
	 * <p>Answer the list of operands for this Boolean class expression.</p>
	 * @return A list of the operands of this expression.
	 * @exception OntProfileException If the operand property is not supported in the current language profile.   
	 */ 
	public RDFList getOperands();

	/**
	 * <p>Answer an iterator over all of the clases that are the operands of this 
	 * Boolean class expression. Each element of the iterator will be an {@link OntClass}.</p>
	 * @return An iterator over the operands of the expression.
	 * @exception OntProfileException If the operand property is not supported in the current language profile.   
	 */ 
	public ExtendedIterator listOperands();

	/**
	 * <p>Answer true if this Boolean class expression has the given class as an operand.</p>
	 * @param cls A class to test 
	 * @return True if the given class is an operand to this expression.
	 * @exception OntProfileException If the operand property is not supported in the current language profile.   
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


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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


