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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.iri.IRIComponents ;
import org.apache.jena.iri.ViolationCodes ;


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


    @Override
	public boolean applies(String scheme) {
		return name().equalsIgnoreCase(scheme);
	}
	
    private List<String> dUris = new ArrayList<>();
    private List<String> dDefnText = new ArrayList<>();
    private List<String> dDefnHtml = new ArrayList<>();
    @Override
    public void addDefinition(String uri, String defn, String defnHtml) {
        dUris.add(uri);
        dDefnText.add(defn);
        dDefnHtml.add(defnHtml);
    }

    @Override
    public void setDNS(boolean b) {
        usesDNS = b;
    }

    @Override
    public void port(int i) {
        port = i;
    }


    private ComponentPattern pattern[] = new ComponentPattern[Parser.fields.length];
    
    @Override
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
    @Override
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
