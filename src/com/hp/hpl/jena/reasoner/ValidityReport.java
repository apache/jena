/******************************************************************
 * File:        ValidityReport.java
 * Created by:  Dave Reynolds
 * Created on:  09-Feb-03
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: ValidityReport.java,v 1.10 2005-02-21 12:16:17 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import java.util.Iterator;

/**
 * Data structure used to report the results of validation
 * or consistency checking operations. It is an array of reports,
 * each of which has a severity, a type (string) and a description (string).
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.10 $ on $Date: 2005-02-21 12:16:17 $
 */
public interface ValidityReport {
    
    /**
     * Returns true if no logical inconsistencies were detected (in which case
     * there will be at least one error Report included). Warnings may still
     * be present. As of Jena 2.2 we regard classes which can't be instantiated
     * as warnings rather than errors. 
     */
    public boolean isValid();
    
    /**
     * Returns true if the model is both valid (logically consistent) and no
     * warnings were generated. 
     */
    public boolean isClean();
    
    /**
     * Return an iterator over the separate ValidityReport.Report records.
     */
    public Iterator getReports();
    
    // Inner class defining the datastructure of a single error report
    static class Report {
        /** 
         * The type of the error discovered, the range of 
         * errors types is reasoner-dependent.
         */
        public String type;
        
        /**
         * True if the report is a error, false if it is just a warning.
         */
        public boolean isError;
        
        /**
         * A textual description of the error or warning.
         */
        public String description;
        
        /**
         * Some reasoner dependent data structure giving more information
         * on the problem.
         */
        public Object extension;
        
        /**
         * Constructor.
         * @param error true if the report is an error, false if it is just a warning
         * @param type a string giving a reasoner-dependent classification for the report
         * @param description a textual description of the problem
         */
        public Report(boolean error, String type, String description) {
            this( error, type, description, null );
        }
        
        /**
         * Constructor
         * @param error true if the report is an error, false if it is just a warning
         * @param type a string giving a reasoner-dependent classification for the report
         * @param description a textual description of the problem
         * @param extension a reasoner dependent data structure giving more information
         * on the problem.
         */
        public Report(boolean error, String type, String description, Object extension) {
            this.isError = error;
            this.type = type;
            this.description = description;
            this.extension = extension;
        }
        
        /**
         * @return a textual description of the problem
         */
        public String getDescription() {
            return description;
        }
        /**
         * @return a reasoner dependent data structure giving more information
         * on the problem.
         */
        public Object getExtension() {
            return extension;
        }
        /**
         * @return True if the report is a error, false if it is just a warning.
         */
        public boolean isError() {
            return isError;
        }
        /**
         * @return a string giving a reasoner-dependent classification for the report
         */
        public String getType() {
            return type;
        }
         /**
         * Printable form of the report
         */
        public String toString() {
            return (isError ? "Error (" : "Warning (") + type + "): " + description;
        }
    }
}

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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