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

package org.apache.jena.riot.lang;

import org.apache.jena.atlas.json.io.parser.TokenizerJSON ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/**
 * See http://docs.api.talis.com/platform-api/output-types/rdf-json
 */
public class LangRDFJSON extends LangBase
{

	public LangRDFJSON(Tokenizer tokenizer, ParserProfile profile, StreamRDF dest)
	{
		super(tokenizer, profile, dest) ;
		if (!(tokenizer instanceof TokenizerJSON))
		{
			throw new IllegalArgumentException("Tokenizer for the RDF/JSON parser must be an instance of org.openjena.atlas.json.io.parser.TokenizerJSON") ;
		}
	}

	@Override
	public Lang getLang()   { return RDFLanguages.RDFJSON ; }

	@Override
	protected void runParser()
	{
		this.tryParseGraph() ;
	}

	private void tryParseGraph()
	{
		//Must be a { to start the JSON Object
		if (lookingAt(TokenType.LBRACE))
		{
			//Can discard the { safely
			nextToken() ;

			//Then want to try parsing triples
			tryParseTriples();

			//Ensure that there is a } to end the JSON Object and discard it
			if (!lookingAt(TokenType.RBRACE))
			{
				exception(peekToken(), "Expected a } character to end a JSON Object but got %s", peekToken()) ;
			}
			nextToken() ;

			//Should now be at the end of the file
			if (!lookingAt(TokenType.EOF))
			{
				exception(peekToken(), "Expected the end of the JSON but there is additional content beyond the end of the JSON Object") ;
			}
			nextToken();
		}
		else
		{
			exception(peekToken(), "Expected a { character to start a JSON Object but got %s", peekToken()) ;
		}
	}

	private void tryParseTriples()
	{
		//First we expect to see a Property Name which is the Subject for
		//our Triples
		//Note that subjectExpected starts as false because we can get an empty graph
		//in which case we won't see any subjects
		boolean subjectExpected = false;
		while (moreTokens())
		{
			if (lookingAt(TokenType.RBRACE))
			{
				if (subjectExpected)
				{
					exception(peekToken(), "Expected a Property Name after a comma to represent the Subject of the next block of triples but got %s", peekToken()) ;
				}

				//Otherwise this is the end of the JSON Object representing the Graph so just return
				return;
			}
			else if (isPropertyName())
			{
				subjectExpected = false;
				//Is a Property Name so represents a Subject
				Token t = nextToken() ;
				Node subj;
				if (t.getImage().startsWith("_:"))
				{
					subj = profile.createBlankNode(null, t.getImage().substring(2), t.getLine(), t.getColumn()) ;
				}
				else
				{
					subj = profile.createURI(t.getImage(), t.getLine(), t.getColumn()) ;
				}

				//Should always be a : after a Property Name
				checkColon() ;

				//Now try and parse the Predicate Object List
				tryParsePredicateObjectList(subj) ;

				//After the end of a Predicate Object List may optionally have a comma
				//to denote there are further
				if (lookingAt(TokenType.COMMA))
				{
					nextToken();
					subjectExpected = true;
				}
			}
			else
			{
				if (subjectExpected)
				{
					exception(peekToken(), "Expected a Property Name after a comma to represent the Subject of the next block of triples but got %s", peekToken()) ;
				}
				else
				{
					exception(peekToken(), "Expected either the end of the JSON Object (the } character) or a JSON Property Name (String) to set the Subject for some Triples but got %s", peekToken()) ;
				}
			}
		}
	}

	private void tryParsePredicateObjectList(Node subj)
	{
		//RDF/JSON defines a Predicate Object list to be a JSON Object
		//where each Predicate is a Property Name and the Object List
		//is a JSON Array

		//First must see the { to start the object which we can then discard
		if (!lookingAt(TokenType.LBRACE))
		{
			exception(peekToken(), "Expected a { character to start the JSON Object for a Predicate Object List but got a %s", peekToken()) ;
		}
		nextToken() ;

		//Then we must see a Property Name or the end of the predicate object list
		boolean first = true;
		boolean propertyNameExpected = true;
		while (true)
		{
			if (isPropertyName())
			{
				first = false;
				propertyNameExpected = false;
				Token t = nextToken();
				Node pred = profile.createURI(t.getImage(), t.getLine(), t.getColumn()) ;

				//Must be a : after Property Name
				checkColon() ;

				//Then we can try and parse the Object List
				tryParseObjectList(subj, pred) ;

				//After this we may optionally see a , which
				//means that there are more property names present
				//i.e. further predicates for this subject
				if (lookingAt(TokenType.COMMA))
				{
					nextToken() ;
					propertyNameExpected = true;
				}
			}
			else if (!first && lookingAt(TokenType.RBRACE))
			{
				//This is the end of the Predicate Object List Object
				if (propertyNameExpected)
				{
					exception(peekToken(), "Expected a further Property Name to represent a Predicate after a comma in a Predicate Object List but got %s", peekToken()) ;
				}
				nextToken();
				return;
			}
			else
			{
				if (propertyNameExpected)
				{
					exception(peekToken(), "Expected a Property Name to represent a Predicate as part of a Predicate Object List but got %s", peekToken()) ;
				}
				else
				{
					exception(peekToken(), "Expected a Property Name or the end of the Predicate Object List but got %s", peekToken()) ;
				}
			}
		}
	}

	private void tryParseObjectList(Node subj, Node pred)
	{
		//RDF/JSON defines an Object List to be a JSON Array where
		//the array may consist of JSON Objects which represent
		//the objects of Triples

		//First must see the [ to start the array
		if (lookingAt(TokenType.LBRACKET))
		{
			nextToken();

			boolean first = true;
			boolean objectExpected = true;
			while (true)
			{
				if (lookingAt(TokenType.LBRACE))
				{
					if (!objectExpected)
					{
						exception(peekToken(), "Expected the end of the JSON Array for the Object List as no comma was seen after the preceding } but got %s", peekToken());
					}

					first = false;
					objectExpected = false;

					//Parse the Object and Emit the Triple
					Node obj = tryParseObject();
					Triple t = profile.createTriple(subj, pred, obj, currLine, currCol) ;
					dest.triple(t) ;

					//After this may optionally see a comma to indicate
					//that there are further objects in the object list
					if (lookingAt(TokenType.COMMA))
					{
						nextToken();
						objectExpected = true;
					}
				}
				else if (!first && lookingAt(TokenType.RBRACKET))
				{
					//This is the end of the Object List Object
					if (objectExpected)
					{
						exception(peekToken(), "Expected a further JSON Object to represent an Object after a comma in a Object List but got %s", peekToken()) ;
					}
					nextToken();
					return;
				}
				else
				{
					if (objectExpected)
					{
						exception(peekToken(), "Expected a JSON Object to represent an Object as part of a Object List but got %s", peekToken()) ;
					}
					else
					{
						exception(peekToken(), "Expected a JSON Object or the end of the Object List but got %s", peekToken()) ;
					}
				}
			}
		}
		else
		{
			exception(peekToken(), "Expected a [ character to start a JSON Array for the Object List but got %s", peekToken()) ;
		}
	}

	private Node tryParseObject()
	{
		//RDF/JSON defines the Object of a Triple to be encoded as a
		//JSON Object
		//It has mandatory properties 'value' and 'type' plus optional
		//properties 'lang', 'xml:lang' and 'datatype'

		Node obj = null;
		Token value = null, type = null, lang = null, datatype = null;

		//First we expect to see the { character to start the JSON Object
		if (lookingAt(TokenType.LBRACE))
		{
			//Discard the {
			nextToken();

			//Then see a stream of tokens which are property value pairs
			//representing the properties of the object
			boolean first = true;
			boolean propertyNameExpected = true;
			while (true)
			{
				if (isPropertyName())
				{
					first = false;
					propertyNameExpected = false;

					Token t = nextToken();
					String name = t.getImage();

					//Must always be a : after a property name
					checkColon();

					//Is this one of our valid properties
                    switch ( name )
                    {
                        case "value":
                            if ( value == null )
                            {
                                value = checkValidForObjectProperty();
                            }
                            else
                            {
                                exception( t,
                                           "Encountered the value property on an Object when the value property has already been specified" );
                            }
                            break;
                        case "type":
                            if ( type == null )
                            {
                                type = checkValidForObjectProperty();
                            }
                            else
                            {
                                exception( t,
                                           "Encountered the type property on an Object when the type property has already been specified" );
                            }
                            break;
                        case "lang":
                        case "xml:lang":
                            if ( lang == null && datatype == null )
                            {
                                lang = checkValidForObjectProperty();
                            }
                            else
                            {
                                exception( t,
                                           "Encountered the %s property on an Object when lang/datatype has already been specified",
                                           name );
                            }
                            break;
                        case "datatype":
                            if ( lang == null && datatype == null )
                            {
                                datatype = checkValidForObjectProperty();
                            }
                            else
                            {
                                exception( t,
                                           "Encountered the %s property on an Object when lang/datatype has already been specified",
                                           name );
                            }
                            break;
                        default:
                            exception( t,
                                       "Unexpected Property Name %s encountered, expected one of value, type, lang or datatype",
                                       t.getImage() );
                            break;
                    }

					//After each Property Value pair we may optionally
					//see a comma to indicate further pairs are present
					if (lookingAt(TokenType.COMMA))
					{
						nextToken();
						propertyNameExpected = true;
					}
				}
				else if (!first && lookingAt(TokenType.RBRACE))
				{
					if (propertyNameExpected)
					{
						exception(peekToken(), "Expected a further Property Name to represent a property of the Object of a Triple after a comma but got %s", peekToken()) ;
					}
					break;
				}
				else
				{
					exception(peekToken(), "Expected a Property Name to define a property relating to the Object of a Triple but got %s", peekToken()) ;
				}
			}
			//Discard the } which terminated the Object
			nextToken();

			//Next up validate the tokens we've got
			if (type == null) exception(peekToken(), "Unable to parse the Object for a Triple from a JSON Object as the required 'type' property is not present") ;
			if (value == null) exception(peekToken(), "Unable to parse the Object for a Triple from a JSON Object as the required 'value' property is not present") ;

			//Use these to create the Object
			String typeStr = type.getImage();
            switch ( typeStr )
            {
                case "uri":
                    obj = profile.createURI( value.getImage(), value.getLine(), value.getColumn() );
                    break;
                case "bnode":
                    obj = profile.createBlankNode( null, value.getImage().substring( 2 ), value.getLine(),
                                                   value.getColumn() );
                    break;
                case "literal":
                    if ( lang != null )
                    {
                        obj = profile.createLangLiteral( value.getImage(), lang.getImage(), value.getLine(),
                                                         value.getColumn() );
                    }
                    else if ( datatype != null )
                    {
                        obj = profile.createTypedLiteral( value.getImage(), TypeMapper.getInstance().getSafeTypeByName(
                            datatype.getImage() ), value.getLine(), value.getColumn() );
                    }
                    else
                    {
                        obj = profile.createStringLiteral( value.getImage(), value.getLine(), value.getColumn() );
                    }
                    break;
                default:
                    exception( type,
                               "Unable to parse the Object for a Triple from a JSON Object as the value %s given for the 'type' property is not one of uri, bnode or literal",
                               typeStr );
                    break;
            }
		}
		else
		{
			exception(peekToken(), "Expected a { character to start a JSON Object to represent the Object of a Triple but got %s", peekToken()) ;
		}
		return obj;
	}

    private boolean isPropertyName()
	{
		return lookingAt(TokenType.STRING1) || lookingAt(TokenType.STRING2);
	}

    private Token checkValidForObjectProperty()
    {
    	Token t = null;
    	if (lookingAt(TokenType.STRING1) || lookingAt(TokenType.STRING2))
    		t = nextToken();
    	else
    		exception(peekToken(), "JSON Values given for properties for an Object must be Strings") ;
    	return t;
    }

	private void checkColon()
	{
		if (!lookingAt(TokenType.COLON))
			exception(peekToken(), "Expected a : character after a JSON Property Name but got %s", peekToken()) ;
		nextToken() ;
	}

}
