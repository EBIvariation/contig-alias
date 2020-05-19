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

package com.ebivariation.contigalias;

import com.ebivariation.contigalias.dus.FTPBrowser;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FTPBrowserTest {

    private static final String SERVER_NCBI = "ftp.ncbi.nlm.nih.gov";

    @Nested
    class WithSetupAndTeardown {

        private FTPBrowser ftpBrowser;

        @BeforeEach
        void setUp() throws IOException {
            ftpBrowser = new FTPBrowser();
            ftpBrowser.connect(SERVER_NCBI);
        }

        @AfterEach
        void tearDown() throws IOException {
            ftpBrowser.disconnect();
        }

        @Test
        void changeDirectory() throws IOException {
            ftpBrowser.navigateToDirectory("genomes");
        }

        @Test
        void changeDirectoryAndList() throws IOException {
            ftpBrowser.navigateToDirectory("genomes");
            FTPFile[] ftpFiles = ftpBrowser.listFiles();
            assertTrue(ftpFiles.length > 0);
        }

        @Test
        void changeToNestedDirectoryAndFindAssemblyReport() throws IOException {
            ftpBrowser.navigateToDirectory("genomes/all/GCA/000/002/305/GCA_000002305.1_EquCab2.0/");
            FTPFile[] ftpFiles = ftpBrowser.listFiles();
            assertTrue(ftpFiles.length > 0);
            String assemblyReport = "GCA_000002305.1_EquCab2.0_assembly_report.txt";
            boolean found = Stream.of(ftpFiles)
                                  .anyMatch(f -> f.getName().contains(assemblyReport));
            assertTrue(found, "didn't find the assembly report '" + assemblyReport + "' in the folder. Contents are:\n"
                    + Stream.of(ftpFiles).map(FTPFile::toString).collect(Collectors.joining("\n")));
        }

        @Test
        void listDirectories() throws IOException {
            FTPFile[] ftpFiles = ftpBrowser.listDirectories();
            assertTrue(ftpFiles.length > 0);
        }

    }

    @Nested
    class WithoutSetupAndTeardown {

        private final int PORT_NCBI_FTP = 21;

        @Test
        void connectToServerWithExplicitPort() throws IOException {
            FTPBrowser ftpBrowser = new FTPBrowser();
            try {
                ftpBrowser.connect(SERVER_NCBI, PORT_NCBI_FTP);
                FTPFile[] ftpFiles = ftpBrowser.listFiles();
                assertTrue(ftpFiles.length > 0);
            } finally {
                ftpBrowser.disconnect();
            }
        }

        @Test
        void FTPClientTest() throws IOException {
            FTPClient ftp = new FTPClient();
            try {
                ftp.connect(SERVER_NCBI, PORT_NCBI_FTP);
                ftp.enterLocalPassiveMode();
                boolean login = ftp.login("anonymous", "anonymous");
                assertTrue(login);
                FTPFile[] ftpFiles = ftp.listDirectories();
                assertTrue(ftpFiles.length > 0);
            } finally {
                ftp.disconnect();
            }
        }

    }

}
