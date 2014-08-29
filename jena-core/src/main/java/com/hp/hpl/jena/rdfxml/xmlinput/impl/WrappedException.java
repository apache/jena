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
 * WrappedException.java
 *
 * Created on July 10, 2001, 11:44 AM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import java.io.IOException;

import org.xml.sax.SAXException;
/**
 * Wrap some other exception - being wise to SAXExceptions which
 * wrap something else.
 */
class WrappedException extends java.lang.RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -4058658905253070902L;
    /** Creates new WrappedException */
    WrappedException(SAXException e) {
        Exception in0 = e.getException();
        if ( in0 == null ) {
            initCause(e);
            return;
        }
        if ( (in0 instanceof RuntimeException) 
             || (in0 instanceof SAXException )
             || (in0 instanceof IOException ) )
            {
            initCause(in0);
            return;
        }
        initCause(e);
    }
//    WrappedException(IOException e) {
//        initCause(e);
//    }
    /** Throw the exception,  falling back to be a wrapped SAXParseException.
     */
    void throwMe() throws IOException, SAXException {
        Throwable inner = this.getCause();
        if ( inner instanceof SAXException ) {
            throw (SAXException)inner;
        }  
        if ( inner instanceof IOException ) {
            throw (IOException)inner;
        }
        if ( inner instanceof RuntimeException ) {
            throw (RuntimeException)inner;
        }
        // I don't think this line is reachable:
        throw new RuntimeException("Supposedly unreacahble code.");
    }
    

}
