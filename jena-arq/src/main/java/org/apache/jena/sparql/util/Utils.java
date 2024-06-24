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

/**
 * Miscellaneous operations - not query specific
 * @deprecated Use XSDNumUtils
 */
@Deprecated(forRemoval = true)
public class Utils {

    /**
     * @deprecated Use {@link XSDNumUtils#stringForm(BigDecimal)}
     */
    @Deprecated(forRemoval = true)
    public static String stringForm(BigDecimal decimal) {
        return XSDNumUtils.stringForm(decimal);
    }

    /**
     * @deprecated Use {@link XSDNumUtils#stringForm(double)}
     */
    @Deprecated(forRemoval = true)
    public static String stringForm(double d) {
        return XSDNumUtils.stringForm(d);
    }

    /**
     * @deprecated Use {@link XSDNumUtils#stringForm(float)}
     */
    @Deprecated(forRemoval = true)
    public static String stringForm(float f) {
        return XSDNumUtils.stringForm(f);
    }
}
