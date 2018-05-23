/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package PddOriginblock;

import java.io.PrintStream;
import java.util.ArrayList;

// Referenced classes of package diversity:
//            EffectiveRuleSet, Rule

public class ESetSequence
{

    public ESetSequence()
    {
        eSetSeq = new ArrayList();
    }

    public Rule get(int i, int j)
    {
        return ((EffectiveRuleSet)eSetSeq.get(i)).get(j);
    }

    public EffectiveRuleSet get(int i)
    {
        return (EffectiveRuleSet)eSetSeq.get(i);
    }

    public void add(EffectiveRuleSet ers)
    {
        eSetSeq.add(ers);
    }

    public void remove(int i)
    {
        eSetSeq.remove(i);
    }

    public int size()
    {
        return eSetSeq.size();
    }

    public int width(int i)
    {
        return ((EffectiveRuleSet)eSetSeq.get(i)).size();
    }

    public void print()
    {
        for(int i = 0; i < size(); i++)
        {
            System.out.println((new StringBuilder("E--")).append(i).toString());
            get(i).print();
        }

    }

    ArrayList eSetSeq;
}
