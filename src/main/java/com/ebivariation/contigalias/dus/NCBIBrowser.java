package com.ebivariation.contigalias.dus;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;

public class NCBIBrowser extends FTPBrowser {

    private static final int FTP_PORT = 21;

    public static final String NCBI_FTP_SERVER = "ftp.ncbi.nlm.nih.gov";
    public static final String PATH_GENOMES_ALL = "/genomes/all/";

    public void connect() throws IOException {
        super.connect(NCBI_FTP_SERVER, FTP_PORT);
    }

    public void navigateToSubDirectoryPath(String path) throws IOException {
        super.navigateToDirectory(path);
    }

    public void navigateToAllGenomesDirectory() throws IOException {
        navigateToSubDirectoryPath(PATH_GENOMES_ALL);
    }

    public String getGenomeReportDirectory(String query) throws IOException {

        //GCA_004051055.1
        String rawQuery = query;
        //GCA/004/051/055/
        String path = "";

        path += query.substring(0, 3) + "/";
        query = query.substring(4);

        path += query.substring(0, 3) + "/";
        query = query.substring(3);

        path += query.substring(0, 3) + "/";
        query = query.substring(3);

        path += query.substring(0, 3) + "/";

        String currPath = PATH_GENOMES_ALL + path;
        FTPFile[] ftpFiles = super.listDirectories(currPath);

        if (ftpFiles.length > 0) {
            String dirName = ftpFiles[0].getName();
            if (dirName.contains(rawQuery)) {
                path += dirName + "/";
            }
        } else path = null;

        return path;
    }

}
