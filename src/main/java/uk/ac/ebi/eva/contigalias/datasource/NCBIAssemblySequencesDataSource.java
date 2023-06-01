package uk.ac.ebi.eva.contigalias.datasource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.eva.contigalias.dus2.NCBIAssemblySequencesReader;
import uk.ac.ebi.eva.contigalias.dus2.NCBIAssemblySequencesReaderFactory;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowser;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowserFactory;
import uk.ac.ebi.eva.contigalias.entities.AssemblySequencesEntity;
import uk.ac.ebi.eva.contigalias.utils.GzipCompress;

@Repository("NCBISequenceDataSource")
public class NCBIAssemblySequencesDataSource implements AssemblySequencesDataSource {

    private final Logger logger = LoggerFactory.getLogger(NCBIAssemblySequencesDataSource.class);

    private final NCBIBrowserFactory factory;

    private final NCBIAssemblySequencesReaderFactory readerFactory;

    @Value("${asm.file.download.dir}")
    private String asmFileDownloadDir;

    @Autowired
    public NCBIAssemblySequencesDataSource(NCBIBrowserFactory factory,
                                           NCBIAssemblySequencesReaderFactory readerFactory){
        this.factory = factory;
        this.readerFactory = readerFactory;
    }

    @Override
    public Optional<AssemblySequencesEntity> getAssemblySequencesByAccession(String accession) throws IOException, IllegalArgumentException, NoSuchAlgorithmException {
            NCBIBrowser ncbiBrowser = factory.build();
            ncbiBrowser.connect();
            GzipCompress gzipCompress = new GzipCompress();

            Optional<Path> downloadFilePath = downloadAssemblySequences(accession, ncbiBrowser);
            if (!downloadFilePath.isPresent()) {
                return Optional.empty();
            }
            logger.info("Assembly sequence _fna.gz file downloaded successfully in: " + downloadFilePath);
            // Uncompress the .gz file
            Optional<Path> compressedFilePath = gzipCompress.unzip(downloadFilePath.get().toString(), asmFileDownloadDir);
            if (!compressedFilePath.isPresent()){
                return Optional.empty();
            }

            AssemblySequencesEntity assemblySequencesEntity;
            try (InputStream stream = new FileInputStream(compressedFilePath.get().toFile())){
                NCBIAssemblySequencesReader reader = readerFactory.build(stream, accession);
                assemblySequencesEntity = reader.getAssemblySequenceEntity();
                logger.info("NCBI: Assembly sequences' fasta file with accession " + accession + " has been parsed successfully" );
            } finally {
                try {
                    ncbiBrowser.disconnect();
                    Files.deleteIfExists(downloadFilePath.get());
                    Files.deleteIfExists(compressedFilePath.get()); // Deleting the fasta file
                } catch (IOException e) {
                    logger.warn("Error while trying to disconnect - ncbiBrowser (assembly: " + accession + ")");
                }
        }
            return Optional.of(assemblySequencesEntity);
    }


    /**
     * Download the assembly fna/fasta file given the accession and save it to /tmp
     * After this method is called, the file will be downloaded, and the path to this file
     * on your local computer will be returned*/
    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 2))
    public Optional<Path> downloadAssemblySequences(String accession, NCBIBrowser ncbiBrowser) throws IOException {
        // The same directory as the report file
        Optional<String> directory = ncbiBrowser.getGenomeReportDirectory(accession);

        if (!directory.isPresent()) {
            return Optional.empty();
        }

        logger.info("NCBI directory for assembly genomic.fna download: " + directory.get());
        FTPFile ftpFile = ncbiBrowser.getAssemblyGenomicFnaFile(directory.get());
        String ftpFilePath = directory.get() + ftpFile.getName();
        Path downloadFilePath = Paths.get(asmFileDownloadDir, ftpFile.getName());
        boolean success = ncbiBrowser.downloadFTPFile(ftpFilePath, downloadFilePath, ftpFile.getSize());
        if (success) {
            logger.info("NCBI assembly genomic.fna downloaded successfully (" + ftpFile.getName() + ")");
            return Optional.of(downloadFilePath);
        } else {
            logger.error("NCBI assembly genomic.fna could not be downloaded successfully(" + ftpFile.getName() + ")");
            return Optional.empty();
        }
    }
}
