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
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NCBIAssemblyReportReader extends AssemblyReportReader {

    public NCBIAssemblyReportReader(InputStreamReader inputStreamReader, boolean isScaffoldsEnabled) {
        super(inputStreamReader, isScaffoldsEnabled);
    }

    protected void parseReport() throws IOException, NullPointerException {
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
                if (columns.length >= 6 && (columns[5].equals("=") || columns[5].equals("<>")) &&
                        (columns[4] != null && !columns[4].isEmpty() && !columns[4].equals("na"))) {
                    if (columns[3].equals("Chromosome") && columns[1].equals("assembled-molecule")) {
                        parseChromosomeLine(columns);
                    } else if (isScaffoldsEnabled) {
                        parseScaffoldLine(columns);
                    }
                }
            }
            line = reader.readLine();
        }
        reportParsed = true;
        reader.close();
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
                assemblyEntity.setInsdcAccession(tagData);
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
        ChromosomeEntity chromosomeEntity = getChromosome(columns);
        if (chromosomeEntity == null) {
            return;
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
        ChromosomeEntity scaffoldEntity = getScaffold(columns);
        if (scaffoldEntity == null) {
            return;
        }

        if (assemblyEntity == null) {
            assemblyEntity = new AssemblyEntity();
        }
        scaffoldEntity.setAssembly(this.assemblyEntity);

        List<ChromosomeEntity> scaffolds = this.assemblyEntity.getChromosomes();
        if (scaffolds == null) {
            scaffolds = new LinkedList<>();
            assemblyEntity.setChromosomes(scaffolds);
        }
        scaffolds.add(scaffoldEntity);
    }

    public static AssemblyEntity getAssemblyEntity(List<String> lines) {
        Map<String, String> tagAndValuesMap = lines.stream()
                .filter(line -> line.startsWith("#"))
                .filter(line -> line.indexOf(':') != -1)
                .collect(Collectors.toMap(l -> l.substring(2, l.indexOf(':')), l -> l.substring(l.indexOf(':') + 1).trim()));

        AssemblyEntity asmEntity = new AssemblyEntity();
        for (Map.Entry<String, String> entry : tagAndValuesMap.entrySet()) {
            String tag = entry.getKey();
            String tagData = entry.getValue();
            switch (tag) {
                case "Assembly name": {
                    asmEntity.setName(tagData);
                    break;
                }
                case "Organism name": {
                    asmEntity.setOrganism(tagData);
                    break;
                }
                case "Taxid": {
                    asmEntity.setTaxid(Long.parseLong(tagData));
                    break;
                }
                case "GenBank assembly accession": {
                    asmEntity.setInsdcAccession(tagData);
                    break;
                }
                case "RefSeq assembly accession": {
                    asmEntity.setRefseq(tagData);
                    break;
                }
                case "RefSeq assembly and GenBank assemblies identical": {
                    asmEntity.setGenbankRefseqIdentical(tagData.equals("yes"));
                    break;
                }
            }
        }

        return asmEntity;
    }

    public static ChromosomeEntity getChromosomeEntity(String line) {
        String[] columns = line.split("\t", -1);
        if (columns.length >= 6 && (columns[5].equals("=") || columns[5].equals("<>")) &&
                (columns[4] != null && !columns[4].isEmpty() && !columns[4].equals("na"))) {
            if (columns[3].equals("Chromosome") && columns[1].equals("assembled-molecule")) {
                return getChromosome(columns);
            } else {
                return getScaffold(columns);
            }
        }

        return null;
    }

    public static ChromosomeEntity getChromosome(String[] columns) {
        ChromosomeEntity chromosomeEntity = new ChromosomeEntity();

        chromosomeEntity.setGenbankSequenceName(columns[0]);
        chromosomeEntity.setInsdcAccession(columns[4]);
        if (columns[6] == null || columns[6].isEmpty() || columns[6].equals("na")) {
            chromosomeEntity.setRefseq(null);
        } else {
            chromosomeEntity.setRefseq(columns[6]);
        }
        if (columns.length > 8) {
            try {
                Long seqLength = Long.parseLong(columns[8]);
                chromosomeEntity.setSeqLength(seqLength);
            } catch (NumberFormatException nfe) {

            }
        }
        if (columns.length > 9 && !columns[9].equals("na")) {
            chromosomeEntity.setUcscName(columns[9]);
        }
        chromosomeEntity.setContigType(SequenceEntity.ContigType.CHROMOSOME);

        return chromosomeEntity;
    }

    public static ChromosomeEntity getScaffold(String[] columns) {
        ChromosomeEntity scaffoldEntity = new ChromosomeEntity();

        scaffoldEntity.setGenbankSequenceName(columns[0]);
        scaffoldEntity.setInsdcAccession(columns[4]);
        if (columns[6] == null || columns[6].isEmpty() || columns[6].equals("na")) {
            scaffoldEntity.setRefseq(null);
        } else {
            scaffoldEntity.setRefseq(columns[6]);
        }
        if (columns.length > 8) {
            try {
                Long seqLength = Long.parseLong(columns[8]);
                scaffoldEntity.setSeqLength(seqLength);
            } catch (NumberFormatException nfe) {

            }
        }
        if (columns.length >= 10) {
            String ucscName = columns[9];
            if (!ucscName.equals("na")) {
                scaffoldEntity.setUcscName(ucscName);
            }
        }
        scaffoldEntity.setContigType(SequenceEntity.ContigType.SCAFFOLD);

        return scaffoldEntity;
    }

}
