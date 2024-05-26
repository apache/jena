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

package org.apache.jena.cdt;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.lang.extra.LangParserBase;

public class CDTLiteralParserBase extends LangParserBase
{
	@Override
	protected Node createBNode( final String label, final int line, final int column ) {
		// We need to cut away the leading '_:' of the given blank node label.
		// This is necessary because the Turtle parser does the same. If we
		// would not do it here for the blank node labels obtained from the
		// lexical forms of CDT literals, then the label-to-bnode mapping of
		// the shared parser state fails to map the same blank node identifiers
		// inside and outside of CDT literals to the same blank node.
		final String lbl = label.startsWith("_:") ? label.substring(2) : label;
		return super.createBNode(lbl, line, column);
	}

	@Override
	protected Node createLiteral( final String lex, final String langTag, final String datatypeURI, final int line, final int column ) {
		if ( CompositeDatatypeList.uri.equals(datatypeURI) ) {
			return profile.createTypedLiteral(lex, CompositeDatatypeList.type, 0L, 0L);
		}

		if ( CompositeDatatypeMap.uri.equals(datatypeURI) ) {
			return profile.createTypedLiteral(lex, CompositeDatatypeMap.type, 0L, 0L);
		}

		return super.createLiteral(lex, langTag, datatypeURI, line, column);
	}

}
