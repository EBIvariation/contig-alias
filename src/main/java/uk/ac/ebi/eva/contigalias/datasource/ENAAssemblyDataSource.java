/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.contigalias.datasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.dus.ENAAssemblyReportReader;
import uk.ac.ebi.eva.contigalias.dus.ENAAssemblyReportReaderFactory;
import uk.ac.ebi.eva.contigalias.dus.ENABrowser;
import uk.ac.ebi.eva.contigalias.dus.ENABrowserFactory;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Repository("ENADataSource")
public class ENAAssemblyDataSource implements AssemblyDataSource {

    private final ENABrowserFactory factory;

    private final ENAAssemblyReportReaderFactory readerFactory;

    @Autowired
    public ENAAssemblyDataSource(ENABrowserFactory factory,
                                 ENAAssemblyReportReaderFactory readerFactory) {
        this.factory = factory;
        this.readerFactory = readerFactory;
    }

    @Override
    public Optional<AssemblyEntity> getAssemblyByAccession(String accession) throws IOException {
        ENABrowser enaBrowser = factory.build();
        enaBrowser.connect();

        AssemblyEntity assemblyEntity;
        try (InputStream stream = enaBrowser.getAssemblyReportInputStream(accession)) {
            ENAAssemblyReportReader reader = readerFactory.build(stream);
            assemblyEntity = reader.getAssemblyEntity();
        } finally {
            enaBrowser.disconnect();
        }
        return Optional.of(assemblyEntity);
    }

    /**
     * Adds ENA sequence names to chromosomes and scaffolds in an assembly. Will modify the AssemblyEntity in-place.
     *
     * @param optional {@link AssemblyEntity} to add ENA sequence names to
     * @throws IOException Passes IOException thrown by {@link #getAssemblyByAccession(String)}
     */
    public void addENASequenceNamesToAssembly(Optional<AssemblyEntity> optional) throws IOException {
        if (optional.isPresent()) {
            AssemblyEntity targetAssembly = optional.get();
            if (!hasAllEnaSequenceNames(targetAssembly)) {
                String genbank = targetAssembly.getGenbank();
                Optional<AssemblyEntity> enaAssembly = getAssemblyByAccession(genbank);

                if (enaAssembly.isPresent()) {
                    AssemblyEntity sourceAssembly = enaAssembly.get();
                    addENASequenceNames(Objects.nonNull(sourceAssembly.getChromosomes()) ?
                            sourceAssembly.getChromosomes() : Collections.emptyList(),
                            Objects.nonNull(targetAssembly.getChromosomes()) ?
                            targetAssembly.getChromosomes() :  Collections.emptyList());
                    addENASequenceNames(Objects.nonNull(sourceAssembly.getScaffolds()) ?
                            sourceAssembly.getScaffolds() : Collections.emptyList(),
                            Objects.nonNull(targetAssembly.getScaffolds()) ?
                            targetAssembly.getScaffolds() : Collections.emptyList());
                }
            }
        }
    }

    public boolean hasAllEnaSequenceNames(AssemblyEntity assembly) {
        List<ChromosomeEntity> chromosomes = Objects.nonNull(assembly.getChromosomes()) ?
                assembly.getChromosomes() : Collections.emptyList();
        List<ScaffoldEntity> scaffolds = Objects.nonNull(assembly.getScaffolds()) ?
                assembly.getScaffolds() : Collections.emptyList();
        return Stream.concat(chromosomes.stream(), scaffolds.stream())
                     .allMatch(sequence -> sequence.getEnaSequenceName() != null);
    }

    private void addENASequenceNames(
            List<? extends SequenceEntity> sourceSequences, List<? extends SequenceEntity> targetSequences) {
        Map<String, SequenceEntity> genbankToSequenceEntity = new HashMap<>();
        for (SequenceEntity targetSeq : targetSequences) {
            genbankToSequenceEntity.put(targetSeq.getGenbank(), targetSeq);
        }
        for (SequenceEntity sourceSeq : sourceSequences) {
            String sourceGenbank = sourceSeq.getGenbank();
            if (genbankToSequenceEntity.containsKey(sourceGenbank)) {
                genbankToSequenceEntity.get(sourceGenbank).setEnaSequenceName(sourceSeq.getEnaSequenceName());
            } else {
                genbankToSequenceEntity.put(sourceGenbank, sourceSeq);
            }
        }
    }
}
