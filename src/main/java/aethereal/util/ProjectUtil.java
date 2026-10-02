package aethereal.util;

import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;

import aethereal.core.Interface;

import lombok.Generated;
import platform.inject.invokers.GameRendererInvoker;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ProjectUtil implements Interface {
    @Generated
    private ProjectUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Vector2f a(double x, double y, double z) {
        net.minecraft.client.render.Camera camera = aM_.getEntityRenderDispatcher().camera;
        Vector3f result3f = new Vector3f((float) (x - camera.getPos().x), (float) (y - camera.getPos().y), (float) (z - camera.getPos().z));
        
        Quaternionf invCamRot = new Quaternionf(camera.getRotation()).conjugate();
        result3f.rotate(invCamRot);
        
        return a(result3f, ((GameRendererInvoker) (Object) aM_.gameRenderer).invokeGetFov(camera, aM_.getRenderTickCounter().getTickDelta(false), true));
    }

    private static Vector2f a(Vector3f result3f, double fov) {
        if (result3f.z >= 0.0f) {
            return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
        }
        float realAspect = (float) aM_.getWindow().getFramebufferWidth() / (float) aM_.getWindow().getFramebufferHeight();
        float modifiedAspect = Delta.h().d().t().aB().m() ? Delta.h().d().t().aB().q() : realAspect;
        
        double halfHeightAtDepth = (-result3f.z) * Math.tan(Math.toRadians(fov / 2.0d));
        double halfWidthAtDepth = halfHeightAtDepth * ((double) modifiedAspect);
        
        double ndcX = result3f.x / halfWidthAtDepth;
        double ndcY = result3f.y / halfHeightAtDepth;
        
        float screenX = (float) (aM_.getWindow().getScaledWidth() / 2.0f + ndcX * (aM_.getWindow().getScaledWidth() / 2.0f));
        float screenY = (float) (aM_.getWindow().getScaledHeight() / 2.0f - ndcY * (aM_.getWindow().getScaledHeight() / 2.0f));
        
        return new Vector2f(screenX, screenY);
    }

    public static float[] a(Box box) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -3.4028235E38f;
        float maxY = -3.4028235E38f;
        for (int corner = 0; corner < 8; corner++) {
            double x = (corner & 1) == 0 ? box.minX : box.maxX;
            double y = (corner & 2) == 0 ? box.minY : box.maxY;
            double z = (corner & 4) == 0 ? box.minZ : box.maxZ;
            Vector2f screen = a(x, y, z);
            if (screen.x() != Float.MAX_VALUE) {
                minX = Math.min(minX, screen.x());
                minY = Math.min(minY, screen.y());
                maxX = Math.max(maxX, screen.x());
                maxY = Math.max(maxY, screen.y());
            }
        }
        if (maxX <= minX || maxY <= minY) {
            return null;
        }
        return new float[]{minX, minY, maxX, maxY};
    }

    public static boolean a(Vector2f screen) {
        return screen.x() != Float.MAX_VALUE && screen.y() != Float.MAX_VALUE && screen.x() >= 0.0f && screen.y() >= 0.0f && screen.x() <= ((float) aM_.getWindow().getScaledWidth()) && screen.y() <= ((float) aM_.getWindow().getScaledHeight());
    }
}
