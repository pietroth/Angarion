package br.angarion.dev.infra.network;

import io.netty.util.AttributeKey;

final class ClientIdAttributeKey {
    static final AttributeKey<Integer> CLIENT_ID = AttributeKey.valueOf("angarion.client-id");
}
