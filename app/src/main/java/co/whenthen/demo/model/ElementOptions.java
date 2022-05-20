package co.whenthen.demo.model;

import android.webkit.JavascriptInterface;

import java.util.Map;

public class ElementOptions {

    private String apiKey;

    private String customerId;

    private boolean isSavedCardCvcRequired;

    private String language;

    private Object theme;

    private Map<String, String> countries;

    private boolean hideBillingAddress;

    private boolean allowSaveCard;

    private Map<String, String> alternativePaymentMethods;

    public ElementOptions(String apiKey, String customerId, boolean isSavedCardCvcRequired, String language, Object theme, Map<String, String> countries, boolean hideBillingAddress, boolean allowSaveCard, Map<String, String> alternativePaymentMethods) {
        this.apiKey = apiKey;
        this.customerId = customerId;
        this.isSavedCardCvcRequired = isSavedCardCvcRequired;
        this.language = language;
        this.theme = theme;
        this.countries = countries;
        this.hideBillingAddress = hideBillingAddress;
        this.allowSaveCard = allowSaveCard;
        this.alternativePaymentMethods = alternativePaymentMethods;
    }

    @JavascriptInterface
    public String getApiKey() {
        return apiKey;
    }

    @JavascriptInterface
    public String getCustomerId() {
        return customerId;
    }

    @JavascriptInterface
    public boolean isSavedCardCvcRequired() {
        return isSavedCardCvcRequired;
    }

    @JavascriptInterface
    public String getLanguage() {
        return language;
    }

    @JavascriptInterface
    public Object getTheme() {
        return theme;
    }

    @JavascriptInterface
    public Map<String, String> getCountries() {
        return countries;
    }

    @JavascriptInterface
    public boolean isHideBillingAddress() {
        return hideBillingAddress;
    }

    @JavascriptInterface
    public boolean isAllowSaveCard() {
        return allowSaveCard;
    }

    @JavascriptInterface
    public Map<String, String> getAlternativePaymentMethods() {
        return alternativePaymentMethods;
    }
}
