/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package PddOriginblock;

import java.io.*;
import java.util.ArrayList;

// Referenced classes of package diversity:
//            Interval

public class Edge extends ArrayList
{
    private int _fatherLabel;
    public Edge(int f)
    {
        _fatherLabel = f;
    }

    public Edge(int f, Interval S)
    {
        super.add(S);
        _fatherLabel = f;
    }

    public Edge(int f, ArrayList a)
    {
        super(a);
        _fatherLabel = f;
    }

    public Edge(Edge e)
    {
        for(int i = 0; i < e.size(); i++)
        {
            Interval S = new Interval((Interval)e.get(i));
            super.add(S);
        }

        _fatherLabel = e.getFatherLabel();
    }

    public void add(Interval S)
    {
        super.add(S);
    }

    public void add(long left, long right)
    {
        super.add(new Interval(left, right));
    }

    public int getFatherLabel()
    {
        return _fatherLabel;
    }

    public Interval getInterval(int index)
    {
    	return (Interval)get(index);
    }
    
    public void minus(Interval S)
    {
        ArrayList result = new ArrayList();
        for(int i = 0; i < size(); i++)
            result.addAll(((Interval)get(i)).minus(S));

        clear();
        addAll(result);
    }

    public boolean overlap(Interval S)
    {
        for(int i = 0; i < size(); i++)
            if(S.overlap((Interval)get(i)))
                return true;

        return false;
    }

    public boolean isSubsetOf(Interval S)
    {
        for(int i = 0; i < size(); i++)
            if(!((Interval)get(i)).isSubsetOf(S))
                return false;

        return true;
    }

    public ArrayList intersection(Interval S)
    {
        ArrayList result = new ArrayList();
        for(int i = 0; i < size(); i++)
        {
            Interval temp = S.intersection((Interval)get(i));
            if(temp != null)
                result.add(temp);
        }

        return result;
    }

    public ArrayList intersection(Edge e)
    {
        ArrayList result = new ArrayList();
        for(int i = 0; i < e.size(); i++)
        {
            ArrayList temp = intersection((Interval)e.get(i));
            if(temp.size() > 0)
                result.addAll(temp);
        }

        return result;
    }

    public boolean has(long x)
    {
        boolean result = false;
        for(int i = 0; i < size(); i++)
            if(((Interval)get(i)).has(x))
                result = true;

        return result;
    }

    public void print()
    {
        //System.out.print((new StringBuilder(String.valueOf(_fatherLabel))).append(":[").toString());
    	System.out.print("[");
        for(int i = 0; i < size(); i++)
            ((Interval)get(i)).print();

        System.out.print("]^");
    }

   
    public void print_file(PrintStream out)
    {

    	//out.print((new StringBuilder(String.valueOf(_fatherLabel))).append(":[").toString());
    	out.print("[");
    	for(int i = 0; i < size(); i++)
    		((Interval)get(i)).print_file(out);

       	out.print("]^");
    }
}
