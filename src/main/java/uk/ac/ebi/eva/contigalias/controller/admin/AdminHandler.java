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
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;

import java.io.IOException;
import java.util.List;

import static uk.ac.ebi.eva.contigalias.controller.BaseHandler.convertToList;

@Service
public class AdminHandler {

    private final AssemblyService service;

    @Autowired
    public AdminHandler(AssemblyService service) {
        this.service = service;
    }

    public List<AssemblyEntity> getAssemblyOrFetchByAccession(String accession) throws IOException {
        return convertToList(service.getAssemblyOrFetchByAccession(accession));
    }

    public void fetchAndInsertAssemblyByAccession(String accession) throws IOException {
        service.fetchAndInsertAssembly(accession);
    }

    public void fetchAndInsertAssemblyByAccession(List<String> accessions) {
        service.fetchAndInsertAssembly(accessions);
    }

    public void deleteAssemblyByAccession(String accession) {
        service.deleteAssemblyByAccession(accession);
    }

}
