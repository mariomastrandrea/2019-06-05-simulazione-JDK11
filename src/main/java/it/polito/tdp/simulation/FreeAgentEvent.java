package it.polito.tdp.simulation;

import java.time.LocalDateTime;

public class FreeAgentEvent extends CrimeEvent
{
	private Agent agent;

	public FreeAgentEvent(LocalDateTime time, Agent agent)
	{
		super(time);
		this.agent = agent;
	}
	
	public Agent getAgent()
	{
		return this.agent;
	}

}
