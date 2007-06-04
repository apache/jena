/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.util.ArrayList;

import com.hp.hpl.jena.iri.IRIComponents;
import com.hp.hpl.jena.iri.ViolationCodes;

public class SchemeSpecification extends Specification implements 
ViolationCodes,
IRIComponents {
    
    boolean usesDNS = false;
    int port = IRIFactoryImpl.UNKNOWN_SYNTAX;

    public SchemeSpecification(String name, String rfc,
            String uri, String title, String section, String[] bad, String[] good) {
        super(name, "scheme", rfc, uri, title, section, bad, good);
        violations[Force.MUST] |= (1l<<SCHEME_PATTERN_MATCH_FAILED)|(1l<<SCHEME_REQUIRES_LOWERCASE);
        violations[Force.MINTING] |= (1l<<SCHEME_PREFERS_LOWERCASE);
    }


	public boolean applies(String scheme) {
		return name().equalsIgnoreCase(scheme);
	}
	
    private ArrayList dUris = new ArrayList();
    private ArrayList dDefnText = new ArrayList();
    private ArrayList dDefnHtml = new ArrayList();
    public void addDefinition(String uri, String defn, String defnHtml) {
        dUris.add(uri);
        dDefnText.add(defn);
        dDefnHtml.add(defnHtml);
    }

    public void setDNS(boolean b) {
        usesDNS = b;
    }

    public void port(int i) {
        port = i;
    }


    private ComponentPattern pattern[] = new ComponentPattern[Parser.fields.length];
    
    public void setPattern(int component, String string) {
         ComponentPattern p = new ComponentPattern(string);
//        if (component==PATHQUERY) {
//            pattern[Parser.invFields[PATH]] = pattern[Parser.invFields[QUERY]] = p;
//        } else {
            pattern[Parser.invFields[component]] = p;
//        }
    }

    private String reserved[] = new String[Parser.fields.length-1];
    /**
     * The given subDelims have syntactic use within this
     * component in this scheme, and must be %-escaped
     * for non-syntactic purposes. For the other subDelims
     * the percent-escaped form, and the normal form are
     * equivalent.
     */
    public void setReserved(int component, String subDelims) {
        if (component==PATHQUERY) {
            setReserved(PATH,subDelims);
            setReserved(QUERY,subDelims);
        } else {
           reserved[Parser.invFields[component]] = subDelims;
        }
    }
// TODO dns part of scheme spec ....
    public void analyse(Parser parser, int range) {
       
        ComponentPattern patt = pattern[Parser.invFields[range]];
        if (patt != null) {
            patt.analyse(parser,range);
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
 
