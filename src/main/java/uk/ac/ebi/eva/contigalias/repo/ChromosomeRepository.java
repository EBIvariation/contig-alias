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

package uk.ac.ebi.eva.contigalias.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;

import java.util.Optional;

@Repository
public interface ChromosomeRepository extends JpaRepository<ChromosomeEntity, Long> {

    Optional<ChromosomeEntity> findChromosomeEntityByGenbank(String genbank);

    Optional<ChromosomeEntity> findChromosomeEntityByRefseq(String refseq);

    Page<ChromosomeEntity> findChromosomeEntitiesByAssembly_Genbank(String asmGenbank, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByAssembly_Refseq(String asmRefseq, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByNameAndAssembly_Taxid(String name, long asmTaxid, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByNameAndAssembly(String name, AssemblyEntity assembly,
                                                                   Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByName(String name, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByAssemblyGenbankOrAssemblyRefseq(String genbank, String refseq, Pageable request);

}
