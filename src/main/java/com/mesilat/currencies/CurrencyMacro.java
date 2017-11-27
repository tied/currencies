package com.mesilat.currencies;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

@Scanned
public class CurrencyMacro implements Macro {
    private final TemplateRenderer renderer;
    private final CurrenciesService service;

    @Override
    public BodyType getBodyType() {
        return BodyType.NONE;
    }
    @Override
    public OutputType getOutputType() {
        return OutputType.INLINE;
    }
    @Override
    public String execute(Map params, String body, ConversionContext conversionContext) throws MacroExecutionException {
        if (!params.containsKey("code")){
            return "";
        } else {
            Map<String,Object> map = new HashMap<>();
            map.put("code", params.get("code"));
            map.put("name", service.getName(params.get("code").toString()));
            return renderFromSoy("Mesilat.Currencies.Templates.currency.soy", map);
        }
    }

    @Inject
    public CurrencyMacro(
        final @ComponentImport TemplateRenderer renderer,
        final CurrenciesService service
    ){
        this.renderer = renderer;
        this.service = service;
    }

    public String renderFromSoy(String soyTemplate, Map soyContext) {
        StringBuilder output = new StringBuilder();
        renderer.renderTo(output, "com.mesilat.currencies:resources", soyTemplate, soyContext);
        return output.toString();
    }
}