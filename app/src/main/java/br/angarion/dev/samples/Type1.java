package br.angarion.dev.samples;

import br.angarion.dev.api.communication.DataField;
import br.angarion.dev.api.communication.Type;
import br.angarion.dev.api.communication.TypeConfiguration;
import br.angarion.dev.api.communication.ValidatorResponse;

@TypeConfiguration(name = "type1", family = Family1.class)
public class Type1 implements Type {
    @DataField(1)
    private int field1 = 0;

    @Override
    public ValidatorResponse validate() {
        return null;
    }

    @Override
    public void onSuccess() {
    }

    @Override
    public void onInvalid() {
    }

    @Override
    public void onPartial() {
    }

    
}
