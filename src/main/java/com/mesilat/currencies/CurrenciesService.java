package com.mesilat.currencies;

import java.util.Map;

public interface CurrenciesService {
    String getName(String code);
    Map<String,String> find(String text);
    String getSymbol(String code);
    void registerReferenceData(Object seedDataService);
}