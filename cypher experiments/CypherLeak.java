package traversal.cypher;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.jgrapht.graph.DirectedPseudograph;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import main.Main;
import model.EntityNode;
import model.EventEdge;


public class CypherLeak {
	public enum MyLabel implements Label {
		File, Process, Network
	}

	public enum MyRelationshipType implements RelationshipType {
		FileEvent, ProcessEvent, NetworkEvent
	}
	
	public static void main(String[] args) {
		Date startDate = new Date();
		
		Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "12345678"));
		
		DirectedPseudograph<EntityNode, EventEdge> jgHost1 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		DirectedPseudograph<EntityNode, EventEdge> jgBack = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String cypherHost1Back = "MATCH (p:Process)-[st:NetworkEvent{id:19396}]->(start:Network{srcip:\"192.168.1.131/32\",dstip:\"192.168.1.128/32\",hostid:\"1\"}) " + 
				"WITH start, 1724731988353526987 AS initialMaxEndTime " + 
				"CALL apoc.path.expandConfig(start, { " + 
				"    relationshipFilter: \"<\", " + 
				"    minLevel: 0, " + 
				"    maxLevel: -1, " + 
				"    bfs: true, " + 
				"    uniqueness: \"NODE_GLOBAL\", " + 
				"    filterStartNode: false " + 
				"}) YIELD path " + 
				"WITH start, path AS visitedPath, last(nodes(path)) AS currentNode, initialMaxEndTime " + 
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
		
		DirectedPseudograph<EntityNode, EventEdge> jgFwd = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		String commonPart = "CALL apoc.path.expandConfig(start, { " + 
				"    relationshipFilter: \">\", " + 
				"    minLevel: 0, " + 
				"    maxLevel: -1, " + 
				"    bfs: true, " + 
				"    uniqueness: \"NODE_GLOBAL\", " + 
				"    filterStartNode: false " + 
				"}) YIELD path " + 
				"WITH start, path AS visitedPath, last(nodes(path)) AS currentNode, initialMinStartTime " + 
				"OPTIONAL MATCH (currentNode)<-[incoming]-()  " + 
				"WHERE incoming IN relationships(visitedPath) OR currentNode = start " + 
				"WITH start, currentNode, incoming, min(incoming.starttime) AS minStarttime, initialMinStartTime, visitedPath " + 
				"WITH start, currentNode, incoming, initialMinStartTime, visitedPath, " + 
				"     CASE " + 
				"         WHEN currentNode = start THEN initialMinStartTime " + 
				"         ELSE COALESCE(minStarttime, initialMinStartTime) " + 
				"     END AS updatedMinStartTime " + 
				"MATCH (currentNode)-[outgoing]->() " + 
				"WHERE outgoing.endtime > updatedMinStartTime and outgoing.starttime<1724731988353526987  " + 
				"WITH start, currentNode, COLLECT(outgoing) AS updatedVisitedRels, updatedMinStartTime, visitedPath " + 
				"WITH start, currentNode, updatedVisitedRels, updatedMinStartTime, " + 
				"     [r IN updatedVisitedRels | startNode(r)] AS sourceNodes, " + 
				"     [r IN updatedVisitedRels | endNode(r)] AS sinkNodes, visitedPath " + 
				"RETURN DISTINCT currentNode, updatedVisitedRels AS visitedRels, updatedMinStartTime, sourceNodes, sinkNodes";
		
		String cypherHost1Fwd1 = "MATCH (start{id:399})-[]->() " + 
				"WITH start, 1724731816277963134 AS initialMinStartTime " + commonPart;			
		
		jgFwd = CommonFunc.generateGraph(driver, cypherHost1Fwd1);
		
		String cypherHost1Fwd2 = "MATCH (start{id:577})-[]->() " + 
				"WITH start, 1724731988353528607 AS initialMinStartTime " + commonPart;

		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd2));
		
		String cypherHost1Fwd3 = "MATCH (start{id:397})-[]->() " + 
				"WITH start, 1724731816276751377 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd3));
		
		String cypherHost1Fwd4 = "MATCH (start{id:569})-[]->() " + 
				"WITH start, 1724731968881753721 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd4));
		
		
		System.out.println("Pwd Host1 forward graph has: "+jgFwd.vertexSet().size()+" vertices and "+jgFwd.edgeSet().size()+" edges.");
		
		jgHost1 = Main.createIntersectJg(jgBack, jgFwd);
		System.out.println("Pwd Host1 graph has: "+jgHost1.vertexSet().size()+" vertices and "+jgHost1.edgeSet().size()+" edges.");
		
		DirectedPseudograph<EntityNode, EventEdge> jgHost2 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		DirectedPseudograph<EntityNode, EventEdge> back = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String cypherHost2Back = "MATCH (p:Process)-[st:FileEvent{optype:\"write\"}]->(start:File{name:\"/tmp/leaked.tar.bz2\", hostid:\"2\"}) " + 
				"WITH start, 1724732088896911090 AS initialMaxEndTime " + 
				"CALL apoc.path.expandConfig(start, { " + 
				"    relationshipFilter: \"<\", " + 
				"    minLevel: 0, " + 
				"    maxLevel: -1, " + 
				"    bfs: true, " + 
				"    uniqueness: \"NODE_GLOBAL\", " + 
				"    filterStartNode: false " + 
				"}) YIELD path " + 
				"WITH start, path AS visitedPath, last(nodes(path)) AS currentNode, initialMaxEndTime " + 
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
		back = CommonFunc.generateGraph(driver, cypherHost2Back);
		System.out.println("Pwd Host2 backward graph has: "+back.vertexSet().size()+" vertices and "+back.edgeSet().size()+" edges.");
		
		DirectedPseudograph<EntityNode, EventEdge> fwd = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String common = "CALL apoc.path.expandConfig(start, { " + 
				"    relationshipFilter: \">\", " + 
				"    minLevel: 0, " + 
				"    maxLevel: -1, " + 
				"    bfs: true, " + 
				"    uniqueness: \"NODE_GLOBAL\", " + 
				"    filterStartNode: false " + 
				"}) YIELD path " + 
				"WITH start, path AS visitedPath, last(nodes(path)) AS currentNode, initialMinStartTime " + 
				"OPTIONAL MATCH (currentNode)<-[incoming]-()  " + 
				"WHERE incoming IN relationships(visitedPath) OR currentNode = start " + 
				"WITH start, currentNode, incoming, min(incoming.starttime) AS minStarttime, initialMinStartTime, visitedPath " + 
				"WITH start, currentNode, incoming, initialMinStartTime, visitedPath, " + 
				"     CASE " + 
				"         WHEN currentNode = start THEN initialMinStartTime " + 
				"         ELSE COALESCE(minStarttime, initialMinStartTime) " + 
				"     END AS updatedMinStartTime " + 
				"MATCH (currentNode)-[outgoing]->() " + 
				"WHERE outgoing.endtime > updatedMinStartTime and outgoing.starttime<1724732088896904555  " + 
				"WITH start, currentNode, COLLECT(outgoing) AS updatedVisitedRels, updatedMinStartTime, visitedPath " + 
				"WITH start, currentNode, updatedVisitedRels, updatedMinStartTime, " + 
				"     [r IN updatedVisitedRels | startNode(r)] AS sourceNodes, " + 
				"     [r IN updatedVisitedRels | endNode(r)] AS sinkNodes, visitedPath " + 
				"RETURN DISTINCT currentNode, updatedVisitedRels AS visitedRels, updatedMinStartTime, sourceNodes, sinkNodes";
		
		String cypherHost2Fwd1 = "MATCH (start{id:4399})-[]->() " + 
				"WITH start, 1724732088891568515 AS initialMinStartTime " + common;
				
		
		fwd = CommonFunc.generateGraph(driver, cypherHost2Fwd1);
		
		String cypherHost2Fwd2 = "MATCH (start{id:3230})-[]->() " + 
				"WITH start, 1724731838112931746 AS initialMinStartTime " + common;

		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd2));
		
		String cypherHost2Fwd3 = "MATCH (start{id:4407})-[]->() " + 
				"WITH start, 1724732088893726301 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd3));
		
		String cypherHost2Fwd4 = "MATCH (start{id:3236})-[]->() " + 
				"WITH start, 1724731839222948151 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd4));
		
		String cypherHost2Fwd5 = "MATCH (start{id:3255})-[]->() " + 
				"WITH start, 1724731846237172602 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd5));
		
		String cypherHost2Fwd6 = "MATCH (start{id:4111})-[]->() " + 
				"WITH start, 1724732067523644685 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd6));
		
		
		System.out.println("Pwd Host2 forward graph has: "+fwd.vertexSet().size()+" vertices and "+fwd.edgeSet().size()+" edges.");
		
		jgHost2 = Main.createIntersectJg(back, fwd);
		System.out.println("Pwd Host2 graph has: "+jgHost2.vertexSet().size()+" vertices and "+jgHost2.edgeSet().size()+" edges.");
		
		
		DirectedPseudograph<EntityNode, EventEdge> jgFinal = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		jgFinal = Main.createUnionJg(jgHost1, jgHost2);
		System.out.println("Pwd final graph has: "+jgFinal.vertexSet().size()+" vertices and "+jgFinal.edgeSet().size()+" edges.");

		// Close the driver
        driver.close();
        
		Date endDate = new Date();
		long diff = endDate.getTime() - startDate.getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(diff); 
		System.out.println("Executed in "+seconds+" seconds.");	
	}

}
