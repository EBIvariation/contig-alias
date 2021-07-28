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

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;

import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class NCBIAssemblyReportReader extends AssemblyReportReader {

    public NCBIAssemblyReportReader(InputStreamReader inputStreamReader, boolean isScaffoldsEnabled) {
        super(inputStreamReader, isScaffoldsEnabled);
    }

    protected void parseAssemblyData(String line) {
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

    protected void parseChromosomeLine(String[] columns) {
        ChromosomeEntity chromosomeEntity = new ChromosomeEntity();

        chromosomeEntity.setName(columns[0]);
        chromosomeEntity.setGenbank(columns[4]);
        chromosomeEntity.setRefseq(columns[6]);

        if (columns.length > 9 && !columns[9].equals("na")) {
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

    protected void parseScaffoldLine(String[] columns) {
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

}
