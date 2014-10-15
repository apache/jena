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

package org.apache.jena.iri.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import java.net.IDN;

import org.apache.jena.iri.* ;
/*
import com.vgrs.xcode.idna.Idna;
import com.vgrs.xcode.idna.Nameprep;
import com.vgrs.xcode.idna.Punycode;
import com.vgrs.xcode.util.XcodeException;
*/
abstract public class AbsIRIImpl extends  IRI implements 
        ViolationCodes, IRIComponents {

    private static final int defaultRelative = ABSOLUTE | SAMEDOCUMENT | CHILD
            | PARENT | GRANDPARENT;

    static String removeDotSegments(String path) {
        // 5.2.4 step 1.
        int inputBufferStart = 0;
        int inputBufferEnd = path.length();
        StringBuffer output = new StringBuffer();
        // 5.2.4 step 2.
        while (inputBufferStart < inputBufferEnd) {
            String in = path.substring(inputBufferStart);
            // 5.2.4 step 2A
            if (in.startsWith("./")) {
                inputBufferStart += 2;
                continue;
            }
            if (in.startsWith("../")) {
                inputBufferStart += 3;
                continue;
            }
            // 5.2.4 2 B.
            if (in.startsWith("/./")) {
                inputBufferStart += 2;
                continue;
            }
            if (in.equals("/.")) {
                in = "/"; // don't continue, process below.
                inputBufferStart += 2; // force end of loop
            }
            // 5.2.4 2 C.
            if (in.startsWith("/../")) {
                inputBufferStart += 3;
                removeLastSeqment(output);
                continue;
            }
            if (in.equals("/..")) {
                in = "/"; // don't continue, process below.
                inputBufferStart += 3; // force end of loop
                removeLastSeqment(output);
            }
            // 5.2.4 2 D.
            if (in.equals(".")) {
                inputBufferStart += 1;
                continue;
            }
            if (in.equals("..")) {
                inputBufferStart += 2;
                continue;
            }
            // 5.2.4 2 E.
            int nextSlash = in.indexOf('/', 1);
            if (nextSlash == -1)
                nextSlash = in.length();
            inputBufferStart += nextSlash;
            output.append(in.substring(0, nextSlash));
        }
        // 5.2.4 3
        return output.toString();
    }

    private static void removeLastSeqment(StringBuffer output) {
        int ix = output.length();
        while (ix > 0) {
            ix--;
            if (output.charAt(ix) == '/')
                break;
        }
        output.setLength(ix);
    }

    private long foundExceptionMask;

    long allErrors;

    abstract long errors(int field);

    abstract SchemeSpecificPart getSchemeSpec();
    abstract Exception getIDNAException();
    
    // void throwExceptions(IRIFactoryImpl f, boolean includeRelative) {
    // long mask = f.exceptions;
    // if (!includeRelative)
    // mask &= ~(1l << RELATIVE_URI);
    // if (hasExceptionMask(mask)) {
    // throw (IRIImplUncheckedException) exceptionsMask(mask).next();
    // }
    // }

    boolean hasExceptionMask(long mask) {
        return (allErrors & mask) != 0;
    }

    private ArrayList<Violation> foundExceptions;

    protected String path;
/*
    static private Idna idna;
    static {
        try {
            idna = new Idna(new Punycode(), new Nameprep());
        } catch (XcodeException e) {
            System.err.println("Internal error in IDN setup");
            e.printStackTrace();
        }
    }
*/
    static final private char hex[] = "0123456789ABCDEF".toCharArray();

    static final Iterator<Violation> nullIterator = new ArrayList<Violation>(0).iterator();

    protected static final int NO_EXCEPTIONS = 1;

    protected static final int ALL_EXCEPTIONS = 2;

    protected static final int NOT_RELATIVE_EXCEPTIONS = 3;

    protected static final int PATH_INDEX = Parser.invFields[PATH];

    public AbsIRIImpl() {
        super();
    }

    Iterator<Violation> exceptionsMask(final long mask) {
        createExceptions(mask);
        return foundExceptions == null ? nullIterator : 
            new Iterator<Violation>() {
               private Iterator<Violation> underlying = foundExceptions.iterator();
  
                private Violation next;
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean hasNext() {
                    if (next==null) {
                        while (underlying.hasNext()) {
                            next = underlying.next();
                            if (((1l << (next).getViolationCode()) 
                                    & mask) != 0) 
                                return true;
                        }
                        next = null;
                        return false;
                    }
                    return true;
                }

                @Override
                public Violation next() {
                    if (hasNext()) {
                        Violation rslt = next;
                        next = null;
                        return rslt;
                    }
                    throw new NoSuchElementException();
                }
            
        };
    }

    private void createExceptions(long m) {
        m &= ~foundExceptionMask;
        if ((allErrors & m) != 0) {
            if (foundExceptions == null) {
                foundExceptions = new ArrayList<>();
            }
            for (int i = 0; i < Parser.fields.length; i++) {
                int f = Parser.fields[i];
                if ((errors(f) & m) != 0) {
                    for (int e = 0; e < 64; e++)
                        if (((1l << e) & m & errors(f)) != 0) {
                            foundExceptions.add(new ViolationImpl(this, f, e));
                        }

                }
            }

        }
        foundExceptionMask |= m;
    }

    @Override
    public boolean isAbsolute() {
        return has(SCHEME);
    }

    abstract boolean has(int component);

    @Override
    public boolean isRelative() {
        return !has(SCHEME);
    }

    /*
     * public boolean isRDFURIReference() { return !hasException(RDF); }
     * 
     * public boolean isIRI() { return !hasException(IRI); }
     * 
     * public boolean isURIinASCII() { return !hasException(URI); }
     */
    // public boolean isVeryBad() {
    // return false;
    // }
    // public boolean isXSanyURI() {
    // return !hasException(XMLSchema);
    // }
    /*
    public boolean hasException(int conformance) {
        return hasExceptionMask(getFactory().recsToMask(conformance));
    }

    public Iterator exceptions(int conformance) {
        return exceptionsMask(getFactory().recsToMask(conformance));
    }
    */

    @Override
    public boolean hasViolation(boolean includeWarnings) {
        return hasExceptionMask(getSchemeSpec().getMask(includeWarnings));
    }

    @Override
    public Iterator<Violation> violations(boolean includeWarnings) {
        return exceptionsMask(getSchemeSpec().getMask(includeWarnings));
    }

    @Override
    public URL toURL() throws MalformedURLException {
        return new URL(toASCIIString());
    }

    // TODO ToAsciiMask
    static long ToAsciiMask = 
        ~0l;
        /*
        (1l << LTR_CHAR) | (1l << ILLEGAL_CHAR)
            | (1l << IRI_CHAR) | (1l << UNWISE_CHAR) | (1l << WHITESPACE)
            | (1l << NOT_XML_SCHEMA_WHITESPACE) | (1l << NON_XML_CHARACTER)
            | (1l << DOUBLE_DASH_IN_REG_NAME);
*/
    @Override
    public String toASCIIString() throws MalformedURLException {
        if (hasExceptionMask(ToAsciiMask)) {
            return createASCIIString();
        }
        return toString();
    }

    private String createASCIIString() throws MalformedURLException {
        StringBuffer asciiString = new StringBuffer();

        if (has(SCHEME)) {
            toAscii(asciiString, getScheme(), errors(SCHEME));
            asciiString.append(':');
        }
        if (has(AUTHORITY)) {
            asciiString.append("//");
            if (has(USER)) {
                toAscii(asciiString, getRawUserinfo(), errors(USER));
                asciiString.append('@');
            }

            String host = getRawHost();
            regNameToAscii(asciiString,host);
            if (has(PORT)) {
                asciiString.append(':');
                toAscii(asciiString, get(PORT), errors(USER));
            }
        }
        toAscii(asciiString, getRawPath(), errors(PATH));
        if (has(QUERY)) {
            asciiString.append('?');
            toAscii(asciiString, getRawQuery(), errors(QUERY));
        }
        if (has(FRAGMENT)) {
            asciiString.append('#');
            toAscii(asciiString, getRawFragment(), errors(FRAGMENT));
        }
        return asciiString.toString();
    }

    private void regNameToAscii(StringBuffer asciiString, String host)
            throws MalformedURLException {
        if ((errors(HOST) & ToAsciiMask) == 0) {
            asciiString.append(host);
            return;
        }
       
        asciiString.append(domainToAscii(host));

    }

    static CharSequence domainToAscii(String host) throws MalformedIDNException {
        
        try {
            return IDNP.toASCII(host, IDN.USE_STD3_ASCII_RULES|IDN.ALLOW_UNASSIGNED);
        } catch (Exception e) {
            throw new MalformedIDNException(e);
        } 
        /*
        int u[] = new int[host.length()];
        for (int i = 0; i < host.length(); i++)
            u[i] = host.charAt(i);

        try {
            return idna.domainToAscii(u);
        } catch (XcodeException e) {
            throw new MalformedIDNException(e);
        }
        */
    }

    private void toAscii(StringBuffer asciiString, String field, long errs) {
        if ((errs & ToAsciiMask) == 0) {
            asciiString.append(field);
            return;
        }
        // 3.1 RFC 3987
        // Step 1c

        // nothing

        // Step 2a
        /*
         * Step 2. For each character in 'ucschar' or 'iprivate', apply steps
         * 2.1 through 2.3 below.
         * 
         * We interpret this as any charcater above 127, below 32 and the unwise
         * chars
         * 
         * Systems accepting IRIs MAY also deal with the printable characters in
         * US-ASCII that are not allowed in URIs, namely "<", ">", '"', space,
         * "{", "}", "|", "\", "^", and "`", in step 2 above.
         */
        for (int i = 0; i < field.length(); i++) {
            // TODO Java 1.4/1.5 issue
            int ch = field.charAt(i);
            if (ch > 127 || "<>\" {}|\\^`".indexOf(ch) != -1 || ch < 32) {
                // 2.1
                byte b[];
                try {
                    b = field.substring(i, i + 1).getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Impossible - utf-8 unsupported");
                }
                // 2.2, 2.3
                for ( byte aB : b )
                {
                    char buf[] = { '%', hex[( aB & ( 255 & ~15 ) ) >> 4], hex[aB & 15] };

                    asciiString.append( buf );
                }
            } else {
                asciiString.append(new char[] { (char) ch });
            }
        }
    }

/*
 * Subroutine for relativize.
 * 
 * Relativizing path components is somewhat tricky.
 * The code is in the method PathRelative.check
 * which is invoked from relativizePaths
 * 
 * There are only three static stateless objects of this class, 
 * each of which checks for a particular rule.
 * 
 * The child object behaves slightly differently, 
 * with a helper method overridden, and one line of
 * code specific to that object only.
 * 
 */
    
    static private final PathRelativize
        child = new PathRelativize(CHILD, CHILD | PARENT | GRANDPARENT,
        		"."){
    	@Override
        String descendentMatch(String descendent) {
    		return maybeDotSlash( descendent);
    	}
    },
        parent = new PathRelativize(PARENT, PARENT | GRANDPARENT,
        		".."),
        grandparent = new PathRelativize(GRANDPARENT, GRANDPARENT,
                		"../..");

    /**
     * 
     * @param in     The path from this IRI
     * @param out    If successful, out[0] contains the answer
     * @param flags  Currently applicable rules
     * @param rel    The path to make relative
     * @return True if the paths were relativized.
     */
    private boolean relativizePaths(String in, String[] out, int flags, String rel) {
		if (child.check(in, out, flags, rel)) 
			return true;
		if (parent.check(out[0], out, flags, rel)) 
			return true;
		return grandparent.check(out[0], out, flags, rel);
	}

    static class PathRelativize {
    	final private int flag;
    	final private int allFlags;
    	final private String replacement;
    	/**
    	 * 
    	 * @param flag      If this flag is not present then this rule does not apply.
    	 * @param allFlags  If none of these flags are present then this rule and subsequence rules do not apply.
    	 * @param replacement     If there is a match, then use this as the relative path
    	 */
    	PathRelativize(int flag, int allFlags, String replacement) {
    		this.flag = flag;
    		this.replacement = replacement;
    		this.allFlags = allFlags;
    	}
    	/**
    	 * Return true if the rule applied.
    	 * The result of the rule is returned in out[0]
    	 * Return false if the rule did not apply.
    	 * The input for the next rule is in out[0]
    	 * @param in      Absolute path to use in match
    	 * @param out     Result, as above.
    	 * @param flags   controlling rule applicability
    	 * @param rel     Relative path to use in match
    	 * @return
    	 */
    	boolean  check(String in, String out[], int flags, String rel) {
    		out[0] = null;
    		if (in==null)
    			return false;
    		if ((flags & allFlags) == 0)
    		     return false;
    		int ix = in.lastIndexOf('/');
    		if (ix==-1)
    			return false;
    		if (ix==0 && (flags & ABSOLUTE)!=0 && flag != CHILD)
    			return false;
    		in = in.substring(0,ix+1);
    		out[0] = in.substring(0,ix);
    		if ((flags & flag) == 0)
   		         return false;
    		if (!rel.startsWith(in))
    			return false;
    		if (rel.length() == ix+1) {
    			out[0] = replacement;
    			return true;
    		}
    		out[0] = descendentMatch(rel.substring(ix+1));
    		return true;
    	}
    	
    	String descendentMatch(String descendent) {
    		return replacement + "/" + descendent;
    	}
    	
    }
    

    @Override
    public IRI relativize(String abs, int flags) {
        return relativize(new IRIImpl(getFactory(), abs), flags);
    }

    @Override
    public IRI relativize(String abs) {
        return relativize(abs, defaultRelative);
    }

    @Override
    public IRI relativize(IRI abs) {
        return relativize(abs, defaultRelative);
    }
    /*
     * public String relativize(String abs, int flags) { return
     * relativize(factory.create(abs),abs,flags); }
     */
    @Override
    public IRI relativize(IRI abs, int flags) {
        String rslt = relativize(abs, null, flags);
        return rslt == null ? abs : getFactory().create(rslt);
    }
    /**
     * 
     * @param r
     * @param def
     *            Default result if can't make this relative.
     * @param flags
     */
    private String relativize(IRI r, String def, int flags) {
        if (!has(AUTHORITY))   // we could use the new rules for relative URIs for rootless, but I don't like them
            return def;
        if (!((AbsIRIImpl)r).has(AUTHORITY))
            return def;
        // logger.info("<"+Util.substituteStandardEntities(abs)+">");
        // logger.info("<"+Util.substituteStandardEntities(r.m_path)+">");
        boolean net = equal(r.getScheme(), getScheme());
        boolean absl = net && equal(r.getRawHost(), getRawHost())
                && equal(getRawUserinfo(), r.getRawUserinfo())
                && equal(getPort(), r.getPort());
        boolean same = absl && equal(getRawPath(), r.getRawPath())
                && equal(getRawQuery(), r.getRawQuery());

        String rslt = r.getRawFragment() == null ? "" : ("#" + r
                .getRawFragment());

        if (same && (flags & SAMEDOCUMENT) != 0)
            return rslt;

    	String thisPath = getRawPath();
    	String pathToRel = r.getRawPath();
        if (r.getRawQuery() != null) {
            rslt = "?" + r.getRawQuery() + rslt;
        	if (equal(thisPath,pathToRel)
            		&& (flags & CHILD)!=0 ) {
            		return rslt;
            	}
        }
        if (absl) {
        	if (pathToRel.length()>0) {
        		if (thisPath.length()>0) {
        		  String out[] = new String[]{null};
        		  if (relativizePaths(thisPath, out, flags, pathToRel)) {
        			return out[0]+rslt;
        		  }
        		}
                rslt = r.getRawPath() + rslt;
                if (absl && (flags & ABSOLUTE) != 0) {
                    return rslt;
                }
        	}
        } else 
        	rslt = r.getRawPath() + rslt;
        if (net && (flags & NETWORK) != 0) {
            return "//"
                    + (r.getRawUserinfo() == null ? ""
                            : (r.getRawUserinfo() + "@")) + r.getRawHost()
                    + (r.getPort() == IRI.NO_PORT ? "" : (":" + ((AbsIRIImpl)r).get(PORT))) + rslt;

        }
        return def;
    }


	static private String maybeDotSlash(String path) {
        int colon = path.indexOf(':');
        if (colon == -1)
            return path;
        int slash = path.indexOf('/');
        if (slash==-1 || slash>colon)
            return "./"+path;
        return path;
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

    public Iterator<Violation> allViolations() {
        return exceptionsMask(~0l);
    }

    @Override
   public String getRawUserinfo() {
        return get(USER);
    }

    @Override
   public int getPort() {
        String port = get(PORT);
        if (port == null)
            return IRI.NO_PORT;
        try {
            int v = Integer.parseInt(port);
            if (v<0)
                return IRI.ILLFORMED_PORT;
            return v;
        } catch (Exception e) {
            return IRI.ILLFORMED_PORT;
        }
    }

    @Override
    public String getRawQuery() {
        return get(QUERY);
    }

    @Override
    public String getRawFragment() {
        return get(FRAGMENT);
    }

    @Override
    public String getRawHost() {
        return get(HOST);
    }

    @Override
    public String getScheme() {
        return get(SCHEME);
    }

    abstract String get(int comp);

    @Override
    public String getRawPath() {
        return path;
    }

    @Override
    public boolean isRootless() {
        if (!has(SCHEME))
            return false;
        if (has(AUTHORITY))
            return false;
        if (path.equals(""))
            return false;
        if (path.charAt(0) == '/')
            return false;
        return true;
    }

    abstract String pathRemoveDots();

    abstract boolean dotsOK();

    @Override
    public String getRawAuthority() {
        return get(AUTHORITY);
    }

    @Override
    public IRI create(IRI i) {
        return new ResolvedRelativeIRI(this, (AbsIRIImpl) getFactory()
                .create(i));
    }

    @Override
    public IRI create(String s) {
        return create(new IRIImpl(getFactory(), s) );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof IRI))
            return false;
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String getAuthority() {
        return getCooked(AUTHORITY);
    }

    @Override
    public String getFragment() {
        return getCooked(FRAGMENT);
    }

    @Override
    public String getHost() {
        return getCooked(HOST);
    }

    @Override
    public String getPath() {
        return getCooked(PATH);
    }

    @Override
    public String getQuery() {
        return getCooked(QUERY);
    }

    @Override
    public String getUserinfo() {
        return getCooked(USER);
    }

    private String getCooked(int component) {
        // TODO getCooked
    	throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public IRI normalize(boolean useDns) {
        // TODO normalize
    	throw new UnsupportedOperationException("not yet implemented");
    }

    /*
     * Bidirectional IRIs MUST be rendered in the same way as they would be if
     * they were in a left-to-right embedding; i.e., as if they were preceded by
     * U+202A, LEFT-TO-RIGHT EMBEDDING (LRE), and followed by U+202C, POP
     * DIRECTIONAL FORMATTING (PDF).
     * 
     */
    @Override
    public String toDisplayString() {
        return "\u202A" + toString() + "\u202C";
    }

    // TODO http://example.com/&#x10300;&#x10301;&#x10302 =>
    // http://example.com/%F0%90%8C%80%F0%90%8C%81%F0%90%8C%82

    @Override
    public String getASCIIHost() throws MalformedURLException {
        StringBuffer asciiString = new StringBuffer();

        String host = getRawHost();
        if (host==null)
            return null;
        regNameToAscii(asciiString,host);
        return asciiString.toString();
    }

    @Override
    public boolean ladderEquals(IRI iri, int other) {
        // TODO ladderEquals
    	throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public int ladderEquals(IRI iri) {
        // TODO ladderEquals
    	throw new UnsupportedOperationException("not yet implemented");
    }
}
