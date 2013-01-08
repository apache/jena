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

package org.openjena.riot;

import org.apache.jena.riot.RDFLanguages ;

/** @deprecated Use {@link RDFLanguages} or {@link org.apache.jena.riot.Lang} */
@Deprecated
public class Lang
{
    /** @deprecated Use constant from {@link RDFLanguages} */
    @Deprecated public static final org.apache.jena.riot.Lang RDFXML = RDFLanguages.RDFXML ; 
    /** @deprecated Use constant from {@link RDFLanguages} */
    @Deprecated public static final org.apache.jena.riot.Lang NTRIPLES = RDFLanguages.NTRIPLES ; 
    /** @deprecated Use constant from {@link RDFLanguages} */
    @Deprecated public static final org.apache.jena.riot.Lang N3 = RDFLanguages.N3 ; 
    /** @deprecated Use constant from {@link RDFLanguages} */
    @Deprecated public static final org.apache.jena.riot.Lang TURTLE = RDFLanguages.TURTLE ; 
    /** @deprecated Use constant from {@link RDFLanguages} */
    @Deprecated public static final org.apache.jena.riot.Lang RDFJSON = RDFLanguages.RDFJSON ; 
    /** @deprecated Use constant from {@link RDFLanguages} */
    @Deprecated public static final org.apache.jena.riot.Lang NQUADS = RDFLanguages.NQUADS ; 
    /** @deprecated Use constant from {@link RDFLanguages} */
    @Deprecated public static final org.apache.jena.riot.Lang TRIG = RDFLanguages.TRIG ; 
}
