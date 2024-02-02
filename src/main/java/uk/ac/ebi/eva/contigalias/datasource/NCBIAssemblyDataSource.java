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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.eva.contigalias.dus.NCBIAssemblyReportReader;
import uk.ac.ebi.eva.contigalias.dus.NCBIAssemblyReportReaderFactory;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowser;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowserFactory;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        Optional<Path> downloadFilePath = downloadAssemblyReport(accession, ncbiBrowser);
        if (!downloadFilePath.isPresent()) {
            return Optional.empty();
        }

        AssemblyEntity assemblyEntity;
        try (InputStream stream = new FileInputStream(downloadFilePath.get().toFile())) {
            NCBIAssemblyReportReader reader = readerFactory.build(stream);
            assemblyEntity = reader.getAssemblyEntity();
            logger.info("NCBI: Number of chromosomes in " + accession + " : " +
                    (assemblyEntity.getChromosomes() != null ? assemblyEntity.getChromosomes().size() : 0));
        } finally {
            try {
                ncbiBrowser.disconnect();
                Files.deleteIfExists(downloadFilePath.get());
            } catch (IOException e) {
                logger.warn("Error while trying to disconnect - ncbiBrowser (assembly: " + accession + ") : " + e);
            }
        }
        return Optional.of(assemblyEntity);
    }

    public AssemblyEntity getAssemblyEntity(Path downloadFilePath) throws IOException {
        List<String> asmDataLines = Files.lines(downloadFilePath)
                .filter(line -> line.startsWith("#"))
                .collect(Collectors.toList());
        return getAssemblyEntity(asmDataLines);
    }

    public AssemblyEntity getAssemblyEntity(List<String> asmDataLines) {
        return NCBIAssemblyReportReader.getAssemblyEntity(asmDataLines);
    }

    public List<ChromosomeEntity> getChromosomeEntityList(AssemblyEntity assemblyEntity, List<String> chrDataList) {
        List<ChromosomeEntity> chromosomeEntityList = new ArrayList<>();
        for (String chrData : chrDataList) {
            ChromosomeEntity chromosomeEntity = getChromosomeEntity(assemblyEntity, chrData);
            if (chromosomeEntity != null) {
                chromosomeEntityList.add(chromosomeEntity);
            }
        }
        return chromosomeEntityList;
    }

    public ChromosomeEntity getChromosomeEntity(AssemblyEntity assemblyEntity, String chrLine) {
        ChromosomeEntity chromosomeEntity = NCBIAssemblyReportReader.getChromosomeEntity(chrLine);
        if (chromosomeEntity != null) {
            chromosomeEntity.setAssembly(assemblyEntity);
        }
        return chromosomeEntity;
    }

    public Optional<Path> downloadAssemblyReport(String accession) throws IOException {
        NCBIBrowser ncbiBrowser = factory.build();
        Optional<Path> downloadPath;
        try {
            ncbiBrowser.connect();
            downloadPath = downloadAssemblyReport(accession, ncbiBrowser);
        } finally {
            try {
                ncbiBrowser.disconnect();
            } catch (IOException e) {
                logger.warn("Error while trying to disconnect - ncbiBrowser (assembly: " + accession + ") : " + e);
            }
        }

        return downloadPath;
    }

    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 2))
    public Optional<Path> downloadAssemblyReport(String accession, NCBIBrowser ncbiBrowser) throws IOException {
        Optional<String> directory = ncbiBrowser.getGenomeReportDirectory(accession);
        if (!directory.isPresent()) {
            return Optional.empty();
        }
        logger.info("NCBI directory for assembly report download: " + directory.get());

        FTPFile ftpFile = ncbiBrowser.getNCBIAssemblyReportFile(directory.get());
        String ftpFilePath = directory.get() + ftpFile.getName();
        Path downloadFilePath = Paths.get(asmFileDownloadDir, ftpFile.getName());
        boolean success = ncbiBrowser.downloadFTPFile(ftpFilePath, downloadFilePath, ftpFile.getSize());
        if (success) {
            logger.info("NCBI assembly report downloaded successfully (" + ftpFile.getName() + ")");
            return Optional.of(downloadFilePath);
        } else {
            logger.error("NCBI assembly report could not be downloaded successfully(" + ftpFile.getName() + ")");
            return Optional.empty();
        }
    }

}
