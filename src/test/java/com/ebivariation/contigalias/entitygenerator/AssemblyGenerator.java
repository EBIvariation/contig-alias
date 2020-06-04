/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ebivariation.contigalias.entitygenerator;

import com.ebivariation.contigalias.entities.AssemblyEntity;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssemblyGenerator {

    public static AssemblyEntity generate(long id) {
        return new AssemblyEntity()
                .setName("name" + id)
                .setOrganism("organism" + id)
                .setGenbank("genbank" + id)
                .setRefseq("refseq" + id)
                .setTaxid(id)
                .setGenbankRefseqIdentical(new Random().nextBoolean())
                .setChromosomes(new LinkedList<>());
    }

    @Test
    void generateTest() {
        long id = 983275;
        AssemblyEntity entity = generate(id);
        String sId = Long.toString(id);
        assertTrue(entity.getName().endsWith(sId));
        assertTrue(entity.getOrganism().endsWith(sId));
        assertEquals(entity.getTaxid(), id);
        assertTrue(entity.getGenbank().endsWith(sId));
        assertTrue(entity.getRefseq().endsWith(sId));
        assertNotNull(entity.getChromosomes());
    }

}
