/*
 *  (c) Copyright Hewlett-Packard Company 2001 
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
 
 * * $Id: ParserSupport.java,v 1.2 2003-02-21 13:28:12 jeremy_carroll Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * S.java
 *
 * Created on July 15, 2001, 7:13 AM
 */

package com.hp.hpl.jena.rdf.arp;

import java.util.*;

import com.hp.hpl.jena.rdf.arp.lang.LanguageTag;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTagSyntaxException;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTagCodes;


import
org.apache.xerces.util.XMLChar;



/**
 *
 * @author  jjc
 * 
 */
class ParserSupport implements ARPErrorNumbers, RDFParserConstants, LanguageTagCodes {
   Map idsUsed = new HashMap();
    ParserSupport(ARPFilter arp) {
        this.arp = arp;
    }
    ARPFilter arp;
   void checkWhite(StrToken st) throws ParseException {
       String s = st.value;
       int lgth = s.length();
       int from = 0;
        // See XML specs for defn of whitespace.
        // Section 2.3
        // [3]    S    ::=    (#x20 | #x9 | #xD | #xA)+
        while ( from < lgth ) {
            switch (s.charAt(from++)) {
                case '\n':
                case '\r':
                case '\t':
                case ' ':
                    continue;
                    default:
                        throw new ParseException(ERR_NOT_WHITESPACE,st.location,
                          "Expected whitespace found: '" + s + "'."
                          );
            }
        }
   }
/**
 * @param str The fully expanded URI
 */
   void checkIdSymbol(XMLContext ctxt, StrToken s, String str) throws ParseException {
   	  Location prev = (Location)idsUsed.get(str);
      if ( prev != null ) {
        arp.parseWarning(WARN_REDEFINITION_OF_ID,s.location,"Redefinition of ID: " + s.value);
        arp.parseWarning(WARN_REDEFINITION_OF_ID,prev,"Previous definition of '" + s.value + "'.");
      } else {
        idsUsed.put(str,s.location);
    	prev = (Location)idsUsed.get(s.value);
    	if ( prev!=null )
           arp.parseWarning(WARN_LEGAL_REUSE_OF_ID,s.location,"The ID: " + s.value+ " is reused in different xml:base contexts; this may be confusing.");
        idsUsed.put(s.value,s.location);
      }
      if ( !ctxt.isSameAsDocument() )
        arp.parseWarning(IGN_XMLBASE_SIGNIFICANT,s.location,"The use of xml:base changes the meaning of ID '" + s.value+ "'.");
        
      checkXMLName(s,s.value);
   }
   private void checkXMLName(StrToken s,String str) throws ParseException {
      if (!XMLChar.isValidNCName(str)) {
   //   	System.err.println("not name (id): " + str);
         arp.parseWarning(WARN_BAD_NAME,s.location,"Not an XML Name: '"+str+"'");
      }
  
  //    if ( str.indexOf(':') != -1 )
  //       arp.parseWarning(WARN_QNAME_AS_ID,s.location,"Colon found in ID or bagID: '"+str+"'");
   }
   String checkNodeID(Token s) throws ParseException {
      String str = ((StrToken)s).value;
      if (!XMLChar.isValidNCName(str)) {
   //   	System.err.println("not name: " + str);
         arp.parseWarning(WARN_BAD_NAME,s.location,"Not an XML Name: '"+str+"'");
      }
      return str;
   }
   void checkString(Token t)  throws ParseException {
   	 if (!CharacterModel.isNormalFormC(((StrToken)t).value)) 
         arp.parseWarning(ERR_STRING_NOT_NORMAL_FORM_C,t.location,"String not in Unicode Normal Form C: " + ((StrToken)t).value);
     checkComposingChar(t);
   }
   void checkComposingChar(Token t)  throws ParseException {
   	 if (CharacterModel.startsWithComposingCharacter(((StrToken)t).value)) 
         arp.parseWarning(WARN_STRING_COMPOSING_CHAR,t.location,"String is not legal in XML 1.1; starts with composing char: " + ((StrToken)t).value);
   }
   void checkNormalFormC(Token t, ARPString str) throws ParseException  {
    if (!CharacterModel.isNormalFormC(str.toString())) 
         arp.parseWarning(ERR_STRING_NOT_NORMAL_FORM_C,t.location,"String not in Unicode Normal Form C: " + str.toString());
    }

   void processingInstruction(Token t) throws ParseException {
     arp.parseWarning(WARN_PROCESSING_INSTRUCTION_IN_RDF,t.location,
             "A processing instruction is in RDF content. No processing was done.");
   }
   void saxException(Token t) throws ParseException {
    SaxExceptionToken sax = (SaxExceptionToken)t;
    arp.parseWarning(sax.errorCode,t.location,sax.toString());
   }
   void checkXMLLang(StrToken s) throws ParseException {
       String lang = s.value;
       try {
           LanguageTag tag = new LanguageTag(lang);
           int tagType = tag.tagType();
           if ( tagType == LT_ILLEGAL ) {
               arp.parseWarning(WARN_BAD_XMLLANG,s.location,tag.errorMessage());
           }
           if ( (tagType & LT_UNDETERMINED) == LT_UNDETERMINED ) {
                   arp.parseWarning(WARN_BAD_XMLLANG,s.location,"Unnecessary use of language tag \"und\" prohibited by RFC3066");
           }
           if ( (tagType & LT_IANA_DEPRECATED) == LT_IANA_DEPRECATED ) {
                   arp.parseWarning(WARN_DEPRECATED_XMLLANG,s.location,"Use of deprecated language tag \""+lang+"\".");
           }
           if ( (tagType & LT_PRIVATE_USE )  == LT_PRIVATE_USE ) {
                   arp.parseWarning(IGN_PRIVATE_XMLLANG,s.location,"Use of (IANA) private language tag \""+lang+"\".");
           } else if ( (tagType & LT_LOCAL_USE)  == LT_LOCAL_USE ) {
                   arp.parseWarning(IGN_PRIVATE_XMLLANG,s.location,"Use of (ISO639-2) local use language tag \""+lang+"\".");
           }else if ( (tagType & LT_EXTRA)  == LT_EXTRA ) {
                   arp.parseWarning(IGN_PRIVATE_XMLLANG,s.location,"Use of additional private subtags on language \""+lang+"\".");
           }
       }
       catch ( LanguageTagSyntaxException e ) {
           arp.parseWarning(WARN_MALFORMED_XMLLANG,s.location,e.getMessage());
       }
   }
   
   private String truncateXMLBase(StrToken s) {
      return truncateXMLBase(s.value);
   }
   static String truncateXMLBase(String rslt) {
      int hash = rslt.indexOf('#');
      if ( hash != -1 ) {
           return rslt.substring(0,hash);
      }
      return rslt;
   }
   XMLContext changeXMLBase(XMLContext ctxt, Token t)  
                         throws ParseException {
      arp.parseWarning(IGN_XMLBASE_USED,t.location,
           "Use of attribute xml:base is not envisaged in RDF Model&Syntax.");
      StrToken base = ((StrToken)t);
 //     String bb = URIref.encode(truncateXMLBase(base));
      String bb = truncateXMLBase(base);
     try {
        ctxt = ctxt.withBase(bb);
      }
      catch (MalformedURIException mal) {
        arp.parseWarning(WARN_MALFORMED_URI,t.location,"Bad URI <" + ((StrToken)t).value +">: " + mal.getMessage());
        ctxt = ctxt.revertToDocument();
      }
      return ctxt;
   }
   URIReference makeURIReference(XMLContext ctxt, Token t) 
                         throws ParseException { 
     StrToken s = (StrToken)t;
//         String val = URIref.encode(s.value);
         String val = s.value;
      if ( !CharacterModel.isNormalFormC(val) )
          arp.parseWarning(ERR_URI_NOT_NORMAL_FORM_C,t.location,
              "<" + val + "> not in Unicode Normal Form C.");
      boolean composing =
            CharacterModel.startsWithComposingCharacter(val);
      if ( composing ) {
    //  	System.err.println(val);
    //  	System.err.println((int)val.charAt(0));
          arp.parseWarning(WARN_URI_COMPOSING_CHAR,t.location,
              "Relative URI reference <" + val + "> starts with composing char.");
      }
     try {
         URIReference rslt = new URIReference(ctxt,val);
         if ( composing && !CharacterModel.isNormalFormC(rslt.getURI()) )
          arp.parseWarning(ERR_URI_NOT_NORMAL_FORM_C,t.location,
              "<" + rslt.getURI() + "> not in Unicode Normal Form C.");
         if ( val.indexOf(':') == -1 ) {
         	if ( (!ctxt.getURI().isNormalFormC())
         	      &&  (!composing)
         	      && !CharacterModel.isNormalFormC(rslt.getURI()) )
          arp.parseWarning(ERR_URI_NOT_NORMAL_FORM_C,t.location,
              "<" + rslt.getURI() + "> not in Unicode Normal Form C.");
        
             if ( !ctxt.isSameAsDocument() ) {
                boolean bad = false;
                try {
                 URIReference other = new URIReference(ctxt.getDocument(),val);
                 bad = !other.equals(rslt);
                }
                catch (Exception e) {
                   // Note resolving the URIReference above may not work.
                }
                 if ( bad  ) {
      arp.parseWarning(IGN_XMLBASE_SIGNIFICANT,t.location,
           "Use of attribute xml:base changes interpretation of relative URI: \"" + val + "\".");
                 }
             }
         }
         return rslt;
     }
     catch (MalformedURIException mal) {
        arp.parseWarning( WARN_MALFORMED_URI,
                          t.location,
                          "Bad URI <" + s.value + ">: " + mal.getMessage()      );
        return new BadURIReference(val);
      }
   }
   
   void createTriple( ARPResource r, Token p, Object v, String reify )
   throws ParseException {
       switch ( p.kind ) {
           case E_OTHER:
           case E_RDF_N:
               r.setPredicateObject( ((ARPQname)p).asURIReference(arp), 
               v, 
               reify ); 
               break;
           case E_LI: 
               r.setLiObject( v, reify );
               break;
               default:
                   throw new RuntimeException("Assertion failure in ParserSupport.createTriple");
       }
   }
   
   ARPDatatypeLiteral createDatatypeLiteral( URIReference dtURI, ARPString dtLex) {
      return new ARPDatatypeLiteral(dtLex,dtURI);
   }
/*
   private Map checkNameSpace(StringBuffer b,ARPQname qn,Map ns) {
       String q = qn.qName;
       int colon = q.indexOf(':');
       String prefix = colon==-1?"":q.substring(0,colon);
       String old = (String)ns.get(prefix);
       if ( old == null || !old.equals(qn.nameSpace) ) {
           Map rslt = new HashMap(ns);
           rslt.put(prefix,qn.nameSpace);
           if ( prefix.length() == 0 ) {
               // MUST use \" as delimiter refer to RFC 2396, \' may appear in uri.
               b.append(" xmlns=\"" + qn.nameSpace + "\"");
           } else {
               // MUST use \" as delimiter refer to RFC 2396, \' may appear in uri.
               b.append(" xmlns:"+prefix+"=\"" + qn.nameSpace + "\"");
           }
           return rslt;
       } else {
           return ns;
       }
   }
   */
   private void useNameSpace(Map ns, ARPQname qn) {
   	useNameSpace(ns,qn.prefix(),qn.nameSpace);
   }
   private void useNameSpace(Map ns, String prefix, String uri) {
   	  ns.put(prefix,uri);
   }
   void startLitElement(StringBuffer b,Token t,Map ns) {
       ARPQname qn = (ARPQname)t;
       b.append("<"+qn.qName);
       useNameSpace(ns,qn);
       return;
   }
   void checkNamespaceURI(Token t) throws ParseException {
   	  checkNamespaceURI(((StrToken)t).value,t);
   }
   private void checkNamespaceURI(String uri, Token t) throws ParseException {
   	if (uri.length()!=0)
   	  try {
   	    URI u = new URI(uri);
   	  }
   	  catch (MalformedURIException m) {
        arp.parseWarning(WARN_BAD_NAMESPACE_URI,t.location,
           "Illegal URI in xmlns declaration: " + uri);
       	  }
   }
   private void checkNamespace(Map allNs, String prefix, String uri, Token t) throws
   ParseException {
   	checkNamespaceURI(uri,t);
   	String ns = (String)allNs.get(prefix);
   	if (ns == null || !ns.equals(uri)) {
   //	System.err.println(prefix);
   //	System.err.println(uri);
   //	System.err.println(ns);
   //	System.err.println(t);
   //	Iterator it = allNs.entrySet().iterator();
   //	while ( it.hasNext() ) {
   //		Map.Entry e = (Map.Entry)it.next();
   //		System.out.println(e.getKey().toString() + " = " +
   //	  e.getValue().toString());
   //	}
      arp.parseWarning(ERR_INTERNAL_ERROR,t.location,
           "Internal namespaces error, please report to jjc@hpl.hp.com.");
      
   	}
   }
   /**
    * @param buf Add namespace attrs and then attrs to this buf.
    * @param attrs The attributes on this element.
    * @param visiblyUsed The visibly used namespaces on this element.
    * @param ns The namespaces declared within the parent element
    *            of the resulting XML Literal
    * @param allNs The namespaces as in the input document.
    */
   Map litAttributes(StringBuffer buf, SortedMap attrs,
                      SortedMap visiblyUsed, Map ns, Map allNs,
                      Token t) 
                      throws ParseException {
    boolean nsIsNew = false; 
    Iterator it = visiblyUsed.entrySet().iterator();
    while (it.hasNext()) {
    	Map.Entry entry = (Map.Entry)it.next();
    	String prefix = (String)entry.getKey();
    	String uri = (String)entry.getValue();
    	checkNamespace(allNs,prefix,uri,t);
    	if ( uri.equals(ns.get(prefix)) ) 
    	  continue;
    	if ( !nsIsNew ) {
    		ns = new HashMap(ns);
    		nsIsNew = true;
    	}
    	ns.put(prefix,uri);
    	String attr = prefix.equals("")?"xmlns":"xmlns:"+prefix;
    	buf.append(" " + attr + "=\"" + encodeAttributeText(uri) + "\"");
    }     
    it = attrs.values().iterator();
    while ( it.hasNext() ) {
    	buf.append((String)it.next());
    }           	
   	return ns;
   }
   Map litNamespace(Token prefix, Token uri,Map ns, Map used) {
   	String urins = ((StrToken)uri).value;
   	String prefixS = ((StrToken)prefix).value;
    useNameSpace(used,prefixS,urins);
    Map rslt = new HashMap(ns);
    rslt.put(prefixS,urins);
   	return rslt;
   }
   String litAttrName(Token attr, Map visiblyUsed) {
   	ARPQname qn = (ARPQname)attr;
   	if (!qn.prefix().equals("")) {
   		useNameSpace(visiblyUsed,qn);
   	}
   	return qn.qName;
   }
   String litAttribute(Token attr, Token val) {
   	ARPQname qn = (ARPQname)attr;
   	return " " + qn.qName + "=\"" + encodeAttributeText(((StrToken)val).value) +"\"";
   }
   void litComment(StringBuffer b, Token comment) {
   	 b.append("<!--" + ((StrToken)comment).value + "-->");
   }
   void litProcessingInstruction(StringBuffer b, Token pi) {
   	 b.append("<?" + ((StrToken)pi).value + "?>");
   }
   void endLitElement(StringBuffer b,Token t) {
       String q = ((ARPQname)t).qName;
       b.append("</"+q+">");
   }
   /*
   Map litAttrName(StringBuffer b,Token t,Map ns) {
       ARPQname qn = (ARPQname)t;
       Map rslt = checkNameSpace(b,qn,ns);
       b.append(" " + qn.qName );
       return ns;
   }
   void litAttrValue(StringBuffer b,Token t) {
       b.append("=\"" + encodeAttr(((StrToken)t).value) + "\"");
   }
   */
   void litText(StringBuffer b,Token t) {
       b.append(encodeTextNode(((StrToken)t).value));
   }
   static Map xmlNameSpace() {
       Map rslt = new HashMap();
       rslt.put("xml",ARPFilter.xmlns);
       rslt.put("","");
       return rslt;
   }
/*
    private void checkXMLLiteralNameSpace(String uri,String raw) {
        if ( !uri.equals("") ) {
            int colon = raw.indexOf(':');
            String prefix = colon==-1?null:raw.substring(0,colon);
            String oldUri = (String)xmlLiteralNameSpaces.get(prefix);
            if (oldUri!=null && oldUri.equals(uri))
                return;
            thisDepthXMLLiteralNameSpaces.add(new String[]{prefix,oldUri});
            xmlLiteralNameSpaces.put(prefix,uri);
            if ( prefix == null ) {
                xmlLiteralValue.append(" xmlns");
            } else {
                xmlLiteralValue.append(" xmlns:");
                xmlLiteralValue.append(prefix);
            }
            xmlLiteralValue.append("='");
            xmlLiteralValue.append(encodeAttr(uri));
            xmlLiteralValue.append('\'');
        }
    }
*/
   
    
   // http://www.w3.org/TR/2001/REC-xml-c14n-20010315#ProcessingModel
   /* The string value of the node is modified by replacing all 
    * ampersands (&) with &amp;, all open angle brackets (<) with 
    * &lt;, all quotation mark characters with &quot;, and the 
    * whitespace characters #x9, #xA, and #xD, with character references. 
    * The character references are written in uppercase hexadecimal 
    * with no leading zeroes (for example, #xD is represented by the 
    * character reference &#xD;). 
    */

    static private String encodeAttributeText(String s) { 
    	StringBuffer rslt = null;
    	String replace;
    	char ch;
    	for (  int i = 0; i< s.length(); i++) {
    		ch = s.charAt(i);
    		switch (ch) {
    			case '&':
    			  replace = "&amp;";
    			  break;
    			case '<':
    			  replace = "&lt;";
    			  break;
    			case '"':
    			  replace = "&quot;";
    			  break;
    			case 9:
    			  replace = "&#x9;";
    			  break;
    			case 0xA:
    			  replace = "&#xA;";
    			  break;
    			case 0xD:
    			  replace = "&#xD;";
    			  break;
    			  default:
    			  replace = null;
    		}
    		if ( replace != null ) {
    			if ( rslt == null ) {
    			  rslt = new StringBuffer();
    			  rslt.append(s.substring(0,i));
    			}
    			rslt.append( replace); 
    		} else if ( rslt != null ) {
    			rslt.append(ch);
    		}
    	}
    	return rslt==null?s:rslt.toString();
    }   
// http://www.w3.org/TR/2001/REC-xml-c14n-20010315#ProcessingModel
   /** except all ampersands are replaced by &amp;, all open angle
     brackets () are replaced by &lt;, all closing angle brackets 
     (>) are replaced by &gt;, and all #xD characters are replaced 
     by &#xD;.  
    */

    static private String encodeTextNode(String s) { 
    	StringBuffer rslt = null;
    	String replace;
    	char ch;
    	for (  int i = 0; i< s.length(); i++) {
    		ch = s.charAt(i);
    		switch (ch) {
    			case '&':
    			  replace = "&amp;";
    			  break;
    			case '<':
    			  replace = "&lt;";
    			  break;
    			case '>':
    			  replace = "&gt;";
    			  break;
    			case 0xD:
    			  replace = "&#xD;";
    			  break;
    			  default:
    			  replace = null;
    		}
    		if ( replace != null ) {
    			if ( rslt == null ) {
    			  rslt = new StringBuffer();
    			  rslt.append(s.substring(0,i));
    			}
    			rslt.append( replace); 
    		} else if ( rslt != null ) {
    			rslt.append(ch);
    		}
    	}
    	return rslt==null?s:rslt.toString();
    }   

}
