/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLInstance.java,v $
 * Revision           $Revision: 1.6 $
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


import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;



/**
 * Java representation of a DAML Instance.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLInstance.java,v 1.6 2004-12-06 13:50:18 andy_seaborne Exp $
 */
public interface DAMLInstance
    extends DAMLCommon
{
    // Constants
    //////////////////////////////////



    // External signature methods
    //////////////////////////////////

    /**
     * Property accessor for <code>daml:sameIndividualAs</code> property on a DAML instance.
     *
     * @return a property accessor
     */
    public PropertyAccessor prop_sameIndividualAs();


    /**
     * Return an iterator over all of the instances that are the same as this one,
     * by generating the transitive closure over the <code>daml:samePropertyAs</code>
     * property.
     *
     * @return an iterator whose values will all be DAMLInstance objects
     */
    public ExtendedIterator getSameInstances();


    /**
     * Answer an iterator over all of the DAML instances that are equivalent to this
     * instance, which will be the union of <code>daml:equivalentTo</code> and
     * <code>daml:sameIndividualAs</code>.
     *
     * @return an iterator ranging over every equivalent DAML instance - each value of
     *         the iteration should be a DAMLInstance object.
     */
    public ExtendedIterator getEquivalentValues();


    /**
     * Answer a property accessor for a user defined property.
     *
     * @param property An RDF or DAML property
     * @return a property accessor, that simplifies some of the basic operations
     *         of a given property on a given object
     */
    public PropertyAccessor accessProperty( Property property );

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

