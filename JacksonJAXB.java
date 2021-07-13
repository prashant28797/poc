package com.poc.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class JacksonJAXB {
	/**This object mapper is setup to produce extra data in the json specifying each.
     * Object type.
     */
    public final static ObjectMapper objectMapperForAbstractTypes = new ObjectMapper();
    /**
     * This object mapper doesn't produce the extra data types. It is necessary to
     * have both to handle json data where its is not known if we have the extra
     * data or not.
     * </p>
     */
    public final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapperForAbstractTypes.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);

        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        objectMapperForAbstractTypes.setAnnotationIntrospectors(introspector, introspector);

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setAnnotationIntrospectors(introspector, introspector)
                .enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME).registerModule(new JodaModule())
                .registerModule(new SimpleModule().addSerializer(DateTime.class,
                        new DateTimeSerializer(new JacksonJodaDateFormat(
                                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZoneUTC()))));
    }

    /**
     * fromJSON.
     * 
     * @param json
     *            , not null.
     * @param clazz
     *            , not null.
     * @return , not null.
     * @throws Exception
     */
    public static <T> T fromJSON(final String json, final Class<T> clazz) throws Exception {
        try {
            return objectMapperForAbstractTypes.readValue(json, clazz);
        } catch (JsonMappingException e) {
            return objectMapper.readValue(json, clazz);
        }
    }

    /**
     * fromJSON.
     * 
     * @param json
     *            , not null.
     * @param typeReference
     *            , not null.
     * @return , not null.
     * @throws Exception
     */
    public static <T> T fromJSON(final String json, final TypeReference<T> typeReference) throws Exception {
        try {
            return objectMapperForAbstractTypes.readValue(json, typeReference);
        } catch (JsonMappingException e) {
            return objectMapper.readValue(json, typeReference);
        }
    }

    /**
     * Converts an object to a json String.
     * <p>
     * toJSON() uses the objectMapperForAbstractTypes which produces extra data
     * specifying each Object type. This is useful for when passing abstract types
     * around in json.
     * </p>
     *
     * @param value
     * @return
     * @throws Exception
     * @throws IOException
     */
    public static String toJSON_MapperForAbstractTypes(final Object value) throws Exception, IOException {
        return objectMapperForAbstractTypes.writeValueAsString(value);
    }

    /**
     * Converts an object to a json String.
     * <p>
     * toJSON() uses the objectMapperForAbstractTypes which produces extra data
     * specifying each Object type. This is useful for when passing abstract types
     * around in json.
     * </p>
     *
     * @param value
     * @return
     * @throws Exception
     * @throws IOException
     */
    public static String toJSON_Basic(final Object value) throws Exception, IOException {
        return objectMapper.writeValueAsString(value);
    }

    /**
     * Converts an object to a XML String. Object is expected to be annotated with
     * {@link javax.xml.bind.annotation.XmlRootElement} annotation.
     *
     * @param object
     *            object to convert to XML.
     * @param clazz
     *            class of object.
     * @param <T>
     *            type of object.
     * @return XML representation of object. In case of errors during the
     *         conversion, {@link IllegalArgumentException} is thrown.
     */
    public static <T> String toXML(final T object, final Class<T> clazz) {
        StringWriter stringWriter = new StringWriter();
        try {
            Marshaller marshaller = JAXBContext.newInstance(clazz).createMarshaller();
            marshaller.setProperty("jaxb.encoding", "Unicode");
            marshaller.marshal(object, stringWriter);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e); // Checked exception isn't commonly handled for such operations.
        }
        return stringWriter.toString();
    }

    /**
     * Converts XML String to an object.
     *
     * @param xml
     *            xml string to convert.
     * @param clazz
     *            class of object.
     * @param <T>
     *            type of object.
     * @return Object representation of given XML. In case of errors during the
     *         conversion, {@link IllegalArgumentException} is thrown.
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXML(final String xml, final Class<T> clazz) {
        try {
            return (T) JAXBContext.newInstance(clazz).createUnmarshaller().unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e); // Checked exception isn't commonly handled for such operations.
        }
    }
}
