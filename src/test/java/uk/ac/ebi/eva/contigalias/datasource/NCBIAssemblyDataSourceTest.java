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

package uk.ac.ebi.eva.contigalias.datasource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class NCBIAssemblyDataSourceTest {

    private static final String GCA_ACCESSION_HAVING_CHROMOSOMES = "GCA_000003055.5";

    private static final String GCF_ACCESSION_NO_CHROMOSOMES = "GCF_006125015.1";

    @Autowired
    private NCBIAssemblyDataSource dataSource;

    @Test
    public void testDownloadAssemblyReport() throws IOException {
        Optional<Path> downloadedAssemblyReport = dataSource.downloadAssemblyReport(GCA_ACCESSION_HAVING_CHROMOSOMES);
        assertTrue(downloadedAssemblyReport.isPresent());
        assertTrue(Files.exists(downloadedAssemblyReport.get()));
    }

    @Test
    public void getAssemblyByAccessionGCAHavingChromosomes() throws IOException {
        Optional<Path> downloadedAssemblyReport = dataSource.downloadAssemblyReport(GCA_ACCESSION_HAVING_CHROMOSOMES);
        AssemblyEntity assembly = dataSource.getAssemblyEntity(downloadedAssemblyReport.get());
        assertEquals(GCA_ACCESSION_HAVING_CHROMOSOMES, assembly.getInsdcAccession());
        List<String> chrLines = Files.lines(downloadedAssemblyReport.get())
                .filter(l -> !l.startsWith("#"))
                .collect(Collectors.toList());
        List<ChromosomeEntity> chromosomeEntityList = dataSource.getChromosomeEntityList(assembly, chrLines);
        assertEquals(3143, chromosomeEntityList.size());
    }

    @Test
    public void getAssemblyByAccessionGCFNoChromosomes() throws IOException {
        Optional<Path> downloadedAssemblyReport = dataSource.downloadAssemblyReport(GCF_ACCESSION_NO_CHROMOSOMES);
        AssemblyEntity assembly = dataSource.getAssemblyEntity(downloadedAssemblyReport.get());
        assertEquals("GCA_006125015.1", assembly.getInsdcAccession());
        List<String> chrLines = Files.lines(downloadedAssemblyReport.get())
                .filter(l -> !l.startsWith("#"))
                .collect(Collectors.toList());
        List<ChromosomeEntity> chromosomeEntityList = dataSource.getChromosomeEntityList(assembly, chrLines);
        long numOfChromosomes = chromosomeEntityList.stream()
                .filter(c -> c.getContigType() == SequenceEntity.ContigType.CHROMOSOME).count();
        long numOfScaffolds = chromosomeEntityList.stream()
                .filter(c -> c.getContigType() == SequenceEntity.ContigType.SCAFFOLD).count();
        assertEquals(0, numOfChromosomes);
        assertEquals(2, numOfScaffolds);
    }

}
