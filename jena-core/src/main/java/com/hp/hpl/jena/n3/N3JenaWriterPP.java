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

package com.hp.hpl.jena.n3;

//import org.apache.commons.logging.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;
import com.hp.hpl.jena.util.iterator.*;

import java.util.* ;

/** An N3 pretty printer.
 *  Tries to make N3 data look readable - works better on regular data.
 */



public class N3JenaWriterPP extends N3JenaWriterCommon
    /*implements RDFWriter*/
{
	// This N3 writer proceeds in 2 stages.  First, it analysises the model to be
	// written to extract information that is going to be specially formatted
	// (RDF lists, small anon nodes) and to calculate the prefixes that will be used.

    final protected boolean doObjectListsAsLists = getBooleanValue("objectLists", true) ;
    
	// Data structures used in controlling the formatting

    protected Set<Resource> rdfLists      	= null ; 		// Heads of lists
    protected Set<Resource> rdfListsAll   	= null ;		// Any resources in a collection
    protected Set<Resource> rdfListsDone  	= null ;		// RDF lists written
    protected Set<RDFNode>  oneRefObjects 	= null ;		// Bnodes referred to once as an object - can inline
    protected Set<Resource> oneRefDone   	= null ;		// Things done - so we can check for missed items

    // Do we do nested (one reference) nodes?
    protected boolean allowDeep = true ;
    
    static final protected String objectListSep = " , " ;
    
    // ----------------------------------------------------
    // Prepatation stage

	@Override
    protected void prepare(Model model)
	{
		prepareLists(model) ;
		prepareOneRefBNodes(model) ;
	}

	// Find well-formed RDF lists - does not find empty lists (this is intentional)
	// Works by finding all tails, and work backwards to the head.
    // RDF lists may, or may not, have a type element.
    // Should do this during preparation, not as objects found during the write
    // phase.   

	protected void prepareLists(Model model)
	{
		Set<Resource> thisListAll = new HashSet<>();

		StmtIterator listTailsIter = model.listStatements(null, RDF.rest, RDF.nil);

		// For every tail of a list
		//tailLoop:
		for ( ; listTailsIter.hasNext() ; )
		{
			// The resource for the current element being considered.
			Resource listElement  = listTailsIter.nextStatement().getSubject() ;
            // The resource pointing to the link we have just looked at.
            Resource validListHead = null ;

			// Chase to head of list
			for ( ; ; )
			{
				boolean isOK = checkListElement(listElement) ;
				if ( ! isOK )
					break ;

				// At this point the element is exactly a collection element.
				if ( N3JenaWriter.DEBUG ) out.println("# RDF list all: "+formatResource(listElement)) ;
				validListHead = listElement ;
				thisListAll.add(listElement) ;

				// Find the previous node.
				StmtIterator sPrev = model.listStatements(null, RDF.rest, listElement) ;

				if ( ! sPrev.hasNext() )
					// No rdf:rest link
					break ;

				// Valid pretty-able list.  Might be longer.
				listElement = sPrev.nextStatement().getSubject() ;
				if ( sPrev.hasNext() )
				{
					if ( N3JenaWriter.DEBUG ) out.println("# RDF shared tail from "+formatResource(listElement)) ;
					break ;
				}
			}
			// At head of a pretty-able list - add its elements and its head.
			if ( N3JenaWriter.DEBUG ) out.println("# Collection list head: "+formatResource(validListHead)) ;
			rdfListsAll.addAll(thisListAll) ;
			if ( validListHead != null )
				rdfLists.add(validListHead) ;
		}
		listTailsIter.close() ;
	}

	// Validate one list element.
	protected boolean checkListElement(Resource listElement) 
	{
	    // Must be a blank node for abbreviated form.
        if ( ! listElement.isAnon() )
            return false ;

        if (!listElement.hasProperty(RDF.rest)
			|| !listElement.hasProperty(RDF.first))
		{
			if (N3JenaWriter.DEBUG)
				out.println(
					"# RDF list element does not have required properties: "
						+ formatResource(listElement));
			return false;
		}

        // Must be exactly two properties (the ones we just tested for)
        // or three including the RDF.type RDF.List statement.
        int numProp = countProperties(listElement);

        if ( numProp == 2)
            // Must have exactly the properties we just tested for.
            return true ;

        // rdf:type is not implicit.
//        if (numProp == 3)
//        {
//            if (listElement.hasProperty(RDF.type, RDF.List))
//                return true;
//            if (N3JenaWriter.DEBUG)
//                out.println(
//                    "# RDF list element: 3 properties but no rdf:type rdf:List"
//                        + formatResource(listElement));
//            return false;
//        }

        if (N3JenaWriter.DEBUG)
            out.println(
                "# RDF list element does not right number of properties: "
                    + formatResource(listElement));
        return false;
	}

	// Find bnodes that are objects of only one statement (and hence can be inlined)
	// which are not RDF lists.
    // Could do this testing at write time (unlike lists)

	protected void prepareOneRefBNodes(Model model) 
	{

		NodeIterator objIter = model.listObjects() ;
		for ( ; objIter.hasNext() ; )
		{
			RDFNode n = objIter.nextNode() ;
            
            if ( testOneRefBNode(n) )
                oneRefObjects.add(n) ; 
            objIter.close() ;

            // N3JenaWriter.DEBUG
            if ( N3JenaWriter.DEBUG )
            {
                out.println("# RDF Lists      = "+rdfLists.size()) ;
                out.println("# RDF ListsAll   = "+rdfListsAll.size()) ;
                out.println("# oneRefObjects  = "+oneRefObjects.size()) ;
            }
        }
    }
    
	protected boolean testOneRefBNode(RDFNode n)
    {
		if ( ! ( n instanceof Resource ) )
			return false ;

		Resource obj = (Resource)n ;

		if ( ! obj.isAnon() )
            return false ;

        // In a list - done as list, not as embedded bNode.
		if ( rdfListsAll.contains(obj) )
			// RDF list (head or element)
            return false ;

		StmtIterator pointsToIter = obj.getModel().listStatements(null, null, obj) ;
		if ( ! pointsToIter.hasNext() )
			// Corrupt graph!
			throw new JenaException("N3: found object with no arcs!") ;

		Statement s = pointsToIter.nextStatement() ;
               
		if ( pointsToIter.hasNext() )
            return false ;

    	if ( N3JenaWriter.DEBUG )
			out.println("# OneRef: "+formatResource(obj)) ;
		return true ; 
	}
  
    // ----------------------------------------------------
    // Output stage 
    
    // Property order is:
    // 1 - rdf:type (as "a")
    // 2 - other rdf: rdfs: namespace items (sorted)
    // 3 - all other properties, sorted by URI (not qname)  
    

    
    @Override
    protected ClosableIterator<Property> preparePropertiesForSubject(Resource r)
    {
        Set<Property> seen = new HashSet<>() ;
        boolean hasTypes = false ;
        SortedMap<String, Property> tmp1 = new TreeMap<>() ;
        SortedMap<String, Property> tmp2 = new TreeMap<>() ;
        
        StmtIterator sIter = r.listProperties();
        for ( ; sIter.hasNext() ; )
        {
            Property p = sIter.nextStatement().getPredicate() ;
            if ( seen.contains(p) )
                continue ;
            seen.add(p) ;
            
            if ( p.equals(RDF.type) )
            {
                hasTypes = true ;
                continue ;
            }
            
            if ( p.getURI().startsWith(RDF.getURI()) ||  
                 p.getURI().startsWith(RDFS.getURI()) )
            {
                tmp1.put(p.getURI(), p) ;
                continue ;
            }
            
            tmp2.put(p.getURI(), p) ;        
        }
        sIter.close() ;
        
        ExtendedIterator<Property> eIter = null ;
        
        if ( hasTypes )
            eIter = new SingletonIterator<>(RDF.type) ;

        ExtendedIterator<Property> eIter2 = WrappedIterator.create(tmp1.values().iterator()) ;
            
        eIter = (eIter == null) ? eIter2 : eIter.andThen(eIter2) ;
                    
        eIter2 = WrappedIterator.create(tmp2.values().iterator()) ;
        
        eIter = (eIter == null) ? eIter2 : eIter.andThen(eIter2) ;
        return eIter ;
    }
    
    @Override
    protected boolean skipThisSubject(Resource subj)
    {
        return rdfListsAll.contains(subj)   ||
               oneRefObjects.contains(subj)  ;
    }

//    protected void writeModel(Model model)
//	{
//        super.writeModel(model) ;
//
//

    // Before ... 

    @Override
    protected void startWriting()
    {
        allocateDatastructures() ;
    }

    // Flush any unwritten objects.
    // 1 - OneRef objects
    //     Normally there are "one ref" objects left
    //     However loops of "one ref" are possible.
    // 2 - Lists

    @Override
    protected void finishWriting()
    {
 
        // Are there any unattached RDF lists?
        // e..g ([] [] []) . in the data.
        // We missed these earlier.
        for ( Resource r : rdfLists )
        {
            if ( rdfListsDone.contains( r ) )
            {
                continue;
            }
            out.println();
            if ( N3JenaWriter.DEBUG )
            {
                out.println( "# RDF List" );
            }

            // Includes the case of shared lists-as-objects. 
//            if (!r.isAnon() || countArcsTo(r) > 0 )
//            {
//                // Name it.
//                out.print(formatResource(r));
//                out.print(" :- ");
//            }
//            writeList(r);
//            out.println(" .");
            // Turtle does not have :-
            writeListUnpretty( r );
        }

        // Finally, panic.
        // Dump anything that has not been output yet. 
        oneRefObjects.removeAll(oneRefDone);
        for ( RDFNode oneRefObject : oneRefObjects )
        {
            out.println();
            if ( N3JenaWriter.DEBUG )
            {
                out.println( "# One ref" );
            }
            // Don't allow further one ref objects to be inlined.
            allowDeep = false;
            writeOneGraphNode( (Resource) oneRefObject );
            allowDeep = true;
        }

        //out.println() ;
        //writeModelSimple(model,  bNodesMap, base) ;
        out.flush();
        clearDatastructures() ;
    }



	// Need to decide between one line or many.
    // Very hard to do a pretty thing here because the objects may be large or small or a mix.

    @Override
    protected void writeObjectList(Resource subject, Property property)
    {
//        if ( ! doObjectListsAsLists )
//        {
//            super.writeObjectList(resource, property) ;
//            return ;
//        }

        String propStr = formatProperty(property);
        
        // Find which objects are simple (i.e. not nested structures)             

        StmtIterator sIter = subject.listProperties(property);
        Set<RDFNode> simple = new HashSet<>() ;
        Set<RDFNode> complex = new HashSet<>() ;

        for (; sIter.hasNext();)
        {
            Statement stmt = sIter.nextStatement();
            RDFNode obj = stmt.getObject() ;
            if ( isSimpleObject(obj) )
                simple.add(obj) ;
            else
                complex.add(obj) ;
        }
        sIter.close() ;
        // DEBUG
        int simpleSize = simple.size() ;
        int complexSize = complex.size() ;
        
        // Write property/simple objects
        
        if ( simple.size() > 0 )
        {
            String padSp = null ;
            // Simple objects - allow property to be long and alignment to be lost
            if ((propStr.length()+minGap) <= widePropertyLen)
                padSp = pad(calcPropertyPadding(propStr)) ;
            
            if ( doObjectListsAsLists )
            {
                // Write all simple objects as one list. 
                out.print(propStr);
                out.incIndent(indentObject) ; 
            
                if ( padSp != null )
                    out.print(padSp) ;
                else
                    out.println() ;
            
                for (Iterator<RDFNode> iter = simple.iterator(); iter.hasNext();)
                {
                    RDFNode n = iter.next();
                    writeObject(n);
                    
                    // As an object list
                    if (iter.hasNext())
                        out.print(objectListSep);
                }
                
                out.decIndent(indentObject) ;
            }
            else
            {
                for (Iterator<RDFNode> iter = simple.iterator(); iter.hasNext();)
                {
                    // This is also the same as the complex case 
                    // except the width the property can go in is different.
                    out.print(propStr);
                    out.incIndent(indentObject) ; 
                    if ( padSp != null )
                        out.print(padSp) ;
                    else
                        out.println() ;
                    
                    RDFNode n = iter.next();
                    writeObject(n);
                    out.decIndent(indentObject) ;
                    
                    // As an object list
                    if (iter.hasNext())
                        out.println(" ;");
                   }
                
            }
        }        
        // Now do complex objects.
        // Write property each time for a complex object.
        // Do not allow over long properties but same line objects.

        if (complex.size() > 0)
        {
            // Finish the simple list if there was one
            if ( simple.size() > 0 )
                out.println(" ;");
            
            int padding = -1 ;
            String padSp = null ;
            
            // Can we fit teh start of teh complex object on this line?
            
            // DEBUG variable.
            int tmp = propStr.length() ;
            // Complex objects - do not allow property to be long and alignment to be lost
            if ((propStr.length()+minGap) <= propertyCol)
            {
                padding = calcPropertyPadding(propStr) ;
                padSp = pad(padding) ;
            }

            for (Iterator<RDFNode> iter = complex.iterator(); iter.hasNext();)
            {
                int thisIndent = indentObject ;
                //if ( i )
                out.incIndent(thisIndent);
                out.print(propStr);
                if ( padSp != null )
                    out.print(padSp) ;
                else
                    out.println() ;
            
                RDFNode n = iter.next();
                writeObject(n);
                out.decIndent(thisIndent);
                if ( iter.hasNext() )
                    out.println(" ;");
            }
        }
        return;
	}


    protected boolean isSimpleObject(RDFNode node)
    {
        if (node instanceof Literal)
            return true ;
        Resource rObj = (Resource) node;
        if ( allowDeep && oneRefObjects.contains(rObj) )
            return false ;
        return true ;
    }

    @Override
    protected void writeObject(RDFNode node)
	{
		if (node instanceof Literal)
		{
			writeLiteral((Literal) node);
			return;
		}

		Resource rObj = (Resource) node;
		if ( allowDeep && ! isSimpleObject(rObj))
		{
			oneRefDone.add(rObj);
	        ClosableIterator<Property> iter = preparePropertiesForSubject(rObj);
	        if (! iter.hasNext() )
	        {
	            // No properties.
	            out.print("[]");
	        }
	        else
	        {
    			out.print("[ ");
    			out.incIndent(2);
    			writePropertiesForSubject(rObj, iter);
                out.decIndent(2);
                out.println() ;
                // Line up []
    			out.print("]");
	        }
	        iter.close();
			return ;
		}

		if (rdfLists.contains(rObj))
			if (countArcsTo(rObj) <= 1)
			{
				writeList(rObj);
				return;
			}

		out.print(formatResource(rObj));
	}



	// Need to out.print in short (all on one line) and long forms (multiple lines)
    protected void writeList(Resource resource)
	{
		out.print( "(");
		out.incIndent(2) ;
		boolean listFirst = true;
		for (Iterator<RDFNode> iter = rdfListIterator(resource); iter.hasNext();)
		{
			if (!listFirst)
				out.print( " ");
			listFirst = false;
			RDFNode n = iter.next();
			writeObject(n) ;
		}
		out.print( ")");
		out.decIndent(2) ;
		rdfListsDone.add(resource);

	}
    
    // Need to out.print in long (triples) form.
    protected void writeListUnpretty(Resource r)
    {
        //for ( ; ! r.equals(RDF.nil); )
        {
            // Write statements at this node.
            StmtIterator sIter = r.getModel().listStatements(r, null, (RDFNode)null) ;
            for ( ; sIter.hasNext() ; )
            {
                Statement s = sIter.next() ;
                writeStatement(s) ;
            }

            // Look for rdf:rest.
            sIter = r.getModel().listStatements(r, RDF.rest, (RDFNode)null) ;
            for ( ; sIter.hasNext() ; )
            {
                Statement s = sIter.next() ;
                RDFNode nextNode = s.getObject() ;
                if ( nextNode instanceof Resource )
                {
                    Resource r2 = (Resource)nextNode ;
                    writeListUnpretty(r2) ;
                }
                else
                    writeStatement(s) ;
            }
        }
    }

    private void writeStatement(Statement s)
    {
        out.print(formatResource(s.getSubject()));
        out.print(" ") ;

        out.print(formatResource(s.getPredicate())) ;
        out.print(" ") ;
        
        out.print(formatNode(s.getObject())) ;
        out.println(" .") ;
        
    }

    // Called before each writing run.
	protected void allocateDatastructures()
	{
		rdfLists 		= new HashSet<>() ;
		rdfListsAll 	= new HashSet<>() ;
		rdfListsDone 	= new HashSet<>() ;
		oneRefObjects 	= new HashSet<>() ;
		oneRefDone 		= new HashSet<>() ;
	}

	// Especially release large intermediate memory objects
	protected void clearDatastructures()
	{
		rdfLists 		= null ;
		rdfListsAll 	= null ;
		rdfListsDone 	= null ;
		oneRefObjects 	= null ;
		oneRefDone 		= null ;
	}
}
