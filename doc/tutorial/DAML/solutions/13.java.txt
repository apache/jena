/*
 * DAMLExercise.java
 *
 * (c) Copyright Hewlett-Packard Company 2002
 * All rights reserved.
 *
 * See end-of-file for license terms.
 */


import com.hp.hpl.jena.daml.*;
import com.hp.hpl.jena.daml.common.DAMLModelImpl;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDF;

import java.io.*;

/**
 *
 * @author  Jeremy Carroll
 */
public class DAMLExercise {
    
    /* Refactor.
     *   Use an object, to create the model and ontology,
     *    use methods to add properties.
     */
    
    String url = "http://www.w3.org/2001/vcard-rdf/3.0";
    String namespace = url +"#";
    DAMLDatatype xsdString;
    DAMLDatatype xsdDate;
    DAMLDatatype xsdBase64Binary;
    DAMLModel model;
    DAMLClass vCard;
    DAMLClass binaryPropertyRange;
    DAMLClass rdfValueIsString;
    
    DAMLExercise() {
        model = new DAMLModelImpl();
        DAMLOntology onto = model.createDAMLOntology(null);
        
        onto.prop_comment().addValue("This is an ontology for vCards.");
        onto.prop_comment().addValue("It's not a good one.");
        
        onto.prop_versionInfo().addValue("0.01");
        
        xsdString = model.createDAMLDatatype("http://www.w3.org/2000/10/XMLSchema#string");
        xsdDate = model.createDAMLDatatype("http://www.w3.org/2000/10/XMLSchema#date");;
        xsdBase64Binary = model.createDAMLDatatype("http://www.w3.org/2000/10/XMLSchema#base64Binary");;
        
        vCard = model.createDAMLClass(namespace+"VCARD");
        
        rdfValueIsString = initRdfValueIsString();
        binaryPropertyRange = initBinaryPropertyRange();
    }
    
    DAMLDatatypeProperty addStringProperty(String name) {
        DAMLDatatypeProperty fn = model.createDAMLDatatypeProperty(namespace+name);
        fn.prop_range().add(xsdString);
        fn.prop_domain().add(vCard);
        return fn;
    }
    DAMLDatatypeProperty addDateProperty(String name) {
        DAMLDatatypeProperty fn = model.createDAMLDatatypeProperty(namespace+name);
        fn.prop_range().add(xsdDate);
        fn.prop_domain().add(vCard);
        return fn;
    }
    DAMLObjectProperty addObjectProperty(String name) {
        DAMLObjectProperty fn = model.createDAMLObjectProperty(namespace+name);
        fn.prop_domain().add(vCard);
        return fn;
    }
    DAMLObjectProperty addVCardProperty(String name) {
        DAMLObjectProperty fn = addObjectProperty(name);
        fn.prop_range().add(vCard);
        return fn;
    }
    
    DAMLClass initRdfValueIsString() {
        DAMLRestriction vVALUE = model.createDAMLRestriction(null);
        vVALUE.prop_onProperty().add(RDF.value);
        vVALUE.prop_toClass().add(xsdString);
        return vVALUE;
    }
    DAMLClass initBinaryPropertyRange() {
        DAMLClass rng = model.createDAMLClass(null);
        PropertyAccessor intersect = rng.prop_intersectionOf();
        DAMLRestriction vTYPE = model.createDAMLRestriction(null);
        DAMLRestriction vENCODING = model.createDAMLRestriction(null);
        DAMLRestriction vVALUE = model.createDAMLRestriction(null);
        intersect.add(vTYPE);
        intersect.add(vENCODING);
        intersect.add(vVALUE);
        
        vVALUE.prop_onProperty().add(RDF.value);
        vVALUE.prop_toClass().add(xsdBase64Binary);
        
        vTYPE.prop_onProperty().add(model.createDAMLProperty(namespace+"TYPE"));
        vTYPE.prop_toClass().add(xsdString);
        
         
        vENCODING.prop_onProperty().add(model.createDAMLProperty(namespace+"ENCODING"));
        vENCODING.prop_toClass().add(xsdString);
        
        return rng;
    }
    /*  A binary property is an object which may have a value
     *  encoded in "B" form which if you chase back through the
     *  specs is exactly xsd:base64Binary.
     *  Unfortunately, the VCard RDF spec makes a mess of this.
     *
     * The object has properties vCard:TYPE and vCard:ENCODING="b"
     * We will use rdf:value for pointing to the xsd:base64Binary data.
     *
     *
     */
    DAMLObjectProperty addBinaryProperty(String name) {
        DAMLObjectProperty fn = addObjectProperty(name);
        fn.prop_range().add(binaryPropertyRange);
        return fn;
    }
    
    
    
    
    void dump() throws RDFException {
        System.out.println("RDF/XML\n");
        model.write(new OutputStreamWriter(System.out),"RDF/XML-ABBREV",url);
        System.out.println("\nN-TRIPLE\n");
        model.write(new OutputStreamWriter(System.out),"N-TRIPLE");
    }
    
    
    DAMLProperty addTypedProperty(boolean objectValued,String propName,String subClasses[] ) {
        String typeName = propName+"TYPES";
        DAMLClass cTYPES = model.createDAMLClass(namespace+typeName);
        
        for (int i=0; i<subClasses.length; i++) {
            PropertyAccessor pa = 
            model.createDAMLClass(namespace+subClasses[i])
            .prop_subClassOf();
            pa.add(cTYPES);
        }
        
        DAMLProperty prop = model.createDAMLObjectProperty(namespace+propName);
        prop.prop_range().add(cTYPES);
        prop.prop_domain().add(vCard);
        if (!objectValued) {
            prop.prop_range().add(rdfValueIsString);
        }
        return prop;
    }
    DAMLObjectProperty addStructuredProperty(String propName, String childProps[][] ) {
        DAMLObjectProperty prop =addObjectProperty(propName);
        DAMLClass propClass = model.createDAMLClass(namespace+propName+"PROPERTIES");
        prop.prop_range().add(propClass);
        for (int i=0;i<childProps.length;i++) {
            DAMLDatatypeProperty childP = model.createDAMLDatatypeProperty(namespace+childProps[i][1]);
            childP.prop_label().addValue(childProps[i][0]);
            childP.prop_range().add(xsdString);
            childP.prop_domain().add(propClass);
            childP.setIsUnique(true);
        }
        return prop;
    }
    /**
     * @param args the command line is ignored.
     */
    public static void main(String args[]) throws RDFException {
        DAMLExercise eg = new DAMLExercise();
        eg.addStringProperty("FN").setIsUnique(true);
        eg.addStringProperty("NICKNAME");
        eg.addStringProperty("MAILER").setIsUnique(true);
        eg.addStringProperty("GEO").setIsUnique(true);
        eg.addStringProperty("TITLE").setIsUnique(true);
        eg.addStringProperty("ROLE").setIsUnique(true);
        eg.addStringProperty("CATEGORIES");
        eg.addStringProperty("NOTE").setIsUnique(true);
        eg.addStringProperty("PRODID").setIsUnique(true);
        eg.addStringProperty("SORT-STRING").setIsUnique(true);
        eg.addStringProperty("CLASS").setIsUnique(true);
        eg.addStringProperty("NAME").setIsUnique(true);
        
        eg.addDateProperty("BDAY").setIsUnique(true);
        eg.addDateProperty("REV").setIsUnique(true);
        
        eg.addObjectProperty("URL").setIsUnique(true);
        eg.addObjectProperty("UID").setIsUnique(true);
        eg.addObjectProperty("SOURCE").setIsUnique(true);
        
        eg.addVCardProperty("AGENT").setIsUnique(true);
        eg.addVCardProperty("GROUP");
        
        eg.addBinaryProperty("PHOTO").setIsUnique(true);
        eg.addBinaryProperty("LOGO").setIsUnique(true);
        eg.addBinaryProperty("SOUND").setIsUnique(true);
        eg.addBinaryProperty("KEY").setIsUnique(true);
        
        eg.addTypedProperty(false,"TEL",new String[]{
            "home", "msg", "work", "pref", "voice", "fax", "cell", "video",
                   "pager", "bbs", "modem", "car", "isdn", "pcs"});
        eg.addTypedProperty(true,"EMAIL",new String[]{
            "home", "work", "pref", "internet", "x400"
          });
        eg.addTypedProperty(true,"ADR",new String[]{
            "home", "work", "pref", "dom", "intl", "psotal", "parcel"
          }).setIsUnique(true);
        eg.addTypedProperty(false,"LABEL",new String[]{
            "home", "work", "pref", "dom", "intl", "psotal", "parcel"
          }).setIsUnique(true);
          
     
        eg.addStructuredProperty("N", new String[][]{
            { "Family Name",            "Family" },
            { "Given Name",             "Given" },
            { "Additional Names",       "Other" },
            { "Honorific Prefixes",     "Prefix" },
            { "Honorific Suffixes",     "Suffix" }
        }).setIsUnique(true);          
        eg.addStructuredProperty("ADR", new String[][]{
            { "Post Office Box",        "Pobox" },
            { "Extended Address",       "Extadd" },
            { "Street Address",         "Street" },
            { "Locality",               "Locality" },
            { "Region",                 "Region" },
            { "Postal Code",            "Pcode" },
            { "Country",                "Country" }
        });      
        eg.addStructuredProperty("ORG", new String[][]{
            { "Organisation Name",      "Orgname" },
            { "Organisation Unit",      "Orgunit" }
        }).setIsUnique(true);
        
        
    
        
        
        eg.dump();
        
        
        
        
    }
    
}


/*
 * (c) Copyright Hewlett-Packard Company 2002
 * All rights reserved.
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
 *****************************************************************************/
