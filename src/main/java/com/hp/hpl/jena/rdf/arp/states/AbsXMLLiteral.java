/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.impl.AbsXMLContext;
import com.hp.hpl.jena.rdf.arp.impl.XMLHandler;

public abstract class AbsXMLLiteral extends Frame {
    boolean checkComposingChar = true;

    static Map<String, String> xmlNameSpace = new TreeMap<String, String>();
    static {
        xmlNameSpace.put("xml", xmlns);
        xmlNameSpace.put("", "");
    }

    @Override
    String suggestParsetypeLiteral() {
        // shouldn't be called.
        return "";
    }
    
    final protected StringBuffer rslt;
    public final Map<String, String> namespaces; 

    private static String prefix(String qname) {
        int colon = qname.indexOf(':');
        return colon == -1?"":qname.substring(0,colon);
    }


    protected void append(String s) {
        rslt.append(s);
    }

    private void append(char ch[], int s, int l) {
        rslt.append(ch,s,l);
    }

    protected void append(char s) {
        rslt.append(s);
    }

    public AbsXMLLiteral(FrameI p, AbsXMLContext x, StringBuffer r) {
        super(p, x);
        rslt = r;
        namespaces = xmlNameSpace;
    }
    public AbsXMLLiteral(AbsXMLLiteral p, Map<String, String> ns) {
        super(p, p.xml);
        rslt = p.rslt;
        namespaces = ns;
    }
    public AbsXMLLiteral(XMLHandler h,AbsXMLContext x) {
        super(h, x);
        rslt = new StringBuffer();
        namespaces = xmlNameSpace;
    }
    
   
    private void useNameSpace(String prefix, String uri, Map<String, String> ns) {
        if (!uri.equals(namespaces.get(prefix)))
              ns.put(prefix, uri);
    }

    @Override
    abstract public void endElement() throws SAXParseException;

    void startLitElement(String uri, String rawName, Map<String, String> ns) {
        append('<');
        append(rawName);
        useNameSpace(prefix(rawName),uri, ns);
    }

    private void appendAttrValue(String s) {
        String replace;
        char ch;
        for (int i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            switch (ch) {
                case '&' :
                    replace = "&amp;";
                    break;
                case '<' :
                    replace = "&lt;";
                    break;
                case '"' :
                    replace = "&quot;";
                    break;
                case 9 :
                    replace = "&#x9;";
                    break;
                case 0xA :
                    replace = "&#xA;";
                    break;
                case 0xD :
                    replace = "&#xD;";
                    break;
                default :
                    replace = null;
            }
            if (replace != null) {
                append(replace);
            } else  {
                append(ch);
            }
        }
    }

    /** except all ampersands are replaced by &amp;, all open angle
      brackets () are replaced by &lt;, all closing angle brackets 
      (>) are replaced by &gt;, and all #xD characters are replaced 
      by &#xD;.  
     * @throws SAXParseException 
     */
    @Override
    public void characters(char[] chrs, int start, int length) throws SAXParseException {

        if (checkComposingChar)
            checkComposingChar(taint,chrs, start, length);
        checkComposingChar = false;
        
        String replace;
        char ch;
        for (int i = 0; i < length; i++) {
            ch = chrs[start+i];
            switch (ch) {
                case '&' :
                    replace = "&amp;";
                    break;
                case '<' :
                    replace = "&lt;";
                    break;
                case '>' :
                    replace = "&gt;";
                    break;
                case 0xD :
                    replace = "&#xD;";
                    break;
                default :
                    replace = null;
            }
            if (replace != null) {
                append(replace);
            } else  {
                append(ch);
            }
        }
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXParseException {
        append("<!--");
        append(ch,start,length);
        append("-->");

        checkComposingChar = true;
    }

    @Override
    public void processingInstruction(String target, String data) {
        append("<?");
        append(target);
        append(' ');
        append(data);
        append("?>");

        checkComposingChar = true;
    }

    @Override
    public FrameI startElement(String uri, String localName, String rawName, Attributes atts) {

        checkComposingChar = true;
        
        Map<String, String> attrMap = new TreeMap<String, String>();
        Map<String, String> childNameSpaces = new TreeMap<String, String>();
        startLitElement( uri,  rawName, childNameSpaces);
        for (int i = atts.getLength()-1;i>=0;i--) {
            String ns = atts.getURI(i);
            String qname = atts.getQName(i);
            String prefix = prefix(qname);
            if (!prefix.equals(""))
                useNameSpace(prefix,ns, childNameSpaces);
            attrMap.put(qname,atts.getValue(i));
        }
        // At this stage, childNameSpaces contains the new visibly used
        // namespaces (i.e. those not in this).
        // attrMap contains the attributes
        // Both are sorted correctly, so we just read them off,
        // namespaces first.
        Iterator<Map.Entry<String, String>> it = childNameSpaces.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            append(" xmlns");
            String prefix = pair.getKey();
            if (!"".equals(prefix)) {
                append(':');
                append(prefix);
            }
            append("=\"");
            appendAttrValue(pair.getValue());
            append('"');
        }
        it = attrMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            append(' ');
            append(pair.getKey());
            append("=\"");
            appendAttrValue(pair.getValue());
            append('"');
        }
        append('>');
        
        // Now sort out our namespaces, so that
        // child can see all of them.
        if (childNameSpaces.isEmpty()) {
            childNameSpaces = namespaces;
        } else {
            it = namespaces.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pair = it.next();
                String prefix = pair.getKey();
                if (!childNameSpaces.containsKey(prefix))
                    childNameSpaces.put(prefix,pair.getValue());
                // else prefix was overwritten with different value
            }
        }
        return new InnerXMLLiteral(this, rawName, childNameSpaces);
    }
 
}


/*
 *  (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 *
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
 */
 
