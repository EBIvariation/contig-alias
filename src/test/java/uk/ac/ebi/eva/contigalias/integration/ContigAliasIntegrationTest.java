package uk.ac.ebi.eva.contigalias.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.repo.AssemblyRepository;
import uk.ac.ebi.eva.contigalias.repo.ChromosomeRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ContigAliasIntegrationTest {
    private static String CHROMOSOME_SEARCH_URL = "/v1/search/chromosome/";
    private static String ASM_1 = "asm_insdc_1";
    private static String ASM_2 = "asm_insdc_2";
    private static String ASM_3 = "asm_insdc_3";

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private AssemblyRepository assemblyRepository;
    @Autowired
    private ChromosomeRepository chromosomeRepository;
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:9.6");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("ftp.proxy.host", () -> "test_ftp_host");
        registry.add("ftp.proxy.port", () -> 20);
    }


    @BeforeEach
    void setup() {
        chromosomeRepository.deleteAll();
        assemblyRepository.deleteAll();

        // setup test data
        AssemblyEntity asm1 = generateAssembly(1);
        AssemblyEntity asm2 = generateAssembly(2);
        AssemblyEntity asm3 = generateAssembly(3);
        // chromosome with same name in same naming convention in both assembly 1 and assembly 2
        ChromosomeEntity chr199 = generateChromosome(99, "insdc", "genbank", "ena",
                "refseq", "ucsc", asm1);
        ChromosomeEntity chr299 = generateChromosome(99, "insdc", "genbank", "ena",
                "refseq", "ucsc", asm2);
        // chromosome with same name but in different naming convention
        ChromosomeEntity chr399 = generateChromosome(99, "ucsc", "insdc", "genbank", "ena",
                "refseq", asm3);

        assemblyRepository.save(asm1);
        assemblyRepository.save(asm2);
        assemblyRepository.save(asm3);
        chromosomeRepository.save(chr199);
        chromosomeRepository.save(chr299);
        chromosomeRepository.save(chr399);
    }

    @AfterEach
    void shutDown() {
        chromosomeRepository.deleteAll();
        assemblyRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testSearchChromosomeByName_Insdc() throws Exception {
        String chromosomeName = "insdc_99";
        String chromosomeSearchURL = CHROMOSOME_SEARCH_URL + chromosomeName;
        String contigNamingConvention = ContigAliasController.AUTHORITY_INSDC;

        // 1. search by name
        MvcResult result = mvc.perform(get(chromosomeSearchURL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode embedded = root.path("_embedded").path("chromosomeEntities");
        List<ChromosomeEntity> chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(3, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getInsdcAccession());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(1).getInsdcAccession());
        assertEquals(ASM_2, chromosomes.get(1).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(2).getGenbankSequenceName());
        assertEquals(ASM_3, chromosomes.get(2).getAssembly().getInsdcAccession());

        // 2. search by name and naming convention
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("namingConvention", contigNamingConvention)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(2, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getInsdcAccession());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(1).getInsdcAccession());
        assertEquals(ASM_2, chromosomes.get(1).getAssembly().getInsdcAccession());


        // 3. search by name and assembly accession
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("assemblyAccession", ASM_1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(1, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getInsdcAccession());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());


        // 4. search by name, naming convention and assembly accession
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("namingConvention", contigNamingConvention)
                        .queryParam("assemblyAccession", ASM_2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(1, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getInsdcAccession());
        assertEquals(ASM_2, chromosomes.get(0).getAssembly().getInsdcAccession());
    }


    @Test
    @Transactional
    public void testSearchChromosomeByName_Genbank() throws Exception {
        String chromosomeName = "genbank_99";
        String chromosomeSearchURL = CHROMOSOME_SEARCH_URL + chromosomeName;
        String contigNamingConvention = ContigAliasController.NAME_GENBANK_TYPE;

        // 1. search by name
        MvcResult result = mvc.perform(get(chromosomeSearchURL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode embedded = root.path("_embedded").path("chromosomeEntities");
        List<ChromosomeEntity> chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(3, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getGenbankSequenceName());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(1).getGenbankSequenceName());
        assertEquals(ASM_2, chromosomes.get(1).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(2).getEnaSequenceName());
        assertEquals(ASM_3, chromosomes.get(2).getAssembly().getInsdcAccession());

        // 2. search by name and naming convention
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("namingConvention", contigNamingConvention)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(2, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getGenbankSequenceName());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(1).getGenbankSequenceName());
        assertEquals(ASM_2, chromosomes.get(1).getAssembly().getInsdcAccession());


        // 3. search by name and assembly accession
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("assemblyAccession", ASM_1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(1, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getGenbankSequenceName());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());


        // 4. search by name, naming convention and assembly accession
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("namingConvention", contigNamingConvention)
                        .queryParam("assemblyAccession", ASM_2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(1, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getGenbankSequenceName());
        assertEquals(ASM_2, chromosomes.get(0).getAssembly().getInsdcAccession());
    }


    @Test
    @Transactional
    public void testSearchChromosomeByName_ENA() throws Exception {
        String chromosomeName = "ena_99";
        String chromosomeSearchURL = CHROMOSOME_SEARCH_URL + chromosomeName;
        String contigNamingConvention = ContigAliasController.NAME_ENA_TYPE;

        // 1. search by name
        MvcResult result = mvc.perform(get(chromosomeSearchURL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode embedded = root.path("_embedded").path("chromosomeEntities");
        List<ChromosomeEntity> chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(3, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getEnaSequenceName());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(1).getEnaSequenceName());
        assertEquals(ASM_2, chromosomes.get(1).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(2).getRefseq());
        assertEquals(ASM_3, chromosomes.get(2).getAssembly().getInsdcAccession());


        // 2. search by name and naming convention
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("namingConvention", contigNamingConvention)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(2, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getEnaSequenceName());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(1).getEnaSequenceName());
        assertEquals(ASM_2, chromosomes.get(1).getAssembly().getInsdcAccession());


        // 3. search by name and assembly accession
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("assemblyAccession", ASM_1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(1, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getEnaSequenceName());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());


        // 4. search by name, naming convention and assembly accession
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("namingConvention", contigNamingConvention)
                        .queryParam("assemblyAccession", ASM_2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(1, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getEnaSequenceName());
        assertEquals(ASM_2, chromosomes.get(0).getAssembly().getInsdcAccession());
    }

    @Test
    @Transactional
    public void testSearchChromosomeByName_RefSeq() throws Exception {
        String chromosomeName = "refseq_99";
        String chromosomeSearchURL = CHROMOSOME_SEARCH_URL + chromosomeName;
        String contigNamingConvention = ContigAliasController.AUTHORITY_REFSEQ;

        // 1. search by name
        MvcResult result = mvc.perform(get(chromosomeSearchURL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode embedded = root.path("_embedded").path("chromosomeEntities");
        List<ChromosomeEntity> chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(3, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getRefseq());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(1).getRefseq());
        assertEquals(ASM_2, chromosomes.get(1).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(2).getUcscName());
        assertEquals(ASM_3, chromosomes.get(2).getAssembly().getInsdcAccession());


        // 2. search by name and naming convention
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("namingConvention", contigNamingConvention)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(2, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getRefseq());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(1).getRefseq());
        assertEquals(ASM_2, chromosomes.get(1).getAssembly().getInsdcAccession());


        // 3. search by name and assembly accession
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("assemblyAccession", ASM_1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(1, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getRefseq());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());


        // 4. search by name, naming convention and assembly accession
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("namingConvention", contigNamingConvention)
                        .queryParam("assemblyAccession", ASM_2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(1, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getRefseq());
        assertEquals(ASM_2, chromosomes.get(0).getAssembly().getInsdcAccession());
    }


    @Test
    @Transactional
    public void testSearchChromosomeByName_UCSC() throws Exception {
        String chromosomeName = "ucsc_99";
        String chromosomeSearchURL = CHROMOSOME_SEARCH_URL + chromosomeName;
        String contigNamingConvention = ContigAliasController.NAME_UCSC_TYPE;

        // 1. search by name
        MvcResult result = mvc.perform(get(chromosomeSearchURL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode embedded = root.path("_embedded").path("chromosomeEntities");
        List<ChromosomeEntity> chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(3, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getUcscName());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(1).getUcscName());
        assertEquals(ASM_2, chromosomes.get(1).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(2).getInsdcAccession());
        assertEquals(ASM_3, chromosomes.get(2).getAssembly().getInsdcAccession());


        // 2. search by name and assembly accession
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("assemblyAccession", ASM_1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(1, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getUcscName());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());


        // 3. search by name and naming convention
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("namingConvention", contigNamingConvention)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(2, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getUcscName());
        assertEquals(ASM_1, chromosomes.get(0).getAssembly().getInsdcAccession());
        assertEquals(chromosomeName, chromosomes.get(1).getUcscName());
        assertEquals(ASM_2, chromosomes.get(1).getAssembly().getInsdcAccession());


        // 4. search by name, naming convention and assembly accession
        result = mvc.perform(get(chromosomeSearchURL)
                        .queryParam("namingConvention", contigNamingConvention)
                        .queryParam("assemblyAccession", ASM_2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        root = objectMapper.readTree(jsonResponse);
        embedded = root.path("_embedded").path("chromosomeEntities");
        chromosomes = objectMapper.readValue(
                embedded.toString(), new TypeReference<List<ChromosomeEntity>>() {
                }
        );
        assertEquals(1, chromosomes.size());
        assertEquals(chromosomeName, chromosomes.get(0).getUcscName());
        assertEquals(ASM_2, chromosomes.get(0).getAssembly().getInsdcAccession());
    }


    public static AssemblyEntity generateAssembly(long id) {
        return new AssemblyEntity()
                .setName("asm_" + id)
                .setOrganism("asm_organism_" + id)
                .setInsdcAccession("asm_insdc_" + id)
                .setRefseq("asm_refseq_" + id)
                .setTaxid(id)
                .setGenbankRefseqIdentical(new Random().nextBoolean())
                .setMd5checksum("asm_md5_" + id)
                .setTrunc512checksum("asm_trunc512_" + id)
                .setChromosomes(new LinkedList<>());
    }

    public static ChromosomeEntity generateChromosome(long id, String insdc, String genbank, String ena, String refseq, String ucsc, AssemblyEntity assembly) {
        ChromosomeEntity entity = (ChromosomeEntity) new ChromosomeEntity()
                .setInsdcAccession(insdc + "_" + id)
                .setGenbankSequenceName(genbank + "_" + id)
                .setEnaSequenceName(ena + "_" + id)
                .setRefseq(refseq + "_" + id)
                .setUcscName(ucsc + "_" + id)
                .setMd5checksum(null)
                .setTrunc512checksum("trunc512_" + id)
                .setAssembly(null);

        if (assembly.getChromosomes() == null) {
            assembly.setChromosomes(new LinkedList<>());
        }
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        chromosomes.add(entity);
        entity.setAssembly(assembly);
        return entity;
    }

}

