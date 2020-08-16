/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.contigalias.dus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

@Component
public class AssemblyReportReader {

    private BufferedReader reader;

    @Value("${config.scaffolds.enabled:false}")
    private boolean SCAFFOLDS_ENABLED;

    private AssemblyEntity assemblyEntity;

    private boolean reportParsed = false;

    public void setInputStream(InputStream inputStream) {
        setInputStreamReader(new InputStreamReader(inputStream));
    }

    public void setInputStreamReader(InputStreamReader inputStreamReader) {
        reader = new BufferedReader(inputStreamReader);
    }

    /**
     * Returns the class-level instance variable of {@link AssemblyEntity}. If the variable has not been initialized
     * or the assembly report has not been parsed yet, it calls {@link #parseReport()} and then returns the variable
     * that would have been initialized by {@link #parseReport()}.
     *
     * @return {@link AssemblyEntity} containing all metadata extracted from the report along with a list of
     * {@link ChromosomeEntity} which also contain their own metadata.
     * @throws IOException Passes IOException thrown by {@link #parseReport()}
     */
    public AssemblyEntity getAssemblyEntity() throws IOException {
        if (!reportParsed || assemblyEntity == null) {
            parseReport();
        }
        return assemblyEntity;
    }

    /**
     * Reads the report line-by-line and calls the relevant methods to parse each line based on its starting characters.
     *
     * @throws IOException Passes IOException thrown by {@link BufferedReader#readLine()}
     */
    private void parseReport() throws IOException, NullPointerException {
        if (reader == null) {
            throw new NullPointerException("Cannot use AssemblyReportReader without having a valid InputStreamReader.");
        }
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("# ")) {
                if (assemblyEntity == null) {
                    assemblyEntity = new AssemblyEntity();
                }
                parseAssemblyData(line);
            } else if (!line.startsWith("#")) {
                String[] columns = line.split("\t", -1);
                if (columns.length >= 6) {
                    if (columns[3].equals("Chromosome")) {
                        parseChromosomeLine(columns);
                    } else if (SCAFFOLDS_ENABLED && columns[1].equals("unplaced-scaffold")) {
                        parseScaffoldLine(columns);
                    }
                }
            }
            line = reader.readLine();
        }
        reportParsed = true;
        reader.close();
    }

    /**
     * Parses lines in assembly report containing Assembly metadata. Breaks line into a tag:tagData format and
     * sets tagData in {@link AssemblyEntity} fields corresponding to the tag found in given line.
     *
     * @param line A line of assembly report file starting with "# ".
     */
    private void parseAssemblyData(String line) {
        int tagEnd = line.indexOf(':');
        if (tagEnd == -1) {
            return;
        }
        String tag = line.substring(2, tagEnd);
        String tagData = line.substring(tagEnd + 1).trim();
        switch (tag) {
            case "Assembly name": {
                assemblyEntity.setName(tagData);
                break;
            }
            case "Organism name": {
                assemblyEntity.setOrganism(tagData);
                break;
            }
            case "Taxid": {
                assemblyEntity.setTaxid(Long.parseLong(tagData));
                break;
            }
            case "GenBank assembly accession": {
                assemblyEntity.setGenbank(tagData);
                break;
            }
            case "RefSeq assembly accession": {
                assemblyEntity.setRefseq(tagData);
                break;
            }
            case "RefSeq assembly and GenBank assemblies identical": {
                assemblyEntity.setGenbankRefseqIdentical(tagData.equals("yes"));
                break;
            }
        }
    }

    /**
     * Parses lines in assembly report containing Chromosome metadata. This array is used to set metadata to
     * corresponding
     * fields in {@link ChromosomeEntity}.
     *
     * @param columns An array of fields in a line of the assembly report file not starting with "#".
     */
    private void parseChromosomeLine(String[] columns) {
        ChromosomeEntity chromosomeEntity = new ChromosomeEntity();

        chromosomeEntity.setName(columns[0]);
        chromosomeEntity.setGenbank(columns[4]);
        chromosomeEntity.setRefseq(columns[6]);

        if (columns.length > 8 && !columns[9].equals("na")) {
            chromosomeEntity.setUcscName(columns[9]);
        }

        if (assemblyEntity == null) {
            assemblyEntity = new AssemblyEntity();
        }
        chromosomeEntity.setAssembly(this.assemblyEntity);

        List<ChromosomeEntity> chromosomes = this.assemblyEntity.getChromosomes();
        if (chromosomes == null) {
            chromosomes = new LinkedList<>();
            assemblyEntity.setChromosomes(chromosomes);
        }
        chromosomes.add(chromosomeEntity);
    }

    /**
     * Parses lines in assembly report containing Scaffold metadata. This array is used to set metadata to corresponding
     * fields in {@link ScaffoldEntity}.
     *
     * @param columns An array of fields in a line of the assembly report file not starting with "#".
     */
    private void parseScaffoldLine(String[] columns) {
        ScaffoldEntity scaffoldEntity = new ScaffoldEntity();

        scaffoldEntity.setName(columns[0]);
        scaffoldEntity.setGenbank(columns[4]);
        scaffoldEntity.setRefseq(columns[6]);

        if (columns.length >= 10) {
            String ucscName = columns[9];
            if (!ucscName.equals("na")) {
                scaffoldEntity.setUcscName(ucscName);
            }
        }

        if (assemblyEntity == null) {
            assemblyEntity = new AssemblyEntity();
        }
        scaffoldEntity.setAssembly(this.assemblyEntity);

        List<ScaffoldEntity> scaffolds = this.assemblyEntity.getScaffolds();
        if (scaffolds == null) {
            scaffolds = new LinkedList<>();
            assemblyEntity.setScaffolds(scaffolds);
        }
        scaffolds.add(scaffoldEntity);
    }

    public boolean ready() throws IOException {
        return reader.ready();
    }
}
