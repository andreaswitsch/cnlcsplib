
//
//package Alica.Reasoner.CNSAT;
//
//
//import Constant;
//import Term;
//import Zero;
//import java.lang.reflect.Type;
//
//public class TermEquality
//{
//    public boolean equalTerms(Term a, Term b) throws Exception {
//        if (a == b)
//            return true;
//
//        Type ta = a.getClass();
//        Type tb = b.getClass();
//        if (tb != ta)
//            return false;
//
//        if (a instanceof Zero)
//            return true;
//
//        if (a instanceof Constant)
//        {
//            Constant ca = (Constant)a;
//            Constant cb = (Constant)b;
//            return ca.Value == cb.Value;
//        }
//
//        if (a instanceof Variable)
//        {
//            return false;
//        }
//
//        //return a == b;
//        if (a instanceof LTConstraint)
//        {
//            LTConstraint ca = (LTConstraint)a;
//            LTConstraint cb = (LTConstraint)b;
//            return EqualTerms(ca.Left, cb.Left) && EqualTerms(ca.Right, cb.Right);
//        }
//
//        if (a instanceof LTEConstraint)
//        {
//            LTEConstraint ca = (LTEConstraint)a;
//            LTEConstraint cb = (LTEConstraint)b;
//            return EqualTerms(ca.Left, cb.Left) && EqualTerms(ca.Right, cb.Right);
//        }
//
//        if (a instanceof Product)
//        {
//            Product ca = (Product)a;
//            Product cb = (Product)b;
//            return EqualTerms(ca.Left, cb.Left) && EqualTerms(ca.Right, cb.Right);
//        }
//
//        if (a instanceof ConstPower)
//        {
//            ConstPower ca = (ConstPower)a;
//            ConstPower cb = (ConstPower)b;
//            if (ca.Exponent != cb.Exponent)
//                return false;
//
//            return EqualTerms(ca.Base, cb.Base);
//        }
//
//        if (a instanceof Sum)
//        {
//            Sum ca = (Sum)a;
//            Sum cb = (Sum)b;
//            if (ca.Terms.Count != cb.Terms.Count)
//                return false;
//
//            for (int i = 0;i < ca.Terms.Count;i++)
//            {
//                if (!EqualTerms(ca.Terms[i], cb.Terms[i]))
//                    return false;
//
//            }
//            return true;
//        }
//
//        if (a instanceof Max)
//        {
//            Max ca = (Max)a;
//            Max cb = (Max)b;
//            return EqualTerms(ca.Left, cb.Left) && EqualTerms(ca.Right, cb.Right);
//        }
//
//        if (a instanceof And)
//        {
//            And ca = (And)a;
//            And cb = (And)b;
//            return EqualTerms(ca.Left, cb.Left) && EqualTerms(ca.Right, cb.Right);
//        }
//
//        if (a instanceof Sin)
//        {
//            Sin ca = (Sin)a;
//            Sin cb = (Sin)b;
//            return EqualTerms(ca.Arg, cb.Arg);
//        }
//
//        if (a instanceof Cos)
//        {
//            Cos ca = (Cos)a;
//            Cos cb = (Cos)b;
//            return EqualTerms(ca.Arg, cb.Arg);
//        }
//
//        if (a instanceof Abs)
//        {
//            Abs ca = (Abs)a;
//            Abs cb = (Abs)b;
//            return EqualTerms(ca.Arg, cb.Arg);
//        }
//
//        Console.WriteLine("Unknown Termtype in TermEquality: {0}", a);
//        return false;
//    }
//
//}
//
//
