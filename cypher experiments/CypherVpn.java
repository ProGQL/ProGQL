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



public class CypherVpn {
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
		
		String cypherHost1Back = "MATCH (p:Process{id:663})-[st:NetworkEvent{optype:\"write\"}]->(start:Network{srcip:\"192.168.1.131/32\",dstip:\"192.168.1.128/32\",hostid:\"1\"}) " + 
				"WITH start, 1724732319377872104 AS initialMaxEndTime " + 
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
				"WHERE outgoing.endtime > updatedMinStartTime and outgoing.starttime<1724732319377872104  " + 
				"WITH start, currentNode, COLLECT(outgoing) AS updatedVisitedRels, updatedMinStartTime, visitedPath " + 
				"WITH start, currentNode, updatedVisitedRels, updatedMinStartTime, " + 
				"     [r IN updatedVisitedRels | startNode(r)] AS sourceNodes, " + 
				"     [r IN updatedVisitedRels | endNode(r)] AS sinkNodes, visitedPath " + 
				"RETURN DISTINCT currentNode, updatedVisitedRels AS visitedRels, updatedMinStartTime, sourceNodes, sinkNodes";
		
		String cypherHost1Fwd1 = "MATCH (start{id:397})-[]->() " + 
				"WITH start, 1724731816276751377 AS initialMinStartTime " + commonPart;			
		
		jgFwd = CommonFunc.generateGraph(driver, cypherHost1Fwd1);
		
		String cypherHost1Fwd2 = "MATCH (start{id:664})-[]->() " + 
				"WITH start, 1724732319377873757 AS initialMinStartTime " + commonPart;

		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd2));
		
		String cypherHost1Fwd3 = "MATCH (start{id:398})-[]->() " + 
				"WITH start, 1724731816276973841 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd3));

		
		System.out.println("Pwd Host1 forward graph has: "+jgFwd.vertexSet().size()+" vertices and "+jgFwd.edgeSet().size()+" edges.");
		
		jgHost1 = Main.createIntersectJg(jgBack, jgFwd);
		System.out.println("Pwd Host1 graph has: "+jgHost1.vertexSet().size()+" vertices and "+jgHost1.edgeSet().size()+" edges.");
		
		DirectedPseudograph<EntityNode, EventEdge> jgHost2Poi1 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		DirectedPseudograph<EntityNode, EventEdge> back1 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String cypherHost2Poi1Back = "MATCH (p:Process)-[st:FileEvent{optype:\"write\"}]->(start:File{name:\"/home/fs1/malicious.sh\", hostid:\"2\"}) " + 
				"WITH start, 1724732531452003131 AS initialMaxEndTime " + 
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
		back1 = CommonFunc.generateGraph(driver, cypherHost2Poi1Back);
		System.out.println("Pwd Host2 POI1 backward graph has: "+back1.vertexSet().size()+" vertices and "+back1.edgeSet().size()+" edges.");
		
		DirectedPseudograph<EntityNode, EventEdge> fwd1 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String common1 = "CALL apoc.path.expandConfig(start, { " + 
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
				"WHERE outgoing.endtime > updatedMinStartTime and outgoing.starttime<1724732531452003131  " + 
				"WITH start, currentNode, COLLECT(outgoing) AS updatedVisitedRels, updatedMinStartTime, visitedPath " + 
				"WITH start, currentNode, updatedVisitedRels, updatedMinStartTime, " + 
				"     [r IN updatedVisitedRels | startNode(r)] AS sourceNodes, " + 
				"     [r IN updatedVisitedRels | endNode(r)] AS sinkNodes, visitedPath " + 
				"RETURN DISTINCT currentNode, updatedVisitedRels AS visitedRels, updatedMinStartTime, sourceNodes, sinkNodes";
		
		String cypherHost2Poi1Fwd1 = "MATCH (start{id:6157})-[]->() " + 
				"WITH start, 1724732422470062882 AS initialMinStartTime " + common1;				
		
		fwd1 = CommonFunc.generateGraph(driver, cypherHost2Poi1Fwd1);
		
		String cypherHost2Poi1Fwd2 = "MATCH (start{id:6381})-[]->() " + 
				"WITH start, 1724732531451896153 AS initialMinStartTime " + common1;

		fwd1 = Main.createUnionJg(fwd1, CommonFunc.generateGraph(driver, cypherHost2Poi1Fwd2));
		
		String cypherHost2Poi1Fwd3 = "MATCH (start{id:2041})-[]->() " + 
				"WITH start, 1724731816466634389 AS initialMinStartTime "+common1;
		fwd1 = Main.createUnionJg(fwd1, CommonFunc.generateGraph(driver, cypherHost2Poi1Fwd3));		
		
		System.out.println("Pwd Host2 POI1 forward graph has: "+fwd1.vertexSet().size()+" vertices and "+fwd1.edgeSet().size()+" edges.");
		
		jgHost2Poi1 = Main.createIntersectJg(back1, fwd1);
		System.out.println("Pwd Host2 POI1 graph has: "+jgHost2Poi1.vertexSet().size()+" vertices and "+jgHost2Poi1.edgeSet().size()+" edges.");
		
		
		
		DirectedPseudograph<EntityNode, EventEdge> jgHost2Poi2 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		DirectedPseudograph<EntityNode, EventEdge> back2 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String cypherHost2Poi2Back = "MATCH (p:Process)-[st:FileEvent{optype:\"write\"}]->(start:File{name:\"/tmp/vpnfilter\", hostid:\"2\"}) " + 
				"WITH start, 1724732373470948490 AS initialMaxEndTime " + 
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
		back2 = CommonFunc.generateGraph(driver, cypherHost2Poi2Back);
		System.out.println("Pwd Host2 POI2 backward graph has: "+back2.vertexSet().size()+" vertices and "+back2.edgeSet().size()+" edges.");
		
		DirectedPseudograph<EntityNode, EventEdge> fwd2 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String common2 = "CALL apoc.path.expandConfig(start, { " + 
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
				"WHERE outgoing.endtime > updatedMinStartTime and outgoing.starttime<1724732372001719913  " + 
				"WITH start, currentNode, COLLECT(outgoing) AS updatedVisitedRels, updatedMinStartTime, visitedPath " + 
				"WITH start, currentNode, updatedVisitedRels, updatedMinStartTime, " + 
				"     [r IN updatedVisitedRels | startNode(r)] AS sourceNodes, " + 
				"     [r IN updatedVisitedRels | endNode(r)] AS sinkNodes, visitedPath " + 
				"RETURN DISTINCT currentNode, updatedVisitedRels AS visitedRels, updatedMinStartTime, sourceNodes, sinkNodes";
		
		String cypherHost2Poi2Fwd1 = "MATCH (start{id:3226})-[]->() " + 
				"WITH start, 1724731838062334661 AS initialMinStartTime " + common2;				
		
		fwd2 = CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd1);
		
		String cypherHost2Poi2Fwd2 = "MATCH (start{id:5314})-[]->() " + 
				"WITH start, 1724732371209352058 AS initialMinStartTime " + common2;

		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd2));
		
		String cypherHost2Poi2Fwd3 = "MATCH (start{id:1968})-[]->() " + 
				"WITH start, 1724731816459907845 AS initialMinStartTime "+common2;
		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd3));
		
		
		System.out.println("Pwd Host2 POI2 forward graph has: "+fwd2.vertexSet().size()+" vertices and "+fwd2.edgeSet().size()+" edges.");
		
		jgHost2Poi2 = Main.createIntersectJg(back2, fwd2);
		System.out.println("Pwd Host2 POI1 graph has: "+jgHost2Poi2.vertexSet().size()+" vertices and "+jgHost2Poi2.edgeSet().size()+" edges.");
		
		
		
		DirectedPseudograph<EntityNode, EventEdge> jgHost2Poi3 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		DirectedPseudograph<EntityNode, EventEdge> back3 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String cypherHost2Poi3Back = "MATCH (p:Process)-[st:FileEvent{optype:\"write\"}]->(start:File{name:\"/var/stage2\", hostid:\"2\"}) " + 
				"WITH start, 1724732375032502907 AS initialMaxEndTime " + 
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
		back3 = CommonFunc.generateGraph(driver, cypherHost2Poi3Back);
		System.out.println("Pwd Host2 POI3 backward graph has: "+back3.vertexSet().size()+" vertices and "+back3.edgeSet().size()+" edges.");
		
		DirectedPseudograph<EntityNode, EventEdge> fwd3 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String common3 = "CALL apoc.path.expandConfig(start, { " + 
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
				"WHERE outgoing.endtime > updatedMinStartTime and outgoing.starttime<1724732375008456085  " + 
				"WITH start, currentNode, COLLECT(outgoing) AS updatedVisitedRels, updatedMinStartTime, visitedPath " + 
				"WITH start, currentNode, updatedVisitedRels, updatedMinStartTime, " + 
				"     [r IN updatedVisitedRels | startNode(r)] AS sourceNodes, " + 
				"     [r IN updatedVisitedRels | endNode(r)] AS sinkNodes, visitedPath " + 
				"RETURN DISTINCT currentNode, updatedVisitedRels AS visitedRels, updatedMinStartTime, sourceNodes, sinkNodes";
		
		String cypherHost2Poi3Fwd1 = "MATCH (start{id:5331})-[]->() " + 
				"WITH start, 1724732373596350345 AS initialMinStartTime " + common3;				
		
		fwd3 = CommonFunc.generateGraph(driver, cypherHost2Poi3Fwd1);
		
		String cypherHost2Poi3Fwd2 = "MATCH (start{id:5325})-[]->() " + 
				"WITH start, 1724732373531292231 AS initialMinStartTime " + common3;

		fwd3 = Main.createUnionJg(fwd3, CommonFunc.generateGraph(driver, cypherHost2Poi3Fwd2));
		
		String cypherHost2Poi3Fwd3 = "MATCH (start{id:5332})-[]->() " + 
				"WITH start, 1724732373603139194 AS initialMinStartTime "+common3;
		fwd3 = Main.createUnionJg(fwd3, CommonFunc.generateGraph(driver, cypherHost2Poi3Fwd3));
		
		
		System.out.println("Pwd Host2 POI3 forward graph has: "+fwd3.vertexSet().size()+" vertices and "+fwd3.edgeSet().size()+" edges.");
		
		jgHost2Poi3 = Main.createIntersectJg(back3, fwd3);
		System.out.println("Pwd Host2 POI3 graph has: "+jgHost2Poi3.vertexSet().size()+" vertices and "+jgHost2Poi3.edgeSet().size()+" edges.");
		
		
		DirectedPseudograph<EntityNode, EventEdge> jgFinal = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		jgFinal = Main.createUnionJg(jgHost1, jgHost2Poi1);
		jgFinal = Main.createUnionJg(jgFinal, jgHost2Poi2);
		jgFinal = Main.createUnionJg(jgFinal, jgHost2Poi3);
		System.out.println("Pwd final graph has: "+jgFinal.vertexSet().size()+" vertices and "+jgFinal.edgeSet().size()+" edges.");

		// Close the driver
        driver.close();
        
		Date endDate = new Date();
		long diff = endDate.getTime() - startDate.getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(diff); 
		System.out.println("Executed in "+seconds+" seconds.");	
	}

}
