/*
 *  (c) Copyright 2001, 2003 Hewlett-Packard Development Company, LP
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
 
 * * $Id: AResource.java,v 1.3 2003-12-05 17:46:34 jeremy_carroll Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * AResource.java
 *
 * Created on June 26, 2001, 9:26 AM
 */

package com.hp.hpl.jena.rdf.arp;

/** A resource from the input file.
 *
 * @author jjc
 */
public interface AResource {
    /*
     *  @return true if this resource is anonymous
     */
/** Was this resource not given a URI in the file.
 * @return True if this resource is anonymous.
 */    
    public boolean isAnonymous();
    /* Undefined results (including an exception) if not isAnonymous().
     * @return An identifier with file scope for this anonymous resource.
     */
/** A string distinguishing this anonymous resource, from other anonymous resources.
 * Not defined if <CODE>isAnonymous()</CODE> returns false.
 * @return A gensym String starting "ARP:" or the value of rdf:nodeID.
 */    
    public String getAnonymousID();
    /*  Undefined results (including an exception) if isAnonymous().
     *  @ return The URI of this non-anonymous resource.
     */
/** If the input file specifies a URI reference for this resource, return it.
 * Not defined if <CODE>isAnonymous()</CODE> returns true.
 * @return The URI reference of this resource.
 */    
    public String getURI();
    
/** The user data allows the RDF application to store one Object with each resource during parsing.
 * This may help with garbage collect strategies when parsing huge files.
 * @return A user data object previously stored with setUserData; or null if none.
 */    
    public Object getUserData();
    
/** The user data allows the RDF application to store one Object with each resource during parsing.
 * This may help with garbage collect strategies when parsing huge files.
 * @param d A user data object which may be retrieved later with getUserData.
 */    
     public void setUserData(Object d);
     /**
      * 
      * @return True, if this is an anonymous resource with an explicit rdf:nodeID
      */
     public boolean hasNodeID();

}

