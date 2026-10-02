package aethereal.render;

import net.minecraft.client.render.VertexConsumer;

public final class ColoredVertexConsumer implements VertexConsumer {
    private final VertexConsumer vertexConsumer;

    public ColoredVertexConsumer(VertexConsumer vertexConsumer) {
        this.vertexConsumer = vertexConsumer;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        this.vertexConsumer.normal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        this.vertexConsumer.vertex(x, y, z).color(-1);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        this.vertexConsumer.texture(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.vertexConsumer.light(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        this.vertexConsumer.overlay(u, v);
        return this;
    }
}
