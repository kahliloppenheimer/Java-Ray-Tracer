package me.kahlil.scene;

import java.util.Optional;

/**
 * Created by edenzik on 9/3/15.
 * If dn > 0
 */
public class Plane3D extends Object3D {
    private final Vector normal;
    private final Vector point;
    private final Material front;

    public Plane3D(Vector point, Vector normal, Material front) {
        this.normal = normal;
        this.point = point;
        this.front = front;
    }

    @Override
    public Optional<RayHit> untransformedIntersection(Ray3D r) {
        double dn = (r.getDirection()).dot(normal);
        double t = (point.subtract(r.getStart())).dot(normal) / dn;
        if(dn == 0.0 || t <= 0) {
            return Optional.empty();
        } else {
            Vector normal = r.getDirection().dot(this.normal) < 0.0 ? this.normal.scale(1) : this.normal.scale(-1);
            return Optional.of(new RayHit(r, t, r.atTime(t).subtract(r.getStart()).magnitude(), r.atTime(t), normal, this));
        }
    }

    @Override
    public Material getInsideMaterial() {
        return front;
    }

    @Override
    public Material getOutsideMaterial() {
        return front;
    }
}
