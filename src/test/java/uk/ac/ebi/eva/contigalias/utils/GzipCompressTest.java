package uk.ac.ebi.eva.contigalias.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GzipCompressTest {

    @Test
    void unzip() {
        String compressedFilePath = "/tmp/GCF_000001765.3_Dpse_3.0_genomic.fna.gz";
        String outputDirPath = "/tmp";
        GzipCompress gzipCompress = new GzipCompress();


        assertEquals("/tmp/genome_sequence.fna", gzipCompress.unzip(compressedFilePath, outputDirPath).get().toString());
    }
}