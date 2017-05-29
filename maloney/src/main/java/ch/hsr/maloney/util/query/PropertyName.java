package ch.hsr.maloney.util.query;

import java.util.Arrays;

/**
 * Created by roman on 29.05.17.
 */
public enum PropertyName {
    FileId("fileId"),
    FileName("fileName"),
    FilePath("filePath"),
    DateAccessed("dateAccessed"),
    DateChanged("dateChanged"),
    DateCreated("dateCreated"),
    Artifacts("artifacts");

    private String fieldName;

    PropertyName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static PropertyName getByFieldName(String fieldName){
        return Arrays.stream(values())
                .filter(v -> v.getFieldName().equalsIgnoreCase(fieldName))
                .findFirst().orElse(null);
    }
}
