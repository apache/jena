/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena;

/**
 * <p>
 * Provides various meta-data constants about the Jena package.
 * </p>
 */
public interface Jena
{
	/** The root package name for Jena */    
    public static final String PATH = "com.hp.hpl.jena";
    static final String UNSET = "unset" ;
    
    /** The product name */    
    public static final String NAME = JenaRuntime.getMetadata( PATH + ".name", UNSET ) ;
    
    /** The Jena web site */    
    public static final String WEBSITE = JenaRuntime.getMetadata( PATH + ".website", UNSET ) ;
    
    /** The full name of the current Jena version */    
    public static final String VERSION = JenaRuntime.getMetadata( PATH + ".version", UNSET ) ;
    
    /** The date and time at which this release was built */    
    public static final String BUILD_DATE = JenaRuntime.getMetadata( PATH + ".build.datetime", UNSET ) ;

    /** @deprecated See the VERSION constant */ 
    @Deprecated 
    public static final String MAJOR_VERSION = "unset" ;
    
    /** @deprecated See the VERSION constant */ 
    @Deprecated 
    public static final String MINOR_VERSION = "unset" ;

    /** @deprecated See the VERSION constant */ 
    @Deprecated 
    public static final String REVISION_VERSION = "unset" ;
    
    /** @deprecated See the VERSION constant */ 
    @Deprecated 
    public static final String VERSION_STATUS = "unset" ;
    
//    /** The major version number for this release of Jena (ie '2' for Jena 2.6.0) */
//    public static final String MAJOR_VERSION = metadata.get ( PATH + ".version.major", UNSET ) ;
//    
//    /** The minor version number for this release of Jena (ie '6' for Jena 2.6.0) */
//    public static final String MINOR_VERSION = metadata.get ( PATH + ".version.minor", UNSET ) ;
//    
//    /** The minor version number for this release of Jena (ie '0' for Jena 2.6.0) */
//    public static final String REVISION_VERSION = metadata.get ( PATH + ".version.revision", UNSET ) ;
//    
//    /** The version status for this release of Jena (eg '-beta1' or the empty string) */
//    public static final String VERSION_STATUS = metadata.get ( PATH + ".version.status", UNSET ) ;
    
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 */