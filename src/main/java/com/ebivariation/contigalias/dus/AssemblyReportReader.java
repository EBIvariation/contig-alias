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

package com.ebivariation.contigalias.dus;

import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.entities.ChromosomeEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class AssemblyReportReader {

    private final BufferedReader reader;

    private AssemblyEntity assemblyEntity;

    private boolean reportParsed = false;

    public AssemblyReportReader(InputStreamReader inputStreamReader) {
        reader = new BufferedReader(inputStreamReader);
    }

    /**
     * Returns the class-level instance variable of {@link AssemblyEntity}. If the variable has not been initialized
     * or the assembly report has not been parsed yet, it calls {@link #parseReport()} and then returns the variable
     * that would have been initialized by {@link #parseReport()}.
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
     * @throws IOException Passes IOException thrown by {@link BufferedReader#readLine()}
     */
    private void parseReport() throws IOException {
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("# ")) {
                if (assemblyEntity == null) {
                    assemblyEntity = new AssemblyEntity();
                }
                parseAssemblyData(line);
            } else if (!line.startsWith("#")) {
                parseChromosomeLine(line);
            }
            line = reader.readLine();
        }
        reportParsed = true;
    }

    /**
     * Parses lines in assembly report containing Assembly metadata. Breaks line into a tag:tagData format and
     * sets tagData in {@link AssemblyEntity} fields corresponding to the tag found in given line.
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
     * Parses lines in assembly report containing Chromosome metadata. Splits line into an array of fields using
     * {@link String#split(String)} with "\t" as the separator. This array is used to set metadata to corresponding
     * fields in {@link ChromosomeEntity}.
     * @param line A line of assembly report file not starting with "#".
     */
    private void parseChromosomeLine(String line) {

        String[] columns = line.split("\t", -1);

        if (columns.length < 6 || !columns[3].equals("Chromosome")) {
            return;
        }

        ChromosomeEntity chromosomeEntity = new ChromosomeEntity();

        chromosomeEntity.setName(columns[0]);
        chromosomeEntity.setGenbank(columns[4]);
        chromosomeEntity.setRefseq(columns[6]);

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

    public boolean ready() throws IOException {
        return reader.ready();
    }
}
