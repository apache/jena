/**
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

package com.hp.hpl.jena.sparql.resultset;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.io.PeekReader;
import org.apache.jena.atlas.iterator.PeekIterator;
import org.apache.jena.atlas.json.io.parser.TokenizerJSON;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.TokenType;
import org.apache.jena.riot.RiotParseException ;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;

/**
 * Streaming Iterator over SPARQL JSON results, not yet fully implemented (see JENA-267)
 * <p>
 * Creating the Iterator automatically causes it to parse a small chunk of the stream to determine the variables in the result set either by reading the header or reading some portion of the results if the results appear before the header since JSON does not guarantee the order of keys within an object
 * </p>
 */
public class JSONInputIterator extends QueryIteratorBase {
	
	private InputStream input;

	private boolean isBooleanResults = false, boolResult = false, headerSeen = false;
	private Binding binding = null;
	private TokenizerJSON tokens;
	private PeekIterator<Token> peekIter;
	
	private Queue<Binding> cache = new LinkedList<>();
	private Set<String> vars = new HashSet<>();
	
	/**
	 * Creates a SPARQL JSON Iterator
	 * <p>
	 * Automatically parses some portion of the input to determine the variables in use
	 * </p>
	 */
	public JSONInputIterator(InputStream input)
	{
		this.input = input;
		this.tokens = new TokenizerJSON(PeekReader.makeUTF8(input));
		this.peekIter = new PeekIterator<>(this.tokens);
		
		//We should always parse the first little bit to see the head stuff or to cache a chunk of results and infer the headers
		//Primarily we are trying to find out what the variables are
		preParse();
	}
	
	/**
	 * Returns the variables present in the result sets
	 */
	public Iterator<String> getVars()
	{
		return vars.iterator();
	}
	
	/**
	 * Gets whether the SPARQL JSON represents a boolean result set
	 */
	public boolean isBooleanResult()
	{
		return isBooleanResults;
	}

	/**
	 * Does the pre-parsing which attempts to read the header of the results file and determine variables present
	 * <p>
	 * If the header is encountered first then we read this, if the results are encountered first we parse the first 100 results and determine the variables present from those instead
	 * </p> 
	 */
	private void preParse()
	{
		//First off the { to start the object
		expect("Expected the start of the JSON Results Object", TokenType.LBRACE);
		
		//Then expect to see a Property Name
		//Loop here because we might see some things we can discard first
		do
		{
			if (!isPropertyName())
			{
				Token t = nextToken();
				String name = t.getImage();
				checkColon();
				
				if (name.equals("head"))
				{
					if (headerSeen) exception(t, "Invalid duplicate header property");
					parseHeader();
					//Continue afterwards because we want to be in place to start streaming results
				}
				else if (name.equals("boolean"))
				{
					parseBoolean();
					//Afterwards we continue because we want to see an empty head
				}
				else if (name.equals("results"))
				{
					if (isBooleanResults) exception(t, "Encountered results property when boolean property has already been countered");
					
					//Scroll to first result
					parseToFirstResult();
					
					//If we already saw the header then exit at this point
					if (headerSeen) return;

					//If not we're going to pre-cache some chunk of results so we can infer the variable names
					boolean complete = cacheResults(100);
					
					//If this exhausted the result set then we can continue looking for the header
					//Otherwise we should exit as we may eventually see the header later...
					if (!complete) 
					{
						//TODO Now determine variables present from this
						return;
					}
				}
				else
				{
					ignoreValue();
				}
				checkComma(TokenType.RBRACE);
			}
			else if (lookingAt(TokenType.RBRACE))
			{
				//We hit the end of the result object already
				if (!headerSeen) exception(peekToken(), "End of JSON Results Object encountered before a valid header was seen");
				nextToken();
				
				//Shouldn't be any further content
				if (!lookingAt(TokenType.EOF)) exception(peekToken(), "Unexpected content after end of JSON Results Object");
				
				//Can stop our initial buffering at this stage
				return;
			}
			else
			{
				exception(peekToken(), "Expected a JSON property name but got %s", peekToken());
			}
		} while (true);
	}
	
	private void parseHeader()
	{
		do
		{
			if (isPropertyName())
			{
				Token t = nextToken();
				String name = t.getImage();
				checkColon();
				
				if (name.equals("vars"))
				{
					parseVars();
				}
				else if (name.equals("link"))
				{
					//Throw away the links
					skipLinks();
				}
				else
				{
					exception(t, "Unexpected property %s encountered in head object", name);
				}
				checkComma(TokenType.RBRACE);
			} 
			else if (lookingAt(TokenType.RBRACE))
			{
				nextToken();
				return;
			}
			else
			{
				exception(peekToken(), "Unexpected Token encountered while parsing head object");
			}
		} while (true);
	}
	
	private void parseVars()
	{
		if (lookingAt(TokenType.LBRACKET))
		{
			nextToken();
			vars.clear();
			do
			{
				if (lookingAt(TokenType.STRING1) || lookingAt(TokenType.STRING2))
				{
					Token t = nextToken();
					String var = t.getImage();
					vars.add(var);
					checkComma(TokenType.RBRACKET);
				}
				else if (lookingAt(TokenType.RBRACKET))
				{
					nextToken();
					return;
				}
				else
				{
					exception(peekToken(), "Unexpected Token encountered while parsing the variables list in the head object");
				}
			} while (true);
		}
		else
		{
			exception(peekToken(), "Unexpected Token ecountered, expected a [ to start the array of variables in the head object");
		}
	}
	
	private void skipLinks()
	{
		if (lookingAt(TokenType.LBRACKET))
		{
			nextToken();
			do
			{
				if (lookingAt(TokenType.RBRACKET))
				{
					//End of links
					nextToken();
					return;
				}
				else if (lookingAt(TokenType.STRING1) || lookingAt(TokenType.STRING2))
				{
					//Ignore link and continue
					nextToken();
				}
				else
				{
					exception(peekToken(), "Unexpected Token when a Link URI was expected");
				}
				checkComma(TokenType.RBRACKET);
			} while (true);
		}
		else
		{
			exception(peekToken(), "Unexpected token when a [ was expected to start the list of URIs for a link property");
		}
	}
	
	private void parseToFirstResult()
	{
		if (lookingAt(TokenType.LBRACE))
		{
			nextToken();
			if (isPropertyName())
			{
				Token t = nextToken();
				String name = t.getImage();
				if (name.equals("bindings"))
				{
					checkColon();
					if (lookingAt(TokenType.LBRACKET))
					{
						nextToken();
					}
					else
					{
						exception(peekToken(), "Unexpected Token encountered, expected a [ for the start of the bindings array");
					}
				}
				else
				{
					exception(t, "Unexpected Token encountered, expected the bindings property");
				}
			}
			else
			{
				exception(peekToken(), "Unexpected Token ecnountered, expected the bindings property");
			}
		}
		else
		{
			exception(peekToken(), "Unexpected Token encountered, expected a { to start the results list object");
		}
	}
		
	private void parseToEnd() {
		//TODO Parse through to end of the JSON document consuming the header if we haven't seen it already
		checkComma(TokenType.RBRACE);
	}
	
	private void ignoreValue() {
		if (isPropertyName()) {
			//Just a string value so can discard and then check for the subsequent comma
			nextToken();
			checkComma(TokenType.RBRACE);
		} else if (lookingAt(TokenType.DECIMAL) || lookingAt(TokenType.INTEGER) || lookingAt(TokenType.DOUBLE) || lookingAt(TokenType.KEYWORD))	{
			//Just a numeric/keyword (boolean) value do discard and check for subsequent comma
			nextToken();
			checkComma(TokenType.RBRACE);
		} else if (lookingAt(TokenType.LBRACE)) {
			//Start of an Object
			nextToken();
			
			//TODO We should really care about the syntactic validity of objects we are ignoring but that seems like a bit too much effort
			int openBraces = 1;
			while (openBraces >= 1)
			{
				Token next = nextToken();
				if (next.getType().equals(TokenType.LBRACE)) {
					openBraces++;
				} else if (next.getType().equals(TokenType.RBRACE)) {
					openBraces--;
				}
			}
			checkComma(TokenType.RBRACE);
		} else if (lookingAt(TokenType.LBRACKET)) {
			//Start of an Array
			nextToken();
			
			//TODO We should really care about the syntactic validity of objects we are ignoring but that seems like a bit too much effort
			int openBraces = 1;
			while (openBraces >= 1)
			{
				Token next = nextToken();
				if (next.getType().equals(TokenType.LBRACKET)) {
					openBraces++;
				} else if (next.getType().equals(TokenType.RBRACKET)) {
					openBraces--;
				}
			}
			checkComma(TokenType.RBRACE);
		} else {
			exception(peekToken(), "Unexpected Token");
		}
	}
	
	/**
	 * Caches the first N results so we can infer variables, indicates whether the caching exhausted the result set
	 * @param n Number of results to cache
	 */
	private boolean cacheResults(int n)
	{
		for (int i = 0; i < n; i++)
		{
			if (parseNextBinding())
			{
				this.cache.add(this.binding);
				this.binding = null;
			}
			else
			{
				return true;
			}
		}
		return false;
	}
	
	private void parseBoolean()
	{
		isBooleanResults = true;
		if (lookingAt(TokenType.KEYWORD))
		{
			Token t = nextToken();
			String keyword = t.getImage();
			if (keyword.equals("true"))
			{
				boolResult = true;
			}
			else if (keyword.equals("false"))
			{
				boolResult = false;
			}
			else
			{
				exception(t, "Unexpected keyword %s encountered, expected true or false", keyword);
			}
		}
		else
		{
			exception(peekToken(), "Unexpected token when a true/false keyword was expected for the value of the boolean property");
		}
	}
	
	@Override
	public void output(IndentedWriter out, SerializationContext sCxt) {
		// Not needed - only called as part of printing/debugging query plans.
		out.println("JSONInputIterator") ;
	}

	@Override
	protected boolean hasNextBinding() {
		if (isBooleanResults) return false;
		
		if (this.input != null)
		{
			if (this.cache.size() > 0) {
				this.binding = this.cache.remove();
				return true;
			} else if (this.binding == null) {
				return this.parseNextBinding();
			} else {
				return true;
			}
		}
		else
		{
			return false;
		}
	}
	
	private boolean parseNextBinding() {
		if (lookingAt(TokenType.LBRACE))
		{
			nextToken();
			BindingMap b = BindingFactory.create();
			do
			{
				if (isPropertyName())
				{
					Token t = nextToken();
					String var = t.getImage();
					checkColon();
										
					Node n = parseNode();
					b.add(Var.alloc(var), n);
					
					checkComma(TokenType.RBRACE);
				}
				else if (lookingAt(TokenType.RBRACE))
				{
					nextToken();
					checkComma(TokenType.RBRACKET);
					break;
				}
				else
				{
					exception(peekToken(), "Unexpected Token encountered, expected a property name to indicate the value for a variable");
				}
			} while (true);
			
			this.binding = b;
			return true;
		}
		else if (lookingAt(TokenType.RBRACKET))
		{
			//End of Bindings Array
			nextToken();
			if (lookingAt(TokenType.RBRACE))
			{
				nextToken();
				parseToEnd();
			}
			else
			{
				exception(peekToken(), "Unexpected Token encountered, expected a } to end the results object");
			}
		}
		else
		{
			exception(peekToken(), "Unexpected Token encountered, expected a { for the start of a binding of ] to end the array of bindings");
		}
		return false;
	}
	
	private Node parseNode() {
		String type, value, lang, datatype;
		type = value = lang = datatype = null;
		
		if (lookingAt(TokenType.LBRACE))
		{
			Token pos = nextToken();
			
			//Collect the Properties
			do
			{
				if (isPropertyName())
				{
					Token t = nextToken();
					String name = t.getImage();
					checkColon();
					
					if (name.equals("type")) {
						if (type != null) exception(t, "Illegal duplicate type property");
						type = parseNodeInfo("type");
					} else if (name.equals("value")) {
						if (value != null) exception(t, "Illegal duplicate value property");
						value = parseNodeInfo("value");
					} else if (name.equals("datatype")) {
						if (datatype != null) exception(t, "Illegal duplicate datatype property");
						datatype = parseNodeInfo("datatype");
					} else if (name.equals("xml:lang")) {
						if (lang != null) exception(t, "Illegal duplicate xml:lang property");
						lang = parseNodeInfo("xml:lang");
					} else {
						exception(t, "Unexpected Property Name '%s', expected one of type, value, datatype or xml:lang", name);
					}					
				}
				else if (lookingAt(TokenType.RBRACE))
				{
					nextToken();
					break;
				}
				else
				{
					exception(peekToken(), "Unexpected Token, expected a property name as part of a Node object");
				}
			} while (true);
			
			//Error if missing type or value
			if (type == null) exception(pos, "Encountered a Node object with no type property");
			if (value == null) exception(pos, "Encountered a Node object with no value property");
			
			//Generate a Node based on the properties we saw
			if (type.equals("uri")) {
				return NodeFactory.createURI(value);
			} else if (type.equals("literal")) {
				if (datatype != null) {
					return NodeFactory.createLiteral(value, TypeMapper.getInstance().getSafeTypeByName(datatype));
				} else if (lang != null) {
					return NodeFactory.createLiteral(value, lang, false);
				} else {
					return NodeFactory.createLiteral(value);
				}
			} else if (type.equals("bnode")) {
				return NodeFactory.createAnon(new AnonId(value));
			} else {
				exception(pos, "Encountered a Node object with an invalid type value '%s', expected one of uri, literal or bnode", type);
			}
		}
		else
		{
			exception(peekToken(), "Unexpected Token, expected a { for the start of a Node object");
		}
		return null;
	}
	
	private String parseNodeInfo(String name) {
		if (lookingAt(TokenType.STRING1) || lookingAt(TokenType.STRING2))
		{
			Token t = nextToken();
			String value = t.getImage();
			checkComma(TokenType.RBRACE);
			return value;
		}
		else
		{
			exception(peekToken(), "Unexpected Token, expected a string as the value for the %s property", name);
			return null;
		}
	}

	@Override
	protected Binding moveToNextBinding() {
        if (!hasNext()) throw new NoSuchElementException() ;
        Binding b = this.binding;
        this.binding = null ;
        return b;
	}

	@Override
	protected void closeIterator() {
		IO.close(input);
		input = null;
	}

	@Override
	protected void requestCancel() {
		//Don't need to do anything special to cancel
		//Superclass should take care of that and call closeIterator() where we do our actual clean up
	}

	// JSON Parsing Helpers taken from LangRDFJSON
	
    private boolean isPropertyName()
	{
		return lookingAt(TokenType.STRING1) || lookingAt(TokenType.STRING2);
	}

    private Token checkValidForStringProperty(String property)
    {
    	Token t = null;
    	if (lookingAt(TokenType.STRING1) || lookingAt(TokenType.STRING2))
    	{
    		t = nextToken();
    	}
    	else
    	{
    		exception(peekToken(), "JSON Values given for property " + property + " must be Strings") ;
    	}
    	return t;
    }

	private void checkColon()
	{
		if (!lookingAt(TokenType.COLON))
		{
			exception(peekToken(), "Expected a : character after a JSON Property Name but got %s", peekToken()) ;
		}
		nextToken() ;
	}
	
	private void checkComma(TokenType terminator)
	{
		if (lookingAt(TokenType.COMMA))
		{
			nextToken();
		}
		else if (lookingAt(terminator))
		{
			return;
		}
		else
		{
			exception(peekToken(), "Unexpected Token encountered, expected a , or a %s", terminator);
		}
	}
	
	// Streaming Parsing Helper Functions nicked from LangEngine
	
    // ---- Managing tokens.
    
    protected final Token peekToken()
    {
        // Avoid repeating.
        if ( eof() ) return tokenEOF ;
        return peekIter.peek() ;
    }
    
    // Set when we get to EOF to record line/col of the EOF.
    private Token tokenEOF = null ;

    protected final boolean eof()
    {
        if ( tokenEOF != null )
            return true ;
        
        if ( ! moreTokens() )
        {
            tokenEOF = new Token(tokens.getLine(), tokens.getColumn()) ;
            tokenEOF.setType(TokenType.EOF) ;
            return true ;
        }
        return false ;
    }

    protected final boolean moreTokens() 
    {
        return peekIter.hasNext() ;
    }
    
    protected final boolean lookingAt(TokenType tokenType)
    {
        if ( eof() )
            return tokenType == TokenType.EOF ;
        if ( tokenType == TokenType.NODE )
            return peekToken().isNode() ;
        return peekToken().hasType(tokenType) ;
    }
    
    // Remember line/col of last token for messages 
    protected long currLine = -1 ;
    protected long currCol = -1 ;
    
    protected final Token nextToken()
    {
        if ( eof() )
            return tokenEOF ;
        
        // Tokenizer errors appear here!
        try {
            Token t = peekIter.next() ;
            currLine = t.getLine() ;
            currCol = t.getColumn() ;
            return t ;
        } catch (RiotParseException ex)
        {
            // Intercept to log it.
            raiseException(ex) ;
            throw ex ;
        }
        catch (AtlasException ex)
        {
            // Bad I/O
            RiotParseException ex2 = new RiotParseException(ex.getMessage(), -1, -1) ;
            raiseException(ex2) ;
            throw ex2 ;
        }
    }
    
    protected final void expectOrEOF(String msg, TokenType tokenType)
    {
        // DOT or EOF
        if ( eof() )
            return ;
        expect(msg, tokenType) ;
    }
    
    protected final void expect(String msg, TokenType ttype)
    {
        
        if ( ! lookingAt(ttype) )
        {
            Token location = peekToken() ;
            exception(location, msg) ;
        }
        nextToken() ;
    }

    protected final void exception(Token token, String msg, Object... args)
    { 
        if ( token != null )
            exceptionDirect(String.format(msg, args), token.getLine(), token.getColumn()) ;
        else
            exceptionDirect(String.format(msg, args), -1, -1) ;
    }
    
    protected final void exceptionDirect(String msg, long line, long col)
    { 
        raiseException(new RiotParseException(msg, line, col)) ;
    }
    
    protected final void raiseException(RiotParseException ex)
    { 
        throw new QueryException("Error passing SPARQL JSON results", ex);
    }
	
}

