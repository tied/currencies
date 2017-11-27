package com.mesilat.currencies;

import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

@Scanned
public class CurrencyMacroLegacy extends BaseMacro {
    private final TemplateRenderer renderer;
    private final CurrenciesService service;

    @Override
    public String execute(@SuppressWarnings("rawtypes") Map params, String body, RenderContext renderContext) throws MacroException {
        if (!params.containsKey("code")){
            return "";
        } else {
            Map<String,Object> map = new HashMap<>();
            map.put("code", params.get("code"));
            map.put("name", service.getName(params.get("code").toString()));
            return renderFromSoy("Mesilat.Currencies.Templates.currency.soy", map);
        }
    }
    @Override
    public RenderMode getBodyRenderMode() {
        return RenderMode.ALL;
    }
    @Override
    public boolean hasBody() {
        return false;
    }

    @Inject
    public CurrencyMacroLegacy(
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