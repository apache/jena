/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            17 Sept 2001
 * Filename           $RCSfile: DAMLDataInstance.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:04:58 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////
import com.hp.hpl.jena.datatypes.RDFDatatype;



/**
 * <p>A data instance is a specific type of DAML object that represents the instantiation
 * of a DAML datatype. The instance is a resource whose <code>rdf:value</code> is a typed literal.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLDataInstance.java,v 1.7 2005-02-21 12:04:58 andy_seaborne Exp $
 */
public interface DAMLDataInstance
    extends DAMLInstance
{
    // Constants
    //////////////////////////////////



    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the typed value translator for values encoded by the datatype of this
     * instance.</p>
     *
     * @return The datatype translator defined for the <code>rdf:type</code> of this instance
     */
    public RDFDatatype getDatatype();


    /**
     * <p>Answer the value of this instance as a Java object, translated from the
     * serialised RDF representation by the Dataype's type mapper.</p>
     *
     * @return The value of this instance, or null if either the translator or the
     *         serialised value is defined
     */
    public Object getValue();


    /**
     * <p>Set the value of this instance to the given Java value, which will be
     * serialised into the RDF graph by the datatype's translator.</p>
     */
    public void setValue( Object value );

}


/*
    (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

