package it.polito.tdp.metroparis;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.metroparis.model.Fermata;
import it.polito.tdp.metroparis.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public class MetroController {

	private Model model;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ComboBox<Fermata> boxPartenza;

	@FXML
	private ComboBox<Fermata> boxArrivo;

	@FXML
	private TextArea txtResult;

	@FXML
	void handleCerca(ActionEvent event) {

		Fermata partenza = boxPartenza.getValue();
		Fermata arrivo = boxArrivo.getValue();

		if (partenza == null || arrivo == null) {
			txtResult.appendText("Errore: devi selezionare entrambe le fermate\n");
			return;
		}
		if (partenza.equals(arrivo)) {
			txtResult.appendText("Errore: devi selezionare fermate diverse\n");
			return;
		}

		model.fermateRaggiungibili(partenza);
		List<Fermata> percorso = model.trovaCamminoMinimo(partenza, arrivo);
		
		txtResult.appendText(String.format("Percorso da %s a %s (%d fermate):\n", partenza.getNome(),
				arrivo.getNome(), percorso.size()));
		int c=0;
		for (Fermata f : percorso) {
			c++;
			txtResult.appendText(String.format("%2d. %s\n", c, f.getNome()));
		}

	}

	@FXML
	void initialize() {
		assert boxPartenza != null : "fx:id=\"boxPartenza\" was not injected: check your FXML file 'Metro.fxml'.";
		assert boxArrivo != null : "fx:id=\"boxArrivo\" was not injected: check your FXML file 'Metro.fxml'.";
		assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Metro.fxml'.";

	}

	public void setModel(Model m) {
		this.model = m;
		model.creaGrafo();
		boxPartenza.getItems().addAll(model.getFermate());
		boxArrivo.getItems().addAll(model.getFermate());
	}
}