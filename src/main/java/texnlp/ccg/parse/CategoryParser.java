// $ANTLR 3.0.1 Category.g 2007-11-15 16:40:06

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

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import texnlp.ccg.AtomCat;
import texnlp.ccg.Cat;
import texnlp.ccg.ComplexCat;
import texnlp.ccg.Slash;

public class CategoryParser extends Parser {
    public static final String[] tokenNames = new String[] { "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SLASH",
            "BASESTRING", "UPPER", "LOWER", "'['", "']'", "'('", "')'" };
    public static final int BASESTRING = 5;
    public static final int SLASH = 4;
    public static final int LOWER = 7;
    public static final int EOF = -1;
    public static final int UPPER = 6;

    public CategoryParser(TokenStream input) {
        super(input);
    }

    public String[] getTokenNames() {
        return tokenNames;
    }

    public String getGrammarFileName() {
        return "Category.g";
    }

    public CategoryParser() {
        this(new CommonTokenStream(new CategoryLexer(new ANTLRStringStream(""))));
    }

    public Cat parse(String catString) {
        setTokenStream(new CommonTokenStream(new CategoryLexer(new ANTLRStringStream(catString))));
        Cat c = null;
        try {
            c = parseCat();
        }
        catch (RecognitionException e) {
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

    // $ANTLR start parseCat
    // Category.g:80:1: parseCat returns [Cat value] : c= cat ;
    public final Cat parseCat() throws RecognitionException {
        Cat value = null;

        Cat c = null;

        try {
            // Category.g:81:5: (c= cat )
            // Category.g:81:7: c= cat
            {
                pushFollow(FOLLOW_cat_in_parseCat57);
                c = cat();
                _fsp--;

                value = c;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        }
        finally {
        }
        return value;
    }

    // $ANTLR end parseCat

    // $ANTLR start cat
    // Category.g:83:1: cat returns [Cat value] : res= atom ( SLASH arg= atom )?
    // ;
    public final Cat cat() throws RecognitionException {
        Cat value = null;

        Token SLASH1 = null;
        Cat res = null;

        Cat arg = null;

        try {
            // Category.g:84:5: (res= atom ( SLASH arg= atom )? )
            // Category.g:84:7: res= atom ( SLASH arg= atom )?
            {
                pushFollow(FOLLOW_atom_in_cat78);
                res = atom();
                _fsp--;

                // Category.g:84:16: ( SLASH arg= atom )?
                int alt1 = 2;
                int LA1_0 = input.LA(1);

                if ((LA1_0 == SLASH)) {
                    alt1 = 1;
                }
                switch (alt1) {
                case 1:
                // Category.g:84:18: SLASH arg= atom
                {
                    SLASH1 = (Token) input.LT(1);
                    match(input, SLASH, FOLLOW_SLASH_in_cat82);
                    pushFollow(FOLLOW_atom_in_cat86);
                    arg = atom();
                    _fsp--;

                }
                    break;

                }

                if (arg != null)
                    value = new ComplexCat(res, new Slash(SLASH1.getText()), arg);
                else
                    value = res;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        }
        finally {
        }
        return value;
    }

    // $ANTLR end cat

    // $ANTLR start atom
    // Category.g:92:1: atom returns [Cat value] : (atomcat= BASESTRING ( '['
    // feature= BASESTRING ']' )? | '(' cat ')' );
    public final Cat atom() throws RecognitionException {
        Cat value = null;

        Token atomcat = null;
        Token feature = null;
        Cat cat2 = null;

        try {
            // Category.g:93:5: (atomcat= BASESTRING ( '[' feature= BASESTRING
            // ']' )? | '(' cat ')' )
            int alt3 = 2;
            int LA3_0 = input.LA(1);

            if ((LA3_0 == BASESTRING)) {
                alt3 = 1;
            }
            else if ((LA3_0 == 10)) {
                alt3 = 2;
            }
            else {
                NoViableAltException nvae = new NoViableAltException(
                        "92:1: atom returns [Cat value] : (atomcat= BASESTRING ( '[' feature= BASESTRING ']' )? | '(' cat ')' );",
                        3, 0, input);

                throw nvae;
            }
            switch (alt3) {
            case 1:
            // Category.g:93:7: atomcat= BASESTRING ( '[' feature= BASESTRING
            // ']' )?
            {
                atomcat = (Token) input.LT(1);
                match(input, BASESTRING, FOLLOW_BASESTRING_in_atom121);
                // Category.g:93:26: ( '[' feature= BASESTRING ']' )?
                int alt2 = 2;
                int LA2_0 = input.LA(1);

                if ((LA2_0 == 8)) {
                    alt2 = 1;
                }
                switch (alt2) {
                case 1:
                // Category.g:93:28: '[' feature= BASESTRING ']'
                {
                    match(input, 8, FOLLOW_8_in_atom125);
                    feature = (Token) input.LT(1);
                    match(input, BASESTRING, FOLLOW_BASESTRING_in_atom129);
                    match(input, 9, FOLLOW_9_in_atom131);

                }
                    break;

                }

                if (feature == null)
                    value = new AtomCat(atomcat.getText());
                else
                    value = new AtomCat(atomcat.getText(), feature.getText());

            }
                break;
            case 2:
            // Category.g:100:7: '(' cat ')'
            {
                match(input, 10, FOLLOW_10_in_atom153);
                pushFollow(FOLLOW_cat_in_atom155);
                cat2 = cat();
                _fsp--;

                match(input, 11, FOLLOW_11_in_atom157);
                value = cat2;

            }
                break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        }
        finally {
        }
        return value;
    }

    // $ANTLR end atom

    public static final BitSet FOLLOW_cat_in_parseCat57 = new BitSet(new long[] { 0x0000000000000002L });
    public static final BitSet FOLLOW_atom_in_cat78 = new BitSet(new long[] { 0x0000000000000012L });
    public static final BitSet FOLLOW_SLASH_in_cat82 = new BitSet(new long[] { 0x0000000000000420L });
    public static final BitSet FOLLOW_atom_in_cat86 = new BitSet(new long[] { 0x0000000000000002L });
    public static final BitSet FOLLOW_BASESTRING_in_atom121 = new BitSet(new long[] { 0x0000000000000102L });
    public static final BitSet FOLLOW_8_in_atom125 = new BitSet(new long[] { 0x0000000000000020L });
    public static final BitSet FOLLOW_BASESTRING_in_atom129 = new BitSet(new long[] { 0x0000000000000200L });
    public static final BitSet FOLLOW_9_in_atom131 = new BitSet(new long[] { 0x0000000000000002L });
    public static final BitSet FOLLOW_10_in_atom153 = new BitSet(new long[] { 0x0000000000000420L });
    public static final BitSet FOLLOW_cat_in_atom155 = new BitSet(new long[] { 0x0000000000000800L });
    public static final BitSet FOLLOW_11_in_atom157 = new BitSet(new long[] { 0x0000000000000002L });

}