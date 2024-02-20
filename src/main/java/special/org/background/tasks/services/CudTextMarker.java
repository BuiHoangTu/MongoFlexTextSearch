package special.org.background.tasks.services;

import lombok.NonNull;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import special.org.configs.subconfig.WatchingCollectionConfig;
import special.org.endpoints.search.fulltext.TextSearchRepo;
import special.org.models.TextIndexMap;
import special.org.models.TextMarker;

import java.util.Arrays;
import java.util.Optional;

@Service
public class CudTextMarker {
    private final IdService idService;
    private final TextSearchRepo textRepo;

    @Autowired
    public CudTextMarker(IdService idService, TextSearchRepo textRepo) {
        this.idService = idService;
        this.textRepo = textRepo;
    }

    public void createDocument(@NonNull Document originalDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        this._upsertDocument(originalDocument, dbName, collectionConfig, false);
    }

    public void upsertDocument(@NonNull Document originalDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        this._upsertDocument(originalDocument, dbName, collectionConfig, true);
    }

    public void deleteDocument(@NonNull Document originalDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        String refId = idService.getId(originalDocument, collectionConfig.getIdName());
        textRepo.deleteTextMarkerByDbNameAndCollectionNameAndRefId(dbName, collectionConfig.getName(), refId);
    }

    private void _upsertDocument(
            @NonNull Document originalDocument,
            String dbName,
            WatchingCollectionConfig collectionConfig,
            boolean mayExist
    ) {
        String refId = idService.getId(originalDocument, collectionConfig.getIdName());
        TextIndexMap textIndexMap = new TextIndexMap();
        for (String key : collectionConfig.getTextFields()) {
            try {
                String[] keyAsParts = key.split("\\.");
                String lastKey = keyAsParts[keyAsParts.length - 1];

                var middleKeys = Arrays.stream(keyAsParts).limit(keyAsParts.length - 1).toList();

                Document currentDocument = originalDocument;
                for (var part : middleKeys) {
                    currentDocument = (Document) currentDocument.get(part);
                }

                textIndexMap.put(key, currentDocument.getString(lastKey));
            } catch (Exception e) {
                textIndexMap.put(key, "");
            }
        }

        Optional<TextMarker> existing;
        if (!mayExist) existing = Optional.empty();
        else existing = textRepo.findByDbNameAndCollectionNameAndRefId(dbName, collectionConfig.getName(), refId);

        if (existing.isPresent()) {
            var updating = existing.get();
            updating.setTextIndexes(textIndexMap);
            textRepo.save(updating);
        } else {
            TextMarker textMarker = new TextMarker();
            textMarker.setDbName(dbName);
            textMarker.setCollectionName(collectionConfig.getName());
            textMarker.setRefId(refId);
            textMarker.setTextIndexes(textIndexMap);

            try {
                textRepo.insert(textMarker);
            } catch (DuplicateKeyException ignored) {

            }
        }
    }

}
