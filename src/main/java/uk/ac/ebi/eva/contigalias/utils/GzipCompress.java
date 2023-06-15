package uk.ac.ebi.eva.contigalias.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GzipCompress {

    private final Logger logger = LoggerFactory.getLogger(GzipCompress.class);

    /**
     * Decompress (Unzip) a .gz file and save the output file in the same
     * input file's location.
     * The output file's name will be genome_sequence.fna
     * @return The output (decompressed) file path*/
    public Optional<Path> unzip(String compressedFilePath, String outputDirPath) {
        String outputFileName = "genome_sequence.fna";
        String decompressedFilePath = outputDirPath + "/" + outputFileName;

        byte[] buffer = new byte[1024];

        try {
            FileInputStream fileIn = new FileInputStream(compressedFilePath);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileIn);
            FileOutputStream fileOutputStream = new FileOutputStream(decompressedFilePath);

            int bytes_read;

            while ((bytes_read = gzipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes_read);
            }
            gzipInputStream.close();
            fileOutputStream.close();
            logger.info("File " + compressedFilePath + " was decompressed successfully");
            Path outputFilePath = Paths.get(outputDirPath, outputFileName);
            return Optional.of(outputFilePath);
        } catch (
                IOException e) {
            logger.error("Could not find or read file !!");
            return Optional.empty();
        }

    }
}
