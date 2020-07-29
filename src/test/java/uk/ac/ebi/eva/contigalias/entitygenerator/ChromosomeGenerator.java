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

package uk.ac.ebi.eva.contigalias.entitygenerator;

import org.junit.jupiter.api.Test;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChromosomeGenerator {

    private static final String PREFIX_NAME = "name";

    public static ChromosomeEntity generate(long id) {
        return new ChromosomeEntity()
                .setName(PREFIX_NAME + id)
                .setGenbank("genbank" + id)
                .setRefseq("refseq" + id)
                .setUcscName("ucsc" + id)
                .setAssembly(null);
    }

    public static ChromosomeEntity generate() {
        long id = new Random().nextLong();
        return generate(id);
    }

    public static ChromosomeEntity generate(long id, AssemblyEntity assembly) {
        if (assembly == null) {
            throw new IllegalArgumentException("Assembly cannot be null!");
        }
        ChromosomeEntity entity = generate(id);
        if (assembly.getChromosomes() == null) {
            assembly.setChromosomes(new LinkedList<>());
        }
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        chromosomes.add(entity);
        entity.setAssembly(assembly);
        return entity;
    }

    public static ChromosomeEntity generate(AssemblyEntity assembly) {
        long id = new Random().nextLong();
        return generate(id, assembly);
    }

    @Test
    void generateTest() {
        ChromosomeEntity entity = generate();
        int length = PREFIX_NAME.length();
        String name = entity.getName();
        assertTrue(name.length() > length);
        String sId = name.substring(length);
        assertTrue(name.endsWith(sId));
        assertTrue(entity.getGenbank().endsWith(sId));
        assertTrue(entity.getRefseq().endsWith(sId));
        assertTrue(entity.getUcscName().endsWith(sId));
        assertNull(entity.getAssembly());
    }

    @Test
    void generateForGivenAssemblyTest() {
        AssemblyEntity assembly = new AssemblyEntity();
        int iterate = 10;
        for (int i = 0; i < iterate; i++) {
            ChromosomeEntity generate = generate(assembly);
            assertEquals(generate, assembly.getChromosomes().get(i));
            assertEquals(assembly, generate.getAssembly());
        }
        assertEquals(iterate, assembly.getChromosomes().size());
    }

    @Test
    void generateForGivenAssemblyWithSpecifiedIdTest() {
        AssemblyEntity assembly = new AssemblyEntity()
                .setChromosomes(new LinkedList<>());
        int iterate = 10;
        for (int i = 0; i < iterate; i++) {
            ChromosomeEntity generate = generate(i, assembly);
            assertEquals(generate, assembly.getChromosomes().get(i));
            assertEquals(assembly, generate.getAssembly());
        }
        assertEquals(iterate, assembly.getChromosomes().size());
    }

}
