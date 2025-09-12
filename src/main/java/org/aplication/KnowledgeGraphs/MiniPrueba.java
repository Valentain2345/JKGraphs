package org.aplication.KnowledgeGraphs;

import org.hypergraphdb.HGConfiguration;
import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HyperGraph;

public class MiniPrueba {
	  public MiniPrueba() {
	        System.out.println( "Hello World!" );
	        String location ="./data";  
	        HGConfiguration config = new HGConfiguration();
	        config.setTransactional(false);
	        config.setSkipOpenedEvent(true);
	        HyperGraph graph = HGEnvironment.get(location, config);
	        String hello = graph.get(graph.add("Hello World")); 
	        System.out.println(hello.toLowerCase());
	       graph.close();
	    }
}
