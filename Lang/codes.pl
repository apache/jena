#!/usr/bin/perl
# http://www.loc.gov/standards/iso639-2/php/English_list.php

# Get content of an element.  Assumes no nesting.
sub term
{
    my $str = $_[0] ;
    $str =~ m!>([^<>]*)<! ;
    $str = $1 ;
    return $str ;
}

# The name of the language as a Java string.
sub termName
{
    my $str = term $_[0] ;
    return "\"$str\"";
}

# The 2-letter code of the language as a Java string or null
sub term2
{
     my $str = term $_[0] ;
     return "null" if ( $str eq '&nbsp;' ) ;
     return "\"$str\"" ;
}

# The 3-letter code of the language as a Java string or null
# Split foms like a/b
sub term3
{
     my $str = term $_[0] ;
     if ( $str =~ m!([^/]*)/([^/]*)! )
     {
        return "\"$1\",\"$2\"" ; ;
     }

     return "\"$str\",\"$str\"" ;
}

# Read everything.
undef $/ ;
$_ = <> ;

# print length($_),"\n" ;
# Find all 5 column table rows.
@rows = (m!<tr[^<>]*>\s*((?:<td[^<>]*>[^<>]*</td>\s*){5})[^<>]*</tr>!sg) ;
# print "rows = $#rows\n" ;

$prev="A" ;
%seen = () ;

for my $r (@rows)
{
    #print "ROW\n" ;
    # Extract the cols from the row.
    my @x = ($r =~ m!(<td[^>]*>[^<]*</td>)!sg ) ;
    #print "Elts: $#x\n" ;
    #print join(':', @x),"\n" ;

    # Extract the name/2-letter code/3-letter codes
    $name = termName $x[0] ;
    $code2 = term2 $x[4] ;
    $code3 = term3 $x[3] ;

    ## Must be unique by 3 first letter 
    $id3 = substr($code3,1, 3) ;
    if ( defined($seen{$id3}) )
    {
	print STDERR "Duplicate: $id3 ($name,$seen{$id3})\n" ;
	next ;
    }
    $seen{$id3} = $name ;


    # First letter after quote
    $name1 = substr($name,1, 1) ;
    if ( $name1 ne $prev )
    {
	print "\n" ;
	$prev = $name1 ;
    }

    if ( $code3 eq '"und","und"' )
    {
	print "        new Iso639($name,$code2,$code3,LT_UNDETERMINED) ;\n" ;
	next ;
    }
    
    # Output ready for including in Iso639.java
    print "        new Iso639($name,$code2,$code3) ;\n" ;
}

