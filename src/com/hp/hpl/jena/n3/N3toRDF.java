/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import antlr.collections.AST ;
import java.util.* ;
import com.hp.hpl.jena.rdf.model.* ;

import com.hp.hpl.jena.vocabulary.*;

/**
 * @author		Andy Seaborne
 * @version 	$Id: N3toRDF.java,v 1.5 2003-03-06 09:45:42 andy_seaborne Exp $
 */
public class N3toRDF implements N3ParserEventHandler
{
	static public boolean VERBOSE = false ;
	
	Model model ;
	Map prefixMap = new HashMap() ;

	// Maps URIref or a _:xxx bNode to a Resource
	Map resourceRef = new HashMap() ;
	// Maps URIref to Property
	Map propertyRef = new HashMap() ;
	
	// Well known namespaces
	
	static final String NS_rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#" ;
    static final String NS_rdfs = "http://www.w3.org/2000/01/rdf-schema#" ;
	
    static final String NS_W3_log = "http://www.w3.org/2000/10/swap/log#" ;
    static final String XMLLiteralURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral" ;

	String base = null ;
	final String anonPrefix = "_" ;
	
	public N3toRDF(Model m, String _base)
	{
		model = m ; base = _base ;
		if ( VERBOSE ) 
			System.out.println("N3toRDF: "+base) ;
	}
		
	
	public void startDocument() { }
	public void endDocument()   { }
	
	// When Jena exceptions are runtime, we will change this
	public void error(Exception ex, String message) 		{ throw new N3Exception(message) ; }
	public void error(String message) 						{ error(null, message) ; }
	public void warning(Exception ex, String message)		{ throw new N3Exception(message) ; }
	public void warning(String message)						{ warning(null, message) ; }
	public void deprecated(Exception ex, String message)	{ throw new N3Exception(message) ; }
	public void deprecated(String message)					{ deprecated(null, message) ; }
	
	public void startFormula(int line, String context)
	{
		error("Line "+line+": N3toRDF: All statements are asserted - no formulae in RDF") ;
	}
					
	public void endFormula(int line, String context) {}
	
	public void directive(int line, AST directive, AST[] args, String context)
	{
		if ( directive.getType() == N3Parser.AT_PREFIX )
		{
			// @prefix now 
			if ( args[0].getType() != N3Parser.QNAME )
			{
				error("Line "+line+": N3toRDF: Prefix directive does not start with a prefix! "+args[0].getText()+ "["+N3Parser.getTokenNames()[args[0].getType()]+"]") ;
				return ;
			}
					
			String prefix = args[0].getText() ;
			if ( prefix.endsWith(":") )
				prefix = prefix.substring(0,prefix.length()-1) ;
				
			if ( args[1].getType() != N3Parser.URIREF )
			{
				error("Line "+line+": N3toRDF: Prefix directive does not supply a URIref! "+args[1].getText()) ;
				return ;
			}
			
			String uriref = args[1].getText() ;
			if ( uriref.equals("") )
				uriref = base ;

			if ( uriref.equals("#") )
				uriref = base+"#" ;

			if ( VERBOSE )
				System.out.println(prefix+" => "+uriref) ;
			prefixMap.put(prefix, uriref) ;
			return ;
		}
		
		warning("Line "+line+": N3toRDF: Directive not recongized and ignored: "+directive.getText()) ;
		return ;
	}
	
	
	public void quad(int line, AST subj, AST prop, AST obj, String context)
	{
        // Syntax that reverses subject and object is done in the grammar

		if ( context != null )
			error("Line "+line+": N3toRDF: All statement are asserted - no formulae") ;
		
		try
		{
			// Converting N3 to RDF:
			// subject: must be a URIref or a bNode name
			// property: remove sugaring and then must be a URIref
			// object: can be a literal or a URIref or a bNode name
			// context must be zero (no formulae)
            
            // Lists: The parser creates list elements as sequnces of triples:
            //       anon  keyword_A list:List
            //       anon  list:first  ....
            //       anon  list:rest   resource
            // Where "resource" is nil for the last element of the list (generated first).
             
            // The properties are inm a unique namespace to distinguish them
            // from lists encoded explicitly, not with the () syntax. 

			int pType = prop.getType();
			String propStr = prop.getText();
            Property pNode = null ;
			
			switch (pType)
			{
				case N3Parser.ARROW_R :
					propStr = NS_W3_log + "implies";
					break;
				case N3Parser.ARROW_MEANS :
					propStr = NS_W3_log + "means";
					break;
				case N3Parser.ARROW_L :
					// Need to reverse subject and object
					propStr = NS_W3_log + "implies";
					AST tmp = obj; obj = subj; subj = tmp;
					break;
				case N3Parser.EQUAL :
					//propStr = NS_DAML + "equivalentTo";
					//propStr = damlVocab.equivalentTo().getURI() ;
                    pNode = OWL.sameAs ;
					break;
				case N3Parser.KW_A :
                    pNode = RDF.type ;
					break ;
				case N3Parser.QNAME:
                    ExpandedQName qn = new ExpandedQName(line, propStr);
                    pNode = model.createProperty(qn.firstPart, qn.secondPart) ;
                    break ;
				case N3Parser.URIREF:
                    break ;
                case N3Parser.TK_LIST_FIRST:
                    pNode = RDF.first ;
                break ;
                case N3Parser.TK_LIST_REST:
                    pNode = RDF.rest ;
                    break ;
                // Literals, parser generated bNodes (other bnodes handled as QNAMEs)
                // and tokens that can't be properties.
				default:
					error("Line "+line+": N3toRDF: Shouldn't see "+
								N3EventPrinter.formatSlot(prop)+
								" at this point!") ;
			}

            // Didn't find an existing one above so make it ...            
            if ( pNode == null )
                pNode = model.createProperty(propStr) ;
            else
                propStr = pNode.getURI() ;


            
			RDFNode sNode = createNode(line, subj);
            // Must be a resource
			if ( sNode instanceof Literal )
				error("Line "+line+": N3toRDF: Subject can't be a literal") ;

			RDFNode oNode = createNode(line, obj);
			
			Statement stmt = model.createStatement((Resource)sNode, pNode, oNode) ;
			if ( VERBOSE )
				System.out.println("Statement: "+stmt) ;
			model.add(stmt) ;
		}
		catch (RDFException rdfEx)
		{
			error("Line "+line+": RDFException: " + rdfEx);
		}
	}
	
	private Map bNodeMap = new HashMap() ;
	
	private RDFNode createNode(int line, AST thing) throws RDFException
	{
		//String tokenType = N3AntlrParser._tokenNames[thing.getType()] ;
		//System.out.println("Token type: "+tokenType) ;
		String text = thing.getText() ;
		switch (thing.getType())
		{
			case N3Parser.LITERAL :
				// Literals have three part: value (string), lang tag, datatype
                AST a1 = thing.getNextSibling() ;
                AST a2 = (a1==null?null:a1.getNextSibling()) ;
                AST datatype = null ;
                AST lang = null ; 
                
                if ( a2 != null )
                {
                    if ( a2.getType() == N3Parser.DATATYPE )
                        datatype = a2.getFirstChild() ;
                    else
                        lang = a2 ;
                }
                // First takes precidence over second.
                if ( a1 != null )
                {
                    if ( a1.getType() == N3Parser.DATATYPE )
                        datatype = a1.getFirstChild() ;
                    else
                        lang = a1 ;
                }

                // Chop leading '@'                
                String langTag = (lang!=null)?lang.getText().substring(1):null ;
                String typeURI = null ;
                if (datatype != null)
                {
                    typeURI = datatype.getText();
                    // Can't have bNodes here so the code is slightly different.
                    switch (datatype.getType())
                    {
                        case N3Parser.QNAME :
                            if (typeURI.startsWith("_:") || typeURI.startsWith("=:"))
                            {
                                error("Line "+ line+ ": N3toRDF: Use bNode for datatype URI: "
                                        + text+ "^^"+ typeURI);
                                return model.createLiteral("Illegal literal: " + text + "^^" + typeURI);
                            }
                            ExpandedQName qn = new ExpandedQName(line, typeURI);
                            typeURI = qn.expansion;
                            // Fall through
                        case N3Parser.URIREF :
                            typeURI = expandURIRef(typeURI);
                            break ;
                        default :
                            error("Line "+ line+ ": N3toRDF: Must use URIref or QName datatype URI: "
                                    + text+ "^^"+ typeURI+"("+N3Parser.getTokenNames()[datatype.getType()]+")");
                            return model.createLiteral("Illegal literal: " + text + "^^" + typeURI);
                    }
                }
                if ( langTag == null )
                    langTag = "" ;
                if ( typeURI == null )
                    return model.createLiteral(text, langTag) ; 
                
                return model.createTypedLiteral(text, langTag, typeURI) ;
                
			case N3Parser.QNAME :
				// Is it a labeled bNode?
				if ( text.startsWith("_:") && ! prefixMap.containsKey("_") )
				{
					if ( ! bNodeMap.containsKey(text) )
						bNodeMap.put(text, model.createResource()) ;
					return (Resource)bNodeMap.get(text) ;
				}
			
				ExpandedQName qn = new ExpandedQName(line, text);
				text = qn.expansion ;
                return model.createResource(expandURIRef(text)) ;
            
            // Lists
            case N3Parser.TK_LIST_NIL:
                return RDF.nil ;
            case N3Parser.TK_LIST:
                return RDF.List ;

			case N3Parser.URIREF :
                return model.createResource(expandURIRef(text)) ;
                
			case N3Parser.ANON:			// bNodes via [] or [:- ] QNAME starts "=:"
				if ( ! bNodeMap.containsKey(text) )
					bNodeMap.put(text, model.createResource()) ;
				return (Resource)bNodeMap.get(text) ;
                
            case N3Parser.UVAR:
                error("Line "+line+": N3toRDF: Can't map variables to RDF: "+text) ;
                break ;
                
			default:
				error("Line "+line+": N3toRDF: Can't map to a resource or literal: "+AntlrUtils.ast(thing)) ;
                break ;
		}
		return null ;
	}

    // Expand shorthand forms (not QNames) for URIrefs.
    private String expandURIRef(String text)
    {
        // Not a "named" bNode (start with _:)
        if ( text.equals("") )
            // The case of <>.
            return base ;
        if ( text.equals("#") )
            // The case of <#>.
            return base+"#" ;
        return text;
    }
    
	class ExpandedQName
	{
		public String qname;

		public String firstPart;
		public String secondPart;
		public String expansion;

		ExpandedQName (int line, String _qname)
		{
			qname = _qname;
			int split = qname.indexOf(':');

			String prefix = qname.substring(0, split);
			if (! prefixMap.containsKey(prefix) )
				error("Line "+line+": N3toRDF: Undefined qname: " + qname);

			secondPart = qname.substring(split + 1);
			firstPart = (String) prefixMap.get(prefix);

			// The :x form
			//if ( firstPart.equals("#") )
			//	firstPart = base+"#" ;
				
			expansion = firstPart + secondPart;
		}
	}
	
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
