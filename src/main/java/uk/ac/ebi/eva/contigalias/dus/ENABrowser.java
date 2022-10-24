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

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ENABrowser extends PassiveAnonymousFTPClient {

    public static final String EBI_FTP_SERVER = "ftp.ebi.ac.uk";

    public static final String PATH_ENA_ASSEMBLY = "/pub/databases/ena/assembly/";

    private String ftpProxyHost;

    private Integer ftpProxyPort;

    public ENABrowser(String ftpProxyHost, Integer ftpProxyPort) {
        this.ftpProxyHost = ftpProxyHost;
        this.ftpProxyPort = ftpProxyPort;
    }

    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier=2))
    public void connect() throws IOException {
        if (ftpProxyHost != null && !ftpProxyHost.equals("null") &&
                ftpProxyPort != null && ftpProxyPort != 0) {
            super.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ftpProxyHost, ftpProxyPort)));
        }
        super.connect(EBI_FTP_SERVER);
    }

    /**
     * Takes INSDC accession and gets the corresponding assembly report.
     * For example, on input "GCA_003005035.1" it will return a stream to the file at
     * ftp.ebi.ac.uk/pub/databases/ena/assembly/GCA_003/GCA_003005/GCA_003005035.1_sequence_report.txt
     *
     * @param accession Any GCA accession
     * @return Input stream of the corresponding sequence_report.txt file.
     * @throws IOException Passes exception thrown by FTPBrowser.retrieveFileStream()
     */
    public InputStream getAssemblyReportInputStream(String accession) throws IOException, IllegalArgumentException {

        if (accession.length() < 15) {
            throw new IllegalArgumentException("Accession should be at least 15 characters long!");
        }

        String directory = accession.substring(0, 7) + "/" + accession.substring(0, 10) + "/";
        String filename = accession + "_sequence_report.txt";
        String fullPath = PATH_ENA_ASSEMBLY + directory + filename;

        return super.retrieveFileStream(fullPath);

    }

    public String getAssemblyDirPath(String accession) {
        if (accession.length() < 15) {
            throw new IllegalArgumentException("Accession should be at least 15 characters long!");
        }
        String directory = accession.substring(0, 7) + "/" + accession.substring(0, 10) + "/";
        return PATH_ENA_ASSEMBLY + directory;
    }

    public FTPFile getAssemblyReportFile(String dirPath, String accession) throws IOException {
        Stream<FTPFile> ftpFileStream = Arrays.stream(super.listFiles(dirPath));
        Stream<FTPFile> assemblyReportFilteredStream = ftpFileStream
                .filter(f -> f.getName().equals(accession + "_sequence_report.txt"));
        Optional<FTPFile> assemblyReport = assemblyReportFilteredStream.findFirst();

        return assemblyReport.orElseThrow(() -> new IllegalArgumentException("Assembly Report File not present in given directory: " + dirPath));
    }

}
