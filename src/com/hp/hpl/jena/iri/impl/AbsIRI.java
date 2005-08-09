/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.RDFURIReference;
import com.hp.hpl.jena.rdf.arp.MalformedURIException;

import java.nio.ByteBuffer;
import java.nio.charset.*;

public abstract class AbsIRI implements RDFURIReference {

    final IRIFactory factory;

    final AbsIRI parent;
    
    abstract public String toString();

    public AbsIRI(IRIFactory f) {
        factory = f;
        parent = null;
    }

    public AbsIRI(AbsIRI p) {
        factory = p.factory;
        parent = p;
    }

    private int ALL = Absolute_URI | IRI | Java_Net_URI | RDF_URI_Reference
            | XML_Schema_anyURI;

    // TODO: make this cleaner ...
    static private class SetFlag extends AbstractCollection {
        boolean seen = false;

        public int size() {
            throw new IllegalStateException(
                    "SetFlag is not really a Collection");
        }

        public Iterator iterator() {
            throw new IllegalStateException(
                    "SetFlag is not really a Collection");
        }

        public boolean add(Object o) {
            seen = true;
            return true;
        }
    }

    public boolean hasException(int conformance) {
        SetFlag setFlag = new SetFlag();
        for (AbsIRI x = this; x != null; x = x.parent) {
            x.addExceptions(conformance, setFlag);
            if (setFlag.seen)
                return true;
        }
        return false;
    }

    public boolean isURIinASCII() {
        // TODO: test cases for ~ (now allowed, but wasn't previously)
        String str = toString();
        for (int i = str.length() - 1; i >= 0; i--) {
            int ch = str.charAt(i);
            if (ch >= 128 || ch <= 32)
                return false;
            switch (ch) {
            case '<':
            case '>':
            case '"':
            case '{':
            case '}':
            case '|':
            case '\\':
            case '^':
            case '`':
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @param level
     * @param here
     * @return false if no more exceptions should be looked for.
     */
    abstract boolean addExceptions(int level, Collection here);

    public Iterator exceptions(int conformance) {
        Collection all = new ArrayList();
        for (AbsIRI x = this; x != null; x = x.parent)
            x.addExceptions(conformance, all);
        return all.iterator();
    }

    public boolean hasException() {
        return hasException(ALL);
    }

    public Iterator exceptions() {
        return exceptions(ALL);
    }

    public URL toURL() throws MalformedURLException {

        return new URL(toString());
    }

    Charset utf8 = Charset.forName("UTF-8");

    public String toASCIIString() {
        String str = toString();
        StringBuffer rslt = new StringBuffer();
        int ln = str.length();
        ByteBuffer bytes;
        for (int i = 0; i < ln; i++) {
            char ch = str.charAt(i);
            if (ch > 32 && ch < 128) {
                switch (ch) {
                case '<':
                case '>':
                case '"':
                case '{':
                case '}':
                case '|':
                case '\\':
                case '^':
                case '`':
                    break;
                default:
                    rslt.append(ch);
                    continue;
                }
                bytes = ByteBuffer.wrap(new byte[] { (byte) ch });
            } else {
                bytes = utf8.encode(str.substring(i, i + 1));
            }
            // TODO: revisit this - possibly inefficient ...
            for (int j = 0; j < bytes.remaining(); j++) {
                rslt.append('%');
                rslt.append(toHexDigit(bytes.get(j) / 16));
                rslt.append(toHexDigit(bytes.get(j) % 16));
            }
        }

        return rslt.toString();
    }

    static private char toHexDigit(int i) {
        return (char) (i < 10 ? ('0' + i) : ('A' + i - 10));
    }

    abstract RDFURIReference resolveAgainst(JavaURIWrapper base);

   
    protected void addException(IRIException exception, int level,
            Collection here) {
        if ((exception.getConformance() & level) != 0)
            here.add(exception);
    }

    abstract public RDFURIReference reparent(AbsIRI p);

    static private int prefs[][] = {
            { RELATIVE, RELATIVE | PARENT | GRANDPARENT },
            { PARENT, PARENT | GRANDPARENT }, { GRANDPARENT, GRANDPARENT } };

    static String exact[] = { ".", "..", "../.." };

    static String sub[] = { "", "../", "../../" };

    public String relativize(String abs, int flags) {  
       return relativize(factory.create(abs),abs,flags);   
    }
    public RDFURIReference relativize(RDFURIReference abs, int flags) {  
        String rslt = relativize(abs,null,flags); 
        return rslt==null?abs:factory.create(rslt);
     }
            
    /**
     * 
     * @param r
     * @param def     Default result if can't make this relative.
     * @param flags
     * @return
     */
   private String relativize(RDFURIReference r, String def, int flags) {
        if (r.isOpaque() || r.isVeryBad())
            return def;
        // logger.info("<"+Util.substituteStandardEntities(abs)+">");
        // logger.info("<"+Util.substituteStandardEntities(r.m_path)+">");
        boolean net = equal(r.getScheme(), getScheme());
        boolean absl = net && equal(r.getHost(), getHost())
                && equal(getUserinfo(), r.getUserinfo()) && equal(getPort(), r.getPort());
        boolean same = absl && equal(getPath(), r.getPath())
                && equal(getQuery(), r.getQuery());

        String rslt = r.getFragment() == null ? "" : ("#" + r.getFragment());

        if (same && (flags & SAMEDOCUMENT) != 0)
            return rslt;
        if (r.getQuery() != null) {
            rslt = "?" + r.getQuery() + rslt;
        }
        if (absl) {
            // TODO: pretty disgusting code, should be rewritten.
            // this array is stupid ...
            String
                m_subPaths[] = new String[] {
                        getPath() == null ? null : (getPath() + "a"), null, null,
                        null };
            
            if (m_subPaths[0] != null)
                for (int i = 0; i < 3; i++) {
                    if ((flags & prefs[i][1]) == 0)
                        break;
                    if (m_subPaths[i + 1] == null)
                        m_subPaths[i + 1] = getLastSlash(m_subPaths[i]);
                    if (m_subPaths[i + 1].length() == 0)
                        break;
                    if ((flags & prefs[i][0]) == 0)
                        continue;
                    if (!r.getPath().startsWith(m_subPaths[i + 1]))
                        continue;
                    // A relative path can be constructed.
                    int lg = m_subPaths[i + 1].length();
                    if (lg == r.getPath().length()) {
                        return exact[i] + rslt;
                    }
                    rslt = sub[i] + r.getPath().substring(lg) + rslt;

                    // logger.info("<"+Util.substituteStandardEntities(rslt)+">["+i+"]");
                    return rslt;
                }
        }
        rslt = r.getPath() + rslt;
        if (absl && (flags & ABSOLUTE) != 0) {
            return rslt;
        }
        if (net && (flags & NETWORK) != 0) {
            return "//" + (r.getUserinfo() == null ? "" : (r.getUserinfo() + "@"))
                    + r.getHost() + (r.getPort() == -1 ? "" : (":" + r.getPort()))
                    + rslt;

        }
        return def;
    }


    static private String getLastSlash(String s) {
        int ix = s.lastIndexOf('/', s.length() - 2);
        return s.substring(0, ix + 1);
    }

    private boolean equal(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }

    private boolean equal(int s1, int s2) {
        return s1 == s2;
    }

/*
 * Default null values for the URI break up parts.
 */    

    public String getUserinfo() {
        return null;
    }

    public int getPort() {
        return -1;
    }

    public String getPath() {
        return null;
    }

    public String getQuery() {
        return null;
    }

    public String getFragment() {
        return null;
    }

    public String getHost() {
        return null;
    }

    public String getScheme() {
        return null;
    }

}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

