# MQ JSON vs Protobuf Benchmark

Sample size: 100 upload events

| Metric | JSON | Protobuf | Delta (Protobuf vs JSON) |
|---|---:|---:|---:|
| Avg serialization time (us) | 44.922 | 22.327 | -50.30% |
| Avg deserialization time (us) | 64.035 | 39.525 | -38.27% |
| Avg message size (bytes) | 496.9 | 337.0 | -32.18% |

Notes:
- Positive delta means Protobuf is higher (worse for time/size).
- Negative delta means Protobuf is lower (better for time/size).
- Benchmark performed in-process on the same `UploadEvent` payload set.
