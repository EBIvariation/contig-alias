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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
        String mockDirectory = "/src/test/resources/";
        String mockFileName = "mock_file.txt";
        Long mockFileSize = 1000l;
        when(ncbiBrowser.getNCBIAssemblyReportFile(mockDirectory)).thenReturn(ftpFile);
        when(ftpFile.getName()).thenReturn(mockFileName);
        when(ftpFile.getSize()).thenReturn(mockFileSize);
        when(ncbiBrowser.downloadFTPFile(mockDirectory + mockFileName, Paths.get("/tmp/mock_file.txt"), mockFileSize))
                .thenReturn(true);
        Optional<Path> result = dataSource.downloadAssemblyReport(ncbiBrowser, mockDirectory);
        assertTrue(result.isPresent());
    }

    @Test
    public void fileDownloadFailedTest() throws IOException {
        String mockDirectory = "/src/test/resources/";
        String mockFileName = "mock_file.txt";
        Long mockFileSize = 1000l;
        when(ncbiBrowser.getNCBIAssemblyReportFile(mockDirectory)).thenReturn(ftpFile);
        when(ftpFile.getName()).thenReturn(mockFileName);
        when(ftpFile.getSize()).thenReturn(mockFileSize);
        when(ncbiBrowser.downloadFTPFile(mockDirectory + mockFileName, Paths.get("/tmp/mock_file.txt"), mockFileSize))
                .thenReturn(false);
        Optional<Path> result = dataSource.downloadAssemblyReport(ncbiBrowser, mockDirectory);
        assertFalse(result.isPresent());
    }

    @Test
    public void fileDownloadFailedRetryTest() throws IOException {
        String mockDirectory = "/src/test/resources/";
        String mockFileName = "mock_file.txt";
        Long mockFileSize = 1000l;
        when(ncbiBrowser.getNCBIAssemblyReportFile(mockDirectory)).thenReturn(ftpFile);
        when(ftpFile.getName()).thenReturn(mockFileName);
        when(ftpFile.getSize()).thenReturn(mockFileSize);
        when(ncbiBrowser.downloadFTPFile(mockDirectory + mockFileName, Paths.get("/tmp/mock_file.txt"), mockFileSize))
                .thenThrow(new DownloadFailedException("Download Failed"));

        NCBIAssemblyDataSource anotherObjSpy = Mockito.spy(dataSource);
        DownloadFailedException thrown = Assertions.assertThrows(DownloadFailedException.class, () -> {
            anotherObjSpy.downloadAssemblyReport(ncbiBrowser, mockDirectory);
        });
        assertEquals("Download Failed", thrown.getMessage());

        verify(ncbiBrowser, times(5))
                .downloadFTPFile(mockDirectory + mockFileName, Paths.get("/tmp/mock_file.txt"), mockFileSize);
    }

}
