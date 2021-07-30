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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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
     * Adds ENA sequence names to chromosomes and scaffolds in an assembly.
     *
     * @param optional {@link AssemblyEntity} to get ENA sequence names for
     * @throws IOException Passes IOException thrown by {@link #getAssemblyByAccession(String)}
     */
    public void getENASequenceNamesForAssembly(Optional<AssemblyEntity> optional) throws IOException {
        if (optional.isPresent()) {
            AssemblyEntity targetAssembly = optional.get();
            if (!hasAllEnaSequenceNames(targetAssembly)) {
                String genbank = targetAssembly.getGenbank();
                Optional<AssemblyEntity> enaAssembly = getAssemblyByAccession(genbank);

                if (enaAssembly.isPresent()) {
                    AssemblyEntity sourceAssembly = enaAssembly.get();
                    putENASequenceNames(sourceAssembly.getChromosomes(), targetAssembly.getChromosomes());
                    putENASequenceNames(sourceAssembly.getScaffolds(), targetAssembly.getScaffolds());
                }
            }
        }
    }

    public boolean hasAllEnaSequenceNames(AssemblyEntity assembly) {
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        List<ScaffoldEntity> scaffolds = assembly.getScaffolds();
        return Stream.concat(chromosomes.stream(), scaffolds.stream())
                     .allMatch(sequence -> sequence.getEnaSequenceName() != null);
    }

    private void putENASequenceNames(
            List<? extends SequenceEntity> sourceSequences, List<? extends SequenceEntity> targetSequences) {
        Stream.concat(targetSequences.stream(), sourceSequences.stream())
              .collect(Collectors.toMap(SequenceEntity::getGenbank, Function.identity(),
                                        (targetSeq, sourceSeq) -> {
                                            targetSeq.setEnaSequenceName(sourceSeq.getEnaSequenceName());
                                            return targetSeq;
                                        }));

    }

}
