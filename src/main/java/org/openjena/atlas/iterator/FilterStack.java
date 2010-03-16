/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.iterator;

/**
 * Add a filter to a chain - the original filter is called after this new sub-filter.
 */
public abstract class FilterStack<T> implements  Filter<T>
{
    private final Filter<T> other ;
    private final boolean subFilterLast ;
    
    public FilterStack(Filter<T> other) { this(other, false) ; }
    
    public FilterStack(Filter<T> other, boolean callOldFilterFirst)
    {
        this.other = other ;
        this.subFilterLast = callOldFilterFirst ;
    }
   
    public final boolean accept(T item)
    {
        if ( subFilterLast )
            return acceptAdditionaOther(item) ;
        else
            return acceptOtherAdditional(item) ;
    }
    
    private boolean acceptAdditionaOther(T item)
    {
        if ( ! acceptAdditional(item) )
            return false ;
        
        if ( other != null && ! other.accept(item) )
            return false ;
        
        return true ;
    }

    private boolean acceptOtherAdditional(T item)
    {
        if ( other != null && ! other.accept(item) )
            return false ;
        return  ! acceptAdditional(item) ;
    }

    
    /** Additional filter condition to apply */
    public abstract boolean acceptAdditional(T item) ;
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
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