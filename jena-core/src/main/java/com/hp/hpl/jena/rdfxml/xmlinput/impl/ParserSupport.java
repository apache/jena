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

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.util.XMLChar;
import org.xml.sax.SAXParseException;
import org.apache.jena.iri.IRI;

import com.hp.hpl.jena.rdfxml.xmlinput.ARPErrorNumbers ;
import com.hp.hpl.jena.rdfxml.xmlinput.lang.LanguageTagCodes ;

/**
 */
public class ParserSupport
	implements ARPErrorNumbers,  LanguageTagCodes, Names {
    
//    protected void checkBadURI(Taint taintMe,RDFURIReference uri) throws SAXParseException {
//        arp.checkBadURI(taintMe,uri);
//    }
    
	protected ParserSupport(XMLHandler arp, AbsXMLContext xml) {
		this.arp = arp;
        this.xml= xml;
	}
    Map<IRI, Map<String,ARPLocation>> idsUsed() {
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
			IRI uri = ctxt.uri;
            Map<String,ARPLocation> idsUsedForBase = idsUsed().get(uri);
			if (idsUsedForBase == null) {
				idsUsedForBase = new HashMap<>();
				idsUsed().put(uri, idsUsedForBase);
			}
			ARPLocation prev = idsUsedForBase.get(str);
			if (prev != null) {
				arp.warning(taintMe,
					WARN_REDEFINITION_OF_ID,
					"Redefinition of ID: " + str);
				arp.warning(taintMe,
					WARN_REDEFINITION_OF_ID,
					prev,
					"Previous definition of '" + str + "'.");
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


		checkXMLName(taintMe,str);
		checkEncoding(taintMe,str);
	}
	protected void checkXMLName( Taint taintMe, String str) throws SAXParseException {
		if (!XMLChar.isValidNCName(str)) {
			//   	System.err.println("not name (id): " + str);
			warning(taintMe,
				WARN_BAD_NAME,
				"Not an XML Name: '" + str + "'");
		}

	}
//	protected void checkNodeID(Taint taintMe, String str) throws SAXParseException {
//		if (!XMLChar.isValidNCName(str)) {
//			warning(taintMe,
//				WARN_BAD_NAME,
//				"Not an XML Name: '" + str + "'");
//		}
//	}
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

	
//	public void checkXMLLang(Taint taintMe, String lang) throws SAXParseException {
//		if (lang.equals(""))
//			return;
//		try {
//			LanguageTag tag = new LanguageTag(lang);
//			int tagType = tag.tagType();
//			if (tagType == LT_ILLEGAL) {
//				warning(taintMe,
//					WARN_BAD_XMLLANG,
//					tag.errorMessage());
//			}
//			if ((tagType & LT_UNDETERMINED) == LT_UNDETERMINED) {
//				warning(taintMe,
//					WARN_BAD_XMLLANG,
//					"Unnecessary use of language tag \"und\" prohibited by RFC3066");
//			}
//			if ((tagType & LT_IANA_DEPRECATED) == LT_IANA_DEPRECATED) {
//				warning(taintMe,
//					WARN_DEPRECATED_XMLLANG,
//					"Use of deprecated language tag \"" + lang + "\".");
//			}
//			if ((tagType & LT_PRIVATE_USE) == LT_PRIVATE_USE) {
//				warning(taintMe,
//					IGN_PRIVATE_XMLLANG,
//					"Use of (IANA) private language tag \"" + lang + "\".");
//			} else if ((tagType & LT_LOCAL_USE) == LT_LOCAL_USE) {
//				warning(taintMe,
//					IGN_PRIVATE_XMLLANG,
//					"Use of (ISO639-2) local use language tag \""
//						+ lang
//						+ "\".");
//			} else if ((tagType & LT_EXTRA) == LT_EXTRA) {
//				warning(taintMe,
//					IGN_PRIVATE_XMLLANG,
//					"Use of additional private subtags on language \""
//						+ lang
//						+ "\".");
//			}
//		} catch (LanguageTagSyntaxException e) {
//			warning(taintMe,
//				WARN_MALFORMED_XMLLANG,
//				e.getMessage());
//		}
//	}


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
        IRI ref = x.resolveAsURI(arp,taintMe,uri);
//        checkBadURI(taintMe,ref);
        return ref.toString();
    }

}
