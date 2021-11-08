package org.emartinez.junitapp.example.services;

import org.emartinez.junitapp.example.models.Examen;

public interface ExamenService {
    Examen findExamenPorNombre(String nombre);
}
