/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.contigalias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;

import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;

@Service
public class AliasService {

    private final ChromosomeService chromosomeService;

    @Autowired
    public AliasService(ChromosomeService chromosomeService) {
        this.chromosomeService = chromosomeService;
    }

    public Optional<AssemblyEntity> getAssemblyByChromosomeGenbank(String chrGenbank) {
        Page<ChromosomeEntity> chromosomeByGenbank
                = chromosomeService.getChromosomeByGenbank(chrGenbank, DEFAULT_PAGE_REQUEST);
        return extractAssemblyFromChromosomePage(chromosomeByGenbank);
    }

    public Optional<AssemblyEntity> getAssemblyByChromosomeRefseq(String chrRefseq) {
        Page<ChromosomeEntity> chromosomeByGenbank
                = chromosomeService.getChromosomeByRefseq(chrRefseq, DEFAULT_PAGE_REQUEST);
        return extractAssemblyFromChromosomePage(chromosomeByGenbank);
    }

    public Optional<AssemblyEntity> extractAssemblyFromChromosomePage(Page<ChromosomeEntity> page) {
        Optional<ChromosomeEntity> chromosomeEntity = page.get().findFirst();
        return chromosomeEntity.map(ChromosomeEntity::getAssembly);
    }

    public List<ChromosomeEntity> getChromosomesByAssemblyGenbank(String asmGenbank) {
        return chromosomeService.getChromosomesByAssemblyGenbank(asmGenbank);
    }

    public List<ChromosomeEntity> getChromosomesByAssemblyRefseq(String asmRefseq) {
        return chromosomeService.getChromosomesByAssemblyRefseq(asmRefseq);
    }

}
