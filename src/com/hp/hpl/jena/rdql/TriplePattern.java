/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

/** Class used by original, Jena1, external query engine
 * @author   Andy Seaborne
 * @version  $Id: TriplePattern.java,v 1.15 2003-08-26 20:23:15 andy_seaborne Exp $
 */

package com.hp.hpl.jena.rdql;

import java.util.* ;

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.mem.* ;
import com.hp.hpl.jena.shared.*;

/*public*/ class TriplePattern
{
    static final boolean DEBUG = false ;

    boolean initialized = false ;

    Slot subjectSlot ;
    Resource fixed_s ;

    Slot predicateSlot ;
    Property fixed_p ;

    Slot objectSlot ;
    RDFNode fixed_o ;

    QueryEngineExt queryEngine = null ;

    /**
     * Deprecated - but not mentioned here to avoid warnings elsewhere
     * and its not public anyway. 
     * @param s
     * @param p
     * @param o
     */

    public TriplePattern(Slot s, Slot p, Slot o)
    {
        subjectSlot = s ;
        fixed_s = null ;
        predicateSlot = p ;
        fixed_p = null ;
        objectSlot = o ;
        fixed_o = null ;
        initialized = false ;
    }


	/** Return true if there are no variables in this TripePattern */
	public boolean isGround()
	{
		return  (! subjectSlot.isVar()) && (! predicateSlot.isVar()) && (! objectSlot.isVar()) ;
	}
	
	// Hack - somewhere to base creating resources, predicate and statements if nowhere else.
	static Model groundingModel = new ModelMem() ;

	/** Get the statement for this TriplePattern
	 * 	@return A statement if this TriplePattern is grounded (no variables) else return null.
	 */

	public Statement asStatement(ResultBinding rb)
	{
		// NB
		// Variables can not be bound to values.
		// This is because variables are only ever bound to Jena objects
		// (Resource/Properties/Literals)
		// Values are just intermediate working items for RDQL
		// (e.g. constant in query or result of an expression)
		// which can not be assigned to a variable.

		// A bit of a hack really.
		init(groundingModel) ;

		Resource subject = fixed_s ;
		Property predicate = fixed_p ;
		RDFNode object = fixed_o ;

		try
		{
			if (subject == null && subjectSlot.isVar())
			{
				// Unbound slot - try to ground.
				Object tmp = rb.get(subjectSlot.getVarName());
				
				if ( tmp == null )
					// Can't ground
					return null ;
					
				if (tmp instanceof Resource)
					subject = (Resource) tmp;
				else if (tmp instanceof Value)
					throw new RDQL_InternalErrorException("TriplePattern.substitute: Value bound to variable") ;
				else
					throw new RDQL_InternalErrorException("TriplePattern.substitute: variable bound to: "+tmp.getClass().getName()) ;
			}

			if (predicate == null && predicateSlot.isVar())
			{
				Object tmp = rb.get(predicateSlot.getVarName());
				if ( tmp == null )
					return null ;

				if (tmp instanceof Property)
					predicate = (Property) tmp;
				else if ( tmp instanceof Resource )
					predicate = subject.getModel().createProperty(((Resource)tmp).getURI()) ;
				else if (tmp instanceof Value)
					// Should not happen
					throw new RDQL_InternalErrorException("TriplePattern.asStatment: value bound to variable") ;
				else
					throw new RDQL_InternalErrorException("TriplePattern.asStatment: variable bound to: "+tmp.getClass().getName()) ;
			}

			if (object == null && objectSlot.isVar())
			{
               	Object tmp = rb.get(objectSlot.getVarName()) ;
               	if ( tmp == null )
				   	return null ;

                if ( tmp instanceof RDFNode )
                	object = (RDFNode)tmp ;
                else if ( tmp instanceof Value )
					// Should not happen
					throw new RDQL_InternalErrorException("TriplePattern.asStatment: value bound to variable") ;
				else
					throw new RDQL_InternalErrorException("TriplePattern.asStatment: variable bound to: "+tmp.getClass().getName()) ;
			}
			return subject.getModel().createStatement(subject, predicate, object) ;
		} catch (JenaException rdfEx)
		{
			QSys.unhandledException(rdfEx, "TriplePattern", "asStatment") ;
		}
		return null ;
	}	

	// Will change in Jena2 when resources, properties and
	// statements are not tied to models.

    private void init(Model m)
    {
    	if ( initialized )
    		return ;
    	
    	// Cache non-variable values into the "fixed_*" slots
        try {
            // Slots are Vars, values, resources or properties.

            // -------- Subject

            if ( subjectSlot.isValue() )
            {
                if ( ! subjectSlot.getValue().isURI() )
                    throw new QueryException("TriplePattern: subject is not a URI") ;
                fixed_s = m.createResource(subjectSlot.getValue().getURI()) ;
            }

            if ( subjectSlot.isResource() )
                fixed_s = subjectSlot.getResource() ;

            if ( subjectSlot.isProperty() )
                // Properties are resources
                fixed_s = subjectSlot.getProperty() ;


            // -------- Property

            if ( predicateSlot.isValue() )
            {
                if ( ! predicateSlot.getValue().isURI() )
                    throw new QueryException("TriplePattern: property is not a URI") ;
                fixed_p = m.createProperty(predicateSlot.getValue().getURI()) ;
            }

            if ( predicateSlot.isProperty() )
                fixed_p = predicateSlot.getProperty() ;


            if ( predicateSlot.isResource() )
                fixed_p = resourceToProperty(m, predicateSlot.getResource()) ;

            // -------- Object

            if ( objectSlot.isValue() )
                fixed_o = valueToRDFNode(m, objectSlot.getValue()) ;

            else if ( objectSlot.isProperty() )
                fixed_o = objectSlot.getProperty() ;

            else if ( objectSlot.isResource() )
                fixed_o = objectSlot.getResource() ;

            else if ( objectSlot.isLiteral() )
                fixed_o = objectSlot.getLiteral() ;

            // --------

            initialized = true ;
        } catch (JenaException rdfEx) { QSys.unhandledException(rdfEx, "TriplePattern", "init") ; }
    }


    // Returns an Iterator of ResultBindings
    // This was written before the substitute operation and has not been
    // converted because it works as it is.

    public Iterator match(QueryEngineExt qe, Model m, ResultBinding env)
    {
        queryEngine = qe ;

        if ( ! initialized )
            init(m) ;

        // Also: cache selector creation or value creation by checking to see if
        // variable has been changed or not (based on current epoch?)

        try {
            // Do bindings

            Resource s = fixed_s ;
            if ( s == null && env != null && subjectSlot.isVar() )
            {
                Object tmp = env.get(subjectSlot.getVarName()) ;

                if ( tmp != null )
                {
                    if ( tmp instanceof Resource )
                        s = (Resource)tmp ;
                    else if ( tmp instanceof RDFNode )
                        // Not a resource (includes properties) so duff.
                        throw new EvalFailureException() ;
                    else if ( tmp instanceof Value )
                    {
                        Value v = (Value)tmp ;
                        if ( ! v.isURI() )
                            // Local throw - caught in this method
                            throw new EvalFailureException() ;
                        s = m.createResource(v.getURI()) ;
                    }
                    else
                    {
                        //throw new EvalFailureException() ;
                        throw new RDQL_InternalErrorException("TriplePattern.match: Subject: Unexpected class in ResultBinding: "+
                                                              tmp.getClass().getName()) ;
                    }
                }
            }

            Property p = fixed_p ;
            if ( p == null && env != null && predicateSlot.isVar() )
            {
                Object tmp = env.get(predicateSlot.getVarName()) ;

                if ( tmp != null )
                {
                    if ( tmp instanceof Property )
                        p = (Property)tmp ;
                    else if ( tmp instanceof Resource )
                    {
                        p = resourceToProperty(m, (Resource)tmp) ;
                        // Anon. properties are (currently) not allowed in RDF
                        // Also: may be a URI that can't be turned into a property.
                        if ( p == null )
                        {
                            Query.logger.warn("TriplePattern: Attempt to match an anonymous resource with a property") ;
                            return null ;
                        }
                    }
                    else if ( tmp instanceof RDFNode )
                        //throw new EvalFailureException() ;
                        throw new RDQL_InternalErrorException("TriplePattern.match: Property: Unexpected RDFNode in ResultBinding: "+
                                                              ((RDFNode)tmp).toString()+ " ("+tmp.getClass().getName()+")") ;
                    else if ( tmp instanceof Value )
                    {
                        Value v = (Value)tmp ;
                        if ( ! v.isURI() )
                            throw new EvalFailureException() ;
                        p = m.createProperty(v.getURI()) ;
                    }
                }
            }

            RDFNode o = fixed_o ;
            if ( o == null && env != null && objectSlot.isVar() )
            {
                Object tmp = env.get(objectSlot.getVarName()) ;
                if ( tmp != null )
                {
                    if ( tmp instanceof RDFNode )
                        o = (RDFNode)tmp ;
                    else if ( tmp instanceof Value )
                        o = valueToRDFNode(m, (Value)tmp) ;
                    else
                        throw new RDQL_InternalErrorException("TriplePattern.match: Object: Unexpected class in ResultBinding: "+
                                                              tmp.getClass().getName()) ;
                }
            }

            // Debugging
            if ( DEBUG )
            {
                System.err.println("Matching ... ");

                System.err.println("    Subject:  Slot:   "+subjectSlot) ;
                System.err.println("              Select: "+(s==null? subjectSlot.getVar().toString() : s.toString())) ;

                System.err.println("    Property: Slot:   "+predicateSlot) ;
                System.err.println("              Select: "+(p==null? predicateSlot.getVar().toString() : p.toString())) ;

                System.err.println("    RDFNode:  Slot:   "+objectSlot) ;
                System.err.print  ("              Select: ") ;
                if ( o == null )
                    System.err.println(objectSlot.getVar().toString()) ;
                else
                {
                                        if ( ! ( o instanceof Literal ) )
                    {
                        System.err.println(o.toString()) ;
                    }
                    else
                    {
                        String tmp = "\""+((Literal)o).toString()+"\"" ;
                        if ( ! ((Literal)o).getLanguage().equals(""))
                            tmp = tmp+"@"+((Literal)o).getLanguage() ;
                        String dt = ((Literal)o).getDatatypeURI() ;
                        if ( ((Literal)o).getDatatypeURI() != null )
                            tmp = tmp+"^^<"+((Literal)o).getDatatypeURI()+">" ;
                        System.err.println(tmp) ;
                    }                }
            }
            // ----

            return new BindingIterator(m, s, p, o, env) ;
        }
        catch (EvalFailureException evalEx) { return null ; }
        catch (JenaException rdfEx) { QSys.unhandledException(rdfEx, "TriplePattern", "match") ; }
        return null ;
    }

    class BindingIterator implements Iterator
    {
        StmtIterator sIter ;
        Resource s ;
        Property p ;
        RDFNode o ;
        ResultBinding currentBinding ;
        Object current ;
        boolean finished = false ;

        BindingIterator(Model m, Resource _s, Property _p, RDFNode _o, ResultBinding binding)
        {
            s = _s ;
            p = _p ;
            o = _o ;
            currentBinding = binding ;
            try {
                // Jena1 needed:
                //Selector selector = new SimpleSelector(s, p, o) ;
                //sIter = m.listStatements(selector) ;
                sIter = m.listStatements(s, p, o) ;
            } catch (JenaException rdfEx)
            {
                QSys.unhandledException(rdfEx, "TriplePattern.BindingIterator(JenaException)", "BindingIterator") ;
            }
            catch (Throwable exEx)
            {
                QSys.unhandledException(exEx, "TriplePattern.BindingIterator(Throwable)", "BindingIterator") ;
            }
        }

        public boolean hasNext()
        {
            if ( queryEngine.queryStop )
            {
                finished = true ;
                // This is synchronous with process() so adjusting throws statement iterator is safe.
                // We will never call process() again.
                if ( sIter != null )
                {
                    try {
                        sIter.close() ;
                    }
                    catch (JenaException rdfEx) { QSys.unhandledException(rdfEx, "BindingIterator", "hasNext") ; }

                    sIter = null ;
                    return false ;
                }
            }

            if ( finished )
                return false ;

            if ( current == null )
            {
                current = process() ;
                String tmp = (current!=null)?((ResultBinding)current).toString():"<<null>>" ;
                Query.logger.debug("("+Thread.currentThread().getName()+") BindingIterator.next: "+tmp) ;
            }

            return current != null ;

            //try {
            //    return sIter.hasNext() ;
            //} catch (JenaException rdfEx) { QSys.unhandledException(rdfEx, "TriplePattern.BindingIterator", "hasNext") ; }
        }

        public Object next()
        {
            if ( hasNext() )
            {
                String tmp = (current!=null)?((ResultBinding)current).toString():"<<null>>" ;
                Query.logger.debug("("+Thread.currentThread().getName()+") BindingIterator.next: "+tmp) ;
                Object x = current ;
                current = null ;
                return x ;
            }
            else
                return null ;
        }

        // Returns null only when finished: the while(true) loop consumes statements
        // until a match is found or there are no more statements

        private Object process()
        {
            try {
                // Loop is for the case where no binding is possible
                // i.e. two slots are the same variable name but the values are different.
                while(true)
                {
                    if ( ! sIter.hasNext() )
                    {
                        finished = true ;
                        sIter.close() ;
                        sIter = null ;
                        String tmp = Thread.currentThread().getName() ;
                        Query.logger.debug("("+tmp+") sIter close") ;
                        return null ;
                    }

                    Statement stmt = sIter.nextStatement() ;
                    if ( DEBUG )
                        System.err.println("Statement: "+stmt) ;

                    String sName = null ;
                    String pName = null ;
                    String oName = null ;
                    Resource subject = stmt.getSubject() ;
                    Property predicate = stmt.getPredicate() ;
                    RDFNode object = stmt.getObject() ;

                    // Create new bindings.
                    if ( s == null )
                    {
                        sName = subjectSlot.getVarName() ;
                    }

                    if ( p == null )
                    {
                        pName = predicateSlot.getVarName() ;
                        // This variable may have been bound by the subject binding.
                        if ( sName != null && sName.equals(pName) )
                        {
                            // Binding the same variable - better be the same value.
                            if ( ! predicate.equals(subject) )
                            {
                                // Different values - skip this entire binding.
                                Query.logger.debug("Triple binding clash: subject/predicate") ;
                                continue ;
                            }
                            // Same variable, already due to be bound
                            pName = null ;
                        }
                    }

                    if ( o == null )
                    {
                        oName = objectSlot.getVarName() ;

                        if ( sName != null && sName.equals(oName) )
                        {
                            if ( ! ( ( object instanceof Resource ) && ((Resource)object).equals(subject) ) )
                            {
                                Query.logger.debug("Triple binding clash: subject/object") ;
                                // Different types or same type, different values - skip entire binding
                                continue ;
                            }
                            // Only bind via subject
                            oName = null ;
                        }

                        if ( pName != null && pName.equals(oName) )
                        {
                            // Note: object is a Resource - not a property - here.
                            if ( ! ( ( object instanceof Resource ) && ((Resource)object).equals(predicate)) )
                            {
                                Query.logger.debug("Triple binding clash: predicate/object") ;
                                continue ;
                            }
                            oName = null ;
                        }

                        // Different variable from subject and predicate
                        // Or same variable, same value.
                    }

                    if ( DEBUG )
                    {
                        System.err.println("Binding:") ;
                        if ( sName == null && pName == null && oName == null )
                            System.err.println("    No bindings") ;

                        if ( sName != null )
                            System.err.println("    "+sName+" <- "+subject) ;
                        if ( pName != null )
                            System.err.println("    "+pName+" <- "+predicate) ;
                        if ( oName != null )
                            System.err.println("    "+oName+" <- "+object) ;
                    }


                    if ( sName == null && pName == null && oName == null )
                        return currentBinding ;

                    // If we get here, we can do the bindings.
                    ResultBinding binding = new ResultBinding(currentBinding) ;

                    if ( sName != null )
                        binding.add(sName, subject) ;

                    if ( pName != null )
                        binding.add(pName, predicate) ;

                    if ( oName != null )
                        binding.add(oName, object) ;

                    if ( binding.size() == 0 )
                        throw new RDQL_InternalErrorException("TriplePattern.BindingIterator: Environemnt is still empty") ;

                    binding.addTriple(stmt) ;

                    return binding ;
                }
            }
            catch (JenaException rdfEx) { QSys.unhandledException(rdfEx, "TriplePattern.BindingIterator(JenaException)", "next") ; }
            catch (Throwable t) { QSys.unhandledException(t, "TriplePattern.BindingIterator(Throwable)", "next") ; }
            return null ;
        }

        public void remove() { throw new UnsupportedOperationException("BindingIterator.remove") ; }
    }


    private RDFNode valueToRDFNode(Model m, Value v) 
    {
        if ( v.isRDFLiteral())
            return v.getRDFLiteral() ;
        if ( v.isRDFResource() )
            return v.getRDFResource() ;

        // Order matters here : string must be last
        if ( v.isURI() )
            return m.createResource(v.getURI()) ;
        else if ( v.isInt() )
            return m.createLiteral(v.getInt()) ;
        else if ( v.isDouble() )
            return m.createLiteral(v.getDouble()) ;
        else if ( v.isBoolean() )
            return m.createLiteral(v.getBoolean()) ;
        else if ( v.isString() )
            return m.createLiteral(v.getString()) ;
        else
            // No idea!
            throw new QueryException("TriplePattern.valueToRDFNode: Unsupported value type: "+v) ;
    }

    // Change this if anonymous resources as predicates are allowed.

    private Property resourceToProperty(Model m, Resource r) throws EvalFailureException
    {
        // If it is an anon resource then error (this should have been caught elsewhere).
        // If it is a URI that can't be turned in to a property (e.g. ends in /, has no slash or #) then return null
        // Else create a property according to the URI.
        try {
            String uri = /*predicateSlot*/r.getURI() ;
            if ( uri == null || uri.equals("") )
                return null ;
            return m.createProperty(uri) ;
        } catch (JenaException rdfEx)
        {
            // Can't create property
            return null ;
        }
    }

    public String toString()
    {
        StringBuffer sbuff = new StringBuffer() ;
        sbuff.append("(") ;
        sbuff.append(subjectSlot.toString()) ;
        sbuff.append(", ") ;
        sbuff.append(predicateSlot.toString()) ;
        sbuff.append(", ") ;
        sbuff.append(objectSlot.toString()) ;
        sbuff.append(")") ;
        return sbuff.toString() ;
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
