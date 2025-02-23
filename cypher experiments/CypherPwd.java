package traversal.cypher;

import java.util.Date;

import java.util.concurrent.TimeUnit;

import org.jgrapht.graph.DirectedPseudograph;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import main.Main;
import model.EntityNode;
import model.EventEdge;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;


public class CypherPwd {
	
	
	public enum MyLabel implements Label {
		File, Process, Network
	}

	public enum MyRelationshipType implements RelationshipType {
		FileEvent, ProcessEvent, NetworkEvent
	}
	
	public static void main(String[] args)  throws Exception {
		Date startDate = new Date();
		
		Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "12345678"));
		
		DirectedPseudograph<EntityNode, EventEdge> jgHost1 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		DirectedPseudograph<EntityNode, EventEdge> jgBack = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String cypherHost1Back = "MATCH (p:Process)-[st:FileEvent{optype:\"write\"}]->(start:File{name:\"/tmp/passwords.tar.bz2\", hostid:\"1\"}) " + 
				"WITH start, 1724731846719889370 AS initialMaxEndTime " + 
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
				"WHERE outgoing.endtime > updatedMinStartTime and outgoing.starttime<1724731846719889370  " + 
				"WITH start, currentNode, COLLECT(outgoing) AS updatedVisitedRels, updatedMinStartTime, visitedPath " + 
				"WITH start, currentNode, updatedVisitedRels, updatedMinStartTime, " + 
				"     [r IN updatedVisitedRels | startNode(r)] AS sourceNodes, " + 
				"     [r IN updatedVisitedRels | endNode(r)] AS sinkNodes, visitedPath " + 
				"RETURN DISTINCT currentNode, updatedVisitedRels AS visitedRels, updatedMinStartTime, sourceNodes, sinkNodes";
		
		String cypherHost1Fwd1 = "MATCH (start{id:386})-[]->() " + 
				"WITH start, 1724731816189578342 AS initialMinStartTime " + commonPart;			
		
		jgFwd = CommonFunc.generateGraph(driver, cypherHost1Fwd1);
		
		String cypherHost1Fwd2 = "MATCH (start{id:513})-[]->() " + 
				"WITH start, 1724731846503878604 AS initialMinStartTime " + commonPart;

		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd2));
		
		String cypherHost1Fwd3 = "MATCH (start{id:250})-[]->() " + 
				"WITH start, 1724731744845510766 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd3));
		
		String cypherHost1Fwd4 = "MATCH (start{id:405})-[]->() " + 
				"WITH start, 1724731816819726862 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd4));
		
		String cypherHost1Fwd5 = "MATCH (start{id:402})-[]->() " + 
				"WITH start, 1724731816300327035 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd5));
		
		String cypherHost1Fwd6 = "MATCH (start{id:389})-[]->() " + 
				"WITH start, 1724731816269989210 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd6));
		
		String cypherHost1Fwd7 = "MATCH (start{id:399})-[]->() " + 
				"WITH start, 1724731816277963134 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd7));
		
		String cypherHost1Fwd8 = "MATCH (start{id:401})-[]->() " + 
				"WITH start, 1724731816278148120 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd8));
		
		String cypherHost1Fwd9 = "MATCH (start{id:400})-[]->() " + 
				"WITH start, 1724731816277968642 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd9));
		
		String cypherHost1Fwd10 = "MATCH (start{id:362})-[]->() " + 
				"WITH start, 1724731760105766262 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd10));
		
		String cypherHost1Fwd11 = "MATCH (start{id:398})-[]->() " + 
				"WITH start, 1724731816276973841 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd11));
		
		String cypherHost1Fwd12 = "MATCH (start{id:210})-[]->() " + 
				"WITH start, 1724731734356761705 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd12));
		
		String cypherHost1Fwd13 = "MATCH (start{id:397})-[]->() " + 
				"WITH start, 1724731816276751377 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd13));
		
		String cypherHost1Fwd14 = "MATCH (start{id:361})-[]->() " + 
				"WITH start, 1724731760077131427 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd14));
		
		String cypherHost1Fwd15 = "MATCH (start{id:113})-[]->() " + 
				"WITH start, 1724731714683825589 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd15));
		System.out.println("Pwd Host1 forward graph has: "+jgFwd.vertexSet().size()+" vertices and "+jgFwd.edgeSet().size()+" edges.");
		
		jgHost1 = Main.createIntersectJg(jgBack, jgFwd);
		System.out.println("Pwd Host1 graph has: "+jgHost1.vertexSet().size()+" vertices and "+jgHost1.edgeSet().size()+" edges.");
		
		DirectedPseudograph<EntityNode, EventEdge> jgHost2 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		DirectedPseudograph<EntityNode, EventEdge> back = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String cypherHost2Back = "MATCH (p:Process)-[st:NetworkEvent{id:100005}]->(start:Network{srcip:\"192.168.1.128/32\",dstip:\"192.168.1.131/32\",hostid:\"2\"}) " + 
				"WITH start, 1724731846712161377 AS initialMaxEndTime " + 
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
				"WHERE outgoing.endtime > updatedMinStartTime and outgoing.starttime<1724731846712161377  " + 
				"WITH start, currentNode, COLLECT(outgoing) AS updatedVisitedRels, updatedMinStartTime, visitedPath " + 
				"WITH start, currentNode, updatedVisitedRels, updatedMinStartTime, " + 
				"     [r IN updatedVisitedRels | startNode(r)] AS sourceNodes, " + 
				"     [r IN updatedVisitedRels | endNode(r)] AS sinkNodes, visitedPath " + 
				"RETURN DISTINCT currentNode, updatedVisitedRels AS visitedRels, updatedMinStartTime, sourceNodes, sinkNodes";
		
		String cypherHost2Fwd1 = "MATCH (start{id:2859})-[]->() " + 
				"WITH start, 1724731816756047685 AS initialMinStartTime " + common;
				
		
		fwd = CommonFunc.generateGraph(driver, cypherHost2Fwd1);
		
		String cypherHost2Fwd2 = "MATCH (start{id:3398})-[]->() " + 
				"WITH start, 1724731846640095520 AS initialMinStartTime " + common;

		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd2));
		
		String cypherHost2Fwd3 = "MATCH (start{id:2858})-[]->() " + 
				"WITH start, 1724731816756027764 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd3));
		
		String cypherHost2Fwd4 = "MATCH (start{id:3295})-[]->() " + 
				"WITH start, 1724731846498614166 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd4));
		
		String cypherHost2Fwd5 = "MATCH (start{id:2438})-[]->() " + 
				"WITH start, 1724731816640315266 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd5));
		
		String cypherHost2Fwd6 = "MATCH (start{id:1890})-[]->() " + 
				"WITH start, 1724731816275006664 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd6));
		
		String cypherHost2Fwd7 = "MATCH (start{id:2158})-[]->() " + 
				"WITH start, 1724731816510169811 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd7));
		
		String cypherHost2Fwd8 = "MATCH (start{id:2861})-[]->() " + 
				"WITH start, 1724731816756311241 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd8));
		
		String cypherHost2Fwd9 = "MATCH (start{id:2919})-[]->() " + 
				"WITH start, 1724731816803856803 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd9));
		
		String cypherHost2Fwd10 = "MATCH (start{id:2932})-[]->() " + 
				"WITH start, 1724731816816441369 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd10));
		
		String cypherHost2Fwd11 = "MATCH (start{id:2918})-[]->() " + 
				"WITH start, 1724731816803820961 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd11));
		
		String cypherHost2Fwd12 = "MATCH (start{id:3159})-[]->() " + 
				"WITH start, 1724731816958898342 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd12));
		
		String cypherHost2Fwd13 = "MATCH (start{id:1965})-[]->() " + 
				"WITH start, 1724731816416993449 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd13));
		
		String cypherHost2Fwd14 = "MATCH (start{id:3240})-[]->() " + 
				"WITH start, 1724731840087864715 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd14));
		
		String cypherHost2Fwd15 = "MATCH (start{id:1935})-[]->() " + 
				"WITH start, 1724731816405974519 AS initialMinStartTime "+common;
		fwd = Main.createUnionJg(fwd, CommonFunc.generateGraph(driver, cypherHost2Fwd15));
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
