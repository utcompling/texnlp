package texnlp.estimate;

/**
 * Generate contextual predicates for a state description (eg POS tag).
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.4 $, $Date: 2004/12/17 15:08:39 $
 */
public class StateContextGenerator {
    private String[][] contexts;

    public StateContextGenerator(String[] stateNames) {
        contexts = new String[stateNames.length][4];
        for (int stateID = 0; stateID < stateNames.length; stateID++) {
            String name = stateNames[stateID];
            contexts[stateID][0] = "full:" + name;
            contexts[stateID][1] = "char1:" + name.substring(0, 1);
            contexts[stateID][2] = "uptolast:" + name.substring(0, name.length() - 1);
            contexts[stateID][3] = "lastchar:" + name.substring(name.length() - 1);
        }

    }

    public String[] getStateContexts(int stateID) {
        return contexts[stateID];
    }

}
