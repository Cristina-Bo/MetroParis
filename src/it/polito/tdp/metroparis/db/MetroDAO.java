package it.polito.tdp.metroparis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.metroparis.model.ConnessioneVelocita;
import it.polito.tdp.metroparis.model.Fermata;
import it.polito.tdp.metroparis.model.Linea;

public class MetroDAO {

	public List<Fermata> getAllFermate() {

		final String sql = "SELECT id_fermata, nome, coordx, coordy FROM fermata ORDER BY nome ASC";
		List<Fermata> fermate = new ArrayList<Fermata>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Fermata f = new Fermata(rs.getInt("id_Fermata"), rs.getString("nome"),
						new LatLng(rs.getDouble("coordx"), rs.getDouble("coordy")));
				fermate.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return fermate;
	}

	public List<Linea> getAllLinee() {
		final String sql = "SELECT id_linea, nome, velocita, intervallo FROM linea ORDER BY nome ASC";

		List<Linea> linee = new ArrayList<Linea>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Linea f = new Linea(rs.getInt("id_linea"), rs.getString("nome"), rs.getDouble("velocita"),
						rs.getDouble("intervallo"));
				linee.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return linee;
	}
	
	public boolean esisteConnessione(Fermata partenza, Fermata arrivo) {
		
		String sql="SELECT COUNT(*) AS cnt " + "FROM connessione " + 
				"WHERE id_stazP=? " + "AND id_stazA=?";
		Connection conn = DBConnect.getConnection();
		PreparedStatement st;
		
		try {
			st = conn.prepareStatement(sql);
			st.setInt(1, partenza.getIdFermata());
			st.setInt(2, arrivo.getIdFermata());
			
			ResultSet rs = st.executeQuery();
			
			rs.next(); //mi posiziono sulla prima e unica riga
			
			int numero = rs.getInt("cnt");
			
			conn.close();
			
			return (numero>0);
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return false;
		
	}

	public List<Fermata> stazioniArrivo(Fermata partenza, Map<Integer, Fermata> idMap) {
		String sql = "SELECT id_stazA "+"FROM connessione "+"WHERE id_stazP=? ";
		Connection conn = DBConnect.getConnection();
		PreparedStatement st;
		
		try {
			st = conn.prepareStatement(sql);
			st.setInt(1, partenza.getIdFermata());
			
			ResultSet rs = st.executeQuery();
			
			List<Fermata> result = new ArrayList<>();
			
			while(rs.next()) {
				result.add(idMap.get(rs.getInt("id_stazA")));
				//result.add(new Fermata(rs.getInt("id_stazA"),null, null));//oggetto effimero NON VA BENE!!
				//non creo la staz part con tutti i campi ma ho gia' i vertici, mi basta collegarli.
				//per fare l'arco cerco la staz con lo stesso id di quella effimera
			}
			conn.close();
			
			return result;
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	public List<ConnessioneVelocita> getConnessioneVelocita(){
		
		String sql = "SELECT connessione.id_stazP, connessione.id_stazA, MAX(linea.velocita) AS velocita "+
				"FROM connessione, linea "+
				"WHERE connessione.id_linea = linea.id_linea "+
				"GROUP BY connessione.id_stazP, connessione.id_stazA ";
		
		
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			
			ResultSet rs = st.executeQuery();
			
			List<ConnessioneVelocita> result = new ArrayList<ConnessioneVelocita>();
			
			while(rs.next()) {
				ConnessioneVelocita item = new ConnessioneVelocita(rs.getInt("id_stazP"), 
						rs.getInt("id_stazA"), rs.getDouble("velocita"));
				result.add(item);
				
			}
			conn.close();
			
			return result;
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}

}
