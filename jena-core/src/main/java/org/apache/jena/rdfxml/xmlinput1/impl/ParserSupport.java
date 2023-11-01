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
 * S.java
 *
 * Created on July 15, 2001, 7:13 AM
 */

package org.apache.jena.rdfxml.xmlinput1.impl;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.irix.IRIx;
import org.apache.jena.rdfxml.xmlinput1.ARPErrorNumbers;
import org.apache.jena.util.XML11Char;
import org.apache.jena.util.XMLChar;
import org.xml.sax.SAXParseException ;

public class ParserSupport implements ARPErrorNumbers, Names {

	protected ParserSupport(XMLHandler arp, AbsXMLContext xml) {
		this.arp = arp;
        this.xml= xml;
	}
    Map<IRIx, Map<String,ARPLocation>> idsUsed() {
        return arp.idsUsed;
    }
    protected final XMLHandler arp;
    public final AbsXMLContext xml;
	/**
	 * @param str The fully expanded URI
	 */
	protected void checkIdSymbol(Taint taintMe, AbsXMLContext ctxt, String str)
		throws SAXParseException {
		if (arp.idsUsed != null) {
			IRIx uri = ctxt.uri;
            Map<String,ARPLocation> idsUsedForBase = idsUsed().get(uri);
			if (idsUsedForBase == null) {
				idsUsedForBase = new HashMap<>();
				idsUsed().put(uri, idsUsedForBase);
			}
			ARPLocation prev = idsUsedForBase.get(str);
			if (prev != null) {
				arp.warning(taintMe,
					WARN_REDEFINITION_OF_ID,
					// RIOT format [line: 22, col: 31]
					"Redefinition of ID: '" + str +"' defined at "+prev.locationStr());
				// ARP used to produce two warnings.
//				arp.warning(taintMe,
//					WARN_REDEFINITION_OF_ID,
//					prev,
//					"Previous definition of '" + str + "'.");
			} else {
				idsUsedForBase.put(str, arp.location());
				arp.idsUsedCount++;
				if (arp.idsUsedCount > 10000) {
					arp.idsUsed = null;
				arp.warning(taintMe,
						WARN_BIG_FILE,
						"Input is large. Switching off checking for illegal reuse of rdf:ID's.");
				}
			}
		}

		checkID_XMLName(taintMe,str);
		checkEncoding(taintMe,str);
	}

	protected void checkNodeID_XMLName( Taint taintMe, String str) throws SAXParseException {
	    if ( ! XMLChar.isValidNCName(str) ) {
            warning(taintMe,
                WARN_BAD_NAME,
                "Not an XML Name: '" + str + "'");
        }
	}

	protected void checkID_XMLName( Taint taintMe, String str) throws SAXParseException {
	    //if (!XMLChar.isValidNCName(str)) {
		if ( ! XML11Char.isXML11ValidNCName(str) ) {
			warning(taintMe,
				WARN_BAD_NAME,
				"Not an XML Name: '" + str + "'");
		}
	}

	public void checkString(Taint taintMe,String t) throws SAXParseException {
		if (!CharacterModel.isNormalFormC(t))
			warning(taintMe,
				WARN_STRING_NOT_NORMAL_FORM_C,
				"String not in Unicode Normal Form C: \"" + t +"\"");
		checkEncoding(taintMe,t);
		checkComposingChar(taintMe,t);
	}
	void checkComposingChar(Taint taintMe,String t) throws SAXParseException {
		if (CharacterModel.startsWithComposingCharacter(t))
			warning(taintMe,
				WARN_STRING_COMPOSING_CHAR,
				"String is not legal in XML 1.1; starts with composing char: \""
					+ t
					+ "\" (" + ((int)t.charAt(0))+ ")");
	}
    public void checkComposingChar(Taint taintMe,char ch[], int st, int ln) throws SAXParseException {
        if (ln>0 && CharacterModel.isComposingChar(ch[st]))
            warning(taintMe,
                WARN_STRING_COMPOSING_CHAR,
                "String is not legal in XML 1.1; starts with composing char: \""
                    + new String(ch,st,ln)
                    + "\" (" + (int)ch[st]+ ")");
    }

	public void checkEncoding(Taint taintMe, String s) throws SAXParseException {
		if (arp.encodingProblems) {
			for (int i = s.length() - 1; i >= 0; i--) {
				if (s.charAt(i) < 0 || s.charAt(i)> 127) {
					warning(taintMe,
						ERR_ENCODING_MISMATCH,
						"Encoding error with non-ascii characters.");
					break;
				}
			}
		}
	}

	/**
     * whether this is a warning or an error is determined later.
     * @param i
     * @param msg
     */
    protected void warning(Taint taintMe, int i, String msg) throws SAXParseException {
        arp.warning(taintMe, i,msg);
    }

    protected boolean isWhite(char ch[], int st, int ln) {
        for (int i=0;i<ln;i++)
            if (! isWhite(ch[st+i]) )
                return false;
         return true;
    }

    protected boolean isWhite(StringBuffer buf) {
        for (int i=buf.length()-1;i>=0;i--)
           if (! isWhite(buf.charAt(i)) )
               return false;
        return true;
    }

    private boolean isWhite(char c) {
        switch (c) {
            case '\n' :
            case '\r' :
            case '\t' :
            case ' ' :
                return true;
            default :
                return false;
        }
    }

    protected void triple(ANode a, ANode b, ANode c) {
        arp.triple(a,b,c);
    }

    public AbsXMLContext getXMLContext() {
        return xml;
    }

    public XMLHandler getXMLHandler() {
        return arp;
    }

    protected String resolve(Taint taintMe,AbsXMLContext x, String uri) throws SAXParseException {
        IRIx ref = x.resolveAsURI(arp,taintMe,uri);
        return ref.toString();
    }
}
