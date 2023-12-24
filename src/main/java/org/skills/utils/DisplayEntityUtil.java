package org.skills.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skills.abilities.vergil.VergilMirageEdgeSlash;
import org.skills.main.SLogger;

import java.util.Arrays;

public final class DisplayEntityUtil {
    public static Entity spawnDisplay(Player player, Location spawnLoc) {
        ItemDisplay itemDisplay = (ItemDisplay) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ITEM_DISPLAY);
        itemDisplay.setItemStack(XMaterial.DIAMOND_SWORD.parseItem());
        // itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
        // itemDisplay.setBillboard(Display.Billboard.CENTER); Automatic rotation
        itemDisplay.setBrightness(new Display.Brightness(10, 10));
        itemDisplay.setDisplayHeight(20);
        itemDisplay.setDisplayWidth(20);

        itemDisplay.setInterpolationDuration(40);
        itemDisplay.setInterpolationDelay(-1);

        Location lookAt = spawnLoc.clone().add(spawnLoc.getDirection().normalize().multiply(5));
        // https://github.com/JOML-CI/JOML/blob/main/src/jmh/java/org/joml/jmh/Matrix4f.java
        Vector axis = player.getEyeLocation().getDirection();
        Transformation transformation = itemDisplay.getTransformation();
        Quaternionf dest2 = transformation.getRightRotation(); // https://www.evl.uic.edu/ralph/508S98/coordinates.html

        double[] angles = Arrays.stream(VergilMirageEdgeSlash.angles.getOrDefault("p", new double[3])).map(Math::toDegrees).toArray();
        if (!player.isSneaking()) {
//            Quaternionf dest1 = new Quaternionf();
//            Quaternionf dest2 = new Quaternionf();

            // blank out my quaternions
//            dest1.w = 1f;
//            dest1.x = 0f;
//            dest1.y = 0f;
//            dest1.z = 0f;

            dest2.w = 1f;
            dest2.x = 0f;
            dest2.y = 0f;
            dest2.z = 0f;

            // I keep my pitch/yaw/roll in another Vector3F called rotationXYZ as degrees
//            Vector rotationXYZ = new Vector(spawnLoc.getPitch() + 90, -spawnLoc.getYaw(), 0);
            SLogger.info("angles: " + Arrays.toString(angles));
            Vector rotationXYZ = new Vector(angles[0], angles[1], angles[2]);
            double x, y, z;
            x = rotationXYZ.getX();// + rotationXYZ.x;// testX;
            y = rotationXYZ.getY();// + rotationXYZ.y;//testY;
            z = rotationXYZ.getZ();// + rotationXYZ.z;//testZ;

            // just make sure the degrees values don't get bigger than they need to be
            x = x % 360.0f;
            y = y % 360.0f;
            z = z % 360.0f;

            // convert to radians and start transforming
            Vector3f v = new Vector3f();
            dest2.rotateXYZ((float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
            // dest1.transform(v);

            // Use v now

        } else {
            ToQuaternion(dest2, angles[0], angles[1], angles[2]);
        }

        itemDisplay.setTransformation(transformation);
//        transformation.getLeftRotation()
//                .set(new AxisAngle4f(20, (float) axis.getX(), (float) axis.getY(), (float) axis.getZ()));
//        itemDisplay.setTransformation(transformation);
        return itemDisplay;
    }

    static void ToQuaternion(Quaternionf q, double roll, double pitch, double yaw) // roll (x), pitch (y), yaw (z), angles are in radians
    {
        // Abbreviations for the various angular functions

        float cr = (float) Math.cos(roll * 0.5);
        float sr = (float) Math.sin(roll * 0.5);
        float cp = (float) Math.cos(pitch * 0.5);
        float sp = (float) Math.sin(pitch * 0.5);
        float cy = (float) Math.cos(yaw * 0.5);
        float sy = (float) Math.sin(yaw * 0.5);

        q.w = cr * cp * cy + sr * sp * sy;
        q.x = sr * cp * cy - cr * sp * sy;
        q.y = cr * sp * cy + sr * cp * sy;
        q.z = cr * cp * sy - sr * sp * cy;
    }

    public void rotate(Quaternionf rotation, float x, float y, float z) {
        //Use modulus to fix values to below 360 then convert values to radians
        float newX = (float) Math.toRadians(x);
        float newY = (float) Math.toRadians(y);
        float newZ = (float) Math.toRadians(z);

        //Create a quaternion with the delta rotation values
        Quaternionf rotationDelta = new Quaternionf();
        rotationDelta.rotationXYZ(newX, newY, newZ);

        //Calculate the inverse of the delta quaternion
        Quaternionf conjugate = rotationDelta.conjugate();

        //Multiply this transform by the rotation delta quaternion and its inverse
        rotation.mul(rotationDelta).mul(conjugate);
    }

}
