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
//            Sequence, Rule, Edge, Interval, 
//            Node

public class Acl
{
	public long conversiontime;
	public long memorysize;
    private Sequence _firewall_seq;
    private Node _root;
    
    private int[][] _table1;
	private int[][] _table2;
	private ArrayList<ArrayList<Node>> _table3;
	//private int[][] _table3;
	private int[][][] _quicktable;
	private int[] _children_number;
    
    public Acl()
    {
    }

    public int sequencial_query(long query[])
    {
        int n = _firewall_seq.size();
        for(int i = 0; i < n; i++)
        {
            Rule r = _firewall_seq.get(i);
            boolean matched = true;
            for(int j = 0; j < 3; j++)
            {
                Interval ti = (Interval)r.get(j).get(0);
                long leftj = ti.getLeft();
                long rightj = ti.getRight();
                if(query[j] >= leftj && query[j] <= rightj)
                    continue;
                matched = false;
                break; 
            }

            if(matched)
                return r.getDecision();
        }

        return -1;
    }

    public void test_acl_query(String rule_file)
    {
        acl_build(rule_file); 
        long query1[] = {0, 0, 0};
        
        for(int i = 1; i < _firewall_seq._range[0]; i++)
        {
            for(int j = 1; j < _firewall_seq._range[1]; j++)
            {
                for(int k = 1; k < _firewall_seq._range[2]; k++) 
                {
                    query1[0] = i;
                    query1[1] = j;
                    query1[2] = k;
                    Set<Integer[]> origins = new HashSet<Integer[]>();
                    int d1 = acl_query_single(query1,origins);
                    int d2 = sequencial_query(query1); 
                    if(d1 != d2)
                        System.out.println((new StringBuilder("not match! ")).append(query1[0]).append(",").append(query1[1]).append(",").append(query1[2]).toString()); 
                }
            }
        }
    }

    public void acl_build(String rule_file)
    {
        _firewall_seq = new Sequence();
        _firewall_seq.createByFile_for_xacml(rule_file);
        long start = System.currentTimeMillis();
        _root = _firewall_seq.buildAllMatchFDD("final");
        //_root = _firewall_seq.buildFDD();
        _root.simplify();
        _root.sort();
        /* test: print out the result
        Rule tmpRule = new Rule();
        _firewall_seq.generateRules(_root, tmpRule, 0);
        ArrayList<Rule> result = new ArrayList<Rule>();
        result = _firewall_seq.getFirstMatchRules();
        for (int i=0; i<result.size(); i++)
        {
        	result.get(i).print_file(System.out);
        }
        */
        long end = System.currentTimeMillis();
        conversiontime = end-start;
        
        memorysize=_root.get_memory_size();
    }

    public int acl_query_single(long query[], Set<Integer[]> origins)
    {
        int value = _root.binary_query(query, origins);
        return value;
    }

    //the fast forwarding table approach
  
	//this is a sub function called by build_quick_table(String rule_file)
	public void set_seq_for_nodes()
	{
		_children_number = new int[3];
		for(int i=0;i<3;i++) _children_number[i] = 0;
			_root.traverse_set_seq(_children_number,0);
	}
    
	public void build_quick_table(String rule_file)
	{
		_firewall_seq = new Sequence();
        _firewall_seq.createByFile_for_xacml(rule_file);
		long start = System.currentTimeMillis();
		
		_root = _firewall_seq.buildAllMatchFDD("final");
		//_root.print();		
		//_root.simple_reduction();
		
        _root.simplify();
        _root.sort();
        
        // test: print out the result
        /*
        Rule tmpRule = new Rule();
        _firewall_seq.generateRules(_root, tmpRule, 0);
        ArrayList<Rule> result = new ArrayList<Rule>();
        result = _firewall_seq.getFirstMatchRules();
        for (int i=0; i<result.size(); i++)
        {
        	result.get(i).print();
        }*/
		  
		set_seq_for_nodes();
		_table1 = new int[_children_number[0]][(int)_firewall_seq._range[0]];
		_table2 = new int[_children_number[1]][(int)_firewall_seq._range[1]];
		_table3 = new ArrayList<ArrayList<Node>>();
		
		//_table3 = new int[_children_number[2]][(int)_firewall_seq._range[2]];
		
		for (int i=0; i<_children_number[2]; i++)
		{
			ArrayList<Node> tmpList = new ArrayList<Node>();
			for (int j=0; j<((int)_firewall_seq._range[2]);j++)
			{
				Node tmp = new Node(null, -1, false);
				tmpList.add(tmp);
			}
			_table3.add(tmpList);
		}
		
		_quicktable = new int[2][][];
		_quicktable[0] =_table1;
		_quicktable[1] =_table2;
		//_quicktable[2] =_table3;
		_root.traverse_set_quicktable(_quicktable, _table3, 0, -1);
		long end = System.currentTimeMillis();
		conversiontime = end-start;
		
		memorysize = 0;
		memorysize += 4*_children_number[0]*_firewall_seq._range[0];
		memorysize += 4*_children_number[1]*_firewall_seq._range[1];
		for (int i=0; i<_children_number[2]; i++)
		{
			for (int j=0; j<((int)_firewall_seq._range[2]);j++)
			{
				memorysize += _table3.get(i).get(j).get_size();
			}
		}	
	}
	 
	public int acl_query_single_with_quick_table(long[] query, Set<Integer[]> origins)
	{				
		 int node1 = _quicktable[0][0][(int)query[0]];
		 int node2 = _quicktable[1][node1][(int)query[1]];
		 Node node3 = _table3.get(node2).get((int)query[2]);
		 int decision = node3.getLabel();
		 origins.addAll(node3.getOrigins());
		  
		 return decision;
	}
    
	public void test_quick_table()
	{
		long []t_query = new long[3];
		for(int i=1;i<(int)_firewall_seq._range[0];i++)
			for(int j=1;j<(int)_firewall_seq._range[1];j++)				  
				for(int k=1;k<(int)_firewall_seq._range[2];k++)				  
				{					 
					t_query[0] = i;					  
					t_query[1] = j;					  
					t_query[2] = k;
					Set<Integer[]> origins = new HashSet<Integer[]>();
					int d1 = acl_query_single_with_quick_table(t_query, origins);
					int d2 = sequencial_query(t_query);
					if(d1 != d2)
                        System.out.println((new StringBuilder("not match! ")).append(i).append(",").append(j).append(",").append(k).toString()); 				  
				}	
	}
    
    public static void redundancy_removal(String input_file, String output_file)
    {
        System.out.println("in removal");
        long start = 0L;
        long end = 0L; 
        long sum = 0L;
        int originalNum = 0;
        int upNum = 0;
        int downNum = 0;
        Sequence seq = new Sequence();
        seq.CreateByFile_for_acl(input_file);
        originalNum = seq.size();
        System.out.println((new StringBuilder("original rule number:")).append(originalNum).toString());
        start = System.currentTimeMillis();
        seq.upwardRemovalfirst();
        upNum = seq.size();
        System.out.println((new StringBuilder("first upward removal number:")).append(upNum).toString());
        seq.downwardRemoval();
        seq.upwardRemoval();
        try
        { 
            BufferedWriter out = new BufferedWriter(new FileWriter(output_file));
            //seq.print_file(out);
            out.close();
        }
        catch(IOException ioexception) { }
        end = System.currentTimeMillis();
        sum += end - start;
        downNum = seq.size();
        System.out.println((new StringBuilder("downward removal number:")).append(downNum).toString());
        seq.print ();
        seq.clear();
        System.gc();
    }

    private static void do_query(String firewallFileName, String queryFileName, String output_file)
    {
        Sequence query_seq = new Sequence(); 
        query_seq.CreateByFile(queryFileName);
        Rule query = query_seq.get(0);
        Sequence firewall_seq = new Sequence();
        firewall_seq.CreateByFile(firewallFileName);
        Node root = firewall_seq.buildFDD(); 
        Sequence result = new Sequence();
        Rule intersectPath = new Rule();
        QueryProcess(root, query, intersectPath, result);
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(output_file)); 
            //result.print_file(out);
            out.close();
        }
        catch(IOException ioexception) { }
        System.out.println("query finished!");
    }

    private static void shape_and_see_difference(String fileName1, String fileName2, String output_file) 
    {
        Sequence seq1 = new Sequence();
        seq1.CreateByFile(fileName1);
        Node root1 = seq1.buildFDD();
        Sequence seq2 = new Sequence();
        seq2.CreateByFile(fileName2);
        Node root2 = seq2.buildFDD();
        nodeShaping(root1, root2);
        Sequence new_seq1 = root1.deriveRules();
        Sequence new_seq2 = root2.deriveRules();
        if(new_seq1.size() != new_seq2.size()) 
            System.out.println("shaping error!");
        else
            System.out.println((new StringBuilder("shaping correct! size is:")).append(new_seq1.size()).toString());
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(output_file));
            for(int i = 0; i < new_seq1.size(); i++)
            {
                Rule t_rule1 = new_seq1.get(i); 
                Rule t_rule2 = new_seq2.get(i);
                if(t_rule1.getDecision() != t_rule2.getDecision())
                {
                    //t_rule1.print_file(out);
                }
            }

            out.close();
        }
        catch(IOException ioexception) { }
    }

    private static void QueryProcess(Node v, Rule testQuery, Rule intersectPath, Sequence result)
    {
        if(v.isLeaf())
        {
            if(v.getLabel() == testQuery.getDecision())
            {
                intersectPath.setDecision(v.getLabel());
                result.add(new Rule(intersectPath));
            }
        } else 
        {
            for(int i = 0; i < v.getChildrenNum(); i++)
            {
                Node son = v.getChild(i);
                ArrayList intersect = testQuery.get(v.getLabel()).intersection(new Edge( son.getInEdge()));
                if(intersect.size() > 0)
                {
                    intersectPath.add(new Edge(v.getLabel(), intersect));
                    QueryProcess(son, testQuery, intersectPath, result); 
                    intersectPath.removeLastEdge();
                }
            }

        }
    }

    public static void nodeShaping(Node na, Node nb)
    {
        na.simplify();
        na.sort();
        nb.simplify();
        nb.sort();
        int i = 0;
        for(int j = 0; i < na.getChildrenNum() && j < nb.getChildrenNum();)
        {
            Node SonA = na.getChild (i);
            Node SonB = nb.getChild(j);
            Interval Sa = (Interval)SonA.getInEdge().get(0);
            Interval Sb = (Interval)SonB.getInEdge().get(0);
            if(Sa.getLeft() != Sb.getLeft()) 
            {
                int aaa = 0;
                System.err.println("Something Wrong in nodeShaping() function!!!");
            }
            if(Sa.getRight() == Sb.getRight())
            { 
                i++;
                j++;
            } else
            if(Sa.getRight() < Sb.getRight())
            {
                Node brother = SonB.deepCopy();
                brother.setInEdge (new Edge(nb.getLabel(), new Interval(Sa.getLeft(), Sa.getRight())));
                nb.addChild(j, brother);
                Sb.setLeft(Sa.getRight() + 1L);
                i++;
                j++;
            } else 
            {
                Node brother = SonA.deepCopy();
                brother.setInEdge(new Edge(na.getLabel(), new Interval(Sb.getLeft(), Sb.getRight())));
                na.addChild(i, brother);
                Sa.setLeft(Sb.getRight() + 1L);
                j++;
                i++;
            }
        }

        if(na.getChildrenNum() != nb.getChildrenNum())
        {
            System.err.println ("na.getChildrenNum() != nb.getChildrenNum()");
        } else
        {
            for(int k = 0; k < na.getChildrenNum(); k++)
                if(!na.getChild(k).isLeaf() && !nb.getChild(k).isLeaf()) 
                    nodeShaping(na.getChild(k), nb.getChild(k));

        }
    }
}
