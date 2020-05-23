package com.mightyjava.Query_and_Ranker;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class Popularity {
	
	 public static Connection con;
	 public static Statement st;
	 public static Statement st_Crawler;
	 public static Statement st_Old;
	 public static Statement st_New;
	 public static Statement st_GetOutBounds;
	 public static ResultSet rs;
	 public static ResultSet rs_IndexedUrls;
	 public static ResultSet rs_GetOutBounds;
	 public static ResultSet rs_OldPopularity;
	 public static ResultSet rs_NewPopularity;
	 public static ResultSet rs_DeleteOldPopularity;
	 
	 public static double UrlPopularity=0.0; // PR(A)
	 public static double LinksPopularity = 0.0; // (1-d) + d*(PR(T)/C(T)

	 public static Set<URL> webLinks = new HashSet<>();
	 public static double d=0.5;
	 public static String tempUrl;


	 

	public static void main(String[] args) {

		 try {
	        	Class.forName("com.mysql.jdbc.Driver");
	        	con=DriverManager.getConnection("jdbc:mysql://localhost/projectdb1?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "root", "");
	        	st = con.createStatement();
	        	st_Crawler = con.createStatement();
	        	st_Old = con.createStatement();
	        	st_New = con.createStatement();
	        	st_GetOutBounds = con.createStatement();
	        }catch(Exception e){
	        	System.out.println(e.getMessage());
	        }    
		String query = "SELECT * FROM `urlspointingtourl`";
		String query_GetOutBounds = "SELECT * FROM `numberoflinksinurl`";
		String query_InsertInOld;
		String query_InsertInNew;
		String query_ReadOld="SELECT * FROM `oldpopularity`";
		String query_ReadNew="SELECT * FROM `newpopularity`";
		String query_DeleteOld="TRUNCATE TABLE `oldpopularity`";
		String query_DeleteNew="TRUNCATE TABLE `newpopularity`";
        String queryIndexedUrls = "SELECT * FROM `indexedurls`";

		try {
			st_Old.executeUpdate(query_DeleteOld);
			st_New.executeUpdate(query_DeleteNew);
			// To Get the Total No. of links in the rs_IndexedUrls and get all the links in the crawler to use it in the oldPopularity table
			rs_IndexedUrls =  st_Crawler.executeQuery(queryIndexedUrls);
        	double TotalNumberOfDocuments=0;
        	while(rs_IndexedUrls.next())
        	{
        		try {
					webLinks.add(new URL(rs_IndexedUrls.getString("URLs")));
					TotalNumberOfDocuments++;
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        	}
        	rs_GetOutBounds=st_GetOutBounds.executeQuery(query_GetOutBounds);
        	List<Integer> OutBoundLinks = new ArrayList<>();
        	while(rs_GetOutBounds.next()){
        		OutBoundLinks.add(Integer.parseInt(rs_GetOutBounds.getString("NumberOfLinks")));
			}
        	// initial popularity
        	double initialValue=1/TotalNumberOfDocuments;
			int Counter=0;
        	// Adding the initial value of popularity to each link and the out bounds links
        	for(URL url :webLinks)
        	{
        		query_InsertInOld = "INSERT INTO `oldpopularity` (`URLs`,`Popularity`,`OutboundLinks`)"
        		 		+ " VALUES ('" + url.toString() + "','" 
        		 		+ initialValue +"','"
        		 		+ OutBoundLinks.get(Counter) +"')";
        		st_Old.executeUpdate(query_InsertInOld);
        		Counter++;
        	}
        	/// here the place for the forLoop
        	for(int i=0;i<2;i++)
        	{
        		System.out.println("Iteration Number:"+i);
        		UrlPopularity=0.0; // PR(A)
      		  	LinksPopularity = 0.0; // (1-d) + d*(PR(T)/C(T)
      		  	Counter =0;
            	// Calculating the popularity
    			rs =  st.executeQuery(query);
    			while(rs.next()){
    				if(!("A new url is comming".equals(rs.getString("URLs"))))
    				{	tempUrl=rs.getString("URLs");
    					rs_OldPopularity =  st_Old.executeQuery(query_ReadOld);
    					while(rs_OldPopularity.next()){
    						if((rs.getString("PointingToIt")).equals(rs_OldPopularity.getString("URLs"))){
    							if((Double.parseDouble(rs_OldPopularity.getString("OutboundLinks"))) !=0)
    									{
    								LinksPopularity+=(Double.parseDouble(rs_OldPopularity.getString("Popularity")))/(Double.parseDouble(rs_OldPopularity.getString("OutboundLinks")));
    									}
    							    
    						}
    						
    						
    					}				 
    				}
    				// insert the urls and its rank in the ranked urls
    				else if(("A new url is comming".equals(rs.getString("URLs")))) {
    				
    					UrlPopularity= (1-d) + d*(LinksPopularity);
    					query_InsertInNew = "INSERT INTO `newpopularity` (`URLs`,`Popularity`,`OutboundLinks`)"
    	        		 		+ " VALUES ('" + tempUrl + "','" 
    	        		 		+ UrlPopularity +"','"
    	        		 		+ OutBoundLinks.get(Counter) +"')";
    					System.out.println("The Popularity of The Url is :" + UrlPopularity);
    					st_New.executeUpdate(query_InsertInNew);
    	        		Counter++;
    	        		UrlPopularity=0.0;
    	        		LinksPopularity=0.0;
    				}

    			}
    			st_Old.executeUpdate(query_DeleteOld);
    			rs_NewPopularity=st_New.executeQuery(query_ReadNew);
    			while(rs_NewPopularity.next()) {
    				query_InsertInOld = "INSERT INTO `oldpopularity` (`URLs`,`Popularity`,`OutboundLinks`)"
            		 		+ " VALUES ('" +rs_NewPopularity.getString("URLs")  + "','" 
            		 		+  Double.parseDouble(rs_NewPopularity.getString("Popularity"))+"','"
            		 		+ Integer.parseInt(rs_NewPopularity.getString("OutboundLinks")) +"')";
            		st_Old.executeUpdate(query_InsertInOld);
    			}
    			st_New.executeUpdate(query_DeleteNew);
        	}
        	
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}