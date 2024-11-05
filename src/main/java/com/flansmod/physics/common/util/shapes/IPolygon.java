package com.flansmod.physics.common.util.shapes;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;

public interface IPolygon
{
    @Nonnull List<Vec3> getVertices();
    @Nonnull IPolygon clip(@Nonnull IPlane clipPlane);
    @Nonnull IPolygon cullClip(@Nonnull IPlane clipPlane);

    default int GetNumVertices() { return getVertices().size(); }
    @Nonnull
    default Vec3 getVertex(int index)
    {
        if(0 <= index && index < getVertices().size())
            return getVertices().get(index);
        return Vec3.ZERO;
    }
    @Nonnull
    default Vec3 getVertexLooped(int index)
    {
        if(getVertices().isEmpty())
            return Vec3.ZERO;

        index = Maths.modulo(index, getVertices().size());
        return getVertices().get(index);
    }
    @Nonnull
    default Vec3 getEdgeVector(int edgeIndex)
    {
        Vec3 v1 = getVertexLooped(edgeIndex);
        Vec3 v2 = getVertexLooped(edgeIndex + 1);
        return v2.subtract(v1);
    }
    @Nonnull
    default Vec3 getAveragePos()
    {
        int numVerts = getVertices().size();
        if(numVerts == 0)
            return Vec3.ZERO;

        Vec3 pos = Vec3.ZERO;

        for(int i = 0; i < numVerts; i++)
            pos = pos.add(getVertices().get(i));
        return pos.scale(1d / numVerts);
    }
    default double getArea()
    {
        int numVerts = getVertices().size();
        if(numVerts <= 2)
            return 0d;

        double totalArea = 0d;
        Vec3 v0 = getVertex(0);
        for(int i = 2; i < numVerts; i++)
        {
            Vec3 v1 = getVertex(i-1).subtract(v0);
            Vec3 v2 = getVertex(i).subtract(v0);
            totalArea += Maths.abs(v1.dot(v2)) * 0.5d;
        }
        return totalArea;
    }

    @Nonnull
    default IPolygon clip(@Nonnull List<IPlane> clipPlanes)
    {
        IPolygon clipResult = this;
        for(IPlane clipPlane : clipPlanes)
            clipResult = clipResult.clip(clipPlane);
        return clipResult;
    }
    @Nonnull
    default IPlane getEdgeClipPlane(@Nonnull Vec3 refNormal, int edgeIndex)
    {
        Vec3 edge = getEdgeVector(edgeIndex);
        refNormal = getEdgeVector(edgeIndex + 1).normalize();
        Vec3 clipNormal = refNormal.cross(refNormal.cross(edge.normalize()));
        double dist = getVertexLooped(edgeIndex).dot(clipNormal);
        return Plane.of(clipNormal, dist);
    }
    @Nonnull
    default IPlane getFaceClipPlane()
    {
        if(GetNumVertices() < 3)
        {
            FlansPhysicsMod.LOGGER.warn("Tried to get clip plane of a polygon with fewer than 3 verts");
            return Plane.of(Vec3.ZERO, 0d);
        }
        Vec3 v1 = getVertexLooped(0);
        Vec3 v2 = getVertexLooped(1);
        Vec3 v3 = getVertexLooped(2);
        Vec3 normal = v2.subtract(v1).cross(v3.subtract(v1)).normalize();
        double dist = v1.dot(normal);
        return Plane.of(normal, dist);
    }

}
