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

package uk.ac.ebi.eva.contigalias.dus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class ENABrowserTest {

    @Autowired
    private ENABrowserFactory factory;

    private ENABrowser enaBrowser;

    @BeforeEach
    void setUp() throws IOException {
        enaBrowser = factory.build();
        enaBrowser.connect();
    }

    @AfterEach
    void tearDown() throws IOException {
        enaBrowser.disconnect();
    }

    @Test
    void connect() throws IOException {
        enaBrowser.connect();
    }

    @Test
    void navigateToENAAssemblyDirectory() throws IOException {
        assertTrue(enaBrowser.changeWorkingDirectory(ENABrowser.PATH_ENA_ASSEMBLY));
        assertTrue(enaBrowser.listFiles().length > 0);
    }

    @Test
    void getAssemblyReportInputStream() throws IOException {
        try (InputStream stream = enaBrowser.getAssemblyReportInputStream("GCA_003005035.1")) {
            assertTrue(stream.read() != -1);
        }
    }
}
