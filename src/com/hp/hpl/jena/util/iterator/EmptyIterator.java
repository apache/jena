/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web site           http://jena.sourceforge.net
 * Created            29-Oct-2004
 * Filename           $RCSfile: EmptyIterator.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-10-31 11:52:17 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, Hewlett-Packard Development Company, LP
 * [See end of file for full copyright notice]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.util.iterator;


// Imports
///////////////


/**
 * <p>
 * The empty iterator, which always returns false to {@link #hasNext}.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: EmptyIterator.java,v 1.1 2004-10-31 11:52:17 ian_dickinson Exp $)
 */
public class EmptyIterator 
    extends NiceIterator
{
    // Constants
    ////////////
    
    /** Shareable singleton instance of the emtpy iterator */
    public static final EmptyIterator INSTANCE = new EmptyIterator();
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer true if there are more elements in the iterator, 
     * which is permanently false for the EmptyIterator.</p>
     * @return False
     */
    public boolean hasNext() {
        return false;
    }
    
    /**
     * <p>Get the next element in the iterator, which will always
     * fail with an exception in the empty iterator.</p>
     * @return Does not return
     * @exception java.util.NoSuchElementException always.
     */
    public Object next() {
        return noElements( "this iterator is empty by construction" );
    }
}

/*
(c) Copyright 2004 Hewlett-Packard Development Company, LP
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
