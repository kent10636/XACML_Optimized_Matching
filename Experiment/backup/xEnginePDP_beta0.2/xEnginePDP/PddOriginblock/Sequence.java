/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package PddOriginblock;

import java.io.*;
import java.util.*;

// Referenced classes of package diversity:
//            ESetSequence, Rule, Edge, Interval, 
//            Node, EffectiveRuleSet

public class Sequence
{
    private ESetSequence eSeq;
    private Rule currentRule;
    private ArrayList<Rule> _rules;
    private ArrayList<Rule> _convertedRules;
    private int _ruleDim;
    public long _range[];
    protected Node _root;
    
    public Sequence()
    {
        _ruleDim = 3;
        _range = new long[_ruleDim + 1];
        eSeq = new ESetSequence();
        currentRule = new Rule();
        _rules = new ArrayList<Rule>();
        _convertedRules = new ArrayList<Rule>();
        _range[0] = 0L;
        _range[1] = 0L;
        _range[2] = 0L;
    }

    public void CreateByFile_for_acl_query(String fileName)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            for(String line = br.readLine(); line != null; line = br.readLine())
            {
                String result[] = line.split("[\\(\\)\\,\\-\\>]");
                Rule r = new Rule();
                long a1 = Long.parseLong(result[1]);
                long a2 = Long.parseLong(result[2]);
                if(a2 >= _range[0])
                    _range[0] = a2 + 1L;
                r.add(new Edge(0, new Interval(a1, a2)));
                long b1 = Long.parseLong(result[4]);
                long b2 = Long.parseLong(result[5]);
                if(b2 >= _range[1])
                    _range[1] = b2 + 1L;
                r.add(new Edge(1, new Interval(b1, b2)));
                long c1 = Long.parseLong(result[7]);
                long c2 = Long.parseLong(result[8]);
                if(c2 >= _range[2])
                    _range[2] = c2 + 1L;
                r.add(new Edge(2, new Interval(c1, c2)));
                r.setDecision(-1);
                add(r);
            }

            br.close();
        }
        catch(FileNotFoundException e)
        {
            System.err.println((new StringBuilder("Alex: I could not open ")).append(fileName).append("!").toString());
        }
        catch(IOException e)
        {
            System.err.println((new StringBuilder("Alex: File operations on ")).append(fileName).append("failed!").toString());
        }
    }

    public void CreateByFile_for_original_acl(String fileName)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            Rule r;
            for(String line = br.readLine(); line != null; line = br.readLine())
            {
                String result[] = line.split("[\\[\\]\\^\\(\\)\\,\\-\\>]");
                r = new Rule();
                long a1 = Long.parseLong(result[1]);
                long a2 = Long.parseLong(result[2]);
                if(a2 >= _range[0])
                    _range[0] = a2 + 1L;
                r.add(new Edge(0, new Interval(a1, a2)));
                long b1 = Long.parseLong(result[5]);
                long b2 = Long.parseLong(result[6]);
                if(b2 >= _range[1])
                    _range[1] = b2 + 1L;
                r.add(new Edge(1, new Interval(b1, b2)));
                long c1 = Long.parseLong(result[9]);
                long c2 = Long.parseLong(result[10]);
                if(c2 >= _range[2])
                    _range[2] = c2 + 1L;
                r.add(new Edge(2, new Interval(c1, c2)));
                int action = Integer.parseInt(result[13]);
                r.setDecision(action);
                add(r);
            }

            r = new Rule();
            for(int j = 0; j < _ruleDim; j++)
                r.add(new Edge(j, new Interval(0L, _range[j] - 1L)));

            r.setDecision(0);
            add(r);
            br.close();
        }
        catch(FileNotFoundException e)
        {
            System.err.println((new StringBuilder("Alex: I could not open ")).append(fileName).append("!").toString());
        }
        catch(IOException e)
        {
            System.err.println((new StringBuilder("Alex: File operations on ")).append(fileName).append("failed!").toString());
        }
    }

    public void createByFile_for_xacml(String fileName)
    {
    	//String tmps = "[1,1]^[1,2]^[1,2]^->1,[1 2 3]";
    	//String tmpresult[] = tmps.split("[\\[\\]\\^\\(\\)\\,\\-\\>\\,\\[\\s\\]]");
    	
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            Rule r;
            for(String line = br.readLine(); line != null; line = br.readLine())
            {
                String result[] = line.split("[\\[\\]\\^\\(\\)\\,\\-\\>\\,\\[\\s\\]]");
                r = new Rule();
                long a1 = Long.parseLong(result[1]);
                long a2 = Long.parseLong(result[2]);
                if(a2 >= _range[0])
                    _range[0] = a2 + 1L;
                r.add(new Edge(0, new Interval(a1, a2)));
                long b1 = Long.parseLong(result[5]);
                long b2 = Long.parseLong(result[6]);
                if(b2 >= _range[1])
                    _range[1] = b2 + 1L;
                r.add(new Edge(1, new Interval(b1, b2)));
                long c1 = Long.parseLong(result[9]);
                long c2 = Long.parseLong(result[10]);
                if(c2 >= _range[2])
                    _range[2] = c2 + 1L;
                r.add(new Edge(2, new Interval(c1, c2)));
                int action = Integer.parseInt(result[14]);
                r.setDecision(action);
                
                int num,dec;
                for (int i=16; i<result.length;i++)
                {              
                	num = Integer.parseInt(result[i++]);
                	dec = Integer.parseInt(result[i]);
                	Integer org[] = {num, dec};
                	r.addOrigin(org);
                }
                add(r);
            }
            r = new Rule();
            for(int j = 0; j < _ruleDim; j++)
                r.add(new Edge(j, new Interval(0L, _range[j] - 1L)));

            r.setDecision(-1);
            r.setOrigin(-1, -1);
            add(r);
            //r.print();
            br.close();
        }
        catch(FileNotFoundException e)
        {
            System.err.println((new StringBuilder("Alex: I could not open ")).append(fileName).append("!").toString());
        }
        catch(IOException e)
        {
            System.err.println((new StringBuilder("Alex: File operations on ")).append(fileName).append("failed!").toString());
        }
    }

    
    public void CreateByFile_for_acl(String fileName)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            Rule r;
            for(String line = br.readLine(); line != null; line = br.readLine())
            {
                String result[] = line.split("[\\(\\)\\,\\-\\>]");
                r = new Rule();
                long a1 = Long.parseLong(result[1]);
                long a2 = Long.parseLong(result[2]);
                if(a2 >= _range[0])
                    _range[0] = a2 + 1L;
                r.add(new Edge(0, new Interval(a1, a2)));
                long b1 = Long.parseLong(result[4]);
                long b2 = Long.parseLong(result[5]);
                if(b2 >= _range[1])
                    _range[1] = b2 + 1L;
                r.add(new Edge(1, new Interval(b1, b2)));
                long c1 = Long.parseLong(result[7]);
                long c2 = Long.parseLong(result[8]);
                if(c2 >= _range[2])
                    _range[2] = c2 + 1L;
                r.add(new Edge(2, new Interval(c1, c2)));
                int action = Integer.parseInt(result[12]);
                r.setDecision(action);
                add(r);
            }

            r = new Rule();
            for(int j = 0; j < _ruleDim; j++)
                r.add(new Edge(j, new Interval(0L, _range[j] - 1L)));

            r.setDecision(0);
            add(r);
            br.close();
        }
        catch(FileNotFoundException e)
        {
            System.err.println((new StringBuilder("Alex: I could not open ")).append(fileName).append("!").toString());
        }
        catch(IOException e)
        {
            System.err.println((new StringBuilder("Alex: File operations on ")).append(fileName).append("failed!").toString());
        }
    }

    public void CreateByFile(String fileName)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            Rule r;
            for(String line = br.readLine(); line != null; line = br.readLine())
            {
                String result[] = line.split("[\\(\\)\\,\\-\\>]");
                r = new Rule();
                long sip_low = Long.parseLong(result[7]);
                long sip_high = Long.parseLong(result[8]);
                r.add(new Edge(0, new Interval(sip_low, sip_high)));
                long dip_low = Long.parseLong(result[4]);
                long dip_high = Long.parseLong(result[5]);
                r.add(new Edge(1, new Interval(dip_low, dip_high)));
                long sport_low = Integer.parseInt(result[10]);
                long sport_high = Integer.parseInt(result[11]);
                r.add(new Edge(2, new Interval(sport_low, sport_high)));
                long dport_low = Integer.parseInt(result[13]);
                long dport_high = Integer.parseInt(result[14]);
                r.add(new Edge(3, new Interval(dport_low, dport_high)));
                long ptype1 = Integer.parseInt(result[1]);
                long ptype2 = Integer.parseInt(result[2]);
                r.add(new Edge(4, new Interval(ptype1, ptype2)));
                int action = Integer.parseInt(result[18]);
                r.setDecision(action);
                add(r);
            }

            r = new Rule();
            for(int j = 0; j < _ruleDim; j++)
                r.add(new Edge(j, new Interval(0L, _range[j] - 1L)));

            r.setDecision(0);
            add(r);
            br.close();
        }
        catch(FileNotFoundException e)
        {
            System.err.println((new StringBuilder("Alex: I could not open ")).append(fileName).append("!").toString());
        }
        catch(IOException e)
        {
            System.err.println((new StringBuilder("Alex: File operations on ")).append(fileName).append("failed!").toString());
        }
    }

    static String IPv4String(long address)
    {
        StringBuffer buf = new StringBuffer();
        buf.append((int)(address >>> 24 & 255L)).append('.');
        buf.append((int)(address >>> 16 & 255L)).append('.');
        buf.append((int)(address >>> 8 & 255L)).append('.');
        buf.append((int)(address & 255L));
        return buf.toString();
    }

    public static void sort(int len, int array[])
    {
        for(int i = 1; i < len; i++)
        {
            for(int j = i; j > 0; j--)
            {
                if(array[j] >= array[j - 1])
                    break;
                int temp = array[j];
                array[j] = array[j - 1];
                array[j - 1] = temp;
            }
        }
    }

    public void setRules(ArrayList<Rule> ruleList)
    {
    	_rules.clear();
    	_rules.addAll(ruleList);
    }
    
    public Rule get(int i)
    {
        return (Rule)_rules.get(i);
    }

    public void add(Rule r)
    {
        _rules.add(r);
    }

    public int size()
    {
        return _rules.size();
    }

    public void print()
    {
        for(int i = 0; i < size(); i++)
        {
            System.out.print((new StringBuilder("Rule ")).append(i).append(":").toString());
            get(i).print();
        }

        System.out.print((new StringBuilder("rule number is:")).append(size()).toString());
    }

    public void print_file(PrintStream out)
    {
    	for(int i = 0; i < size(); i++)        
    	{        
    		out.print((new StringBuilder("Rule ")).append(i).append(":").toString());            
    		get(i).print_file(out);            
    	}
    	out.print((new StringBuilder("rule number is:")).append(size()).toString());
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public Node buildFDD()
    {
        if(isEmpty()) return null;
        
        Node root = get(0).buildChain(0);
        for(int i = 1; i < _rules.size(); i++)
            root.appendAtomicRule(get(i), 0);
        
        _root = root;
        return root;
    }

    public Node buildAllMatchFDD(String alg){
	    if( isEmpty() ) return null;

	    Node root = get(0).buildChain(0);
	    for( int i=1; i<_rules.size(); i++ )
	    {
	    	//root.appendAtomicRule( get(i), 0 );
	    	root.allMatchAppendAtomicRule(get(i),0, alg);
	    	//	System.out.println("finished "+i+" rules");
	    }

	    _root = root;
	    return root;
    }
    
    public ArrayList<Rule> getFirstMatchRules()
    {
    	return _convertedRules;
    }
    
    public void generateRules(Node node, Rule tmpRule, int level)
    {
    	for (int i=0; i<node.getChildrenNum(); i++)
    	{
    		Node n = node.getChild(i);
    		tmpRule.set( level, n.getInEdge() );
    		if(n.isLeaf())
    		{
    			tmpRule.setDecision( n.getLabel() );//need to change
    			tmpRule.setOrigins(n.getOrigins());
    			Rule newRule=new Rule(tmpRule);
    			_convertedRules.add(newRule);
    		}
    		else
    		{
    			generateRules(n,tmpRule, level+1);
    		}
    	}
    }
    
    public boolean isEquivalent(Sequence seq, long range0, long range1, long range2, 
            long range3, long range4)
    {
        long packet[] = new long[_ruleDim];
        for(int i = 0; (long)i < range0; i++)
        {
            for(int j = 0; (long)j < range1; j++)
            {
                for(int k = 0; (long)k < range2; k++)
                {
                    for(int m = 0; (long)m < range3; m++)
                    {
                        for(int n = 0; (long)n < range4; n++)
                        {
                            packet[0] = i;
                            packet[1] = j;
                            packet[2] = k;
                            packet[3] = m;
                            packet[4] = n;
                            if(resolve(packet) != seq.resolve(packet))
                            {
                                System.out.println((new StringBuilder("Different! Packet = ")).append(i).append(",").append(j).append(",").append(k).append(",").append(m).append(",").append(n).toString());
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public void clear()
    {
        _rules.clear();
    }

    public void CreateNiceRandom(int ruleNum)
    {
        Random ranVal = new Random();
        Rule r;
        for(int i = 0; i < ruleNum - 1; i++)
        {
            r = new Rule();
            for(int j = 0; j < _ruleDim; j++)
            {
                int left = ranVal.nextInt(10);
                int right = ranVal.nextInt(10);
                r.add(new Edge(j, new Interval(left >= right ? right : left, left >= right ? left : right)));
            }

            r.setDecision(ranVal.nextInt(2));
            add(r);
        }

        r = new Rule();
        for(int j = 0; j < _ruleDim; j++)
            r.add(new Edge(j, new Interval(0L, 9L)));

        r.setDecision(ranVal.nextInt(2));
        add(r);
    }

    public void CreateByRandom(int ruleNum)
    {
        Random ranVal = new Random();
        Rule r;
        for(int i = 0; i < ruleNum - 1; i++)
        {
            r = new Rule();
            long left = ranVal.nextInt(10);
            r.add(new Edge(0, new Interval(left, left)));
            left = ranVal.nextInt(10);
            long right = ranVal.nextInt(10);
            r.add(new Edge(1, new Interval(left >= right ? right : left, left >= right ? left : right)));
            left = ranVal.nextInt(10);
            right = ranVal.nextInt(10);
            r.add(new Edge(2, new Interval(left >= right ? right : left, left >= right ? left : right)));
            left = ranVal.nextInt(10);
            right = ranVal.nextInt(10);
            r.add(new Edge(3, new Interval(left >= right ? right : left, left >= right ? left : right)));
            left = ranVal.nextInt(10);
            r.add(new Edge(4, new Interval(left, left)));
            r.setDecision(ranVal.nextInt(2));
            add(r);
        }

        r = new Rule();
        for(int j = 0; j <= _ruleDim - 1; j++)
            r.add(new Edge(j, new Interval(0L, 9L)));

        r.setDecision(ranVal.nextInt(2));
        add(r);
    }

    public void eCal(Node v, EffectiveRuleSet ers, Rule r, int beginPos)
    {
        if(v.getLabel() != r.getLabel(beginPos))
        {
            System.out.println((new StringBuilder("Label = ")).append(v.getLabel()).append(", but r.getLabel( beginPos ) = ").append(r.getLabel(beginPos)).toString());
        } else
        {
            int k = v.getChildrenNum();
            Interval Si = new Interval((Interval)r.get(beginPos).get(0));
            Edge oldEdges = new Edge(v.getLabel());
            for(int i = 0; i < v.getChildrenNum(); i++)
                oldEdges.addAll(v.getChild(i).getInEdge());

            Edge newEdge = new Edge(v.getLabel(), Si.minus(oldEdges));
            if(newEdge.size() != 0)
            {
                Node newSon = r.buildChain(beginPos + 1);
                newSon.setInEdge(newEdge);
                v.addChild(newSon);
                currentRule.add(newEdge);
                ers.add(currentRule.combineRulesCopy(r, beginPos + 1));
                currentRule.removeLastEdge();
            }
            if(beginPos < r.size() - 1)
            {
                for(int j = 0; j < k; j++)
                {
                    Node n = v.getChild(j);
                    Edge Ei = n.getInEdge();
                    if(Ei.isSubsetOf(Si))
                    {
                        currentRule.add(Ei);
                        eCal(n, ers, r, beginPos + 1);
                        currentRule.removeLastEdge();
                    } else
                    if(Ei.overlap(Si))
                    {
                        ArrayList overlap = Ei.intersection(Si);
                        Node copy = n.deepCopy();
                        Edge e = copy.getInEdge();
                        e.clear();
                        e.addAll(overlap);
                        v.addChild(copy);
                        Ei.minus(Si);
                        currentRule.add(e);
                        eCal(copy, ers, r, beginPos + 1);
                        currentRule.removeLastEdge();
                    }
                }

            }
        }
    }

    public Node upwardRemovalfirst()
    {
        Node root = get(0).buildChain(0);
        EffectiveRuleSet ers0 = new EffectiveRuleSet();
        ers0.add(get(0));
        eSeq.add(ers0);
        for(int i = 1; i < size() - 1; i++)
        {
            EffectiveRuleSet ers = new EffectiveRuleSet();
            eCal(root, ers, get(i), 0);
            if(ers.isEmpty())
            {
                _rules.remove(i);
                i--;
            } else
            {
                eSeq.add(ers);
            }
        }

        return root;
    }

    public Node upwardRemoval()
    {
        Node root = get(0).buildChain(0);
        EffectiveRuleSet ers0 = new EffectiveRuleSet();
        ers0.add(get(0));
        eSeq.add(ers0);
        for(int i = 1; i < size(); i++)
        {
            EffectiveRuleSet ers = new EffectiveRuleSet();
            eCal(root, ers, get(i), 0);
            if(ers.isEmpty())
            {
                _rules.remove(i);
                i--;
            } else
            {
                eSeq.add(ers);
            }
        }

        return root;
    }

    public Node downwardRemoval()
    {
        if(size() == 1)
            return null;
        Node root = get(size() - 1).buildChain(0);
        for(int i = size() - 2; i >= 0; i--)
            if(isDownwardRedundant(root, eSeq.get(i)))
            {
                _rules.remove(i);
                eSeq.remove(i);
            } else
            {
                Sequence atomicRulesSeq = get(i).simplify();
                for(int j = 0; j < atomicRulesSeq.size(); j++)
                    root.appendAtomicRuleForDownward(atomicRulesSeq.get(j), 0);

            }

        return root;
    }

    public boolean isDownwardRedundant(Node root, EffectiveRuleSet ers)
    {
        for(int i = 0; i < ers.size(); i++)
        {
            Sequence atomicRulesSeq = ers.get(i).simplify();
            for(int j = 0; j < atomicRulesSeq.size(); j++)
                if(!haveSameDecision(root, atomicRulesSeq.get(j), 0))
                    return false;

        }

        return true;
    }

    public boolean haveSameDecision(Node v, Rule r, int beginPos)
    {
        Interval Si = new Interval((Interval)r.get(beginPos).get(0));
        for(int i = 0; i < v.getChildrenNum(); i++)
        {
            Node son = v.getChild(i);
            Edge Ei = son.getInEdge();
            if(Ei.overlap(Si))
                if(beginPos < r.size() - 1)
                {
                    if(!haveSameDecision(son, r, beginPos + 1))
                        return false;
                } else
                if(son.getLabel() != r.getDecision())
                    return false;
        }

        return true;
    }

    public int resolve(long packet[])
    {
        int result = -1;
        for(int i = 0; i < size(); i++)
        {
            result = get(i).resolve(packet);
            if(result != -1)
                return result;
        }

        return result;
    }

    public long getRange(int i)
    {
        if(i >= 0 && i <= _ruleDim)
            return _range[i];
        else
            return -1L;
    }

    public boolean isEquivalent(Sequence seq, int range0, int range1, int range2, int range3, int range4)
    {
        long packet[] = new long[_ruleDim];
        for(int i = 0; i < range0; i++)
        {
            for(int j = 0; j < range1; j++)
            {
                for(int k = 0; k < range2; k++)
                {
                    for(int m = 0; m < range3; m++)
                    {
                        for(int n = 0; n < range4; n++)
                        {
                            packet[0] = i;
                            packet[1] = j;
                            packet[2] = k;
                            packet[3] = m;
                            packet[4] = n;
                            if(resolve(packet) != seq.resolve(packet))
                            {
                                System.out.println((new StringBuilder("Different! Packet = ")).append(i).append(",").append(j).append(",").append(k).append(",").append(m).append(",").append(n).toString());
                                return false;
                            }
                        }

                    }

                }

            }

        }

        return true;
    }

    public void printEffectiveSet()
    {
        eSeq.print();
    }
}
