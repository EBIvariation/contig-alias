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

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.eva.contigalias.dus.ENAAssemblyReportReader;
import uk.ac.ebi.eva.contigalias.dus.ENAAssemblyReportReaderFactory;
import uk.ac.ebi.eva.contigalias.dus.ENABrowser;
import uk.ac.ebi.eva.contigalias.dus.ENABrowserFactory;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;
import uk.ac.ebi.eva.contigalias.exception.DownloadFailedException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("ENADataSource")
public class ENAAssemblyDataSource implements AssemblyDataSource {

    private final Logger logger = LoggerFactory.getLogger(ENAAssemblyDataSource.class);

    private final ENABrowserFactory factory;

    private final ENAAssemblyReportReaderFactory readerFactory;

    @Value("${asm.file.download.dir}")
    private String asmFileDownloadDir;

    @Autowired
    public ENAAssemblyDataSource(ENABrowserFactory factory,
                                 ENAAssemblyReportReaderFactory readerFactory) {
        this.factory = factory;
        this.readerFactory = readerFactory;
    }

    @Override
    public Optional<AssemblyEntity> getAssemblyByAccession(String accession) throws IOException {
        ENABrowser enaBrowser = factory.build();
        enaBrowser.connect();
        try {
            Optional<Path> downloadFilePath = downloadAssemblyReport(enaBrowser, accession);
            if (!downloadFilePath.isPresent()) {
                return Optional.empty();
            }

            AssemblyEntity assemblyEntity;
            try (InputStream stream = new FileInputStream(downloadFilePath.get().toFile())) {
                ENAAssemblyReportReader reader = readerFactory.build(stream);
                assemblyEntity = reader.getAssemblyEntity();
                logger.info("ENA: Number of chromosomes in " + accession + " : " + assemblyEntity.getChromosomes().size());
            } finally {
                try {
                    enaBrowser.disconnect();
                    Files.deleteIfExists(downloadFilePath.get());
                } catch (IOException e) {
                    logger.warn("Error while trying to disconnect - enaBrowser (assembly: " + accession + ") : " + e);
                }
            }
            return Optional.of(assemblyEntity);
        } catch (Exception e) {
            logger.warn("Could not fetch Assembly Report from ENA for accession " + accession + "Exception: " + e);
            return Optional.empty();
        }

    }

    public Optional<Path> downloadAssemblyReport(String accession) throws IOException {
        ENABrowser enaBrowser = factory.build();
        enaBrowser.connect();
        try {
            enaBrowser.connect();
            return downloadAssemblyReport(enaBrowser, accession);
        } catch (Exception e){
            logger.warn("Could not fetch Assembly Report from ENA for accession " + accession + "Exception: " + e);
            return Optional.empty();
        }finally {
            try {
                enaBrowser.disconnect();
            } catch (IOException e) {
                logger.warn("Error while trying to disconnect - ncbiBrowser (assembly: " + accession + ") : " + e);
            }
        }
    }

    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 2))
    public Optional<Path> downloadAssemblyReport(ENABrowser enaBrowser, String accession) throws IOException {
        String dirPath = enaBrowser.getAssemblyDirPath(accession);
        FTPFile ftpFile = enaBrowser.getAssemblyReportFile(dirPath, accession);
        String ftpFilePath = dirPath + ftpFile.getName();
        Path downloadFilePath = Paths.get(asmFileDownloadDir, ftpFile.getName());
        try {
            boolean success = enaBrowser.downloadFTPFile(ftpFilePath, downloadFilePath, ftpFile.getSize());
            if (success) {
                logger.info("ENA assembly report downloaded successfully for accession " + accession);
                return Optional.of(downloadFilePath);
            } else {
                logger.warn("ENA assembly report could not be downloaded successfully for accession " + accession);
                return Optional.empty();
            }
        } catch (IOException | DownloadFailedException e) {
            logger.warn("Error downloading ENA assembly report for accession " + accession + e);
            return Optional.empty();
        }
    }

    public List<ChromosomeEntity> getChromosomeEntityList(List<String> chrDataList) {
        List<ChromosomeEntity> chromosomeEntityList = new ArrayList<>();
        for (String chrData : chrDataList) {
            ChromosomeEntity chromosomeEntity = getChromosomeEntity(chrData);
            if (chromosomeEntity != null) {
                chromosomeEntityList.add(chromosomeEntity);
            }
        }
        return chromosomeEntityList;
    }

    public ChromosomeEntity getChromosomeEntity(String chrLine) {
        return ENAAssemblyReportReader.getChromosomeEntity(chrLine);
    }

    /**
     * Adds ENA sequence names to chromosomes and scaffolds in an assembly. Will modify the AssemblyEntity in-place.
     *
     * @param optional {@link AssemblyEntity} to add ENA sequence names to
     * @throws IOException Passes IOException thrown by {@link #getAssemblyByAccession(String)}
     */
    public void addENASequenceNamesToAssembly(AssemblyEntity targetAssembly) throws IOException {
        if (!hasAllEnaSequenceNames(targetAssembly)) {
            String insdcAccession = targetAssembly.getInsdcAccession();
            Optional<AssemblyEntity> enaAssembly = getAssemblyByAccession(insdcAccession);

            if (enaAssembly.isPresent()) {
                AssemblyEntity sourceAssembly = enaAssembly.get();
                addENASequenceNames(Objects.nonNull(sourceAssembly.getChromosomes()) ?
                                sourceAssembly.getChromosomes() : Collections.emptyList(),
                        Objects.nonNull(targetAssembly.getChromosomes()) ?
                                targetAssembly.getChromosomes() : Collections.emptyList());
            }
        }
    }

    public boolean hasAllEnaSequenceNames(AssemblyEntity assembly) {
        List<ChromosomeEntity> chromosomes = Objects.nonNull(assembly.getChromosomes()) ?
                assembly.getChromosomes() : Collections.emptyList();
        return chromosomes.stream().allMatch(sequence -> sequence.getEnaSequenceName() != null);
    }

    public void addENASequenceNames(
            List<? extends SequenceEntity> sourceSequences, List<? extends SequenceEntity> targetSequences) {
        if (targetSequences == null || sourceSequences == null || targetSequences.isEmpty() || sourceSequences.isEmpty()) {
            return;
        }
        Map<String, SequenceEntity> insdcToSequenceEntityMap = targetSequences.stream()
                .collect(Collectors.toMap(s->s.getInsdcAccession(), s->s));

        for (SequenceEntity sourceSeq : sourceSequences) {
            String sourceInsdcAccession = sourceSeq.getInsdcAccession();
            if (insdcToSequenceEntityMap.containsKey(sourceInsdcAccession)) {
                insdcToSequenceEntityMap.get(sourceInsdcAccession).setEnaSequenceName(sourceSeq.getEnaSequenceName());
            }
        }
    }

    public void addENASequenceNameToChromosomes(List<ChromosomeEntity> ncbiChromosomeList,
                                                Path downloadedENAFilePath, final int BATCH_SIZE) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(downloadedENAFilePath.toFile()))) {
            List<String> chrLines = new ArrayList<>();
            List<ChromosomeEntity> enaChromosomeList;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("accession")) {
                    continue;
                }
                chrLines.add(line);
                if (chrLines.size() == BATCH_SIZE) {
                    enaChromosomeList = getChromosomeEntityList(chrLines);
                    addENASequenceNames(enaChromosomeList, ncbiChromosomeList);

                    chrLines = new ArrayList<>();
                }
            }
            if (!chrLines.isEmpty()) {
                enaChromosomeList = getChromosomeEntityList(chrLines);
                addENASequenceNames(enaChromosomeList, ncbiChromosomeList);
            }
        }
    }
}
