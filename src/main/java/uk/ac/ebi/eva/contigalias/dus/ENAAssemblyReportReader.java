/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
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

import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;

import java.util.ArrayList;
import java.util.List;

public class ENAAssemblyReportReader {

    public static List<ChromosomeEntity> getChromosomeEntity(List<String> lines) {
        List<ChromosomeEntity> chromosomeEntityList = new ArrayList<>();
        for (String line : lines) {
            ChromosomeEntity chromosomeEntity = getChromosomeEntity(line);
            if (chromosomeEntity != null) {
                chromosomeEntityList.add(chromosomeEntity);
            }
        }

        return chromosomeEntityList;
    }

    public static ChromosomeEntity getChromosomeEntity(String line) {
        if (!line.startsWith("accession")) {
            String[] columns = line.split("\t", -1);
            if (columns.length >= 6) {
                if (columns[5].equals("Chromosome") && columns[3].equals("assembled-molecule")) {
                    return getChromosome(columns);
                } else {
                    return getScaffold(columns);
                }
            }
        }

        return null;
    }

    public static ChromosomeEntity getChromosome(String[] columns) {
        ChromosomeEntity chromosomeEntity = new ChromosomeEntity();
        chromosomeEntity.setInsdcAccession(columns[0]);
        chromosomeEntity.setEnaSequenceName(columns[1]);
        chromosomeEntity.setContigType(SequenceEntity.ContigType.CHROMOSOME);

        return chromosomeEntity;
    }

    public static ChromosomeEntity getScaffold(String[] columns) {
        ChromosomeEntity scaffoldEntity = new ChromosomeEntity();
        scaffoldEntity.setInsdcAccession(columns[0]);
        scaffoldEntity.setEnaSequenceName(columns[1]);
        scaffoldEntity.setContigType(SequenceEntity.ContigType.SCAFFOLD);

        return scaffoldEntity;
    }

}
