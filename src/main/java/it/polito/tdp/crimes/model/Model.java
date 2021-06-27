package it.polito.tdp.crimes.model;

import java.time.Year;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.crimes.db.EventsDao;

public class Model 
{
	private final EventsDao dao;
	private Graph<Integer, DefaultWeightedEdge> graph;
	private List<Year> allYears;

	
	public Model() 
	{
		this.dao = new EventsDao();
	}
	
	public List<Year> getAllYears()
	{
		if(this.allYears == null)
			this.allYears = this.dao.getAllYears();
		
		return this.allYears;
	}
	
	public void createGraph(Year selectedYear)
	{
		this.graph = GraphTypeBuilder.<Integer, DefaultWeightedEdge>undirected()
									 .allowingMultipleEdges(false)
									 .allowingSelfLoops(false)
									 .weighted(true)
									 .edgeClass(DefaultWeightedEdge.class)
									 .buildGraph();
		
		//add vertices
		Collection<Integer> districtIDs = this.dao.getAllDistrictIDs();
		Graphs.addAllVertices(this.graph, districtIDs);
		
		//add edges
		Map<Integer, LatLng> districtsGeographicCenters = this.dao.getGeographicCenters(selectedYear);
		
		for(var pair1 : districtsGeographicCenters.entrySet())
		{
			int districtId1 = pair1.getKey();
			LatLng coord1 = pair1.getValue();
			
			for(var pair2 : districtsGeographicCenters.entrySet())
			{
				int districtId2 = pair2.getKey();
				LatLng coord2 = pair2.getValue();
				
				if(!this.graph.containsVertex(districtId1) || !this.graph.containsVertex(districtId2))
					throw new RuntimeException("Error: node not found in graph");	//for debug
				
				if(districtId1 == districtId2 || this.graph.containsEdge(districtId1, districtId2))
					continue;	//no edge to create
					
				double distance = LatLngTool.distance(coord1, coord2, LengthUnit.KILOMETER);
				Graphs.addEdge(this.graph, districtId1, districtId2, distance);
			}
		}		
	}

	public int getNumVertices() { return this.graph.vertexSet().size(); }
	public int getNumEdges() { return this.graph.edgeSet().size(); }
	
	public TreeMap<Integer, Map<Integer, Double>> getOrderedDistrictsAdjacences()
	{
		if(this.graph == null) return null;
		
		TreeMap<Integer, Map<Integer, Double>> orderedMap = new TreeMap<>();
		
		for(int district : this.graph.vertexSet())
		{
			orderedMap.put(district, new HashMap<>());
		}
		
		for(int district : orderedMap.keySet())
		{
			Map<Integer, Double> districtDistances = orderedMap.get(district);
			
			for(var adjacentEdge : this.graph.edgesOf(district))
			{
				int adjacentDistrict = Graphs.getOppositeVertex(this.graph, adjacentEdge, district);
				double distance = this.graph.getEdgeWeight(adjacentEdge);
				
				districtDistances.put(adjacentDistrict, distance);
			}
		}
		
		return orderedMap;
	}
	
}
