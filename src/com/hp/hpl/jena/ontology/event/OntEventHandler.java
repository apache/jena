/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            09-Sep-2003
 * Filename           $RCSfile: OntEventHandler.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-08-13 16:14:12 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.event;



// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * A callback class modelled on Command Patern, that is 
 * intended to perform some processing when a particular change to
 * an ontology model is detected. The event types themselves are
 * modelled via a simple vocabulary generated from an OWL document
 * (see ont-event.owl).  
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntEventHandler.java,v 1.3 2004-08-13 16:14:12 ian_dickinson Exp $
 */
public interface OntEventHandler 
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Handle the occurrence of the ontology event denoted by the given
     * event code.  Standard OWL event contstants are defined in 
     * {@linkplain com.hp.hpl.jena.vocabulary.OntEventsVocab the Ontology events vocabulary}.
     * </p>
     * 
     * @param event The event code as a resource
     * @param add This event represents an addition to the model if true, or a deletion from
     * the model if false.
     * @param source The model that was the source of the event
     * @param arg0 The first argument of the event; typically principal subject of the change
     * @param arg1 Optional second argument to the event, or null
     * @param arg2 Optional third argument to the event, or null
     */
    public void action( Resource event, boolean add, Model source, RDFNode arg0, RDFNode arg1, RDFNode arg2 );
}


/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 */
