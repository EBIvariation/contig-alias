package uk.ac.ebi.eva.contigalias.datasource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.eva.contigalias.dus2.NCBIAssemblySequenceReader;
import uk.ac.ebi.eva.contigalias.dus2.NCBIAssemblySequenceReaderFactory;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowser;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowserFactory;
import uk.ac.ebi.eva.contigalias.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.contigalias.utils.GzipCompress;

@Repository("NCBISequenceDataSource")
public class NCBIAssemblySequenceDataSource implements AssemblySequenceDataSource{

    private final Logger logger = LoggerFactory.getLogger(NCBIAssemblySequenceDataSource.class);

    private final NCBIBrowserFactory factory;

    private final NCBIAssemblySequenceReaderFactory readerFactory;

    @Value("${asm.file.download.dir}")
    private String asmFileDownloadDir;

    @Autowired
    public NCBIAssemblySequenceDataSource(NCBIBrowserFactory factory,
                                          NCBIAssemblySequenceReaderFactory readerFactory){
        this.factory = factory;
        this.readerFactory = readerFactory;
    }

    @Override
    public Optional<AssemblySequenceEntity> getAssemblySequenceByAccession(String accession) throws IOException, IllegalArgumentException {
            NCBIBrowser ncbiBrowser = factory.build();
            ncbiBrowser.connect();
            GzipCompress gzipCompress = new GzipCompress();

            Optional<Path> downloadFilePath = downloadAssemblySequence(accession, ncbiBrowser);
            if (!downloadFilePath.isPresent()) {
                return Optional.empty();
            }
            logger.info("Assembly sequence _fna.gz file downloaded successfully in: " + downloadFilePath);
            // Uncompress the .gz file
            Optional<Path> uncompressedFilePath = gzipCompress.unzip(downloadFilePath.get().toString(), asmFileDownloadDir);
            if (!uncompressedFilePath.isPresent()){
                return Optional.empty();
            }

            AssemblySequenceEntity assemblySequenceEntity;
            try (InputStream stream = new FileInputStream(uncompressedFilePath.get().toFile())){
                NCBIAssemblySequenceReader reader = readerFactory.build(stream);
                assemblySequenceEntity = reader.getAssemblySequenceEntity();
                //TODO : The logger info will be canged when we add more attributes to the entity and we parse the whole file info
                logger.info("NCBI: Name of the sequence in " + accession + " : " + assemblySequenceEntity.getName());
            } finally {
                try {
                    ncbiBrowser.disconnect();
                    //Files.deleteIfExists(downloadFilePath.get());
                } catch (IOException e) {
                    logger.warn("Error while trying to disconnect - ncbiBrowser (assembly: " + accession + ")");
                }
        }
            return Optional.of(assemblySequenceEntity);
    }


    /**
     * Download the assembly fna/fasta file given the accession and save it to /tmp
     * After this method is called, the file will be downloaded, and the path to this file
     * on your local computer will be returned*/
    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 2))
    public Optional<Path> downloadAssemblySequence(String accession, NCBIBrowser ncbiBrowser) throws IOException {
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
