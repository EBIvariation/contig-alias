package com.ebivariation.contigalias.dus;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;

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

}
