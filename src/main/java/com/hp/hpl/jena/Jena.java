/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            01-Aug-2003
 * Filename           $RCSfile: Jena.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2009-06-29 14:46:43 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena;


// Imports
///////////////


/**
 * <p>
 * Provides various meta-data constants about the Jena package.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version Version 2.6.2-dev, CVS $Id: Jena.java,v 1.2 2009-06-29 14:46:43 andy_seaborne Exp $
 */
public interface Jena
{
    // Constants
    //////////////////////////////////


    /** The root package name for Jena */    
    public static final String PATH = "com.hp.hpl.jena";
    
    /** The product name */    
    public static final String NAME = "Jena";
    
    /** The Jena web site */    
    public static final String WEBSITE = "http://jena.sourceforge.net/";
    
    /** The full name of the current Jena version */    
    public static final String VERSION = "2.6.2-dev";
    
    /** The major version number for this release of Jena (ie '2' for Jena 2.6.0) */
    public static final String MAJOR_VERSION = "2";
    
    /** The minor version number for this release of Jena (ie '6' for Jena 2.6.0) */
    public static final String MINOR_VERSION = "6";
    
    /** The minor version number for this release of Jena (ie '0' for Jena 2.6.0) */
    public static final String REVISION_VERSION = "2";
    
    /** The version status for this release of Jena (eg '-beta1' or the empty string) */
    public static final String VERSION_STATUS = "-dev";
    
    /** The date and time at which this release was built */    
    public static final String BUILD_DATE = "29-June-2009 15:01";
    

    // External signature methods
    //////////////////////////////////

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
