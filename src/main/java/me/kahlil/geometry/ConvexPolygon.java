package me.kahlil.geometry;

import static com.google.common.base.Preconditions.checkArgument;
import static me.kahlil.config.Counters.NUM_TRIANGLES;
import static me.kahlil.config.Parameters.OCTREE_ENABLED;
import static me.kahlil.config.Parameters.OCTREE_MAX_DEPTH;
import static me.kahlil.config.Parameters.OCTREE_MAX_SHAPES_PER_LEAF;

import java.util.Arrays;
import java.util.Optional;
import me.kahlil.octree.BoundsHelper;
import me.kahlil.octree.Octree;
import me.kahlil.scene.Material;

/** Shape representing a convex polygon. */
public class ConvexPolygon extends Shape implements Polygon {

  private final Triangle[] triangles;
  private final Octree<Triangle> octree;

  // Min/max (x, y, z) that the ConvexPolygon occupies for forming a bounding volume.
  private final Vector minBound;
  private final Vector maxBound;

  private ConvexPolygon(
      Material material,
      Vector[] vertexes,
      Vector[] vertexNormals,
      int[] faces,
      int[] vertexIndexes) {
    checkArgument(
        vertexes.length >= 3,
        "A polygon must have at least 3 vertices. Found: %d",
        Arrays.toString(vertexes));
    checkArgument(faces.length > 0, "A convex polygon must have at least one face.");
    checkArgument(
        vertexIndexes.length > 0, "A convex polygon must have at least one vertex index.");

    this.triangles =
        convertVertexesToTriangles(material, vertexes, vertexNormals, faces, vertexIndexes);
    NUM_TRIANGLES.getAndAdd(triangles.length);

    Vector[] minMaxBounds = BoundsHelper.computeGlobalMinAndMax(triangles);
    this.minBound = minMaxBounds[0];
    this.maxBound = minMaxBounds[1];

    this.octree =
        new Octree(getTriangles(), OCTREE_MAX_SHAPES_PER_LEAF, OCTREE_MAX_DEPTH);
  }

  public static ConvexPolygon withSurfaceNormals(
      Material material, Vector[] vertexes, int[] faces, int[] vertexIndexes) {
    return new ConvexPolygon(material, vertexes, new Vector[] {}, faces, vertexIndexes);
  }

  public static ConvexPolygon withVertexNormals(
      Material material,
      Vector[] vertexes,
      Vector[] vertexNormals,
      int[] faces,
      int[] vertexIndexes) {
    checkArgument(
        vertexNormals.length == vertexes.length,
        "A polygon with vertex normals must have the same number of vertexes as normals. Instead, found vertexes=%s, normals=%s",
        vertexes,
        vertexNormals);
    return new ConvexPolygon(material, vertexes, vertexNormals, faces, vertexIndexes);
  }

  public static ConvexPolygon cube(Material material) {
    return ConvexPolygon.withSurfaceNormals(
        material,
        new Vector[] {
          new Vector(-1, -1, 0),
          new Vector(1, -1, 0),
          new Vector(1, 1, 0),
          new Vector(-1, 1, 0),
          new Vector(-1, -1, -1),
          new Vector(1, -1, -1),
          new Vector(1, 1, -1),
          new Vector(-1, 1, -1),
        },
        new int[] {4, 4, 4, 4, 4, 4},
        new int[] {
          0, 1, 2, 3, // front face
          3, 2, 6, 7, // top face
          0, 4, 5, 1, // bottom face
          0, 3, 7, 4, // left face
          1, 5, 6, 2, // right face
          4, 5, 6, 7 // back face
        });
  }

  @Override
  Optional<RayHit> internalIntersectInObjectSpace(Ray ray) {
    double minTime = Integer.MAX_VALUE;
    Optional<RayHit> closestHit = Optional.empty();
    if (OCTREE_ENABLED) {
      return octree.intersectWith(ray);
    }
    for (Triangle triangle : triangles) {
      Optional<RayHit> rayHit = triangle.intersectInObjectSpace(ray);
      if (rayHit.isPresent()) {
        double time = rayHit.get().getTime();
        if (time < minTime) {
          minTime = time;
          closestHit = rayHit;
        }
      }
    }
    return closestHit;
  }

  @Override
  public Triangle[] getTriangles() {
    return this.triangles;
  }

  @Override
  public Vector minBound() {
    return minBound;
  }

  @Override
  public Vector maxBound() {
    return maxBound;
  }

  /**
   * Converts the given set of vertices into triangles using the simple algorithm described at:
   * https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-polygon-mesh/polygon-to-triangle-mesh
   */
  private Triangle[] convertVertexesToTriangles(
      Material material,
      Vector[] vertexes,
      Vector[] vertexNormals,
      int[] faces,
      int[] vertexIndexes) {
    int vertexIndex = 0;
    int trianglesIndex = 0;
    int numTriangles = computeNumTriangles(faces);
    Triangle[] triangles = new Triangle[numTriangles];
    for (int faceIndex = 0; faceIndex < faces.length; faceIndex++) {
      convertFaceToTriangles(
          triangles,
          material,
          vertexes,
          vertexNormals,
          trianglesIndex,
          faces[faceIndex],
          vertexIndexes,
          vertexIndex);
      vertexIndex += faces[faceIndex];
      trianglesIndex += faces[faceIndex] - 2;
    }

    return triangles;
  }

  private static int computeNumTriangles(int[] faces) {
    int sum = 0;
    for (int i : faces) {
      checkArgument(i >= 3, "A face must have at least 3 vertices. Found: %d", i);
      sum += i - 2;
    }
    return sum;
  }

  /**
   * Converts the given geometric specification of a face (e.g. it's number of vertexes and the
   * vertex index array) into the triangles that can be drawn to represent it.
   *
   * <p>For efficiency, it mutates the triangle array input.
   */
  private void convertFaceToTriangles(
      Triangle[] triangles,
      Material material,
      Vector[] vertexes,
      Vector[] vertexNormals,
      int trianglesIndex,
      int numVertexes,
      int[] vertexIndexes,
      int startingVertexIndex) {
    for (int i = 0; i < numVertexes - 2; i++) {
      int firstVertexIndex = vertexIndexes[startingVertexIndex];
      int secondVertexIndex = vertexIndexes[startingVertexIndex + i + 1];
      int thirdVertexIndex = vertexIndexes[startingVertexIndex + i + 2];
      triangles[trianglesIndex + i] =
          constructTriangle(
              material,
              vertexes,
              vertexNormals,
              firstVertexIndex,
              secondVertexIndex,
              thirdVertexIndex);
    }
  }

  /**
   * Constructs a triangle with the given parameters, determining to use vertex normals or surface
   * normals based on the size of the {@code vertexNormals} array.
   */
  private Triangle constructTriangle(
      Material material,
      Vector[] vertexes,
      Vector[] vertexNormals,
      int firstVertexIndex,
      int secondVertexIndex,
      int thirdVertexIndex) {
    Triangle triangle;
    Vector[] triangleVertexes = {
      vertexes[firstVertexIndex], vertexes[secondVertexIndex], vertexes[thirdVertexIndex]
    };
    if (vertexNormals.length == 0) {
      triangle = Triangle.withSurfaceNormals(material, triangleVertexes);
    } else {
      triangle =
          Triangle.withVertexNormals(
              material,
              triangleVertexes,
              new Vector[] {
                vertexNormals[firstVertexIndex],
                vertexNormals[secondVertexIndex],
                vertexNormals[thirdVertexIndex]
              });
    }
    return triangle;
  }
}
