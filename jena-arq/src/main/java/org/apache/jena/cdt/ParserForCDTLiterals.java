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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.parser.CDTLiteralParser;
import org.apache.jena.cdt.parser.ParseException;
import org.apache.jena.cdt.parser.TokenMgrError;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.RiotLib;

public class ParserForCDTLiterals
{
	public static List<CDTValue> parseListLiteral( final String lex ) {
		return parseListLiteral( RiotLib.dftProfile(), lex );
	}

	public static List<CDTValue> parseListLiteral( final ParserProfile pp, final String lex ) {
		try ( Reader reader = new StringReader(lex) ) {
		    return parseListLiteral(pp, reader);
		} catch ( final IOException e ) {
			throw new CDTLiteralParseException("Closing the reader caused an exception.", e);
		}
	}

	public static List<CDTValue> parseListLiteral( final Reader reader ) {
		return parseListLiteral( RiotLib.dftProfile(), reader );
	}

	public static List<CDTValue> parseListLiteral( final ParserProfile pp, final Reader reader ) {
		final CDTLiteralParser parser = new CDTLiteralParser(reader);
		parser.setProfile(pp);

		final List<CDTValue> list;
		try {
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

		return list;
	}

	public static Map<CDTKey,CDTValue> parseMapLiteral( final String lex ) {
		return parseMapLiteral( RiotLib.dftProfile(), lex );
	}

	public static Map<CDTKey,CDTValue> parseMapLiteral( final ParserProfile pp, final String lex ) {
	    try ( Reader reader = new StringReader(lex) ) {
	        return parseMapLiteral(pp, reader);
	    } catch ( final IOException e ) {
			throw new CDTLiteralParseException("Closing the reader caused an exception.", e);
		}
	}

	public static Map<CDTKey,CDTValue> parseMapLiteral( final Reader reader ) {
		return parseMapLiteral( RiotLib.dftProfile(), reader );
	}

	public static Map<CDTKey,CDTValue> parseMapLiteral( final ParserProfile pp, final Reader reader ) {
		final CDTLiteralParser parser = new CDTLiteralParser(reader);
		parser.setProfile(pp);

		final Map<CDTKey,CDTValue> map;
		try {
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

		return map;
	}

}
