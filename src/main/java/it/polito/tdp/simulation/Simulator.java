package it.polito.tdp.simulation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.crimes.model.Event;

public class Simulator
{
	//constants
	private static final double SPEED_IN_KM_H = 60.0;
	
	//input
	private Graph<Integer, DefaultWeightedEdge> graph;
	@SuppressWarnings("unused")
	private int startDistrict;
	
	//events
	private PriorityQueue<Event> notManagedEvents;
	private PriorityQueue<CrimeEvent> nextEventsQueue;
	
	//world status
	private Map<Agent, Integer> agentsInDistricts;	//agent - districtId
	private LocalDateTime currentTime;
	
	//output
	private int badManagedEvents;
	
	
	public void initialize(Graph<Integer, DefaultWeightedEdge> graph, int startDistrict,
			Collection<Event> events, int numAgents)
	{
		this.graph = graph;
		this.startDistrict = startDistrict;
		
		this.notManagedEvents = new PriorityQueue<>();
		this.nextEventsQueue = new PriorityQueue<>();
		this.nextEventsQueue.addAll(events);
		
		this.currentTime = null;
		this.agentsInDistricts = new HashMap<>();
		
		for(int i=1; i<=numAgents; i++)
		{
			Agent agent = new Agent(i, true);
			this.agentsInDistricts.put(agent, startDistrict);
		}
		
		this.badManagedEvents = 0;
	}
	
	public void run()
	{
		CrimeEvent nextEvent = this.nextEventsQueue.poll();
		
		while(nextEvent != null)
		{
			this.currentTime = nextEvent.getTime();
			
			if(nextEvent instanceof FreeAgentEvent)
			{
				FreeAgentEvent freeAgentEvent = (FreeAgentEvent)nextEvent;
				Agent agentToBeFree = freeAgentEvent.getAgent();
				agentToBeFree.setAvailable(true);
				
				if(!this.notManagedEvents.isEmpty())
				{
					Event notManagedEvent = this.notManagedEvents.poll();
					this.processCrime(notManagedEvent, agentToBeFree);
				}
			}
			else if(nextEvent instanceof Event)
			{
				Event event = (Event)nextEvent;
				
				Agent agentInvolved = this.computeAgentInvolvedIn(event);
				
				if(agentInvolved != null)
					this.processCrime(event, agentInvolved);
				else //no available agents
					this.notManagedEvents.add(event);
			}
			
			nextEvent = this.nextEventsQueue.poll();
		}
	}
	
	private Agent computeAgentInvolvedIn(Event event)
	{
		int district = event.getDistrict_id();
		
		List<Agent> agentsInOrderOfDistance = new ArrayList<>(this.agentsInDistricts.keySet());
		agentsInOrderOfDistance.sort((a1, a2) -> 
		{
			int district1 = this.agentsInDistricts.get(a1);
			int district2 = this.agentsInDistricts.get(a2);
			
			double distance1 = district == district1 ? 0.0 :
					this.graph.getEdgeWeight(this.graph.getEdge(district, district1));
			
			double distance2 = district == district2 ? 0.0 :
					this.graph.getEdgeWeight(this.graph.getEdge(district, district2));
			
			return Double.compare(distance1, distance2);
		});
		
		for(Agent agent : agentsInOrderOfDistance)
		{
			if(agent.isAvailable())	return agent;
		}
		//else
		return null;	//no available agents 
	}

	public void processCrime(Event event, Agent agent)
	{		
		agent.setAvailable(false);
		
		//agent leaves
		int agentDistrict = this.agentsInDistricts.get(agent);
		int eventDistrict = event.getDistrict_id();
		double distance = agentDistrict == eventDistrict ? 0.0 :
				this.graph.getEdgeWeight(this.graph.getEdge(agentDistrict, eventDistrict));
		
		double hourTravelDuration = distance / SPEED_IN_KM_H;
		Duration travelDuration = Duration.ofSeconds((int)(hourTravelDuration * 60.0 * 60.0));
		
		//agent is arrived
		LocalDateTime arrivalTime = this.currentTime.plus(travelDuration);
		this.agentsInDistricts.put(agent, eventDistrict);
		LocalDateTime crimeTime = event.getTime();

		if(Duration.between(crimeTime, arrivalTime).compareTo(Duration.ofMinutes(15)) > 0)
			this.badManagedEvents++;	//agent is late
		
		//agent analyses the crime
		Duration crimeAnalysisDuration = null;
		String category = event.getOffense_category_id();
		
		if(!category.equals("all_other_crimes"))
			crimeAnalysisDuration = Duration.ofHours(2); //hours
		else
			crimeAnalysisDuration = Math.random() < 0.5 ? Duration.ofHours(1) : Duration.ofHours(2);
		
		//generate free agent event
		LocalDateTime timeOfFree = arrivalTime.plus(crimeAnalysisDuration);
		FreeAgentEvent newEvent = new FreeAgentEvent(timeOfFree, agent);
		this.nextEventsQueue.add(newEvent);
	}
	
	public int getNumOfBadManagedEvents()
	{
		return this.badManagedEvents;
	}
}
