/*
 *  (c) Copyright Hewlett-Packard Company 2000 
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
 * RSS.java
 *
 * Created on 31 August 2000, 15:41
 */

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.impl.ErrorHelper;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFException;

/**
 *
 * @author  bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1.1.1 $' Date='$Date: 2002-12-19 19:21:44 $'
 */
public class RSS extends Object {
    
    // next free error code = 2
    
    protected static final String uri = "http://purl.org/rss/1.0/";
    
/** returns the URI for this schema
 * @return the URI for this schema
 */    
    public static String getURI()
    {
        return uri;
    }    
    
           static final String   nchannel = "channel";
    public static       Resource channel = null;       
           static final String   nitem = "item";
    public static       Resource item = null;
          
           static final String   ndescription = "description";
    public static       Property description = null;     
           static final String   nimage = "image";
    public static       Property image = null;          
           static final String   nitems = "items";
    public static       Property items = null;     
           static final String   nlink = "link";
    public static       Property link = null;        
           static final String   nname = "name";     
    public static       Property name = null;
           static final String   ntextinput = "textinput";
    public static       Property textinput = null;    
           static final String   ntitle = "title";
    public static       Property title = null;   
           static final String   nurl = "url";
    public static       Property url = null;
    
        
    static {
        try {
            channel     = new ResourceImpl(uri+nchannel);
            item        = new ResourceImpl(uri+nitem);
            
            description = new PropertyImpl(uri, ndescription);
            image       = new PropertyImpl(uri, nimage);       
            items       = new PropertyImpl(uri, nitems);
            link        = new PropertyImpl(uri, nlink);
            name        = new PropertyImpl(uri, nname);
            textinput   = new PropertyImpl(uri, ntextinput);
            title       = new PropertyImpl(uri, ntitle);
            url         = new PropertyImpl(uri, nurl);
        } catch (RDFException e) {
            ErrorHelper.logInternalError("RSS", 1, e);
        }
    }

}