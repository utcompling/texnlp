package texnlp.estimate;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * 
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.4 $, $Date: 2005/03/24 22:04:31 $
 */
public final class FeatureMap extends TObjectIntHashMap<String> {
    private int idCounter;
    private int numLabels;

    public FeatureMap(int _numLabels) {
        idCounter = _numLabels;
        numLabels = _numLabels;
    }

    // This controls when there should not be any new feature indices assigned
    private boolean capped = false;

    public final int getID(String feature) {
        int index = index(feature);

        // check if we've seen this feature before
        if (index < 0) {

            if (capped)
                return -1;

            // The map is not capped, so add the new feature
            int id = idCounter;
            put(feature, id);
            idCounter += numLabels;
            return id;
        }
        else {
            return _values[index];
        }
    }

    public int getNumFeatures() {
        return idCounter;
    }

    // For when we are done observing features and just want to access
    // the features which a model has been trained for
    public void cap() {
        capped = true;
        compact();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        TObjectIntIterator<String> it = iterator();
        for (int i = size(); i-- > 0;) {
            it.advance();
            sb.append(it.key()).append(" -> ").append(it.value());
        }
        return sb.toString();
    }

}
