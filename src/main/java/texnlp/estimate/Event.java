package texnlp.estimate;

import gnu.trove.THashSet;
import gnu.trove.TLinkableAdapter;
import texnlp.util.StringUtil;

/**
 * A training event.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.4 $, $Date: 2004/12/17 15:08:39 $
 */
public class Event extends TLinkableAdapter {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int label;
    public String[] contexts;

    public Event(int _label, String[] _contexts) {
        label = _label;
        contexts = _contexts;
    }

    public Event(int _label, THashSet<String> _contexts) {
        label = _label;
        contexts = new String[_contexts.size()];
        _contexts.toArray(contexts);
    }

    public String toString() {
        return label + ": " + StringUtil.join(contexts);
    }
}
