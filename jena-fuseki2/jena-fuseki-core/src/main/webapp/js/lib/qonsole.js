/* Copyright (c) 2012-2013 Epimorphics Ltd. Released under Apache License 2.0 http://www.apache.org/licenses/ */

var qonsole = function() {
    "use strict";
    var YASQE = require('yasqe'),
        YASR = require('yasr');
    
    /**
     * Some custom requirements for Jena, on how to present the bindings. I.e., bnodes prefixed with _:, literals with surrounding quotes, and URIs with brackets
     */
    YASR.plugins.table.defaults.getCellContent = function (yasr, plugin, bindings, variable, context) {
        var binding = bindings[variable];
        var value = null;
        if (binding.type == "uri") {
            var title = null;
            var href = binding.value;
            var visibleString = href;
            var prefixed = false;
            if (context.usedPrefixes) {
                for (var prefix in context.usedPrefixes) {
                    if (visibleString.indexOf(context.usedPrefixes[prefix]) == 0) {
                        visibleString = prefix + ':' + href.substring(context.usedPrefixes[prefix].length);
                        prefixed = true;
                        break;
                    }
                }
            }
            if (!prefixed) visibleString = "&lt;" + visibleString + "&gt;";
            value = "<a " + (title? "title='" + href + "' ": "") + "class='uri' target='_blank' href='" + href + "'>" + visibleString + "</a>";
        } else if (binding.type == "bnode"){
            value = "<span class='nonUri'>_:" + binding.value + "</span>";
        } else if (binding.type == "literal") {
            var stringRepresentation = binding.value;
            if (binding["xml:lang"]) {
                stringRepresentation = '"' + binding.value + '"@' + binding["xml:lang"];
            } else if (binding.datatype) {
                var xmlSchemaNs = "http://www.w3.org/2001/XMLSchema#";
                var dataType = binding.datatype;
                if (dataType.indexOf(xmlSchemaNs) == 0) {
                    dataType = "xsd:" + dataType.substring(xmlSchemaNs.length);
                } else {
                    dataType = "<" + dataType + ">";
                }
                
                stringRepresentation = '"' + stringRepresentation + '"^^' + dataType;
            } else {
                //just put quotes around it
                stringRepresentation = '"' + stringRepresentation + '"';
            }
            value = "<span class='nonUri'>" + stringRepresentation + "</span>";
        } else {
            //this is a catch-all: when using e.g. a csv content type, the bindings are not typed
            value = binding.value;
        }
        return "<div>" + value + "</div>";
    };
    
    /* JsLint */
    /*
     * global sprintf, testCSS, loadConfig, bindEvents, $, onConfigLoaded,
     * updatePrefixDeclaration, _, showCurrentQuery, setCurrentEndpoint,
     * setCurrentFormat, elementVisible, runQuery, onLookupPrefix,
     * startTimingResults, onAddPrefix, initQuery, CodeMirror, onQuerySuccess,
     * onQueryFail, ajaxDataType, resetResults, XMLSerializer, showTableResult,
     * showCodeMirrorResult
     */

    /* --- module vars --- */
    /** The loaded configuration */
    var _config = {};
    var yasqe = null;
    var yasr = null;
    var _startTime = 0;
    var _outstandingQueries = 0;

    /* --- utils --- */

    /**
     * Return the string representation of the given XML value, which may be a
     * string or a DOM object
     */
    var xmlToString = function(xmlData) {
        var xs = _.isString(xmlData) ? xmlData : null;

        if (!xs && window.ActiveXObject && xmlData.xml) {
            xs = xmlData.xml;
        }

        if (!xs) {
            xs = new XMLSerializer().serializeToString(xmlData);
        }

        return xs;
    };

    /** Browser sniffing */
    var isOpera = function() {
        return !!(window.opera && window.opera.version);
    }; // Opera 8.0+
    var isFirefox = function() {
        return testCSS('MozBoxSizing');
    }; // FF 0.8+
    var isSafari = function() {
        return Object.prototype.toString.call(window.HTMLElement).indexOf(
                'Constructor') > 0;
    }; // At least Safari 3+: "[object HTMLElementConstructor]"
    var isChrome = function() {
        return !isSafari() && testCSS('WebkitTransform');
    }; // Chrome 1+
    var isIE = function() {
        return /* @cc_on!@ */false || testCSS('msTransform');
    }; // At least IE6

    var testCSS = function(prop) {
        return document.documentElement.style.hasOwnProperty(prop);
    };

    /* --- application code --- */

    /** Initialisation - only called once */
    var init = function(config) {
        initYasqe();
        loadConfig(config);
        bindEvents();
    };

    var initYasqe = function() {
        yasqe = YASQE(document.getElementById("query-edit-cm"), {
            sparql: {
                showQueryButton: true,
                callbacks: {
                    beforeSend: startTimingResults,
                    complete: showTime,
                }
            }
        });
        yasr = YASR(document.getElementById("results"), {
            useGoogleCharts: false,
            //this way, the URLs in the results are prettified using the defined prefixes in the query
            getUsedPrefixes: yasqe.getPrefixesFromQuery
        });
        

        /**
        * Set some of the hooks to link YASR and YASQE
        */
        yasqe.options.sparql.callbacks.complete = yasr.setResponse;
    };

    /** Load the configuration definition */
    var loadConfig = function(config) {
        if (config.configURL) {
            $.getJSON(config.configURL, onConfigLoaded);
        } else {
            onConfigLoaded(config);
        }
    };

    /** Return the current config object */
    var config = function() {
        return _config;
    };

    /** Bind events that we want to manage */
    var bindEvents = function() {
        $("ul.prefixes").on(
                "click",
                "a.btn",
                function(e) {
                    var elem = $(e.currentTarget);
                    updatePrefixDeclaration($.trim(elem.text()), elem
                            .data("uri"), !elem.is(".active"));
                });
        $("ul.examples").on("click", "a", function(e) {
            var elem = $(e.currentTarget);
            $("ul.examples a").removeClass("active");
            _.defer(function() {
                showCurrentQuery();
            });
        });
        $(".endpoints").on("click", "a", function(e) {
            var elem = $(e.currentTarget);
            setCurrentEndpoint($.trim(elem.text()));
        });
        $("#sparqlEndpoint").change(function() {
           yasqe.options.sparql.endpoint = $(this).val();
        });

        // dialogue events
        $("#prefixEditor").on("click", "#lookupPrefix", onLookupPrefix).on(
                "keyup", "#inputPrefix", function(e) {
                    var elem = $(e.currentTarget);
                    $("#lookupPrefix span").text(sprintf("'%s'", elem.val()));
                });
        $("#addPrefix").on("click", onAddPrefix);
        
        /**
         * register content type changes.
         * Do not need to set them on load, as their default values are already the default vals of YASQE as well
         */
        $("#graphContentType").change(function(){yasqe.options.sparql.acceptHeaderGraph = $(this).val()});
        $("#selectContentType").change(function(){yasqe.options.sparql.acceptHeaderSelect = $(this).val()});
    };

    /** List the current defined prefixes from the config */
    var initPrefixes = function(config) {
        var prefixAdd = $("ul.prefixes li:last");
        $
                .each(
                        config.prefixes,
                        function(key, value) {
                            var html = sprintf(
                                    "<li><a class='btn btn-custom2 btn-sm' data-toggle='button' data-uri='%s'>%s</a></li>",
                                    value, key);
                            $(html).insertBefore(prefixAdd);
                        });
    };

    /** List the example queries from the config */
    var initExamples = function(config) {
        var examples = $("ul.examples");
        examples.empty();

        $
                .each(
                        config.queries,
                        function(i, queryDesc) {
                            var html = sprintf(
                                    "<li><a class='btn btn-custom2 btn-sm' data-toggle='button'>%s</a></li>",
                                    queryDesc.name);
                            examples.append(html);

                            if (queryDesc.queryURL) {
                                loadRemoteQuery(queryDesc.name,
                                        queryDesc.queryURL);
                            }
                        });
        
        setFirstQueryActive();
    };

    /** Set the default active query */
    var setFirstQueryActive = function() {
        if (_outstandingQueries === 0 && yasqe.getValue() == YASQE.defaults.value) {
            //only load the example query, when YASQE has not retrieved a previous query executed by the client
            $("ul.examples").find("a").first().addClass("active");
            
            showCurrentQuery();
        }
    };

    /** Load a remote query */
    var loadRemoteQuery = function(name, url) {
        _outstandingQueries++;

        var options = {
            success : function(data, xhr) {
                namedExample(name).query = data;

                _outstandingQueries--;
                setFirstQueryActive();
            },
            failure : function() {
                namedExample(name).query = "Not found: " + url;

                _outstandingQueries--;
                setFirstQueryActive();
            },
            dataType : "text"
        };

        $.ajax(url, options);
    };

    /** Set up the drop-down list of end-points */
    var initEndpoints = function(config) {
        var endpoints = $("ul.endpoints");
        endpoints.empty();
        if (config.endpoints) {
            $
                    .each(
                            config.endpoints,
                            function(key, url) {
                                var html = sprintf(
                                        "<li role='presentation'><a role='menuitem' tabindex='-1' href='#'>%s</a></li>",
                                        url);
                                endpoints.append(html);
                            });

            setCurrentEndpoint(config.endpoints["default"]);
        }
    };

    /** Successfully loaded the configuration */
    var onConfigLoaded = function(config, status, jqXHR) {
        _config = config;
        initPrefixes(config);
        initExamples(config);
        initEndpoints(config);
    };

    /** Set the current endpoint text */
    var setCurrentEndpoint = function(url) {
        yasqe.options.sparql.endpoint = url;
        $("[id=sparqlEndpoint]").val(url);
    };

    /** Return the current endpoint text */
    var currentEndpoint = function(url) {
        return $("[id=sparqlEndpoint]").val();
    };

    /** Return the query definition with the given name */
    var namedExample = function(name) {
        return _.find(config().queries, function(ex) {
            return ex.name === name;
        });
    };

    /** Return the currently active named example */
    var currentNamedExample = function() {
        return namedExample($.trim($("ul.examples a.active").first().text()));
    };

    /** Display the given query, with the currently defined prefixes */
    var showCurrentQuery = function() {
        var query = currentNamedExample();
        displayQuery(query);
    };

    /** Display the given query */
    var displayQuery = function(query) {
        if (query) {
            var queryBody = query.query ? query.query : query;
            var prefixes = assemblePrefixes(queryBody, query.prefixes)

            var q = sprintf("%s\n\n%s", renderPrefixes(prefixes),
                    stripLeader(queryBody));
            yasqe.setValue(q);

            syncPrefixButtonState(prefixes);
        }
    };

    /** Return the currently selected output format */
    var selectedFormat = function() {
        return $("a.display-format").data("value");
    };

    /** Update the user's format selection */
    var setCurrentFormat = function(val, label) {
        $("a.display-format").data("value", val).find("span").text(label);
    };

    /** Assemble the set of prefixes to use when initially rendering the query */
    var assemblePrefixes = function(queryBody, queryDefinitionPrefixes) {
        if (queryBody.match(/^prefix/)) {
            // strategy 1: there are prefixes encoded in the query body
            return assemblePrefixesFromQuery(queryBody);
        } else if (queryDefinitionPrefixes) {
            // strategy 2: prefixes given in query def
            return _.map(queryDefinitionPrefixes, function(prefixName) {
                return {
                    name : prefixName,
                    uri : config().prefixes[prefixName]
                };
            });
        } else {
            return assembleCurrentPrefixes();
        }
    };

    /** Return an array comprising the currently selected prefixes */
    var assembleCurrentPrefixes = function() {
        var l = $("ul.prefixes a.active").map(function(i, elt) {
            return {
                name : $.trim($(elt).text()),
                uri : $(elt).data("uri")
            };
        });
        return $.makeArray(l);
    };

//    /** Return an array of the prefixes parsed from the given query body */
//    var assemblePrefixesFromQuery = function(queryBody) {
//        var leader = queryLeader(queryBody)[0].trim();
//        var pairs = _.compact(leader.split("prefix"));
//        var prefixes = [];
//
//        _.each(pairs, function(pair) {
//            var m = pair.match("^\\s*(\\w+)\\s*:\\s*<([^>]*)>\\s*$");
//            prefixes.push({
//                name : m[1],
//                uri : m[2]
//            });
//        });
//
//        return prefixes;
//    };

    /**
     * Ensure that the prefix buttons are in sync with the prefixes used in a
     * new query
     */
    var syncPrefixButtonState = function(prefixes) {
        $("ul.prefixes a").each(function(i, elt) {
            var name = $.trim($(elt).text());

            if (_.find(prefixes, function(p) {
                return p.name === name;
            })) {
                $(elt).addClass("active");
            } else {
                $(elt).removeClass("active");
            }
        });
    };

    /** Split a query into leader (prefixes and leading blank lines) and body */
    var queryLeader = function(query) {
        var pattern = /(prefix [^>]+>[\s\n]*)/;
        var queryBody = query;
        var i = 0;
        var m = queryBody.match(pattern);

        while (m) {
            i += m[1].length;
            queryBody = queryBody.substring(i);
            m = queryBody.match(pattern);
        }

        return [ query.substring(0, query.length - queryBody.length), queryBody ];
    };

    /** Remove the query leader */
    var stripLeader = function(query) {
        return queryLeader(query)[1];
    };

    /** Return a string comprising the given prefixes */
    var renderPrefixes = function(prefixes) {
        return _.map(prefixes, function(p) {
            return sprintf("prefix %s: <%s>", p.name, p.uri);
        }).join("\n");
    };

    /** Add or remove the given prefix declaration from the current query */
    var updatePrefixDeclaration = function(prefix, uri, added) {
        var prefixObj = {};
        prefixObj[prefix] = uri;
        if (added) {
            yasqe.addPrefixes(prefixObj);
        } else {
            yasqe.removePrefixes(prefixObj);
        }
    };

    /** Return the sparql service we're querying against */
    var sparqlService = function() {
        var service = config().service;
        if (!service) {
            // default is the remote service
            config().service = new RemoteSparqlService();
            service = config().service;
        }

        return service;
    };


    /** Hide or reveal an element using Bootstrap .hidden class */
    var elementVisible = function(elem, visible) {
        if (visible) {
            $(elem).removeClass("hidden");
        } else {
            $(elem).addClass("hidden");
        }
    };

    /** Prepare to show query time taken */
    var startTimingResults = function() {
        _startTime = new Date().getTime();
        elementVisible(".timeTaken");
    };
    
    
    /** Show results count and time */
    var showTime = function() {
        var duration = new Date().getTime() - _startTime;
        var ms = duration % 1000;
        duration = Math.floor(duration / 1000);
        var s = duration % 60;
        var m = Math.floor(duration / 60);

        var html = sprintf("time taken:  %d min %d.%03d s", m, s, ms);

        $(".timeTaken").html(html);
        elementVisible(".timeTaken", true);
    };


    /** Lookup a prefix on prefix.cc */
    var onLookupPrefix = function(e) {
        e.preventDefault();

        var prefix = $.trim($("#inputPrefix").val());
        $("#inputURI").val("");

        if (prefix) {
            $.getJSON(sprintf("http://prefix.cc/%s.file.json", prefix),
                    function(data) {
                        $("#inputURI").val(data[prefix]);
                    });
        }
    };

    /** User wishes to add the prefix */
    var onAddPrefix = function(e) {
        var prefix = $.trim($("#inputPrefix").val());
        var uri = $.trim($("#inputURI").val());

        if (uri) {
            _config.prefixes[prefix] = uri;
        } else {
            delete _config.prefixes[prefix];
        }

        // remember the state of current user selections, then re-create the
        // list
        var selections = {};
        $("ul.prefixes a.btn").each(function(i, a) {
            selections[$(a).text()] = $(a).hasClass("active");
        });

        $("ul.prefixes li[class!=keep]").remove();
        initPrefixes(_config);

        // restore selections state
        $.each(selections, function(k, v) {
            if (!v) {
                $(sprintf("ul.prefixes a.btn:contains('%s')", k)).removeClass(
                        "active");
            }
        });

        var lines = yasqe.getValue().split("\n");
        lines = _.reject(lines, function(line) {
            return line.match(/^prefix/);
        });
        var q = sprintf("%s\n%s", renderPrefixes(assembleCurrentPrefixes()),
                lines.join("\n"));
        yasqe.setValue(q);
    };

    /** Disable or enable the button to submit a query */
    var disableSubmit = function(disable) {
        var elem = $("a.run-query");
        elem.prop('disabled', disable);
        if (disable) {
            elem.addClass("disabled");
        } else {
            elem.removeClass("disabled");
        }
    };

    return {
        init : init,
        setCurrentEndpoint: setCurrentEndpoint
    };
}();
