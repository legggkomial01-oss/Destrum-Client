package aethereal.render.targetesp;

public interface ParticleLayout {
    void frame(ParticleBuffer particleBuffer, float[] frameData, float progress, int index);

    void place(int vertexIndex, float[] sourceData, float[] targetCoords, int dataIndex, ParticleBuffer particleBuffer);
}
