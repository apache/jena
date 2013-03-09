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
    
    /** The root name for metadata */
    
    public static final String MPATH = "org.apache.jena";
    static final String UNSET = "unset" ;
    
    /** The product name */    
    public static final String NAME = JenaRuntime.getMetadata( MPATH + ".name", UNSET ) ;
    
    /** The Jena web site */    
    public static final String WEBSITE = JenaRuntime.getMetadata( MPATH + ".website", UNSET ) ;
    
    /** The full name of the current Jena version */    
    public static final String VERSION = JenaRuntime.getMetadata( MPATH + ".version", UNSET ) ;
    
    /** The date and time at which this release was built */    
    public static final String BUILD_DATE = JenaRuntime.getMetadata( MPATH + ".build.datetime", UNSET ) ;
}
