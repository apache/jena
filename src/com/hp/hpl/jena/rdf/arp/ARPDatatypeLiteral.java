package com.hp.hpl.jena.rdf.arp;

/**
 * @author Jeremy J. Carroll
 * 
 */
class ARPDatatypeLiteral implements ALiteral {

    final private String datatype;
    final private String lang;
    final private String lexForm;
    ARPDatatypeLiteral(ARPString lexf,URIReference dt){
       datatype = dt.getURI();
       lang = lexf.getLang();
       lexForm = lexf.toString();
    }
    /**
     * @see com.hp.hpl.jena.rdf.arp.ALiteral#isWellFormedXML()
     */
    public boolean isWellFormedXML() {
        return false; //datatype.equals(ARPString.RDFXMLLiteral);
    }
    /**
     * @see com.hp.hpl.jena.rdf.arp.ALiteral#getParseType()
     */
    public String getParseType() {
        return null;
    }
    public String toString() {
        return lexForm;
    }

    /**
     * @see com.hp.hpl.jena.rdf.arp.ALiteral#getDatatypeURI()
     */
    public String getDatatypeURI() {
        return datatype;
    }

    /**
     * @see com.hp.hpl.jena.rdf.arp.ALiteral#getLang()
     */
    public String getLang() {
        return lang;
    }

}
