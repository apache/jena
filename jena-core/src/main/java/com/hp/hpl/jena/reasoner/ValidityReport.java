/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.reasoner;

import java.util.Iterator;

/**
 * Data structure used to report the results of validation
 * or consistency checking operations. It is an array of reports,
 * each of which has a severity, a type (string) and a description (string).
 */
public interface ValidityReport {
    
    /**
     * Returns true if no logical inconsistencies were detected. If it is false
     * then ether will be at least one error Report included. If it is true
     * then warnings may still
     * be present. As of Jena 2.2 we regard classes which can't be instantiated
     * as warnings (of type 'Inconsistent class') rather than errors. 
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
    public Iterator<Report> getReports();
    
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
        @Override
        public String toString() {
            return (isError ? "Error (" : "Warning (") + type + "): " + description;
        }
    }
}
