package traversal.cypher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DirectedPseudograph;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import model.EntityAttributeProvider;
import model.EntityIdProvider;
import model.EntityNameProvider;
import model.EntityNode;
import model.EventEdge;
import model.EventEdgeProvider;

public class CypherPwdByDepth {
	public enum MyLabel implements Label {
		File, Process, Network
	}

	public enum MyRelationshipType implements RelationshipType {
		FileEvent, ProcessEvent, NetworkEvent
	}
	
	public static void main(String[] args)  throws Exception {
		Date startDate = new Date();
		
		int level = -1;
		String output = "";
		int index = 0;
		while (index < args.length) {
			String option = args[index].trim();
			if (option.equalsIgnoreCase("-level")) {
				index++;
				level = Integer.parseInt(args[index]);
			} else if (option.equalsIgnoreCase("-output")) {
				index++;
				output = args[index];
			} 
			index++;
		}
		
		
		Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "12345678"));
		
//		DirectedPseudograph<EntityNode, EventEdge> jgHost1 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		DirectedPseudograph<EntityNode, EventEdge> jgBack = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String cypherHost1Back = "MATCH (p:Process)-[st:FileEvent{optype:\"write\"}]->(start:File{name:\"/tmp/passwords.tar.bz2\", hostid:\"1\"}) " + 
				"WITH start, 1724731846719889370 AS initialMaxEndTime " + 
				"CALL apoc.path.expandConfig(start, { " + 
				"    relationshipFilter: \"<\", " + 
				"    minLevel: 0, " + 
				"    maxLevel: "+level+", " + 
				"    bfs: true, " + 
				"    uniqueness: \"NODE_GLOBAL\", " + 
				"    filterStartNode: false " + 
				"}) YIELD path " + 
				"WITH start, path AS visitedPath, last(nodes(path)) AS currentNode, initialMaxEndTime " + 
				"WHERE length(visitedPath) < " + level + " "+
				"OPTIONAL MATCH (currentNode)-[outgoing]->() " + 
				"WHERE outgoing IN relationships(visitedPath) OR currentNode = start " + 
				"WITH start, currentNode, outgoing, max(outgoing.endtime) AS maxEndtime, initialMaxEndTime, visitedPath " + 
				"WITH start, currentNode, outgoing, initialMaxEndTime, visitedPath, " + 
				"     CASE " + 
				"         WHEN currentNode = start THEN initialMaxEndTime " + 
				"         ELSE COALESCE(maxEndtime, initialMaxEndTime) " + 
				"     END AS updatedMaxEndTime " + 
				"MATCH (currentNode)<-[incoming]-() " + 
				"WHERE incoming.starttime < updatedMaxEndTime " + 
				"WITH start, currentNode, COLLECT(incoming) AS updatedVisitedRels, updatedMaxEndTime, visitedPath " + 
				"WITH start, currentNode, updatedVisitedRels, updatedMaxEndTime, " + 
				"     [r IN updatedVisitedRels | startNode(r)] AS sourceNodes, " + 
				"     [r IN updatedVisitedRels | endNode(r)] AS sinkNodes, visitedPath " + 
				"RETURN DISTINCT currentNode, updatedVisitedRels AS visitedRels, updatedMaxEndTime, sourceNodes, sinkNodes";
		jgBack = CommonFunc.generateGraph(driver, cypherHost1Back);
		System.out.println("Pwd Host1 backward graph has: "+jgBack.vertexSet().size()+" vertices and "+jgBack.edgeSet().size()+" edges.");
		DOTExporter<EntityNode, EventEdge> exporter = new DOTExporter<EntityNode, EventEdge>(new EntityIdProvider(),
				new EntityNameProvider(), new EventEdgeProvider(), new EntityAttributeProvider(), null);
		try {
			exporter.exportGraph(jgBack, new FileWriter(new File(String.format("/home/feishao/GQL/final/%s.dot", output))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		driver.close();        
		Date endDate = new Date();
		long diff = endDate.getTime() - startDate.getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(diff); 
		System.out.println("Executed in "+seconds+" seconds.");	
	}
		
}
