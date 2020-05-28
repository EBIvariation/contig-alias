package com.ebivariation.contigalias.controller;

import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.service.AssemblyService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequestMapping("contig-alias-admin")
@RestController
public class AdminController {

    private final AssemblyService service;

    public AdminController(AssemblyService service) {
        this.service = service;
    }

    @GetMapping(value = "assemblies/{accession}")
    public Optional<AssemblyEntity> getAssemblyOrFetchByAccession(@PathVariable String accession) throws IOException {
        return service.getAssemblyOrFetchByAccession(accession);
    }

    @PostMapping(value = "assemblies/{accession}")
    public void fetchAndInsertAssemblyByAccession(@PathVariable String accession) throws IOException {
        service.fetchAndInsertAssembly(accession);
    }

    @PostMapping(value = "assemblies")
    public void fetchAndInsertAssemblyByAccession(@RequestBody Optional<List<String>> accessions) {
        accessions.ifPresentOrElse((list -> {
            if (list.size() > 0) {
                service.fetchAndInsertAssembly(list);
            } else throw new IllegalArgumentException("List of accessions can not be empty!");
        }), (() -> {
            throw new IllegalArgumentException("List of accessions must be provided!");
        }));
    }

    @DeleteMapping(value = "assemblies/{accession}")
    public void deleteAssemblyByAccession(@PathVariable String accession) {
        service.deleteAssembly(accession);
    }

}
