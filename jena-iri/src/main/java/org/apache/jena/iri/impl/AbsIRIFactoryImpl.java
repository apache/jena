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
import org.apache.jena.iri.IRIException ;
import org.apache.jena.iri.IRIFactoryI ;

abstract public class AbsIRIFactoryImpl implements IRIFactoryI {

    protected abstract IRIFactoryImpl getFactory();
    public AbsIRIFactoryImpl() {
    }

//    public IRI create(String s) {
//        return create(s);
//    }

    @Override
    public IRI create(String s) {
        return create(new IRIImpl(getFactory(),s )
//                ,
//                throwEx?AbsIRIImpl.ALL_EXCEPTIONS:
//                AbsIRIImpl.NO_EXCEPTIONS)
                );
    }
    
    //@Override
    @Override
    public IRI construct(String s) throws IRIException {
      return throwAnyErrors(create(s));
    }

    //@Override
    @Override
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
            throw new IRIImplException(rslt.violations(false).next());
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
    @Override
    abstract public IRI create(IRI i);
}
