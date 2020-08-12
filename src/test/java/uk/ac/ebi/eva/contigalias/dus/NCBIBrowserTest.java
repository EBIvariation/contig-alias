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

package uk.ac.ebi.eva.contigalias.dus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class NCBIBrowserTest {

    @Autowired
    private NCBIBrowser ncbiBrowser;

    @BeforeEach
    void setUp() throws IOException {
        ncbiBrowser.connect();
    }

    @AfterEach
    void tearDown() throws IOException {
        ncbiBrowser.disconnect();
    }

    @Test
    void connect() throws IOException {
        ncbiBrowser.connect();
    }

    @Test
    void navigateToAllGenomesDirectory() throws IOException {
        assertTrue(ncbiBrowser.changeWorkingDirectory(NCBIBrowser.PATH_GENOMES_ALL));
        assertTrue(ncbiBrowser.listFiles().length > 0);
    }

    @Test
    void navigateToSubDirectoryPath() throws IOException {
        ncbiBrowser.changeWorkingDirectory("/genomes/INFLUENZA/");
        assertTrue(ncbiBrowser.listFiles().length > 0);
    }

    @Test
    void getGenomeReportDirectoryGCATest() throws IOException, IllegalArgumentException {
        Optional<String> path = ncbiBrowser.getGenomeReportDirectory("GCA_004051055.1");
        assertTrue(path.isPresent());
        assertEquals("/genomes/all/GCA/004/051/055/GCA_004051055.1_ASM405105v1/", path.get());
    }

    @Test
    void getGenomeReportDirectoryGCFTest() throws IOException, IllegalArgumentException {
        Optional<String> path = ncbiBrowser.getGenomeReportDirectory("GCF_007608995.1");
        assertTrue(path.isPresent());
        assertEquals("/genomes/all/GCF/007/608/995/GCF_007608995.1_ASM760899v1/", path.get());
    }

    @Test
    void getAssemblyReportInputStream() throws IOException {
        try (InputStream stream = ncbiBrowser.getAssemblyReportInputStream(
                "/genomes/all/GCF/007/608/995/GCF_007608995.1_ASM760899v1/")) {
            assertTrue(stream.read() != -1);
        }
    }
}
