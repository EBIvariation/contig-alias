package uk.ac.ebi.eva.contigalias.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowser;
import uk.ac.ebi.eva.contigalias.exception.DownloadFailedException;
import uk.ac.ebi.eva.contigalias.scheduler.Md5ChecksumRetriever;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Autowired
    private Md5ChecksumRetriever md5ChecksumRetriever;

    @MockBean
    private RestTemplate restTemplate;


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


    @Test
    public void retrieveMd5ChecksumRetry() {
        String insdcAccession = "TEST_ACCESSION";
        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("Simulated network issue"));

        Md5ChecksumRetriever anotherObjSpy = Mockito.spy(md5ChecksumRetriever);
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            anotherObjSpy.retrieveMd5Checksum(insdcAccession);
        });

        assertEquals("Simulated network issue", thrown.getMessage());
        verify(restTemplate, times(5)).getForObject(anyString(), eq(JsonNode.class));
    }

}
