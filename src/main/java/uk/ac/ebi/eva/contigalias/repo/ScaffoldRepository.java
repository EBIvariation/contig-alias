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
import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;

@Repository
public interface ScaffoldRepository extends JpaRepository<ScaffoldEntity, Long> {

    Page<ScaffoldEntity> findScaffoldEntitiesByGenbank(String genbank, Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByRefseq(String refseq, Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByAssembly_Genbank(String asmGenbank, Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByAssembly_Refseq(String asmRefseq, Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByName(String name, Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByEnaSequenceName(String enaSequenceName, Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByNameAndAssembly_Taxid(String name, long asmTaxid, Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByNameAndAssembly(String name, AssemblyEntity assembly, Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByAssemblyGenbankOrAssemblyRefseq(String accession, String accession1,
                                                                               Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByUcscName(String ucscName, Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByUcscNameAndAssembly_Taxid(String ucscName, long asmTaxid,
                                                                         Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByUcscNameAndAssembly(String ucscName, AssemblyEntity assembly,
                                                                   Pageable request);

    Page<ScaffoldEntity> findScaffoldEntitiesByGenbankOrRefseq(String accession, String accession1, Pageable request);

    long countScaffoldEntitiesByGenbank(String genbank);

    long countScaffoldEntitiesByRefseq(String refseq);

    long countScaffoldEntitiesByAssembly_Genbank(String asmGenbank);

    long countScaffoldEntitiesByAssembly_Refseq(String asmRefseq);

    long countScaffoldEntitiesByNameAndAssembly_Taxid(String name, long asmTaxid);

    long countScaffoldEntitiesByUcscNameAndAssembly_Taxid(String ucscName, long asmTaxid);

    long countScaffoldEntitiesByNameAndAssembly(String name, AssemblyEntity assembly);

    long countScaffoldEntitiesByUcscNameAndAssembly(String ucscName, AssemblyEntity assembly);

    long countScaffoldEntitiesByName(String name);

    long countScaffoldEntitiesByAssemblyGenbankOrAssemblyRefseq(String genbank, String refseq);

    long countScaffoldEntitiesByUcscName(String ucscName);
}