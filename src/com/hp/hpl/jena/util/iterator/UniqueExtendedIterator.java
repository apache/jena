/******************************************************************
 * File:        UniqueExtendedIterator.java
 * Created by:  Dave Reynolds
 * Created on:  28-Jan-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: UniqueExtendedIterator.java,v 1.6 2003-08-04 13:59:56 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.util.iterator;

import java.util.*;

/**
 * A variant on the closable/extended iterator that filters out
 * duplicate values. There is one complication that the value
 * which filtering is done on might not be the actual value
 * to be returned by the iterator. 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2003-08-04 13:59:56 $
 */
public class UniqueExtendedIterator extends WrappedIterator {

    /** The set of objects already seen */
    protected HashSet seen = new HashSet();
    
    /** One level lookahead */
    protected Object next = null;
    
    /** constructor */
    public UniqueExtendedIterator(Iterator underlying) {
        super(underlying);
    }
    /**
        factory method for creating a wrapper around _it_. We reserve
        the right to deliver the argument if it's already an extended iterator.
    */
    public static WrappedIterator create( Iterator it )
        { return new UniqueExtendedIterator( it ); }
    
    /**
     * Fetch the next object to be returned, only if not already seen.
     * Subclasses which need to filter on different objects than the
     * return values should override this method.
     * @return the object to be returned or null if the object has been filtered.
     */
    protected Object nextIfNew() {
        Object value = super.next();
        return seen.add( value ) ? value : null;
    }
    
    /**
     * @see Iterator#hasNext()
     */
    public boolean hasNext() {
        while (next == null && super.hasNext()) next = nextIfNew();
        return next != null;
    }

    /**
     * @see Iterator#next()
     */
    public Object next() {
        if (hasNext() == false) noElements( "exhausted UniqueIterator" );
        Object result = next;
        next = null;
        return result;
    }


}
