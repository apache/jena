/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIComponents;
import com.hp.hpl.jena.iri.Violation;


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


    public int getViolationCode() {
        return code;
    }

    public IRI getIRI() {
        return iri;
    }

    public int getComponent() {
        return slot;
    }
    
    public String component() {
    	return componentName(slot);
    }

    public String codeName() {
        return PatternCompiler.errorCodeName(code);
    }

    public boolean isError() {
        return (iri.getSchemeSpec().getMask(false) & (1l << code)) != 0;
    }

    public String getShortMessage() {
        return "Code: " + code + "/"+ codeName() + " in "+component() +": " +
              description();

    }

    private String description() {
		ViolationCodeInfo info = ViolationCodeInfo.all[code];
		if (info==null)
			return "internal error: description of error not found";
		return info.description(slot,iri.getFactory());
	}


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

	public String getSpecificationURL() {
        // TODO Auto-generated method stub
        return null;
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
 
