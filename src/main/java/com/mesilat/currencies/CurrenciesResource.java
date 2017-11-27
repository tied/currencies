package com.mesilat.currencies;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

@Scanned
@Path("/")
public class CurrenciesResource {
    private final String baseUrl;
    private final CurrenciesService service;

    @AnonymousAllowed
    @GET
    @Path("find")
    @Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
    public Response find(
        final @QueryParam("title")String title,
        final @QueryParam("max-results")Integer maxResults,
        final @QueryParam("filter")String filter
    ) {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        try {
            Map<String,String> currencies = service.find(filter);
            ObjectNode root = mapper.createObjectNode();

            ArrayNode result = mapper.createArrayNode();
            currencies.entrySet().stream().sorted((a,b)->{
                return a.getValue().compareTo(b.getValue());
            }).forEach(e->{
                if (
                       (filter == null || filter.isEmpty() || e.getValue().toUpperCase().contains(filter.toUpperCase()))
                    && (maxResults == null || result.size() < maxResults)
                ){
                    result.add(createCurrencyObject(mapper, e.getKey(), e.getValue()));
                }
            });

            root.put("totalSize", 0);
            root.put("result", mapper.createArrayNode());
            ArrayNode group = mapper.createArrayNode();
            root.put("group", group);
            ObjectNode content = mapper.createObjectNode();
            group.add(content);
            content.put("name", "content");
            content.put("result", result);

            mapper.writerWithDefaultPrettyPrinter().writeValue(sw, root);
            return Response.ok(sw.toString()).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    private ObjectNode createCurrencyObject(ObjectMapper mapper, String id, String title){
        ObjectNode node = mapper.createObjectNode();
        node.put("id", id);
        node.put("title", title);
        node.put("link", createLinkObject(mapper, id));
        node.put("thumbnailLink", createThumbnailObject(mapper, id));
        return node;
    }
    private ObjectNode createThumbnailObject(ObjectMapper mapper, String id){
        ObjectNode node = mapper.createObjectNode();
        node.put("href", String.format("%s/download/resources/com.mesilat.currencies/images/%s.png", baseUrl, id));
        node.put("type", "image/png");
        node.put("rel", "thumbnail");
        return node;
    }
    private ArrayNode createLinkObject(ObjectMapper mapper, String id){
        ArrayNode arr = mapper.createArrayNode();
        ObjectNode node = mapper.createObjectNode();
        node.put("href", String.format("%s/rest/currencies/1.0/get/%s", baseUrl, id));
        node.put("rel", "self");
        arr.add(node);
        return arr;
    }

    @Inject
    public CurrenciesResource(final @ComponentImport SettingsManager settingsManager, CurrenciesService service){
        baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
        this.service = service;
    }
}