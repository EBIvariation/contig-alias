package com.ebivariation.contigalias;

import com.ebivariation.contigalias.dus.FTPBrowser;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FTPBrowserTest {

    @Test
    void connectToServer() throws IOException {
        FTPBrowser ftpBrowser = new FTPBrowser();
        try {
            ftpBrowser.connect();
            FTPFile[] ftpFiles = ftpBrowser.listDirectories();
            assertTrue(ftpFiles.length > 0);
        } finally {
            ftpBrowser.disconnect();
        }
    }

    @Test
    void FTPClientTest() throws IOException {
        FTPClient ftp = new FTPClient();
        try {
            String server = "ftp.ncbi.nlm.nih.gov";
            ftp.connect(server, 21);
            ftp.enterLocalPassiveMode();
            boolean login = ftp.login("anonymous", "anonymous");
            assertTrue(login);
            FTPFile[] ftpFiles = ftp.listDirectories();
            assertTrue(ftpFiles.length > 0);
        } finally {
            ftp.disconnect();
        }
    }

    @Test
    void changeDirectory() throws IOException {
        FTPBrowser ftpBrowser = new FTPBrowser();
        try {
            ftpBrowser.connect();
            ftpBrowser.changeToDirectory("genomes");
        } finally {
            ftpBrowser.disconnect();
        }
    }

    @Test
    void changeDirectoryAndList() throws IOException {
        FTPBrowser ftpBrowser = new FTPBrowser();
        try {
            ftpBrowser.connect();
            ftpBrowser.changeToDirectory("genomes");
            FTPFile[] ftpFiles = ftpBrowser.listDirectories();
            assertTrue(ftpFiles.length > 0);
        } finally {
            ftpBrowser.disconnect();
        }
    }
    
    @Test
    void changeToNestedDirectoryAndFindAssemblyReport() throws IOException {
        FTPBrowser ftpBrowser = new FTPBrowser();
        try {
            ftpBrowser.connect();
            ftpBrowser.changeToDirectory("genomes/all/GCA/000/002/305/GCA_000002305.1_EquCab2.0/");
            FTPFile[] ftpFiles = ftpBrowser.listDirectories();
            assertTrue(ftpFiles.length > 0);
            String assemblyReport = "GCA_000002305.1_EquCab2.0_assembly_report.txt";
            boolean found = Stream.of(ftpFiles)
                                  .anyMatch(f -> f.getName().contains(assemblyReport));
            assertTrue(found, "didn't find the assembly report '" + assemblyReport + "' in the folder. Contents are:\n"
                    + Stream.of(ftpFiles).map(FTPFile::toString).collect(Collectors.joining("\n")));
        } finally {
            ftpBrowser.disconnect();
        }
    }
}
