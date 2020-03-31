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

package org.apache.jena.riot.writer;

import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.util.Context;

/** Package-scoped utilities */
/*package*/ class WriterLib {

    static DirectiveStyle dftDirectiveStyle = DirectiveStyle.AT;

    // Determine the directive style (applies to PREFIX and BASE).
    /*package*/ static DirectiveStyle directiveStyle(Context context) {
        Object x = context.get(RIOT.symTurtleDirectiveStyle) ;
        if ( x == null ) {
            @SuppressWarnings("deprecation")
            String x1 = context.get(RIOT.symTurtlePrefixStyle) ;
            x = x1;
        }

        if ( x instanceof String ) {
            String s = (String)x ;
            DirectiveStyle style = DirectiveStyle.create(s);
            return style == null ? dftDirectiveStyle : style;
        }
        if ( x instanceof DirectiveStyle )
            return (DirectiveStyle)x ;

        // Default choice; includes null in context.
        return dftDirectiveStyle;
    }
}
