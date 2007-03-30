/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.nodevalue.*;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.*;
import com.hp.hpl.jena.vocabulary.XSD;


public abstract class NodeValue extends ExprNode
{
    // Maybe:: NodeValueStringLang - strings with language tag
    
    /* Naming:
     * getXXX => plain accessor
     * asXXX =>  force to the required thing if necessary. 
     * 
     * Implementation notes:
     * 
     * 1. There is little point delaying turning a node into its value.
     *    Because a NodeValue is being created, it is almost certainly
     *    going to be used for it's value, so processing the datatype
     *    can be done at creation time for no loss of efficiency but
     *    it is clearer.
     *    
     * 2. Conversely, delaying turing a value into a graph node is
     *    valuable because intmediates, like the result of 2+3, will not
     *    be needed as nodes unless assignment (and there is no assignment
     *    in SPARQL even if there is for ARQ). 
     *    Node level operations like str() don't need a full node.
     *      
     * 3. The XQuery/Xpath functions and operations are implemented in
     *    a separate class Function for convenience.
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
    
    // Effective boolean value rules.
    //   boolean: value of the boolean 
    //   string: length(string) > 0 is true
    //   numeric: number != Nan && number != 0 is true
    // http://www.w3.org/TR/xquery/#dt-ebv
    
    private static Log log = LogFactory.getLog(NodeValue.class) ;
    
    // ---- Constants and initializers / public
    
    public static boolean VerboseWarnings = true ;
    public static boolean VerboseExceptions = false ;
    
    private static boolean VALUE_EXTENSIONS = ARQ.getContext().isTrueOrUndef(ARQ.extensionValueTypes) ;
    private static boolean sameValueAsString = VALUE_EXTENSIONS ;
    
    private static RefBoolean enableRomanNumerals = new RefBoolean(ARQ.enableRomanNumerals, false) ;
    //private static RefBoolean strictSPARQL = new RefBoolean(ARQ.strictSPARQL, false) ;
    
    public static final BigInteger IntegerZERO = new BigInteger("0") ; // Java-1.5 :: BigInteger.ZERO ;
    public static final BigDecimal DecimalZERO = new BigDecimal("0") ; // Java-1.5 :: BigDecimal.ZERO ;

    public static final NodeValue TRUE   = NodeValue.makeNode("true", XSDDatatype.XSDboolean) ;
    public static final NodeValue FALSE  = NodeValue.makeNode("false", XSDDatatype.XSDboolean) ;
    
    
    // Initialization
//    static
//    {}
    
    // ---- State
    
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
//        log.warn("Unknown NodeValue in constructor: "+v) ;
//    }

    // ----------------------------------------------------------------
    // ---- Construct NodeValue without a graph node.
    
    public static NodeValue makeInteger(long i)
    { return new NodeValueInteger(i) ; }
    
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
    //{ return new NodeValueDecimal(new BigDecimal(i)) ; } // Java 1.5-ism - the long constructor.  If 1.4, type promotion to double :-( 
    { return new NodeValueDecimal(BigDecimal.valueOf(i)) ; }
  
    public static NodeValue makeDecimal(double d)
    { return new NodeValueDecimal(new BigDecimal(d)) ; }   // java 1.5 ; use BigDecimal.valueOf(double)
  
    public static NodeValue makeDecimal(String lexicalForm)
    { return NodeValue.makeNode(lexicalForm, XSDDatatype.XSDdecimal) ; }

    public static NodeValue makeDateTime(String lexicalForm)
    { return NodeValue.makeNode(lexicalForm, XSDDatatype.XSDdateTime) ; }
    
    public static NodeValue makeDateTime(Calendar cal)
    { return new NodeValueDateTime(cal) ; }

    public static NodeValue makeDate(String lexicalForm)
    { return NodeValue.makeNode(lexicalForm, XSDDatatype.XSDdate) ; }
    
    public static NodeValue makeDate(Calendar cal)
    { return new NodeValueDate(cal) ; }

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
        Node n = Node.createLiteral(lexicalForm, null, dtype) ;
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
            log.warn("Both lang tag and datatype defined (lexcial form '"+lexicalForm+"')") ;
        
        Node n = null ; 
        
        if ( datatype != null)
        {
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatype) ;
            n = Node.createLiteral(lexicalForm, null, dType) ;
        }
        else
            n = Node.createLiteral(lexicalForm, langTag, null) ;
        return NodeValue.makeNode(n) ;
    }
    
    // ----------------------------------------------------------------
    // ---- Construct NodeValue with graph node and value. 

    public static NodeValue makeNodeBoolean(boolean b)
    { return b ? NodeValue.TRUE : NodeValue.FALSE ; }

    public static NodeValue makeNodeBoolean(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, null, XSD.xboolean.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeInteger(long v)
    {
        NodeValue nv = makeNode(Long.toString(v), null, XSD.integer.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeInteger(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, null, XSD.integer.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeFloat(float f)
    {
        NodeValue nv = makeNode(Utils.stringForm(f), null, XSD.xfloat.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeFloat(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, null, XSD.xdouble.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDouble(double v)
    {
        NodeValue nv = makeNode(Utils.stringForm(v), null, XSD.xdouble.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDouble(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, null, XSD.xdouble.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDecimal(BigDecimal decimal)
    {
        // Java 1.5-ism
        //NodeValue nv = makeNode(decimal.toPlainString(), null, XSD.decimal.getURI()) ;
        NodeValue nv = makeNode(Utils.stringForm(decimal), null, XSD.decimal.getURI()) ;
        return nv ;
    }

    public static NodeValue makeNodeDecimal(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, null, XSD.decimal.getURI()) ;
        return nv ;
    }
    
    public static NodeValue makeNodeString(String string)
    {
        NodeValue nv = makeNode(string, null, (String)null) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDateTime(Calendar date)
    {
        //XSDDateTime dt = new XSDDateTime(date) ;
        String lex = Utils.calendarToXSDDateTimeString(date) ;
        NodeValue nv = makeNode(lex, XSDDatatype.XSDdateTime) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDateTime(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, XSDDatatype.XSDdateTime) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDate(Calendar date)
    {
        //XSDDateTime dt = new XSDDateTime(date) ;
        String lex = Utils.calendarToXSDDateString(date) ;
        NodeValue nv = makeNode(lex, XSDDatatype.XSDdate) ;
        return nv ;
    }
    
    public static NodeValue makeNodeDate(String lexicalForm)
    {
        NodeValue nv = makeNode(lexicalForm, XSDDatatype.XSDdate) ;
        return nv ;
    }
    
   // ----------------------------------------------------------------
   // ---- Expr interface
    
    public NodeValue eval(Binding binding, FunctionEnv env)
    { return this ; }

    // NodeValues are immutable so no need to duplicate.
    public Expr copySubstitute(Binding binding, boolean foldConstants)
    {  // return this ; 
        Node n = asNode() ;
        return makeNode(n) ;
    }
    
    // New version
    public NodeValue evalNodeValue(Binding binding, ExecutionContext execCxt)
    {
         //return this ;
        System.err.println("eval - convert back to NodeValue") ;
        return null ;
    }

    public Node evalNode(Binding binding, ExecutionContext execCxt)
    {
        return asNode() ;
    }

    
    public boolean isConstant() { return true ; }

    public NodeValue getConstant()     { return this ; }
    
    // ----------------------------------------------------------------
    // ---- sameValueAs
    
    // Disjoint value spaces : dateTime and dates are not comparable 
    // Every langtag implies another value space as well.
    private static final int VSPACE_NUM       = 10 ;
    private static final int VSPACE_DATETIME  = 20 ;
    private static final int VSPACE_DATE      = 30 ;
    private static final int VSPACE_STRING    = 40 ;
    private static final int VSPACE_LANG      = 50 ;    // Lang tag (valid - no datatype).
    private static final int VSPACE_BOOLEAN   = 60 ;
    private static final int VSPACE_NODE      = 70 ;    // RDT Terms that are not literals   
    private static final int VSPACE_UNKNOWN   = 80 ;    // Nodes - literal unknown value space or wrong in some way.
    private static final int VSPACE_DIFFERENT = 90 ;    // Known to be different values spaces
    
//    /** Return true if the two Nodes are known to be the same value
//     *  return false if known to be different values,
//     *  throw ExprEvalException if unknown
//     */
//    public static boolean sameAs(Node n1, Node n2)
//    {
//        return sameAs(NodeValue.makeNode(n1), NodeValue.makeNode(n2)) ;  
//    }

    /** Return true if the two NodeValues are known to be the same value
     *  return false if known to be different values,
     *  throw ExprEvalException otherwise
     */
    public static boolean sameAs(NodeValue nv1, NodeValue nv2)
    {
        // Currently only called from E_Equals/E_NotEquals
        if ( nv1 == null || nv2 == null )
            new ARQInternalErrorException("Attempt to sameValueAs on a null") ;
        
        int compType = classifyValueOp(nv1, nv2) ;
        switch (compType)
        {
            case VSPACE_NUM:        return Functions.compareNumeric(nv1, nv2) == 0 ;
            case VSPACE_DATETIME:   return Functions.compareDateTime(nv1, nv2) == 0 ;
            case VSPACE_DATE:       return Functions.compareDate(nv1, nv2) == 0 ;
            case VSPACE_STRING:     return Functions.compareString(nv1, nv2) == 0 ;
            case VSPACE_BOOLEAN:    return Functions.compareBoolean(nv1, nv2) == 0 ;
            
            case VSPACE_LANG:
            {
                // two literals, both with a language tag
                Node node1 = nv1.getNode() ;
                Node node2 = nv2.getNode() ;
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
                
                if ( ! VALUE_EXTENSIONS && ( node1.isLiteral() && node2.isLiteral() ) )
                {
                    return NodeFunctions.rdfTermEquals(node1, node2) ;
//                    if ( NodeFunctions.sameTerm(node1, node2) )
//                        return true ;
//                    else
//                        raise(new ExprNotComparableException("Unknown equality test: "+nv1+" and "+nv2)) ;
                }
                
                // Zero or one non-literal (not both non-literals), that's case VSPACE_NODE)
                if ( ! node1.isLiteral() || ! node2.isLiteral() )
                    // So it's false? node1 can't be same form as node2.
                    return false ;
                    //return NodeFunctions.sameTerm(node1, node2) ;
                
                // Two literals
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
                if ( ! VALUE_EXTENSIONS && ( nv1.isLiteral() && nv2.isLiteral() ) )
                    raise(new ExprEvalException("Incompatible: "+nv1+" and "+nv2)) ;
                return false ; 
            default:
                throw new ARQInternalErrorException("sameValueAs failure"+nv1+" and "+nv2) ;
        }
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
        nv1.forceToNode() ;
        nv2.forceToNode() ;
        return NodeUtils.compareRDFTerms(nv1.getNode(), nv2.getNode()) ;
    }
    
    /** Compare by value (and only value) if possible.
     *  Supports <, <=, >, >= but not = nor != (which are sameValueAs and notSameValueAs)
     * @param nv1
     * @param nv2
     * @return negative, 0 , or positive for not possible, less than, equal, greater than.
     * @throws ExprEvalException  
     */
    public static int compare(NodeValue nv1, NodeValue nv2)
    {
        // Called from E_LessThat etc
        // and NodeUtils.comparLiteralsByValue 
        if ( nv1 == null || nv2 == null )
            //raise(new ExprEvalException("Attempt to notSameValueAs on null") ;
            new ARQInternalErrorException("Attempt to compare on null") ;
        int x = compare(nv1, nv2, false) ;
        return x ;
    }
    
    // E_GreaterThan/E_LessThan/E_GreaterThanOrEqual/E_LessThanOrEqual
    // ==> compare(nv1, nv12) => compare (nv1, nv2, false)
    
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
        
        int compType = classifyValueOp(nv1, nv2) ;
        
        switch (compType)
        {
            case VSPACE_NUM:        return Functions.compareNumeric(nv1, nv2) ;
            case VSPACE_DATETIME:   return Functions.compareDateTime(nv1, nv2) ;
            case VSPACE_DATE:       return Functions.compareDate(nv1, nv2) ;
            case VSPACE_STRING:
            {
                int cmp = Functions.compareString(nv1, nv2) ;
                
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
            case VSPACE_BOOLEAN:    return Functions.compareBoolean(nv1, nv2) ;
            
            case VSPACE_LANG:
            {
                // Two literals, both with language tags.
                Node node1 = nv1.asNode() ;
                Node node2 = nv2.asNode() ;
                int x = StringUtils.strCompareIgnoreCase(node1.getLiteralLanguage(), node2.getLiteralLanguage()) ;
                
                if ( x != Expr.CMP_EQUAL )
                {
                    // Different lang tags
                    if ( ! sortOrderingCompare )
                        raise(new ExprNotComparableException("Can't compare (different languages) "+nv1+" and "+nv2)) ;
                    // Different lang tags - sorting
                    return x ;
                }

                // same lang tag (case insensitive)
                x = StringUtils.strCompare(node1.getLiteralLexicalForm(), node2.getLiteralLexicalForm()) ;
                if ( x != Expr.CMP_EQUAL )
                    return x ;
                // Same lexcial forms, same lang tag by value
                // Try to split by syntactic lang tags.
                x = StringUtils.strCompare(node1.getLiteralLanguage(), node2.getLiteralLanguage()) ;
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
            default:
                throw new ARQInternalErrorException("Compare failure "+nv1+" and "+nv2) ;
        }
    }

    private static int classifyValueOp(NodeValue nv1, NodeValue nv2)
    {
        int c1 = classifyValueSpace(nv1) ;
        int c2 = classifyValueSpace(nv2) ;
        if ( c1 == c2 ) return c1 ;
        if ( c1 == VSPACE_UNKNOWN || c2 == VSPACE_UNKNOWN )
            return VSPACE_UNKNOWN ;
        
        // Known values spaces but incompatible  
        return VSPACE_DIFFERENT ;
    }
    
    private static int classifyValueSpace(NodeValue nv)
    {
        if ( nv.isNumber() )   return VSPACE_NUM ;
        if ( nv.isDateTime() ) return VSPACE_DATETIME ;
        
        
        if ( VALUE_EXTENSIONS && nv.isDate() )
            return VSPACE_DATE ;
        if ( nv.isString())    return VSPACE_STRING ;
        if ( nv.isBoolean())   return VSPACE_BOOLEAN ;
        
        if ( ! nv.isLiteral() )
            return VSPACE_NODE ;

        if ( VALUE_EXTENSIONS && nv.getNode() != null &&
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
    
    public Node getNode() { return node ; }
    
    public boolean hasNode() { return node != null ; }

    // ----------------------------------------------------------------
    // ---- Subclass operations 
    
    // One of the known types. 
    public boolean hasKnownValue()    { return isBoolean() || isNumber() || isString() || isDateTime() || isDate() ; }
    
    //Don't forget: dynamicNumberConversion
    
    public boolean isBoolean()     { return false ; } 
//    public boolean isBooleanValue()      { return false ; }
//    public boolean isBooleanBEV()
//    { return isBooleanValue() || isNumber() || isString() ; }

    public boolean isString()      { return false ; } 

    public boolean isNumber()      { return false ; }
    public boolean isInteger()     { return false ; }
    public boolean isDecimal()     { return false ; }
    public boolean isFloat()       { return false ; }
    public boolean isDouble()      { return false ; }
    
    public boolean isDateTime()    { return false ; }
    public boolean isDate()        { return false ; }
    public boolean isLiteral()     { return getNode() == null || getNode().isLiteral() ; } 
    // getters
    
    public boolean getBoolean()
    { raise(new ExprEvalException("Not a boolean: "+this)) ; return false ; }
    
    public String      getString()   { raise(new ExprEvalException("Not a string: "+this)) ; return null ; }
    public BigInteger  getInteger()  { raise(new ExprEvalException("Not an integer: "+this)) ; return null ; }
    public BigDecimal  getDecimal()  { raise(new ExprEvalException("Not a decimal: "+this)) ; return null ; }
    public float       getFloat()    { raise(new ExprEvalException("Not a float: "+this)) ; return Float.NaN ; }
    public double      getDouble()   { raise(new ExprEvalException("Not a double: "+this)) ; return Double.NaN ; }
    public Calendar    getDateTime() { raise(new ExprEvalException("Not a dateTime: "+this)) ; return null ; }
    public Calendar    getDate()     { raise(new ExprEvalException("Not a date: "+this)) ; return null ; }

//    // ---- Force to a type : Needed? 
//    
//    public boolean asBoolean() { raise(new ExprEvalException("Not compatible with boolean: "+this)) ; return false ; } 
//
//    //public String asString() { raise(new ExprEvalException("Not a string: "+this)) ; return null ; }
//
//    public long asInteger()  { raise(new ExprEvalException("Not compatible with integer: "+this)) ; return -999 ; }
//    public double asDouble() { raise(new ExprEvalException("Not compatible with double: "+this)) ; return Double.NaN ; }
//    public float asFloat()   { raise(new ExprEvalException("Not compatible with float: "+this)) ; return Float.NaN ; }
//    public BigDecimal asDecimal() { raise(new ExprEvalException("Not compatible with decimal: "+this)) ; return null ; }
//    public Calendar asDate() { raise(new ExprEvalException("Not compatible with a date: "+this)) ; return null ; }
    

    // ----------------------------------------------------------------
    // ---- Setting : used when a node is used to make a NodeValue
    
    private static NodeValue nodeToNodeValue(Node node)
    {
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
                    log.warn("Lang tag and datatype (datatype ignored)") ;
                else if ( log.isDebugEnabled() )
                    log.debug("Lang tag and datatype (datatype ignored)") ; 
            }
            return new NodeValueNode(node) ;
        }

        // Typed literal
        LiteralLabel lit = node.getLiteral() ;
        
        
        // This includes type testing
        if ( ! lit.getDatatype().isValidLiteral(lit) )
        {
            if ( NodeValue.VerboseWarnings )
            {
                String tmp =  FmtUtils.stringForNode(node, ARQConstants.getGlobalPrefixMap()) ;
                log.warn("Datatype format exception: "+tmp) ;
            }
            else if ( log.isDebugEnabled() )
            {
                String tmp =  FmtUtils.stringForNode(node, ARQConstants.getGlobalPrefixMap()) ;
                log.debug("Datatype format exception: "+tmp) ;
            }
            // Invalid lexical form.
            return new NodeValueNode(node) ;
        }
        
        NodeValue nv = _setByValue(node) ;
        if ( nv != null )
            return nv ;
            
        // No idea.  
        if ( log.isDebugEnabled() )
            log.debug("Unrecognized literal: "+node) ;
        return new NodeValueNode(node) ;
        //raise(new ExprException("NodeValue.nodeToNodeValue: Unknown Node type: "+n)) ;
             
    }

    // Returns null for unrecognized literal.
    private static NodeValue _setByValue(Node node)
    {
        LiteralLabel lit = node.getLiteral() ;
        
        try { // DatatypeFormatException - should not happen

            if ( sameValueAsString && XSDDatatype.XSDstring.isValidLiteral(node.getLiteral()) ) 
                    // String - plain or xsd:string
                return new NodeValueString(lit.getLexicalForm(), node) ;
            
            // Otherwise xsd:string is like any other unknown datatype.
            // Ditto literals with language tags (which are handled by nodeToNodeValue)
            
            // isValidLiteral is a value test - not a syntactic test.  
            // This makes a diffeence in that "1"^^xsd:decimal" is a
            // valid literal for xsd:integer (all other cases are subtypes of xsd:integer)
            // which we want to become integer anyway).

            // Order here is promotion order integer-decimal-float-double
            
            if ( ! node.getLiteralDatatype().equals(XSDDatatype.XSDdecimal) ) 
            {
                if ( XSDDatatype.XSDinteger.isValidLiteral(lit) )
                {
                    // Includes subtypes (int, byte, postiveInteger etc).
                    // NB Known to be valid for type by now
                    long i = ((Number)lit.getValue()).longValue() ;
                    return new NodeValueInteger(i, node) ;
                }
            }
            
            if ( XSDDatatype.XSDdecimal.isValidLiteral(lit) )
            {
                BigDecimal decimal = new BigDecimal(lit.getLexicalForm()) ;
                return new NodeValueDecimal(decimal, node) ;
            }
            
            if ( XSDDatatype.XSDfloat.isValidLiteral(lit) )
            {
                // NB If needed, call to floatValue, then assign to double.
                // Gets 1.3f != 1.3d right
                float f = ((Number)lit.getValue()).floatValue() ;
                return new NodeValueFloat(f, node) ;
            }

            if ( XSDDatatype.XSDdouble.isValidLiteral(lit) )
            {
                double d = ((Number)lit.getValue()).doubleValue() ;
                return new NodeValueDouble(d, node) ;
            }

            if ( XSDDatatype.XSDdateTime.isValidLiteral(lit) ) 
            {
                XSDDateTime dateTime = (XSDDateTime)lit.getValue() ;
                return new NodeValueDateTime(dateTime.asCalendar(), node) ;
            }
            
            if ( XSDDatatype.XSDdate.isValidLiteral(lit) )
            {
                // Jena datatype support works on masked dataTimes. 
                XSDDateTime dateTime = (XSDDateTime)lit.getValue() ;
                return new NodeValueDate(dateTime.asCalendar(), node) ;
            }
            
            if ( XSDDatatype.XSDboolean.isValidLiteral(lit) )
            {
                boolean b = ((Boolean)lit.getValue()).booleanValue() ;
                return new NodeValueBoolean(b, node) ;
            }
            
            // If wired into the TypeMapper via RomanNumeralDatatype.enableAsFirstClassDatatype
//            if ( RomanNumeralDatatype.get().isValidLiteral(lit) )
//            {
//                int i = ((RomanNumeral)lit.getValue()).intValue() ;
//                return new NodeValueInteger(i) ; 
//            }
            
            // Not wired in
            if ( enableRomanNumerals.getValue() )
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
        if ( log.isDebugEnabled() )
            log.debug("Exception: "+ex.getMessage()) ;
        throw ex ; 
    }

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
    
    public int hashCode() 
    {
        return asNode().hashCode() ;
    }
    
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        if ( ! ( other instanceof NodeValue ) )
            return false ;
        NodeValue nv = (NodeValue)other ;
        return NodeFunctions.sameTerm(this.asNode(), nv.asNode()) ;
    }

    public abstract void visit(NodeValueVisitor visitor) ;
    
    public String toString()
    { 
        return asQuotedString() ;
    }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
