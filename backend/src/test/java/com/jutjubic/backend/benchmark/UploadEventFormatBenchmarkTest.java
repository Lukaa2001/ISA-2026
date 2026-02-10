package com.jutjubic.backend.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jutjubic.backend.mq.UploadEventMapper;
import com.jutjubic.backend.mq.UploadEventMessage;
import com.jutjubic.backend.proto.UploadEventProto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UploadEventFormatBenchmarkTest {
  private static final int WARMUP_COUNT = 20;
  private static final int SAMPLE_COUNT = 100;

  @Test
  void compareJsonVsProtobufAndWriteReport() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    List<UploadEventMessage> warmupEvents = generateEvents(WARMUP_COUNT, 12345L);
    runBenchmark(mapper, warmupEvents);

    List<UploadEventMessage> events = generateEvents(SAMPLE_COUNT, 42L);
    BenchmarkResult result = runBenchmark(mapper, events);

    assertTrue(result.sampleCount >= 50, "Benchmark must run on at least 50 events");

    String report = toMarkdownReport(result);
    Path reportPath = Path.of("benchmark-results", "mq-json-vs-protobuf.md");
    Files.createDirectories(reportPath.getParent());
    Files.writeString(
        reportPath,
        report,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    );

    System.out.println(report);
  }

  private BenchmarkResult runBenchmark(ObjectMapper mapper, List<UploadEventMessage> events) {
    long jsonSerNs = 0;
    long jsonDeserNs = 0;
    long protobufSerNs = 0;
    long protobufDeserNs = 0;
    long jsonSizeBytes = 0;
    long protobufSizeBytes = 0;

    for (UploadEventMessage event : events) {
      long start = System.nanoTime();
      byte[] jsonBytes;
      try {
        jsonBytes = mapper.writeValueAsBytes(event);
      } catch (Exception ex) {
        throw new RuntimeException("JSON serialization failed", ex);
      }
      jsonSerNs += System.nanoTime() - start;
      jsonSizeBytes += jsonBytes.length;

      start = System.nanoTime();
      try {
        mapper.readValue(jsonBytes, UploadEventMessage.class);
      } catch (Exception ex) {
        throw new RuntimeException("JSON deserialization failed", ex);
      }
      jsonDeserNs += System.nanoTime() - start;

      start = System.nanoTime();
      byte[] protobufBytes = UploadEventMapper.toProto(event).toByteArray();
      protobufSerNs += System.nanoTime() - start;
      protobufSizeBytes += protobufBytes.length;

      start = System.nanoTime();
      try {
        UploadEventProto.UploadEvent parsed = UploadEventProto.UploadEvent.parseFrom(protobufBytes);
        UploadEventMapper.fromProto(parsed);
      } catch (Exception ex) {
        throw new RuntimeException("Protobuf deserialization failed", ex);
      }
      protobufDeserNs += System.nanoTime() - start;
    }

    int count = events.size();
    return new BenchmarkResult(
        count,
        nsToMicros(jsonSerNs / (double) count),
        nsToMicros(jsonDeserNs / (double) count),
        jsonSizeBytes / (double) count,
        nsToMicros(protobufSerNs / (double) count),
        nsToMicros(protobufDeserNs / (double) count),
        protobufSizeBytes / (double) count
    );
  }

  private List<UploadEventMessage> generateEvents(int count, long seed) {
    Random random = new Random(seed);
    List<UploadEventMessage> events = new ArrayList<>(count);

    for (int i = 0; i < count; i++) {
      long videoId = i + 1L;
      long authorId = 1L + random.nextInt(5000);
      long videoSize = 10_000_000L + random.nextInt(250_000_000);
      long thumbnailSize = 10_000L + random.nextInt(4_000_000);
      String title = "Video " + videoId + " - " + randomAlphaNumeric(random, 16);
      String description = "Generated benchmark description " + randomAlphaNumeric(random, 120);

      List<String> tags = new ArrayList<>();
      int tagCount = 2 + random.nextInt(6);
      for (int t = 0; t < tagCount; t++) {
        tags.add("tag-" + randomAlphaNumeric(random, 6));
      }

      events.add(new UploadEventMessage(
          videoId,
          title,
          videoSize,
          thumbnailSize,
          authorId,
          "user_" + authorId,
          description,
          tags,
          "videos/video-" + videoId + ".mp4",
          "thumbnails/video-" + videoId + ".jpg",
          OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(i)
      ));
    }

    return events;
  }

  private String randomAlphaNumeric(Random random, int length) {
    String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
    }
    return sb.toString();
  }

  private double nsToMicros(double ns) {
    return ns / 1_000.0;
  }

  private String toMarkdownReport(BenchmarkResult r) {
    double serDeltaPct = percentageDelta(r.protobufSerializationMicros, r.jsonSerializationMicros);
    double deserDeltaPct = percentageDelta(r.protobufDeserializationMicros, r.jsonDeserializationMicros);
    double sizeDeltaPct = percentageDelta(r.protobufMessageSizeBytes, r.jsonMessageSizeBytes);

    return """
        # MQ JSON vs Protobuf Benchmark

        Sample size: %d upload events

        | Metric | JSON | Protobuf | Delta (Protobuf vs JSON) |
        |---|---:|---:|---:|
        | Avg serialization time (us) | %.3f | %.3f | %.2f%% |
        | Avg deserialization time (us) | %.3f | %.3f | %.2f%% |
        | Avg message size (bytes) | %.1f | %.1f | %.2f%% |

        Notes:
        - Positive delta means Protobuf is higher (worse for time/size).
        - Negative delta means Protobuf is lower (better for time/size).
        - Benchmark performed in-process on the same `UploadEvent` payload set.
        """.formatted(
        r.sampleCount,
        r.jsonSerializationMicros,
        r.protobufSerializationMicros,
        serDeltaPct,
        r.jsonDeserializationMicros,
        r.protobufDeserializationMicros,
        deserDeltaPct,
        r.jsonMessageSizeBytes,
        r.protobufMessageSizeBytes,
        sizeDeltaPct
    );
  }

  private double percentageDelta(double value, double baseline) {
    if (baseline == 0) {
      return 0;
    }
    return ((value - baseline) / baseline) * 100.0;
  }

  private record BenchmarkResult(
      int sampleCount,
      double jsonSerializationMicros,
      double jsonDeserializationMicros,
      double jsonMessageSizeBytes,
      double protobufSerializationMicros,
      double protobufDeserializationMicros,
      double protobufMessageSizeBytes
  ) {}
}
