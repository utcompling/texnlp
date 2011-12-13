package texnlp.estimate;

/**
 * Generate contextual predicates for a word.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.4 $, $Date: 2004/12/17 15:08:39 $
 */
public class CCWordContextGenerator extends WordContextGenerator {

    public final String[] getWordContexts(String word) {

        final int wlength = word.length();

        // get the right number of elements to initialize the contexts array
        int numFeatures = 5;
        if (wlength > 2)
            numFeatures += 2 * (Math.min(6, wlength) - 1);

        String[] contexts = new String[numFeatures];

        int index = 0;
        contexts[index++] = "label_feature";

        if (word.indexOf('-') != -1)
            contexts[index++] = "has_dash";

        if (reSimpleNum.matcher(word).find())
            contexts[index++] = "re:has_num";

        if (reAllCaps.matcher(word).find())
            contexts[index++] = "re:has_cap";

        if (wlength > 2) {
            contexts[index++] = "pre1:" + word.substring(0, 1);
            contexts[index++] = "suff1:" + word.substring(wlength - 1);

            if (wlength > 3) {
                contexts[index++] = "pre2:" + word.substring(0, 2);
                contexts[index++] = "suff2:" + word.substring(wlength - 2);

                if (wlength > 4) {
                    contexts[index++] = "pre3:" + word.substring(0, 3);
                    contexts[index++] = "suff3:" + word.substring(wlength - 3);

                    if (wlength > 5) {
                        contexts[index++] = "pre4:" + word.substring(0, 4);
                        contexts[index++] = "suff4:" + word.substring(wlength - 4);

                    }
                }
            }
        }

        return java.util.Arrays.copyOf(contexts, index);
    }

}
