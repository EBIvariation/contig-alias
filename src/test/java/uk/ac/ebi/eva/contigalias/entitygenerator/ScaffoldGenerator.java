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
import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScaffoldGenerator {

    private static final String PREFIX_NAME = "name";

    public static ScaffoldEntity generate(long id) {
        return (ScaffoldEntity) new ScaffoldEntity()
                .setName(PREFIX_NAME + id)
                .setGenbank("genbank" + id)
                .setRefseq("refseq" + id)
                .setUcscName("ucsc" + id)
                .setAssembly(null);
    }

    public static ScaffoldEntity generate() {
        long id = new Random().nextLong();
        return generate(id);
    }

    public static ScaffoldEntity generate(long id, AssemblyEntity assembly) {
        if (assembly == null) {
            throw new IllegalArgumentException("Assembly cannot be null!");
        }
        ScaffoldEntity entity = generate(id);
        if (assembly.getScaffolds() == null) {
            assembly.setScaffolds(new LinkedList<>());
        }
        List<ScaffoldEntity> scaffolds = assembly.getScaffolds();
        scaffolds.add(entity);
        entity.setAssembly(assembly);
        return entity;
    }

    public static ScaffoldEntity generate(AssemblyEntity assembly) {
        long id = new Random().nextLong();
        return generate(id, assembly);
    }

    @Test
    void generateTest() {
        ScaffoldEntity entity = generate();
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
            ScaffoldEntity generate = generate(assembly);
            assertEquals(generate, assembly.getScaffolds().get(i));
            assertEquals(assembly, generate.getAssembly());
        }
        assertEquals(iterate, assembly.getScaffolds().size());
    }

    @Test
    void generateForGivenAssemblyWithSpecifiedIdTest() {
        AssemblyEntity assembly = new AssemblyEntity()
                .setScaffolds(new LinkedList<>());
        int iterate = 10;
        for (int i = 0; i < iterate; i++) {
            ScaffoldEntity generate = generate(i, assembly);
            assertEquals(generate, assembly.getScaffolds().get(i));
            assertEquals(assembly, generate.getAssembly());
        }
        assertEquals(iterate, assembly.getScaffolds().size());
    }

}
