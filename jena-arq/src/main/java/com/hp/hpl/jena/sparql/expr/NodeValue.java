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

package com.hp.hpl.jena.sparql.expr;

import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSD ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDboolean ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdate ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdateTime ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdecimal ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdouble ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDduration ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDfloat ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgDay ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgMonth ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgMonthDay ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgYear ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgYearMonth ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDinteger ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDstring ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDtime ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_BOOLEAN ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_DATE ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_DATETIME ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_DIFFERENT ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_DURATION ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_G_DAY ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_G_MONTH ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_G_MONTHDAY ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_G_YEAR ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_G_YEARMONTH ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_LANG ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_NODE ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_NUM ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_STRING ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_TIME ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_UNKNOWN ;
import static javax.xml.datatype.DatatypeConstants.DAYS ;
import static javax.xml.datatype.DatatypeConstants.HOURS ;
import static javax.xml.datatype.DatatypeConstants.MINUTES ;
import static javax.xml.datatype.DatatypeConstants.MONTHS ;
import static javax.xml.datatype.DatatypeConstants.SECONDS ;
import static javax.xml.datatype.DatatypeConstants.YEARS ;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.InputStream ;
import java.math.BigDecimal ;
import java.math.BigInteger ;
import java.util.Calendar ;
import java.util.Iterator ;
import java.util.Properties ;
import java.util.ServiceLoader ;

import javax.xml.datatype.DatatypeConfigurationException ;
import javax.xml.datatype.DatatypeFactory ;
import javax.xml.datatype.Duration ;
import javax.xml.datatype.XMLGregorianCalendar ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.datatypes.DatatypeFormatException ;
import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.nodevalue.* ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.* ;

public abstract class NodeValue extends ExprNode
{
    // Maybe:: NodeValueStringLang - strings with language tag
    
    /* Naming:
     * getXXX => plain accessor
     * asXXX =>  force to the required thing if necessary. 
     * 
     * Implementation notes:
     * 
     * 1. There is little point delaying turning a node into its value
     *    because it has to be verified anyway (e.g. illegal literals).
     *    Because a NodeValue is being created, it is reasonably likely it
     *    is going to be used for it's value, so processing the datatype
     *    can be done at creation time where it is clearer.
     *    
     * 2. Conversely, delaying turning a value into a graph node is
     *    valuable because intermediates, like the result of 2+3, will not
     *    be needed as nodes unless assignment (and there is no assignment
     *    in SPARQL even if there is for ARQ). 
     *    Node level operations like str() don't need a full node.
     *      
     * 3. nodevalue.NodeFunctions contains the SPARQL builtin implementations. 
     *    nodevalue.XSDFuncOp contains the implementation of the XQuery/Xpath
     *    functions and operations.
     *    See also NodeUtils.
     *    
     * 4. Note that SPARQL "=" is "known to be sameValueAs". Similarly "!=" is
     *    known to be different.
     *    
     * 5. To add a new number type:
     *    Add sub type into nodevalue.NodeValueXXX
     *      Must implement .hashCode() and .equals() based on value. 
     *    Add Functions.add/subtract/etc code and compareNumeric
     *    Add to compare code
     *    Fix TestExprNumeric
     *    Write lots of tests.
     *    Library code Maths1 and Maths2 for maths functions
     */
    
    /*
     * Effective boolean value rules.
     *    boolean: value of the boolean 
     *    string: length(string) > 0 is true
     *    numeric: number != Nan && number != 0 is true
     * ref:  http://www.w3.org/TR/xquery/#dt-ebv
     * 
     */
    
    private static Logger log = LoggerFactory.getLogger(NodeValue.class) ;
    
    // ---- Constants and initializers / public
    
    public static boolean VerboseWarnings = true ;
    public static boolean VerboseExceptions = false ;
    
    public static final BigInteger IntegerZERO = BigInteger.ZERO ;
    public static final BigDecimal DecimalZERO = BigDecimal.ZERO ;

    public static final NodeValue TRUE   = NodeValue.makeNode("true", XSDboolean) ;
    public static final NodeValue FALSE  = NodeValue.makeNode("false", XSDboolean) ;
    
    public static final NodeValue nvZERO = NodeValue.makeNode(NodeConst.nodeZero) ;
    public static final NodeValue nvONE  = NodeValue.makeNode(NodeConst.nodeOne) ;
    
    public static final NodeValue nvNaN     = NodeValue.makeNode("NaN", XSDdouble) ;
    public static final NodeValue nvINF     = NodeValue.makeNode("INF", XSDdouble) ;
    public static final NodeValue nvNegINF  = NodeValue.makeNode("-INF",XSDdouble) ;
    
    public static final NodeValue nvEmptyString  = NodeValue.makeString("") ;
    
    // Use "==" for equality.
    private static final String strForUnNode = "node value nothing" ;
    public static final NodeValue nvNothing = NodeValue.makeNode(com.hp.hpl.jena.graph.NodeFactory.createAnon(new AnonId("node value nothing"))) ;
    
    public static final String xsdNamespace = XSD+"#" ; 
    
    public static DatatypeFactory xmlDatatypeFactory = null ;
    static
    {
        try { xmlDatatypeFactory = getDatatypeFactory() ; }
        catch (DatatypeConfigurationException ex)
        { throw new ARQInternalErrorException("Can't create a javax.xml DatatypeFactory", ex) ; }
    }
    
    /**
     * Get a datatype factory using the correct classloader
     * 
     * See JENA-328. DatatypeFactory.newInstance() clashes with OSGi
     * This is clearly crazy, but DatatypeFactory is missing a very obvious
     * method newInstance(Classloader). The method that was added is very
     * hard to use correctly, as we shall see...
     */
    private static DatatypeFactory getDatatypeFactory() 
            throws DatatypeConfigurationException {
        ClassLoader cl = NodeValue.class.getClassLoader();
        File jaxpPropFile = new File(
            System.getProperty("java.home") + File.pathSeparator + 
            "lib" + File.pathSeparator + 
            "jaxp.properties");
        
        // Step 1. Try the system property
        String dtfClass = System.getProperty(DatatypeFactory.DATATYPEFACTORY_PROPERTY);
        
        try {
            // Step 2. Otherwise, try property in jaxp.properties
            if (dtfClass == null && jaxpPropFile.exists() && jaxpPropFile.canRead()) {
                Properties jaxp = new Properties();
                try(InputStream in = new FileInputStream(jaxpPropFile)) {
                    jaxp.load(in);
                    dtfClass = jaxp.getProperty(DatatypeFactory.DATATYPEFACTORY_PROPERTY);
                } catch (Exception e) {
                    log.warn("Issue loading jaxp.properties", e);
                }
            }
        }
        // File.exists and File.canRead may throw  SecurityException (probably AccessControlException)
        catch (SecurityException ex) {
            log.warn("Security exception try to get jaxp.properties: "+ex.getMessage()) ;
        }
        
        // Step 3. Otherwise try the service approach
        if (dtfClass == null) {
            Iterator<DatatypeFactory> factoryIterator = 
                ServiceLoader.load(DatatypeFactory.class, cl).iterator();
            if (factoryIterator.hasNext()) return factoryIterator.next();
        }
        
        // Step 4. Use the default
        if (dtfClass == null) dtfClass = DatatypeFactory.DATATYPEFACTORY_IMPLEMENTATION_CLASS;
        
        return DatatypeFactory.newInstance(dtfClass, NodeValue.class.getClassLoader()) ;
    }
    
    private Node node = null ;     // Null used when a value has not be turned into a Node.
    
    // Don't create direct - the static builders manage the value/node relationship 
    protected NodeValue() { super() ; }
    protected NodeValue(Node n) { super() ; node = n ; } 
    
//    protected makeNodeValue(NodeValue nv)
//    {
//        if ( v.isNode() )    { ... }
//        if ( v.isBoolean() ) { ... }
//        if ( v.isInteger() ) { ... }
//        if ( v.isDouble() )  { ... }
//        if ( v.isDecimal() ) { ... }
//        if ( v.isString() )  { ... }
//        if ( v.isDate() )    { ... } 
//    }

    // ----------------------------------------------------------------
    // ---- Construct NodeValue without a graph node.
    
    /** Convenience operation - parse a string to produce a NodeValue - common namespaces like xsd: are built-in */
    public static NodeValue parse(String string)
    { return makeNode(NodeFactoryExtra.parseNode(string)) ; }
    
    public static NodeValue makeInteger(long i)
    { return new NodeValueInteger(BigInteger.valueOf(i)) ; }
    
    public static NodeValue makeInteger(BigInteger i)
    { return new NodeValueInteger(i) ; }

    public static NodeValue makeInteger(String lexicalForm)
    { return new NodeValueInteger(new BigInteger(lexicalForm)) ; }

    public static NodeValue makeFloat(float f)
    { return new NodeValueFloat(f) ; }
    
    public static NodeValue makeDouble(double d)
    { return new NodeValueDouble(d) ; }

    public static NodeValue makeString(String s) 
    { return new NodeValueString(s) ; }

    public static NodeValue makeDecimal(BigDecimal d)
    { return new NodeValueDecimal(d) ; }
  
    public static NodeValue makeDecimal(long i)
    { return new NodeValueDecimal(BigDecimal.valueOf(i)) ; }
  
    public static NodeValue makeDecimal(double d)
    { return new NodeValueDecimal(BigDecimal.valueOf(d)) ; } 
  
    public static NodeValue makeDecimal(String lexicalForm)
    { return NodeValue.makeNode(lexicalForm, XSDdecimal) ; }

    public static NodeValue makeDateTime(String lexicalForm)
    { return NodeValue.makeNode(lexicalForm, XSDdateTime) ; }

    public static NodeValue makeDate(String lexicalForm)
    { return NodeValue.makeNode(lexicalForm, XSDdate) ; }

    public static NodeValue makeDateTime(Calendar cal)
    {
        String lex = Utils.calendarToXSDDateTimeString(cal) ;
        return NodeValue.makeNode(lex, XSDdateTime) ;
    }

    public static NodeValue makeDateTime(XMLGregorianCalendar cal)
    {
        String lex = cal.toXMLFormat() ;
        Node node = com.hp.hpl.jena.graph.NodeFactory.createLiteral(lex, XSDdateTime) ; 
        return new NodeValueDT(lex, node) ;
    }

    public static NodeValue makeDate(Calendar cal)
    { 
        String lex = Utils.calendarToXSDDateString(cal) ;
        return NodeValue.makeNode(lex, XSDdate) ;
    }
    
    public static NodeValue makeDate(XMLGregorianCalendar cal)
    {
        String lex = cal.toXMLFormat() ;
        Node node = com.hp.hpl.jena.graph.NodeFactory.createLiteral(lex, XSDdate) ; 
        return new NodeValueDT(lex, node) ;
    }

    public static NodeValue makeDuration(String lexicalForm)
    { return NodeValue.makeNode(lexicalForm, XSDduration) ; }

    public static NodeValue makeDuration(Duration duration)
    { return new NodeValueDuration(duration); }

    public static NodeValue makeNodeDuration(Duration duration, Node node)
    { return new NodeValueDuration(duration, node); }

    public static NodeValue makeBoolean(boolean b)
    { return b ? NodeValue.TRUE : NodeValue.FALSE ; }
    
    public static NodeValue booleanReturn(boolean b)
    { return b ? NodeValue.TRUE : NodeValue.FALSE ; }

    // ----------------------------------------------------------------
    // ---- Construct NodeValue from graph nodes 

    public static NodeValue makeNode(Node n)
    {
        NodeValue nv = nodeToNodeValue(n) ;
        return nv ;
    }
                               
    public static NodeValue makeNode(String lexicalForm, XSDDatatype dtype)
    {
        Node n = com.hp.hpl.jena.graph.NodeFactory.createLiteral(lexicalForm, null, dtype) ;
        NodeValue nv = NodeValue.makeNode(n) ;
        return nv ;
    }

    // Convenience - knows that lang tags aren't allowed with datatypes.
    public static NodeValue makeNode(String lexicalForm, String langTag, Node datatype)
    {
        String uri = (datatype==null) ? null : datatype.getURI() ;
        return makeNode(lexicalForm, langTag,  uri) ;
    }

    public static NodeValue makeNode(String lexicalForm, String langTag, String datatype)
    {
        if ( datatype != null && datatype.equals("") )
            datatype = null ;
        
        if ( langTag != null && datatype != null )
            // raise??
            Log.warn(NodeValue.class, "Both lang tag and datatype defined (lexcial form '"+lexicalForm+"')") ;
        
        Node n = null ; 
        
        if ( datatype != null)
        {
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatype) ;
            n = com.hp.hpl.jena.graph.NodeFactory.createLiteral(lexicalForm, null, dType) ;
        }
        else
            n = com.hp.hpl.jena.graph.NodeFactory.createLiteral(lexicalForm, langTag, null) ;
        return NodeValue.makeNode(n) ;
    }
    
    // ----------------------------------------------------------------
    // ---- Construct NodeValue with graph node and value. 

    public static NodeValue makeNodeBoolean(boolean b)
    { return b ? NodeValue.TRUE : NodeValue.FALSE ; }

    public static NodeValue makeNodeBoolean(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, null, XSDboolean.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeInteger(long v)
    {
        NodeValue nv = makeNode(Long.toString(v), null, XSDinteger.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeInteger(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, null, XSDinteger.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeFloat(float f)
    {
        NodeValue nv = makeNode(Utils.stringForm(f), null, XSDfloat.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeFloat(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, null, XSDdouble.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDouble(double v)
    {
        NodeValue nv = makeNode(Utils.stringForm(v), null, XSDdouble.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDouble(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, null, XSDdouble.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDecimal(BigDecimal decimal)
    {
        NodeValue nv = makeNode(Utils.stringForm(decimal), null, XSDdecimal.getURI()) ;
        return nv ;
    }

    public static NodeValue makeNodeDecimal(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, null, XSDdecimal.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeString(String string)
    {
        NodeValue nv = makeNode(string, null, (String)null) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDateTime(Calendar date)
    {
        String lex = Utils.calendarToXSDDateTimeString(date) ;
        NodeValue nv = makeNode(lex, XSDdateTime) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDateTime(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, XSDdateTime) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDate(Calendar date)
    {
        String lex = Utils.calendarToXSDDateString(date) ;
        NodeValue nv = makeNode(lex, XSDdate) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDate(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, XSDdate) ;
        return nv ;
    }
    
   // ----------------------------------------------------------------
   // ---- Expr interface
    
    @Override
    public NodeValue eval(Binding binding, FunctionEnv env)
    { return this ; }

    // NodeValues are immutable so no need to duplicate.
    @Override
    public Expr copySubstitute(Binding binding)
    {
        return this ;
    }
    
    @Override
    public Expr applyNodeTransform(NodeTransform transform)
    { 
        Node n = asNode() ;
        n = transform.convert(n) ;
        return makeNode(n) ;
    }

    public Node evalNode(Binding binding, ExecutionContext execCxt)
    {
        return asNode() ;
    }

    @Override
    public boolean isConstant() { return true ; }

    @Override
    public NodeValue getConstant()     { return this ; }

    public boolean isIRI()
    {
        if ( node == null ) return false ;
        forceToNode() ;
        return node.isURI() ;
    }

    public boolean isBlank()
    {
        if ( node == null ) return false ;
        forceToNode() ;
        return node.isBlank() ;
    }
    
    // ----------------------------------------------------------------
    // ---- sameValueAs
    
    // Disjoint value spaces : dateTime and dates are not comparable 
    // Every langtag implies another value space as well.
    
    /** Return true if the two NodeValues are known to be the same value
     *  return false if known to be different values,
     *  throw ExprEvalException otherwise
     */
    public static boolean sameAs(NodeValue nv1, NodeValue nv2)
    {
        if ( nv1 == null || nv2 == null )
            throw new ARQInternalErrorException("Attempt to sameValueAs on a null") ;
        
        ValueSpaceClassification compType = classifyValueOp(nv1, nv2) ;
        
        // Special case - date/dateTime comparison is affected by timezones and may be
        // interdeterminate based on the value of the dateTime/date.

        switch (compType)
        {
            case VSPACE_NUM:        
                return XSDFuncOp.compareNumeric(nv1, nv2) == Expr.CMP_EQUAL ;
            case VSPACE_DATETIME:   
            case VSPACE_DATE:
            case VSPACE_TIME:
            case VSPACE_G_YEAR :
            case VSPACE_G_YEARMONTH :
            case VSPACE_G_MONTH :
            case VSPACE_G_MONTHDAY :
            case VSPACE_G_DAY :
            {
                int x = XSDFuncOp.compareDateTime(nv1, nv2) ; 
                if ( x == Expr.CMP_INDETERMINATE )
                    throw new ExprNotComparableException("Indeterminate dateTime comparison") ;
                return  x == Expr.CMP_EQUAL ;
            }
            case VSPACE_DURATION:
            {
                int x = XSDFuncOp.compareDuration(nv1, nv2) ;
                if ( x == Expr.CMP_INDETERMINATE )
                	throw new ExprNotComparableException("Indeterminate duration comparison") ;
                return  x == Expr.CMP_EQUAL ;
            }

            case VSPACE_STRING:     return XSDFuncOp.compareString(nv1, nv2) == Expr.CMP_EQUAL ;
            case VSPACE_BOOLEAN:    return XSDFuncOp.compareBoolean(nv1, nv2) == Expr.CMP_EQUAL ;
            
            case VSPACE_LANG:
            {
                // two literals, both with a language tag
                Node node1 = nv1.asNode() ;
                Node node2 = nv2.asNode() ;
                return node1.getLiteralLexicalForm().equals(node2.getLiteralLexicalForm()) &&
                       node1.getLiteralLanguage().equalsIgnoreCase(node2.getLiteralLanguage()) ;
            }

            case VSPACE_NODE:
                // Two non-literals
                return NodeFunctions.sameTerm(nv1.getNode(), nv2.getNode()) ;

            case VSPACE_UNKNOWN:
            {
                // One or two unknown value spaces, or one has a lang tag (but not both).
                Node node1 = nv1.getNode() ;
                Node node2 = nv2.getNode() ;
                
                if ( ! SystemARQ.ValueExtensions )
                    // No value extensions => raw rdfTermEquals
                    return NodeFunctions.rdfTermEquals(node1, node2) ;

                // Some "value spaces" are know to be not equal (no overlap).
                // Like one literal with a language tag, and one without can't be sameAs.
                
                if ( ! node1.isLiteral() || ! node2.isLiteral() )
                    // One or other not a literal => not sameAs
                    return false ;

                // Two literals at this point.
                
                if ( NodeFunctions.sameTerm(node1, node2) )
                    return true ;

                if ( ! node1.getLiteralLanguage().equals("") ||
                     ! node2.getLiteralLanguage().equals("") )
                    // One had lang tags but weren't sameNode => not equals
                    return false ;
                
                raise(new ExprEvalException("Unknown equality test: "+nv1+" and "+nv2)) ;
                throw new ARQInternalErrorException("raise returned (sameValueAs)") ;
            }
            
            case VSPACE_DIFFERENT:
                // Known to be incompatible.
                if ( ! SystemARQ.ValueExtensions && ( nv1.isLiteral() && nv2.isLiteral() ) )
                    raise(new ExprEvalException("Incompatible: "+nv1+" and "+nv2)) ;
                return false ;
        }
        
        throw new ARQInternalErrorException("sameValueAs failure "+nv1+" and "+nv2) ;
    }
    
    /** Return true if the two Nodes are known to be different,
     *  return false if the two Nodes are known to be the same,
     *  else throw ExprEvalException
     */
    public static boolean notSameAs(Node n1, Node n2)
    {
        return notSameAs(NodeValue.makeNode(n1), NodeValue.makeNode(n2)) ; 
    }

    /** Return true if the two NodeValues are known to be different,
     *  return false if the two NodeValues are known to be the same,
     *  else throw ExprEvalException
     */
    public static boolean notSameAs(NodeValue nv1, NodeValue nv2)
    {
        return ! sameAs(nv1, nv2) ;
    }

    // ----------------------------------------------------------------
    // compare
    
    // Compare by value code is here
    // NodeUtils.compareRDFTerms for syntactic comparison
    
    /** Compare by value if possible else compare by kind/type/lexical form 
     *  Only use when you want an ordering regardless of form of NodeValue,
     *  for example in ORDER BY
     *  
     * @param nv1
     * @param nv2
     * @return negative, 0, or postive for less than, equal, greater than.  
     */

    public static int compareAlways(NodeValue nv1, NodeValue nv2)
    {
        // ***** Only called from a test. Sort out with NodeUtils.
        try {
            int x = compare(nv1, nv2, true) ;
            // Same?
            if ( x != Expr.CMP_EQUAL )
                return x ;
        } catch (ExprNotComparableException ex)
        { /* Drop through */ }
        return NodeUtils.compareRDFTerms(nv1.asNode(), nv2.asNode()) ;
    }
    
    /** Compare by value (and only value) if possible.
     *  Supports <, <=, >, >= but not = nor != (which are sameValueAs and notSameValueAs)
     * @param nv1
     * @param nv2
     * @return negative, 0 , or positive for not possible, less than, equal, greater than.
     * @throws ExprNotComparableException  
     */
    public static int compare(NodeValue nv1, NodeValue nv2)
    {
        // Called from E_LessThat etc
        // and NodeUtils.comparLiteralsByValue 
        if ( nv1 == null || nv2 == null )
            //raise(new ExprEvalException("Attempt to notSameValueAs on null") ;
            throw new ARQInternalErrorException("Attempt to compare on null") ;
        int x = compare(nv1, nv2, false) ;
        return x ;
    }
    
    // E_GreaterThan/E_LessThan/E_GreaterThanOrEqual/E_LessThanOrEqual
    // ==> compare(nv1, nv2) => compare (nv1, nv2, false)
    
    // BindingComparator => compareAlways(nv1, nv2) => compare (nv1, nv2, true)
    
    // E_Equals calls NodeValue.sameAs() ==> 
    
    // sortOrderingCompare means that the comparison should do something with normally unlike things,
    // and split plain strings from xsd:strings.
    
    private static int compare(NodeValue nv1, NodeValue nv2, boolean sortOrderingCompare)
    {
        if ( nv1 == null && nv2 == null )
            return Expr.CMP_EQUAL ;
        
        if ( nv1 == null )
            return Expr.CMP_LESS ;
        if ( nv2 == null )
            return Expr.CMP_GREATER ;
        
        ValueSpaceClassification compType = classifyValueOp(nv1, nv2) ;
        
        // Special case - date/dateTime comparison is affected by timezones and may be
        // interdeterminate based on the value of the dateTime/date.
        // Do this first, 
        
        switch (compType)
        {
            case VSPACE_DATETIME:
            case VSPACE_DATE:
            case VSPACE_TIME:
            case VSPACE_G_DAY :
            case VSPACE_G_MONTH :
            case VSPACE_G_MONTHDAY :
            case VSPACE_G_YEAR :
            case VSPACE_G_YEARMONTH :
            {
                int x = XSDFuncOp.compareDateTime(nv1, nv2) ;
                if ( x != Expr.CMP_INDETERMINATE )
                    return x ;
                // Indeterminate => can't compare as strict values.
                compType = ValueSpaceClassification.VSPACE_DIFFERENT ;
                break ;
            }
            case VSPACE_DURATION:
            {
                int x = XSDFuncOp.compareDuration(nv1, nv2) ;
                if ( x != Expr.CMP_INDETERMINATE )
                    return x ;
                // Indeterminate => can't compare as strict values.
                compType = ValueSpaceClassification.VSPACE_DIFFERENT ;
                break ;
            }

            //default:
            case VSPACE_BOOLEAN :
            case VSPACE_DIFFERENT :
            case VSPACE_LANG :
            case VSPACE_NODE :
            case VSPACE_NUM :
            case VSPACE_STRING :
            case VSPACE_UNKNOWN :
                // Drop through.
        }
            
        switch (compType)
        {    
            case VSPACE_DATETIME:
            case VSPACE_DATE:
            case VSPACE_TIME:
            case VSPACE_G_DAY :
            case VSPACE_G_MONTH :
            case VSPACE_G_MONTHDAY :
            case VSPACE_G_YEAR :
            case VSPACE_G_YEARMONTH :
            case VSPACE_DURATION:
                throw new ARQInternalErrorException("Still seeing date/dateTime/time/duration compare type") ;
            
            case VSPACE_NUM:        return XSDFuncOp.compareNumeric(nv1, nv2) ;
            case VSPACE_STRING:
            {
                int cmp = XSDFuncOp.compareString(nv1, nv2) ;
                
                // Split plain literals and xsd:strings for sorting purposes.
                if ( ! sortOrderingCompare )
                    return cmp ;
                if ( cmp != Expr.CMP_EQUAL )
                    return cmp ;
                // Same by string value.
                String dt1 = nv1.asNode().getLiteralDatatypeURI() ;
                String dt2 = nv2.asNode().getLiteralDatatypeURI() ;
                if ( dt1 == null && dt2 != null )
                    return Expr.CMP_LESS ;
                if ( dt2 == null && dt1 != null )
                    return Expr.CMP_GREATER ;
                return Expr.CMP_EQUAL;  // Both plain or both xsd:string.
            }
            case VSPACE_BOOLEAN:    return XSDFuncOp.compareBoolean(nv1, nv2) ;
            
            case VSPACE_LANG:
            {
                // Two literals, both with language tags.
                Node node1 = nv1.asNode() ;
                Node node2 = nv2.asNode() ;
                int x = StrUtils.strCompareIgnoreCase(node1.getLiteralLanguage(), node2.getLiteralLanguage()) ;
                
                if ( x != Expr.CMP_EQUAL )
                {
                    // Different lang tags
                    if ( ! sortOrderingCompare )
                        raise(new ExprNotComparableException("Can't compare (different languages) "+nv1+" and "+nv2)) ;
                    // Different lang tags - sorting
                    return x ;
                }

                // same lang tag (case insensitive)
                x = StrUtils.strCompare(node1.getLiteralLexicalForm(), node2.getLiteralLexicalForm()) ;
                if ( x != Expr.CMP_EQUAL )
                    return x ;
                // Same lexcial forms, same lang tag by value
                // Try to split by syntactic lang tags.
                x = StrUtils.strCompare(node1.getLiteralLanguage(), node2.getLiteralLanguage()) ;
                // Maybe they are the same after all!
                // Should be node.equals by now.
                if ( x == Expr.CMP_EQUAL  && ! NodeFunctions.sameTerm(node1, node2) )
                    throw new ARQInternalErrorException("Look the same (lang tags) but no node equals") ;
                return x ;
            }
            
            case VSPACE_NODE:
                // Two non-literals don't compare except for sorting.
                if ( sortOrderingCompare )
                    return NodeUtils.compareRDFTerms(nv1.asNode(), nv2.asNode()) ;
                else
                {
                    raise(new ExprNotComparableException("Can't compare (nodes) "+nv1+" and "+nv2)) ;
                    throw new ARQInternalErrorException("NodeValue.raise returned") ;
                }

            case VSPACE_UNKNOWN:
            {
                // One or two unknown value spaces.
                Node node1 = nv1.asNode() ;
                Node node2 = nv2.asNode() ;
                // Two unknown literals can be equal.
                if ( NodeFunctions.sameTerm(node1, node2) )
                    return Expr.CMP_EQUAL ;
                
                if ( sortOrderingCompare )
                    return NodeUtils.compareRDFTerms(node1, node2) ;
                
                raise(new ExprNotComparableException("Can't compare "+nv1+" and "+nv2)) ;
                throw new ARQInternalErrorException("NodeValue.raise returned") ;
            }
            
            case VSPACE_DIFFERENT:
                // Two literals, from different known value spaces
                if ( sortOrderingCompare )
                    return NodeUtils.compareRDFTerms(nv1.asNode(), nv2.asNode()) ;
                
                raise(new ExprNotComparableException("Can't compare (incompatible value spaces)"+nv1+" and "+nv2)) ;
                throw new ARQInternalErrorException("NodeValue.raise returned") ;
        }
        throw new ARQInternalErrorException("Compare failure "+nv1+" and "+nv2) ;
    }

    public static ValueSpaceClassification classifyValueOp(NodeValue nv1, NodeValue nv2)
    {
        ValueSpaceClassification c1 = nv1.getValueSpace() ;
        ValueSpaceClassification c2 = nv2.getValueSpace() ;
        if ( c1 == c2 ) return c1 ;
        if ( c1 == VSPACE_UNKNOWN || c2 == VSPACE_UNKNOWN )
            return VSPACE_UNKNOWN ;
        
        // Known values spaces but incompatible  
        return VSPACE_DIFFERENT ;
    }
    
    public ValueSpaceClassification getValueSpace()     { return classifyValueSpace(this) ; }
    
    private static ValueSpaceClassification classifyValueSpace(NodeValue nv)
    {
        if ( nv.isNumber() )        return VSPACE_NUM ;
        if ( nv.isDateTime() )      return VSPACE_DATETIME ;
        if ( nv.isDate() )          return VSPACE_DATE ;
        if ( nv.isTime() )          return VSPACE_TIME ;
        if ( nv.isDuration() )      return VSPACE_DURATION ;
        
        if ( nv.isGYear() )         return VSPACE_G_YEAR ;
        if ( nv.isGYearMonth() )    return VSPACE_G_YEARMONTH ;
        if ( nv.isGMonth() )        return VSPACE_G_MONTH ;
        if ( nv.isGMonthDay() )     return VSPACE_G_MONTHDAY ;
        if ( nv.isGDay() )          return VSPACE_G_DAY ;
        
        if ( SystemARQ.ValueExtensions && nv.isDate() )
            return VSPACE_DATE ;
        
        if ( nv.isString())         return VSPACE_STRING ;
        if ( nv.isBoolean())        return VSPACE_BOOLEAN ;
        
        if ( ! nv.isLiteral() )     return VSPACE_NODE ;

        if ( SystemARQ.ValueExtensions && nv.getNode() != null &&
             nv.getNode().isLiteral() &&
             ! nv.getNode().getLiteralLanguage().equals("") )
            return VSPACE_LANG ;
        
        return VSPACE_UNKNOWN ;
    }
        
    // ----------------------------------------------------------------
    // ---- Node operations
    
    public static Node toNode(NodeValue nv)
    {
        if ( nv == null )
            return null ;
        return nv.asNode() ;
    }
        
    public final Node asNode()
    { 
        if ( node == null )
            node = makeNode() ;
        return node ;
    }
    protected abstract Node makeNode() ;
    
    /** getNode - return the node form - may be null (use .asNode() to force to a node) */
    public Node getNode() { return node ; }
    
    public String getDatatypeURI() { return asNode().getLiteralDatatypeURI() ; }
    
    public boolean hasNode() { return node != null ; }
    
    // ----------------------------------------------------------------
    // ---- Subclass operations 
    
    public boolean isBoolean()      { return false ; } 
    public boolean isString()       { return false ; } 

    public boolean isNumber()       { return false ; }
    public boolean isInteger()      { return false ; }
    public boolean isDecimal()      { return false ; }
    public boolean isFloat()        { return false ; }
    public boolean isDouble()       { return false ; }
    
    public boolean hasDateTime()    { return isDateTime() || isDate() || isTime() || isGYear() || isGYearMonth() || isGMonth() || isGMonthDay() || isGDay() ; }
    public boolean isDateTime()     { return false ; }
    public boolean isDate()         { return false ; }
    public boolean isLiteral()      { return getNode() == null || getNode().isLiteral() ; }
    public boolean isTime()         { return false ; }
    public boolean isDuration()     { return false ; }

    public boolean isYearMonth()
    {
        if ( ! isDuration() ) return false ;
        Duration dur = getDuration() ;
        return ( dur.isSet(YEARS) || dur.isSet(MONTHS) ) &&
               ! dur.isSet(DAYS) && ! dur.isSet(HOURS) && ! dur.isSet(MINUTES) && ! dur.isSet(SECONDS) ;
    }

    boolean isDayTime()
    {
        if ( ! isDuration() ) return false ;
        Duration dur = getDuration() ;
        return !dur.isSet(YEARS) && ! dur.isSet(MONTHS) &&
            ( dur.isSet(DAYS) || dur.isSet(HOURS) || dur.isSet(MINUTES) || dur.isSet(SECONDS) );
    }

    public boolean isGYear()        { return false ; }
    public boolean isGYearMonth()   { return false ; }
    public boolean isGMonth()       { return false ; }
    public boolean isGMonthDay()    { return false ; }
    public boolean isGDay()         { return false ; }
    
    public boolean     getBoolean()     { raise(new ExprEvalTypeException("Not a boolean: "+this)) ; return false ; }
    public String      getString()      { raise(new ExprEvalTypeException("Not a string: "+this)) ; return null ; }
    public BigInteger  getInteger()     { raise(new ExprEvalTypeException("Not an integer: "+this)) ; return null ; }
    public BigDecimal  getDecimal()     { raise(new ExprEvalTypeException("Not a decimal: "+this)) ; return null ; }
    public float       getFloat()       { raise(new ExprEvalTypeException("Not a float: "+this)) ; return Float.NaN ; }
    public double      getDouble()      { raise(new ExprEvalTypeException("Not a double: "+this)) ; return Double.NaN ; }
    // Value representation for all date and time values.
    public XMLGregorianCalendar getDateTime()    { raise(new ExprEvalTypeException("No DateTime value: "+this)) ; return null ; }
    public Duration    getDuration() { raise(new ExprEvalTypeException("Not a duration: "+this)) ; return null ; }

    // ----------------------------------------------------------------
    // ---- Setting : used when a node is used to make a NodeValue
    
    private static NodeValue nodeToNodeValue(Node node)
    {
        if ( node.isVariable() )
            Log.warn(NodeValue.class, "Variable passed to NodeValue.nodeToNodeValue") ;

        if ( ! node.isLiteral() )
            // Not a literal - no value to extract
            return new NodeValueNode(node) ;

        boolean hasLangTag = ( node.getLiteralLanguage() != null && ! node.getLiteralLanguage().equals("")) ;
        boolean isPlainLiteral = ( node.getLiteralDatatypeURI() == null && ! hasLangTag ) ; 
            
        if ( isPlainLiteral )
            return new NodeValueString(node.getLiteralLexicalForm(), node) ;

        if ( hasLangTag )
        {
            if ( node.getLiteralDatatypeURI() != null )
            {
                if ( NodeValue.VerboseWarnings )
                    Log.warn(NodeValue.class, "Lang tag and datatype (datatype ignored)") ;
            }
            return new NodeValueNode(node) ;
        }

        // Typed literal
        LiteralLabel lit = node.getLiteral() ;
        
        // This includes type testing
        //if ( ! lit.getDatatype().isValidLiteral(lit) )
        // Use this - already calculated when the node is formed. 
        if ( !node.getLiteral().isWellFormed() )
        {
            if ( NodeValue.VerboseWarnings )
            {
                String tmp =  FmtUtils.stringForNode(node) ;
                Log.warn(NodeValue.class, "Datatype format exception: "+tmp) ;
            }
            // Invalid lexical form.
            return new NodeValueNode(node) ;
        }
        
        NodeValue nv = _setByValue(node) ;
        if ( nv != null )
            return nv ;
            
        return new NodeValueNode(node) ;
        //raise(new ExprException("NodeValue.nodeToNodeValue: Unknown Node type: "+n)) ;
             
    }

    // Jena code does not have these types (yet)
    private static final String dtXSDdateTimeStamp      = XSD+"#dateTimeStamp" ; 
    private static final String dtXSDdayTimeDuration    = XSD+"#dayTimeDuration" ; 
    private static final String dtXSDyearMonthDuration  = XSD+"#yearMonthDuration" ; 
    private static final String dtXSDprecisionDecimal   = XSD+"#precisionDecimal" ; 
    
    // Returns null for unrecognized literal.
    private static NodeValue _setByValue(Node node)
    {
        if ( NodeUtils.hasLang(node) )
            // Check for RDF 1.1!
            return null ;
        LiteralLabel lit = node.getLiteral() ;
        String lex = lit.getLexicalForm() ;
        RDFDatatype datatype = lit.getDatatype() ;

        // Quick check.
        // Only XSD supported.
        // And (for testing) roman numerals.
        String datatypeURI = datatype.getURI() ;
        if ( ! datatypeURI.startsWith(xsdNamespace) && ! SystemARQ.EnableRomanNumerals )
        {
            // Not XSD.
            return null ;
        }

        try { // DatatypeFormatException - should not happen
            
            if ( SystemARQ.SameValueAsString && XSDstring.isValidLiteral(lit) ) 
                    // String - plain or xsd:string
                return new NodeValueString(lit.getLexicalForm(), node) ;
            
            // Otherwise xsd:string is like any other unknown datatype.
            // Ditto literals with language tags (which are handled by nodeToNodeValue)
            
            // isValidLiteral is a value test - not a syntactic test.  
            // This makes a difference in that "1"^^xsd:decimal" is a
            // valid literal for xsd:integer (all other cases are subtypes of xsd:integer)
            // which we want to become integer anyway).

            // Order here is promotion order integer-decimal-float-double
            
            if ( ! datatype.equals(XSDdecimal) ) 
            {
                // XSD integer and derived types 
                if ( XSDinteger.isValidLiteral(lit) )
                {
                    String s = node.getLiteralLexicalForm() ;
                    if ( s.startsWith("+") )
                        // BigInteger does not accept leading "+"
                        s = s.substring(1) ;
                    // Includes subtypes (int, byte, postiveInteger etc).
                    // NB Known to be valid for type by now
                    BigInteger integer = new BigInteger(s) ;
                    return new NodeValueInteger(integer, node) ;
                }
            }
            
            if ( datatype.equals(XSDdecimal) && XSDdecimal.isValidLiteral(lit) )
            {
                BigDecimal decimal = new BigDecimal(lit.getLexicalForm()) ;
                return new NodeValueDecimal(decimal, node) ;
            }
            
            if ( datatype.equals(XSDfloat) && XSDfloat.isValidLiteral(lit) )
            {
                // NB If needed, call to floatValue, then assign to double.
                // Gets 1.3f != 1.3d right
                float f = ((Number)lit.getValue()).floatValue() ;
                return new NodeValueFloat(f, node) ;
            }

            if ( datatype.equals(XSDdouble) && XSDdouble.isValidLiteral(lit) )
            {
                double d = ((Number)lit.getValue()).doubleValue() ;
                return new NodeValueDouble(d, node) ;
            }

            // XXX Pending Jena update ... 
            if ( ( datatype.equals(XSDdateTime) || dtXSDdateTimeStamp.equals(datatypeURI) ) &&
                    XSDdateTime.isValid(lex) ) 
            {
                XSDDateTime dateTime = (XSDDateTime)lit.getValue() ;
                return new NodeValueDT(lex, node) ;
            }
            
            if ( datatype.equals(XSDdate) && XSDdate.isValidLiteral(lit) )
            {
                // Jena datatype support works on masked dataTimes. 
                XSDDateTime dateTime = (XSDDateTime)lit.getValue() ;
                return new NodeValueDT(lex, node) ;
            }
            
            if ( datatype.equals(XSDtime) && XSDtime.isValidLiteral(lit) )
            {
                // Jena datatype support works on masked dataTimes. 
                XSDDateTime time = (XSDDateTime)lit.getValue() ;
                return new NodeValueDT(lex, node) ;
            }
            
            if ( datatype.equals(XSDgYear) && XSDgYear.isValidLiteral(lit) )
            {
                XSDDateTime time = (XSDDateTime)lit.getValue() ;
                return new NodeValueDT(lex, node) ;
            }
            if ( datatype.equals(XSDgYearMonth) && XSDgYearMonth.isValidLiteral(lit) )
            {
                XSDDateTime time = (XSDDateTime)lit.getValue() ;
                return new NodeValueDT(lex, node) ;
            }
            if ( datatype.equals(XSDgMonth) && XSDgMonth.isValidLiteral(lit) )
            {
                XSDDateTime time = (XSDDateTime)lit.getValue() ;
                return new NodeValueDT(lex, node) ;
            }
            
            if ( datatype.equals(XSDgMonthDay) && XSDgMonthDay.isValidLiteral(lit) )
            {
                XSDDateTime time = (XSDDateTime)lit.getValue() ;
                return new NodeValueDT(lex, node) ;
            }
            if ( datatype.equals(XSDgDay) && XSDgDay.isValidLiteral(lit) )
            {
                XSDDateTime time = (XSDDateTime)lit.getValue() ;
                return new NodeValueDT(lex, node) ;
            }
            
            // XXX Pending Jena update ... 
            if ( ( datatype.equals(XSDduration) || 
                   dtXSDdayTimeDuration.equals(datatypeURI) || 
                   dtXSDyearMonthDuration.equals(datatypeURI) ) &&
                   XSDduration.isValid(lex) ) // use lex
            {
                Duration duration = xmlDatatypeFactory.newDuration(lex) ;
                
                if ( dtXSDdayTimeDuration.equals(datatypeURI) && ! XSDFuncOp.isDayTime(duration) )
                    return null ;
                if ( dtXSDyearMonthDuration.equals(datatypeURI) && ! XSDFuncOp.isYearMonth(duration) )
                    return null ;
                
                return new NodeValueDuration(duration, node) ;
            }
            
            if ( datatype.equals(XSDboolean) && XSDboolean.isValidLiteral(lit) )
            {
                boolean b = (Boolean) lit.getValue();
                return new NodeValueBoolean(b, node) ;
            }
            
            // If wired into the TypeMapper via RomanNumeralDatatype.enableAsFirstClassDatatype
//            if ( RomanNumeralDatatype.get().isValidLiteral(lit) )
//            {
//                int i = ((RomanNumeral)lit.getValue()).intValue() ;
//                return new NodeValueInteger(i) ; 
//            }
            
            // Not wired in
            if ( SystemARQ.EnableRomanNumerals )
            {
                if ( lit.getDatatypeURI().equals(RomanNumeralDatatype.get().getURI()) )
                {
                    Object obj = RomanNumeralDatatype.get().parse(lit.getLexicalForm()) ;
                    if ( obj instanceof Integer )
                        return new NodeValueInteger(((Integer)obj).longValue()) ; 
                    if ( obj instanceof RomanNumeral )
                        return new NodeValueInteger( ((RomanNumeral)obj).intValue() ) ;
                    throw new ARQInternalErrorException("DatatypeFormatException: Roman numeral is unknown class") ;
                }
            }            
            
        } catch (DatatypeFormatException ex)
        {
            // Should have been caught earlier by special test in nodeToNodeValue
            throw new ARQInternalErrorException("DatatypeFormatException: "+lit, ex) ;
        }
        return null ;
    }
    
    // ----------------------------------------------------------------

    // Point to catch all exceptions.
    public static void raise(ExprException ex)
    {
        throw ex ; 
    }

    @Override
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }

    private void forceToNode()
    {
        if ( node == null ) 
            node = asNode() ;
        
        if ( node == null )
            raise(new ExprEvalException("Not a node: "+this)) ;
    }
    
    // ---- Formatting (suitable for SPARQL syntax).
    // Usually done by being a Node and formatting that.
    // In desperation, will try toString() (no quoting)
    
    public final String asUnquotedString()
    { return asString() ; }

    public final String asQuotedString()
    { return asQuotedString(new SerializationContext()) ; }

    public final String asQuotedString(SerializationContext context)
    { 
        // If possible, make a node and use that as the formatted output.
        if ( node == null )
            node = asNode() ;
        if ( node != null )
            return FmtUtils.stringForNode(node, context) ;
        return toString() ;
    }

    // Convert to a string  - usually overridden.
    public String asString()
    {
        // Do not call .toString() 
        forceToNode() ;
        return NodeFunctions.str(node) ;
    }
    
    @Override
    public int hashCode() 
    {
        return asNode().hashCode() ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        // This is the equality condition Jena uses - lang tags are different by case. 
        if ( this == other ) return true ;

        if ( ! ( other instanceof NodeValue ) )
            return false ;
        NodeValue nv = (NodeValue)other ;
        return asNode().equals(nv.asNode()) ;
        // Not NodeFunctions.sameTerm (which smooshes language tags by case)
    }

    public abstract void visit(NodeValueVisitor visitor) ;
    
    public Expr apply(ExprTransform transform)  { return transform.transform(this) ; }

    @Override
    public String toString()
    { 
        return asQuotedString() ;
    }
}
