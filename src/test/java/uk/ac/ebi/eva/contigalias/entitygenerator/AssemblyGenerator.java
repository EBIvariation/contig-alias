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

import java.util.LinkedList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssemblyGenerator {

    private static final String PREFIX_NAME = "name";

    public static AssemblyEntity generate(long id) {
        return new AssemblyEntity()
                .setName(PREFIX_NAME + id)
                .setOrganism("organism" + id)
                .setGenbank("genbank" + id)
                .setRefseq("refseq" + id)
                .setTaxid(id)
                .setGenbankRefseqIdentical(new Random().nextBoolean())
                .setMd5checksum("md5" + id)
                .setTrunc512checksum("trunc512" + id)
                .setChromosomes(new LinkedList<>())
                .setScaffolds(new LinkedList<>());
    }

    public static AssemblyEntity generate() {
        long id = new Random().nextLong();
        return generate(id);
    }

    @Test
    void generateTest() {
        AssemblyEntity entity = generate();
        int length = PREFIX_NAME.length();
        String name = entity.getName();
        assertTrue(name.length() > length);
        String sId = name.substring(length);
        assertTrue(name.endsWith(sId));
        assertTrue(entity.getOrganism().endsWith(sId));
        assertTrue(entity.getGenbank().endsWith(sId));
        assertTrue(entity.getRefseq().endsWith(sId));
        assertTrue(entity.getMd5checksum().endsWith(sId));
        assertTrue(entity.getTrunc512checksum().endsWith(sId));
        assertNotNull(entity.getChromosomes());
        assertNotNull(entity.getScaffolds());
    }

    @Test
    void generateWithSpecifiedIdTest() {
        long id = 983275;
        AssemblyEntity entity = generate(id);
        String sId = Long.toString(id);
        assertTrue(entity.getName().endsWith(sId));
        assertTrue(entity.getOrganism().endsWith(sId));
        assertEquals(entity.getTaxid(), id);
        assertTrue(entity.getGenbank().endsWith(sId));
        assertTrue(entity.getRefseq().endsWith(sId));
        assertTrue(entity.getMd5checksum().endsWith(sId));
        assertTrue(entity.getTrunc512checksum().endsWith(sId));
        assertNotNull(entity.getChromosomes());
        assertNotNull(entity.getScaffolds());
    }


}
