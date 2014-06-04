<?xml version="1.0"?>

<!--

XSLT script to format SPARQL Query Results XML Format into xhtml

Copyright Â© 2004, 2005 World Wide Web Consortium, (Massachusetts
Institute of Technology, European Research Consortium for
Informatics and Mathematics, Keio University). All Rights
Reserved. This work is distributed under the W3CÂ® Software
License [1] in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.

[1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231

Version 1 : Dave Beckett (DAWG)
Version 2 : Jeen Broekstra (DAWG)
Customization for SPARQler: Andy Seaborne
URIs as hrefs in results : Bob DuCharme & Andy Seaborne

> -    <xsl:for-each select="//res:head/res:variable">
> +    <xsl:for-each select="/res:sparql/res:head/res:variable">

-->

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns="http://www.w3.org/1999/xhtml"
		xmlns:res="http://www.w3.org/2005/sparql-results#"
		xmlns:fn="http://www.w3.org/2005/xpath-functions"
		exclude-result-prefixes="res xsl">

  <!--
    <xsl:output
    method="html"
    media-type="text/html"
    doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
    indent="yes"
    encoding="UTF-8"/>
  -->

  <!-- or this? -->

  <xsl:output
   method="xml" 
   indent="yes"
   encoding="UTF-8" 
   doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
   doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
   omit-xml-declaration="no" />

    <xsl:template match="res:link">
      <p>Link to <xsl:value-of select="@href"/></p>
    </xsl:template>

    <xsl:template name="header">
      <div>
        <h2>Header</h2>
        <xsl:apply-templates select="res:head/res:link"/>
      </div>
    </xsl:template>

  <xsl:template name="boolean-result">
    <div>
      <p>ASK => <xsl:value-of select="res:boolean"/></p>
    </div>
  </xsl:template>


  <xsl:template name="vb-result">
    <div>
      <table>
	<xsl:text>
	</xsl:text>
	<tr>
	  <xsl:for-each select="res:head/res:variable">
	    <th><xsl:value-of select="@name"/></th>
	  </xsl:for-each>
	</tr>
	<xsl:text>
	</xsl:text>
	<xsl:for-each select="res:results/res:result">
	  <tr>
	    <xsl:apply-templates select="."/>
	  </tr>
	</xsl:for-each>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="res:result">
    <xsl:variable name="current" select="."/>
    <xsl:for-each select="/res:sparql/res:head/res:variable">
      <xsl:variable name="name" select="@name"/>
      <td>
	<xsl:choose>
	  <xsl:when test="$current/res:binding[@name=$name]">
	    <!-- apply template for the correct value type (bnode, uri, literal) -->
	    <xsl:apply-templates select="$current/res:binding[@name=$name]"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <!-- no binding available for this variable in this solution -->
	  </xsl:otherwise>
	</xsl:choose>
      </td>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="res:bnode">
    <xsl:text>_:</xsl:text>
    <xsl:value-of select="text()"/>
  </xsl:template>

  <xsl:template match="res:uri">
    <!-- Roughly: SELECT ($uri AS ?subject) ?predicate ?object { $uri ?predicate ?object } -->
    <!-- XSLT 2.0
    <xsl:variable name="x"><xsl:value-of select="fn:encode-for-uri(.)"/></xsl:variable>
    -->
    <xsl:variable name="x"><xsl:value-of select="."/></xsl:variable>
    <!--
    <xsl:variable name="query">SELECT%20%28%3C<xsl:value-of select="."/>%3E%20AS%20%3Fsubject%29%20%3Fpredicate%20%3Fobject%20%7B%3C<xsl:value-of select="."/>%3E%20%3Fpredicate%20%3Fobject%20%7D</xsl:variable>
    -->
     <xsl:variable name="query">SELECT%20%28%3C<xsl:value-of select="$x"/>%3E%20AS%20%3Fsubject%29%20%3Fpredicate%20%3Fobject%20%7B%3C<xsl:value-of select="$x"/>%3E%20%3Fpredicate%20%3Fobject%20%7D</xsl:variable>
    <xsl:text>&lt;</xsl:text>
    <a href="?query={$query}&amp;output=xml&amp;stylesheet=%2Fxml-to-html-links.xsl">
    <xsl:value-of select="."/>
    </a>
    <xsl:text>&gt;</xsl:text>
  </xsl:template>

  <xsl:template match="res:literal">
    <xsl:text>"</xsl:text>
    <xsl:value-of select="text()"/>
    <xsl:text>"</xsl:text>

    <xsl:choose>
      <xsl:when test="@datatype">
        <!-- datatyped literal value -->
        ^^&lt;<xsl:value-of select="@datatype"/>&gt;
      </xsl:when>
      <xsl:when test="@xml:lang">
        <!-- lang-string -->
        @<xsl:value-of select="@xml:lang"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="res:sparql">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <head>
	<title>SPARQLer Query Results</title>
	<style>
	  <![CDATA[
	  h1 { font-size: 150% ; }
	  h2 { font-size: 125% ; }
	  table { border-collapse: collapse ; border: 1px solid black ; }
	  td, th
 	  { border: 1px solid black ;
	    padding-left:0.5em; padding-right: 0.5em; 
	    padding-top:0.2ex ; padding-bottom:0.2ex 
	  }
	  ]]>
	</style>
      </head>
      <body>


	<h1>SPARQLer Query Results</h1>

	<xsl:if test="res:head/res:link">
	  <xsl:call-template name="header"/>
	</xsl:if>

	<xsl:choose>
	  <xsl:when test="res:boolean">
	    <xsl:call-template name="boolean-result" />
	  </xsl:when>

	  <xsl:when test="res:results">
	    <xsl:call-template name="vb-result" />
	  </xsl:when>

	</xsl:choose>


      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
