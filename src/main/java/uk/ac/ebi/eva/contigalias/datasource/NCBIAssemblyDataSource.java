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

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.dus.NCBIAssemblyReportReader;
import uk.ac.ebi.eva.contigalias.dus.NCBIAssemblyReportReaderFactory;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowser;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowserFactory;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Repository("NCBIDataSource")
public class NCBIAssemblyDataSource implements AssemblyDataSource {

    private final Logger logger = LoggerFactory.getLogger(NCBIAssemblyDataSource.class);

    private final NCBIBrowserFactory factory;

    private final NCBIAssemblyReportReaderFactory readerFactory;

    @Value("${asm.file.download.dir}")
    private String asmFileDownloadDir;

    @Autowired
    public NCBIAssemblyDataSource(NCBIBrowserFactory factory,
                                  NCBIAssemblyReportReaderFactory readerFactory) {
        this.factory = factory;
        this.readerFactory = readerFactory;
    }

    @Override
    public Optional<AssemblyEntity> getAssemblyByAccession(
            String accession) throws IOException, IllegalArgumentException {
        NCBIBrowser ncbiBrowser = factory.build();
        ncbiBrowser.connect();
        Optional<String> directory = ncbiBrowser.getGenomeReportDirectory(accession);
        if (!directory.isPresent()) {
            return Optional.empty();
        }

        FTPFile ftpFile = ncbiBrowser.getNCBIAssemblyReportFile(directory.get());
        String ftpFilePath = directory.get() + ftpFile.getName();
        Path downloadFilePath = Paths.get(asmFileDownloadDir, ftpFile.getName());
        boolean success = ncbiBrowser.downloadFTPFile(ftpFilePath, downloadFilePath, ftpFile.getSize());
        if (!success) {
            return Optional.empty();
        }

        AssemblyEntity assemblyEntity;
        try (InputStream stream = new FileInputStream(downloadFilePath.toFile())) {
            NCBIAssemblyReportReader reader = readerFactory.build(stream);
            assemblyEntity = reader.getAssemblyEntity();
            logger.info("NCBI: Number of chromosomes in " + accession + " : " + assemblyEntity.getChromosomes().size());
        } finally {
            try {
                ncbiBrowser.disconnect();
            } catch (IOException e) {
                logger.warn("Error while trying to disconnect - ncbiBrowser (assembly: " + accession + ") : " + e);
            }
        }
        return Optional.of(assemblyEntity);
    }

}
