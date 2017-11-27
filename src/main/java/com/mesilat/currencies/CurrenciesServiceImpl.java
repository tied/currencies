package com.mesilat.currencies;

import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.User;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

@ExportAsService({CurrenciesService.class})
@Named("com.mesilat:currencies:service")
public class CurrenciesServiceImpl implements InitializingBean, DisposableBean, CurrenciesService {
    private static final String SEED_DATA_SERVICE_NAME = "com.mesilat:lov-placeholder:seedDataService";
    private static final Logger LOGGER = LoggerFactory.getLogger("com.mesilat.currencies");

    private final LocaleManager localeManager;
    private final Map<Locale,Map<String,String>> currencies = new HashMap<>();
    private final Properties symbols = new Properties();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (symbols.isEmpty()){
            readSymbols();
        }
        try {
            Object seedDataService = ContainerManager.getComponent(SEED_DATA_SERVICE_NAME);
            registerReferenceData(seedDataService);
        } catch(Throwable ex){
            LOGGER.debug(String.format("No %s -- can't register with lov-placeholder", SEED_DATA_SERVICE_NAME));
        }
    }
    @Override
    public void destroy() throws Exception {
    }

    @Override
    public String getName(String code) {
        User user = AuthenticatedUserThreadLocal.get();
        Locale locale = user == null? localeManager.getSiteDefaultLocale(): localeManager.getLocale(user);
        return getCurrencies(locale).get(code);
    }
    @Override
    public Map<String,String> find(String text) {
        User user = AuthenticatedUserThreadLocal.get();
        Locale locale = user == null? localeManager.getSiteDefaultLocale(): localeManager.getLocale(user);
        if (text == null){
            return getCurrencies(locale);
        } else {
            Map<String,String> map = new HashMap<>();
            for (Entry<String,String> e : getCurrencies(locale).entrySet()){
                if (e.getValue().toUpperCase().contains(text.toUpperCase())){
                    map.put(e.getKey(), e.getValue());
                }
            }
            return map;
        }
    }
    @Override
    public String getSymbol(String code){
        return symbols.getProperty(code);
    }
    private Map<String,String> getCurrencies(Locale locale){
        if (!currencies.containsKey(locale)){
            Map<String,String> _currencies = new HashMap<>();
            readResource("/i18n/en.properties", _currencies);
            String localeSuffix = locale.toString();
            readResource(String.format("/i18n/%s.properties", localeSuffix.substring(0, 2)), _currencies);
            readResource(String.format("/i18n/%s.properties", localeSuffix), _currencies);
            currencies.put(locale, _currencies);
        }
        return currencies.get(locale);
    }
    private void readResource(String path, Map<String,String> currencies){
        try (
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);
        ){
            Properties props = new Properties();
            props.load(in);
            props.entrySet().forEach((e)->{
                currencies.put(e.getKey().toString(), e.getValue().toString());
            });
        } catch (Throwable ignore) {
            //LOGGER.warn("Failed to read resource: " + path);
        }
    }
    private void readSymbols(){
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("/i18n/currency_symbols.properties")){
            symbols.clear();
            symbols.load(in);
        } catch (Throwable ex) {
            LOGGER.warn("Failed to read currency symbols", ex);
        }
    }
    @Override
    public void registerReferenceData(Object seedDataService) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("/data/currencies.txt")){
            String data = IOUtils.toString(in, StandardCharsets.UTF_8.name());
            registerReferenceData(seedDataService, "CURR", "Currencies", 1, data);
            LOGGER.debug("Registered data source 'currencies'");
        } catch (Throwable ex) {
            LOGGER.warn("Failed to register data source 'currencies'", ex);
        }
    }
    private void registerReferenceData(Object seedDataService, String code, String name, int type, String data) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = seedDataService.getClass().getMethod("putReferenceData", String.class, String.class, int.class, String.class);
        m.invoke(seedDataService, code, name, type, data);
    }


    @Inject
    public CurrenciesServiceImpl(
        final @ComponentImport LocaleManager localeManager
    ){
        this.localeManager = localeManager;
    }

    Pattern P = Pattern.compile("^([A-Z]{3}) = (.+)$");
}