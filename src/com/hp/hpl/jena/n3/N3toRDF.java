/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import antlr.collections.AST ;
import java.util.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.vocabulary.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author		Andy Seaborne
 * @version 	$Id: N3toRDF.java,v 1.28 2005-02-21 12:04:06 andy_seaborne Exp $
 */
public class N3toRDF implements N3ParserEventHandler
{
    protected static Log logger = LogFactory.getLog( N3toRDF.class );
	
	Model model ;

	// Maps URIref or a _:xxx bNode to a Resource
	Map resourceRef = new HashMap() ;
	// Maps URIref to Property
	Map propertyRef = new HashMap() ;
    
    // A more liberal prefix mapping map.
    Map myPrefixMapping = new HashMap() ;
    
    boolean allowPropertySymbols = true ;
    boolean allowKeywordA        = true ;
	
	// Well known namespaces
	
	static final String NS_rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#" ;
    static final String NS_rdfs = "http://www.w3.org/2000/01/rdf-schema#" ;
	
    static final String NS_W3_log = "http://www.w3.org/2000/10/swap/log#" ;
    static final String LOG_IMPLIES = NS_W3_log+"implies" ; 
    static final String LOG_MEANS =   NS_W3_log+"means" ; 

    static final String XMLLiteralURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral" ;

	private String base = null ;
    private String basedir = null ;
	final String anonPrefix = "_" ;
	
    N3toRDF() {}
    
    public void setBase(String str)
    {
        if ( str == null )
        {
            base = null ;
            basedir = null ;
            return ;
        }
        base = str ;
        if ( base.startsWith("file:"))
        {
            int i = base.lastIndexOf('/') ;
            if ( i >= 0 )
                // Include the /
                basedir = base.substring(0,i+1) ;
            else 
                basedir = base ;
        }
        else
            basedir = base ;
    }
    
    public void setModel(Model model)
    {
        this.model = model ; 
    }
    
    // Need to delay setting the model and base
//	N3toRDF(Model m, String _base)
//	{
//		model = m ; base = _base ;
//		if ( VERBOSE )
//			System.out.println("N3toRDF: "+base) ;
//	}
	
    
	public void startDocument() { }
	public void endDocument()   { }
	
	// When Jena exceptions are runtime, we will change this
	public void error(Exception ex, String message) 		{ throw new N3Exception(message) ; }
	public void error(String message) 						{ error(null, message) ; }
    
	public void warning(Exception ex, String message)       { logger.warn(message, ex) ; }
	public void warning(String message)						{ logger.warn(message) ; }
    
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
            uriref = expandURIRef(uriref, line) ;
            
            if ( uriref == null )
			    error("Line "+line+": N3toRDF: Relative URI can't be resolved in in @prefix directive") ;  
                
            setPrefixMapping(model, prefix, uriref) ;
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

            // Lists: The parser creates list elements as sequences of triples:
            //       anon  list:first  ....
            //       anon  list:rest   resource
            // Where "resource" is nil for the last element of the list (generated first).

            // The properties are in a unique namespace to distinguish them
            // from lists encoded explicitly, not with the () syntax.

			int pType = prop.getType();
			String propStr = prop.getText();
            Property pNode = null ;
			
			switch (pType)
			{
				case N3Parser.ARROW_R :
                    if ( ! allowPropertySymbols )
                        error("Line "+line+": N3toRDF: Propertry symbol '=>' not allowed") ;
					propStr = LOG_IMPLIES ;
					break;
				case N3Parser.ARROW_MEANS :
                    if ( ! allowPropertySymbols )
                        error("Line "+line+": N3toRDF: Propertry symbol '<=>' not allowed") ;
					propStr = LOG_MEANS ;
					break;
				case N3Parser.ARROW_L :
                    if ( ! allowPropertySymbols )
                        error("Line "+line+": N3toRDF: Propertry symbol '<=' not allowed") ;
					// Need to reverse subject and object
					propStr = LOG_IMPLIES ;
					AST tmp = obj; obj = subj; subj = tmp;
					break;
				case N3Parser.EQUAL :
					//propStr = NS_DAML + "equivalentTo";
					//propStr = damlVocab.equivalentTo().getURI() ;
                    if ( ! allowPropertySymbols )
                        error("Line "+line+": N3toRDF: Propertry symbol '=' not allowed") ;
                    pNode = OWL.sameAs ;
					break;
				case N3Parser.KW_A :
                    if ( ! allowKeywordA )
                        error("Line "+line+": N3toRDF: Propertry symbol 'a' not allowed") ;
                    pNode = RDF.type ;
					break ;
				case N3Parser.QNAME:
                    
                    if ( prop.getText().startsWith("_:") )
                        error("Line "+line+": N3toRDF: Can't have properties with labelled bNodes in RDF") ;
                    
                    String uriref = expandPrefix(model, propStr) ;
                    if ( uriref == propStr )
                    {
                        // Failed to expand ...
                        error("Line "+line+": N3toRDF: Undefined qname namespace: " + propStr);
                        return ;
                    }
                    pNode = model.createProperty(uriref) ;
                    break ;
				case N3Parser.URIREF:
                    propStr = expandURIRef(propStr, line) ;
                    break ;
                case N3Parser.TK_LIST_FIRST:
                    pNode = RDF.first ;
                break ;
                case N3Parser.TK_LIST_REST:
                    pNode = RDF.rest ;
                    break ;
                // Literals, parser generated bNodes (other bnodes handled as QNAMEs)
                // and tokens that can't be properties.
                case N3Parser.ANON:
                    error("Line "+line+": N3toRDF: Can't have anon. properties in RDF") ;
                    break ;
				default:
					error("Line "+line+": N3toRDF: Shouldn't see "+
								N3EventPrinter.formatSlot(prop)+
								" at this point!") ;
                    break ;
			}

            // Didn't find an existing one above so make it ...
            if ( pNode == null )
                pNode = model.createProperty(propStr) ;
            else
                propStr = pNode.getURI() ;

			RDFNode sNode = createNode(line, subj);
            // Must be a resource
			if ( sNode instanceof Literal )
				error("Line "+line+": N3toRDF: Subject can't be a literal: " +subj.getText()) ;

			RDFNode oNode = createNode(line, obj);
			
			Statement stmt = model.createStatement((Resource)sNode, pNode, oNode) ;
			model.add(stmt) ;
		}
		catch (JenaException rdfEx)
		{
			error("Line "+line+": JenaException: " + rdfEx);
		}
	}
	
	private Map bNodeMap = new HashMap() ;
    
	private RDFNode createNode(int line, AST thing) 
	{
		//String tokenType = N3AntlrParser._tokenNames[thing.getType()] ;
		//System.out.println("Token type: "+tokenType) ;
		String text = thing.getText() ;
		switch (thing.getType())
		{
            case N3Parser.NUMBER :
                Resource xsdType = XSD.integer ;
                if ( text.indexOf('.') >= 0 )
                    // The choice of XSD:double is for compatibility with N3/cwm.
                    xsdType = XSD.xdouble ;
                if ( text.indexOf('e') >= 0 || text.indexOf('E') >= 0 )
                    xsdType = XSD.xdouble ;
                return model.createTypedLiteral(text, xsdType.getURI());
                
			case N3Parser.LITERAL :
				// Literals have three part: value (string), lang tag, datatype
                // Can't have a lang tag and a data type - if both, just use the datatype
                
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
                // First takes precedence over second.
                if ( a1 != null )
                {
                    if ( a1.getType() == N3Parser.DATATYPE )
                        datatype = a1.getFirstChild() ;
                    else
                        lang = a1 ;
                }

                // Chop leading '@'
                String langTag = (lang!=null)?lang.getText().substring(1):null ;
                
                if ( datatype == null )
                    return model.createLiteral(text, langTag) ;
                
                // If there is a datatype, it takes predence over lang tag.
                String typeURI = datatype.getText();

                if ( datatype.getType() != N3Parser.QNAME &&
                     datatype.getType() != N3Parser.URIREF )
                {
                    error("Line "+ line+ ": N3toRDF: Must use URIref or QName datatype URI: "
                            + text+ "^^"+ typeURI+"("+N3Parser.getTokenNames()[datatype.getType()]+")");
                    return model.createLiteral("Illegal literal: " + text + "^^" + typeURI);
 
                }
                
                // Can't have bNodes here so the code is slightly different for expansion
                
                if ( datatype.getType() == N3Parser.QNAME )
                {
                    if (typeURI.startsWith("_:") || typeURI.startsWith("=:"))
                    {
                        error("Line "+ line+ ": N3toRDF: Can't use bNode for datatype URI: "
                                + text+ "^^"+ typeURI);
                        return model.createLiteral("Illegal literal: " + text + "^^" + typeURI);
                    }

                    String typeURI2 = expandPrefix(model, typeURI) ;
                    if ( typeURI2 == typeURI )
                    {
                        error("Line "+line+": N3toRDF: Undefined qname namespace in datatype: " + typeURI);
                    }
                    
                    typeURI = typeURI2 ;
                }

                typeURI = expandURIRef(typeURI, line);
                // 2003-08 - Ignore lang tag when there is an type. 
                return model.createTypedLiteral(text, typeURI) ;

			case N3Parser.QNAME :
				// Is it a labelled bNode?
                // Check if _ has been defined.
				if ( text.startsWith("_:") && ( model.getNsPrefixURI("_") == null ) )
				{
					if ( ! bNodeMap.containsKey(text) )
						bNodeMap.put(text, model.createResource()) ;
					return (Resource)bNodeMap.get(text) ;
				}
			
                String uriref = expandPrefix(model, text) ;
                if ( uriref == text )
                {
                    error("Line "+line+": N3toRDF: Undefined qname namespace: " + text);
                    return null ;
                }
                return model.createResource(expandURIRef(uriref, line)) ;

            // Normal URIref - may be <> or <#>
            case N3Parser.URIREF :
                return model.createResource(expandURIRef(text, line)) ;

            // Lists
            case N3Parser.TK_LIST_NIL:
                return RDF.nil ;
            case N3Parser.TK_LIST:
                return RDF.List ;

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
    private String expandURIRef(String text, int line)
    {
        // Not a "named" bNode (start with _:)
        if ( text.equals("") && base == null )
            error("Line "+line+": N3toRDF: Relative URI but no base for <>") ;
        
        if ( text.equals("#") && base == null )
            error("Line "+line+": N3toRDF: Relative URI but no base for <#>") ;
        
        if ( text.equals("") )
            // The case of <>.
            return base ;
        
        if ( text.equals("#") )
            // The case of <#>.
            return base+"#" ;
        
        if ( base != null && ! hasURIscheme(text) )
        {
            if ( ! base.startsWith("file:"))
            {
                if ( text.startsWith("#"))
                    return base+text ;
                else
                    return base+"#"+text ;
            }
            
            // File-like things.
            if ( text.startsWith("#") )
                return base + text ;
            if ( text.startsWith("/") )
                return "file:" + text ;
            else
                return basedir + text ;
        }
        
        return text;
    }
    
    private boolean hasURIscheme(String text)
    {
        for ( int i = 0 ; i < text.length() ; i++ )
        {
            char ch = text.charAt(i) ;
            if ( ch == ':' )
                return true ;
            if ( ( ch >= 'a' && ch <= 'z' ) ||
                 ( ch >= 'A' && ch <= 'Z' ) )
                continue ;
            return false ;
        }
        return false ;
    }
    
    private void setPrefixMapping(Model m, String prefix, String uriref)
    {
        try { model.setNsPrefix(prefix, uriref); }
        catch (PrefixMapping.IllegalPrefixException ex)
        {
            warning("Prefix mapping '" + prefix + "' illegal: used but not recorded in model");
        }
        myPrefixMapping.put(prefix, uriref);
    }
    
    private String expandPrefix(Model m, String prefixed)
    {
        // Code from shared.impl.PrefixMappingImpl ...
        // Needed a copy as we store unchecked prefixes for N3.
        int colon = prefixed.indexOf( ':' );
        if (colon < 0) 
            return prefixed;
        else
        {
            String prefix = prefixed.substring( 0, colon );
            String uri = (String) myPrefixMapping.get( prefix );
            if ( uri == null )
                return prefixed ;
            return uri + prefixed.substring( colon + 1 ) ;
        }
        // Not this - model may already have prefixes defined;
        // we allow "illegal" prefixes (e.g. starts with a number)
        // for compatibility 
        //return model.expandPrefix(prefix) ;
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
