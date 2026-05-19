package br.angarion.dev.samples;

import br.angarion.dev.api.communication.DataField;
import br.angarion.dev.api.communication.TypeConfiguration;

@TypeConfiguration(name = "type2", family = Family1.class)
public class Type2 {
    @DataField(1)
    private int field1 = 0;

    
}
