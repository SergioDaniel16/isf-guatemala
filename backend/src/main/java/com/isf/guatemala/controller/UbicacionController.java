package com.isf.guatemala.controller;

import com.isf.guatemala.model.Departamento;
import com.isf.guatemala.model.Municipio;
import com.isf.guatemala.repository.DepartamentoRepository;
import com.isf.guatemala.repository.MunicipioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UbicacionController {
    
    @Autowired
    private DepartamentoRepository departamentoRepository;
    
    @Autowired
    private MunicipioRepository municipioRepository;
    
    @GetMapping("/departamentos")
    public List<Departamento> getDepartamentos() {
        return departamentoRepository.findAll();
    }
    
    @GetMapping("/municipios/{departamentoId}")
    public List<Municipio> getMunicipiosByDepartamento(@PathVariable Long departamentoId) {
        return municipioRepository.findByDepartamentoId(departamentoId);
    }
}
