package co.whenthen.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.google.android.gms.wallet.WalletConstants;


public class Constants {

    /**
     * WhenThen API endpoint as defined in the docs
     * https://documentation.whenthen.com/api-reference#endpoints
     */
    public static final String API_ENDPOINT = "https://api.dev.whenthen.co/api/graphql";

    /**
     * Client token should be populated from a server-side request.
     * https://documentation.whenthen.com/api-reference#client-token
     */
    public static String CLIENT_TOKEN = "sk_test_FZjGdNNNhMQOqGeU7eqMHXcysl9YucgM";

    public static final String WEBVIEW_SDK_URL = "https://mobile-hosted-checkout.whenthen.com/";

    /**
     * The Id of the flow you want the payment to run against. You can find it in
     * https://app.whenthen.co/settings/developers
     */
    public static final String FLOW_ID = "6bbd2b09-0e8a-4682-874f-d1028425805e";

    public static final String PAYMENT_METHOD_GOOGLE_PAY = "GOOGLE_PAY";

    public static final String PAYMENT_METHOD_CARD = "CARD";

    public static final String CHECKOUT_LANGUAGE = "en";


    /**
     * The allowed networks to be requested from the GooglePay API. If the user has cards from networks not
     * specified here in their account, these will not be offered for them to choose in the popup.
     *
     * @value #SUPPORTED_NETWORKS
     */
    public static final List<String> SUPPORTED_NETWORKS = Arrays.asList(
            "AMEX",
            "DISCOVER",
            "JCB",
            "MASTERCARD",
            "VISA");

    /**
     * The Google Pay API may return cards on file on Google.com (PAN_ONLY) and/or a device token on
     * an Android device authenticated with a 3-D Secure cryptogram (CRYPTOGRAM_3DS).
     *
     * @value #SUPPORTED_METHODS
     */
    public static final List<String> SUPPORTED_METHODS = Arrays.asList(
            "PAN_ONLY",
            "CRYPTOGRAM_3DS");

    /**
     * Required by the GooglePay API, but not visible to the user.
     *
     * @value #COUNTRY_CODE Your local country
     */
    public static final String COUNTRY_CODE = "FI";

    /**
     * Required by the GooglePay API, but not visible to the user.
     *
     * @value #CURRENCY_CODE Your local currency
     */
    public static final String CURRENCY_CODE = "EUR";

    /**
     * Supported countries for shipping (use ISO 3166-1 alpha-2 country codes). Relevant only when
     * requesting a shipping address.
     *
     * @value #SHIPPING_SUPPORTED_COUNTRIES
     */
    public static final List<String> SHIPPING_SUPPORTED_COUNTRIES = Arrays.asList("US", "GB", "FI", "SE", "IE");

    /**
     * The name of your payment processor/gateway. Please refer to their documentation for more
     * information.
     *
     * @value #PAYMENT_GATEWAY_TOKENIZATION_NAME
     */
    public static final String PAYMENT_GATEWAY_TOKENIZATION_NAME = "whenthen";

    /**
     * Payment gateway merchantID.
     * For testing, use any string (must match what you've set when calling setupWallet API)
     * in https://documentation.whenthen.com/orchestrate/alternative-payment-methods/google-pay#configuring-whenThen-to-accept-googlepay
     */
    public static final String PAYMENT_GATEWAY_MERCHANT_ID = "test";


    /**
     * Custom parameters required by the processor/gateway.
     * In many cases, your processor / gateway will only require a gatewayMerchantId.
     * Please refer to your processor's documentation for more information. The number of parameters
     * required and their names vary depending on the processor.
     *
     * @value #PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS
     */
    public static final HashMap<String, String> PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS =
            new HashMap<String, String>() {{
                put("gateway", PAYMENT_GATEWAY_TOKENIZATION_NAME);
                put("gatewayMerchantId", PAYMENT_GATEWAY_MERCHANT_ID);
                // Your processor may require additional parameters.
            }};

    /**
     * Only used for {@code DIRECT} tokenization. Can be removed when using {@code PAYMENT_GATEWAY}
     * tokenization.
     *
     * @value #DIRECT_TOKENIZATION_PUBLIC_KEY
     */
    public static final String DIRECT_TOKENIZATION_PUBLIC_KEY = "REPLACE_ME";

    /**
     * Parameters required for {@code DIRECT} tokenization.
     * Only used for {@code DIRECT} tokenization. Can be removed when using {@code PAYMENT_GATEWAY}
     * tokenization.
     *
     * @value #DIRECT_TOKENIZATION_PARAMETERS
     */
    public static final HashMap<String, String> DIRECT_TOKENIZATION_PARAMETERS =
            new HashMap<String, String>() {{
                put("protocolVersion", "ECv2");
                put("publicKey", DIRECT_TOKENIZATION_PUBLIC_KEY);
            }};
}
