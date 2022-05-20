package co.whenthen.demo.model;

import java.util.Map;

public class DropInOptions {

    private String apiKey;

    private Long amount;

    private String flowId;

    private String currencyCode;

    private String orderId;

    private String intentId;

    private String payButton;

    private String customerId;

    private boolean isSavedCardCvcRequired;

    private String language;

    private Object theme;

    private Map<String, String> countries;

    private boolean hideBillingAddress;

    private boolean allowSaveCard;

    private Map<String, String> alternativePaymentMethods;

    //For required fields
    public DropInOptions(String apiKey, Long amount, String flowId, String currencyCode) {
        this.apiKey = apiKey;
        this.amount = amount;
        this.flowId = flowId;
        this.currencyCode = currencyCode;
    }

    public DropInOptions(String apiKey, Long amount, String flowId, String currencyCode, String orderId, String intentId, String payButton, String customerId, boolean isSavedCardCvcRequired, String language, Object theme, Map<String, String> countries, boolean hideBillingAddress, boolean allowSaveCard, Map<String, String> alternativePaymentMethods) {
        this.apiKey = apiKey;
        this.amount = amount;
        this.flowId = flowId;
        this.currencyCode = currencyCode;
        this.orderId = orderId;
        this.intentId = intentId;
        this.payButton = payButton;
        this.customerId = customerId;
        this.isSavedCardCvcRequired = isSavedCardCvcRequired;
        this.language = language;
        this.theme = theme;
        this.countries = countries;
        this.hideBillingAddress = hideBillingAddress;
        this.allowSaveCard = allowSaveCard;
        this.alternativePaymentMethods = alternativePaymentMethods;
    }

    private void setApiKey(String apiKey) {
        if(apiKey == null || "".equals(apiKey)){
            throw new IllegalArgumentException("apiKey cannot be empty or null");
        }
        this.apiKey = apiKey;
    }

    private void setAmount(Long amount) {
        if(amount == null || amount == 0){
            throw new IllegalArgumentException("amount cannot be 0 or null");
        }
        this.amount = amount;
    }

    private void setFlowId(String flowId) {
        if(flowId == null || "".equals(flowId)){
            throw new IllegalArgumentException("flowId cannot be empty or null");
        }
        this.flowId = flowId;
    }

    private void setCurrencyCode(String currencyCode) {
        if(currencyCode == null || "".equals(currencyCode)){
            throw new IllegalArgumentException("currencyCode cannot be empty or null");
        }
        this.currencyCode = currencyCode;
    }
}
