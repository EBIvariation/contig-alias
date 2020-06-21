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

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;

import java.util.Optional;

@Repository
public interface AssemblyRepository extends JpaRepository<AssemblyEntity, Long>,
        JpaSpecificationExecutor<AssemblyEntity> {

    default Optional<AssemblyEntity> findAssemblyEntityByAccession(String accession) {
        return this.findAssemblyEntityByGenbankOrRefseq(accession, accession);
    }

    Optional<AssemblyEntity> findAssemblyEntityByGenbankOrRefseq(String genbank, String refseq);

    long count();

    Optional<AssemblyEntity> findTopByIdNotNullOrderById();

    Optional<AssemblyEntity> findAssemblyEntityByGenbank(String genbank);

    Optional<AssemblyEntity> findAssemblyEntityByRefseq(String refseq);

    Slice<AssemblyEntity> findAssemblyEntitiesByTaxid(long taxid, Pageable pageable);

}
