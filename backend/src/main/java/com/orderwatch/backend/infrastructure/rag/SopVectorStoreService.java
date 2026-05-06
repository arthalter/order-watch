package com.orderwatch.backend.infrastructure.rag;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orderwatch.backend.infrastructure.milvus.MilvusProperties;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.UpsertResp;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SopVectorStoreService {

    private final MilvusProperties milvusProperties;

    public SopVectorStoreService(MilvusProperties milvusProperties) {
        this.milvusProperties = milvusProperties;
    }

    public long upsert(List<SopChunkVector> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return 0;
        }

        MilvusClientV2 client = openClient();
        try {
            UpsertReq request = UpsertReq.builder()
                    .collectionName(milvusProperties.getCollectionName())
                    .data(chunks.stream().map(this::toJson).toList())
                    .build();

            UpsertResp response = client.upsert(request);
            return response.getUpsertCnt();
        } finally {
            client.close();
        }
    }

    public List<SopSearchMatch> search(float[] queryVector, int topK) {
        if (queryVector == null || queryVector.length == 0) {
            throw new IllegalArgumentException("queryVector is required");
        }
        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be positive");
        }

        MilvusClientV2 client = openClient();
        try {
            String collectionName = milvusProperties.getCollectionName();
            client.loadCollection(LoadCollectionReq.builder()
                    .collectionName(collectionName)
                    .async(false)
                    .build());

            SearchReq request = SearchReq.builder()
                    .collectionName(collectionName)
                    .annsField(SopCollectionService.VECTOR_FIELD)
                    .metricType(IndexParam.MetricType.COSINE)
                    .topK(topK)
                    .data(List.of(new FloatVec(queryVector)))
                    .outputFields(List.of(
                            SopCollectionService.CONTENT_FIELD,
                            SopCollectionService.FILE_NAME_FIELD,
                            SopCollectionService.CHUNK_INDEX_FIELD,
                            SopCollectionService.TITLE_FIELD
                    ))
                    .build();

            SearchResp response = client.search(request);
            if (response.getSearchResults() == null || response.getSearchResults().isEmpty()) {
                return List.of();
            }

            return response.getSearchResults().get(0).stream()
                    .map(this::toSearchMatch)
                    .toList();
        } finally {
            client.close();
        }
    }

    private MilvusClientV2 openClient() {
        String uri = "http://" + milvusProperties.getHost() + ":" + milvusProperties.getPort();
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(uri)
                .build();
        return new MilvusClientV2(connectConfig);
    }

    private JsonObject toJson(SopChunkVector chunk) {
        JsonObject row = new JsonObject();
        row.addProperty(SopCollectionService.ID_FIELD, chunk.id());
        row.addProperty(SopCollectionService.CONTENT_FIELD, chunk.content());
        row.add(SopCollectionService.VECTOR_FIELD, vectorToJson(chunk.vector()));
        row.addProperty(SopCollectionService.FILE_NAME_FIELD, chunk.fileName());
        row.addProperty(SopCollectionService.CHUNK_INDEX_FIELD, chunk.chunkIndex());
        row.addProperty(SopCollectionService.TITLE_FIELD, chunk.title());
        return row;
    }

    private SopSearchMatch toSearchMatch(SearchResp.SearchResult result) {
        Map<String, Object> entity = result.getEntity();
        return new SopSearchMatch(
                stringValue(entity, SopCollectionService.FILE_NAME_FIELD),
                intValue(entity, SopCollectionService.CHUNK_INDEX_FIELD),
                result.getScore() == null ? 0.0 : result.getScore(),
                stringValue(entity, SopCollectionService.CONTENT_FIELD),
                stringValue(entity, SopCollectionService.TITLE_FIELD)
        );
    }

    private static String stringValue(Map<String, Object> entity, String fieldName) {
        if (entity == null || entity.get(fieldName) == null) {
            return "";
        }
        return String.valueOf(entity.get(fieldName));
    }

    private static int intValue(Map<String, Object> entity, String fieldName) {
        if (entity == null || entity.get(fieldName) == null) {
            return 0;
        }

        Object value = entity.get(fieldName);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private static JsonArray vectorToJson(float[] vector) {
        JsonArray array = new JsonArray();
        if (vector == null) {
            return array;
        }
        for (float value : vector) {
            array.add(value);
        }
        return array;
    }

    public record SopChunkVector(
            String id,
            String content,
            float[] vector,
            String fileName,
            int chunkIndex,
            String title
    ) {
    }

    public record SopSearchMatch(
            String fileName,
            int chunkIndex,
            double score,
            String content,
            String title
    ) {
    }
}
