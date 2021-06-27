package it.polito.tdp.simulation;

import java.time.LocalDateTime;

public abstract class CrimeEvent implements Comparable<CrimeEvent>
{
	private LocalDateTime time;
	
	
	public CrimeEvent(LocalDateTime time)
	{
		this.time = time;
	}
	
	public LocalDateTime getTime()
	{
		return this.time; 
	}
	
	@Override
	public int compareTo(CrimeEvent other)
	{
		return this.time.compareTo(other.time);
	}
}
