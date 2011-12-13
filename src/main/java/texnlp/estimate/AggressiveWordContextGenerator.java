package texnlp.estimate;


/**
 * Generate contextual predicates for a word.
 *
 * @author  Jason Baldridge
 * @version $Revision: 1.4 $, $Date: 2004/12/17 15:08:39 $
 */
public class AggressiveWordContextGenerator extends WordContextGenerator {

    public final String[] getWordContexts (String word) {

	final int wlength = word.length();

	// get the right number of elements to initialize the contexts array
	int numFeatures = 20;
	if (wlength > 2)
	    numFeatures += 2*(Math.min(6,wlength)-1);

	String[] contexts = new String[numFeatures];
	
	int index = 0;
	contexts[index++] = "label_feature";
	contexts[index++] = "word:"+word;
	contexts[index++] = "uc:"+word.toUpperCase();
	contexts[index++] = "lc:"+word.toLowerCase();

	char first = word.charAt(0);
	if (wlength == 1
	    && (first == '.'
		|| first == ','
		|| first == '$'
		|| first == ':'
		|| first == ';'
		|| first == '?'
		|| first == '!'
		|| first == '#'
		|| first == '%'
		|| first == '@'
		|| first == '&'
		|| first == '*'
		|| first == '^'
		|| first == '('
		|| first == ')'
		|| first == '{'
		|| first == '}'
		|| first == '+'
		|| first == '='
		|| first == '-'
		|| first == '_'
		|| first == '['
		|| first == ']'))
	    contexts[index++] = "is_symbol";

	if (word.indexOf('-') != -1) 
	    contexts[index++] = "has_dash";

	if (reSimpleNum.matcher(word).find())
	    contexts[index++] = "re:has_num";

	if (reAllCaps.matcher(word).find())
	    contexts[index++] = "re:has_cap";
	
	if (word.indexOf('_') != -1) 
	    contexts[index++] = "has_underscore";

	if (word.indexOf('.') != -1) 
	    contexts[index++] = "has_period";
	
	if (word.indexOf('&') != -1) 
	    contexts[index++] = "has_ampersand";
	
	if (word.indexOf(',') != -1) 
	    contexts[index++] = "has_comma";

	if (word.indexOf('\'') != -1) 
	    contexts[index++] = "has_apostrophe";

	if (reSimpleNum.matcher(word).matches())
	    contexts[index++] = "re:simple_num";

	if (reDigPeriodDig.matcher(word).matches())
	    contexts[index++] = "re:D.D";

	if (reDigComDig.matcher(word).matches())
	    contexts[index++] = "re:D,D";

	if (reAllCaps.matcher(word).matches())
	    contexts[index++] = "re:AllCaps";

	if (reAbbrev.matcher(word).matches())
	    contexts[index++] = "re:Abbrev";

	if (reXxx.matcher(word).matches())
	    contexts[index++] = "re:Xxx";

	if (rexxx.matcher(word).matches())
	    contexts[index++] = "re:xxx";

	if (reWordDashWord.matcher(word).matches())
	    contexts[index++] = "re:xx-xx";

	if (reWordUnderscoreWord.matcher(word).matches())
	    contexts[index++] = "re:xx_xx";

	if (wlength > 2) {
	    contexts[index++] = "pre1:"+word.substring(0,1);
	    contexts[index++] = "suff1:"+word.substring(wlength-1);
	
	    if (wlength > 3) {
		contexts[index++] = "pre2:"+word.substring(0,2);
		contexts[index++] = "suff2:"+word.substring(wlength-2);
		
		if (wlength > 4) {
		    contexts[index++] = "pre3:"+word.substring(0,3);
		    contexts[index++] = "suff3:"+word.substring(wlength-3);
		    
		    if (wlength > 5) {
		    	contexts[index++] = "pre4:"+word.substring(0,4);
		    	contexts[index++] = "suff4:"+word.substring(wlength-4);
		    }
		}
	    }
	}

	//System.out.println(word + " -- " + StringUtil.join(java.util.Arrays.copyOf(contexts,index)));

	return java.util.Arrays.copyOf(contexts,index);
    }


}
