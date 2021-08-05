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

@Repository
public interface ChromosomeRepository extends JpaRepository<ChromosomeEntity, Long> {

    Page<ChromosomeEntity> findChromosomeEntitiesByGenbank(String genbank, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByRefseq(String refseq, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByGenbankOrRefseq(String genbank, String refseq, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByAssembly_Genbank(String asmGenbank, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByAssembly_Refseq(String asmRefseq, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByGenbankSequenceNameAndAssembly_Taxid(String genbankName, long asmTaxid, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByUcscNameAndAssembly_Taxid(String ucscName, long asmTaxid,
                                                                             Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByEnaSequenceNameAndAssembly_Taxid(String enaName, long asmTaxid,
                                                                                    Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByGenbankSequenceNameAndAssembly(String genbankName, AssemblyEntity assembly,
                                                                                  Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByUcscNameAndAssembly(String ucscName, AssemblyEntity assembly,
                                                                       Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByEnaSequenceNameAndAssembly(String enaName, AssemblyEntity assembly,
                                                                              Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByGenbankSequenceName(String genbankName, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByEnaSequenceName(String enaSequenceName, Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByAssemblyGenbankOrAssemblyRefseq(String genbank, String refseq,
                                                                                   Pageable request);

    Page<ChromosomeEntity> findChromosomeEntitiesByUcscName(String ucscName, Pageable request);

    long countChromosomeEntitiesByGenbank(String genbank);

    long countChromosomeEntitiesByRefseq(String refseq);

    long countChromosomeEntitiesByAssembly_Genbank(String asmGenbank);

    long countChromosomeEntitiesByAssembly_Refseq(String asmRefseq);

    long countChromosomeEntitiesByGenbankSequenceNameAndAssembly_Taxid(String genbankName, long asmTaxid);

    long countChromosomeEntitiesByUcscNameAndAssembly_Taxid(String ucscName, long asmTaxid);

    long countChromosomeEntitiesByEnaSequenceNameAndAssembly_Taxid(String enaName, long asmTaxid);

    long countChromosomeEntitiesByGenbankSequenceNameAndAssembly(String genbankName, AssemblyEntity assembly);

    long countChromosomeEntitiesByUcscNameAndAssembly(String ucscName, AssemblyEntity assembly);

    long countChromosomeEntitiesByEnaSequenceNameAndAssembly(String enaName, AssemblyEntity assembly);

    long countChromosomeEntitiesByGenbankSequenceName(String genbankName);

    long countChromosomeEntitiesByAssemblyGenbankOrAssemblyRefseq(String genbank, String refseq);

    long countChromosomeEntitiesByUcscName(String ucscName);

    long countChromosomeEntitiesByEnaSequenceName(String enaName);

}
