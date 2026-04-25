package com.project.dao;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.project.datasource.DataSource;
import com.project.model.Projekt;

public class ProjektDAOImpl implements ProjektDAO {

    @Override
    public Projekt getProjekt(Integer projektId) {
        String query = "SELECT * FROM projekt WHERE projekt_id = ?";
        try (Connection connect = DataSource.getConnection();
             PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, projektId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Projekt p = new Projekt();
                    p.setProjektId(rs.getInt("projekt_id"));
                    p.setNazwa(rs.getString("nazwa"));
                    p.setOpis(rs.getString("opis"));
                    p.setDataCzasUtworzenia(rs.getObject("dataczas_utworzenia", LocalDateTime.class));
                    p.setDataOddania(rs.getObject("data_oddania", LocalDate.class));
                    return p;
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override
    public void setProjekt(Projekt projekt) {
        boolean isInsert = projekt.getProjektId() == null;
        String query = isInsert ?
            "INSERT INTO projekt (nazwa, opis, dataczas_utworzenia, data_oddania) VALUES (?, ?, ?, ?)"
            : "UPDATE projekt SET nazwa = ?, opis = ?, dataczas_utworzenia = ?, data_oddania = ? WHERE projekt_id = ?";
        try (Connection connect = DataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            prepStmt.setString(1, projekt.getNazwa());
            prepStmt.setString(2, projekt.getOpis());
            if (projekt.getDataCzasUtworzenia() == null) projekt.setDataCzasUtworzenia(LocalDateTime.now());
            prepStmt.setObject(3, projekt.getDataCzasUtworzenia());
            prepStmt.setObject(4, projekt.getDataOddania());
            if (!isInsert) prepStmt.setInt(5, projekt.getProjektId());
            
            int affected = prepStmt.executeUpdate();
            if (isInsert && affected > 0) {
                try (ResultSet keys = prepStmt.getGeneratedKeys()) {
                    if (keys.next()) projekt.setProjektId(keys.getInt(1));
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void deleteProjekt(Integer projektId) {
        String query = "DELETE FROM projekt WHERE projekt_id = ?";
        try (Connection connect = DataSource.getConnection();
             PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, projektId);
            stmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private List<Projekt> fetchProjekty(String query, Object param, Integer offset, Integer limit) {
        List<Projekt> projekty = new ArrayList<>();
        String fullQuery = query + (offset != null ? " OFFSET ?" : "") + (limit != null ? " LIMIT ?" : "");
        try (Connection connect = DataSource.getConnection();
             PreparedStatement stmt = connect.prepareStatement(fullQuery)) {
            int i = 1;
            if (param != null) stmt.setObject(i++, param);
            if (offset != null) stmt.setInt(i++, offset);
            if (limit != null) stmt.setInt(i++, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Projekt p = new Projekt();
                    p.setProjektId(rs.getInt("projekt_id"));
                    p.setNazwa(rs.getString("nazwa"));
                    p.setOpis(rs.getString("opis"));
                    p.setDataCzasUtworzenia(rs.getObject("dataczas_utworzenia", LocalDateTime.class));
                    p.setDataOddania(rs.getObject("data_oddania", LocalDate.class));
                    projekty.add(p);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return projekty;
    }

    private int fetchCount(String query, Object param) {
        try (Connection connect = DataSource.getConnection();
             PreparedStatement stmt = connect.prepareStatement(query)) {
            if (param != null) stmt.setObject(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return 0;
    }

    @Override
    public List<Projekt> getProjekty(Integer offset, Integer limit) {
        return fetchProjekty("SELECT * FROM projekt ORDER BY dataczas_utworzenia DESC", null, offset, limit);
    }

    @Override
    public List<Projekt> getProjektyWhereNazwaLike(String nazwa, Integer offset, Integer limit) {
        return fetchProjekty("SELECT * FROM projekt WHERE LOWER(nazwa) LIKE LOWER(?) ORDER BY dataczas_utworzenia DESC", "%" + nazwa + "%", offset, limit);
    }

    @Override
    public List<Projekt> getProjektyWhereDataOddaniaIs(LocalDate dataOddania, Integer offset, Integer limit) {
        return fetchProjekty("SELECT * FROM projekt WHERE data_oddania = ? ORDER BY dataczas_utworzenia DESC", dataOddania, offset, limit);
    }

    @Override
    public int getRowsNumber() {
        return fetchCount("SELECT COUNT(*) FROM projekt", null);
    }

    @Override
    public int getRowsNumberWhereNazwaLike(String nazwa) {
        return fetchCount("SELECT COUNT(*) FROM projekt WHERE LOWER(nazwa) LIKE LOWER(?)", "%" + nazwa + "%");
    }

    @Override
    public int getRowsNumberWhereDataOddaniaIs(LocalDate dataOddania) {
        return fetchCount("SELECT COUNT(*) FROM projekt WHERE data_oddania = ?", dataOddania);
    }
}