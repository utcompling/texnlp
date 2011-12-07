package texnlp.estimate;

import java.util.regex.*;

/**
 * Generate contextual predicates for a word.
 *
 * @author  Jason Baldridge
 * @version $Revision: 1.4 $, $Date: 2004/12/17 15:08:39 $
 */
public abstract class WordContextGenerator {

    protected static Pattern reSimpleNum = Pattern.compile("\\d+"); // 42
    protected static Pattern reDigPeriodDig = Pattern.compile("\\d+\\.\\d+"); // 4.065
    protected static Pattern reDigComDig = Pattern.compile("\\d+,\\d+"); // 2,302
    protected static Pattern reAllCaps = Pattern.compile("[A-Z]+"); // IBM
    protected static Pattern reXxx = Pattern.compile("[A-Z][a-z]+"); // Chicago
    protected static Pattern rexxx = Pattern.compile("[a-z]+"); // blue
    protected static Pattern reWordDashWord = Pattern.compile("\\w+-\\w+"); // near-record
    protected static Pattern reWordUnderscoreWord = Pattern.compile("\\w+_\\w+"); // near_record
    protected static Pattern reAbbrev = Pattern.compile("(([A-Z]\\.|)+|[A-Za-z]\\.)"); // U.S.A. or Oct.
	

    public abstract String[] getWordContexts (String word);


}
