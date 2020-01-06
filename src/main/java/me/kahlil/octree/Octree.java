package me.kahlil.octree;

import static me.kahlil.octree.BoundsHelper.computeGlobalMinAndMax;

import com.google.common.annotations.VisibleForTesting;
import me.kahlil.geometry.BoundingVolume;
import me.kahlil.geometry.Extents;
import me.kahlil.geometry.Polygon;
import me.kahlil.geometry.Ray;
import me.kahlil.geometry.Vector;

/**
 * Implementation of an Octree.
 */
public class Octree<T extends Polygon> implements BoundingVolume {

  @VisibleForTesting
  final OctreeNode<T> root;
  final int maxObjectsPerLeaf;
  final int maxDepth;
  final Extents extents;

  public Octree(
      T[] shapes,
      int maxObjectsPerLeaf,
      int maxDepth) {
    this.maxObjectsPerLeaf = maxObjectsPerLeaf;
    this.maxDepth = maxDepth;
    Vector[] minAndMax = computeGlobalMinAndMax(shapes);
    this.root = new OctreeNode<>(shapes, maxObjectsPerLeaf, maxDepth, minAndMax[0], minAndMax[1], 0);

    for (int i = 0; i < shapes.length; i++) {
      root.insert(i);
    }

    this.extents = root.computeExtents();
  }

  @Override
  public boolean intersectsWith(Ray ray) {
    return root.intersectsWith(ray);
  }
}
