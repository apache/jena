/******************************************************************
 * File:        UniqueExtendedIterator.java
 * Created by:  Dave Reynolds
 * Created on:  28-Jan-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: UniqueExtendedIterator.java,v 1.4 2003-04-15 21:31:59 jeremy_carroll Exp $
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
 * @version $Revision: 1.4 $ on $Date: 2003-04-15 21:31:59 $
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
     * Fetch the next object to be returned, only if not already seen.
     * Subclasses which need to filter on different objects than the
     * return values should override this method.
     * @return the object to be returned or null if the object has been filtered.
     */
    protected Object nextIfNew() {
        Object value = super.next();
        if (seen.add(value)) {
            return value;
        } else {
            return null;
        }
    }
    
    /**
     * @see Iterator#hasNext()
     */
    public boolean hasNext() {
        while (next == null && super.hasNext()) {
            next = nextIfNew();
        }
        return next != null;
    }

    /**
     * @see Iterator#next()
     */
    public Object next() {
        if (next == null) {
            if (!hasNext()) {
                throw new NoSuchElementException("Element no available");
            }
        }
        Object result = next;
        next = null;
        return result;
    }


}
