/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package PddOriginblock;

import java.util.ArrayList;

// Referenced classes of package diversity:
//            Rule

public class EffectiveRuleSet
{

    public EffectiveRuleSet()
    {
        eRules = new ArrayList();
    }

    public int size()
    {
        return eRules.size();
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public Rule get(int i)
    {
        return (Rule)eRules.get(i);
    }

    public void add(Rule r)
    {
        eRules.add(r);
    }

    public void print()
    {
        for(int i = 0; i < size(); i++)
            get(i).print();

    }

    ArrayList eRules;
}
