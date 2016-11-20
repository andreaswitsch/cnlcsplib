using System;

namespace Alica.Reasoner.CNSAT
{
	public class Watcher
	{
		public Clause Clause{ get; set; } 
		public Lit Lit{	get; set; } 
		
		public Watcher (Lit l, Clause parent)
		{
			this.Clause = parent;
			this.Lit = l;
			Lit.Var.WatchList.Add(this);
		}
		
		
	}
}

