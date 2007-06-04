/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.io.UnsupportedEncodingException;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIComponents;
import com.hp.hpl.jena.iri.ViolationCodes;

public class IRIFactoryImpl 
  extends AbsIRIFactoryImpl
  implements ViolationCodes, Force, IRIComponents {

public static final int UNKNOWN_SYNTAX = -4;
    //    boolean throwUncheckedExceptions = false;
    /*
    static final long conformanceMasks[] = {
            // RFC3986
              (1l<<ILLEGAL_CHAR) 
              |(1l<<ILLEGAL_PERCENT_ENCODING)
              |(1l<<EMPTY_SCHEME)
              |(1l<<IP_V4_HAS_FOUR_COMPONENTS)
              |(1l<<IP_V4_OCTET_RANGE)
              |(1l<<IP_V6_OR_FUTURE_ADDRESS_SYNTAX)
              |(1l<<IRI_CHAR)
              |(1l<<LTR_CHAR)
              |(1l<<NOT_XML_SCHEMA_WHITESPACE)
              |(1l<<SCHEME_MUST_START_WITH_LETTER)
              |(1l<<UNWISE_CHAR)
              |(1l<<WHITESPACE)
              |(1l<<ARBITRARY_CHAR)
              ,
            // RFC3987
              (1l<<ILLEGAL_CHAR) 
              |(1l<<ILLEGAL_PERCENT_ENCODING)
              |(1l<<EMPTY_SCHEME)
              |(1l<<IP_V4_HAS_FOUR_COMPONENTS)
              |(1l<<IP_V4_OCTET_RANGE)
              |(1l<<IP_V6_OR_FUTURE_ADDRESS_SYNTAX)
              |(1l<<LTR_CHAR)
              |(1l<<NOT_XML_SCHEMA_WHITESPACE)
              |(1l<<SCHEME_MUST_START_WITH_LETTER)
              |(1l<<UNWISE_CHAR)
              |(1l<<WHITESPACE)
              |(1l<<ARBITRARY_CHAR)
              ,
            // RDF
              (1l<<ILLEGAL_CHAR) 
              |(1l<<ILLEGAL_PERCENT_ENCODING)
              |(1l<<EMPTY_SCHEME)
              |(1l<<IP_V4_HAS_FOUR_COMPONENTS)
              |(1l<<IP_V4_OCTET_RANGE)
              |(1l<<IP_V6_OR_FUTURE_ADDRESS_SYNTAX)
              |(1l<<SCHEME_MUST_START_WITH_LETTER)
              |(1l<<RELATIVE_URI)
              ,
            // XLink
              (1l<<ILLEGAL_CHAR) 
              |(1l<<ILLEGAL_PERCENT_ENCODING)
              |(1l<<EMPTY_SCHEME)
              |(1l<<IP_V4_HAS_FOUR_COMPONENTS)
              |(1l<<IP_V4_OCTET_RANGE)
              |(1l<<IP_V6_OR_FUTURE_ADDRESS_SYNTAX)
              |(1l<<SCHEME_MUST_START_WITH_LETTER)
              |(1l<<NON_XML_CHARACTER)
              ,
            // XMLSchema
              (1l<<ILLEGAL_CHAR) 
              |(1l<<ILLEGAL_PERCENT_ENCODING)
              |(1l<<EMPTY_SCHEME)
              |(1l<<IP_V4_HAS_FOUR_COMPONENTS)
              |(1l<<IP_V4_OCTET_RANGE)
              |(1l<<IP_V6_OR_FUTURE_ADDRESS_SYNTAX)
              |(1l<<SCHEME_MUST_START_WITH_LETTER)
              |(1l<<NOT_XML_SCHEMA_WHITESPACE)
              |(1l<<NON_XML_CHARACTER)
              ,
            // IDN
//              (1l<<ACE_PREFIX)
              0
              ,
            // Should
              (1l<<LOWERCASE_PREFERRED) 
              |(1l<<PORT_SHOULD_NOT_BE_EMPTY)
              |(1l<<PORT_SHOULD_NOT_START_IN_ZERO)
    //          |(1l<<SCHEME_NAMES_SHOULD_BE_LOWER_CASE)
              |(1l<<PERCENT_ENCODING_SHOULD_BE_UPPERCASE)
              |(1l<<IPv6ADDRESS_SHOULD_BE_LOWERCASE)
              |(1l<<USE_PUNYCODE_NOT_PERCENTS)
              ,
            // Minting
      /* consider HAS_PASSWORD vs LOWER_CASE_PREFERRED
       * The former should be an error unless switched
       * off (but it can be, unlike a MUST), whereas the 
       * latter should be a warning by default.
       * /
              (1l<<LOWERCASE_PREFERRED) 
              |(1l<<PORT_SHOULD_NOT_BE_EMPTY)
              |(1l<<PORT_SHOULD_NOT_START_IN_ZERO)
    //          |(1l<<SCHEME_NAMES_SHOULD_BE_LOWER_CASE)
              |(1l<<PERCENT_ENCODING_SHOULD_BE_UPPERCASE)
              |(1l<<IPv6ADDRESS_SHOULD_BE_LOWERCASE)
              |(1l<<USE_PUNYCODE_NOT_PERCENTS)
              ,
            // DNS
              (1l<<NOT_DNS_NAME)
              ,
        };
        */
    protected long errors;
    protected long warnings;
    
    protected Set specs = new HashSet();

    public IRIFactoryImpl() {
    }

    public IRIFactoryImpl(IRIFactoryImpl template) {
        backwardCompatibleRelativeRefs = template.backwardCompatibleRelativeRefs;
        encoding = template.encoding;
        errors = template.errors;
        prohibited = template.prohibited;
        required = template.required;
        warnings = template.warnings;
        System.arraycopy(template.asErrors,0,asErrors,0,asErrors.length);
        System.arraycopy(template.asWarnings,0,asWarnings,0,asWarnings.length);
        Iterator it = template.schemes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            SchemeSpecificPart p = (SchemeSpecificPart)entry.getValue();
            if (p.withScheme()) {
                schemes.put(entry.getKey(),new WithScheme((WithScheme)p));
            } else if (p.port()!=IRI.NO_PORT) {
                schemes.put(entry.getKey(),new NoScheme(p.port()));
            }
        }
    }

    /*
    protected long recsToMask(int recs) {
        long  mask = 0;
        for (int i=0; recs != 0 && i<conformanceMasks.length; i++) {
            if ((recs & (1<<i)) != 0) {
                mask |= conformanceMasks[i];
                recs &= ~(1<<i);
            }
        }
        return mask;
    }
*/
    private final long getMask(boolean includeWarnings) {
        
        return includeWarnings?(errors|warnings):errors;
    }

    protected IRIFactoryImpl getFactory() {
        return this;
    }

    public IRI create(IRI i) {
        if (i instanceof AbsIRIImpl && 
                ((AbsIRIImpl)i).getFactory()==this)
            return i;
        return 
            create(i.toString());
    }

    boolean getSameSchemaRelativeReferences(String scheme) {
        return backwardCompatibleRelativeRefs.contains(scheme.toLowerCase());
    }
    
    private String encoding = "utf-8";
    String getEncoding() {
        return encoding;
    }
    public void setEncoding(String enc) throws UnsupportedEncodingException {
        // check enc is valid encoding.
        "".getBytes(enc);
        encoding = enc;
    }
    
   
    boolean asErrors[] = new boolean[]{
            true,
            true,
            false,
            true,
            true,
            true,
            
    };
    boolean asWarnings[] = new boolean[]{
      false,
      false,
      true,
      false,
      false,
      false
    };
    
    protected void setViolation(int ix, boolean e, boolean w){
        if (e && w)
            throw new IllegalArgumentException("xxxViolation(true,true) is not permitted.");
        initializing();
        asErrors[ix]=e;
        asWarnings[ix]=w;
        
    }
    
    protected boolean getAsWarnings(int ix) {
        return asWarnings[ix];
    }

    protected boolean getAsErrors(int ix) {
        return asErrors[ix];
    }
//    boolean isException(int code) {
//        return (errors & (1l<<code))!=0;
//    }

    private boolean initializing = true;
private Set backwardCompatibleRelativeRefs = new HashSet();
    protected void initializing() {
        if (!initializing)
            throw new IllegalStateException("Cannot reinitialize IRIFactory after first use.");
        
    }
    
    public IRI create(String s) {
        initializing = false;
        return super.create(s);
    }

    public void setSameSchemeRelativeReferences(String scheme) {
        if (scheme.equals("*"))
          backwardCompatibleRelativeRefs  = new AbstractSet(){

            public int size() {
                return Integer.MAX_VALUE;
            }

            public Iterator iterator() {
                throw new UnsupportedOperationException();
            }
            public boolean add(Object o) {
                return false;
            }

            public boolean contains(Object o) {
                return true;
            }
        };
        else 
            backwardCompatibleRelativeRefs.add(scheme.toLowerCase());
    }

    protected void useSpec(String name, boolean asErr) {
        initializing();
        Specification spec = Specification.get(name);
        specs.add(spec);
        for (int i=0; i<Force.SIZE; i++) {
            if (asErrors[i] || (asWarnings[i] && asErr)) {
                errors |= spec.getErrors(i);
            }
            if (asWarnings[i] ) {
                warnings |= spec.getErrors(i);
            }
        }
        prohibited |= spec.getProhibited();
        required |= spec.getRequired();
        warnings &= ~errors;
        
    }

    public SchemeSpecificPart getScheme(String scheme, Parser parser) {
        
        scheme = scheme.toLowerCase();
        SchemeSpecificPart p = (SchemeSpecificPart)schemes.get(scheme);
        if (p!=null) {
            p.usedBy(parser);
            return p;
        }
        int dash = scheme.indexOf('-');
        if (dash != -1) {
            if (scheme.startsWith("x-")) {
                p = noScheme();
            } else {
            	if (nonIETFScheme==null)
            		nonIETFScheme = new NoScheme() {
                  void usedBy(Parser pp) {
                      pp.recordError(SCHEME,UNREGISTERED_NONIETF_SCHEME_TREE);
                  }
                };
                p = nonIETFScheme;
            }
        } else if (Specification.schemes.containsKey(scheme)) {
            SchemeSpecification spec = (SchemeSpecification)Specification.schemes.get(scheme);
            p = new NoScheme(spec.port);
        } else{
        	if (unregisteredScheme==null){
        		unregisteredScheme = new NoScheme() {
        	        void usedBy(Parser pp) {
        	            pp.recordError(SCHEME,UNREGISTERED_IANA_SCHEME);
        	        }
        	      };
        	}
            p= unregisteredScheme;
        }
        p.usedBy(parser);
//        System.err.println("Scheme: "+scheme);
        if (schemes.size() < 1000)
          schemes.put(scheme,p);
        return p;
    }
    
    private NoScheme unregisteredScheme=null;
    private NoScheme nonIETFScheme=null;

    public SchemeSpecificPart noScheme() {
        return noScheme;
    }
    private class WithScheme extends SchemeSpecificPart
    {
        long zerrors;
        long zwarnings;
        int zrequired;
        int zprohibited;
        boolean inited = false;
        final SchemeSpecification scheme;
        private WithScheme(WithScheme ws) {
            zerrors = ws.zerrors;
            zwarnings = ws.zwarnings;
            zprohibited = ws.zprohibited;
            zrequired = ws.zrequired;
            scheme = ws.scheme;
        }
        private WithScheme(SchemeSpecification spec, boolean asErr){
            scheme = spec;
            for (int i=0; i<Force.SIZE; i++) {
                if (asErrors[i] || (asWarnings[i] && asErr)) {
                    zerrors |= spec.getErrors(i);
                }
                if (asWarnings[i] ) {
                    zwarnings |= spec.getErrors(i);
                }
            }
            
        }
        void usedBy(Parser parser) {
            if (!inited) {
                inited = true;
                zerrors |= errors;
                zwarnings |= warnings;
                zwarnings &= ~zerrors;
                zrequired = scheme.getRequired() | required;
                zprohibited = scheme.getProhibited() | prohibited;
            }
            
        }
        public long getMask(boolean includeWarnings) {
            return includeWarnings?(zerrors|zwarnings):zerrors;
        }

        public int getRequired() {
            return zrequired;
        }

        public int getProhibited() {
            return zprohibited;
        }
        public void analyse(Parser parser, int range) {
            scheme.analyse(parser,range);
        }
        public int port() {
            return scheme.port;
        }
        public boolean withScheme() {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
    private class NoScheme extends SchemeSpecificPart {
        
        NoScheme() {
            this(-1);
        }
        final private int port;
        NoScheme(int i) {
            port = i;
        }

        public long getMask(boolean includeWarnings) {
            return IRIFactoryImpl.this.getMask(includeWarnings);
        }

        public int getRequired() {
            return IRIFactoryImpl.this.getRequired();
        }

        public int getProhibited() {
            return IRIFactoryImpl.this.getProhibited();
        }

        void usedBy(Parser parser) { /* nothing */ }

        public void analyse(Parser parser, int range) {/* nothing */ }

        public int port() {
            return port;
        }

        public boolean withScheme() {
            return false;
        }
        
    }
    
    final private NoScheme noScheme = new NoScheme();
    private int required = 0;
    private int prohibited = 0;

    public int getRequired() {
        return required;
    }

    public int getProhibited() {
        return prohibited ;
    }

    final private Map schemes = new HashMap();
    
    public void useSchemeSpecificRules(String scheme, boolean asErr) {
        if (scheme.equals("*")) {
            Iterator it =
            Specification.schemes.keySet().iterator();
            while (it.hasNext()) {
                scheme = (String)it.next();
                if (!schemes.containsKey(scheme))
                   useSchemeSpecificRules(scheme,asErr);
            }
            return;
        } 
        scheme = scheme.toLowerCase();
        SchemeSpecification spec = (SchemeSpecification)Specification.schemes.get(scheme);
        if (spec==null) {
            schemes.put(scheme,noScheme);
        } else {
            schemes.put(scheme,new WithScheme(spec,asErr));
        }
        
    }
}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 
