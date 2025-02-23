package traversal.cypher;

import java.util.List;

import org.jgrapht.graph.DirectedPseudograph;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import model.EntityNode;
import model.EventEdge;
import model.FileEntity;
import model.NetworkEntity;
import model.ProcessEntity;
import traversal.cypher.CypherPwd.MyLabel;

public class CommonFunc {
	
	public static DirectedPseudograph<EntityNode, EventEdge> generateGraph(Driver driver, String cypher) {
		System.out.println("generateGraph called!");
		DirectedPseudograph<EntityNode, EventEdge> jg = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        
        // Open a session
        try (Session session = driver.session()) {
            
            org.neo4j.driver.Result result = session.run(cypher);
         // Process the result
            for (Record record : result.list()) {
                // Extract the updatedVisitedRels (list of incoming relationships)
                List<Object> relationships = record.get("visitedRels").asList();

                for (Object relObj : relationships) {
                    
                    if (relObj instanceof Relationship) {
                    	Relationship rel = (Relationship) relObj;
                        
                    	// Fetch node IDs
                        long startNodeId = rel.startNodeId();
                        long endNodeId = rel.endNodeId();
                        
                        // Retrieve nodes using IDs
                        Node startNode = session.run("MATCH (n) WHERE id(n) = "+startNodeId+" RETURN n")
                                               .single()
                                               .get("n")
                                               .asNode();
                        Node endNode = session.run("MATCH (n) WHERE id(n) = "+endNodeId+" RETURN n")
                                             .single()
                                             .get("n")
                                             .asNode();
                        
                        // Convert nodes to EntityNode
                        EntityNode sNode = getEntityNode(startNode);
                        EntityNode eNode = getEntityNode(endNode);

                        // Add vertices and edges
                        if (!jg.containsVertex(sNode)) 
                            jg.addVertex(sNode);
                        if (!jg.containsVertex(eNode)) 
                            jg.addVertex(eNode);
                        
                        EventEdge e = createEventEdge(sNode, eNode, rel);
                        if (!jg.containsEdge(e)) {
                            jg.addEdge(sNode, eNode, e);
                        }
                    }
                }
            }
        }
            
        
		return jg;
	}
	
	private static EntityNode getEntityNode(org.neo4j.driver.types.Node n) {
	    EntityNode node = null;
	    if (n.hasLabel(MyLabel.File.toString())) {
	        FileEntity f = new FileEntity("", "", n.get("name").asString(), n.get("id").asLong(),
	                n.get("hostname").asString(), "");
	        node = new EntityNode(f);
	    } else if (n.hasLabel(MyLabel.Process.toString())) {
	        String exePath = n.get("exename").asString();
	        String reducedExepath = (!exePath.isEmpty() && exePath.contains(" ")) ? exePath.substring(0, exePath.indexOf(" ")) : exePath;  // Don't print params out in the graph.
	        int pid = n.containsKey("pid") ? n.get("pid").asInt() : 0; // Use asInt() instead of asString()
	        ProcessEntity p = new ProcessEntity((pid==0?"":Integer.toString(pid)), "", "", "", "",
	                n.get("id").asLong(), reducedExepath,
	                n.get("hostname").asString(), "", 0L, "", "");
	        node = new EntityNode(p);
	    } else if (n.hasLabel(MyLabel.Network.toString())) {
	    	int srcport = n.containsKey("srcport") ? n.get("srcport").asInt() : 0;
	    	int dstport = n.containsKey("dstport") ? n.get("dstport").asInt() : 0;
	    	
	    	NetworkEntity network = new NetworkEntity(n.containsKey("srcip") ? n.get("srcip").asString() : "",
	                n.containsKey("dstip") ? n.get("dstip").asString() : "",
	                		(srcport==0?"":Integer.toString(srcport)),
	                		(dstport==0?"":Integer.toString(dstport)),
	                        n.containsKey("id") ? n.get("id").asLong() : 0L,
	                        n.containsKey("hostname") ? n.get("hostname").asString() : "","");
	        node = new EntityNode(network);
	    }
	    return node;
	}

	private static EventEdge createEventEdge(EntityNode srcNode, EntityNode endNode, Relationship r) {
		int eventId = r.get("id").asInt();						
		String opType =r.get("optype").asString();
		Long size = r.containsKey("amount")? r.get("amount").asLong():0L;
		Long startTime = r.get("starttime").asLong();
		Long endTime = r.get("endtime").asLong();
		String hostName = r.get("hostname").asString();
		EventEdge e = new EventEdge(srcNode, endNode, eventId, opType, size,startTime,endTime,"",hostName);
		return e;
	}

}
