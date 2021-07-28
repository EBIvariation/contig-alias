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

public class ENAAssemblyReportReader extends AssemblyReportReader {

    public ENAAssemblyReportReader(InputStreamReader inputStreamReader, boolean isScaffoldsEnabled) {
        super(inputStreamReader, isScaffoldsEnabled);
    }

    // Not present in ENA assembly reports
    protected void parseAssemblyData(String line) {}

    protected void parseChromosomeLine(String[] columns) {
        ChromosomeEntity chromosomeEntity = new ChromosomeEntity();

        chromosomeEntity.setGenbank(columns[0]);
        chromosomeEntity.setEnaSequenceName(columns[1]);

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

        scaffoldEntity.setGenbank(columns[0]);
        scaffoldEntity.setEnaSequenceName(columns[1]);

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
