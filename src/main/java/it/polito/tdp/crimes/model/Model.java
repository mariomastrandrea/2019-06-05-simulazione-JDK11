package it.polito.tdp.crimes.model;

import java.time.LocalDate;
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
import it.polito.tdp.simulation.Simulator;

public class Model 
{
	private final EventsDao dao;
	private Graph<Integer, DefaultWeightedEdge> graph;
	private List<Year> allYears;
	private Map<Integer, Integer> numOfCrimesByDistrict;
	private final Simulator simulator;

	
	public Model() 
	{
		this.dao = new EventsDao();
		this.simulator = new Simulator();
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
		
		
		this.numOfCrimesByDistrict = new HashMap<>();
		//add edges
		Map<Integer, LatLng> districtsGeographicCenters = this.dao.getGeographicCenters(selectedYear, this.numOfCrimesByDistrict);
		
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
	
	public boolean isGraphCreated()
	{
		return this.graph != null;
	}
	
	public boolean runSimulation(int numAgents, LocalDate date)
	{
		if(this.graph == null || numAgents < 1 || numAgents > 10 || 
				date == null || this.numOfCrimesByDistrict.isEmpty()) 
			throw new RuntimeException("Error in runSimulation()");
		
		Collection<Event> eventsInDate = this.dao.getCrimeEventsOn(date);
		
		if(eventsInDate.isEmpty()) return false;
		
		//compute the less crimes district
		int startDistrict = Integer.MAX_VALUE;
		int minCrimes = Integer.MAX_VALUE;
		
		for(int district : this.numOfCrimesByDistrict.keySet())
		{
			int numCrimes = this.numOfCrimesByDistrict.get(district);
			
			if(numCrimes < minCrimes)
			{
				minCrimes = numCrimes;
				startDistrict = district;
			}
		}
		
		//initialise and run simulation
		this.simulator.initialize(this.graph, startDistrict, eventsInDate, numAgents);
		this.simulator.run();
		
		return true;
	}
	
	public int getNumOfBadManagedEvents()
	{
		return this.simulator.getNumOfBadManagedEvents();
	}
	
}
