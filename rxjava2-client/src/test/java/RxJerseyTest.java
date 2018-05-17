import net.winterly.rxjersey.client.WebResourceFactory;
import net.winterly.rxjersey.client.inject.RemoteResolver;
import net.winterly.rxjersey.client.rxjava2.FlowableClientMethodInvoker;
import net.winterly.rxjersey.client.rxjava2.RxJerseyClientFeature;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;

public class RxJerseyTest extends JerseyTest {

    @Inject
    @Named(RemoteResolver.RX_JERSEY_CLIENT_NAME)
    private Provider<Client> clientProvider;

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig()
                .register(LocatorFeature.class)
                .register(JacksonFeature.class)
                .register(RxJerseyClientFeature.class)
                .register(ServerResource.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(RxJerseyTest.this).to(JerseyTest.class);
                    }
                });

        configure(resourceConfig);

        return resourceConfig;
    }

    protected void configure(ResourceConfig resourceConfig) {

    }

    @Override
    protected void configureClient(ClientConfig config) {

    }

    protected <T> T resource(Class<T> resource) {
        return WebResourceFactory.newResource(resource, target(), new FlowableClientMethodInvoker());
    }

    @Override
    protected Client getClient() {
        return clientProvider.get();
    }

    public static class LocatorFeature implements Feature {

        @Inject
        private InjectionManager injectionManager;

        @Inject
        private JerseyTest jerseyTest;

        @Override
        public boolean configure(FeatureContext context) {
            injectionManager.inject(jerseyTest);
            return true;
        }
    }

    @Path("/endpoint")
    public static class ServerResource {

        @GET
        @Path("json")
        @Produces(MediaType.APPLICATION_JSON)
        public Entity json(@QueryParam("message") String message) {
            return new Entity(message);
        }

        @GET
        @Path("echo")
        public String echo(@QueryParam("message") String message) {
            return message;
        }

        @GET
        @Path("empty")
        public String empty() {
            return null;
        }

        @GET
        @Path("error")
        public String error() {
            throw new BadRequestException();
        }
    }

    public static class Entity {

        public String message;

        public Entity() {
        }

        public Entity(String message) {
            this.message = message;
        }
    }
}
