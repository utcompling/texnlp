// $ANTLR 3.0.1 Category.g 2007-11-15 16:40:06

package texnlp.ccg.parse;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;

public class CategoryLexer extends Lexer {
    public static final int BASESTRING = 5;
    public static final int SLASH = 4;
    public static final int T10 = 10;
    public static final int T11 = 11;
    public static final int T8 = 8;
    public static final int LOWER = 7;
    public static final int T9 = 9;
    public static final int Tokens = 12;
    public static final int EOF = -1;
    public static final int UPPER = 6;

    public CategoryLexer() {
        ;
    }

    public CategoryLexer(CharStream input) {
        super(input);
    }

    public String getGrammarFileName() {
        return "Category.g";
    }

    // $ANTLR start T8
    public final void mT8() throws RecognitionException {
        try {
            int _type = T8;
            // Category.g:6:4: ( '[' )
            // Category.g:6:6: '['
            {
                match('[');
            }

            this.type = _type;
        }
        finally {
        }
    }

    // $ANTLR end T8

    // $ANTLR start T9
    public final void mT9() throws RecognitionException {
        try {
            int _type = T9;
            // Category.g:7:4: ( ']' )
            // Category.g:7:6: ']'
            {
                match(']');
            }

            this.type = _type;
        }
        finally {
        }
    }

    // $ANTLR end T9

    // $ANTLR start T10
    public final void mT10() throws RecognitionException {
        try {
            int _type = T10;
            // Category.g:8:5: ( '(' )
            // Category.g:8:7: '('
            {
                match('(');
            }

            this.type = _type;
        }
        finally {
        }
    }

    // $ANTLR end T10

    // $ANTLR start T11
    public final void mT11() throws RecognitionException {
        try {
            int _type = T11;
            // Category.g:9:5: ( ')' )
            // Category.g:9:7: ')'
            {
                match(')');
            }

            this.type = _type;
        }
        finally {
        }
    }

    // $ANTLR end T11

    // $ANTLR start BASESTRING
    public final void mBASESTRING() throws RecognitionException {
        try {
            int _type = BASESTRING;
            // Category.g:104:11: ( ( UPPER | LOWER | '.' | ',' | ':' | ';' )+ )
            // Category.g:104:13: ( UPPER | LOWER | '.' | ',' | ':' | ';' )+
            {
                // Category.g:104:13: ( UPPER | LOWER | '.' | ',' | ':' | ';' )+
                int cnt1 = 0;
                loop1: do {
                    int alt1 = 2;
                    int LA1_0 = input.LA(1);

                    if ((LA1_0 == ',' || LA1_0 == '.' || (LA1_0 >= ':' && LA1_0 <= ';')
                            || (LA1_0 >= 'A' && LA1_0 <= 'Z') || (LA1_0 >= 'a' && LA1_0 <= 'z'))) {
                        alt1 = 1;
                    }

                    switch (alt1) {
                    case 1:
                    // Category.g:
                    {
                        if (input.LA(1) == ',' || input.LA(1) == '.' || (input.LA(1) >= ':' && input.LA(1) <= ';')
                                || (input.LA(1) >= 'A' && input.LA(1) <= 'Z')
                                || (input.LA(1) >= 'a' && input.LA(1) <= 'z')) {
                            input.consume();

                        }
                        else {
                            MismatchedSetException mse = new MismatchedSetException(null, input);
                            recover(mse);
                            throw mse;
                        }

                    }
                        break;

                    default:
                        if (cnt1 >= 1)
                            break loop1;
                        EarlyExitException eee = new EarlyExitException(1, input);
                        throw eee;
                    }
                    cnt1++;
                } while (true);

            }

            this.type = _type;
        }
        finally {
        }
    }

    // $ANTLR end BASESTRING

    // $ANTLR start UPPER
    public final void mUPPER() throws RecognitionException {
        try {
            // Category.g:105:15: ( 'A' .. 'Z' )
            // Category.g:105:17: 'A' .. 'Z'
            {
                matchRange('A', 'Z');

            }

        }
        finally {
        }
    }

    // $ANTLR end UPPER

    // $ANTLR start LOWER
    public final void mLOWER() throws RecognitionException {
        try {
            // Category.g:106:15: ( 'a' .. 'z' )
            // Category.g:106:17: 'a' .. 'z'
            {
                matchRange('a', 'z');

            }

        }
        finally {
        }
    }

    // $ANTLR end LOWER

    // $ANTLR start SLASH
    public final void mSLASH() throws RecognitionException {
        try {
            int _type = SLASH;
            // Category.g:107:6: ( '\\\\' | '/' )
            // Category.g:
            {
                if (input.LA(1) == '/' || input.LA(1) == '\\') {
                    input.consume();

                }
                else {
                    MismatchedSetException mse = new MismatchedSetException(null, input);
                    recover(mse);
                    throw mse;
                }

            }

            this.type = _type;
        }
        finally {
        }
    }

    // $ANTLR end SLASH

    public void mTokens() throws RecognitionException {
        // Category.g:1:8: ( T8 | T9 | T10 | T11 | BASESTRING | SLASH )
        int alt2 = 6;
        switch (input.LA(1)) {
        case '[': {
            alt2 = 1;
        }
            break;
        case ']': {
            alt2 = 2;
        }
            break;
        case '(': {
            alt2 = 3;
        }
            break;
        case ')': {
            alt2 = 4;
        }
            break;
        case ',':
        case '.':
        case ':':
        case ';':
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z': {
            alt2 = 5;
        }
            break;
        case '/':
        case '\\': {
            alt2 = 6;
        }
            break;
        default:
            NoViableAltException nvae = new NoViableAltException(
                    "1:1: Tokens : ( T8 | T9 | T10 | T11 | BASESTRING | SLASH );", 2, 0, input);

            throw nvae;
        }

        switch (alt2) {
        case 1:
        // Category.g:1:10: T8
        {
            mT8();

        }
            break;
        case 2:
        // Category.g:1:13: T9
        {
            mT9();

        }
            break;
        case 3:
        // Category.g:1:16: T10
        {
            mT10();

        }
            break;
        case 4:
        // Category.g:1:20: T11
        {
            mT11();

        }
            break;
        case 5:
        // Category.g:1:24: BASESTRING
        {
            mBASESTRING();

        }
            break;
        case 6:
        // Category.g:1:35: SLASH
        {
            mSLASH();

        }
            break;

        }

    }

}