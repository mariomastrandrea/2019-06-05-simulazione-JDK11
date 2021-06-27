package it.polito.tdp.crimes;

import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import it.polito.tdp.crimes.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FXMLController 
{
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ComboBox<Year> boxAnno;

    @FXML
    private ComboBox<Month> boxMese;

    @FXML
    private ComboBox<Integer> boxGiorno;

    @FXML
    private Button btnCreaReteCittadina;

    @FXML
    private Button btnSimula;

    @FXML
    private TextField txtN;

    @FXML
    private TextArea txtResult;
    
	private Model model;


    @FXML
    void doCreaReteCittadina(ActionEvent event) 
    {
    	Year selectedYear = this.boxAnno.getValue();
    	
    	if(selectedYear == null)
    	{
    		this.txtResult.setText("Errore: selezionare un anno dal menù a tendina");
    		return;
    	}
    	
    	this.model.createGraph(selectedYear);
    	
    	String graphInfo = this.printGraphInfo();
    	
    	//adjacences
    	TreeMap<Integer, Map<Integer, Double>> districtsAdjacences = this.model.getOrderedDistrictsAdjacences();
    	String graphAdjacences = this.printDistanceOrderedAdjacences(districtsAdjacences);
    	
    	this.txtResult.setText(String.format("%s\n\n%s", graphInfo, graphAdjacences));
    }

    private String printDistanceOrderedAdjacences(TreeMap<Integer, Map<Integer, Double>> districtsAdjacences)
	{
		StringBuilder sb = new StringBuilder();
		
		for(var districtPair : districtsAdjacences.entrySet())
		{
			int district = districtPair.getKey();
			var districtMap = districtPair.getValue();
			
			sb.append("Distretto ").append(district).append(":");
			
			List<Integer> distanceOrderedDistricts = new ArrayList<>(districtMap.keySet());
			distanceOrderedDistricts.sort((d1, d2) -> 
			{
				double distance1 = districtMap.get(d1);
				double distance2 = districtMap.get(d2);
				
				return Double.compare(distance1, distance2);
			});
			
			for(int adjacentDistrict : distanceOrderedDistricts)
			{
				double distance = districtMap.get(adjacentDistrict);
				
				sb.append("\n - ").append(adjacentDistrict).append(" --> ")
					.append(String.format("%.3f km", distance));
			}
			
			sb.append("\n\n");
		}
		
		return sb.toString();
	}

	private String printGraphInfo()
	{
		int numVertices = this.model.getNumVertices();
		int numEdges = this.model.getNumEdges();
		
		if(numVertices == 0)
			return "Errore: il grafo è vuoto!";
		
		String output = String.format("Grafo creato\n#Vertici: %d\n#Archi: %d", numVertices, numEdges);
		return output;
	}

	@FXML
    void doSimula(ActionEvent event) 
    {
		if(!this.model.isGraphCreated())
		{
			this.txtResult.setText("Errore: creare prima il grafo");
			return;
		}
		
		Year selectedYear = this.boxAnno.getValue();
		Month selectedMonth = this.boxMese.getValue();
		Integer selectedDay = this.boxGiorno.getValue();
		
		if(selectedYear == null || selectedMonth == null || selectedDay == null)
		{
			this.txtResult.setText("Errore: selezionare un anno, un mese e un giorno dai menù a tendina");
			return;
		}
		
		String numInput = this.txtN.getText();
		
		if(numInput == null) 
		{
			this.txtResult.setText("Errore: inserire un intero N");
			return;
		}
		
		int num;
		try
		{
			num = Integer.parseInt(numInput.trim());
		}
		catch(NumberFormatException nfe)
		{
			this.txtResult.setText("Errore: inserire un valore intero valido per N");
			return;
		}
		
		if(num < 1 || num > 10)
		{
			this.txtResult.setText("Errore: inserire un valore intero di N compreso tra 1 e 10 (inclusi)");
			return;
		}
		
		LocalDate date = LocalDate.of(selectedYear.getValue(), selectedMonth, selectedDay);
		
		boolean ok = this.model.runSimulation(num, date);
		
		if(!ok)
		{
			this.txtResult.setText("Errore: non esistono eventi criminosi in data " + date + ".\nSelezionare un'altra data");
			return;
		}
		
		int numOfBadManagedEvents = this.model.getNumOfBadManagedEvents();
		
		this.txtResult.setText(String.format(
				"Simulazione effettuata\nData: %02d/%02d/%d\nNumero di eventi criminosi mal gestiti: %d", 
				selectedDay, selectedMonth.getValue(), selectedYear.getValue(), numOfBadManagedEvents));
    }
	
	@FXML
    void handleSelection(ActionEvent event) 
	{
		this.boxGiorno.getItems().clear();
		
		Year selectedYear = this.boxAnno.getValue();
		Month selectedMonth = this.boxMese.getValue();
		
		if(selectedMonth == null || selectedYear == null)
			return;
		
		YearMonth yearMonth = YearMonth.of(selectedYear.getValue(), selectedMonth);
		int lastDay = yearMonth.lengthOfMonth();
		
		for(int i=1; i<=lastDay; i++)
			this.boxGiorno.getItems().add(i);
    }

    @FXML
    void initialize() 
    {
        assert boxAnno != null : "fx:id=\"boxAnno\" was not injected: check your FXML file 'Scene.fxml'.";
        assert boxMese != null : "fx:id=\"boxMese\" was not injected: check your FXML file 'Scene.fxml'.";
        assert boxGiorno != null : "fx:id=\"boxGiorno\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnCreaReteCittadina != null : "fx:id=\"btnCreaReteCittadina\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnSimula != null : "fx:id=\"btnSimula\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtN != null : "fx:id=\"txtN\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Scene.fxml'.";
    }
    
    public void setModel(Model model) 
    {
    	this.model = model;
    	
    	List<Year> allYears = this.model.getAllYears();
    	this.boxAnno.getItems().addAll(allYears);
    	
    	int i = 1;
    	while(i <= 12)
    	{
        	Month m = Month.of(i);
    		this.boxMese.getItems().add(m);
    		i++;
    	}
    }
}

