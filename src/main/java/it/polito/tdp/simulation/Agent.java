package it.polito.tdp.simulation;

public class Agent
{
	private final int agentId;
	private boolean available;
	
	
	public Agent(int agentId, boolean available)
	{
		this.agentId = agentId;
		this.available = available;
	}

	public boolean isAvailable()
	{
		return this.available;
	}

	public void setAvailable(boolean available) 
	{
		this.available = available;
	}

	public int getAgentId()
	{
		return this.agentId;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + agentId;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Agent other = (Agent) obj;
		if (agentId != other.agentId)
			return false;
		return true;
	}
}
