/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            17 Sept 2001
 * Filename           $RCSfile: DAMLDataInstanceImpl.java,v $
 * Revision           $Revision: 1.10 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-08-27 13:04:45 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.*;

import java.util.Iterator;

import org.apache.log4j.Logger;



/**
 * <p>A data instance is a specific type of DAML object that represents the instantiation
 * of a DAML datatype. The instance is a resource whose <code>rdf:value</code> is a typed literal.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLDataInstanceImpl.java,v 1.10 2003-08-27 13:04:45 andy_seaborne Exp $
 */
public class DAMLDataInstanceImpl
    extends DAMLInstanceImpl
    implements DAMLDataInstance
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating DAMLDataInstance facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new DAMLDataInstanceImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to DAMLDataInstance" );
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            return eg.asGraph().contains( node, RDF.type.asNode(), Node.ANY );
        }
    };

    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a DAML data instance represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public DAMLDataInstanceImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////


    /**
     * <p>Answer the typed value translator for values encoded by the datatype of this
     * instance.</p>
     *
     * @return The datatype translator defined for the <code>rdf:type</code> of this instance
     */
    public RDFDatatype getDatatype() {
        // search for an RDF type that we have a translator for
        for (Iterator i = listRDFTypes( true ); i.hasNext(); ) {
            Resource rType = (Resource) i.next();
            if (rType.isAnon()) {
                continue;
            }
            
            RDFDatatype dt = TypeMapper.getInstance().getTypeByName( rType.getURI() );
            
            if (dt != null) {
                // found a candidate datatype
                if (i instanceof ClosableIterator) {
                    ((ClosableIterator) i).close();
                }
                
                return dt;
            }
        }
        
        return null;
    }


    /**
     * <p>Answer the value of this instance as a Java object, translated from the
     * serialised RDF representation by the Dataype's type mapper.</p>
     *
     * @return The value of this instance, or null if either the translator or the
     *         serialised value is defined
     */
    public Object getValue() {
        if (hasProperty( RDF.value )) {
            RDFDatatype dType = getDatatype();
            
            if (dType == null) {
                Logger.getLogger( getClass() ).warn( "No RDFDatatype defined for DAML data instance " + this );
            }
            else {
                return dType.parse( getRequiredProperty( RDF.value ).getString() );
            }
        }
        
        return null;
    }


    /**
     * <p>Set the value of this instance to the given Java value, which will be
     * serialised into the RDF graph by the datatype's translator.</p>
     * 
     * @param value The value to be encoded as a typed literal
     */
    public void setValue( Object value ) {
        setPropertyValue( RDF.value, "", getModel().createTypedLiteral( value, getDatatype() ) );
    }



    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}


/*
    (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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

