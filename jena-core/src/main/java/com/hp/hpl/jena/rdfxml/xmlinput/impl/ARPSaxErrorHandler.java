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

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
/**
 * This class is not part of the API.
 * It is public merely for test purposes.
 */
public class ARPSaxErrorHandler extends Object implements org.xml.sax.ErrorHandler {
    protected RDFErrorHandler errorHandler;
    
    public ARPSaxErrorHandler(RDFErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    @Override
    public void error(SAXParseException e) throws SAXException {
        errorHandler.error(e);
    }
    
    @Override
    public void warning(SAXParseException e) throws SAXException {
        errorHandler.warning(e);
    }
    
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        errorHandler.fatalError(e);
    }

	/**
	 * @param errorHandler The errorHandler to set.
	 */
	public void setErrorHandler(RDFErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

}
