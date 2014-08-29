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
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A lexical handler that does nothing.
 */
class LexicalHandlerImpl extends DefaultHandler implements LexicalHandler {

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	@Override
    public void endCDATA() throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	@Override
    public void endDTD() throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	@Override
    public void startCDATA() throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	@Override
    public void comment(char[] ch, int start, int length) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	@Override
    public void endEntity(String name) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	@Override
    public void startEntity(String name) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
    public void startDTD(String name, String publicId, String systemId)
			throws SAXException {
	}

}
