package br.angarion.dev.engine.network.client;

import br.angarion.dev.engine.network.transport.Channel;

public class Client {
    private final int id;
    private final Channel channel;

    public Client(Builder builder) {
        this.id = builder.id;
        this.channel = builder.channel;
    }

    public int getId() {
        return id;
    }

    public Channel getChannel() {
        return channel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int id;
        private Channel channel;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder channel(Channel channel) {
            this.channel = channel;
            return this;
        }

        public Client build() {
            return new Client(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;

        Client other = (Client) o;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
