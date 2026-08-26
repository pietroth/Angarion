package br.angarion.dev;

import br.angarion.dev.api.communication.Type;

@Type(family = Family1.class, payload = Example2Payload.class, isNotification = false, isCpuIntensive = false, isBlocking = true)
public class Example2 {}
