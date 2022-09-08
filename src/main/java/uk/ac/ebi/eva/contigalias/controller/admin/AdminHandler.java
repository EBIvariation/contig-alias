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
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AdminHandler {

    private final AssemblyService assemblyService;

    private final ChromosomeService chromosomeService;

    private final PagedResourcesAssembler<AssemblyEntity> assemblyAssembler;

    @Autowired
    public AdminHandler(AssemblyService assemblyService,
                        ChromosomeService chromosomeService,
                        PagedResourcesAssembler<AssemblyEntity> assemblyAssembler) {
        this.assemblyService = assemblyService;
        this.chromosomeService = chromosomeService;
        this.assemblyAssembler = assemblyAssembler;
    }

    public void fetchAndInsertAssemblyByAccession(String accession) throws IOException {
        assemblyService.fetchAndInsertAssembly(accession);
    }

    public Map<String, List<String>> fetchAndInsertAssemblyByAccession(List<String> accessions) {
        return assemblyService.fetchAndInsertAssembly(accessions);
    }

    public void deleteAssemblyByAccession(String accession) {
        assemblyService.deleteAssemblyByAccession(accession);
    }

    public void putAssemblyChecksumsByAccession(String accession, String md5, String trunc512) {
        assemblyService.putAssemblyChecksumsByAccession(accession, md5, trunc512);
    }

    public void putChromosomeChecksumsByAccession(String accession, String md5, String trunc512) {
        chromosomeService.putChromosomeChecksumsByAccession(accession, md5, trunc512);
    }
}
