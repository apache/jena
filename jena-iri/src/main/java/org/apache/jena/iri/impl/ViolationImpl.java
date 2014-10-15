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

import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIComponents ;
import org.apache.jena.iri.Violation ;


public class ViolationImpl extends Violation {
	static String componentNames[];
    static String componentName(int j) {
		if (componentNames == null) {
			componentNames = PatternCompiler.constantsFromClass(IRIComponents.class, 40);
        }
        return componentNames[j];
    }
    final private int code;
    final private int slot;
//    int index;
    final private AbsIRIImpl iri;
    
//    public IRIImplUncheckedException(String iri,int code, int slot, int charIndex) {
//        this.iri = iri;
//        this.code = code;
//        this.slot = slot;
//        this.index = charIndex;
//    }
    
    public ViolationImpl(IRI iri, int slot, int code) {
      this.iri = (AbsIRIImpl)iri;
      this.code = code;
      this.slot = slot;
    }

    @Override
    public int getViolationCode() {
        return code;
    }

    @Override
    public IRI getIRI() {
        return iri;
    }

    @Override
    public int getComponent() {
        return slot;
    }
    
    @Override
    public String component() {
    	return componentName(slot);
    }

    @Override
    public String codeName() {
        return PatternCompiler.errorCodeName(code);
    }

    @Override
    public boolean isError() {
        return (iri.getSchemeSpec().getMask(false) & (1l << code)) != 0;
    }

    @Override
    public String getShortMessage() {
        return  "<" + getIRI() + "> Code: " + code + "/"+ codeName() + " in "+component() +": " +
              description();

    }

    private String description() {
		ViolationCodeInfo info = ViolationCodeInfo.all[code];
		if (info==null)
			return "internal error: description of error not found";
		return info.description(slot,iri);
	}


    @Override
	public String getLongMessage() {
        return "<" + getIRI() + "> Code: " + code + "/"+ codeName()  +
        " in "+component() + ": " +description() +
        " see: "
        + specs();
    }

    private String specs() {
		ViolationCodeInfo info = ViolationCodeInfo.all[code];
		if (info==null)
			return "(null)";
		return info.specs(slot,iri.getFactory(), iri.getScheme());
	}

    @Override
	public String getSpecificationURL() {
        // TODO getSpecificationURL
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
