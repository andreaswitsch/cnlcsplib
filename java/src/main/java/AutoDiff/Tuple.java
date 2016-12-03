//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

public class Tuple<A,B> {
    A item1;
    B item2;

    public Tuple() {}

    public Tuple(A it1, B it2) {
        item1=it1;
        item2=it2;
    }


    public A getItem1()
    {
        return item1;
    }

    public void setItem1(final A item1)
    {
        this.item1 = item1;
    }

    public B getItem2()
    {
        return item2;
    }

    public void setItem2(final B item2)
    {
        this.item2 = item2;
    }

    public static <A, B>Tuple<A,B> create(A item1, B item2) throws Exception {
        return new Tuple<A,B>(item1,item2);
    }

}


