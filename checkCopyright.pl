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

    if ( m!Copyright.*200[^9] Hewlett-Packard! )
    {
 	print "$file: Not 2009\n" ;
    }

    unless ( m/THIS SOFTWARE IS PROVIDED BY THE AUTHOR/ )
    {
	print "$file: no copyright notice\n" ;
    }
}

sub setExclude
{
    $exclude{'src-dev/dev/Dev.java'} = 1 ;
    $exclude{'src-lib/lib/MD5.java'} = 1 ;
    $exclude{'src-lib/lib/Base64.java'} = 1 ;
}
