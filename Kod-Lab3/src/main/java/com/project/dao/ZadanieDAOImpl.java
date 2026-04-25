package com.project.dao;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.project.datasource.DataSource;
import com.project.model.Zadanie;

public class ZadanieDAOImpl implements ZadanieDAO {
    @Override
    public List<Zadanie> getZadaniaByProjektId(Integer projektId) {
        List<Zadanie> zadania = new ArrayList<>();
        String query = "SELECT * FROM zadanie WHERE projekt_id = ? ORDER BY kolejnosc ASC";
        try (Connection connect = DataSource.getConnection();
             PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, projektId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Zadanie z = new Zadanie();
                    z.setZadanieId(rs.getInt("zadanie_id"));
                    z.setNazwa(rs.getString("nazwa"));
                    z.setOpis(rs.getString("opis"));
                    z.setKolejnosc(rs.getInt("kolejnosc"));
                    z.setDataCzasUtworzenia(rs.getObject("dataczas_utworzenia", LocalDateTime.class));
                    z.setProjektId(rs.getInt("projekt_id"));
                    zadania.add(z);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return zadania;
    }

    @Override
    public void setZadanie(Zadanie zadanie) {
        boolean isInsert = zadanie.getZadanieId() == null;
        String query = isInsert ?
            "INSERT INTO zadanie (nazwa, opis, kolejnosc, dataczas_utworzenia, projekt_id) VALUES (?, ?, ?, ?, ?)"
            : "UPDATE zadanie SET nazwa = ?, opis = ?, kolejnosc = ?, dataczas_utworzenia = ?, projekt_id = ? WHERE zadanie_id = ?";
        try (Connection connect = DataSource.getConnection();
             PreparedStatement stmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, zadanie.getNazwa());
            stmt.setString(2, zadanie.getOpis());
            stmt.setObject(3, zadanie.getKolejnosc(), Types.INTEGER);
            if (zadanie.getDataCzasUtworzenia() == null) zadanie.setDataCzasUtworzenia(LocalDateTime.now());
            stmt.setObject(4, zadanie.getDataCzasUtworzenia());
            stmt.setInt(5, zadanie.getProjektId());
            if (!isInsert) stmt.setInt(6, zadanie.getZadanieId());
            
            int affected = stmt.executeUpdate();
            if (isInsert && affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) zadanie.setZadanieId(keys.getInt(1));
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void deleteZadanie(Integer zadanieId) {
        String query = "DELETE FROM zadanie WHERE zadanie_id = ?";
        try (Connection connect = DataSource.getConnection();
             PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, zadanieId);
            stmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}