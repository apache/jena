/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.ext.xerces.impl.dv.xs;

import org.apache.jena.ext.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.jena.ext.xerces.impl.dv.ValidationContext;
import org.apache.jena.ext.xerces.impl.dv.util.ByteListImpl;
import org.apache.jena.ext.xerces.impl.dv.util.HexBin;

/**
 * Represent the schema type "hexBinary"
 *
 * {@literal @xerces.internal} 
 *
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @author Sandy Gao, IBM
 *
 * @version $Id: HexBinaryDV.java 446745 2006-09-15 21:43:58Z mrglavas $
 */
public class HexBinaryDV extends TypeValidator {

    @Override
    public short getAllowedFacets(){
        return (XSSimpleTypeDecl.FACET_LENGTH | XSSimpleTypeDecl.FACET_MINLENGTH | XSSimpleTypeDecl.FACET_MAXLENGTH | XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_ENUMERATION | XSSimpleTypeDecl.FACET_WHITESPACE );
    }

    @Override
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException {
        byte[] decoded = HexBin.decode(content);
        if (decoded == null)
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "hexBinary"});

        return new XHex(decoded);
    }

    // length of a binary type is the number of bytes
    @Override
    public int getDataLength(Object value) {
        return ((XHex)value).getLength();
    }

    private static final class XHex extends ByteListImpl {

        public XHex(byte[] data) {
            super(data);
        }
        @Override
        public synchronized String toString() {
            if (canonical == null) {
                canonical = HexBin.encode(data);
            }
            return canonical;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof XHex))
                return false;
            byte[] odata = ((XHex)obj).data;
            int len = data.length;
            if (len != odata.length)
                return false;
            for (int i = 0; i < len; i++) {
                if (data[i] != odata[i])
                    return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int hash = 0;
            for (int i = 0; i < data.length; ++i) {
                hash = hash * 37 + ((data[i]) & 0xff);
            }
            return hash;
        }
    }
}
