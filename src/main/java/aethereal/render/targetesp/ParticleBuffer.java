package aethereal.render.targetesp;

import net.minecraft.client.render.BufferBuilder;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class ParticleBuffer {
    private BufferBuilder bufferBuilder;
    private Matrix4f matrix4f;
    private final Vector3f camRight = new Vector3f();
    private final Vector3f camUp = new Vector3f();

    private float posX;
    private float posY;
    private float posZ;
    private int baseColor;
    private float alphaScale;
    private boolean isReversed;
    private float glowIntensity;

    public float radius;
    public float height;
    public float thickness;
    public float phase;
    public int density;
    public int tail;

    private float[] normalBuffer = new float[128];
    private float[] tangentBuffer = new float[128];
    private float[] tempCoords = new float[128];
    private float[] floatArray4 = new float[128];
    public final float[] vertexPos = new float[3];
    private int[] intArray = new int[64];
    private int[] intArray2 = new int[64];

    public void setup(
        int color,
        BufferBuilder builder,
        float x,
        float y,
        Matrix4f matrix,
        float targetHeight,
        Vector3f up,
        Vector3f right,
        int densitySegments,
        float lineThickness,
        int tailSegments,
        float glow,
        float z,
        float alpha,
        float targetRadius,
        boolean reversed,
        float currentPhase
    ) {
        this.bufferBuilder = builder;
        this.matrix4f = matrix;
        this.camRight.set(right);
        this.camUp.set(up);
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.radius = targetRadius;
        this.height = targetHeight;
        this.thickness = lineThickness;
        this.phase = currentPhase;
        this.density = densitySegments;
        this.tail = tailSegments;
        this.baseColor = color;
        this.alphaScale = alpha;
        this.isReversed = reversed;
        this.glowIntensity = glow;
    }

    public int[] getIntArray2(int count) {
        if (this.intArray2.length < count) {
            this.intArray2 = new int[count];
        }
        return this.intArray2;
    }

    public int[] getIntArray(int count) {
        if (this.intArray.length < count) {
            this.intArray = new int[count];
        }
        return this.intArray;
    }

    public float[] getFloatArray(int count) {
        if (this.tempCoords.length < count) {
            this.tempCoords = new float[count];
        }
        return this.tempCoords;
    }

    public float[] getFloatArray4(int count) {
        if (this.floatArray4.length < count) {
            this.floatArray4 = new float[count];
        }
        return this.floatArray4;
    }

    public int computeColor(float factor) {
        int alpha = (int) (((this.baseColor >>> 24) & 0xFF) * this.alphaScale * factor * this.glowIntensity);
        return (Math.min(255, Math.max(0, alpha)) << 24) | (this.baseColor & 0x00FFFFFF);
    }

    public float getFade(float progress) {
        float f = this.isReversed
            ? this.alphaScale * 1.18f - progress
            : progress - ((1.0f - this.alphaScale) * 1.18f - 0.18f);
        return f <= 0.0f ? 0.0f : Math.min(1.0f, f / 0.18f);
    }

    public void drawRibbon(float halfWidth, float[] points, int[] colors, boolean closed, int count) {
        renderRibbon(colors, count, halfWidth, points, 0, closed);
    }

    public void drawRibbonSingleColor(boolean closed, int count, int color, float[] points, float width) {
        if ((color >>> 24) != 0) {
            renderRibbon(null, count, width, points, color, closed);
        }
    }

    private void renderRibbon(int[] colors, int count, float width, float[] points, int fallbackColor, boolean closed) {
        if (count < 2) return;

        if (this.normalBuffer.length < count * 2) {
            this.normalBuffer = new float[count * 2];
        }
        if (this.tangentBuffer.length < count * 2) {
            this.tangentBuffer = new float[count * 2];
        }

        float halfW = width * 0.5f;
        int segCount = closed ? count : count - 1;

        for (int j = 0; j < segCount; j++) {
            int next = (j + 1) % count;
            calcTangent(j, j * 2, points, next);
        }

        float lastTx = 0.0f;
        float lastTy = 0.0f;

        for (int k = 0; k < segCount; k++) {
            if (this.tangentBuffer[k * 2] == 0.0f && this.tangentBuffer[k * 2 + 1] == 0.0f) {
                this.tangentBuffer[k * 2] = lastTx;
                this.tangentBuffer[k * 2 + 1] = lastTy;
            } else {
                lastTx = this.tangentBuffer[k * 2];
                lastTy = this.tangentBuffer[k * 2 + 1];
            }
        }

        for (int l = segCount - 1; l >= 0; l--) {
            if (this.tangentBuffer[l * 2] == 0.0f && this.tangentBuffer[l * 2 + 1] == 0.0f) {
                this.tangentBuffer[l * 2] = lastTx;
                this.tangentBuffer[l * 2 + 1] = lastTy;
            } else {
                lastTx = this.tangentBuffer[l * 2];
                lastTy = this.tangentBuffer[l * 2 + 1];
            }
        }

        for (int i = 0; i < count; i++) {
            int prevIdx = closed ? (i + count - 1) % count : Math.max(0, i - 1);
            int nextIdx = closed ? i : Math.min(i, segCount - 1);
            float px0 = this.tangentBuffer[prevIdx * 2];
            float py0 = this.tangentBuffer[prevIdx * 2 + 1];
            float px1 = this.tangentBuffer[nextIdx * 2];
            float py1 = this.tangentBuffer[nextIdx * 2 + 1];
            float nx = -py0 - py1;
            float ny = px0 + px1;
            float len = (float) Math.sqrt(nx * nx + ny * ny);
            if (len < 1.0e-5f) {
                nx = -py1;
                ny = px1;
                len = (float) Math.sqrt(nx * nx + ny * ny);
            }
            if (len < 1.0e-5f) {
                nx = 1.0f;
                ny = 0.0f;
                len = 1.0f;
            }
            nx /= len;
            ny /= len;
            float dot = Math.abs(nx * -py1 + ny * px1);
            float scale = (dot < 0.35f) ? halfW : (halfW / dot);
            this.normalBuffer[i * 2] = nx * scale;
            this.normalBuffer[i * 2 + 1] = ny * scale;
        }

        for (int j = 0; j < segCount; j++) {
            int next = (j + 1) % count;
            int c1 = (colors == null) ? fallbackColor : colors[j];
            int c2 = (colors == null) ? fallbackColor : colors[next];
            if ((c1 >>> 24) != 0 || (c2 >>> 24) != 0) {
                emitSegment(c1, next, points, j, this.normalBuffer, c2);
            }
        }
    }

    private void emitSegment(int c1, int idx2, float[] points, int idx1, float[] normals, int c2) {
        float x1 = this.posX + points[idx1 * 3];
        float y1 = this.posY + points[idx1 * 3 + 1];
        float z1 = this.posZ + points[idx1 * 3 + 2];

        float x2 = this.posX + points[idx2 * 3];
        float y2 = this.posY + points[idx2 * 3 + 1];
        float z2 = this.posZ + points[idx2 * 3 + 2];

        float n1x = normals[idx1 * 2];
        float n1y = normals[idx1 * 2 + 1];
        float n2x = normals[idx2 * 2];
        float n2y = normals[idx2 * 2 + 1];

        float off1x = this.camRight.x * n1x + this.camUp.x * n1y;
        float off1y = this.camRight.y * n1x + this.camUp.y * n1y;
        float off1z = this.camRight.z * n1x + this.camUp.z * n1y;

        float off2x = this.camRight.x * n2x + this.camUp.x * n2y;
        float off2y = this.camRight.y * n2x + this.camUp.y * n2y;
        float off2z = this.camRight.z * n2x + this.camUp.z * n2y;

        this.bufferBuilder.vertex(this.matrix4f, x1 - off1x, y1 - off1y, z1 - off1z).texture(0.0f, 0.5f).color(c1);
        this.bufferBuilder.vertex(this.matrix4f, x2 - off2x, y2 - off2y, z2 - off2z).texture(0.0f, 0.5f).color(c2);
        this.bufferBuilder.vertex(this.matrix4f, x2 + off2x, y2 + off2y, z2 + off2z).texture(1.0f, 0.5f).color(c2);
        this.bufferBuilder.vertex(this.matrix4f, x1 + off1x, y1 + off1y, z1 + off1z).texture(1.0f, 0.5f).color(c1);
    }

    private void calcTangent(int from, int outIdx, float[] points, int to) {
        float dx = points[to * 3] - points[from * 3];
        float dy = points[to * 3 + 1] - points[from * 3 + 1];
        float dz = points[to * 3 + 2] - points[from * 3 + 2];
        float tx = dx * this.camRight.x + dy * this.camRight.y + dz * this.camRight.z;
        float ty = dx * this.camUp.x + dy * this.camUp.y + dz * this.camUp.z;
        float len = (float) Math.sqrt(tx * tx + ty * ty);
        if (len < 1.0e-5f) {
            this.tangentBuffer[outIdx] = 0.0f;
            this.tangentBuffer[outIdx + 1] = 0.0f;
        } else {
            this.tangentBuffer[outIdx] = tx / len;
            this.tangentBuffer[outIdx + 1] = ty / len;
        }
    }

    public void drawBillboardQuad(float x, float size, float z, float y, int color) {
        if ((color >>> 24) == 0) return;
        float half = size * 0.5f;
        float cx = this.posX + x;
        float cy = this.posY + y;
        float cz = this.posZ + z;

        float rx = this.camRight.x * half;
        float ry = this.camRight.y * half;
        float rz = this.camRight.z * half;

        float ux = this.camUp.x * half;
        float uy = this.camUp.y * half;
        float uz = this.camUp.z * half;

        this.bufferBuilder.vertex(this.matrix4f, cx - rx + ux, cy - ry + uy, cz - rz + uz).texture(0.0f, 1.0f).color(color);
        this.bufferBuilder.vertex(this.matrix4f, cx + rx + ux, cy + ry + uy, cz + rz + uz).texture(1.0f, 1.0f).color(color);
        this.bufferBuilder.vertex(this.matrix4f, cx + rx - ux, cy + ry - uy, cz + rz - uz).texture(1.0f, 0.0f).color(color);
        this.bufferBuilder.vertex(this.matrix4f, cx - rx - ux, cy - ry - uy, cz - rz - uz).texture(0.0f, 0.0f).color(color);
    }
}
