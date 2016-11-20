using System;
namespace AutoDiff
{
	public class Tuple {
		public static Tuple<A,B> Create<A,B>(A item1, B item2) {
			return new Tuple<A, B>(item1,item2);
		}
	}
	
	public class Tuple<A,B> {
		readonly A item1;
		readonly B item2;
		public Tuple(A it1, B it2) {
			item1=it1;
			item2=it2;
		}
		
	
		public A Item1 {
			get { return this.item1;}		
		}
		public B Item2 {
			get { return this.item2;}			
		}

	}
}

