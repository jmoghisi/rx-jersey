package net.winterly.rx.jersey.example;

import net.winterly.rx.jersey.client.inject.Remote;
import rx.Single;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/example/")
@Produces(MediaType.APPLICATION_JSON)
public class GithubResource {

    @Remote("https://api.github.com/")
    private GithubApi githubApi;

    @GET
    public Single<GithubRepository> getMessage() {
        return githubApi.getRepository("alex-shpak", "rx-jersey").toSingle();
    }


}