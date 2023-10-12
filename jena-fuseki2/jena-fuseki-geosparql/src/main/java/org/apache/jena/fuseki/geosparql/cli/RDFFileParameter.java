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
package org.apache.jena.fuseki.geosparql.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ext.io.github.galbiston.rdf_tables.cli.FormatParameter;
import org.apache.jena.riot.RDFFormat;

/**
 *
 *
 */
public class RDFFileParameter implements IStringConverter<List<FileGraphFormat>>, IParameterValidator {

    private static final FormatParameter FORMAT_PARAMETER = new FormatParameter();
    private static final String FORMAT_SEP = ">";
    private static final String NAME_SEP = "#";

    @Override
    public List<FileGraphFormat> convert(String value) {
        String[] values = value.split(",");
        List<FileGraphFormat> fileList = new ArrayList<>();
        for (String val : values) {
            FileGraphFormat file = build(val);
            fileList.add(file);
        }
        return fileList;
    }

    public FileGraphFormat build(String value) {
        File file;
        String name = "";
        RDFFormat format = RDFFormat.TTL;

        String target = value;
        if (target.contains(FORMAT_SEP)) {
            String[] parts = target.split(FORMAT_SEP);
            format = FORMAT_PARAMETER.convert(parts[1]);
            target = parts[0];
        }

        if (target.contains(NAME_SEP)) {
            String[] parts = target.split(NAME_SEP);
            name = parts[1];
            target = parts[0];
        }

        file = new File(target);

        return new FileGraphFormat(file, name, format);

    }

    @Override
    public void validate(String name, String value) throws ParameterException {

        int formatIndex;
        int nameIndex;
        String[] values = value.split(",");
        for (String val : values) {
            formatIndex = val.indexOf(FORMAT_SEP);
            nameIndex = val.indexOf(NAME_SEP);
            if (formatIndex > -1 && nameIndex > -1) {
                if (formatIndex < nameIndex) {
                    throw new ParameterException("Parameter " + name + " and value " + val + " must have format (" + formatIndex + ") after graph name (" + nameIndex + ").");
                }
            }
        }
    }

}
