#!/bin/perl

# Run with: find src -name \*.java | xargs perl checkCopyright.pl
# Check source code

&setExclude ;

undef $/ ;

#checkOne('copyright.txt') ;


while(defined($file = shift @ARGV))
{
    # Excludes
    next if ( defined($exclude{$file}) ) ;
    &checkOne($file) ;
}


sub checkOne
{
    my $file = @_[0] ;
    open(FILE, "<$file") || die ;
    binmode FILE ;
    $_ = <FILE> ;

    if ( m/Copyright.*Hewlett-Packard Company/ )
    {
	print "$file: old style copyright\n" ;
	next ;
    }

    unless ( m!Copyright\s.*Hewlett-Packard Development Company, LP! )
    {
 	print "$file: no copyright line\n" ;
    }

    if ( m!Copyright\s*Hewlett-Packard! )
    {
 	print "$file: No date or wrong order\n" ;
    }

    if ( m!Copyright.*200[^8] Hewlett-Packard! )
    {
 	print "$file: Not 2008\n" ;
    }

    unless ( m/THIS SOFTWARE IS PROVIDED BY THE AUTHOR/ )
    {
	print "$file: no copyright notice\n" ;
    }
}

sub setExclude
{
    # Mostly, these files are the output of JavaCC
    $exclude{'src/com/hp/hpl/jena/n3/N3AntlrLexer.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/N3AntlrLexer.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/N3AntlrLexer.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/N3AntlrParser.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/N3AntlrParser.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/N3AntlrParser.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/N3AntlrParserTokenTypes.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/N3AntlrParserTokenTypes.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/N3AntlrParserTokenTypes.java'} = 1 ;

    $exclude{'src/com/hp/hpl/jena/n3/turtle/parser/JavaCharStream.java'}= 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/parser/ParseException.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/parser/Token.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/parser/TokenMgrError.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/parser/TurtleParser.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/parser/TurtleParser.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/parser/TurtleParserConstants.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/parser/TurtleParserConstants.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/parser/TurtleParserTokenManager.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/parser/TurtleParserTokenManager.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/test/TurtleTestVocab.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/n3/turtle/test/TurtleTestVocab.java'} = 1 ;

    $exclude{'src/com/hp/hpl/jena/rdql/parser/JavaCharStream.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/rdql/parser/ParseException.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/rdql/parser/RDQLParser.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/rdql/parser/RDQLParserConstants.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/rdql/parser/RDQLParserTokenManager.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/rdql/parser/RDQLParserTreeConstants.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/rdql/parser/RDQLParserTreeConstants.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/rdql/parser/RDQLParserTreeConstants.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/rdql/parser/Token.java'} = 1 ;
    $exclude{'src/com/hp/hpl/jena/rdql/parser/TokenMgrError.java'} = 1 ;

    $exclude{'src/com/hp/hpl/jena/shared/uuid/MD5.java'} = 1 ;
}
