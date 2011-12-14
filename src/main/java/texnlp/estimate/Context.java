package texnlp.estimate;

import gnu.trove.list.TLinkableAdapter;
import gnu.trove.set.hash.THashSet;

import java.util.Arrays;

import texnlp.util.StringUtil;

/**
 * A training context
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.4 $, $Date: 2004/12/17 15:08:39 $
 */
public class Context extends TLinkableAdapter {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int hashcode = 0;
    public String[] contexts;

    public Context(String[] _contexts) {
        contexts = _contexts;
        computeHashCode();
    }

    public Context(THashSet<String> _contexts) {
        contexts = new String[_contexts.size()];
        _contexts.toArray(contexts);
        computeHashCode();
    }

    public final int hashCode() {
        return hashcode;
    }

    private final void computeHashCode() {
        hashcode = Arrays.hashCode(contexts);
    }

    public boolean equals(Object o) {
        if (o instanceof Context) {
            Context c2 = (Context) o;
            if (contexts.length == c2.contexts.length) {
                for (int i = 0; i < contexts.length; i++)
                    if (!contexts[i].equals(c2.contexts[i]))
                        return false;
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return StringUtil.join(contexts);
    }
}
