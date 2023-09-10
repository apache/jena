-# This template is used for generating a rollup EARL report. It expects to be
-# called with a single _tests_ local with the following structure
-#
-#  {
-#    "@context": {...},
-#    "@id": "",
-#    "@type": "earl:Software",
-#    "name": "...",
-#    "bibRef": "[[...]]",
-#    "assertions": ["rdfxml-streaming-parser.js.ttl"],
-#    "testSubjects": [
-#      {
-#        "@id": "https://www.npmjs.com/package/rdfxml-streaming-parser/",
-#        "@type": "earl:TestSubject",
-#        "name": "rdfxml-streaming-parser"
-#      },
-#      ...
-#    ],
-#    "tests": [{
-#      "@id": "http://www.w3.org/2013/TurtleTests/manifest.ttl#turtle-syntax-file-01",
-#      "@type": ["earl:TestCriterion", "earl:TestCase"],
-#      "title": "subm-test-00",
-#      "description": "Blank subject",
-#      "testAction": "http://www.w3.org/2013/TurtleTests/turtle-syntax-file-01.ttl",
-#      "testResult": "http://www.w3.org/2013/TurtleTests/turtle-syntax-file-01.out"
-#      "mode": "earl:automatic",
-#      "assertions": [
-#        {
-#          "@type": "earl:Assertion",
-#          "assertedBy": "http://greggkellogg.net/foaf#me",
-#          "test": "http://svn.apache.org/repos/asf/jena/Experimental/riot-reader/testing/RIOT/Lang/TurtleSubm/manifest.ttl#testeval00",
-#          "subject": "http://rubygems.org/gems/rdf-turtle",
-#          "result": {
-#            "@type": "earl:TestResult",
-#            "outcome": "earl:passed"
-#          }
-#        }
-#      ]
-#    }]
-#  }
- require 'cgi'

!!! 5
%html{:prefix => "earl: http://www.w3.org/ns/earl# doap: http://usefulinc.com/ns/doap# mf: http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#"}
  - test_info = {}
  - test_refs = {}
  - subject_refs = {}
  - passed_tests = []
  - subjects = tests['testSubjects'].sort_by {|s| s['name'].to_s.downcase}
  - subjects.each_with_index do |subject, index|
    - subject_refs[subject['@id']] = "subj_#{index}"
  %head
    %meta{"http-equiv" => "Content-Type", :content => "text/html;charset=utf-8"}
    %link{:rel => "alternate", :href => "earl.ttl"}
    %link{:rel => "alternate", :href => "earl.jsonld"}
    - tests['assertions'].each do |file|
      %link{:rel => "related", :href => file}
    %title
      = tests['name']
      Implementation Report
    %script.remove{:src => "../../local-biblio.js"}
    %script.remove{:type => "text/javascript", :src => "https://www.w3.org/Tools/respec/respec-w3c-common"}
    :javascript
      var respecConfig = {
          // extend the bibliography entries
          localBiblio: localBibliography,

          // specification status (e.g. WD, LCWD, NOTE, etc.). If in doubt use ED.
          specStatus:           "base",
          copyrightStart:       "2010",
          doRDFa:               "1.1",

          // the specification's short name, as in http://www.w3.org/TR/short-name/
          shortName:            "rdf-syntax-grammar",
          //subtitle:             "RDF/XML Implementation Conformance Report",
          // if you wish the publication date to be other than today, set this
          publishDate:  "#{Time.now.strftime("%Y/%m/%d")}",

          // if there is a previously published draft, uncomment this and set its YYYY-MM-DD date
          // and its maturity status
          //previousPublishDate:  "2011-10-23",
          //previousMaturity:     "ED",
          //previousDiffURI:      "http://json-ld.org/spec/ED/json-ld-syntax/20111023/index.html",
          //diffTool:             "http://www.aptest.com/standards/htmldiff/htmldiff.pl",

          // if there a publicly available Editor's Draft, this is the link
          //edDraftURI:           "",

          // if this is a LCWD, uncomment and set the end of its review period
          // lcEnd: "2009-08-05",

          // editors, add as many as you like
          // only "name" is required
          editors:  [
              { name: "Gregg Kellogg", url: "http://greggkellogg.net/",
                company: "Kellogg Associates" },
              { name: "Andy Seaborne",
                company: "The Apache Software Foundation"}
          ],

          // authors, add as many as you like.
          // This is optional, uncomment if you have authors as well as editors.
          // only "name" is required. Same format as editors.
          //authors:  [
          //RDF Working Group],

          // name of the WG
          wg:           "RDF Working Group",

          // URI of the public WG page
          wgURI:        "http://www.w3.org/2011/rdf-wg/",

          // name (with the @w3c.org) of the public mailing to which comments are due
          wgPublicList: "public-rdf-comments",

          // URI of the patent status for this WG, for Rec-track documents
          // !!!! IMPORTANT !!!!
          // This is important for Rec-track documents, do not copy a patent URI from a random
          // document unless you know what you're doing. If in doubt ask your friendly neighbourhood
          // Team Contact.
          wgPatentURI:  "http://www.w3.org/2004/01/pp-impl/46168/status",
          alternateFormats: [
            {uri: "earl.ttl", label: "Turtle"},
            {uri: "earl.jsonld", label: "JSON-LD"}
          ],
      };
    :css
      span[property='dc:description'] { display: none; }
      td.PASS { color: green; }
      td.FAIL { color: red; }
      table.report {
        border-width: 1px;
        border-spacing: 2px;
        border-style: outset;
        border-color: gray;
        border-collapse: separate;
        background-color: white;
      }
      table.report th {
        border-width: 1px;
        padding: 1px;
        border-style: inset;
        border-color: gray;
        background-color: white;
        -moz-border-radius: ;
      }
      table.report td {
        border-width: 1px;
        padding: 1px;
        border-style: inset;
        border-color: gray;
        background-color: white;
        -moz-border-radius: ;
      }
      tr.summary {font-weight: bold;}
      td.passed-all {color: green;}
      td.passed-most {color: darkorange;}
      td.passed-some {color: red;}
  %body{:prefix => "earl: http://www.w3.org/ns/earl# doap: http://usefulinc.com/ns/doap# mf: http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#"}
    %section#abstract{:about => tests['@id'], :typeof => [tests['@type']].flatten.join(" ")}
      %p
        This document report test subject conformance for and related specifications for
        %span{:property => "doap:name"}<=tests['name']
        %span{:property => "dc:bibliographicCitation"}<
          = tests['bibRef']
        according to the requirements of the Evaluation and Report Language (EARL) 1.0 Schema [[EARL10-SCHEMA]].
      %p
        This report is also available in alternate formats:
        %a{:rel => "xhv:alternate", :href => "earl.ttl"}
          Turtle
        and
        %a{:rel => "xhv:alternate", :href => "earl.jsonld"}
          JSON-LD
    %section#sodt
    %section
      :markdown
        ## Instructions for submitting implementation reports

          Tests should be run using the test manifests defined in the 
          [Test Manifests](#test-manifests) Section.

          The assumed base URI for the tests is `<http://example/base/>` if needed.

          Reports should be submitted in Turtle format to [public-rdf-comments@w3.org](mailto:public-rdf-comments@w3.org)
          and include an `earl:Assertion`
          for each test, referencing the test resource from the associated manifest
          and the test subject being reported upon. An example test entry is be the following:

              [ a earl:Assertion;
                earl:assertedBy <https://www.rubensworks.net/#me>;
                earl:subject <https://www.npmjs.com/package/rdfxml-streaming-parser/>;
                earl:test <https://www.w3.org/2013/RDFXMLTests/manifest.ttl#rdf-element-not-mandatory-test001>;
                earl:result [
                  a earl:TestResult;
                  earl:outcome earl:passed;
                  dc:date "2018-10-08T23:16:03.823Z"^^xsd:dateTime];
                earl:mode earl:automatic ] .

          The Test Subject should be defined as a `doap:Project`, including the name,
          homepage and developer(s) of the software (see [[DOAP]]). Optionally, including the
          project description and programming language. An example test subject description is the following:

              <> foaf:primaryTopic <https://www.npmjs.com/package/rdfxml-streaming-parser/>;
                  dc:issued "2018-10-08T23:16:03.823Z"^^xsd:dateTime;
                  foaf:maker <https://www.rubensworks.net/#me>.

              <https://www.npmjs.com/package/rdfxml-streaming-parser/> a earl:Software, earl:TestSubject, doap:Project;
                  doap:name "rdfxml-streaming-parser";
                  dc:title "rdfxml-streaming-parser";
                  doap:homepage <https://github.com/rdfjs/rdfxml-streaming-parser.js#readme>;
                  doap:license <http://opensource.org/licenses/MIT>;
                  doap:programming-language "JavaScript";
                  doap:implements <https://www.w3.org/TR/rdf-syntax-grammar/>;
                  doap:category <http://dbpedia.org/resource/Resource_Description_Framework>;
                  doap:download-page <https://npmjs.org/package/rdfxml-streaming-parser>;
                  doap:bug-database <https://github.com/rdfjs/rdfxml-streaming-parser.js/issues>;
                  doap:developer <https://www.rubensworks.net/#me>;
                  doap:maintainer <https://www.rubensworks.net/#me>;
                  doap:documenter <https://www.rubensworks.net/#me>;
                  doap:maker <https://www.rubensworks.net/#me>;
                  dc:creator <https://www.rubensworks.net/#me>;
                  dc:description "Streaming RDF/XML parser"@en;
                  doap:description "Streaming RDF/XML parser"@en.

          The software developer, either an organization or one or more individuals SHOULD be
          referenced from `doap:developer` using [[FOAF]]. For example:

              <https://www.rubensworks.net/#me> a foaf:Person, earl:Assertor;
                  foaf:name "Ruben Taelman <rubensworks@gmail.com>";
                  foaf:homepage <https://www.rubensworks.net/>.

          See [RDF Test Suite Wiki](https://www.w3.org/2011/rdf-wg/wiki/RDF_Test_Suites)
          for more information.
    %section
      %h2
        Test Manifests
      - tests['entries'].each_with_index do |manifest, ndx2|
        - test_cases = manifest['entries']
        %section{:typeof => manifest['@type'].join(" "), :resource => manifest['@id']}
          %h2<="RDF/XML Tests"
          - [manifest['description']].flatten.compact.each do |desc|
            %p<
              ~ CGI.escapeHTML desc.to_s
          %table.report
            - skip_subject = {}
            - passed_tests[ndx2] = []
            %tr
              %th
                Test
              - subjects.each_with_index do |subject, index|
                -# If subject is untested for every test in this manifest, skip it
                - skip_subject[subject['@id']] = manifest['entries'].all? {|t| t['assertions'][index]['result']['outcome'] == 'earl:untested'}
                - unless skip_subject[subject['@id']]
                  %th
                    %a{:href => '#' + subject_refs[subject['@id']]}<=Array(subject['name']).first
            - test_cases.each do |test|
              - tid = 'test_' + (test['@id'][0,2] == '_:' ? test['@id'][2..-1] : test['@id'].split('#').last)
              - (test_info[tid] ||= []) << test
              - test_refs[test['@id']] = tid
              %tr{:rel => "mf:entries", :typeof => test['@type'].join(" "), :resource => test['@id'], :inlist => true}
                %td
                  %a{:href => "##{tid}"}<
                    ~ CGI.escapeHTML test['title'].to_s
                - subjects.each_with_index do |subject, ndx|
                  - next if skip_subject[subject['@id']]
                  - assertion = test['assertions'].detect {|a| a['subject'] == subject['@id']}
                  - pass_fail = assertion['result']['outcome'].split(':').last.upcase.sub(/(PASS|FAIL)ED$/, '\1')
                  - passed_tests[ndx2][ndx] = (passed_tests[ndx2][ndx] || 0) + (pass_fail == 'PASS' ? 1 : 0)
                  %td{:class => pass_fail, :property => "earl:assertions", :typeof => assertion['@type']}
                    - if assertion['assertedBy']
                      %link{:property => "earl:assertedBy", :href => assertion['assertedBy']}
                    %link{:property => "earl:test", :href => assertion['test']}
                    %link{:property => "earl:subject", :href => assertion['subject']}
                    - if assertion['mode']
                      %link{:property => 'earl:mode', :href => assertion['mode']}
                    %span{:property => "earl:result", :typeof => assertion['result']['@type']}
                      %span{:property => 'earl:outcome', :resource => assertion['result']['outcome']}
                        = pass_fail
            %tr.summary
              %td
                = "Percentage passed out of #{manifest['entries'].length} Tests"
              - passed_tests[ndx2].compact.each do |r|
                - pct = (r * 100.0) / manifest['entries'].length
                %td{:class => (pct == 100.0 ? 'passed-all' : (pct >= 95.0 ? 'passed-most' : 'passed-some'))}
                  = "#{'%.1f' % pct}%"
    %section.appendix
      %h2
        Test Subjects
      %p
        This report was tested using the following test subjects:
      %dl
        - subjects.each_with_index do |subject, index|
          %dt{:id => subject_refs[subject['@id']]}
            %a{:href => subject['@id']}
              %span{:about => subject['@id'], :property => "doap:name"}<= Array(subject['name']).first
          %dd{:property => "earl:testSubjects", :resource => subject['@id'], :typeof => [subject['@type']].flatten.join(" ")}
            %dl
              - if subject['doapDesc']
                %dt= "Description"
                %dd{:property => "doap:description", :lang => 'en'}<
                  ~ CGI.escapeHTML subject['doapDesc'].to_s
              - if subject['language']
                %dt= "Programming Language"
                %dd{:property => "doap:programming-language"}<
                  ~ CGI.escapeHTML subject['language'].to_s
              - if subject['homepage']
                %dt= "Home Page"
                %dd{:property => "doap:homepage"}
                  %a{:href=> subject['homepage']}
                    ~ CGI.escapeHTML subject['homepage'].to_s
              - if subject['developer']
                %dt= "Developer"
                - subject['developer'].each do |dev|
                  %dd{:rel => "doap:developer"}
                    %div{:resource => dev['@id'], :typeof => [dev['@type']].flatten.join(" ")}
                      - if dev.has_key?('@id')
                        %a{:href => dev['@id']}
                          %span{:property => "foaf:name"}<
                            ~ CGI.escapeHTML dev['foaf:name'].to_s
                      - else
                        %span{:property => "foaf:name"}<
                          ~ CGI.escapeHTML dev['foaf:name'].to_s
              %dt
                Test Suite Compliance
              %dd
                %table.report
                  %tbody
                    - tests['entries'].each_with_index do |manifest, ndx|
                      - passed = passed_tests[ndx][index].to_i
                      - next if passed == 0
                      - total = manifest['entries'].length
                      - pct = (passed * 100.0) / total
                      %tr
                        %td{:class => (pct == 100.0 ? 'passed-all' : (pct >= 85.0 ? 'passed-most' : 'passed-some'))}
                          = "#{passed}/#{total} (#{'%.1f' % pct}%)"
    - unless tests['assertions'].empty?
      %section.appendix{:rel => "xhv:related earl:assertions"}
        %h2
          Individual Test Results
        %p
          Individual test results used to construct this report are available here:
        %ul
          - tests['assertions'].each do |file|
            %li
              %a.source{:href => file}<= file
    %section#appendix{:property => "earl:generatedBy", :resource => tests['generatedBy']['@id'], :typeof => tests['generatedBy']['@type']}
      %h2
        Report Generation Software
      - doap = tests['generatedBy']
      - rel = doap['release']
      %p
        This report generated by
        %span{:property => "doap:name"}<
          %a{:href => tests['generatedBy']['@id']}<
            = doap['name']
        %meta{:property => "doap:shortdesc", :content => doap['shortdesc'], :lang => 'en'}
        %meta{:property => "doap:description", :content => doap['doapDesc'], :lang => 'en'}
        version
        %span{:property => "doap:release", :resource => rel['@id'], :typeof => 'doap:Version'}
          %span{:property => "doap:revision"}<=rel['revision']
          %meta{:property => "doap:name", :content => rel['name']}
          %meta{:property => "doap:created", :content => rel['created'], :datatype => "xsd:date"}
        an
        %a{:property => "doap:license", :href => doap['license']}<="Unlicensed"
        %span{:property => "doap:programming-language"}<="Ruby"
        application. More information is available at
        %a{:property => "doap:homepage", :href => doap['homepage']}<=doap['homepage']
        = "."
      %p{:property => "doap:developer", :resource => "http://greggkellogg.net/foaf#me", :typeof => "foaf:Person"}
        This software is provided by
        %a{:property => "foaf:homepage", :href => "http://greggkellogg.net/"}<
          %span{:aboue => "http://greggkellogg.net/foaf#me", :property => "foaf:name"}<
            Gregg Kellogg
        in hopes that it might make the lives of conformance testers easier.
