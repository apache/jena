/*
 *  (c) Copyright Hewlett-Packard Company 1999-2001 
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
 * $Id: ArrayMap.java,v 1.1.1.1 2002-12-19 19:21:10 bwm Exp $
 *
 */
/*
 * ArrayMap.java
 *
 * Created on June 14, 2001, 9:56 AM
 */

package com.hp.hpl.jena.util.iterator;

import java.util.*;
/** Creates a Map from an array of pairs.
 * @author jjc
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1.1.1 $' Date='$Date: 2002-12-19 19:21:10 $'
 */
public class ArrayMap extends java.util.AbstractMap {
    private Set entrySet;
    /** Creates new ArrayMap
     * @param pairs The key-value pairs in the Map.
 */
    public ArrayMap(Object pairs[][]) {
        entrySet = new HashSet();
        for (int i=0;i<pairs.length;i++) {
            if (pairs[i].length != 2)
                throw new IllegalArgumentException("Not a list of pairs.");
            if (containsKey(pairs[i][0]))
                throw new IllegalArgumentException("Duplicate key: " + pairs[i][0]+".");
            entrySet.add(new PairEntry(pairs[i][0],pairs[i][1]));
            
        }
    }

    public Set entrySet() {
        return this.entrySet;
    }
    
}
