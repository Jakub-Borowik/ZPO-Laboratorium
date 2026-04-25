package com.project.dao;
import java.util.List;
import com.project.model.Zadanie;

public interface ZadanieDAO {
    List<Zadanie> getZadaniaByProjektId(Integer projektId);
    void setZadanie(Zadanie zadanie);
    void deleteZadanie(Integer zadanieId);
}