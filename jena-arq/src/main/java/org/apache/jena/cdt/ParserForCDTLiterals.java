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

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.parser.CDTLiteralParser;
import org.apache.jena.cdt.parser.ParseException;
import org.apache.jena.ttl.turtle.parser.TokenMgrError;
import org.apache.jena.util.FileUtils;

public class ParserForCDTLiterals
{
	public static List<CDTValue> parseListLiteral( final String lex, final boolean recursive ) {
		final Reader reader = new StringReader(lex);
		final List<CDTValue> result = parseListLiteral(reader, recursive);

		try { reader.close(); } catch ( final IOException e ) {
			throw new CDTLiteralParseException("Closing the reader caused an exception.", e);
		}

		return result;
	}

	public static List<CDTValue> parseListLiteral( final InputStream in, final boolean recursive ) {
		final Reader reader = FileUtils.asUTF8(in);
		final List<CDTValue> result = parseListLiteral(reader, recursive);

		try { reader.close(); } catch ( final IOException ex ) {
			throw new CDTLiteralParseException("Closing the reader caused an exception.", ex);
		}

		return result;
	}

	public static List<CDTValue> parseListLiteral( final Reader reader, final boolean recursive ) {
		final List<CDTValue> list;
		try {
			final CDTLiteralParser parser = new CDTLiteralParser(reader);
			list = parser.List();
		}
		catch ( final ParseException | TokenMgrError ex ) {
			throw new CDTLiteralParseException( ex.getMessage() );
		}
		catch ( final CDTLiteralParseException ex ) {
			throw ex;
		}
		catch ( final Throwable th ) {
			throw new CDTLiteralParseException( th.getMessage(), th );
		}

		if ( recursive && ! list.isEmpty() ) {
			for ( int i = 0; i < list.size(); i++ ) {
				final CDTValue v = list.get(i);
				if ( v.isNode() && v.asNode().isLiteral() ) {
					final String lex = v.asNode().getLiteralLexicalForm();
					final String dtURI = v.asNode().getLiteralDatatypeURI();

					if ( dtURI.equals(CompositeDatatypeList.uri) ) {
						final List<CDTValue> subList = parseListLiteral(lex, recursive);
						list.set( i, CDTFactory.createValue(subList) );
					}
					else if ( dtURI.equals(CompositeDatatypeMap.uri) ) {
						final Map<CDTKey,CDTValue> subMap = parseMapLiteral(lex, recursive);
						list.set( i, CDTFactory.createValue(subMap) );
					}
				}
			}
		}

		return list;
	}

	public static Map<CDTKey,CDTValue> parseMapLiteral( final String lex, final boolean recursive ) {
		final Reader reader = new StringReader(lex);
		final Map<CDTKey,CDTValue> result = parseMapLiteral(reader, recursive);

		try { reader.close(); } catch ( final IOException e ) {
			throw new CDTLiteralParseException("Closing the reader caused an exception.", e);
		}

		return result;
	}

	public static Map<CDTKey,CDTValue> parseMapLiteral( final InputStream in, final boolean recursive ) {
		final Reader reader = FileUtils.asUTF8(in);
		final Map<CDTKey,CDTValue> result = parseMapLiteral(reader, recursive);

		try { reader.close(); } catch ( final IOException ex ) {
			throw new CDTLiteralParseException("Closing the reader caused an exception.", ex);
		}

		return result;
	}

	public static Map<CDTKey,CDTValue> parseMapLiteral( final Reader reader, final boolean recursive ) {
		final Map<CDTKey,CDTValue> map;
		try {
			final CDTLiteralParser parser = new CDTLiteralParser(reader);
			map = parser.Map();
		}
		catch ( final ParseException | TokenMgrError ex ) {
			throw new CDTLiteralParseException( ex.getMessage() );
		}
		catch ( final CDTLiteralParseException ex ) {
			throw ex;
		}
		catch ( final Throwable th ) {
			throw new CDTLiteralParseException( th.getMessage(), th );
		}

		if ( recursive && ! map.isEmpty() ) {
			for ( final CDTKey key : map.keySet() ) {
				final CDTValue v = map.get(key);
				if ( v.isNode() && v.asNode().isLiteral() ) {
					final String lex = v.asNode().getLiteralLexicalForm();
					final String dtURI = v.asNode().getLiteralDatatypeURI();

					if ( dtURI.equals(CompositeDatatypeMap.uri) ) {
						final Map<CDTKey,CDTValue> subMap = parseMapLiteral(lex, recursive);
						map.put( key, CDTFactory.createValue(subMap) );
					}
					else if ( dtURI.equals(CompositeDatatypeList.uri) ) {
						final List<CDTValue> subList = parseListLiteral(lex, recursive);
						map.put( key, CDTFactory.createValue(subList) );
					}
				}
			}
		}

		return map;
	}

}
