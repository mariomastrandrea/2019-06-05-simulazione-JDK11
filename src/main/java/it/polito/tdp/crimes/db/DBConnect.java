package it.polito.tdp.crimes.db;


import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnect 
{
	private static final String jdbcURL = "jdbc:mariadb://localhost/denver_crimes";
	private static HikariDataSource ds;
	
	
	public static Connection getConnection() 
	{	
		if (ds == null) 
		{
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(jdbcURL);
			config.setUsername("root");
			config.setPassword("root");
			
			// configurazione MySQL
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
			
			ds = new HikariDataSource(config);
		}
		
		try 
		{	
			return ds.getConnection();
		} 
		catch (SQLException e) 
		{
			System.err.println("Errore connessione al DB");
			throw new RuntimeException(e);
		}
	}


	public static void close(AutoCloseable... resources)
	{
		for(var res : resources)
		{
			try
			{
				res.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException("Error closing resource: " + res.toString(), e);
			}
		}
	}
}