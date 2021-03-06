/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spectator.nflx;

import com.netflix.config.ConfigurationManager;
import com.netflix.spectator.api.ExtendedRegistry;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Spectator;
import com.netflix.spectator.api.SystemConfigMap;
import com.netflix.spectator.gc.GcEvent;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.GcInfo;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

@RunWith(JUnit4.class)
public class ChronosGcEventListenerTest {

  private static final String client = "chronos_gc";
  private static final int retries = 5;

  private static HttpServer server;
  private static int port;

  private static AtomicInteger statusCode = new AtomicInteger(200);
  private static AtomicIntegerArray statusCounts = new AtomicIntegerArray(600);

  private static void set(String k, String v) {
    ConfigurationManager.getConfigInstance().setProperty(k, v);
  }

  @BeforeClass
  public static void startServer() throws Exception {
    server = HttpServer.create(new InetSocketAddress(0), 100);
    server.setExecutor(Executors.newFixedThreadPool(10, new ThreadFactory() {
      @Override public Thread newThread(Runnable r) {
        return new Thread(r, "HttpServer");
      }
    }));
    port = server.getAddress().getPort();

    server.createContext("/api/v2/event", new HttpHandler() {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
        statusCounts.incrementAndGet(statusCode.get());
        exchange.sendResponseHeaders(statusCode.get(), -1L);
        exchange.close();
      }
    });

    server.start();

    String uri = "niws://chronos_gc/http://localhost:" + port + "/api/v2/event";
    set("spectator.gc.chronosUri", uri);
    set(client + ".niws.client.MaxAutoRetriesNextServer", "" + retries);
    set(client + ".niws.client.RetryDelay", "10");
  }

  @AfterClass
  public static void stopServer() {
    server.stop(0);
  }

  private long reqCount(int status) {
    ExtendedRegistry r = Spectator.registry();
    Id requests = r.createId("spectator.gc.chronosPost", "status", "" + status);
    return r.timer(requests).count();
  }

  private long reqCount(String status) {
    ExtendedRegistry r = Spectator.registry();
    Id requests = r.createId("spectator.gc.chronosPost", "status", status);
    return r.timer(requests).count();
  }

  private GcEvent newGcEvent() {
    GcInfo gcInfo = null;
    for (java.lang.management.GarbageCollectorMXBean mbean : ManagementFactory.getGarbageCollectorMXBeans()) {
      GarbageCollectorMXBean sunMbean = (GarbageCollectorMXBean) mbean;
      while (sunMbean.getLastGcInfo() == null) {
        System.gc();
      }
      gcInfo = sunMbean.getLastGcInfo();
    }
    final GarbageCollectionNotificationInfo info = new GarbageCollectionNotificationInfo(
        "test", "action", "cause", gcInfo);
    return new GcEvent(info, 1234567890L);
  }

  @Test
  public void ok() throws Exception {
    statusCode.set(200);
    long before = reqCount(200);

    final ChronosGcEventListener listener = new ChronosGcEventListener();
    listener.onComplete(newGcEvent(), true);
    listener.shutdown();

    Assert.assertEquals(before + 1, reqCount(200));
  }

  private void errorTest(int status, int attempts) throws Exception {
    errorTest(status, attempts, "" + status);
  }

  private void errorTest(int status, int attempts, String statusStr) throws Exception {
    statusCode.set(status);
    long before2xx = reqCount(200);
    long beforeError = reqCount(statusStr);
    int errorCount = statusCounts.get(status);

    final ChronosGcEventListener listener = new ChronosGcEventListener();
    listener.onComplete(newGcEvent(), true);
    listener.shutdown();

    Assert.assertEquals(errorCount + attempts, statusCounts.get(status));
    Assert.assertEquals(before2xx, reqCount(200));
    Assert.assertEquals(beforeError + 1, reqCount(statusStr));
  }

  @Test
  public void clientError() throws Exception {
    errorTest(400, 1);
  }

  @Test
  public void serverError() throws Exception {
    errorTest(500, retries + 1);
  }

  @Test
  public void serverThrottle429() throws Exception {
    errorTest(429, retries + 1);
  }

  @Test
  public void serverThrottle503() throws Exception {
    errorTest(503, retries + 1);
  }
}

