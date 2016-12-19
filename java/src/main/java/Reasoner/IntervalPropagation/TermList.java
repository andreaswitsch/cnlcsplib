//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:47
//

package Reasoner.IntervalPropagation;


import AutoDiff.Term;

public class TermList
{
    public Term First;
    public Term Last;
    public TermList() throws Exception {
    }

    public boolean contains(Term t) throws Exception {
        if (t.Next != null || t.Prev != null)
            return true;
         
        return t == First;
    }

    public Term dequeue() throws Exception {
        Term ret = First;
        if (ret == null)
            return ret;
         
        First = ret.Next;
        if (First != null)
            First.Prev = null;
         
        ret.Next = null;
        ret.Prev = null;
        if (ret == Last)
        {
            Last = null;
            First = null;
        }
         
        return ret;
    }

    public void enqueue(Term t) throws Exception {
        if (First == null)
        {
            First = t;
            Last = t;
            return ;
        }
         
        Last.Next = t;
        t.Prev = Last;
        Last = t;
    }

    /*public void MoveToEnd(Term t) {
    			if (t == Last) return;
    			if (t == First) {
    				Last.Next = t;
    				t.Prev = Last;
    				First = t.Next;
    				First.Prev = null;
    				t.Next = null;
    				Last = t;
    				return;
    			}
    			if (First==null) {
    				First = t;
    				Last = t;
    				t.Prev = null;
    				t.Next = null;
    				return;
    			}			
    			if (t.Next != null) {
    				t.Next.Prev = t.Prev;
    				t.Next = null;
    			}
    			
    			//remove:
    			if (t.Prev != null) {
    				t.Prev.Next = t.Next;				
    			}
    			//add
    			t.Prev = Last;
    			Last.Next = t;			
    			Last = t;
    		}*/
    public void clear() throws Exception {
        Term cur = First;
        Term next = null;
        while (cur != null)
        {
            cur.Prev = null;
            next = cur.Next;
            cur.Next = null;
            cur = next;
        }
        First = null;
        Last = null;
    }

}


