package aethereal.render.targetesp;

public class RingParticleLayout implements ParticleLayout {
    @Override
    public void frame(ParticleBuffer pb, float[] data, float progress, int index) {
        data[index] = TargetEffect.triangleWave(TargetEffect.fract(progress + TargetEffect.noise(11, progress * 0.7f) * 0.05f)) * pb.height;
        data[index + 1] = progress * 3.0f + TargetEffect.noise(12, progress * 0.6f) * 3.0f;
    }

    @Override
    public void place(int vertexIndex, float[] sourceData, float[] targetCoords, int dataIndex, ParticleBuffer pb) {
        float angle = (float) (Math.PI * 2.0) * vertexIndex / pb.density;
        float r = pb.radius * (1.0f + 0.045f * (float) Math.sin(angle * 2.0f + sourceData[dataIndex + 1]));
        targetCoords[0] = (float) Math.cos(angle) * r;
        targetCoords[1] = sourceData[dataIndex];
        targetCoords[2] = (float) Math.sin(angle) * r;
    }
}
