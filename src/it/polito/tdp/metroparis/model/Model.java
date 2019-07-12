package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private class EdgeTraversedGraphListener implements TraversalListener<Fermata, DefaultWeightedEdge>{

		@Override
		public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> ev) {
			Fermata sourceVertex = grafo.getEdgeSource(ev.getEdge());	
			Fermata targetVertex = grafo.getEdgeTarget(ev.getEdge());
			
			/* se il grafo e' orientato, allora source==parent, target==child*/
			 /* se il grafo non e' orientato potrebbe anche essere il contrario... */
			
			if(!backVisit.containsKey(targetVertex) && backVisit.containsKey(sourceVertex)) {
				backVisit.put(targetVertex, sourceVertex);
			}else if(!backVisit.containsKey(sourceVertex) && backVisit.containsKey(targetVertex)) {
				backVisit.put(sourceVertex, targetVertex);
			}
			
		}

		@Override
		public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
			// TODO Auto-generated method stub
			
		}
		// costruisco la classe come privata ed interna al model, ha accesso a tutte le variaabili del model
		//non possono modificarla dall'esterno perche' e' private
		
		
	}
	
	private Graph<Fermata, DefaultWeightedEdge> grafo; //arco orientato non pesato
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	Map<Fermata, Fermata> backVisit;
	
	
	
	public void creaGrafo() {
		// creare oggetto grafo
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiungo i vertici
		MetroDAO dao = new MetroDAO();
		this.fermate = dao.getAllFermate();
		Graphs.addAllVertices(this.grafo, this.fermate);
		
		// crea idMap
		this.fermateIdMap = new HashMap<>();
		for(Fermata f: this.fermate)
			fermateIdMap.put(f.getIdFermata(), f);
		
		//Aggiungere gli archi(opzione 1)
		/*
		for(Fermata partenza :this.grafo.vertexSet()) {//tutte le fermate del grafo
			for(Fermata arrivo:this.grafo.vertexSet()) {
				
				if(dao.esisteConnessione(partenza, arrivo)) {
					this.grafo.addEdge(partenza, arrivo);
				}
			}
		}*/
		//Aggiungere gli archi(opzione 2)
		//for(Fermata partenza :this.grafo.vertexSet()) {//tutte le fermate del grafo
		//List<Fermata> arrivi = dao.stazioniArrivo(partenza, fermateIdMap);
		
		//for(Fermata arrivo: arrivi)
		//	this.grafo.addEdge(partenza, arrivo);
		// NON VA BENE IL VERTICE O ARCO PARZIALMENTE CREATO!!! O EFFIMERO
		//}
		
		//Aggiungere gli archi(opzione 3)
		//mi faccio passare dal db gia' le coppie di stazioni
		
		// Aggiungi i pesi agli archi
		List<ConnessioneVelocita> archiPesati = dao.getConnessioneVelocita();
		for(ConnessioneVelocita cp : archiPesati) {
			Fermata partenza = fermateIdMap.get(cp.getStazP());
			Fermata arrivo = fermateIdMap.get(cp.getStazA());
			
			double distanza = LatLngTool.distance(partenza.getCoords(), arrivo.getCoords(), LengthUnit.KILOMETER);
			double peso = distanza/cp.getVelocita()*3600;//*3600 converte ore in secondi
			
			//grafo.setEdgeWeight(partenza, arrivo, peso);
			// aggiungo archi e pesi insieme
			Graphs.addEdgeWithVertices(grafo, partenza, arrivo, peso);
		}
		
	}
	
	public List<Fermata> fermateRaggiungibili(Fermata source){
		
		List<Fermata> result = new ArrayList<Fermata>();
		backVisit = new HashMap<>();
		
		
		//GraphIterator<Fermata, DefaultEdge> it = new BreadthFirstIterator<>(this.grafo, source);
		GraphIterator<Fermata, DefaultWeightedEdge> it = new DepthFirstIterator<>(this.grafo, source);
		// iteratore serve per una SOLA iterazione e poi devo farne uno nuovo per una nuova iterazione di tutto il grafo
		
		it.addTraversalListener(new Model.EdgeTraversedGraphListener());
		backVisit.put(source, null);//nodo radice
		
		while(it.hasNext()) {
			result.add(it.next());
		}
		
		//System.out.println(back);
		
		return result;
		//non e' un cammino
	}
	
	public List<Fermata> percorsoFinoA(Fermata target){
		if(!backVisit.containsKey(target)) {
			// il target non e' raggiunggibile dalla source
			return null;
		}
		
		List<Fermata> percorso = new LinkedList<>();
		Fermata f = target;
		
		while(f!= null) {
		//percorso.add(f);
		percorso.add(0, f);
		//aggiungo sempre nella prima posizione
		f=backVisit.get(f);
		}
		
		return percorso;
		
	}
	
	
	public List<Fermata> trovaCamminoMinimo(Fermata partenza, Fermata arrivo){
		DijkstraShortestPath<Fermata, DefaultWeightedEdge> dijstra = new DijkstraShortestPath<>(this.grafo);
		GraphPath<Fermata, DefaultWeightedEdge> path = dijstra.getPath(partenza, arrivo);
		return path.getVertexList();
	}

	
	
	public Graph<Fermata, DefaultWeightedEdge> getGrafo() {
		return grafo;
	}

	public List<Fermata> getFermate() {
		return fermate;
	}
	
	
	

}
