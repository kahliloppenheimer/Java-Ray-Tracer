package me.kahlil.graphics;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import me.kahlil.scene.Scene3D;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * Created by kahliloppenheimer on 9/2/15.
 */
public class RayTracerCoordinator {

  private final ExecutorService executor;

  private SimpleFrame3D frame;
  private Camera3D camera;
  private Scene3D scene;

  RayTracerCoordinator(SimpleFrame3D frame, Camera3D camera, Scene3D scene) {
    this.frame = frame;
    this.camera = camera;
    this.scene = scene;
    this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  }

  SimpleFrame3D render(boolean shadowsEnabled, int numThreads)
      throws InterruptedException, ExecutionException {

    RayTracer rayTracer = new SimpleAntiAliaser(
        camera,
        frame,
        new BasicTracer(scene, camera, frame, shadowsEnabled),
        new RandomAntiAliasingMethod(),
        16);

    // Construct individual worker threads
    ImmutableList<RayTracerWorker> rayTracerWorkers = IntStream.range(0, numThreads)
        .mapToObj(i ->  new RayTracerWorker(
            rayTracer,
            frame,
            camera,
            scene,
            shadowsEnabled,
            i,
            numThreads))
        .collect(toImmutableList());

    // Start all workers
    ImmutableList<Future<?>> futures = rayTracerWorkers.stream()
        .map(executor::submit)
        .collect(toImmutableList());

    // Wait for all workers to finish
    for (Future<?> future : futures) {
      future.get();
    }

    int totalNumTraces = rayTracerWorkers.stream()
        .mapToInt(RayTracerWorker::getNumTraces)
        .sum();

    System.out.printf("Total number of rays traced = %d\n", totalNumTraces);

    return frame;
  }

}