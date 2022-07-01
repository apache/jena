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

package org.apache.jena.riot.system;

import org.apache.jena.iri.IRIFactory;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import org.apache.jena.irix.SetupJenaIRI;

/**
 * Settings for the jena-iri used in Jena  package.
 * <p>
 * See package {@code org.apache.jena.irix}.
 * <p>
 * Use {@link IRIs} for operations such as "resolve(string, string)", and {@link IRIx} for {@code IRI}.
 * Use {@link SetupJenaIRI} if direct use of jena-iri setup is required.
 *
 * @deprecated Use {@link IRIs} and {@link IRIx}.
 */
@Deprecated
public final class IRIResolver
{
    private IRIResolver() {}

    // Currently, Jena 4.0.0, these are the same setup.
    private static final IRIFactory iriFactoryInst = SetupJenaIRI.iriFactory();
    private static final IRIFactory iriCheckerInst = SetupJenaIRI.iriCheckerFactory();

    /**
     * The IRI setup, focused on parsing and languages.
     */
    public static IRIFactory iriFactory() {
        return iriFactoryInst;
    }

    /**
     * The IRI setup for use in RDF/XML parsing.
     */
    public static IRIFactory iriFactory_RDFXML() {
        // Used in ReaderRiotRDFXML
        return iriFactory();
    }

    /**
     * An IRIFactory with more detailed warnings.
     */
    public static IRIFactory iriCheckerFactory() {
        return iriCheckerInst;
    }
}
