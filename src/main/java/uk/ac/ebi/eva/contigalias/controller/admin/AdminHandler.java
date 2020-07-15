/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.contigalias.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;

import java.io.IOException;
import java.util.List;

import static uk.ac.ebi.eva.contigalias.controller.BaseController.BAD_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createAppropriateResponseEntity;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.paramsValidForSingleResponseQuery;

@Service
public class AdminHandler {

    private final AssemblyService service;

    @Autowired
    public AdminHandler(AssemblyService service) {
        this.service = service;
    }

    public ResponseEntity<List<AssemblyEntity>> getAssemblyOrFetchByAccession(
            String accession, Integer pageNumber, Integer pageSize) throws IOException {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            List<AssemblyEntity> entities;
            try {
                entities = service.getAssemblyOrFetchByAccession(accession);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return createAppropriateResponseEntity(entities);
        } else return BAD_REQUEST;
    }

    public ResponseEntity<?> fetchAndInsertAssemblyByAccession(String accession) throws IOException {
        try {
            service.fetchAndInsertAssembly(accession);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> fetchAndInsertAssemblyByAccession(List<String> accessions) {
        if (accessions == null || accessions.size() <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        service.fetchAndInsertAssembly(accessions);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public void deleteAssemblyByAccession(String accession) {
        service.deleteAssemblyByAccession(accession);
    }

}
