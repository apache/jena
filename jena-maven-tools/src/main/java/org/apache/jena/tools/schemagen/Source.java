/**
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

package org.apache.jena.tools.schemagen;

import jena.schemagen.SchemagenOptions.OPT;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * <p>Simple container object to hold the per-source configuration
 * values from the <code>pom.xml</code>.</p> Source objects are used
 * configure SchemagenOptions object during plugin execution.
 *
 * Use Parameter annotations on fields to designate Maven properties and
 * SchemagenOption annotations on getters to designate SchemagenOptions
 * destination options.
 */
public class Source {
    /** Name of default options element */
    public static final String DEFAULT_OPTIONS_ELEM = "default";

    @Parameter(property="config-file")
    private String configFile;

    @Parameter(property="no-comments")
    private Boolean noComments;

    @Parameter
    private String input;

    @Parameter(property="lang-daml")
    private Boolean langDaml;

    @Parameter(property="lang-owl")
    private Boolean langOwl;

    @Parameter(property="lang-rdfs")
    private Boolean langRdfs;

    @Parameter
    private String output;

    @Parameter
    private String header;

    @Parameter
    private String footer;

    @Parameter
    private String root;

    @Parameter
    private String marker;

    @Parameter(property="package-name")
    private String packageName;

    @Parameter
    private Boolean ontology;

    @Parameter(property="classname")
    private String className;

    @Parameter(property="classdec")
    private String classDec;

    @Parameter
    private String namespace;

    @Parameter
    private String declarations;

    @Parameter(property="property-section")
    private String propertySection;

    @Parameter(property="class-section")
    private String classSection;

    @Parameter(property="individuals-section")
    private String individualsSection;
    
    @Parameter(property="datatypes-section")
    private String datatypesSection;

    @Parameter(property="noproperties")
    private Boolean noProperties;

    @Parameter(property="noclasses")
    private Boolean noClasses;

    @Parameter(property="noindividuals")
    private Boolean noIndividuals;
    
    @Parameter(property="nodatatypes")
    private Boolean noDatatypes;

    @Parameter(property="noheader")
    private Boolean noHeader;

    @Parameter(property="prop-template")
    private String propTemplate;

    @Parameter(property="classtemplate")
    private String classTemplate;

    @Parameter(property="individualtemplate")
    private String individualTemplate;
    
    @Parameter(property="datatypetemplate")
    private String datatypeTemplate;

    @Parameter(property="uc-names")
    private Boolean ucNames;

    @Parameter
    private String include;

    @Parameter(property="classname-suffix")
    private String classNameSuffix;

    @Parameter
    private String encoding;

    @Parameter
    private Boolean help;

    @Parameter
    private Boolean dos;

    @Parameter(property="use-inf")
    private Boolean useInf;

    @Parameter(property="strict-individuals")
    private Boolean strictIndividuals;

    @Parameter(property="include-source")
    private Boolean includeSource;

    @Parameter(property="no-strict")
    private Boolean noStrict;

    @SchemagenOption(opt=OPT.CONFIG_FILE)
    public String getConfigFile() {
        return configFile;
    }

    @SchemagenOption(opt=OPT.NO_COMMENTS)
    public Boolean isNoComments() {
        return noComments;
    }

    @SchemagenOption(opt=OPT.INPUT)
    public String getInput() {
        return input;
    }

    @SchemagenOption(opt=OPT.LANG_DAML)
    public Boolean isLangDaml() {
        return langDaml;
    }

    @SchemagenOption(opt=OPT.LANG_OWL)
    public Boolean isLangOwl() {
        return langOwl;
    }

    @SchemagenOption(opt=OPT.LANG_RDFS)
    public Boolean isLangRdfs() {
        return langRdfs;
    }

    @SchemagenOption(opt=OPT.OUTPUT)
    public String getOutput() {
        return output;
    }

    @SchemagenOption(opt=OPT.HEADER)
    public String getHeader() {
        return header;
    }

    @SchemagenOption(opt=OPT.FOOTER)
    public String getFooter() {
        return footer;
    }

    @SchemagenOption(opt=OPT.ROOT)
    public String getRoot() {
        return root;
    }

    @SchemagenOption(opt=OPT.MARKER)
    public String getMarker() {
        return marker;
    }

    @SchemagenOption(opt=OPT.PACKAGENAME)
    public String getPackageName() {
        return packageName;
    }

    @SchemagenOption(opt=OPT.ONTOLOGY)
    public Boolean isOntology() {
        return ontology;
    }

    @SchemagenOption(opt=OPT.CLASSNAME)
    public String getClassName() {
        return className;
    }

    @SchemagenOption(opt=OPT.CLASSDEC)
    public String getClassDec() {
        return classDec;
    }

    @SchemagenOption(opt=OPT.NAMESPACE)
    public String getNamespace() {
        return namespace;
    }

    @SchemagenOption(opt=OPT.DECLARATIONS)
    public String getDeclarations() {
        return declarations;
    }

    @SchemagenOption(opt=OPT.PROPERTY_SECTION)
    public String getPropertySection() {
        return propertySection;
    }

    @SchemagenOption(opt=OPT.CLASS_SECTION)
    public String getClassSection() {
        return classSection;
    }

    @SchemagenOption(opt=OPT.INDIVIDUALS_SECTION)
    public String getIndividualsSection() {
        return individualsSection;
    }
    
    @SchemagenOption(opt=OPT.DATATYPES_SECTION)
    public String getDatatypesSection() {
        return datatypesSection;
    }

    @SchemagenOption(opt=OPT.NOPROPERTIES)
    public Boolean isNoProperties() {
        return noProperties;
    }

    @SchemagenOption(opt=OPT.NOCLASSES)
    public Boolean isNoClasses() {
        return noClasses;
    }

    @SchemagenOption(opt=OPT.NOINDIVIDUALS)
    public Boolean isNoIndividuals() {
        return noIndividuals;
    }
    
    @SchemagenOption(opt=OPT.NODATATYPES)
    public Boolean isNoDatatypes() {
        return noDatatypes;
    }

    @SchemagenOption(opt=OPT.NOHEADER)
    public Boolean isNoHeader() {
        return noHeader;
    }

    @SchemagenOption(opt=OPT.PROP_TEMPLATE)
    public String getPropTemplate() {
        return propTemplate;
    }

    @SchemagenOption(opt=OPT.CLASS_TEMPLATE)
    public String getClassTemplate() {
        return classTemplate;
    }

    @SchemagenOption(opt=OPT.INDIVIDUAL_TEMPLATE)
    public String getIndividualTemplate() {
        return individualTemplate;
    }
    
    @SchemagenOption(opt=OPT.DATATYPE_TEMPLATE)
    public String getDatatypeTemplate() {
        return datatypeTemplate;
    }

    @SchemagenOption(opt=OPT.UC_NAMES)
    public Boolean isUcNames() {
        return ucNames;
    }

    @SchemagenOption(opt=OPT.INCLUDE)
    public String getInclude() {
        return include;
    }

    @SchemagenOption(opt=OPT.CLASSNAME_SUFFIX)
    public String getClassNameSuffix() {
        return classNameSuffix;
    }

    @SchemagenOption(opt=OPT.ENCODING)
    public String getEncoding() {
        return encoding;
    }

    @SchemagenOption(opt=OPT.HELP)
    public Boolean isHelp() {
        return help;
    }

    @SchemagenOption(opt=OPT.DOS)
    public Boolean isDos() {
        return dos;
    }

    @SchemagenOption(opt=OPT.USE_INF)
    public Boolean isUseInf() {
        return useInf;
    }

    @SchemagenOption(opt=OPT.STRICT_INDIVIDUALS)
    public Boolean isStrictIndividuals() {
        return strictIndividuals;
    }

    @SchemagenOption(opt=OPT.INCLUDE_SOURCE)
    public Boolean isIncludeSource() {
        return includeSource;
    }

    @SchemagenOption(opt=OPT.NO_STRICT)
    public Boolean isNoStrict() {
        return noStrict;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setNoComments(Boolean noComments) {
        this.noComments = noComments;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setLangDaml(Boolean langDaml) {
        this.langDaml = langDaml;
    }

    public void setLangOwl(Boolean langOwl) {
        this.langOwl = langOwl;
    }

    public void setLangRdfs(Boolean langRdfs) {
        this.langRdfs = langRdfs;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setOntology(Boolean ontology) {
        this.ontology = ontology;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setClassDec(String classDec) {
        this.classDec = classDec;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setDeclarations(String declarations) {
        this.declarations = declarations;
    }

    public void setPropertySection(String propertySection) {
        this.propertySection = propertySection;
    }

    public void setClassSection(String classSection) {
        this.classSection = classSection;
    }

    public void setIndividualsSection(String individualsSection) {
        this.individualsSection = individualsSection;
    }
    
    public void setDatatypesSection(String datatypesSection) {
        this.datatypesSection = datatypesSection;
    }

    public void setNoProperties(Boolean noProperties) {
        this.noProperties = noProperties;
    }

    public void setNoClasses(Boolean noClasses) {
        this.noClasses = noClasses;
    }

    public void setNoIndividuals(Boolean noIndividuals) {
        this.noIndividuals = noIndividuals;
    }
    
    public void setNoDatatypes(Boolean noDatatypes) {
        this.noDatatypes = noDatatypes;
    }

    public void setNoHeader(Boolean noHeader) {
        this.noHeader = noHeader;
    }

    public void setPropTemplate(String propTemplate) {
        this.propTemplate = propTemplate;
    }

    public void setClassTemplate(String classTemplate) {
        this.classTemplate = classTemplate;
    }

    public void setIndividualTemplate(String individualTemplate) {
        this.individualTemplate = individualTemplate;
    }
    
    public void setDatatypeTemplate(String datatypeTemplate) {
        this.datatypeTemplate = datatypeTemplate;
    }

    public void setUcNames(Boolean ucNames) {
        this.ucNames = ucNames;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public void setClassNameSuffix(String classNameSuffix) {
        this.classNameSuffix = classNameSuffix;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setHelp(Boolean help) {
        this.help = help;
    }

    public void setDos(Boolean dos) {
        this.dos = dos;
    }

    public void setUseInf(Boolean useInf) {
        this.useInf = useInf;
    }

    public void setStrictIndividuals(Boolean strictIndividuals) {
        this.strictIndividuals = strictIndividuals;
    }

    public void setIncludeSource(Boolean includeSource) {
        this.includeSource = includeSource;
    }

    public void setNoStrict(Boolean noStrict) {
        this.noStrict = noStrict;
    }

    /**
     * Return true if this source actually represents the default options
     * element
     *
     * @return True for the default options
     */
    public boolean isDefaultOptions() {
        return input.equals( DEFAULT_OPTIONS_ELEM );
    }
}