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

import uk.ac.ebi.eva.contigalias.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.contigalias.exception.IncorrectAccessionException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Browses and downloads NCBI assembly reports over HTTPS. NCBI mirrors the same file tree served
 * over FTP (ftp.ncbi.nlm.nih.gov) at the same paths over plain HTTPS - see
 * {@link HttpFileBrowser} for why this class talks HTTPS rather than FTP.
 */
public class NCBIBrowser {

    public static final String NCBI_SERVER = "https://ftp.ncbi.nlm.nih.gov";

    public static final String PATH_GENOMES_ALL = "/genomes/all/";

    private final HttpFileBrowser browser;

    public NCBIBrowser() {
        this.browser = new HttpFileBrowser();
    }

    /**
     * Takes a Genbank or Refseq accession and converts it to the equivalent path used by NCBI's server.
     * For example, on input "GCF_007608995.1" the output path is "/genomes/all/GCF/007/608/995/GCF_007608995
     * .1_ASM760899v1/".
     *
     * @param accession Any GCA or GCF String
     * @return Path relative to ftp.ncbi.nlm.nih.gov
     * @throws IOException Passes exception thrown while listing the remote directory
     */
    public Optional<String> getGenomeReportDirectory(String accession) throws IOException, IllegalArgumentException {

        if (accession.length() < 15) {
            throw new IncorrectAccessionException("Accession should be at least 15 characters long!");
        }

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
        List<String> entries = browser.listDirectory(NCBI_SERVER + currPath);

        // We're assuming that the directory will always have a suffix starting with an underscore GCA_004051055.1_
        Optional<String> dir = entries.stream()
                                       .filter(name -> name.startsWith(rawQuery + "_") && name.endsWith("/"))
                                       .findFirst();
        if (dir.isPresent()) {
            // path = "GCA/004/051/055/GCA_004051055.1_ASM405105v1/"
            return Optional.of(currPath + dir.get());
        }

        return Optional.empty();
    }

    /**
     * @param directoryPath The path of the directory in which target report is located relative to root of the
     *                      server. Eg:- "/genomes/all/GCF/007/608/995/GCF_007608995.1_ASM760899v1/"
     * @return An InputStream of the first *assembly_report.txt file it finds.
     * @throws IOException Passes exception thrown while listing or fetching the remote directory
     */
    public InputStream getAssemblyReportInputStream(String directoryPath) throws IOException {
        String reportName = findAssemblyReportName(directoryPath);
        return browser.openStream(NCBI_SERVER + directoryPath + reportName);
    }

    public RemoteFile getNCBIAssemblyReportFile(String directoryPath) throws IOException {
        String reportName = findAssemblyReportName(directoryPath);
        long size = browser.headContentLength(NCBI_SERVER + directoryPath + reportName);
        return new RemoteFile(reportName, size);
    }

    public boolean downloadFile(String filePath, Path downloadFilePath, long expectedSize) throws IOException {
        return browser.downloadFile(NCBI_SERVER + filePath, downloadFilePath, expectedSize);
    }

    private String findAssemblyReportName(String directoryPath) throws IOException {
        List<String> entries = browser.listDirectory(NCBI_SERVER + directoryPath);
        return entries.stream()
                       .filter(name -> name.contains("assembly_report.txt"))
                       .findFirst()
                       .orElseThrow(() -> new AssemblyNotFoundException(
                               "Assembly Report File not present in given directory: " + directoryPath));
    }

}
