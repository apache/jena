/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;


import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactoryI;
import com.hp.hpl.jena.iri.Violation;

abstract public class AbsIRIFactoryImpl implements IRIFactoryI {

    protected abstract IRIFactoryImpl getFactory();
    public AbsIRIFactoryImpl() {
    }

//    public IRI create(String s) {
//        return create(s);
//    }

    public IRI create(String s) {
        return create(new IRIImpl(getFactory(),s )
//                ,
//                throwEx?AbsIRIImpl.ALL_EXCEPTIONS:
//                AbsIRIImpl.NO_EXCEPTIONS)
                );
    }

    public IRI construct(String s) throws IRIException {
      return throwAnyErrors(create(s));
    }

    public IRI construct(IRI i) throws IRIException {
        return throwAnyErrors(create(i));
        
//     
//     try {
//     return create(i,true);
//     } catch (Violation e) {
//     throw new IRIImplException(e);
//     }
    }
    protected IRI throwAnyErrors(IRI rslt) throws IRIException {
        if (rslt.hasViolation(false)) {
            throw new IRIImplException((Violation)rslt.violations(false).next());
//            Iterator it = rslt.exceptions();
//            while (it.hasNext()){
//                Violation v = (Violation)it.next();
//                if (v.isError())
//                    throw new IRIImplException(v);
//            } 
        }
        return rslt;
    }
//    public IRI create(IRI i) {
//        return create(i);
//    }
    abstract public IRI create(IRI i);
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
 
