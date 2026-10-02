package aethereal.render.targetesp;

public class TriangleParticleLayout implements ParticleLayout {
    @Override
    public void frame(ParticleBuffer pb, float[] data, float progress, int index) {
        data[index] = TargetEffect.triangleWave(TargetEffect.fract(progress + TargetEffect.noise(21, progress * 0.7f) * 0.05f)) * pb.height;
        data[index + 1] = progress * (float) (Math.PI * 2.0) * 2.2f + TargetEffect.noise(22, progress * 0.9f) * 0.65f;
    }

    @Override
    public void place(int vertexIndex, float[] sourceData, float[] targetCoords, int dataIndex, ParticleBuffer pb) {
        int sideSegments = Math.max(4, pb.density / 3);
        int side = vertexIndex / sideSegments;
        float t = (float) (vertexIndex % sideSegments) / sideSegments;
        float a1 = sourceData[dataIndex + 1] + side * (float) (Math.PI * 2.0) / 3.0f;
        float a2 = a1 + (float) (Math.PI * 2.0 / 3.0);
        float x1 = (float) Math.cos(a1) * pb.radius;
        float z1 = (float) Math.sin(a1) * pb.radius;
        float x2 = (float) Math.cos(a2) * pb.radius;
        float z2 = (float) Math.sin(a2) * pb.radius;
        targetCoords[0] = x1 + (x2 - x1) * t;
        targetCoords[1] = sourceData[dataIndex];
        targetCoords[2] = z1 + (z2 - z1) * t;
    }
}
