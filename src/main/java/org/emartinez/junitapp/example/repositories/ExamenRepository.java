package org.emartinez.junitapp.example.repositories;

import org.emartinez.junitapp.example.models.Examen;

import java.util.List;

public interface ExamenRepository {
    List<Examen> findAll();
}
