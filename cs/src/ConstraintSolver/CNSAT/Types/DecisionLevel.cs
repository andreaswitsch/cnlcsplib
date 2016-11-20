using System;
using System.Collections.Generic;

namespace Alica.Reasoner.CNSAT
{
	public class DecisionLevel
	{
		public int Level {get; set;}
		
		//public double[] Seed {get; set;}
		
		public DecisionLevel (int level)
		{
			this.Level = level;
		}
	}
}

