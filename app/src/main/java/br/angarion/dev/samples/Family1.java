package br.angarion.dev.samples;

import br.angarion.dev.api.communication.DataField;
import br.angarion.dev.api.communication.Family;
import br.angarion.dev.api.communication.FamilyConfiguration;

@FamilyConfiguration("family1")
public class Family1 implements Family {
    @DataField(1)
    private final int field1 = 0;
    @DataField(2) 
    private final String field2 = null;
}
