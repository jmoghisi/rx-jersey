package net.winterly.rxjersey.server;

import org.glassfish.hk2.api.Rank;
import org.glassfish.jersey.message.MessageBodyWorkers;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Generic {@link MessageBodyWriter} that overrides writer to serialise incoming entity as type of generic class. <br>
 * This class will only redirect writing to another {@link MessageBodyWriter} <br>
 * Requires list of supported types
 */
@Rank(1)
public abstract class RxGenericBodyWriter implements MessageBodyWriter<Object> {

    @Inject
    private Provider<MessageBodyWorkers> workers;

    private final List<Class> allowedTypes;

    protected RxGenericBodyWriter(Class... allowedTypes) {
        this.allowedTypes = Arrays.asList(allowedTypes);
    }

    private static Class raw(Type genericType) {
        final ParameterizedType parameterizedType = (ParameterizedType) genericType;
        return (Class) parameterizedType.getRawType();
    }

    private static Type actual(Type genericType) {
        final ParameterizedType actualGenericType = (ParameterizedType) genericType;
        return actualGenericType.getActualTypeArguments()[0];
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        Class rawType = raw(genericType);
        return allowedTypes.contains(rawType);
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0; //skip
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeTo(Object entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        final Type actualTypeArgument = actual(genericType);
        final MessageBodyWriter writer = workers.get().getMessageBodyWriter(entity.getClass(), actualTypeArgument, annotations, mediaType);

        writer.writeTo(entity, entity.getClass(), actualTypeArgument, annotations, mediaType, httpHeaders, entityStream);
    }
}