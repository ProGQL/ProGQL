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
		
		String cypherHost1Back = "MATCH (p:Process{id:663})-[st:NetworkEvent{id:29704}]->(start:Network{srcip:\"192.168.1.131/32\",dstip:\"192.168.1.128/32\",hostid:\"1\"}) " + 
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
		
		String cypherHost1Fwd1 = "MATCH (start{id:399})-[]->() " + 
				"WITH start, 1724731816277963134 AS initialMinStartTime " + commonPart;			
		
		jgFwd = CommonFunc.generateGraph(driver, cypherHost1Fwd1);
		
		String cypherHost1Fwd2 = "MATCH (start{id:664})-[]->() " + 
				"WITH start, 1724732319377873757 AS initialMinStartTime " + commonPart;

		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd2));
		
		String cypherHost1Fwd3 = "MATCH (start{id:397})-[]->() " + 
				"WITH start, 1724731816276751377 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd3));

		String cypherHost1Fwd4 = "MATCH (start{id:655})-[]->() " + 
				"WITH start, 1724732298818635849 AS initialMinStartTime "+commonPart;
		jgFwd = Main.createUnionJg(jgFwd, CommonFunc.generateGraph(driver, cypherHost1Fwd4));
		
		System.out.println("Pwd Host1 forward graph has: "+jgFwd.vertexSet().size()+" vertices and "+jgFwd.edgeSet().size()+" edges.");
		
		jgHost1 = Main.createIntersectJg(jgBack, jgFwd);
		System.out.println("Pwd Host1 graph has: "+jgHost1.vertexSet().size()+" vertices and "+jgHost1.edgeSet().size()+" edges.");
		
		DirectedPseudograph<EntityNode, EventEdge> jgHost2Poi1 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		DirectedPseudograph<EntityNode, EventEdge> back1 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String cypherHost2Poi1Back = "MATCH (p:Process)-[st:FileEvent{optype:\"write\"}]->(start:File{name:\"/var/stage2\", hostid:\"2\"}) " + 
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
		
		String cypherHost2Poi1Fwd1 = "MATCH (start{id:5331})-[]->() " + 
				"WITH start, 1724732373596350345 AS initialMinStartTime " + common1;				
		
		fwd1 = CommonFunc.generateGraph(driver, cypherHost2Poi1Fwd1);
		
		String cypherHost2Poi1Fwd2 = "MATCH (start{id:5325})-[]->() " + 
				"WITH start, 1724732373531292231 AS initialMinStartTime " + common1;

		fwd1 = Main.createUnionJg(fwd1, CommonFunc.generateGraph(driver, cypherHost2Poi1Fwd2));
			
		
		System.out.println("Pwd Host2 POI1 forward graph has: "+fwd1.vertexSet().size()+" vertices and "+fwd1.edgeSet().size()+" edges.");
		
		jgHost2Poi1 = Main.createIntersectJg(back1, fwd1);
		System.out.println("Pwd Host2 POI1 graph has: "+jgHost2Poi1.vertexSet().size()+" vertices and "+jgHost2Poi1.edgeSet().size()+" edges.");
		
		
		
		DirectedPseudograph<EntityNode, EventEdge> jgHost2Poi2 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		DirectedPseudograph<EntityNode, EventEdge> back2 = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		
		String cypherHost2Poi2Back = "MATCH (p:Process)-[st:FileEvent{optype:\"write\"}]->(start:File{name:\"/tmp/vpnfilter\", hostid:\"2\"}) " + 
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
		
		String cypherHost2Poi2Fwd4 = "MATCH (start{id:3230})-[]->() " + 
				"WITH start, 1724731838112931746 AS initialMinStartTime "+common2;
		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd4));
		
		String cypherHost2Poi2Fwd5 = "MATCH (start{id:1754})-[]->() " + 
				"WITH start, 1724731609948249784 AS initialMinStartTime "+common2;
		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd5));
		
		String cypherHost2Poi2Fwd6 = "MATCH (start{id:3236})-[]->() " + 
				"WITH start, 1724731839222948151 AS initialMinStartTime "+common2;
		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd6));
		
		String cypherHost2Poi2Fwd7 = "MATCH (start{id:3168})-[]->() " + 
				"WITH start, 1724731817008125122 AS initialMinStartTime "+common2;
		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd7));
		
		String cypherHost2Poi2Fwd8 = "MATCH (start{id:5028})-[]->() " + 
				"WITH start, 1724732356343976355 AS initialMinStartTime "+common2;
		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd8));
		
		String cypherHost2Poi2Fwd9 = "MATCH (start{id:3167})-[]->() " + 
				"WITH start, 1724731817008117814 AS initialMinStartTime "+common2;
		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd9));
		
		String cypherHost2Poi2Fwd10 = "MATCH (start{id:5175})-[]->() " + 
				"WITH start, 1724732356617848773 AS initialMinStartTime "+common2;
		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd10));
		
		String cypherHost2Poi2Fwd11 = "MATCH (start{id:1923})-[]->() " + 
				"WITH start, 1724731816281018539 AS initialMinStartTime "+common2;
		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd11));
		
		String cypherHost2Poi2Fwd12 = "MATCH (start{id:4491})-[]->() " + 
				"WITH start, 1724732319374454771 AS initialMinStartTime "+common2;
		fwd2 = Main.createUnionJg(fwd2, CommonFunc.generateGraph(driver, cypherHost2Poi2Fwd12));
		
		
		System.out.println("Pwd Host2 POI2 forward graph has: "+fwd2.vertexSet().size()+" vertices and "+fwd2.edgeSet().size()+" edges.");
		
		jgHost2Poi2 = Main.createIntersectJg(back2, fwd2);
		System.out.println("Pwd Host2 POI2 graph has: "+jgHost2Poi2.vertexSet().size()+" vertices and "+jgHost2Poi2.edgeSet().size()+" edges.");
		
		
		DirectedPseudograph<EntityNode, EventEdge> jgFinal = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
		jgFinal = Main.createUnionJg(jgHost1, jgHost2Poi1);
		jgFinal = Main.createUnionJg(jgFinal, jgHost2Poi2);

		System.out.println("Pwd final graph has: "+jgFinal.vertexSet().size()+" vertices and "+jgFinal.edgeSet().size()+" edges.");

		// Close the driver
        driver.close();
        
		Date endDate = new Date();
		long diff = endDate.getTime() - startDate.getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(diff); 
		System.out.println("Executed in "+seconds+" seconds.");	
	}

}
