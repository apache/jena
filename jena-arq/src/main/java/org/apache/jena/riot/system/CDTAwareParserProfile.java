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

import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.cdt.ParserForCDTLiterals;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.sparql.util.Context;

/**
 * This is a {@link ParserProfile} that supports parsing of CDT literals
 * that occur within the parsed file. The main point is to share the
 * {@link FactoryRDF} object of this parser profile with the parser of
 * these literals in order to get the same blank nodes for the same
 * blank node identifiers both within and outside of the literals, as
 * well as across multiple CDT literals that occur in the parsed file.
 */
public class CDTAwareParserProfile extends ParserProfileStd {

    public CDTAwareParserProfile( final FactoryRDF factory,
                                  final ErrorHandler errorHandler,
                                  final IRIxResolver resolver,
                                  final PrefixMap prefixMap,
                                  final Context context,
                                  final boolean checking,
                                  final boolean strictMode ) {
        super(factory, errorHandler, resolver, prefixMap, context, checking, strictMode);
    }

    @Override
    public Node createTypedLiteral( final String lex, final RDFDatatype datatype, final long line, final long col ) {
        // cdt:List and cdt:Map literals need to be treated in a special way
        // because we need to construct their value by parsing them using this
        // same parser profile; this is necessary to make sure that blank node
        // identifiers inside the lexical forms of multiple of these literals
        // within the same file are all mapped to the same blank node, and so
        // are all blank node identifers that occur directly in the file (i.e.,
        // ourside of CDT literals).

        if ( datatype.equals(CompositeDatatypeList.type) ) {
            return createListLiteral(lex);
        }

        if ( datatype.equals(CompositeDatatypeMap.type) ) {
            return createMapLiteral(lex);
        }

        return super.createTypedLiteral(lex, datatype, line, col);
    }

    protected Node createListLiteral( final String lex ) {
        // Attention: In contrast to the overridden createTypedLiteral function
        // in the superclass, for literals of the CDT datatypes we do not perform
        // a checkLiteral check because that would parse the lexical form of the
        // literal already once before doing the other parse to obtain the value.

        // parse the given lexical form using this same parser profile
        final List<CDTValue> value;
        try {
            value = ParserForCDTLiterals.parseListLiteral(this, lex);
        }
        catch ( final Exception ex ) {
            throw new DatatypeFormatException(lex, CompositeDatatypeList.type, ex);
        }

        // At this point we have both the lexical form (which, after the
        // parsing, we now know is well formed) and the corresponding value
        // of the literal. We create a LiteralLabel with both of them, for
        // the following reasons.
        // If we were to create the LiteralLabel with the lexical form only,
        // then the LiteralLabel implementation would parse that lexical form
        // again, which gives us an unnecessary performance penalty (in
        // particular for huge lists).
        // If we were to create the LiteralLabel with the value only, then the
        // LiteralLabel implementation would reproduce the lexical form, which
        // may not be identical to the lexical form with which we have created
        // the value here. The issue with that is that a SAMETERM comparison
        // would incorrectly return false.
        final LiteralLabel ll = LiteralLabelFactory.createIncludingValue(lex, value, CompositeDatatypeList.type);
        @SuppressWarnings("deprecation")
        Node n = NodeFactory.createLiteral(ll);
        return n;
    }

    protected Node createMapLiteral( final String lex ) {
        // Attention: In contrast to the overridden createTypedLiteral function
        // in the superclass, for literals of the CDT datatypes we do not perform
        // a checkLiteral check because that would parse the lexical form of the
        // literal already once before doing the other parse to obtain the value.

        // parse the given lexical form using this same parser profile
        final Map<CDTKey,CDTValue> value;
        try {
            value = ParserForCDTLiterals.parseMapLiteral(this, lex);
        }
        catch ( final Exception ex ) {
            throw new DatatypeFormatException(lex, CompositeDatatypeMap.type, ex);
        }

        // We create a LiteralLabel with both the lexical form and the value,
        // for the same reasons as described above in createListLiteral().
        final LiteralLabel ll = LiteralLabelFactory.createIncludingValue(lex, value, CompositeDatatypeMap.type);
        @SuppressWarnings("deprecation")
        Node n = NodeFactory.createLiteral(ll);
        return n;
    }

}
