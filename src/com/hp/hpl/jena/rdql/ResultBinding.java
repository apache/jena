/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.util.* ;
import com.hp.hpl.jena.rdf.model.* ;

/** A mapping from variable name to a value.
 * 
 * @author   Andy Seaborne
 * @version  $Id: ResultBinding.java,v 1.4 2003-03-11 18:03:16 andy_seaborne Exp $
 */


public class ResultBinding
{
	// We store name -> the value, not the variable itself, because variables
    // may be rebound in the process of the backtracking.  ResultBindings will
    // keep the association of name-value.

    // A binding for a triple pattern is at most 3 variables.
    // The three lists seem expensive for this.  A reallocating vector may be better.
    // Note: The remote query client and the QueryResultMem class create single,
    // long ResultBindings.
    
    ResultBinding parent = null ;
    List triples = new ArrayList() ;               // The triples that caused this ResultBinding

    List varNames = new ArrayList() ;
    List values = new ArrayList() ;     // Values or RDFNodes (Resource or Properties)

    public ResultBinding() {}
    
    // Return index of added item
    /** Set the parent ResultBinding.
     *  This is only needed for testing of parts of the query engine
     *  @param varName String name of the variable to bind
     *  @param value   The value to set
     *  @returns The index of the added value
     */

    public int add(String varName, Value value)
    {
        //QSys.assertTrue(varName != null , "Null variable name", "ResultBinding", "add") ;
        //QSys.assertTrue(varNames.size() == values.size(), "Lists out of step", "ResultBinding", "add") ;
        varNames.add(varName) ;
        values.add(value) ;
        check() ;
        return varNames.size()-1 ;
    }

    public int add(String varName, RDFNode node)
    {
        //QSys.assertTrue(varName != null , "Null variable name", "ResultBinding", "add") ;
        //QSys.assertTrue(varNames.size() == values.size(), "Lists out of step", "ResultBinding", "add") ;
        varNames.add(varName) ;
        values.add(node) ;
        check() ;
        return varNames.size()-1 ;
    }

    /** Add a triple to the ResultBinding. Assumed to be related to
     *  the some binding of this result binding.
     */
    // Stop being public when/if remote QueryEngineRemote moves into rdf.query
    public void addTriple(Statement s) { triples.add(s) ; }

    /** Get the set of statements that caused this ResultBinding.
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
        acc.addAll(triples) ;
        if ( parent != null )
            parent.getTriples(acc) ;
    }
    
    /** Merge the triples that caused this result binding into a model.
     *  @return The model passed in
     */
    public Model mergeTriples(Model model)
    {
        try {
            for ( Iterator iter = triples.iterator() ; iter.hasNext() ; )
                model.add((Statement)iter.next()) ;
            if ( parent != null )
                parent.mergeTriples(model) ;
        } catch (RDFException rdfEx)
        {
            QSys.unhandledException(rdfEx, "ResultBinding", "mergeTriples") ;
        }
        return model ;
    }
    
    public ResultBindingIterator iterator()
    {
        return new ResultBindingIterator(this) ;
    }

    public Object get(String varName)
    {
        //QSys.assertTrue(varName != null , "Null variable name", "ResultBinding", "get") ;
        //QSys.assertTrue(varNames.size() == values.size(), "Lists out of step", "ResultBinding", "get") ;

        return lookup(varName, 0) ;
    }

    private Object lookup(String varName, int localOffset)
    {
        for ( int i = localOffset ; i < varNames.size() ; i++ )
        {
            if ( varName.equals((String)varNames.get(i)) )
            {
                Object tmp = values.get(i) ;
                return values.get(i) ;
            }
        }

        if ( parent == null )
            return null ;

        // Tail recursion
        return parent.lookup(varName,0) ;
    }


    public Value getValue(String varName)
    {
        Object tmp = get(varName) ;
        return convert(tmp) ;
    }
    
    static private Value convert(Object arg)
    {
        if ( arg == null ) return null ;

        if ( arg instanceof Value ) return (Value)arg ;

        // Try to turn an RDFNode into a Value.
        // RDFNodes are RDF Literals, Resources, Properties or a container.
        // But containers are Resources.

        // Properties and Resources
        if ( arg instanceof Resource )
        {
            String uri = ((Resource)arg).getURI() ;
            WorkingVar w = new WorkingVar() ;
            w.setRDFResource((Resource)arg) ;
            return w ;
        }

        if ( arg instanceof Literal )
        {
            WorkingVar w = new WorkingVar() ;
            w.setRDFLiteral((Literal)arg) ;
            return w ;
        }

        // Opps!
        throw new RDQL_InternalErrorException("ResultBinding: unexpected object class: "+arg.getClass().getName()) ;
        //return null ;
    }

    /** Set the parent ResultBinding.  This is only needed for testing of parts of the query engine */
    
    void setParent(ResultBinding p)
    {
        //QSys.assertTrue( parent == null, "Attempt to change parent", "ResultBinding", "setParent") ;
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
            //QSys.assertTrue(tmp == null , "Duplicate binding: "+this, "ResultBinding", "check") ;
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
    
    /** Iterates over the variable names.
     *  Has extra operations to access the current variable value.
     */
    
    public static class ResultBindingIterator implements Iterator
    {
        boolean active  ;
        boolean doneThisOne  ;
        ResultBinding binding ;
        int i ;
        
        ResultBindingIterator(ResultBinding _binding)
        {
            binding = _binding ;
            // -1 indicates not started
            i = -1 ;
            // Set true to make it move on on first use.
            doneThisOne = true ;
            active = true ;
        }
        
        public boolean hasNext()
        {
            if ( doneThisOne )
                advance() ;
            return active ;
        }
        
        public Object next()
        {
            advance() ;
            doneThisOne = true ;
            return binding.varNames.get(i) ;
        }
        
        public String varName()
        {
            if ( ! active )
                return null ;
            return (String)binding.varNames.get(i)  ;
        }

        public Value value()
        {
            if ( ! active )
                return null ;
            return convert(binding.values.get(i))  ;
        }
        
        public void remove() { throw new java.lang.UnsupportedOperationException("ResultsBindingIterator.remove") ; }
        
        private boolean advance()
        {
            if ( ! active )
                return false ;
            // Have not yielded the current value (no next called).
            if ( ! doneThisOne )
                return true ;
            i++ ;
            if ( i >= binding.varNames.size() )
            {
                i = -1 ;
                binding = binding.parent ;
                doneThisOne = true ;
                if ( binding == null )
                {
                    active = false ;
                    return active ;
                }
                // Moved to parent but it may itself be empty.
                return advance() ;
            }
            doneThisOne = false ;
            //active = true ;
            return active ;
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
