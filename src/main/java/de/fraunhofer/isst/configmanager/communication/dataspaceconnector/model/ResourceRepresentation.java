package de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

/**
 * <p>ResourceRepresentation class.</p>
 *
 * @author Julia Pampus
 * @version $Id: $Id
 */
@Schema(
        name = "ResourceRepresentation",
        description = "Representation of a resource",
        oneOf = ResourceRepresentation.class,
        example = "{\n" +
                "      \"type\": \"json\",\n" +
                "      \"byteSize\": 105,\n" +
                "      \"source\": {\n" +
                "        \"type\":\"http-get\", \n"+
                "        \"url\": \"https://samples.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=439d4b804bc8187953eb36d2a8c26a02\",\n" +
                "        \"username\": \"-\",\n" +
                "        \"password\": \"-\",\n" +
                "        \"system\": \"Open Weather Map API\"\n" +
                "      }\n" +
                "    }"
)
public class ResourceRepresentation implements Serializable {

    @Id
    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("type")
    private String type;

    @JsonProperty("byteSize")
    private Integer byteSize;

    @JsonProperty("source")
    @Column(columnDefinition = "BLOB")
    private BackendSource source;

    /**
     * <p>Constructor for ResourceRepresentation.</p>
     */
    public ResourceRepresentation() {
    }

    /**
     * <p>Constructor for ResourceRepresentation.</p>
     *
     * @param uuid       a {@link java.util.UUID} object.
     * @param type       a {@link java.lang.String} object.
     * @param byteSize   a {@link java.lang.Integer} object.
     * @param source     a {@link de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.BackendSource} object.
     */
    public ResourceRepresentation(UUID uuid, String type, Integer byteSize,BackendSource source) {
        this.uuid = uuid;
        this.type = type;
        this.byteSize = byteSize;
        this.source = source;
    }

    /**
     * <p>Getter for the field <code>uuid</code>.</p>
     *
     * @return a {@link java.util.UUID} object.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * <p>Setter for the field <code>uuid</code>.</p>
     *
     * @param uuid a {@link java.util.UUID} object.
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return type;
    }

    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * <p>Getter for the field <code>byteSize</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getByteSize() {
        return byteSize;
    }

    /**
     * <p>Setter for the field <code>byteSize</code>.</p>
     *
     * @param byteSize a {@link java.lang.Integer} object.
     */
    public void setByteSize(Integer byteSize) {
        this.byteSize = byteSize;
    }

    /**
     * <p>Getter for the field <code>source</code>.</p>
     *
     * @return a {@link de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.BackendSource} object.
     */
    public BackendSource getSource() {
        return source;
    }

    /**
     * <p>Setter for the field <code>source</code>.</p>
     *
     * @param source a {@link de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.BackendSource} object.
     */
    public void setSource(BackendSource source) {
        this.source = source;
    }
}
