package br.angarion.dev;

import br.angarion.dev.api.communication.Type;

@Type(family = Family1.class, payload = Example1Payload.class, isNotification = false, isCpuIntensive = false, isBlocking = false)
public class Example1 {}
