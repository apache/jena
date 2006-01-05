function exchange(id)
    {
    var divA = document.getElementById( id + "A" );
    var divB = document.getElementById( id + "B" );
    var temp = divA.className;
    divA.className = divB.className;
    divB.className = temp;
    }

function setMagicDivs( forA, forB )
    {
    var divs = document.getElementsByTagName( "div" );
    for (i = 0; i < divs.length; i += 1)
        {
        var div = divs[i];
        if (div.id)
            {
            if (div.id.match( /A$/ )) div.className = forA;
            if (div.id.match( /B$/ )) div.className = forB;
            }
        else
            { }
        }
    }
    
function allAsN3()
    { setMagicDivs( "hide", "show" ); }
    
function allAsCondensed()
    { setMagicDivs( "show", "hide" ); }

function printCurrentDisplay()
    { window.print(); }

