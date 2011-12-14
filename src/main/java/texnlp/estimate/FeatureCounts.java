package texnlp.estimate;

import gnu.trove.map.hash.TIntIntHashMap;

/**
 * 
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.2 $, $Date: 2005/01/04 17:43:19 $
 */
public final class FeatureCounts extends TIntIntHashMap {
    private FeatureMap fmap;
    private int numLabels;

    public FeatureCounts(FeatureMap _fmap, int _numLabels) {
        super();
        fmap = _fmap;
        numLabels = _numLabels;
    }

    public void observeFeature(String feature) {
        int id = fmap.getID(feature);

        if (id == -1) {
            return;
        }
        else {
            if (!increment(id))
                put(id, 1);
        }
    }

    public String toString(int trueLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append(numLabels).append('\n');

        final int size = size();

        int[] keys = keys();
        int[] vals = new int[size];
        for (int i = 0; i < size; i++)
            vals[i] = get(keys[i]);

        for (int label = 0; label < numLabels; label++) {
            if (label == trueLabel)
                sb.append("1 ");
            else
                sb.append("0 ");

            sb.append(size);

            for (int i = 0; i < size; i++)
                sb.append(' ').append(keys[i] - label).append(' ').append(vals[i]);
            sb.append('\n');
        }

        return sb.toString();
    }

    public String toString(int[] labelCounts) {
        StringBuilder sb = new StringBuilder();
        sb.append(numLabels).append('\n');

        final int size = size();

        int[] keys = keys();
        int[] vals = new int[size];
        for (int i = 0; i < size; i++)
            vals[i] = get(keys[i]);

        for (int label = 0; label < numLabels; label++) {
            sb.append(labelCounts[label]).append(' ');
            sb.append(size);
            for (int i = 0; i < size; i++)
                sb.append(' ').append(keys[i] - label).append(' ').append(vals[i]);
            sb.append('\n');
        }

        return sb.toString();
    }

}
