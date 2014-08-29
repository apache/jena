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

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdfxml.xmlinput.ParseException ;
import com.hp.hpl.jena.shared.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * The default error handler for I/O.
 * This uses log4j as its utility.
 */
public class RDFDefaultErrorHandler extends Object implements RDFErrorHandler {

	/**
	 * Change this global to make all RDFDefaultErrorHandler's silent!
	 * Intended for testing purposes only.
	 */
	public static boolean silent = false;
	
    public static final Logger logger = LoggerFactory.getLogger( RDFDefaultErrorHandler.class );
    
    /** Creates new RDFDefaultErrorHandler */
    public RDFDefaultErrorHandler() {
    }

    @Override
    public void warning(Exception e) {
        if (!silent) logger.warn(ParseException.formatMessage(e));
    }

    @Override
    public void error(Exception e) {
    	if (!silent) logger.error(ParseException.formatMessage(e));
    }

    @Override
    public void fatalError(Exception e) {
    	if (!silent) logger.error(ParseException.formatMessage(e));
        throw e instanceof RuntimeException 
            ? (RuntimeException) e
            : new JenaException( e );
    }
}
