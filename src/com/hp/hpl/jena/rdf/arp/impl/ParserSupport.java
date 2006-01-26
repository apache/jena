/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 * * $Id: ParserSupport.java,v 1.10 2006-01-26 14:33:35 jeremy_carroll Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * S.java
 *
 * Created on July 15, 2001, 7:13 AM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.util.XMLChar;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTagCodes;

/**
 *
 * @author  jjc
 * 
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
    Map idsUsed() {
        return arp.idsUsed;
    }
    protected final XMLHandler arp;
    public final AbsXMLContext xml;
	/**
	 * @param str The fully expanded URI
	 */
	protected void checkIdSymbol(Taint taintMe, AbsXMLContext ctxt, String str)
		throws SAXParseException {
		if (!arp.ignoring(WARN_REDEFINITION_OF_ID)) {
			IRI uri = ctxt.uri;
            Map idsUsedForBase = (Map) idsUsed().get(uri);
			if (idsUsedForBase == null) {
				idsUsedForBase = new HashMap();
				idsUsed().put(uri, idsUsedForBase);
			}
			Location prev = (Location) idsUsedForBase.get(str);
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
				if (s.charAt(i) > 127)
					warning(taintMe,
						ERR_ENCODING_MISMATCH,
						"Encoding error with non-ascii characters.");
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
