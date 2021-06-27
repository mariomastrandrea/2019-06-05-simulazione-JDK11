package it.polito.tdp.crimes.db;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.crimes.model.Event;

public class EventsDao 
{
	public List<Event> listAllEvents()
	{
		String sql = "SELECT * FROM events";
		try 
		{
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			List<Event> list = new ArrayList<>();
			ResultSet res = st.executeQuery();
			
			while(res.next()) 
			{
				try 
				{
					list.add(new Event(res.getLong("incident_id"),
							res.getInt("offense_code"),
							res.getInt("offense_code_extension"), 
							res.getString("offense_type_id"), 
							res.getString("offense_category_id"),
							res.getTimestamp("reported_date").toLocalDateTime(),
							res.getString("incident_address"),
							res.getDouble("geo_lon"),
							res.getDouble("geo_lat"),
							res.getInt("district_id"),
							res.getInt("precinct_id"), 
							res.getString("neighborhood_id"),
							res.getInt("is_crime"),
							res.getInt("is_traffic")));
				} 
				catch (Throwable t) 
				{
					t.printStackTrace();
					System.out.println(res.getInt("id"));
				}
			}
			
			conn.close();
			return list ;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			return null ;
		}
	}

	public List<Year> getAllYears()
	{
		final String sqlQuery = "SELECT DISTINCT YEAR(reported_date) AS year FROM events ORDER BY year ASC";
		
		List<Year> allYears = new ArrayList<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{	
				try
				{
					int yearInt = queryResult.getInt("year");
					Year year = Year.of(yearInt);
					allYears.add(year);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			DBConnect.close(queryResult, statement, connection);
			return allYears;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getAllYears()", sqle);
		}		
	}

	public Collection<Integer> getAllDistrictIDs()
	{
		final String sqlQuery = "SELECT DISTINCT district_id FROM events ORDER BY district_id ASC";
		
		Collection<Integer> allIDs = new ArrayList<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{	
				try
				{
					int districtId = queryResult.getInt("district_id");
					allIDs.add(districtId);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			DBConnect.close(queryResult, statement, connection);
			return allIDs;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getAllDistrictIDs()", sqle);
		}
	}

	public Map<Integer, LatLng> getGeographicCenters(Year selectedYear, 
			Map<Integer, Integer> numOfCrimesByDistrict)
	{
		final String sqlQuery = String.format("%s %s %s %s",
				"SELECT district_id, AVG(geo_lon) AS avgLon, AVG(geo_lat) AS avgLat, COUNT(*) numCrimes",
				"FROM events",
				"WHERE YEAR(reported_date) = ?",
				"GROUP BY district_id");
		
		Map<Integer, LatLng> districtsGeographicCenters = new HashMap<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			statement.setInt(1, selectedYear.getValue());
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{
				int districtId = queryResult.getInt("district_id");
				double avgLongitude = queryResult.getDouble("avgLon");
				double avgLatitude = queryResult.getDouble("avgLat");
				
				LatLng avgCoordinates = new LatLng(avgLatitude, avgLongitude);
				districtsGeographicCenters.put(districtId, avgCoordinates);
				
				int numCrimes = queryResult.getInt("numCrimes");
				numOfCrimesByDistrict.put(districtId, numCrimes);
			}
			
			DBConnect.close(queryResult, statement, connection);
			return districtsGeographicCenters;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getGeographicCenters()", sqle);
		}
	}

	public Collection<Event> getCrimeEventsOn(LocalDate date)
	{
		final String sqlQuery = "SELECT * FROM events WHERE DATE(reported_date) = ?";
		
		Collection<Event> events = new ArrayList<>();
		
		try 
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			statement.setDate(1, Date.valueOf(date));
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next()) 
			{
				try 
				{
					events.add(new Event(queryResult.getLong("incident_id"),
							queryResult.getInt("offense_code"),
							queryResult.getInt("offense_code_extension"), 
							queryResult.getString("offense_type_id"), 
							queryResult.getString("offense_category_id"),
							queryResult.getTimestamp("reported_date").toLocalDateTime(),
							queryResult.getString("incident_address"),
							queryResult.getDouble("geo_lon"),
							queryResult.getDouble("geo_lat"),
							queryResult.getInt("district_id"),
							queryResult.getInt("precinct_id"), 
							queryResult.getString("neighborhood_id"),
							queryResult.getInt("is_crime"),
							queryResult.getInt("is_traffic")));
				} 
				catch (Throwable t) 
				{
					t.printStackTrace();
				}
			}
			
			DBConnect.close(queryResult, statement, connection);
			return events;
		} 
		catch (SQLException sqle) 
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getCrimeEventsOn()", sqle);
		}
	}
}
