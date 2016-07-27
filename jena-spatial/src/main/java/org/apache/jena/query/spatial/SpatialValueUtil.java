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

package org.apache.jena.query.spatial;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.impl.LiteralLabel ;
import org.apache.jena.sparql.util.NodeUtils ;

public class SpatialValueUtil {

    /** Does the LiteralLabel look like a decimal? 
     * (Maybe a string - if so, OK if it parses as a decimal)
     */
	public static boolean isDecimal(Node n) {
	    if ( NodeUtils.isSimpleString(n) || NodeUtils.isLangString(n) ) {
	        try {
                Double.parseDouble(n.getLiteralLexicalForm()) ;
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
	    }
	    
		RDFDatatype dtype = n.getLiteralDatatype();
		if ((dtype.equals(XSDDatatype.XSDfloat))
				|| (dtype.equals(XSDDatatype.XSDdecimal))
				|| (dtype.equals(XSDDatatype.XSDdouble) || (dtype
						.equals(XSDDatatype.XSDinteger))))
			return true;
		return false;
	}

	public static boolean isWKTLiteral(LiteralLabel literal) {
		RDFDatatype dtype = literal.getDatatype();
		if (dtype == null)
			return false;
		if (dtype.getURI().equals(
				EntityDefinition.geosparql_wktLiteral.getURI()))
			return true;
		return false;
	}
}
