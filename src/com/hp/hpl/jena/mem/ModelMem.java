
/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001 
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
 * ModelMem.java
 *
 * Created on 03 August 2000, 15:02
 */

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
/*
import java.io.*;
import java.util.*;
import com.hp.hpl.jena.vocabulary.RDF;
*/

/** A main memory implemention of the RDF API.
 *
 * <p>This implementation of the RDF API stores its all its data in main memory.
 *   It supports all methods except the transaction calls.</p>
 * <p>This is a prototype implementation, and should be considered to be alpha
 *   quality code.  It has not yet been performance optimised and is known to be
 *   wasteful of memory.  It has been tested by loading the wordnet database
 *   (see Sample3) which results in a model containing over 600,000 statements.
 *   </p>
 * @author bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1.1.1 $' Date='$Date: 2002-12-19 19:14:16 $'
 */
public class ModelMem extends ModelCom implements Model, ModelI {    
    
    // next free error code = 3

    /** Create an empty model.
     */
    public ModelMem() {
    	this(BuiltinPersonalities.model);
    }
    public ModelMem(GraphPersonality p) {
    	this( p, new GraphMem() ) ;
    }

	public ModelMem( Graph g ) {
		this(BuiltinPersonalities.model, g );
	}
	
    public ModelMem( GraphPersonality p, Graph g ) {
    	super( g, p );
    }
    

}