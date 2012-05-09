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
