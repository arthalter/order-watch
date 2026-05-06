package com.orderwatch.backend.infrastructure.rag;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orderwatch.backend.infrastructure.milvus.MilvusProperties;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.UpsertResp;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
