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
//            Edge, Node, Sequence, Interval

public class Rule
{
    private ArrayList<Edge> _edges;
    private int _decision;
    private Set<Integer[]> _origins;
	
    public Rule()
    {
        _edges = new ArrayList<Edge>();
        _origins = new HashSet<Integer[]>();
        _decision = -1;
    }

    public Rule(Rule r)
    {
        _edges = new ArrayList<Edge>(r.getEdgeList());
        _decision = r.getDecision();
        _origins = new HashSet<Integer[]>();
        _origins.addAll(r.getOrigins());
    }

    public ArrayList<Edge> getEdgeList()
    {
        return _edges;
    }

    public void setEdgeList(ArrayList a)
    {
        _edges = a;
    }

    public int getDecision()
    {
        return _decision;
    }

    public Set<Integer[]> getOrigins()
    {
    	return _origins;
    }
    
    public void addOrigins(Set<Integer[]> origins)
    {
    	_origins.addAll(origins);
    }
    
    public void setOrigins(Set<Integer[]> origins)
    {
    	_origins.clear();
    	_origins.addAll(origins);
    }
    
    public void addOrigin(int num, int dec)
    {
    	Integer org[] = {num, dec};
    	_origins.add(org);
    }
  
    public void addOrigin(Integer[] org)
    {
    	_origins.add(org);
    }
    
    public void setOrigin(int num, int dec)
    {
    	_origins.clear();
    	Integer org[] = {num, dec};
    	_origins.add(org);
    }
    
    public void setDecision(int i)
    {
        _decision = i;
    }

    public Edge get(int i)
    {
        return (Edge)_edges.get(i);
    }

    public int getLabel(int i)
    {
        return get(i).getFatherLabel();
    }

    public int getLastLabel()
    {
        return getLabel(size() - 1);
    }

    public void add(Edge e)
    {
        _edges.add(e);
    }

    public void set(int index, Edge e)
    {
    	if(_edges.size()-1 >= index)
    		_edges.set(index, e);
    	else if (_edges.size()==index)
    		_edges.add(e);
    }
    
    public void removeLastEdge()
    {
        _edges.remove(_edges.size() - 1);
    }

    public int size()
    {
        return _edges.size();
    }

    public void deepCopy(Rule r)
    {  	  
    	setDecision( r.getDecision() );
    	setOrigins(r.getOrigins());
  	    for( int i=0; i < r.size(); i++ ){
  	      add( new Edge( r.get( i ) ) );
  	    }
    }
    
    public void print()
    {
        for(int i = 0; i < size(); i++)
            get(i).print();

        String s = "[";
        Integer tmp[];
        Iterator<Integer[]> it = _origins.iterator();
        while(it.hasNext())
        {
        	tmp = it.next();
        	s += tmp[0].toString()+" "+ tmp[1].toString()+" ";
        }
        s +="]";
        System.out.println((new StringBuilder("->")).append(_decision).append(",").append(s).toString());
    }

    public void print_file(PrintStream out)
    {
    	
        for(int i = 0; i < size(); i++)
            get(i).print_file(out);

        String s = "[";
        Integer tmp[];
        Iterator<Integer[]> it = _origins.iterator();
        while(it.hasNext())
        {
        	tmp = it.next();
        	s += tmp[0].toString()+" "+ tmp[1].toString()+" ";
        }
        s +="]";
        out.println((new StringBuilder("->")).append(_decision).append(",").append(s).toString());
        
        /*
    	//split the rule to the single range rule
    	Edge e0 = get(0);
    	int num0=e0.size();
    	Edge e1 = get(1);
    	int num1=e1.size();
    	Edge e2 = get(2);
    	int num2=e2.size();
    	
    	ArrayList<String> sa = new ArrayList<String>();
    	for (int i=0; i<num0; i++)
    	{
    		String s1 = "["+ e0.getInterval(i).getLeft()+ ","+e0.getInterval(i).getRight()+"]^";
    		for (int j=0;j<num1; j++)
    		{
    			String s2 = "["+ e1.getInterval(j).getLeft()+ ","+e1.getInterval(j).getRight()+"]^";
    			for (int k=0; k<num2; k++)
    			{
    				String s3 = "["+ e2.getInterval(k).getLeft()+ ","+e2.getInterval(k).getRight()+"]^";
    				sa.add(s1+s2+s3);
    			}
    		}
    	}
        
        String orgS = "[";
        Integer tmp[];
        Iterator<Integer[]> it = _origins.iterator();
        while(it.hasNext())
        {
        	tmp = it.next();
        	orgS += tmp[0].toString()+" "+ tmp[1].toString()+" ";
        }
        orgS +="]";
        
        for (int i=0; i<sa.size();i++)
        {
        	//String s3 = sa.get(i)+"->"+_decision+","+orgS;
        	out.println(sa.get(i)+"->"+_decision+","+orgS);
        }
        */
        //out.println((new StringBuilder("->")).append(_decision).append(",").append(s).toString());
    }

    /*
    public Node buildChain(int begin)
    {
        return buildChain(begin, 0);
    }
	*/
    
    public Node buildChain(int begin)
    {
        Node root;
        if(begin == size())
        {
            root = new Node(get(size() - 1), _decision, true);
            root.setOrigins(_origins);
            //root.setRuleNum(rule_num);
        } 
        else
        {
            root = new Node(null, begin, false);
            Node father = root;
            Node son;
            for(int i = begin; i < size() - 1; i++)
            {
                son = new Node(get(i), get(i + 1).getFatherLabel(), false);
                father.addChild(son);
                father = son;
            }

            son = new Node(get(size() - 1), _decision, true);
            son.setOrigins(_origins);
            //son.setRuleNum(rule_num);
            father.addChild(son);
        }
        return root;
    }

    public int resolve(long packet[])
    {
        boolean match = true;
        for(int i = 0; i < size(); i++)
        {
            Edge e = get(i);
            if(!e.has(packet[e.getFatherLabel()]))
                match = false;
        }

        return match ? _decision : -1;
    }

    public Rule combineRulesCopy(Rule r, int beginPos)
    {
        Rule resultRule = new Rule();
        resultRule.setDecision(r.getDecision());
        for(int i = 0; i < size(); i++)
            resultRule.add(new Edge(get(i)));

        for(int i = beginPos; i < r.size(); i++)
            resultRule.add(new Edge(r.get(i)));

        return resultRule;
    }

    public Sequence simplify()
    {
        Sequence atomicSeq = new Sequence();
        for(int i = 0; i < get(0).size(); i++)
        {
            for(int j = 0; j < get(1).size(); j++)
            {
                for(int k = 0; k < get(2).size(); k++)
                {
                    Rule r = new Rule();
                    r.add(new Edge(0, (Interval)get(0).get(i)));
                    r.add(new Edge(1, (Interval)get(1).get(j)));
                    r.add(new Edge(2, (Interval)get(2).get(k)));
                    r.setDecision(getDecision());
                    atomicSeq.add(r);
                }
            }
        }
        return atomicSeq;
    }
}
