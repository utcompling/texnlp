package texnlp.estimate;

import gnu.trove.set.hash.THashSet;

/**
 * Generate contextual predicates for a word.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.4 $, $Date: 2004/12/17 15:08:39 $
 */
public class SimpleWordContextGenerator extends WordContextGenerator {

    public final String[] getWordContexts(String word) {

        THashSet<String> contexts = new THashSet<String>(20);

        contexts.add("word:" + word);

        String[] contextsSA = new String[contexts.size()];
        contexts.toArray(contextsSA);

        return contextsSA;
    }

}
