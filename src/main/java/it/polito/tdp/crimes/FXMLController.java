package it.polito.tdp.crimes;

import java.net.URL;
import java.time.Year;
import java.util.List;
import java.util.ResourceBundle;

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
    private ComboBox<?> boxMese;

    @FXML
    private ComboBox<?> boxGiorno;

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
    	
    	String output = this.printGraphInfo();
    	this.txtResult.setText(output);
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
    }
}

