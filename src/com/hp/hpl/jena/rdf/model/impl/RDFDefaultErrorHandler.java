/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: RDFDefaultErrorHandler.java,v 1.7 2003-12-08 10:48:25 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.arp.ParseException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * The default error handler for I/O.
 * This uses log4j as its utility.
 * @author  jjc,bwm
 * @version $Revision: 1.7 $ $Date: 2003-12-08 10:48:25 $
 */
public class RDFDefaultErrorHandler extends Object implements RDFErrorHandler {

    public static final Log logger = LogFactory.getLog( RDFDefaultErrorHandler.class );
    
    /** Creates new RDFDefaultErrorHandler */
    public RDFDefaultErrorHandler() {
    }

    public void warning(Exception e) {
        logger.warn(ParseException.formatMessage(e));
    }

    public void error(Exception e) {
        logger.error(ParseException.formatMessage(e));
    }

    public void fatalError(Exception e) {
        logger.error(ParseException.formatMessage(e));
        throw e instanceof RuntimeException 
            ? (RuntimeException) e
            : new JenaException( e );
    }
}
/*
 *  (c) Copyright 2001 Hewlett-Packard Development Company, LP
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
 * $Id: RDFDefaultErrorHandler.java,v 1.7 2003-12-08 10:48:25 andy_seaborne Exp $
 */