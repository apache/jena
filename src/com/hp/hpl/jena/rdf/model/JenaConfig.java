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
 * $Id: JenaConfig.java,v 1.6 2003-12-08 10:48:25 andy_seaborne Exp $
 *
 * Created on 27 June 2002, 08:49
 */

package com.hp.hpl.jena.rdf.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.*;

/** A Class for configuring Jena's behaviour.
 *
 * <p>It is sometimes necessary to configure Jena's behaviour.  For example
 * when external functionality has changed, it may, for a time be desirable
 * to be able to configure Jena to continue the old behaviour so that existing
 * code which relied on the old behaviour does not break.<p>
 *
 * <p> Configuration options can sometimes be set using system properties.  The
 * following system properties are defined:</p>
 * <ul>
 *  <li><code>com.hp.hpl.jena.oldLiteralCompare</code>: This can be set
       to "true" before running Jena to turn on old literal compare behaviour.
       See <code>setOldLiteralCompare</code> below.</li>
 * </ul>
 * @author bwm
 * @version $Revision: 1.6 $
 *
 */
public class JenaConfig {

    /** Creates new JenaConfig */
    private JenaConfig() {
    }
    
    protected static Log logger = LogFactory.getLog( JenaConfig.class );
    
    private static boolean oldLiteralCompare;
    
    /** Configure Jena to use its previous algorithm for comparing Literals.
     *
     * <p>The RDFCore WG recently decided that two literals were not equal
     * if they differed only in the setting of their isWellFormedXML
     * flag.  This is different from Jena's original behaviour.</p>
     *
     * <p>Jena literals have been modified to support the behaviour
     * defined by RDFCore.  By calling this method with true as
     * a parameter, Jena can be configured to use its old behaviour.</p>
     *
     * @param b The value to set the oldLiteralCompare flag
     * @return the previous value of the oldLiteralCompare flag
     * @deprecated this functionality is temporary
     */    
    public static boolean setOldLiteralCompare(boolean b) {
        boolean previous = oldLiteralCompare;
        oldLiteralCompare = b;
        return previous;
    }
    
    /** Return the value of the oldLiteralCompare flag
     * @return the value of the oldLiteralCompare flag
     * @deprecated this functionality is temporary
     */    
    public static boolean getOldLiteralCompare() {
        return oldLiteralCompare;
    }
    
    static {
        try {
            String str = 
                  System.getProperty(Jena.PATH + ".oldLiteralCompare", "false");
            oldLiteralCompare = 
                        (str.equalsIgnoreCase("true") || str.equals("1"));
        } catch (SecurityException se) {
            // could get this in an applet for example
            // so ignore
            oldLiteralCompare = false;
        } catch (Exception e) {
            // other exceptions are unexpected
            // log and ignore
            logger.warn("Unexpected Exception: JenaConfig.<Static Init>", e);
            oldLiteralCompare = false;
        }
    }
}
