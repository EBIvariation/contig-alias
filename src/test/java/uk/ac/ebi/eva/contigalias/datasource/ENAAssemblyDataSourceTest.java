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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;

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
public class ENAAssemblyDataSourceTest {

    private static final String GCA_ACCESSION_HAVING_CHROMOSOMES = "GCA_000003055.5";

    @Autowired
    private ENAAssemblyDataSource enaDataSource;

    @Test
    public void testDownloadAssemblyReport() throws IOException {
        Optional<Path> downloadedAssemblyReport = enaDataSource.downloadAssemblyReport(GCA_ACCESSION_HAVING_CHROMOSOMES);
        assertTrue(downloadedAssemblyReport.isPresent());
        assertTrue(Files.exists(downloadedAssemblyReport.get()));
    }

    @Test
    public void getChromosomeEntityFromAssemblyReport() throws IOException {
        Optional<Path> downloadedAssemblyReport = enaDataSource.downloadAssemblyReport(GCA_ACCESSION_HAVING_CHROMOSOMES);
        List<String> chrLines = Files.lines(downloadedAssemblyReport.get())
                .filter(l -> !l.startsWith("accession"))
                .collect(Collectors.toList());
        List<ChromosomeEntity> chromosomeEntityList = enaDataSource.getChromosomeEntityList(chrLines);
        assertEquals(3143, chromosomeEntityList.size());
        chromosomeEntityList.stream().forEach(c -> assertTrue(!c.getEnaSequenceName().isEmpty()));
    }

}
