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

/*
 * ResourceReader.java
 *
 * Created on June 29, 2001, 9:54 PM
 */

package com.hp.hpl.jena.regression;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
/**
 * read a data file stored on the class path.
 * To use this, ensure your data is on the class path (e.g. in the
 * program jar, or in a separate data.jar), and give a relative path
 * name to the data.
 * Not intended for an applet environment.
 * 
 * @author  jjc
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2009-06-29 08:55:39 $'
 */
class ResourceReader  {
    // If false use FileInputSDtream's assuming we are in the correct directory;
    static boolean useClassLoader = false;
    /** Creates new ResourceReader 
     * @param resource The filename of the data file relative to the Java classpath.
     * @exception java.lang.SecurityException If cannot access the classloader, e.g. in applet.
     * @exception java.lang.IllegalArgumentException If file not found.
     */
    private ResourceReader() {}
    /*
    public ResourceReader(String resource) throws IOException {
        super(getInputStream(resource));
    }
    */
    
    static InputStream getInputStream(String prop) throws IOException {
        if ( useClassLoader) {
            ClassLoader loader = ResourceReader.class.getClassLoader();
            if ( loader == null ) 
                throw new SecurityException("Cannot access class loader");
            InputStream in = loader.getResourceAsStream(prop);
            if ( in == null )
                throw new IllegalArgumentException("Resource: " + prop + " not found on class path.");
            return in;
        } else {
            return new FileInputStream(prop);
        }
    }

}
