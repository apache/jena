/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.util.* ;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.* ;


/** A mapping from variable name to a value.
 * 
 * @author   Andy Seaborne
 * @version  $Id: ResultBindingImpl.java,v 1.4 2005-02-21 12:15:26 andy_seaborne Exp $
 */


public class ResultBindingImpl implements ResultBinding
{
	// We store name -> the value, not the variable itself.  ResultBindings will
    // keep the association of name-value.

    ResultBindingImpl parent = null ;

    List varNames = new ArrayList() ;
    List values = new ArrayList() ;     // Values or usually RDFNodes (Resource or Properties or Literals)
    List causalTriples = null ;  
    Query query = null ;

    public ResultBindingImpl(ResultBindingImpl parent) { this.parent = parent ; }
    public ResultBindingImpl() { this(null) ; }
    
    public int add(String varName, Value value)
    {
        varNames.add(varName) ;
        values.add(value) ;
        check() ;
        return varNames.size()-1 ;
    }

    public int add(String varName, RDFNode node)
    {
        varNames.add(varName) ;
        values.add(node) ;
        check() ;
        return varNames.size()-1 ;
    }

    public void setQuery(Query q)
    {
        query = q ;
    }
    

    /** Add a triple to the ResultBindingImpl. Assumed to be related to
     *  the some binding of this result binding.
     */
    // Stop being public when/if remote QueryEngineRemote moves into rdf.query
    public void addTriple(Statement s)
    {
        if ( causalTriples == null )
            causalTriples = new ArrayList() ; 
        causalTriples.add(s) ;
    }

    /** Get the set of statements that caused this ResultBindingImpl.
     *  Note: returns a set so there may be less statements in some result bindings
     *  than in others and there may be less than the number of triple patterns in
     *  the query.
     */
    public Set getTriples()
    {
        Set set = new HashSet() ;
        getTriples(set) ;
        return set ;
    }
    
    // Worker function
    private void getTriples(Collection acc)
    {
        if ( causalTriples == null )
        {
            causalTriples = new ArrayList() ;
            
            if ( query != null )
            {
                Model model = query.getSource() ;
                try {
                    for ( Iterator iter = query.getTriplePatterns().iterator() ; iter.hasNext() ; )
                    {
                        Triple t1 = (Triple)iter.next() ;
                        Triple t2 = QueryEngine.substituteIntoTriple(t1, this) ;
                        RDFNode s = QueryEngine.convertGraphNodeToRDFNode(t2.getSubject(), model) ;
                        RDFNode p = QueryEngine.convertGraphNodeToRDFNode(t2.getPredicate(), model) ;
                        if ( p instanceof Resource )
                            p = model.createProperty(((Resource)p).getURI()) ;
                        RDFNode o = QueryEngine.convertGraphNodeToRDFNode(t2.getObject(), model) ;
                        Statement stmt = model.createStatement((Resource)s, (Property)p, o) ;
                        causalTriples.add(stmt) ;
                    }
                } catch (Exception ex)
                {
                    System.err.println("ResultBindingImpl.getTriples: Substitution error: "+ex) ;
                }
            }
        }
        
        if ( causalTriples == null )
            return ;
            
        acc.addAll(causalTriples) ;
    }
    
    /** Merge the triples that caused this result binding into a model.
     *  @return The model passed in
     */
    public Model mergeTriples(Model model)
    {
        Set s = getTriples() ;
        for ( Iterator iter = s.iterator() ; iter.hasNext() ; )
        {
            model.add((Statement)iter.next()) ;
        }
        return model ;
    }
    
    public Iterator names() { return new ResultBindingIterator(this) ; }
    
    public ResultBindingIterator iterator()
    {
        return new ResultBindingIterator(this) ;
    }

    public Object get(String varName)
    {
        return lookup(varName, 0) ;
    }

    private Object lookup(String varName, int localOffset)
    {
        for ( int i = localOffset ; i < varNames.size() ; i++ )
        {
            if ( varName.equals((String)varNames.get(i)) )
            {
                // Turn graph Nodes into Model onjects
                Object obj = values.get(i) ;
                if ( obj instanceof RDFNode )
                    return obj ;
                if ( obj instanceof Node )
                    return QueryEngine.convertGraphNodeToRDFNode((Node)obj, query.getSource()) ;
            }
        }

        if ( parent == null )
            return null ;

        // Tail recursion
        return parent.lookup(varName,0) ;
    }


//    public Value getValue(String varName)
//    {
//        Object tmp = get(varName) ;
//        if ( ! ( tmp instanceof Node ) )
//            return null ;
//        return convert(tmp) ;
//    }
//
//    static private Value convert(Object arg)
//    {
//        if ( arg == null ) return null ;
//
//        if ( arg instanceof Value ) return (Value)arg ;
//
//        // Try to turn an RDFNode into a Value.
//        // RDFNodes are RDF Literals, Resources, Properties or a container.
//        // But containers are Resources.
//
//        // Properties and Resources
//        if ( arg instanceof Resource )
//        {
//            WorkingVar w = new WorkingVar() ;
//            w.setRDFResource((Resource)arg) ;
//            return w ;
//        }
//
//        if ( arg instanceof Literal )
//        {
//            WorkingVar w = new WorkingVar() ;
//            w.setRDFLiteral((Literal)arg) ;
//            return w ;
//        }
//
//        // Opps!
//        throw new RDQL_InternalErrorException("ResultBinding: unexpected object class: "+arg.getClass().getName()) ;
//        //return null ;
//    }


    /** Set the parent ResultBindingImpl.  This is only needed for testing of parts of the query engine */
    
    void setParent(ResultBindingImpl p)
    {
        //QSys.assertTrue( parent == null, "Attempt to change parent", "ResultBindingImpl", "setParent") ;
        parent = p ;
    }

    public int size()
    {
        int size = varNames.size() ;

        if ( parent != null )
            size += parent.size() ;

        return size ;
    }

    // Check for multiple bindings of the same variable.
    public void check()
    {
        for (int i = 0 ; i < varNames.size() ; i++ )
        {
            String varName = (String)varNames.get(i) ;
            Object tmp = lookup(varName, i+1) ;
            //QSys.assertTrue(tmp == null , "Duplicate binding: "+this, "ResultBindingImpl", "check") ;
        }
    }


    public String toString()
    {
        StringBuffer sbuff = new StringBuffer("") ;
        for (int i = 0 ; i < varNames.size() ; i++ )
        {
            if ( i != 0 )
                sbuff.append(" ") ;
            sbuff.append("("+varNames.get(i)+", "+values.get(i)+")") ;
        }

        if ( parent != null )
        {
            String tmp = parent.toString() ;
            if ( tmp != null && (tmp.length() != 0 ) )
            {
                sbuff.append(" ") ;
                sbuff.append(tmp) ;
            }
        }
        return sbuff.toString() ;
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
