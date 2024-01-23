package uk.ac.ebi.eva.contigalias.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Md5ChecksumRetriever {
    private final Logger logger = LoggerFactory.getLogger(Md5ChecksumRetriever.class);
    private String INSDC_ACCESSION_PLACE_HOLDER = "INSDC_ACCESSION_PLACE_HOLDER";
    private String INSDC_CHECKSUM_URL = "https://www.ebi.ac.uk/ena/cram/sequence/insdc:" + INSDC_ACCESSION_PLACE_HOLDER + "/metadata";

    private RestTemplate restTemplate;

    @Autowired
    public Md5ChecksumRetriever(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 2))
    public String retrieveMd5Checksum(String insdcAccession) {
        String apiURL = INSDC_CHECKSUM_URL.replace(INSDC_ACCESSION_PLACE_HOLDER, insdcAccession);
        JsonNode jsonResponse = restTemplate.getForObject(apiURL, JsonNode.class);
        String md5Checksum = jsonResponse.get("metadata").get("md5").asText();
        return md5Checksum;
    }
}
