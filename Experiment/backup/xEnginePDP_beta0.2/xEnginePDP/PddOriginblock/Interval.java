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
//            Edge

public class Interval
{

    public Interval(long a, long b)
    {
        _left = a;
        _right = b;
    }

    public Interval(Interval S)
    {
        _left = S.getLeft();
        _right = S.getRight();
    }

    public ArrayList minus(Interval S)
    {
        ArrayList inArray = new ArrayList();
        if(_left > S.getRight() || _right < S.getLeft())
        {
            inArray.add(new Interval(_left, _right));
        } else
        {
            if(_left < S.getLeft())
                inArray.add(new Interval(_left, S.getLeft() - 1L));
            if(_right > S.getRight())
                inArray.add(new Interval(S.getRight() + 1L, _right));
        }
        return inArray;
    }

    public ArrayList minus(Edge e)
    {
        ArrayList result = new ArrayList();
        ArrayList temp = new ArrayList();
        result.add(new Interval(_left, _right));
        for(int i = 0; i < e.size(); i++)
        {
            for(int j = 0; j < result.size(); j++)
                temp.addAll(((Interval)result.get(j)).minus((Interval)e.get(i)));

            result.clear();
            result.addAll(temp);
            temp.clear();
        }

        return result;
    }

    public boolean overlap(Interval S)
    {
        return _left <= S.getRight() && S.getLeft() <= _right;
    }

    public boolean isSubsetOf(Interval S)
    {
        return S.getLeft() <= _left && _right <= S.getRight();
    }

    public Interval intersection(Interval S)
    {
        if(!overlap(S))
            return null;
        else
            return new Interval(S.getLeft() <= _left ? _left : S.getLeft(), S.getRight() <= _right ? S.getRight() : _right);
    }

    public boolean has(long x)
    {
        return _left <= x && x <= _right;
    }

    public void print()
    {
    	String s = _left+","+_right;
        System.out.print(s);
    }

    public void print_file(PrintStream out)
    {
    	String s = _left+","+_right;
        out.print(s);
    }

    public long getLeft()
    {
        return _left;
    }

    public void setLeft(long a)
    {
        _left = a;
    }

    public long getRight()
    {
        return _right;
    }

    public void setRight(long b)
    {
        _right = b;
    }

    private long _left;
    private long _right;
}
