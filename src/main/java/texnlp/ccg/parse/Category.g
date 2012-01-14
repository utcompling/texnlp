///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 Jason Baldridge, The University of Texas at Austin
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////
grammar Category;

@header {
///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 Jason Baldridge, The University of Texas at Austin
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////
package texnlp.ccg.parse;

import austinnlp.ccg.*;
}

@lexer::header{
package texnlp.ccg.parse;
}

@members {

    public CategoryParser () {
	this(new CommonTokenStream(new CategoryLexer(new ANTLRStringStream(""))));
    }

    public Cat parse (String catString) {
	setTokenStream(new CommonTokenStream(new CategoryLexer(new ANTLRStringStream(catString))));
	Cat c = null;
	try {
	    c = parseCat();
	} catch (RecognitionException e) {
	    System.out.println("Unable to parse " + catString + " as a category.");
	    System.out.println(e.toString());
	    System.exit(0);
	}
	return c;
    }

    public static void main(String[] args) throws Exception {
	CategoryParser parser = new CategoryParser();
        Cat c1 = parser.parse("(S[c]/S)/NP");
	System.out.println(c1);
        Cat c2 = parser.parse("(S[b]/S[ing])/NP");
	System.out.println(c2);
	System.out.println(c1.equals(c2));
        Cat c3 = parser.parse("(S[c]/S[ing])/NP");
	System.out.println(c3);
	System.out.println(c1.equals(c3));
    }
    
}

parseCat returns [Cat value]
    : c=cat {$value = $c.value;} ;

cat returns [Cat value]
    : res=atom ( SLASH arg=atom)?
        { if ($arg.value != null) 
            $value = new ComplexCat($res.value, new Slash($SLASH.text), $arg.value);
          else 
            $value = $res.value;
        }
    ;

atom returns [Cat value]
    : atomcat=BASESTRING ( '[' feature=BASESTRING ']' )? 
        { 
	    if ($feature == null) 
		$value = new AtomCat($atomcat.text);
	    else
		$value = new AtomCat($atomcat.text, $feature.text);
        }
    | '(' cat ')' { $value = $cat.value; }
    ;


BASESTRING: ( UPPER | LOWER | '.' | ',' | ':' | ';' | '#' | '$')+ ;
fragment UPPER: 'A'..'Z';
fragment LOWER: 'a'..'z';
SLASH: '\\' | '/' ;
