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

package org.apache.jena.sparql.util ;

import java.math.BigDecimal ;

import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;

/** Miscellaneous operations - not query specific */

public class Utils {
    /** @deprecated Use {@link XSDFuncOp#canonicalDecimalStr}. */
    @Deprecated
    static public String stringForm(BigDecimal decimal) {
        return XSDFuncOp.canonicalDecimalStr(decimal);
    }

    static public String stringForm(double d) {
        if ( Double.isInfinite(d) ) {
            if ( d < 0 )
                return "-INF" ;
            return "INF" ;
        }

        if ( Double.isNaN(d) )
            return "NaN" ;

        // Otherwise, SPARQL form always has "e0"
        String x = Double.toString(d) ;
        if ( (x.indexOf('e') != -1) || (x.indexOf('E') != -1) )
            return x ;
        // Renormalize?
        return x + "e0" ;
    }

    static public String stringForm(float f) {
        if ( Float.isInfinite(f) ) {
            if ( f < 0 )
                return "-INF" ;
            return "INF" ;
        }
        
        // No SPARQL short form.
        return Float.toString(f) ;
    }
}
