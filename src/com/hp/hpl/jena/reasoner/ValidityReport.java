/******************************************************************
 * File:        ValidityReport.java
 * Created by:  Dave Reynolds
 * Created on:  09-Feb-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: ValidityReport.java,v 1.5 2003-05-08 15:08:16 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import java.util.Iterator;

/**
 * Data structure used to report the results of validation
 * or consistency checking operations. It is an array of reports,
 * each of which has a severity, a type (string) and a description (string).
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.5 $ on $Date: 2003-05-08 15:08:16 $
 */
public interface ValidityReport {
    
    /**
     * Return true if there are no problems reported by the validation. There may
     * still be warnings genererate.
     */
    public boolean isValid();
    
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
            this.isError = error;
            this.type = type;
            this.description = description;
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
    (c) Copyright Hewlett-Packard Company 2003
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