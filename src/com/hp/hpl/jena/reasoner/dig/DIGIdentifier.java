/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            11-Sep-2003
 * Filename           $RCSfile: DIGIdentifier.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-02 10:02:56 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;



// Imports
///////////////
import java.util.Iterator;


/**
 * <p>
 * A structure that presents identification information about the attached DIG reasoner.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGIdentifier.java,v 1.2 2003-12-02 10:02:56 ian_dickinson Exp $)
 */
public interface DIGIdentifier 
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the name of the attached reasoner, as a string.</p>
     * @return The name of the DIG reasoner.
     */
    public String getName();
    
    /**
     * <p>Answer the version string of the attached reasoner.</p>
     * @return The version string for the reasoner.
     */
    public String getVersion();
    
    /**
     * <p>Answer the message string from the DIG identifier element.</p>
     * @return The identification message
     */
    public String getMessage();
    
    /**
     * <p>Answer an iterator over the language elements that this reasoner supports.</p>
     * @return An iterator, each element of which is a string denoting a DIG language
     * term that the attached reasoner supports.
     */
    public Iterator supportsLanguage();
    
    /**
     * <p>Answer an iterator over the TELL verbs that this reasoner supports.</p>
     * @return An iterator, each element of which is a string denoting a DIG TELL
     * verb that the attached reasoner supports.
     */
    public Iterator supportsTell();
    
    /**
     * <p>Answer an iterator over the ASK verbs that this reasoner supports.</p>
     * @return An iterator, each element of which is a string denoting a DIG ASK
     * verb that the attached reasoner supports.
     */
    public Iterator supportsAsk();
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
