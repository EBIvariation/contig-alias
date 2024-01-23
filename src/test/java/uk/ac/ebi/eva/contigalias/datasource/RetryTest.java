package uk.ac.ebi.eva.contigalias.datasource;

import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowser;
import uk.ac.ebi.eva.contigalias.exception.DownloadFailedException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableRetry
@TestPropertySource("classpath:application-test.properties")
@SpringBootTest
public class RetryTest {

    @Mock
    NCBIBrowser ncbiBrowser;

    @Mock
    FTPFile ftpFile;

    @Autowired
    private NCBIAssemblyDataSource dataSource;

    @Test
    public void fileDownloadSuccessfulTest() throws IOException {
        String mockAccession = "GCA_000012345.1";
        String mockDirectory = "/src/test/resources/";
        String mockFileName = "mock_file.txt";
        Long mockFileSize = 1000l;
        when(ncbiBrowser.getGenomeReportDirectory(mockAccession)).thenReturn(Optional.of(mockDirectory));
        when(ncbiBrowser.getNCBIAssemblyReportFile(mockDirectory)).thenReturn(ftpFile);
        when(ftpFile.getName()).thenReturn(mockFileName);
        when(ftpFile.getSize()).thenReturn(mockFileSize);
        when(ncbiBrowser.downloadFTPFile(mockDirectory + mockFileName, Paths.get("/tmp/mock_file.txt"), mockFileSize))
                .thenReturn(true);
        Optional<Path> result = dataSource.downloadAssemblyReport(mockAccession, ncbiBrowser);
        assertTrue(result.isPresent());
    }

    @Test
    public void fileDownloadFailedTest() throws IOException {
        String mockAccession = "GCA_000012345.1";
        String mockDirectory = "/src/test/resources/";
        String mockFileName = "mock_file.txt";
        Long mockFileSize = 1000l;
        when(ncbiBrowser.getGenomeReportDirectory(mockAccession)).thenReturn(Optional.of(mockDirectory));
        when(ncbiBrowser.getNCBIAssemblyReportFile(mockDirectory)).thenReturn(ftpFile);
        when(ftpFile.getName()).thenReturn(mockFileName);
        when(ftpFile.getSize()).thenReturn(mockFileSize);
        when(ncbiBrowser.downloadFTPFile(mockDirectory + mockFileName, Paths.get("/tmp/mock_file.txt"), mockFileSize))
                .thenReturn(false);
        Optional<Path> result = dataSource.downloadAssemblyReport(mockAccession, ncbiBrowser);
        assertFalse(result.isPresent());
    }

    @Test
    public void fileDownloadFailedRetryTest() throws IOException {
        String mockAccession = "GCA_000012345.1";
        String mockDirectory = "/src/test/resources/";
        String mockFileName = "mock_file.txt";
        Long mockFileSize = 1000l;
        when(ncbiBrowser.getGenomeReportDirectory(mockAccession)).thenReturn(Optional.of(mockDirectory));
        when(ncbiBrowser.getNCBIAssemblyReportFile(mockDirectory)).thenReturn(ftpFile);
        when(ftpFile.getName()).thenReturn(mockFileName);
        when(ftpFile.getSize()).thenReturn(mockFileSize);
        when(ncbiBrowser.downloadFTPFile(mockDirectory + mockFileName, Paths.get("/tmp/mock_file.txt"), mockFileSize))
                .thenThrow(new DownloadFailedException("Download Failed"));

        NCBIAssemblyDataSource anotherObjSpy = Mockito.spy(dataSource);
        DownloadFailedException thrown = Assertions.assertThrows(DownloadFailedException.class, () -> {
            anotherObjSpy.downloadAssemblyReport(mockAccession, ncbiBrowser);
        });
        assertEquals("Download Failed", thrown.getMessage());

        verify(ncbiBrowser, times(5))
                .downloadFTPFile(mockDirectory + mockFileName, Paths.get("/tmp/mock_file.txt"), mockFileSize);
    }

    @Test
    public void fileDownloadFailedRetryTest2() throws IOException {
        String mockAccession = "GCA_000012345.1";
        when(ncbiBrowser.getGenomeReportDirectory(mockAccession)).thenThrow(new IOException("Error listing files"));

        NCBIAssemblyDataSource anotherObjSpy = Mockito.spy(dataSource);
        IOException thrown = Assertions.assertThrows(IOException.class, () -> {
            anotherObjSpy.downloadAssemblyReport(mockAccession, ncbiBrowser);
        });

        assertEquals("Error listing files", thrown.getMessage());
        verify(ncbiBrowser, times(5)).getGenomeReportDirectory(mockAccession);
    }

}
