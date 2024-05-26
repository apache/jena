/**
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.ext.io.github.galbiston.rdf_tables.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.riot.RDFFormat;

/**
 *
 *
 */
public class FormatParameter implements IStringConverter<RDFFormat>, IParameterValidator {

    @Override
    public RDFFormat convert(String rdfFormat) {

        switch (rdfFormat.toLowerCase()) {
            case "ttl":
                return RDFFormat.TTL;
            case "ttl-pretty":
                return RDFFormat.TURTLE_PRETTY;
            case "json-ld":
                return RDFFormat.JSONLD;
            case "nt":
                return RDFFormat.NT;
            case "nq":
                return RDFFormat.NQ;
            case "json-rdf":
                return RDFFormat.RDFJSON;
            case "xml-plain":
                return RDFFormat.RDFXML_PLAIN;
            case "xml-pretty":
                return RDFFormat.RDFXML_PRETTY;
            case "xml":
                return RDFFormat.RDFXML;
            case "thrift":
                return RDFFormat.RDF_THRIFT;
            case "trig":
                return RDFFormat.TRIG;
            case "trix":
                return RDFFormat.TRIX;
            default:
                return RDFFormat.TTL;
        }
    }

    private static final List<String> PERMITTED_FORMATS = Arrays.asList("json-ld", "nt", "nq", "json-rdf", "xml-plain", "xml-pretty", "xml", "thrift", "trig", "trix", "ttl", "ttl-pretty");

    @Override
    public void validate(String name, String value) throws ParameterException {
        String val = value.toLowerCase();
        if (!PERMITTED_FORMATS.contains(val)) {
            throw new ParameterException("Parameter " + name + " and value " + value + " should be one of " + String.join(", ", PERMITTED_FORMATS) + ".");
        }

    }

    public static String fileExtension(RDFFormat rdfFormat) {

        switch (rdfFormat.toString()) {
            case "TTL":
                return ".ttl";
            case "ttl-pretty":
                return ".ttl";
            case "json-ld":
                return ".json";
            case "nt":
                return ".nt";
            case "nq":
                return ".nq";
            case "json-rdf":
                return ".jsonld";
            case "xml-plain":
                return ".rdf";
            case "xml-pretty":
                return ".rdf";
            case "xml":
                return ".rdf";
            case "thrift":
                return ".trdf";
            case "trig":
                return ".trig";
            case "trix":
                return ".trix";
            default:
                return ".ttl";
        }

    }

    public static String buildFilename(File inputFile, RDFFormat rdfFormat) {

        String inputFilename = inputFile.getName();

        int endIndex = inputFilename.indexOf(".");
        //No file extension so set to the end of the file.
        if (endIndex == -1) {
            endIndex = inputFilename.length();
        }
        return inputFilename.substring(0, endIndex) + fileExtension(rdfFormat);
    }

}
