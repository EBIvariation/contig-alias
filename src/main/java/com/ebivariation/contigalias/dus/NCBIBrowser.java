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

package com.ebivariation.contigalias.dus;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class NCBIBrowser extends FTPBrowser {

    public static final String NCBI_FTP_SERVER = "ftp.ncbi.nlm.nih.gov";

    public static final String PATH_GENOMES_ALL = "/genomes/all/";

    public void connect() throws IOException {
        super.connect(NCBI_FTP_SERVER);
    }

    public void navigateToSubDirectoryPath(String path) throws IOException {
        super.navigateToDirectory(path);
    }

    public void navigateToAllGenomesDirectory() throws IOException {
        navigateToSubDirectoryPath(PATH_GENOMES_ALL);
    }

    /**
     * Takes a Genbank or Refseq accession and converts it to the equivalent path used by NCBI's FTP server.
     * For example, on input "GCF_007608995.1" the output path is "GCF/007/608/995/GCF_007608995.1_ASM760899v1/".
     *
     * @param accession Any GCA or GCF String
     * @return Path relative to ftp.ncbi.nlm.nih.gov/genomes/all/
     * @throws IOException Passes exception thrown by FTPBrowser.listDirectories()
     */
    public String getGenomeReportDirectory(String accession) throws IOException {

        //GCA_004051055.1
        String rawQuery = accession;
        String path = "";

        // path = "GCA/"
        path += accession.substring(0, 3) + "/";
        // accession = "004051055.1"
        accession = accession.substring(4);

        // path = "GCA/004/"
        path += accession.substring(0, 3) + "/";
        // accession = "051055.1"
        accession = accession.substring(3);

        // path = "GCA/004/051/"
        path += accession.substring(0, 3) + "/";
        // accession = "055.1"
        accession = accession.substring(3);

        // path = "GCA/004/051/055/"
        path += accession.substring(0, 3) + "/";

        String currPath = PATH_GENOMES_ALL + path;
        FTPFile[] ftpFiles = super.listDirectories(currPath);

        if (ftpFiles.length > 0) {
            String dirName = ftpFiles[0].getName();
            if (dirName.contains(rawQuery)) {
                // path = "GCA/004/051/055/GCA_004051055.1_ASM405105v1/"
                path += dirName + "/";
            }
        } else path = null;

        return path;
    }

    /**
     * @param directoryPath The path of the directory in which target report is located related to current directory.
     * @return An InputStream of the first *assembly_report.txt file it finds.
     * @throws IOException Passes exception thrown by FTPBrowser.retrieveFileStream()
     */
    public InputStream getAssemblyReportInputStream(String directoryPath) throws IOException {

        InputStream fileStream;

        Stream<FTPFile> ftpFileStream = Arrays.stream(super.listFiles(directoryPath));
        Stream<FTPFile> assemblyReportFilteredStream = ftpFileStream.filter(f -> f.getName().contains("assembly_report.txt"));
        Optional<FTPFile> assemblyReport = assemblyReportFilteredStream.findFirst();

        if (assemblyReport.isPresent()) {
            directoryPath += assemblyReport.get().getName();
            fileStream = super.retrieveFileStream(directoryPath);
        } else {
            throw new IllegalArgumentException("Assembly Report File not present in given directory: " + directoryPath);
        }
        return fileStream;
    }

}
