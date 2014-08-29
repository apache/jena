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
 * DefaultErrorHandler.java
 *
 * Created on July 10, 2001, 11:23 AM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import com.hp.hpl.jena.rdfxml.xmlinput.ParseException ;

/**
 */
public class DefaultErrorHandler implements org.xml.sax.ErrorHandler {

    /** Creates new DefaultErrorHandler */
    public DefaultErrorHandler() {
        // no initialization
    }

    @Override
    public void error(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        System.err.println("Error: " + ParseException.formatMessage(e)); 
    }
    
    @Override
    public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        System.err.println("Fatal Error: " + ParseException.formatMessage(e));
        throw e;
    }
    
    @Override
    public void warning(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        System.err.println("Warning: " + ParseException.formatMessage(e)); 
//        e.printStackTrace();
        
    }
    
}
