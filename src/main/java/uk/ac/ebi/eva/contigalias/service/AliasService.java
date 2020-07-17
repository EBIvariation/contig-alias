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

package uk.ac.ebi.eva.contigalias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;

import java.util.List;
import java.util.Optional;

@Service
public class AliasService {

    private final ChromosomeService chromosomeService;

    @Autowired
    public AliasService(ChromosomeService chromosomeService) {
        this.chromosomeService = chromosomeService;
    }

    public Optional<AssemblyEntity> getAssemblyByChromosomeGenbank(String chrGenbank) {
        Optional<ChromosomeEntity> entity = chromosomeService.getChromosomeByGenbank(chrGenbank);
        return extractAssemblyFromChromosome(entity);
    }

    public Optional<AssemblyEntity> getAssemblyByChromosomeRefseq(String chrRefseq) {
        Optional<ChromosomeEntity> entity = chromosomeService.getChromosomeByRefseq(chrRefseq);
        return extractAssemblyFromChromosome(entity);
    }

    public Optional<AssemblyEntity> extractAssemblyFromChromosome(Optional<ChromosomeEntity> entity) {
        return entity.map(ChromosomeEntity::getAssembly);
    }

    public List<ChromosomeEntity> getChromosomesByAssemblyGenbank(String asmGenbank) {
        return chromosomeService.getChromosomesByAssemblyGenbank(asmGenbank);
    }

    public List<ChromosomeEntity> getChromosomesByAssemblyRefseq(String asmRefseq) {
        return chromosomeService.getChromosomesByAssemblyRefseq(asmRefseq);
    }

    List<ChromosomeEntity> getChromosomesByNameAndAssemblyTaxid(String name, long asmTaxid) {
        return chromosomeService.getChromosomesByNameAndAssemblyTaxid(name, asmTaxid);
    }

}
