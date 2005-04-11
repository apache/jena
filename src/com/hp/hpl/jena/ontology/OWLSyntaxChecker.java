/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology;

import java.util.List;

import com.hp.hpl.jena.ontology.impl.OWLLiteProfile;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * This interface is currently part of SPI, not the API.
 * This means that the methods in it may be changed
 * as part of the Jena development process without
 * deprecation etc. People other than the Jena development
 * team are discouraged from making direct use of this interface.
 * Comments on this interface and how we should progress it,
 * and generalize it are welcome on jena-devel@lists.sourceforge.net
 * 
 * The API to access this functionality is {@link OntModel#getOWLLanguageLevel(List)},
 * and requires owlsyntax.jar (separately downloadable from the Jena
 * sourceforge site) to be on the classpath.
 *  
 * @author Jeremy J. Carroll
 *
 */
public interface OWLSyntaxChecker {
    /**
     * <p>Given an OWL ontology model owlModel, 
     * answer the minimum OWL language 
     * level that the constructs
     * used in this model lie entirely within.  The three possible return values are 
     * {@link OWL#FULL_LANG} for OWL-full, 
     * {@link OWL#DL_LANG} for OWL-DL or
     * {@link OWL#LITE_LANG} for OWL-lite.
     * Note that these URI's are <strong>not</strong> officially sanctioned by the WebOnt 
     * working group.  For unknown reasons, the working group chose not to assign official
     * URI's to represent the different OWL language levels. There is a slim chance that this
     * may change in future, in which case these return values will change apropriately.
     * In addition, the given <code>problems</problems> list, if non-null, will be filled with the syntax
     * problems detected by the syntax checker.
     * </p>
     * <p>
     * The Jena OWL syntax checker will normally list as problems those constructs used in
     * this model that are in OWL Full but not permitted in OWL DL.  The exception to this
     * is if the {@linkplain #getProfile() language profile} for this model is  
     * {@linkplain OWLLiteProfile OWL Lite}, then the syntax checker will
     * test for constructs that lie in OWL-DL or OWL-Full and hence outside in OWL-Lite.
     * </p>
     * 
     * @param owlModel An OntModel that must be an OWL ontology.
     * @param problems A list that, if non-null, will have the various problems discovered by the OWL syntax
     * checker added to it.
     * @return A resource denoting the minimum OWL language level for owlModel
     * @exception OntologyException if owlModel is not an OWL model
     */
    public Resource getOWLLanguageLevel( OntModel owlModel, List problems )
      throws OntologyException;
}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 *
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
 */
 
