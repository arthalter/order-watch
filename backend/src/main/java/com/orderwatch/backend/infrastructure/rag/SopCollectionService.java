package com.orderwatch.backend.infrastructure.rag;

import com.orderwatch.backend.infrastructure.milvus.MilvusProperties;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SopCollectionService {

    static final String ID_FIELD = "id";
    static final String CONTENT_FIELD = "content";
    static final String VECTOR_FIELD = "vector";
    static final String FILE_NAME_FIELD = "fileName";
    static final String CHUNK_INDEX_FIELD = "chunkIndex";
    static final String TITLE_FIELD = "title";

    private final MilvusProperties milvusProperties;

    public SopCollectionService(MilvusProperties milvusProperties) {
        this.milvusProperties = milvusProperties;
    }

    public InitResult initializeCollection() {
        validateProperties();

        MilvusClientV2 client = openClient();
        try {
            String collectionName = milvusProperties.getCollectionName();
            if (hasCollection(client, collectionName)) {
                return new InitResult(collectionName, true, false, milvusProperties.getVectorDimension());
            }

            CreateCollectionReq.CollectionSchema schema = client.createSchema();
            schema.addField(varcharField(ID_FIELD, 128, true));
            schema.addField(varcharField(CONTENT_FIELD, 4096, false));
            schema.addField(AddFieldReq.builder()
                    .fieldName(VECTOR_FIELD)
                    .description("SOP chunk embedding vector")
                    .dataType(DataType.FloatVector)
                    .dimension(milvusProperties.getVectorDimension())
                    .build());
            schema.addField(varcharField(FILE_NAME_FIELD, 512, false));
            schema.addField(AddFieldReq.builder()
                    .fieldName(CHUNK_INDEX_FIELD)
                    .description("Chunk index in source document")
                    .dataType(DataType.Int64)
                    .build());
            schema.addField(varcharField(TITLE_FIELD, 512, false));

            IndexParam vectorIndex = IndexParam.builder()
                    .fieldName(VECTOR_FIELD)
                    .metricType(IndexParam.MetricType.COSINE)
                    .build();

            CreateCollectionReq request = CreateCollectionReq.builder()
                    .collectionName(collectionName)
                    .description("OrderWatch SOP chunks for RAG")
                    .collectionSchema(schema)
                    .indexParams(List.of(vectorIndex))
                    .build();

            client.createCollection(request);
            return new InitResult(collectionName, true, true, milvusProperties.getVectorDimension());
        } finally {
            client.close();
        }
    }

    public boolean hasCollection() {
        validateProperties();

        MilvusClientV2 client = openClient();
        try {
            return hasCollection(client, milvusProperties.getCollectionName());
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

    private static boolean hasCollection(MilvusClientV2 client, String collectionName) {
        return client.hasCollection(HasCollectionReq.builder()
                .collectionName(collectionName)
                .build());
    }

    private static AddFieldReq varcharField(String name, int maxLength, boolean primaryKey) {
        return AddFieldReq.builder()
                .fieldName(name)
                .description(name)
                .dataType(DataType.VarChar)
                .maxLength(maxLength)
                .isPrimaryKey(primaryKey)
                .autoID(false)
                .build();
    }

    private void validateProperties() {
        if (milvusProperties.getHost() == null || milvusProperties.getHost().isBlank()) {
            throw new IllegalStateException("milvus.host is required");
        }
        if (milvusProperties.getPort() <= 0) {
            throw new IllegalStateException("milvus.port must be positive");
        }
        if (milvusProperties.getCollectionName() == null || milvusProperties.getCollectionName().isBlank()) {
            throw new IllegalStateException("milvus.collection-name is required");
        }
        if (milvusProperties.getVectorDimension() <= 0) {
            throw new IllegalStateException("milvus.vector-dimension must be positive");
        }
    }

    public record InitResult(
            String collectionName,
            boolean exists,
            boolean created,
            int vectorDimension
    ) {
    }
}
